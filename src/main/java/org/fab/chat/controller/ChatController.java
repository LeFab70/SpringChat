package org.fab.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fab.chat.dto.ChatMessageDto;
import org.fab.chat.enums.MessageType;
import org.fab.chat.exception.InvalidMessageException;
import org.fab.chat.exception.UsernameAlreadyInUseException;
import org.fab.chat.services.ActiveUserService;
import org.fab.chat.services.ChatMessageService;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final ActiveUserService activeUserService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessageDto sendMessage(@Valid @Payload ChatMessageDto chatMessageDto) {
        if (chatMessageDto.content() == null || chatMessageDto.content().isBlank()) {
            throw new InvalidMessageException("Le message ne peut pas être vide.");
        }
        return chatMessageService.save(chatMessageDto);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Valid @Payload ChatMessageDto chatMessageDto, SimpMessageHeaderAccessor headerAccessor) {

        String username = chatMessageDto.sender();

        if (!activeUserService.tryAdd(username)) {
            throw new UsernameAlreadyInUseException(username);
        }

        // Add username in web socket session
        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("username", username);

        // Send the existing conversation privately to the joining user before broadcasting their arrival,
        // so their own "joined" event isn't duplicated between history and the live broadcast.
        sendHistoryTo(headerAccessor.getSessionId());

        ChatMessageDto saved = chatMessageService.save(chatMessageDto.withType(MessageType.JOIN));

        messagingTemplate.convertAndSend("/topic/public", saved);
    }

    private void sendHistoryTo(String sessionId) {
        List<ChatMessageDto> history = chatMessageService.getRecentHistory();
        messagingTemplate.convertAndSendToUser(sessionId, "/queue/history", history, createHeaders(sessionId));
    }

    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }

}
