package com.example.video_platform.controller;

import com.example.video_platform.service.ChunkUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/chunk")
public class ChunkUploadController {

    @Autowired
    private ChunkUploadService chunkUploadService;

    @GetMapping("/check")
    public String checkFile(@RequestParam String fileMd5) {
        return chunkUploadService.checkFileExists(fileMd5);
    }

    @PostMapping("/init")
    public String initUpload(@RequestParam String fileName,
                             @RequestParam String fileMd5,
                             @RequestParam long fileSize) throws Exception {
        return chunkUploadService.initUpload(fileName, fileMd5, fileSize);
    }

    @PostMapping("/upload")
    public String uploadChunk(@RequestParam String fileMd5,
                              @RequestParam int chunkIndex,
                              @RequestParam("file") MultipartFile chunk) throws Exception {
        return chunkUploadService.uploadChunk(fileMd5, chunkIndex, chunk);
    }

    @PostMapping("/complete")
    public String completeUpload(@RequestParam String fileMd5) throws Exception {
        return chunkUploadService.completeUpload(fileMd5);
    }

    @GetMapping("/chunks")
    public List<Integer> getUploadedChunks(@RequestParam String fileMd5) {
        return chunkUploadService.getUploadedChunks(fileMd5);
    }
}