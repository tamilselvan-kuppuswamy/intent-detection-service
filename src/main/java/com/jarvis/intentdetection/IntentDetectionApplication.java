package com.jarvis.intentdetection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class IntentDetectionApplication {
  public static void main(String[] args) {
    log.info("Starting IntentDetectionApplication...");
    SpringApplication.run(IntentDetectionApplication.class, args);
    log.info("IntentDetectionApplication started successfully.");
  }
}
