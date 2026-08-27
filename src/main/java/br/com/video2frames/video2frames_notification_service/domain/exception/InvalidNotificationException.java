package br.com.video2frames.video2frames_notification_service.domain.exception;

public class InvalidNotificationException extends RuntimeException {
    public InvalidNotificationException(String message) {
        super(message);
    }
}
