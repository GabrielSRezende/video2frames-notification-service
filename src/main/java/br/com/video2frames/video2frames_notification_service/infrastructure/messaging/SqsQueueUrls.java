package br.com.video2frames.video2frames_notification_service.infrastructure.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SqsQueueUrls {

    private final SqsClient sqsClient;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public SqsQueueUrls(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public String resolve(String queueName) {
        return cache.computeIfAbsent(queueName, name -> {
            log.info("Resolvendo URL da fila {} junto ao SQS", name);
            return sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(name).build()).queueUrl();
        });
    }
}
