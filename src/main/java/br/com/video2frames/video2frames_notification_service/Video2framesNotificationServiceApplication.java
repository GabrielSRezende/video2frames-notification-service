package br.com.video2frames.video2frames_notification_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Video2framesNotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(Video2framesNotificationServiceApplication.class, args);
	}

}
