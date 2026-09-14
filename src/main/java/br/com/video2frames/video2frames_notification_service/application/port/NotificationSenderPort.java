package br.com.video2frames.video2frames_notification_service.application.port;

import br.com.video2frames.video2frames_notification_service.domain.model.FailureNotification;

public interface NotificationSenderPort {

    void send(FailureNotification notification);
}
