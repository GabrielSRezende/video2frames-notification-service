package br.com.video2frames.video2frames_notification_service.infrastructure.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SqsQueueUrlsTest {

    @Mock
    private SqsClient sqsClient;

    private SqsQueueUrls queueUrls;

    @BeforeEach
    void setUp() {
        queueUrls = new SqsQueueUrls(sqsClient);
    }

    @Test
    void resolve_primeiraChamada_buscaUrlNoSqsClient() {
        when(sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName("video-failed-queue").build()))
                .thenReturn(GetQueueUrlResponse.builder().queueUrl("https://sqs/video-failed-queue").build());

        String url = queueUrls.resolve("video-failed-queue");

        assertThat(url).isEqualTo("https://sqs/video-failed-queue");
    }

    @Test
    void resolve_chamadasRepetidasParaAMesmaFila_usaCacheENaoConsultaOSqsClientNovamente() {
        when(sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName("video-failed-queue").build()))
                .thenReturn(GetQueueUrlResponse.builder().queueUrl("https://sqs/video-failed-queue").build());

        queueUrls.resolve("video-failed-queue");
        queueUrls.resolve("video-failed-queue");
        String url = queueUrls.resolve("video-failed-queue");

        assertThat(url).isEqualTo("https://sqs/video-failed-queue");
        verify(sqsClient, times(1)).getQueueUrl(GetQueueUrlRequest.builder().queueName("video-failed-queue").build());
    }
}
