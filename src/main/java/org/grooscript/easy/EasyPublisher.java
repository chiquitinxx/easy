package org.grooscript.easy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

/**
 * JFL 2019-04-07
 */
public class EasyPublisher<T> implements Flow.Publisher<T> {

    private Collection<Flow.Subscriber<? super T>> subscribers = new ArrayList<>();
    private boolean closed = false;

    @Override
    public void subscribe(Flow.Subscriber<? super T> subscriber) {
        this.subscribers.add(subscriber);
        subscriber.onSubscribe(new EasySubscription());
    }

    public void submit(T item) {
        CompletableFuture.runAsync(() ->
            this.subscribers.forEach(subscriber -> subscriber.onNext(item))
        ).thenRun(() -> this.closed = true);
    }

    public boolean isClosed() {
        return this.closed;
    }

    public boolean hasSubscribers() {
        return this.subscribers != null && this.subscribers.size() > 0;
    }
}
