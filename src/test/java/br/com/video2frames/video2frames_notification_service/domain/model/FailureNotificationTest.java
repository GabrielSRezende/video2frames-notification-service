package br.com.video2frames.video2frames_notification_service.domain.model;

import br.com.video2frames.video2frames_notification_service.domain.exception.InvalidNotificationException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FailureNotificationTest {

    @Test
    void of_comDadosValidos_criaNotificacaoComOsCamposInformados() {
        UUID videoId = UUID.randomUUID();

        FailureNotification notification = FailureNotification.of(videoId, "gabriel@video2frames.com", "arquivo corrompido");

        assertThat(notification.getVideoId()).isEqualTo(videoId);
        assertThat(notification.getRecipientEmail()).isEqualTo("gabriel@video2frames.com");
        assertThat(notification.getReason()).isEqualTo("arquivo corrompido");
    }

    @Test
    void of_quandoVideoIdNulo_lancaInvalidNotificationException() {
        assertThatThrownBy(() -> FailureNotification.of(null, "gabriel@video2frames.com", "erro"))
                .isInstanceOf(InvalidNotificationException.class)
                .hasMessage("videoId é obrigatório");
    }

    @Test
    void of_quandoRecipientEmailNulo_lancaInvalidNotificationException() {
        assertThatThrownBy(() -> FailureNotification.of(UUID.randomUUID(), null, "erro"))
                .isInstanceOf(InvalidNotificationException.class)
                .hasMessage("recipientEmail é obrigatório");
    }

    @Test
    void of_quandoRecipientEmailEmBranco_lancaInvalidNotificationException() {
        assertThatThrownBy(() -> FailureNotification.of(UUID.randomUUID(), "   ", "erro"))
                .isInstanceOf(InvalidNotificationException.class)
                .hasMessage("recipientEmail é obrigatório");
    }

    @Test
    void of_quandoReasonNulo_usaMensagemPadrao() {
        FailureNotification notification = FailureNotification.of(UUID.randomUUID(), "gabriel@video2frames.com", null);

        assertThat(notification.getReason()).isEqualTo("Falha no processamento do vídeo");
    }

    @Test
    void of_quandoReasonEmBranco_usaMensagemPadrao() {
        FailureNotification notification = FailureNotification.of(UUID.randomUUID(), "gabriel@video2frames.com", "   ");

        assertThat(notification.getReason()).isEqualTo("Falha no processamento do vídeo");
    }
}
