package org.fab.chat.services;

import org.fab.chat.dto.ChatMessageDto;

import java.util.List;

public interface ChatMessageService {

    ChatMessageDto save(ChatMessageDto chatMessageDto);

    List<ChatMessageDto> getRecentHistory();

}
