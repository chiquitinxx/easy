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

    public synchronized void processAllAndEmpty(T value) {
        if (this.current != null) {
            this.current.getValue().accept(value);
            this.current = this.current.getNext();
            this.processAllAndEmpty(value);
        }
    }
}
