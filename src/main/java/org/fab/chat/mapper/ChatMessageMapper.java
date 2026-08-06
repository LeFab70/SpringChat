package org.fab.chat.mapper;

import org.fab.chat.dto.ChatMessageDto;
import org.fab.chat.entities.ChatMessage;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageMapper {

    public ChatMessage toEntity(ChatMessageDto dto) {
        return ChatMessage.builder()
                .sender(dto.getSender())
                .content(dto.getContent())
                .type(dto.getType())
                .timestamp(dto.getTimestamp())
                .build();
    }

    public ChatMessageDto toDto(ChatMessage entity) {
        return ChatMessageDto.builder()
                .sender(entity.getSender())
                .content(entity.getContent())
                .type(entity.getType())
                .timestamp(entity.getTimestamp())
                .build();
    }

}
