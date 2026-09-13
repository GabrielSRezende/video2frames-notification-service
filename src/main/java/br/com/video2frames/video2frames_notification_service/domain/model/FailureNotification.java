package br.com.video2frames.video2frames_notification_service.domain.model;

import br.com.video2frames.video2frames_notification_service.domain.exception.InvalidNotificationException;

import java.util.UUID;

public final class FailureNotification {

    private final UUID videoId;
    private final String recipientEmail;
    private final String reason;

    private FailureNotification(UUID videoId, String recipientEmail, String reason) {
        this.videoId = videoId;
        this.recipientEmail = recipientEmail;
        this.reason = reason;
    }

    public static FailureNotification of(UUID videoId, String recipientEmail, String reason) {
        if (videoId == null) {
            throw new InvalidNotificationException("videoId é obrigatório");
        }
        if (recipientEmail == null || recipientEmail.isBlank()) {
            throw new InvalidNotificationException("recipientEmail é obrigatório");
        }
        return new FailureNotification(videoId, recipientEmail, blankToDefault(reason));
    }

    private static String blankToDefault(String reason) {
        return (reason == null || reason.isBlank()) ? "Falha no processamento do vídeo" : reason;
    }

    public UUID getVideoId() {
        return videoId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getReason() {
        return reason;
    }
}
