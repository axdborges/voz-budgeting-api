package com.axdborges.voz.budgeting.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionIdTest {

    @Test
    void shouldExposeTheGivenValue() {
        UUID uuid = UUID.randomUUID();
        var id = new TransactionId(uuid);

        assertThat(id.value()).isEqualTo(uuid);
    }

    @Test
    void shouldBeEqualWhenValueIsTheSame() {
        UUID uuid = UUID.randomUUID();

        assertThat(new TransactionId(uuid)).isEqualTo(new TransactionId(uuid));
    }

    @Test
    void shouldNotBeEqualWhenValueDiffers() {
        assertThat(new TransactionId(UUID.randomUUID())).isNotEqualTo(new TransactionId(UUID.randomUUID()));
    }

    @Test
    void shouldGenerateARandomId() {
        assertThat(TransactionId.generate()).isNotEqualTo(TransactionId.generate());
    }

    @Test
    void shouldParseAValidUuidString() {
        UUID uuid = UUID.randomUUID();

        assertThat(TransactionId.of(uuid.toString())).isEqualTo(new TransactionId(uuid));
    }

    @Test
    void shouldRejectAnInvalidUuidString() {
        assertThatThrownBy(() -> TransactionId.of("nao-e-um-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
