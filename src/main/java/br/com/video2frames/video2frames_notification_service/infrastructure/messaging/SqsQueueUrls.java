package br.com.video2frames.video2frames_notification_service.infrastructure.messaging;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SqsQueueUrls {

    private final SqsClient sqsClient;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public SqsQueueUrls(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public String resolve(String queueName) {
        return cache.computeIfAbsent(queueName, name ->
                sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(name).build()).queueUrl());
    }
}
