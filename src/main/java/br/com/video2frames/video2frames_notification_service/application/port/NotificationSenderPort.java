package br.com.video2frames.video2frames_notification_service.application.port;

import br.com.video2frames.video2frames_notification_service.domain.model.FailureNotification;

/**
 * O caso de uso não sabe que o canal é e-mail — só que existe um jeito de
 * notificar. Trocar por SMS, push ou Slack depois é só escrever outro
 * adapter implementando este port; nada em application/ ou domain/ muda.
 */
public interface NotificationSenderPort {

    void send(FailureNotification notification);
}
