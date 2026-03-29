package com.example.video_platform.service;

import com.example.video_platform.config.RabbitMQConfig;
import com.example.video_platform.entity.TranscodeMessage;
import com.example.video_platform.mapper.MediaFileMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class VideoProcessService {

    @Autowired
    private MediaFileMapper mediaFileMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private Random random = new Random();

    /**
     * 发送转码任务
     */
    public void sendTranscodeTask(Long fileId, String fileMd5, String filePath) {
        TranscodeMessage message = new TranscodeMessage();
        message.setFileId(fileId);
        message.setFileMd5(fileMd5);
        message.setFilePath(filePath);
        message.setTimestamp(System.currentTimeMillis());

        rabbitTemplate.convertAndSend(RabbitMQConfig.VIDEO_EXCHANGE, "video.transcode", message);
        System.out.println("发送转码任务: " + fileMd5);
    }

    /**
     * 发送审核任务
     */
    public void sendAuditTask(Long fileId, String fileMd5, String filePath) {
        TranscodeMessage message = new TranscodeMessage();
        message.setFileId(fileId);
        message.setFileMd5(fileMd5);
        message.setFilePath(filePath);
        message.setTimestamp(System.currentTimeMillis());

        rabbitTemplate.convertAndSend(RabbitMQConfig.VIDEO_EXCHANGE, "video.audit", message);
        System.out.println("发送审核任务: " + fileMd5);
    }

    /**
     * 消费转码任务
     */
    @RabbitListener(queues = RabbitMQConfig.TRANSCODE_QUEUE)
    public void handleTranscode(TranscodeMessage message) {
        try {
            System.out.println("开始转码: " + message.getFileMd5());

            // 模拟转码过程
            for (int i = 0; i <= 100; i += 10) {
                Thread.sleep(500);
                mediaFileMapper.updateTranscodeProgress(message.getFileMd5(), i);
                System.out.println("转码进度: " + i + "%");
            }

            // 转码成功，更新结果
            mediaFileMapper.updateTranscodeResult(message.getFileMd5(), "转码成功");
            mediaFileMapper.updateStatus(message.getFileMd5(), 2, null); // 状态改为待审核

            // 转码完成后发送审核任务
            sendAuditTask(message.getFileId(), message.getFileMd5(), message.getFilePath());

            System.out.println("转码完成: " + message.getFileMd5());

        } catch (Exception e) {
            e.printStackTrace();
            mediaFileMapper.updateTranscodeResult(message.getFileMd5(), "转码失败: " + e.getMessage());
            // 发送到死信队列
            rabbitTemplate.convertAndSend(RabbitMQConfig.VIDEO_EXCHANGE, "video.dead.letter", message);
        }
    }

    /**
     * 消费审核任务
     */
    @RabbitListener(queues = RabbitMQConfig.AUDIT_QUEUE)
    public void handleAudit(TranscodeMessage message) {
        try {
            System.out.println("开始审核: " + message.getFileMd5());

            // 模拟审核过程
            Thread.sleep(2000);

            // 随机决定审核结果（90%通过，10%拒绝）
            int auditResult = random.nextInt(100) < 90 ? 1 : 2;

            mediaFileMapper.updateAuditStatus(message.getFileMd5(), auditResult);

            if (auditResult == 1) {
                mediaFileMapper.updateStatus(message.getFileMd5(), 3, null);
                System.out.println("审核通过: " + message.getFileMd5());
            } else {
                mediaFileMapper.updateStatus(message.getFileMd5(), 4, null);
                System.out.println("审核拒绝: " + message.getFileMd5());
            }

        } catch (Exception e) {
            e.printStackTrace();
            mediaFileMapper.updateAuditStatus(message.getFileMd5(), 0);
            rabbitTemplate.convertAndSend(RabbitMQConfig.VIDEO_EXCHANGE, "video.dead.letter", message);
        }
    }
}