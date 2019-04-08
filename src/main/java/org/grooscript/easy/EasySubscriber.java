package org.grooscript.easy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

/**
 * JFL 2019-04-07
 */
public class EasySubscriber<T> implements Flow.Subscriber<T> {

    private Collection<Flow.Subscription> subscriptions = new ArrayList<>();
    private final Consumer<T> consumer;

    public EasySubscriber(Consumer<T> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscriptions.add(getEasySubscription(subscription).setAction(this.consumer));
    }

    @Override
    public void onNext(T item) {
        this.subscriptions.forEach(subscription -> {
            getEasySubscription(subscription).doWith(item).cancel();
        });
    }

    @Override
    public void onError(Throwable throwable) {
        this.subscriptions.forEach(Flow.Subscription::cancel);
    }

    @Override
    public void onComplete() {
        this.subscriptions.forEach(Flow.Subscription::cancel);
    }

    @SuppressWarnings("unchecked")
    private EasySubscription<T> getEasySubscription(Flow.Subscription subscription) {
        return (EasySubscription<T>)subscription;
    }
}
