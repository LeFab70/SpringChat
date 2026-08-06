package org.fab.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.With;
import org.fab.chat.enums.MessageType;

import java.time.LocalDateTime;

@Builder
@With
public record ChatMessageDto(

        @NotBlank(message = "Le pseudo est requis.")
        @Size(max = 50, message = "Le pseudo ne doit pas dépasser 50 caractères.")
        String sender,

        @Size(max = 1000, message = "Le message ne doit pas dépasser 1000 caractères.")
        String content,

        MessageType type,
        LocalDateTime timestamp
) {
}
