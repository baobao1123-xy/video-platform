package com.example.video_platform.entity;

import java.time.LocalDateTime;

public class MediaFile {
    private Long id;
    private String fileMd5;
    private String fileName;
    private Long fileSize;
    private String bucketName;
    private String objectName;
    private Integer status; // 0:上传中,1:转码中,2:审核中,3:成功,4:失败
    private Integer transcodeProgress; // 转码进度 0-100
    private String transcodeResult; // 转码结果
    private Integer auditStatus; // 审核状态 0:待审核,1:通过,2:拒绝
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileMd5() { return fileMd5; }
    public void setFileMd5(String fileMd5) { this.fileMd5 = fileMd5; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getBucketName() { return bucketName; }
    public void setBucketName(String bucketName) { this.bucketName = bucketName; }

    public String getObjectName() { return objectName; }
    public void setObjectName(String objectName) { this.objectName = objectName; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getTranscodeProgress() { return transcodeProgress; }
    public void setTranscodeProgress(Integer transcodeProgress) { this.transcodeProgress = transcodeProgress; }

    public String getTranscodeResult() { return transcodeResult; }
    public void setTranscodeResult(String transcodeResult) { this.transcodeResult = transcodeResult; }

    public Integer getAuditStatus() { return auditStatus; }
    public void setAuditStatus(Integer auditStatus) { this.auditStatus = auditStatus; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}