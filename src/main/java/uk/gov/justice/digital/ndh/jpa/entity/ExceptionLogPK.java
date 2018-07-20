package uk.gov.justice.digital.ndh.jpa.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

public class ExceptionLogPK implements Serializable {
    private Integer excSeq;
    private Timestamp excDatetime;

    @Column(name = "EXC_SEQ")
    @Id
    public Integer getExcSeq() {
        return excSeq;
    }

    public void setExcSeq(Integer excSeq) {
        this.excSeq = excSeq;
    }

    @Column(name = "EXC_DATETIME")
    @Id
    public Timestamp getExcDatetime() {
        return excDatetime;
    }

    public void setExcDatetime(Timestamp excDatetime) {
        this.excDatetime = excDatetime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExceptionLogPK that = (ExceptionLogPK) o;
        return Objects.equals(excSeq, that.excSeq) &&
                Objects.equals(excDatetime, that.excDatetime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(excSeq, excDatetime);
    }
}
