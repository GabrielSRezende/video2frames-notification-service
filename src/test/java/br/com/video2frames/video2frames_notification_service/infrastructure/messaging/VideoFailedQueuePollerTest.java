package br.com.video2frames.video2frames_notification_service.infrastructure.messaging;

import br.com.video2frames.video2frames_notification_service.application.dto.NotifyVideoFailedCommand;
import br.com.video2frames.video2frames_notification_service.application.usecase.NotifyVideoFailedUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoFailedQueuePollerTest {

    @Mock
    private SqsClient sqsClient;

    @Mock
    private SqsQueueUrls queueUrls;

    @Mock
    private NotifyVideoFailedUseCase notifyVideoFailedUseCase;

    private VideoFailedQueuePoller poller;

    @BeforeEach
    void setUp() {
        poller = new VideoFailedQueuePoller(sqsClient, queueUrls, notifyVideoFailedUseCase, "video-failed-queue", 5);

        when(queueUrls.resolve("video-failed-queue")).thenReturn("https://sqs/video-failed-queue");
    }

    @Test
    void poll_quandoHaMensagemValida_notificaEDeletaAMensagem() {
        UUID videoId = UUID.randomUUID();
        Message message = Message.builder()
                .body("{\"videoId\":\"" + videoId + "\",\"ownerEmail\":\"gabriel@video2frames.com\",\"reason\":\"erro\"}")
                .receiptHandle("receipt-1")
                .build();
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(message).build());

        poller.poll();

        ArgumentCaptor<NotifyVideoFailedCommand> captor = ArgumentCaptor.forClass(NotifyVideoFailedCommand.class);
        verify(notifyVideoFailedUseCase).execute(captor.capture());
        assertThat(captor.getValue().videoId()).isEqualTo(videoId);
        assertThat(captor.getValue().ownerEmail()).isEqualTo("gabriel@video2frames.com");
        assertThat(captor.getValue().reason()).isEqualTo("erro");

        ArgumentCaptor<DeleteMessageRequest> deleteCaptor = ArgumentCaptor.forClass(DeleteMessageRequest.class);
        verify(sqsClient).deleteMessage(deleteCaptor.capture());
        assertThat(deleteCaptor.getValue().queueUrl()).isEqualTo("https://sqs/video-failed-queue");
        assertThat(deleteCaptor.getValue().receiptHandle()).isEqualTo("receipt-1");
    }

    @Test
    void poll_quandoUseCaseLancaExcecao_naoDeletaAMensagemParaPermitirNovaTentativa() {
        Message message = Message.builder()
                .body("{\"videoId\":\"" + UUID.randomUUID() + "\",\"ownerEmail\":\"gabriel@video2frames.com\",\"reason\":\"erro\"}")
                .receiptHandle("receipt-1")
                .build();
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(message).build());
        org.mockito.Mockito.doThrow(new RuntimeException("smtp fora do ar"))
                .when(notifyVideoFailedUseCase).execute(any(NotifyVideoFailedCommand.class));

        poller.poll();

        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void poll_quandoMensagemComJsonInvalido_naoDeletaAMensagem() {
        Message message = Message.builder()
                .body("json-invalido")
                .receiptHandle("receipt-1")
                .build();
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(message).build());

        poller.poll();

        verify(notifyVideoFailedUseCase, never()).execute(any(NotifyVideoFailedCommand.class));
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void poll_semMensagens_naoNotificaNemDeleta() {
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(java.util.List.of()).build());

        poller.poll();

        verify(notifyVideoFailedUseCase, never()).execute(any(NotifyVideoFailedCommand.class));
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void poll_comMultiplasMensagens_processaTodas() {
        Message message1 = Message.builder()
                .body("{\"videoId\":\"" + UUID.randomUUID() + "\",\"ownerEmail\":\"a@video2frames.com\",\"reason\":\"erro-1\"}")
                .receiptHandle("receipt-1")
                .build();
        Message message2 = Message.builder()
                .body("{\"videoId\":\"" + UUID.randomUUID() + "\",\"ownerEmail\":\"b@video2frames.com\",\"reason\":\"erro-2\"}")
                .receiptHandle("receipt-2")
                .build();
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(message1, message2).build());

        poller.poll();

        verify(notifyVideoFailedUseCase, times(2)).execute(any(NotifyVideoFailedCommand.class));
        verify(sqsClient, times(2)).deleteMessage(any(DeleteMessageRequest.class));
    }
}
