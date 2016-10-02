package ru.mit.spbau.java.lazy;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Supplier;

/**
 * Created by: Egor Gorbunov
 * Date: 9/14/16
 * Email: egor-mailbox@ya.com
 */
public class LazyTest {

    private static class SimpleSupplier implements Supplier<Integer> {
        private int cnt = 0;

        @Override
        public Integer get() {
            cnt += 1;
            return new Integer(42);
        }

        int getCount() {
            return cnt;
        }
    }

    private static void createStartAndJoinAll(Runnable r, int n) throws InterruptedException {
        Thread[] ts = new Thread[n];
        for (int i = 0; i < n; ++i) {
            ts[i] = new Thread(r);
        }
        for (Thread t : ts) {
            t.start();
        }
        for (Thread t : ts) {
            t.join();
        }
    }


    @Test
    public void testSingleThreadedLazy() {
        SimpleSupplier s = new SimpleSupplier();
        Lazy<Integer> l = LazyFactory.createSingleThreadedLazy(s);
        Object obj = l.get();
        Assert.assertTrue(obj == l.get());
        Assert.assertTrue(obj == l.get());
        Assert.assertTrue(obj == l.get());
        Assert.assertEquals(s.getCount(), 1);
    }

    @Test
    public void testLockingLazy() throws InterruptedException {
        SimpleSupplier s = new SimpleSupplier();
        Lazy<Integer> l = LazyFactory.createLockingLazy(s);
        Runnable r = l::get;
        createStartAndJoinAll(r, 10);
        Assert.assertEquals(1, s.getCount());
    }

    @Test
    public void testSingleReferenceMultiThread() throws InterruptedException {
        SimpleSupplier s = new SimpleSupplier();

        Lazy[] ls = new Lazy[]{
                LazyFactory.createLockingLazy(s),
                LazyFactory.createLockFreeLazy(s)
        };

        for (Lazy l : ls) {
            Object obj = l.get();
            Runnable r = () -> Assert.assertTrue(l.get() == obj);
            createStartAndJoinAll(r, 10);
        }
    }

    @Test
    public void testNullSupplier() {
        Supplier<Object> s = () -> null;
        Lazy[] ls = new Lazy[]{
                LazyFactory.createSingleThreadedLazy(s),
                LazyFactory.createLockingLazy(s),
                LazyFactory.createLockFreeLazy(s)
        };
        for (Lazy l : ls) {
            Assert.assertNull(l.get());
        }
    }

}
