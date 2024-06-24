package com.example.demo.Service;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
@Service
public class KafkaConsumerService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @KafkaListener(topics = "p1-presion", groupId = "group_id")
    public void consume(String message) {
        System.out.println("Received message: " + message);
        // Forward the message to the WebSocket endpoint
        messagingTemplate.convertAndSend("/topic/pressure", message);
    }
}

