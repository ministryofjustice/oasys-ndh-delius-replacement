package uk.gov.justice.digital.ndh.service;

import lombok.Synchronized;

public class Sequence {

    private final Long modulus;
    private final int width;
    private Long count;

    public Sequence(Long beginAt, Long max) {
        this.count = beginAt;
        this.modulus = max + 1;
        this.width = max.toString().length();
    }

    @Synchronized
    public String nextVal() {
        final long next = count++ % modulus;

        return String.format("%0" + width + "d", next);
    }
}
