package br.com.video2frames.video2frames_notification_service.infrastructure.email;

import br.com.video2frames.video2frames_notification_service.application.port.NotificationSenderPort;
import br.com.video2frames.video2frames_notification_service.domain.exception.NotificationDeliveryException;
import br.com.video2frames.video2frames_notification_service.domain.model.FailureNotification;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Component
public class SmtpNotificationSender implements NotificationSenderPort {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String fromName;

    public SmtpNotificationSender(
            JavaMailSender mailSender,
            @Value("${app.notification.from-address}") String fromAddress,
            @Value("${app.notification.from-name}") String fromName) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
    }

    @Override
    public void send(FailureNotification notification) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setFrom(fromAddress, fromName);
            helper.setTo(notification.getRecipientEmail());
            helper.setSubject("Video2Frames — falha ao processar seu vídeo");
            helper.setText(buildBody(notification), false);

            mailSender.send(message);
        } catch (jakarta.mail.MessagingException | UnsupportedEncodingException e) {
            throw new NotificationDeliveryException(
                    "Não foi possível enviar o e-mail de notificação para " + notification.getRecipientEmail(), e);
        }
    }

    private String buildBody(FailureNotification notification) {
        return """
                Olá,

                Não foi possível concluir o processamento do seu vídeo (id: %s).

                Motivo: %s

                Você pode tentar enviar o vídeo novamente pela plataforma.

                — Video2Frames
                """.formatted(notification.getVideoId(), notification.getReason());
    }
}
