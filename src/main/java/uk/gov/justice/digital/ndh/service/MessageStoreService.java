package uk.gov.justice.digital.ndh.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.ndh.jpa.entity.MsgStore;
import uk.gov.justice.digital.ndh.jpa.repository.MessageStoreRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static uk.gov.justice.digital.ndh.ThatsNotMyNDH.NDH_PROCESS_NAME;

@Service
@Slf4j
public class MessageStoreService {


    private final MessageStoreRepository messageStoreRepository;

    @Autowired
    public MessageStoreService(MessageStoreRepository messageStoreRepository) {
        this.messageStoreRepository = messageStoreRepository;
    }

    @Transactional
    public void writeMessage(String body, String correlationId, ProcStates procState) {
        messageStoreRepository.save(MsgStore
                .builder()
                .correlationId(correlationId)
                .payload(body)
                .storeDatetime(Timestamp.valueOf(LocalDateTime.now()))
                .processName(NDH_PROCESS_NAME)
                .msgProcState(procState.getValue())
                .build());
    }

    public enum ProcStates {
        GLB_ProcState_InboundBeforeTransformation("1"),
        GLB_ProcState_InboundAfterTransformation("2"),
        GLB_ProcState_OutboundBeforeTransformation("3"),
        GLB_ProcState_OutboundAfterTransformation("4"),
        GLB_ProcState_Undefined("0");
        private String value;

        ProcStates(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }


    public enum ContentTypes {
        GLB_ContentTypeBinary("BIN"),
        GLB_ContentTypeText("TXT"),
        GLB_ContentTypeXML("XML");

        private String value;

        ContentTypes(String type) {
            this.value = type;
        }

        public String getValue() {
            return value;
        }
    }
}
