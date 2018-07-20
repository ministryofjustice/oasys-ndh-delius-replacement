package uk.gov.justice.digital.ndh.jpa.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

public class MsgStorePK implements Serializable {
    private Integer msgStoreSeq;
    private Timestamp storeDatetime;

    @Column(name = "MSG_STORE_SEQ")
    @Id
    public Integer getMsgStoreSeq() {
        return msgStoreSeq;
    }

    public void setMsgStoreSeq(Integer msgStoreSeq) {
        this.msgStoreSeq = msgStoreSeq;
    }

    @Column(name = "STORE_DATETIME")
    @Id
    public Timestamp getStoreDatetime() {
        return storeDatetime;
    }

    public void setStoreDatetime(Timestamp storeDatetime) {
        this.storeDatetime = storeDatetime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MsgStorePK that = (MsgStorePK) o;
        return Objects.equals(msgStoreSeq, that.msgStoreSeq) &&
                Objects.equals(storeDatetime, that.storeDatetime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(msgStoreSeq, storeDatetime);
    }
}
