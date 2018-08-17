package uk.gov.justice.digital.ndh.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.ndh.jpa.entity.ExceptionLog;
import uk.gov.justice.digital.ndh.jpa.repository.ExceptionLogRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static uk.gov.justice.digital.ndh.ThatsNotMyNDH.ASSESSMENT_PROCESS;

@Service
@Slf4j
public class ExceptionLogService {

    public static final String MAPPING_EXCEPTION = "MAPPING EXCEPTION";
    public static final String PROCESS_EXCEPTION = "PROCESS EXCEPTION";
    private final ExceptionLogRepository exceptionLogRepository;

    @Autowired
    public ExceptionLogService(ExceptionLogRepository exceptionLogRepository) {
        this.exceptionLogRepository = exceptionLogRepository;
    }


    public void logFault(String body, String correlationId, String description) {
        exceptionLogRepository.save(ExceptionLog
                .builder()
                .excDatetime(Timestamp.valueOf(LocalDateTime.now()))
                .correlationId(correlationId)
                .processName(ASSESSMENT_PROCESS)
                .description(description)
                .payload(body)
                .excCode(PROCESS_EXCEPTION)
                .build());
    }

    public void logMappingFail(Long codeType, String sourceVal, String context, String correlationId, String crnOrPnc) {
        exceptionLogRepository.save(ExceptionLog
                .builder()
                .excDatetime(Timestamp.valueOf(LocalDateTime.now()))
                .description("Failed mapping: CodeType : " + codeType + ", SourceVal : " + sourceVal + " to " + context)
                .correlationId(correlationId)
                .customId(crnOrPnc)
                .excCode(MAPPING_EXCEPTION)
                .build());

    }
}
