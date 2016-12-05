package ru.spbau.mit.java.tracker;

/**
 * Simple interface for classes, who can produce ids =)
 * Every producer instance must be able to produce unique ids.
 *
 * @param <T> type of id to produce
 */
public interface IdProducer<T> {

    /**
     * Method, which must produce unique ids if it is
     * invoked on the same instance.
     * If it is not possible to produce next unique id
     * {@link NoFreeIdsLeftException} exception must be thrown
     *
     * @return new id
     */
    T nextId();
}
