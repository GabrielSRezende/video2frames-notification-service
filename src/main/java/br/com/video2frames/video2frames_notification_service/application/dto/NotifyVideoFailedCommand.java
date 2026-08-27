package br.com.video2frames.video2frames_notification_service.application.dto;

import java.util.UUID;

public record NotifyVideoFailedCommand(UUID videoId, String ownerEmail, String reason) {
}
