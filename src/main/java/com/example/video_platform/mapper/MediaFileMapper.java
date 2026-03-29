package com.example.video_platform.mapper;

import com.example.video_platform.entity.MediaFile;
import org.apache.ibatis.annotations.*;

@Mapper
public interface MediaFileMapper {

    @Insert("INSERT INTO media_file (file_md5, file_name, file_size, bucket_name, object_name, status) " +
            "VALUES (#{fileMd5}, #{fileName}, #{fileSize}, #{bucketName}, #{objectName}, #{status})")
    void insert(MediaFile mediaFile);

    @Select("SELECT * FROM media_file WHERE file_md5 = #{fileMd5}")
    MediaFile findByFileMd5(@Param("fileMd5") String fileMd5);

    @Update("UPDATE media_file SET status = #{status}, object_name = #{objectName} WHERE file_md5 = #{fileMd5}")
    void updateStatus(@Param("fileMd5") String fileMd5, @Param("status") Integer status, @Param("objectName") String objectName);

    // 添加这些方法

    @Update("UPDATE media_file SET transcode_progress = #{progress} WHERE file_md5 = #{fileMd5}")
    void updateTranscodeProgress(@Param("fileMd5") String fileMd5, @Param("progress") Integer progress);

    @Update("UPDATE media_file SET transcode_result = #{result} WHERE file_md5 = #{fileMd5}")
    void updateTranscodeResult(@Param("fileMd5") String fileMd5, @Param("result") String result);

    @Update("UPDATE media_file SET audit_status = #{status} WHERE file_md5 = #{fileMd5}")
    void updateAuditStatus(@Param("fileMd5") String fileMd5, @Param("status") Integer status);
}