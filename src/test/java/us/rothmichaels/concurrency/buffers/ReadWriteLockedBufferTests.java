/*
 * ManuallyLockedBufferTestts.java
 *
 * Mar 4, 2014 
 */
package us.rothmichaels.concurrency.buffers;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import us.rothmichaels.testing.async.AsyncTester;
import us.rothmichaels.testing.error.NotUsedInTestsError;

/**
 * Tests {@link us.rothmichaels.concurrency.buffers.ReadWriteLockedBuffer}.
 *
 * @author Roth Michaels 
 * (<i><a href="mailto:roth@rothmichaels.us">roth@rothmichaels.us</a></i>)
 *
 */
public class ReadWriteLockedBufferTests {

	static int SIZE = 10;
	
	ReadWriteLockedBuffer<float[]> testBuffer; 
	float[] testData = new float[SIZE];
	Timer t;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testBuffer = new BufferT<float[]>(testData);
		t = new Timer();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetDataRef() {
		assertSame(testData,testBuffer.getDataRef());
	}
	
	@Test
	public void testGetDataClass() {
		ArrayList<Object> testObject = new ArrayList<Object>();
		ReadWriteLockedBuffer<ArrayList<?>> testBuffer = 
				new BufferT<ArrayList<?>>(testObject);
		assertSame(ArrayList.class,testBuffer.getDataClass());
	}
	
	@Test
	public void testTwoCanRead() throws InterruptedException {
		AsyncTester tester = new AsyncTester(new Runnable() {
			@Override
			public void run() {
				assertTrue(testBuffer.readLock().tryLock());
			}
		});
		
		testBuffer.readLock().lock();
		tester.runTest();
		tester.verify();
		testBuffer.readLock().unlock();
	}
	
	@Test
	public void oneCanWrite() {
		AsyncTester tester = new AsyncTester(new Runnable() {
			@Override
			public void run() {
				assertFalse(testBuffer.writeLock().tryLock());
			}
		});
		
		testBuffer.writeLock().lock();
		tester.runTest();
		tester.verify();
		testBuffer.writeLock().unlock();
	}
	
	@Test
	public void testCantReadWhenWriting() {
		AsyncTester tester = new AsyncTester(new Runnable() {
			@Override
			public void run() {
				assertFalse(testBuffer.readLock().tryLock());
			}
		});
		
		testBuffer.writeLock().lock();
		tester.runTest();
		tester.verify();
		testBuffer.writeLock().unlock();
	}
	
	@Test
	public void testCantWriteWhenReading() {
		AsyncTester tester = new AsyncTester(new Runnable() {
			@Override
			public void run() {
				assertFalse(testBuffer.writeLock().tryLock());
			}
		});
		
		testBuffer.readLock().lock();
		tester.runTest();
		tester.verify();
		testBuffer.readLock().unlock();
	}
	
	@Test
	public void testSize() {
		assertEquals(SIZE, testBuffer.getSize());
		testBuffer.setSize(3);
		assertEquals(3, testBuffer.getSize());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSizeNegative() {
		testBuffer.setSize(-1);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSizeTooBig() {
		testBuffer.setSize(SIZE+1);
	}
	
	/**
	 * Concrete implementation for testing
	 *
	 * @author Roth Michaels 
	 * (<i><a href="mailto:roth@rothmichaels.us">roth@rothmichaels.us</a></i>)
	 *
	 * @param <T>
	 */
	static class BufferT<T> extends ReadWriteLockedBuffer<T> {

		public BufferT(T buffer) {
			super(buffer, SIZE);
		}

		/**
		 * @see us.rothmichaels.concurrency.buffers.IReadWriteLockedBuffer#clear()
		 */
		@Override
		public void clear() {
			throw new NotUsedInTestsError("clear() not implmented on ReadWriteLockedBuffer");
		}

	}
}
