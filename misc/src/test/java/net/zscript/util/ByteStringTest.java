package net.zscript.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import net.zscript.util.ByteString.ImmutableByteString;

class ByteStringTest {
    @Test
    public void shouldAppendBytes() throws IOException {
        ByteString.ByteStringBuilder strBuilder = ByteString.builder().appendByte(0x61).appendByte('b');
        ByteString                   str        = strBuilder.build();
        assertThat(str.toByteArray()).containsExactly('a', 'b');

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        assertThat(str.writeTo(byteArrayOutputStream)).isSameAs(str);

        ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
        strBuilder.writeTo(byteArrayOutputStream2);
        assertThat(strBuilder.toByteArray()).containsExactly('a', 'b');
        assertThat(byteArrayOutputStream2.toByteArray()).containsExactly('a', 'b');

        assertThatThrownBy(() -> ByteString.builder().appendByte('Z').appendByte(257).toByteArray())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldWriteToByteArrayNumbers() {
        assertThat(ByteString.builder().appendByte('Z').appendNumeric(0x1a2b).toByteArray())
                .containsExactly('Z', '1', 'a', '2', 'b');
    }

    @Test
    public void shouldOmitNumberZero() {
        assertThat(ByteString.builder().appendByte('Z').appendNumeric(0).toByteArray())
                .containsExactly('Z');
    }

    @Test
    public void shouldWriteNumberZero() {
        assertThat(ByteString.builder().appendByte('Z').appendNumericKeepZero(0).toByteArray())
                .containsExactly('Z', '0');
    }

    @Test
    public void shouldThrowOnOutOfRangeNumericValue() {
        assertThatThrownBy(() -> ByteString.builder().appendByte('Z').appendNumeric(123456).toByteArray())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldWriteHex() {
        ByteString.ByteStringBuilder builder = ByteString.builder();
        assertThat(builder.appendHexPair(0).toByteArray()).containsExactly('0', '0');
        assertThat(builder.appendHexPair(0x3d).toByteArray()).containsExactly('0', '0', '3', 'd');
        assertThat(builder.appendHexPair(0xff).toByteArray()).containsExactly('0', '0', '3', 'd', 'f', 'f');

        assertThatThrownBy(() -> builder.appendHexPair(123456))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(ImmutableByteString.class).verify();
    }

    @Test
    public void toStringContract() {
        assertThat(ByteString.builder().appendByte('Z').appendNumeric(0x12).build().toString())
                .isEqualTo("ImmutableByteString[Z12]");
    }
}
