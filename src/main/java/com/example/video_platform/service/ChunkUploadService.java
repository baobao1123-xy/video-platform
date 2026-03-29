package com.example.video_platform.service;

import com.example.video_platform.entity.FileChunkRecord;
import com.example.video_platform.entity.MediaFile;
import com.example.video_platform.mapper.FileChunkRecordMapper;
import com.example.video_platform.mapper.MediaFileMapper;
import com.example.video_platform.utils.MD5Utils;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class ChunkUploadService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private FileChunkRecordMapper chunkRecordMapper;

    @Autowired
    private MediaFileMapper mediaFileMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private VideoProcessService videoProcessService;

    @Value("${minio.bucket-name}")
    private String bucketName;

    private static final String TEMP_DIR = "D:\\minio-temp\\";
    private static final String REDIS_CHUNK_PREFIX = "chunk:";

    public String initUpload(String fileName, String fileMd5, long fileSize) throws Exception {
        MediaFile existing = mediaFileMapper.findByFileMd5(fileMd5);
        if (existing != null && existing.getStatus() == 3) {
            return "秒传成功|" + existing.getObjectName();
        }

        File tempDir = new File(TEMP_DIR);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        String redisKey = REDIS_CHUNK_PREFIX + fileMd5;
        Map<String, Object> uploadInfo = new HashMap<>();
        uploadInfo.put("fileName", fileName);
        uploadInfo.put("fileSize", fileSize);
        uploadInfo.put("totalChunks", (int) Math.ceil((double) fileSize / (5 * 1024 * 1024)));
        uploadInfo.put("uploadedChunks", new ArrayList<Integer>());

        redisTemplate.opsForHash().putAll(redisKey, uploadInfo);
        redisTemplate.expire(redisKey, 7, TimeUnit.DAYS);

        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileMd5(fileMd5);
        mediaFile.setFileName(fileName);
        mediaFile.setFileSize(fileSize);
        mediaFile.setBucketName(bucketName);
        mediaFile.setObjectName(fileMd5 + "_" + fileName);
        mediaFile.setStatus(0);
        mediaFileMapper.insert(mediaFile);

        return "新建|" + fileMd5;
    }

    public String uploadChunk(String fileMd5, int chunkIndex, MultipartFile chunk) throws Exception {
        String redisKey = REDIS_CHUNK_PREFIX + fileMd5;

        if (!Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            return "请先初始化上传";
        }

        String chunkFileName = TEMP_DIR + fileMd5 + "_" + chunkIndex + ".part";
        File chunkFile = new File(chunkFileName);
        chunk.transferTo(chunkFile);

        redisTemplate.opsForHash().put(redisKey, "chunk_" + chunkIndex, "uploaded");

        Long uploadedCount = redisTemplate.opsForHash().size(redisKey) - 3;
        Integer totalChunks = (Integer) redisTemplate.opsForHash().get(redisKey, "totalChunks");

        return "分片 " + (chunkIndex + 1) + " 上传成功，已完成 " + uploadedCount + "/" + totalChunks;
    }

    public String completeUpload(String fileMd5) throws Exception {
        String redisKey = REDIS_CHUNK_PREFIX + fileMd5;

        String fileName = (String) redisTemplate.opsForHash().get(redisKey, "fileName");
        Integer totalChunks = (Integer) redisTemplate.opsForHash().get(redisKey, "totalChunks");

        String finalFileName = TEMP_DIR + fileMd5 + "_" + fileName;
        File finalFile = new File(finalFileName);

        try (FileOutputStream fos = new FileOutputStream(finalFile)) {
            for (int i = 0; i < totalChunks; i++) {
                String chunkFileName = TEMP_DIR + fileMd5 + "_" + i + ".part";
                File chunkFile = new File(chunkFileName);

                if (!chunkFile.exists()) {
                    throw new Exception("分片 " + i + " 不存在");
                }

                Files.copy(chunkFile.toPath(), fos);
                chunkFile.delete();
            }
        }

        String objectName = fileMd5 + "_" + fileName;
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(Files.newInputStream(finalFile.toPath()), finalFile.length(), -1)
                        .contentType("application/octet-stream")
                        .build()
        );

        finalFile.delete();

        MediaFile mediaFile = mediaFileMapper.findByFileMd5(fileMd5);
        mediaFileMapper.updateStatus(fileMd5, 1, objectName);

        videoProcessService.sendTranscodeTask(mediaFile.getId(), fileMd5, finalFileName);

        redisTemplate.delete(redisKey);

        return "http://localhost:9000/" + bucketName + "/" + objectName;
    }

    public List<Integer> getUploadedChunks(String fileMd5) {
        String redisKey = REDIS_CHUNK_PREFIX + fileMd5;
        List<Integer> uploadedChunks = new ArrayList<>();

        if (!Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            return uploadedChunks;
        }

        Map<Object, Object> entries = redisTemplate.opsForHash().entries(redisKey);
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            String key = entry.getKey().toString();
            if (key.startsWith("chunk_")) {
                int chunkIndex = Integer.parseInt(key.substring(6));
                uploadedChunks.add(chunkIndex);
            }
        }

        Collections.sort(uploadedChunks);
        return uploadedChunks;
    }

    public String checkFileExists(String fileMd5) {
        MediaFile existing = mediaFileMapper.findByFileMd5(fileMd5);
        if (existing != null && existing.getStatus() == 3) {
            return "已存在|" + "http://localhost:9000/" + bucketName + "/" + existing.getObjectName();
        }
        return "不存在";
    }
}