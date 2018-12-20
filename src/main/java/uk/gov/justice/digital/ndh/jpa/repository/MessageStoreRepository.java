package uk.gov.justice.digital.ndh.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.ndh.jpa.entity.MsgStore;
import uk.gov.justice.digital.ndh.jpa.entity.MsgStorePK;

import java.util.Optional;

@Repository
public interface MessageStoreRepository extends JpaRepository<MsgStore, MsgStorePK> {

    Optional<MsgStore> findFirstByProcessNameOrderByMsgStoreSeqDesc(String processName);
}
