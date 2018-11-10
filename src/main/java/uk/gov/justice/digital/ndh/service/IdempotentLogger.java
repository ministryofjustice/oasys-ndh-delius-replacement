package uk.gov.justice.digital.ndh.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Optional;

import static uk.gov.justice.digital.ndh.ThatsNotMyNDH.ASSESSMENT_PROCESS;

@Component
@Slf4j
public class IdempotentLogger {
    private final ExceptionLogService exceptionLogService;
    private final MessageStoreService messageStoreService;

    @Autowired
    public IdempotentLogger(ExceptionLogService exceptionLogService, MessageStoreService messageStoreService) {
        this.exceptionLogService = exceptionLogService;
        this.messageStoreService = messageStoreService;
    }

    public void errorIdempotent(String correlationId, Optional<String> maybeSoapXml, Message message, String description) {
        try {
            if (!message.getJMSRedelivered()) {
                exceptionLogService.logFault(maybeSoapXml.get(), correlationId, description);
            }
        } catch (JMSException e) {
            log.error(e.getMessage());
            exceptionLogService.logFault(maybeSoapXml.get(), correlationId, e.getMessage());
        }
    }

    public void logIdempotent(Message message, String xml, String correlationId, String offenderId, MessageStoreService.ProcStates procState) {
        try {
            if (!message.getJMSRedelivered()) {
                messageStoreService.writeMessage(
                        xml,
                        correlationId,
                        offenderId,
                        ASSESSMENT_PROCESS,
                        procState);
            }
        } catch (JMSException e) {
            log.error(e.getMessage());
        }
    }
}
