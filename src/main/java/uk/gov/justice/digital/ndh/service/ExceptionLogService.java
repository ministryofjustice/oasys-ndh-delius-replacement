package uk.gov.justice.digital.ndh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.ndh.jpa.entity.ExceptionLog;
import uk.gov.justice.digital.ndh.jpa.repository.ExceptionLogRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static uk.gov.justice.digital.ndh.ThatsNotMyNDH.NDH_PROCESS_NAME;

@Service
public class ExceptionLogService {

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
                .processName(NDH_PROCESS_NAME)
                .description(description)
                .payload(body)
                .build());
    }

    public void logMappingFail(Long codeType, String sourceVal) {
        exceptionLogRepository.save(ExceptionLog
                .builder()
                .excDatetime(Timestamp.valueOf(LocalDateTime.now()))
                // TODO: FInd out where and how to write code & value
                .build());

    }
}
