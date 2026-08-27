package br.com.video2frames.video2frames_notification_service.infrastructure.email;

import br.com.video2frames.video2frames_notification_service.domain.exception.NotificationDeliveryException;
import br.com.video2frames.video2frames_notification_service.domain.model.FailureNotification;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmtpNotificationSenderTest {

    @Mock
    private JavaMailSender mailSender;

    private SmtpNotificationSender sender;

    @BeforeEach
    void setUp() {
        sender = new SmtpNotificationSender(mailSender, "naoresponda@video2frames.com", "Video2Frames");
    }

    @Test
    void send_comNotificacaoValida_enviaEmailComOsDadosCorretos() throws Exception {
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        UUID videoId = UUID.randomUUID();
        FailureNotification notification = FailureNotification.of(videoId, "gabriel@video2frames.com", "arquivo corrompido");

        sender.send(notification);

        verify(mailSender).send(mimeMessage);
        assertThat(mimeMessage.getSubject()).isEqualTo("Video2Frames — falha ao processar seu vídeo");
        assertThat(mimeMessage.getAllRecipients()[0].toString()).isEqualTo("gabriel@video2frames.com");
        assertThat(mimeMessage.getFrom()[0].toString()).contains("naoresponda@video2frames.com");
        assertThat((String) mimeMessage.getContent()).contains(videoId.toString()).contains("arquivo corrompido");
    }

    @Test
    void send_quandoEnderecoDeDestinoEInvalido_lancaNotificationDeliveryException() {
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        FailureNotification notification = FailureNotification.of(
                UUID.randomUUID(), "endereço com espaço@video2frames.com", "erro");

        assertThatThrownBy(() -> sender.send(notification))
                .isInstanceOf(NotificationDeliveryException.class)
                .hasMessageContaining("endereço com espaço@video2frames.com")
                .hasCauseInstanceOf(jakarta.mail.MessagingException.class);
    }
}
