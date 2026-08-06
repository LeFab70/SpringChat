package org.fab.chat.services;

import lombok.RequiredArgsConstructor;
import org.fab.chat.entities.ChatMessage;
import org.fab.chat.repositories.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    @Override
    public ChatMessage save(ChatMessage chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now());
        return chatMessageRepository.save(chatMessage);
    }

    @Override
    public List<ChatMessage> getRecentHistory() {
        List<ChatMessage> history = chatMessageRepository.findTop50ByOrderByIdDesc();
        Collections.reverse(history);
        return history;
    }

}
