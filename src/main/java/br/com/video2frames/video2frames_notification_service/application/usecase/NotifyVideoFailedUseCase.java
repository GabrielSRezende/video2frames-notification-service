package br.com.video2frames.video2frames_notification_service.application.usecase;

import br.com.video2frames.video2frames_notification_service.application.dto.NotifyVideoFailedCommand;
import br.com.video2frames.video2frames_notification_service.application.port.NotificationSenderPort;
import br.com.video2frames.video2frames_notification_service.domain.model.FailureNotification;
import org.springframework.stereotype.Component;

@Component
public class NotifyVideoFailedUseCase {

    private final NotificationSenderPort notificationSenderPort;

    public NotifyVideoFailedUseCase(NotificationSenderPort notificationSenderPort) {
        this.notificationSenderPort = notificationSenderPort;
    }

    public void execute(NotifyVideoFailedCommand command) {
        FailureNotification notification = FailureNotification.of(
                command.videoId(), command.ownerEmail(), command.reason());

        notificationSenderPort.send(notification);
    }
}
