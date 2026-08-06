package org.fab.chat.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fab.chat.dto.ChatMessageDto;
import org.fab.chat.services.ActiveUserService;
import org.fab.chat.services.ChatMessageService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final ActiveUserService activeUserService;

    @EventListener //EventListener annotation is used to mark a method as an event listener for a specific event type.
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        log.info("A web socket connection was disconnected");
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("username");
        if (username != null) {
            log.info("User Disconnected : {}", username);
            activeUserService.remove(username);
            var chatMessageDto = ChatMessageDto.builder()
                    .type(org.fab.chat.enums.MessageType.LEAVE)
                    .sender(username)
                    .build();
            ChatMessageDto saved = chatMessageService.save(chatMessageDto);
            messagingTemplate.convertAndSend("/topic/public", saved);
        }
    }
}
