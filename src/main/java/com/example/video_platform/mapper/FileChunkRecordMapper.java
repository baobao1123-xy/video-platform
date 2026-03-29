package com.example.video_platform.mapper;

import com.example.video_platform.entity.FileChunkRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FileChunkRecordMapper {

    @Insert("INSERT INTO file_chunk_record (file_md5, upload_id, chunk_index, chunk_md5, status, expire_time) " +
            "VALUES (#{fileMd5}, #{uploadId}, #{chunkIndex}, #{chunkMd5}, #{status}, #{expireTime})")
    void insert(FileChunkRecord record);

    @Select("SELECT * FROM file_chunk_record WHERE file_md5 = #{fileMd5} AND upload_id = #{uploadId}")
    List<FileChunkRecord> findByFileMd5AndUploadId(@Param("fileMd5") String fileMd5, @Param("uploadId") String uploadId);

    @Select("SELECT * FROM file_chunk_record WHERE file_md5 = #{fileMd5} AND upload_id = #{uploadId} AND status = 1")
    List<FileChunkRecord> findUploadedChunks(@Param("fileMd5") String fileMd5, @Param("uploadId") String uploadId);

    @Update("UPDATE file_chunk_record SET status = 1 WHERE file_md5 = #{fileMd5} AND upload_id = #{uploadId} AND chunk_index = #{chunkIndex}")
    void updateChunkStatus(@Param("fileMd5") String fileMd5, @Param("uploadId") String uploadId, @Param("chunkIndex") Integer chunkIndex);

    @Delete("DELETE FROM file_chunk_record WHERE file_md5 = #{fileMd5} AND upload_id = #{uploadId}")
    void deleteByFileMd5AndUploadId(@Param("fileMd5") String fileMd5, @Param("uploadId") String uploadId);
}