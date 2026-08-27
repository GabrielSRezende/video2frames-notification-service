package br.com.video2frames.video2frames_notification_service.application.usecase;

import br.com.video2frames.video2frames_notification_service.application.dto.NotifyVideoFailedCommand;
import br.com.video2frames.video2frames_notification_service.application.port.NotificationSenderPort;
import br.com.video2frames.video2frames_notification_service.domain.exception.InvalidNotificationException;
import br.com.video2frames.video2frames_notification_service.domain.exception.NotificationDeliveryException;
import br.com.video2frames.video2frames_notification_service.domain.model.FailureNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotifyVideoFailedUseCaseTest {

    @Mock
    private NotificationSenderPort notificationSenderPort;

    private NotifyVideoFailedUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new NotifyVideoFailedUseCase(notificationSenderPort);
    }

    @Test
    void execute_quandoComandoValido_enviaNotificacaoParaOPort() {
        UUID videoId = UUID.randomUUID();
        NotifyVideoFailedCommand command = new NotifyVideoFailedCommand(videoId, "gabriel@video2frames.com", "arquivo corrompido");

        useCase.execute(command);

        ArgumentCaptor<FailureNotification> captor = ArgumentCaptor.forClass(FailureNotification.class);
        verify(notificationSenderPort).send(captor.capture());

        FailureNotification sent = captor.getValue();
        assertThat(sent.getVideoId()).isEqualTo(videoId);
        assertThat(sent.getRecipientEmail()).isEqualTo("gabriel@video2frames.com");
        assertThat(sent.getReason()).isEqualTo("arquivo corrompido");
    }

    @Test
    void execute_quandoReasonNaoInformado_enviaNotificacaoComMensagemPadrao() {
        UUID videoId = UUID.randomUUID();
        NotifyVideoFailedCommand command = new NotifyVideoFailedCommand(videoId, "gabriel@video2frames.com", null);

        useCase.execute(command);

        ArgumentCaptor<FailureNotification> captor = ArgumentCaptor.forClass(FailureNotification.class);
        verify(notificationSenderPort).send(captor.capture());

        assertThat(captor.getValue().getReason()).isEqualTo("Falha no processamento do vídeo");
    }

    @Test
    void execute_quandoVideoIdNulo_lancaInvalidNotificationExceptionSemChamarOPort() {
        NotifyVideoFailedCommand command = new NotifyVideoFailedCommand(null, "gabriel@video2frames.com", "erro");

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidNotificationException.class);

        verify(notificationSenderPort, never()).send(any(FailureNotification.class));
    }

    @Test
    void execute_quandoOwnerEmailEmBranco_lancaInvalidNotificationExceptionSemChamarOPort() {
        NotifyVideoFailedCommand command = new NotifyVideoFailedCommand(UUID.randomUUID(), "   ", "erro");

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidNotificationException.class);

        verify(notificationSenderPort, never()).send(any(FailureNotification.class));
    }

    @Test
    void execute_quandoPortLancaNotificationDeliveryException_propagaAExcecao() {
        NotifyVideoFailedCommand command = new NotifyVideoFailedCommand(UUID.randomUUID(), "gabriel@video2frames.com", "erro");

        doThrow(new NotificationDeliveryException("falha no envio", new RuntimeException("smtp fora do ar")))
                .when(notificationSenderPort).send(any(FailureNotification.class));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(NotificationDeliveryException.class)
                .hasMessage("falha no envio");
    }
}
