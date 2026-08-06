package org.fab.chat.services;

import lombok.RequiredArgsConstructor;
import org.fab.chat.dto.ChatMessageDto;
import org.fab.chat.entities.ChatMessage;
import org.fab.chat.mapper.ChatMessageMapper;
import org.fab.chat.repositories.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageMapper chatMessageMapper;

    @Override
    public ChatMessageDto save(ChatMessageDto chatMessageDto) {
        ChatMessageDto timestamped = chatMessageDto.withTimestamp(LocalDateTime.now());
        ChatMessage saved = chatMessageRepository.save(chatMessageMapper.toEntity(timestamped));
        return chatMessageMapper.toDto(saved);
    }

    @Override
    public List<ChatMessageDto> getRecentHistory() {
        List<ChatMessage> history = chatMessageRepository.findTop50ByOrderByIdDesc();
        Collections.reverse(history);
        return history.stream().map(chatMessageMapper::toDto).toList();
    }

}
