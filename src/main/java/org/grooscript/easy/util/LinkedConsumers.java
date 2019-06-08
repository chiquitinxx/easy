package org.grooscript.easy.util;

import java.util.function.Consumer;

/**
 * JFL 2019-06-07
 */
public class LinkedConsumers<T> {

    private Element<Consumer<T>> current;

    public LinkedConsumers() {
        this.current = null;
    }

    public synchronized LinkedConsumers<T> add(Consumer<T> value) {
        this.current = new Element<>(value, this.current);
        return this;
    }

    public synchronized void process(T value) {
        processOnNext(value, this.current);
    }

    private void processOnNext(T value, Element<Consumer<T>> next) {
        if (next != null) {
            next.getValue().accept(value);
            processOnNext(value, next.getNext());
        }
    }
}
