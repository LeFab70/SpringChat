package org.fab.chat.services;

import org.fab.chat.entities.ChatMessage;

import java.util.List;

public interface ChatMessageService {

    ChatMessage save(ChatMessage chatMessage);

    List<ChatMessage> getRecentHistory();

}
