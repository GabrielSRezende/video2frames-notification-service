package br.com.video2frames.video2frames_notification_service.infrastructure.messaging;

import br.com.video2frames.video2frames_notification_service.infrastructure.messaging.dto.VideoProcessedMessage;
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

/**
 * O requisito do hackathon só pede notificação EM CASO DE ERRO. Sucesso
 * não precisa virar e-mail (ninguém quer ser notificado toda vez que algo
 * deu certo) — mas o desenho de arquitetura pede que os eventos de sucesso
 * também sejam registrados, então este poller só loga. Se um dia quiser
 * avisar o usuário também no sucesso, é só criar um segundo use case e
 * chamá-lo aqui, do mesmo jeito que o VideoFailedQueuePoller chama o dele.
 */
@Component
public class VideoProcessedQueuePoller {

    private static final Logger log = LoggerFactory.getLogger(VideoProcessedQueuePoller.class);

    private final SqsClient sqsClient;
    private final SqsQueueUrls queueUrls;
    private final String queueName;
    private final int waitTimeSeconds;
    private final Gson gson = new Gson();

    public VideoProcessedQueuePoller(
            SqsClient sqsClient,
            SqsQueueUrls queueUrls,
            @Value("${aws.sqs.video-processed-queue}") String queueName,
            @Value("${aws.sqs.poll-wait-time-seconds}") int waitTimeSeconds) {
        this.sqsClient = sqsClient;
        this.queueUrls = queueUrls;
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
            var payload = gson.fromJson(message.body(), VideoProcessedMessage.class);
            log.info(
                    "Vídeo {} processado com sucesso para {} ({} frames, zip: {})",
                    payload.videoId(), payload.ownerEmail(), payload.frameCount(), payload.zipKey());

            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build());
        }
    }
}
