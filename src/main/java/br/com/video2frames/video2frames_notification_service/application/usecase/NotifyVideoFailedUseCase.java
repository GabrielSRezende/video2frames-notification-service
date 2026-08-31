package br.com.video2frames.video2frames_notification_service.application.usecase;

import br.com.video2frames.video2frames_notification_service.application.dto.NotifyVideoFailedCommand;
import br.com.video2frames.video2frames_notification_service.application.port.NotificationSenderPort;
import br.com.video2frames.video2frames_notification_service.domain.exception.InvalidNotificationException;
import br.com.video2frames.video2frames_notification_service.domain.model.FailureNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotifyVideoFailedUseCase {

    private final NotificationSenderPort notificationSenderPort;

    public NotifyVideoFailedUseCase(NotificationSenderPort notificationSenderPort) {
        this.notificationSenderPort = notificationSenderPort;
    }

    public void execute(NotifyVideoFailedCommand command) {
        log.info("Processando notificação de falha para o vídeo {}", command.videoId());

        FailureNotification notification;
        try {
            notification = FailureNotification.of(
                    command.videoId(), command.ownerEmail(), command.reason());
        } catch (InvalidNotificationException e) {
            log.warn("Notificação de falha inválida para o vídeo {}: {}", command.videoId(), e.getMessage());
            throw e;
        }

        log.info("Enviando e-mail de falha para {}", notification.getRecipientEmail());
        notificationSenderPort.send(notification);
        log.info("E-mail de falha enviado com sucesso para o vídeo {}", notification.getVideoId());
    }
}
