package org.fab.chat.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
@Slf4j
public class StompExceptionHandler {

    @MessageExceptionHandler(UsernameAlreadyInUseException.class)
    @SendToUser(value = "/queue/errors", broadcast = false)
    public ChatErrorPayload handleUsernameAlreadyInUse(UsernameAlreadyInUseException exception) {
        log.info("Rejected join attempt: {}", exception.getMessage());
        return new ChatErrorPayload("USERNAME_TAKEN", exception.getMessage());
    }

}
