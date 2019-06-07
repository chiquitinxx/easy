package org.grooscript.easy.util;

/**
 * JFL 2019-06-07
 */
public class Element<T> {
    private final T value;
    private final Element<T> next;

    public Element(T value, Element<T> next) {
        this.value = value;
        this.next = next;
    }

    public T getValue() {
        return this.value;
    }

    public Element<T> getNext() {
        return this.next;
    }

}
