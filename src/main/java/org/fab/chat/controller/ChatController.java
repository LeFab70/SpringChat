package org.fab.chat.controller;

import lombok.RequiredArgsConstructor;
import org.fab.chat.entities.ChatMessage;
import org.fab.chat.enums.MessageType;
import org.fab.chat.exception.UsernameAlreadyInUseException;
import org.fab.chat.repository.ChatMessageRepository;
import org.fab.chat.service.ActiveUserRegistry;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final ActiveUserRegistry activeUserRegistry;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now());
        return chatMessageRepository.save(chatMessage);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {

        String username = chatMessage.getSender();

        if (!activeUserRegistry.tryAdd(username)) {
            throw new UsernameAlreadyInUseException(username);
        }

        // Add username in web socket session
        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("username", username);

        // Send the existing conversation privately to the joining user before broadcasting their arrival,
        // so their own "joined" event isn't duplicated between history and the live broadcast.
        sendHistoryTo(headerAccessor.getSessionId());

        chatMessage.setType(MessageType.JOIN);
        chatMessage.setTimestamp(LocalDateTime.now());
        ChatMessage saved = chatMessageRepository.save(chatMessage);

        messagingTemplate.convertAndSend("/topic/public", saved);
    }

    private void sendHistoryTo(String sessionId) {
        List<ChatMessage> history = chatMessageRepository.findTop50ByOrderByIdDesc();
        Collections.reverse(history);
        messagingTemplate.convertAndSendToUser(sessionId, "/queue/history", history, createHeaders(sessionId));
    }

    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }

}
