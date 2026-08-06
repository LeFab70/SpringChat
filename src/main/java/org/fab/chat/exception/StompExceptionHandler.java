package org.fab.chat.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
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

    @MessageExceptionHandler(InvalidMessageException.class)
    @SendToUser(value = "/queue/errors", broadcast = false)
    public ChatErrorPayload handleInvalidMessage(InvalidMessageException exception) {
        log.info("Rejected message: {}", exception.getMessage());
        return new ChatErrorPayload("INVALID_MESSAGE", exception.getMessage());
    }

    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    @SendToUser(value = "/queue/errors", broadcast = false)
    public ChatErrorPayload handleValidationError(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Message invalide.");
        log.info("Validation failed: {}", message);
        return new ChatErrorPayload("VALIDATION_ERROR", message);
    }

}
