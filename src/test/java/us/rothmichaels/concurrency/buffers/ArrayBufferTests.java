/*
 * ArrayBufferTests.java
 *
 * Mar 4, 2014 
 */
package us.rothmichaels.concurrency.buffers;

import static org.junit.Assert.*;

import java.util.concurrent.locks.Lock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import us.rothmichaels.testing.async.AsyncTester;

/**
 * Tests {@link us.rothmichaels.concurrency.buffers.ArrayBuffer}.
 *
 * @author Roth Michaels 
 * (<i><a href="mailto:roth@rothmichaels.us">roth@rothmichaels.us</a></i>)
 *
 */
public class ArrayBufferTests {

	static final int SIZE1 = 10;
	static final int SIZE2 = 7;
	static final int SIZE3 = 17;
	
	ArrayBuffer<?> testBuffer;
	
	volatile Boolean success;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testBuffer = new ArrayBufferT(SIZE1);
		success = false;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testConstruction() {
		assertEquals("Bad size returned.",
				SIZE1, testBuffer.getSize());
		assertNotNull("Buffer ref null.", testBuffer.getDataRef());
		assertTrue("Internal data structure too small.",
				(SIZE1 <= ((ArrayBufferT) testBuffer).getDataRef().length));
	}

	@Test
	public void testSetSize() {
		testBuffer.setSize(SIZE2);
		assertEquals("Bad size returned.",
				SIZE2, testBuffer.getSize());
		assertTrue("Internal data structure too small.",
				(SIZE2 <= ((ArrayBufferT) testBuffer).getDataRef().length));
		
		testBuffer.setSize(SIZE3);
		assertEquals("Bad size returned.",
				SIZE3, testBuffer.getSize());
		assertTrue("Internal data structure too small.",
				(SIZE3 <= ((ArrayBufferT) testBuffer).getDataRef().length));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSetSizeNegative() {
		testBuffer.setSize(-1);
	}
	
	AsyncTester makeLockTester(final Lock lock) {
		return new AsyncTester(new Runnable() {
			@Override
			public void run() {
				try {
					lock.lock();
					Thread.sleep(1000);
					assertFalse(success.booleanValue());
					lock.unlock();
				} catch (InterruptedException e) {
					fail(e.toString());
				}
				
			}
		});
	}
	
	@Test
	public void testSetSizeWhenWriteLocked() throws InterruptedException {
		AsyncTester tester = makeLockTester(testBuffer.writeLock());
		
		tester.runTest();
		Thread.sleep(200);
		testBuffer.setSize(SIZE3);
		success = true;
		tester.verify();
	}
	
	@Test
	public void testSetSizeWhenReadLocked() throws InterruptedException {
		AsyncTester tester = makeLockTester(testBuffer.readLock());
		
		tester.runTest();
		Thread.sleep(200);
		testBuffer.setSize(SIZE3);
		success = true;
		tester.verify();
	}
	
	@Test
	public void testClear() {
		testBuffer.writeLock().lock();
		int[] buf = ((ArrayBufferT) testBuffer).getDataRef();
		for (int i = 0, end = testBuffer.getSize(); i < end; ++i) {
			buf[i] = i;
		}
		testBuffer.clear();
		buf = ((ArrayBufferT) testBuffer).getDataRef();
		for (int i = 0, end = testBuffer.getSize(); i < end; ++i) {
			assertEquals(0, buf[i]);
		}
		testBuffer.writeLock().unlock();
	}
	
	@Test
	public void testClearWhenWriteLocked() throws InterruptedException {
			AsyncTester tester = makeLockTester(testBuffer.writeLock());
		
		tester.runTest();
		Thread.sleep(200);
		testBuffer.clear();
		success = true;
		tester.verify();
	}
	
	@Test
	public void testClearWhenReadLocked() throws InterruptedException {
			AsyncTester tester = makeLockTester(testBuffer.readLock());
		
		tester.runTest();
		Thread.sleep(200);
		testBuffer.clear();
		success = true;
		tester.verify();
	}
	
	/**
	 * Concrete type for testing abstract class.
	 */
	static class ArrayBufferT extends ArrayBuffer<int[]> {
		public ArrayBufferT(int size) {
			super(Integer.TYPE, size);

		}
	}
}
