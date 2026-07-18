package com.axdborges.voz.budgeting.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionIdTest {

    @Test
    void shouldExposeTheGivenValue() {
        var id = new TransactionId("abc-123");

        assertThat(id.value()).isEqualTo("abc-123");
    }

    @Test
    void shouldBeEqualWhenValueIsTheSame() {
        assertThat(new TransactionId("abc-123")).isEqualTo(new TransactionId("abc-123"));
    }

    @Test
    void shouldNotBeEqualWhenValueDiffers() {
        assertThat(new TransactionId("abc-123")).isNotEqualTo(new TransactionId("xyz-999"));
    }
}
