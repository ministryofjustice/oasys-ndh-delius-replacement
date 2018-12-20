package uk.gov.justice.digital.ndh.service;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class SequenceTest {

    @Test
    public void canInitialise() {
        assertThat(new Sequence(5L, 10L).nextVal()).isEqualTo("05");
    }

    @Test
    public void doesIncrement() {
        final Sequence sequence = new Sequence(0L, 999999L);
        sequence.nextVal();
        assertThat(sequence.nextVal()).isEqualTo("000001");
    }

    @Test
    public void canMax() {
        final Sequence sequence = new Sequence(999999L, 999999L);
        assertThat(sequence.nextVal()).isEqualTo("999999");
    }

    @Test
    public void doesWrap() {
        final Sequence sequence = new Sequence(999999L, 999999L);
        sequence.nextVal();
        assertThat(sequence.nextVal()).isEqualTo("000000");
    }

}