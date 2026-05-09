package domain.shared;

import java.util.concurrent.atomic.AtomicInteger;

public final class IdSequence {
    private final AtomicInteger counter = new AtomicInteger(1);
    public int next() { return counter.getAndIncrement(); }
    public int peek() { return counter.get(); }
    public void seedAtLeast(int n) {
        counter.updateAndGet(current -> Math.max(current, n));
    }
}
