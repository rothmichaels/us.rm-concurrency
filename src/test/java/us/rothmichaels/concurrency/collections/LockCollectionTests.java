/*
 * LockCollectionTests.java
 *
 * Mar 6, 2014 
 */
package us.rothmichaels.concurrency.collections;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.States;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import us.rothmichaels.testing.async.AsyncTester;

/**
 * Tests {@link us.rothmichaels.concurrency.collections.LockCollection}
 *
 * @author Roth Michaels 
 * (<i><a href="mailto:roth@rothmichaels.us">roth@rothmichaels.us</a></i>)
 *
 */
@RunWith(JMock.class)
public class LockCollectionTests {

	static final String INIT = "init";
	static final String FIRST = "first";
	static final String SECOND = "second";

	LockCollection testLock;

	Mockery context = new JUnit4Mockery();
	Lock mockLock;
	List<Lock> innerLocks;
	States callState = context.states("call-state").startsAs(INIT);

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		mockLock = context.mock(Lock.class, "outter-lock");
		innerLocks = new ArrayList<Lock>();
		for (int i = 1; i <= 3; ++i) {
			innerLocks.add(context.mock(Lock.class, "inner-lock-"+i));
		}

		testLock = new LockCollection(mockLock, innerLocks);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test that lock() is called on the 
	 * collection's lock and all inner locks
	 */
	@Test
	public void testLock() {
		context.checking(new Expectations() {{
			oneOf(mockLock).lock(); when(callState.is(INIT));
			then(callState.is(FIRST));
			for (Lock lock : innerLocks) {
				oneOf(lock).lock(); when(callState.is(FIRST));
			}
		}});

		testLock.lock();
	}

	/**
	 * Test that the internal collection of locks is immutable.
	 */
	@Test
	public void testImmutability() {
		context.checking(new Expectations() {{
			oneOf(mockLock).lock(); when(callState.is(INIT));
			then(callState.is(FIRST));
			for (Lock lock : innerLocks) {
				oneOf(lock).lock(); when(callState.is(FIRST));
			}
		}});

		innerLocks.clear();
		innerLocks.add(context.mock(Lock.class,"inner-mock-X"));

		testLock.lock();
	}

	@Test
	public void testLockInterruptibly() throws InterruptedException {
		context.checking(new Expectations() {{
			oneOf(mockLock).lockInterruptibly(); when(callState.is(INIT));
			then(callState.is(FIRST));
			for (Lock lock : innerLocks) {
				oneOf(lock).lockInterruptibly(); when(callState.is(FIRST));
			}
		}});

		testLock.lockInterruptibly();
	}

	@Test
	public void testTryLock() {
		// can get lock
		context.checking(new Expectations() {{
			oneOf(mockLock).tryLock(); will(returnValue(true)); 
			when(callState.is(INIT));
			then(callState.is(FIRST));
			for (Lock lock : innerLocks) {
				oneOf(lock).tryLock(); will(returnValue(true)); 
				when(callState.is(FIRST));
				allowing(lock).unlock();
			}
			allowing(mockLock).unlock();
		}});

		assertTrue(testLock.tryLock());
	}
	@Test
	public void testTryLockB() {
		// can't get outter lock
		callState.is(INIT);
		context.checking(new Expectations() {{
			allowing(mockLock).tryLock(); will(returnValue(false)); 
			when(callState.is(INIT));
			then(callState.is(FIRST));
			for (Lock lock : innerLocks) {
				allowing(lock).tryLock(); will(returnValue(true)); 
				when(callState.is(FIRST));
				allowing(lock).unlock();
			}
			allowing(mockLock).unlock();
		}});

		assertFalse(testLock.tryLock());
	}
	@Test
	public void testTryLockC() {
		// can't get inner lock
		callState.is(INIT);
		context.checking(new Expectations() {{
			allowing(mockLock).tryLock(); will(returnValue(true)); 
			when(callState.is(INIT));
			then(callState.is(FIRST));
			boolean ret = true;
			for (Lock lock : innerLocks) {
				allowing(lock).tryLock(); will(returnValue(ret)); 
				when(callState.is(FIRST));
				allowing(lock).unlock();
				ret = !ret;
			}
			allowing(mockLock).unlock();
		}});

		assertFalse(testLock.tryLock());
	}

	
	static final long TIMEOUT = TimeUnit.NANOSECONDS.convert(1,TimeUnit.SECONDS);
	
	@Test
	public void testTryLockTimeout() throws InterruptedException {
		// setup
		innerLocks = new ArrayList<Lock>();
		for (int i = 1; i <= 3; ++i) {
			innerLocks.add(new ReentrantLock());
		}
		testLock = new LockCollection(new ReentrantLock(), innerLocks);
		
		// lock in test thread
		testLock.lock();
		final Lock syncLock = new ReentrantLock();
		final Condition triedLock = syncLock.newCondition();
		// async tries lock with short (no?) timeout => false
		// tries with longer time out => true
		AsyncTester asyncTests = new AsyncTester(new Runnable() {
			@Override
			public void run() {
				final long start = System.nanoTime();
				ReentrantLock lock = (ReentrantLock)syncLock;
				try {
					assertFalse(testLock.tryLock(TIMEOUT, TimeUnit.NANOSECONDS));
					final long time = System.nanoTime() - start;
					assertTrue(time >= TIMEOUT && time < TIMEOUT*1.25);
				} catch (InterruptedException e) {
					fail("Interrupted.");
				} finally {
					lock.lock();
					triedLock.signalAll();
					lock.unlock();
				}
				
				try {
					assertTrue(testLock.tryLock(TIMEOUT, TimeUnit.NANOSECONDS));
				} catch (InterruptedException e) {
					fail("Interrupted.");
				}
			}
		});
		
		callState.become(FIRST);
		asyncTests.runTest();
		syncLock.lock();
		triedLock.await();
		syncLock.unlock();
		testLock.unlock();
		asyncTests.verify();
	}

	@Test
	public void testUnlock() {
		context.checking(new Expectations() {{
			oneOf(mockLock).unlock(); when(callState.is(FIRST));
			then(callState.is(SECOND));
			for (Lock lock : innerLocks) {
				oneOf(lock).unlock(); when(callState.isNot(SECOND));
			}
			then(callState.is(FIRST));
		}});

		testLock.unlock();
	}

	@Test
	public void testNewCondition() {
		context.checking(new Expectations() {{
			oneOf(mockLock).newCondition();
		}});

		testLock.newCondition();
	}


}
