package ru.mit.spbau.java.lazy;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

/**
 * Created by: Egor Gorbunov
 * Date: 9/13/16
 * Email: egor-mailbox@ya.com
 */
public final class LazyFactory {

    private LazyFactory() {
    }

    public static <T> Lazy<T> createSingleThreadedLazy(Supplier<T> s) {
        return new Lazy<T>() {
            private T obj = null;
            private boolean isCached = false;

            @Override
            public T get() {
                if (!isCached) {
                    obj = s.get();
                    isCached = true;
                }
                return obj;
            }
        };
    }

    public static <T> Lazy<T> createLockingLazy(Supplier<T> s) {
        return new Lazy<T>() {
            private volatile T obj = null;
            private boolean isCached = false;

            @Override
            public T get() {
                if (!isCached) {
                    synchronized (this) {
                        if (!isCached) {
                            obj = s.get();
                            isCached = true;
                        }
                    }
                }
                return obj;
            }
        };
    }

    private static class LockFreeLazy<T> implements Lazy<T> {
        private volatile T obj;
        private volatile boolean isCached;
        private Supplier<T> supplier;
        private static final AtomicReferenceFieldUpdater<LockFreeLazy, Object> OBJ_UPDATER =
                AtomicReferenceFieldUpdater.newUpdater(LockFreeLazy.class, Object.class, "obj");

        LockFreeLazy(Supplier<T> s) {
            supplier = s;
        }

        @Override
        public T get() {
            while (!isCached) {
                if (OBJ_UPDATER.compareAndSet(this, null, supplier.get())) {
                    isCached = true;
                }
            }
            return obj;
        }
    }

    public static <T> Lazy<T> createLockFreeLazy(Supplier<T> s) {
        return new LockFreeLazy<>(s);
    }
}
