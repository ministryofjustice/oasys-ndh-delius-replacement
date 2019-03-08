package uk.gov.justice.digital.ndh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CorrelationService {
    final Sequence xtagSequence;

    @Autowired
    public CorrelationService(Sequence xtagSequence) {
        this.xtagSequence = xtagSequence;
    }

    String nextCorrelationId() {
        return "NOMISHNOMIS" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                xtagSequence.nextVal();
    }
}