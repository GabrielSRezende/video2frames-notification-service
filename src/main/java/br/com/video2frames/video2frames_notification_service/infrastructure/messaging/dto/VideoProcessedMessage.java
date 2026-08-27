package br.com.video2frames.video2frames_notification_service.infrastructure.messaging.dto;

public record VideoProcessedMessage(String videoId, String ownerEmail, String zipKey, int frameCount) {
}
