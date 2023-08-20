package net.zscript.util;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class OptIteratorTest {
    final OptIterator<Integer> simpleListIterator = OptIterator.of(List.of(1, 2, 3));

    @Test
    void shouldIterateList() {
        assertThat(simpleListIterator.next()).isPresent().get().isEqualTo(1);
        assertThat(simpleListIterator.next()).isPresent().get().isEqualTo(2);
        assertThat(simpleListIterator.next()).isPresent().get().isEqualTo(3);
        assertThat(simpleListIterator.next()).isEmpty();
    }

    @Test
    void shouldIterateEmptyList() {
        OptIterator<Integer> oi = OptIterator.of(List.of());
        assertThat(oi.next()).isEmpty();
    }

    @Test
    void shouldCreateStream() {
        List<Integer> filteredList = simpleListIterator.stream().filter(n -> n != 2).collect(toList());
        assertThat(filteredList).isEqualTo(List.of(1, 3));
    }

    @Test
    void shouldExecuteForEach() {
        AtomicInteger total = new AtomicInteger(); // using this as a mutable int
        simpleListIterator.forEach(n -> total.addAndGet(n));
        assertThat(total.get()).isEqualTo(6);
    }

}
