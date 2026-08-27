package br.com.video2frames.video2frames_notification_service.infrastructure.messaging;

import br.com.video2frames.video2frames_notification_service.application.dto.NotifyVideoFailedCommand;
import br.com.video2frames.video2frames_notification_service.application.usecase.NotifyVideoFailedUseCase;
import br.com.video2frames.video2frames_notification_service.infrastructure.messaging.dto.VideoFailedMessage;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.UUID;

/**
 * Diferente do poller de video-uploaded no processing-service: aqui, se o
 * envio do e-mail falhar (ex: SMTP fora do ar), a mensagem NÃO é deletada
 * — fica para nova tentativa. Faz sentido porque uma falha de SMTP costuma
 * ser transitória (diferente de um vídeo corrompido, que falha sempre do
 * mesmo jeito).
 */
@Component
public class VideoFailedQueuePoller {

    private static final Logger log = LoggerFactory.getLogger(VideoFailedQueuePoller.class);

    private final SqsClient sqsClient;
    private final SqsQueueUrls queueUrls;
    private final NotifyVideoFailedUseCase notifyVideoFailedUseCase;
    private final String queueName;
    private final int waitTimeSeconds;
    private final Gson gson = new Gson();

    public VideoFailedQueuePoller(
            SqsClient sqsClient,
            SqsQueueUrls queueUrls,
            NotifyVideoFailedUseCase notifyVideoFailedUseCase,
            @Value("${aws.sqs.video-failed-queue}") String queueName,
            @Value("${aws.sqs.poll-wait-time-seconds}") int waitTimeSeconds) {
        this.sqsClient = sqsClient;
        this.queueUrls = queueUrls;
        this.notifyVideoFailedUseCase = notifyVideoFailedUseCase;
        this.queueName = queueName;
        this.waitTimeSeconds = waitTimeSeconds;
    }

    @Scheduled(fixedDelay = 1000)
    public void poll() {
        String queueUrl = queueUrls.resolve(queueName);

        var response = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(waitTimeSeconds)
                .build());

        for (Message message : response.messages()) {
            try {
                var payload = gson.fromJson(message.body(), VideoFailedMessage.class);

                notifyVideoFailedUseCase.execute(new NotifyVideoFailedCommand(
                        UUID.fromString(payload.videoId()), payload.ownerEmail(), payload.reason()));

                sqsClient.deleteMessage(DeleteMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .receiptHandle(message.receiptHandle())
                        .build());
            } catch (Exception e) {
                log.error("Falha ao notificar erro de processamento, deixando para nova tentativa", e);
            }
        }
    }
}
