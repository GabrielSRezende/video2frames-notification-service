package br.com.video2frames.video2frames_notification_service.infrastructure.messaging;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoProcessedQueuePollerTest {

    @Mock
    private SqsClient sqsClient;

    @Mock
    private SqsQueueUrls queueUrls;

    private VideoProcessedQueuePoller poller;

    @BeforeEach
    void setUp() {
        poller = new VideoProcessedQueuePoller(sqsClient, queueUrls, "video-processed-queue", 5);

        when(queueUrls.resolve("video-processed-queue")).thenReturn("https://sqs/video-processed-queue");
    }

    @Test
    void poll_quandoHaMensagem_deletaAMensagemAposProcessar() {
        Message message = Message.builder()
                .body("{\"videoId\":\"video-1\",\"ownerEmail\":\"gabriel@video2frames.com\",\"zipKey\":\"zip-1\",\"frameCount\":10}")
                .receiptHandle("receipt-1")
                .build();
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(message).build());

        poller.poll();

        ArgumentCaptor<DeleteMessageRequest> deleteCaptor = ArgumentCaptor.forClass(DeleteMessageRequest.class);
        verify(sqsClient).deleteMessage(deleteCaptor.capture());
        assertThat(deleteCaptor.getValue().queueUrl()).isEqualTo("https://sqs/video-processed-queue");
        assertThat(deleteCaptor.getValue().receiptHandle()).isEqualTo("receipt-1");
    }

    @Test
    void poll_semMensagens_naoDeletaNada() {
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(java.util.List.of()).build());

        poller.poll();

        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void poll_comMultiplasMensagens_deletaTodas() {
        Message message1 = Message.builder()
                .body("{\"videoId\":\"video-1\",\"ownerEmail\":\"a@video2frames.com\",\"zipKey\":\"zip-1\",\"frameCount\":5}")
                .receiptHandle("receipt-1")
                .build();
        Message message2 = Message.builder()
                .body("{\"videoId\":\"video-2\",\"ownerEmail\":\"b@video2frames.com\",\"zipKey\":\"zip-2\",\"frameCount\":8}")
                .receiptHandle("receipt-2")
                .build();
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(message1, message2).build());

        poller.poll();

        verify(sqsClient, times(2)).deleteMessage(any(DeleteMessageRequest.class));
    }
}
