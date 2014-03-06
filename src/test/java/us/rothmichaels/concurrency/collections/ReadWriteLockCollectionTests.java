/*
 * ReadWriteLockCollectionTests.java
 *
 * Mar 6, 2014 
 */
package us.rothmichaels.concurrency.collections;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import us.rothmichaels.testing.async.AsyncTester;

/**
 *
 *
 * @author Roth Michaels 
 * (<i><a href="mailto:roth@rothmichaels.us">roth@rothmichaels.us</a></i>)
 *
 */
@RunWith(JMock.class)
public class ReadWriteLockCollectionTests {

	ReadWriteLockCollection testLocker;
	Collection<ReadWriteLock> testCollection;
	
	Mockery mockContext = new JUnit4Mockery();
	ReadWriteLock firstMock = mockContext.mock(ReadWriteLock.class,"firstMock");
	Lock firstReadLock = mockContext.mock(Lock.class,"firstReadLock");
	Lock firstWriteLock = mockContext.mock(Lock.class,"firstWriteLock");
	ReadWriteLock secondMock = mockContext.mock(ReadWriteLock.class,"secondMock");
	Lock secondReadLock = mockContext.mock(Lock.class,"secondReadLock");
	Lock secondWriteLock = mockContext.mock(Lock.class,"secondWriteLock");
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testCollection = new HashSet<ReadWriteLock>();
		testCollection.add(firstMock);
		testCollection.add(secondMock);
		testLocker = 
				new ReadWriteLockCollection<Collection<ReadWriteLock>>(testCollection);
		
		mockContext.checking(new Expectations() {{
			allowing(firstMock).readLock(); will(returnValue(firstReadLock));
			allowing(firstMock).writeLock(); will(returnValue(firstWriteLock));
			allowing(secondMock).readLock(); will(returnValue(secondReadLock));
			allowing(secondMock).writeLock(); will(returnValue(secondWriteLock));
		}});
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testReadLock() {
		testLocker.readLock().lock();
		AsyncTester asyncTester = new AsyncTester(new Runnable() {
			@Override
			public void run() {
				assertTrue(testLocker.readLock().tryLock());
				testLocker.readLock().unlock();
				assertFalse(testLocker.writeLock().tryLock());
			}
		});
		
		for (final Lock mock : new Lock[]{firstReadLock,secondReadLock}) {
			mockContext.checking(new Expectations() {{
				oneOf(mock).lock();
			}});
		}
		
		mockContext.checking(new Expectations() {{
			// allow write lock calls
		}});
	}
	
	@Test
	public void testWriteLock() {
		final ReadWriteLock[] innerLocks = new ReadWriteLock[] {
				new ReentrantReadWriteLock(),
				new ReentrantReadWriteLock()
		};
		testLocker = 
				new ReadWriteLockCollection<Collection<ReadWriteLock>>(
						Arrays.asList(innerLocks));
		
		// Tests that another thread can't get read or write locks
		AsyncTester asyncTests = new AsyncTester(new Runnable() {
			@Override
			public void run() {
				assertFalse(innerLocks[0].readLock().tryLock());
				assertFalse(innerLocks[0].readLock().tryLock());
				assertFalse(testLocker.readLock().tryLock());
				assertFalse(innerLocks[0].writeLock().tryLock());
				assertFalse(innerLocks[0].writeLock().tryLock());
				assertFalse(testLocker.writeLock().tryLock());
			}
		});
		
		// lock in test thread
		testLocker.writeLock().lock();
		// test acquire in this thread
		assertTrue(innerLocks[0].writeLock().tryLock());
		innerLocks[0].writeLock().unlock();
		assertTrue(innerLocks[1].writeLock().tryLock());
		innerLocks[1].writeLock().unlock();
		assertTrue(testLocker.writeLock().tryLock());
		testLocker.writeLock().unlock();
		assertTrue(innerLocks[0].readLock().tryLock());
		innerLocks[0].readLock().unlock();
		assertTrue(innerLocks[1].readLock().tryLock());
		innerLocks[1].readLock().unlock();
		assertTrue(testLocker.readLock().tryLock());
		testLocker.readLock().unlock();
		
		asyncTests.runTest();
		asyncTests.verify();
	}
	
	@Test
	public void testWriteUnlock() {
		
	}

	@Test
	public void testCallReadLockOnCollection() {
		mockContext.checking(new Expectations() {{
			oneOf(firstReadLock).lock();
			oneOf(secondReadLock).lock();
		}});
	
		testLocker.readLock().lock();
		
		// new test
		
	}
	
	@Test
	public void testCallReadLockInterruptiblyOnCollection() throws InterruptedException {
		mockContext.checking(new Expectations() {{
			oneOf(firstReadLock).lockInterruptibly();
			oneOf(secondReadLock).lockInterruptibly();
		}});
	
		testLocker.readLock().lockInterruptibly();
	}
	
	@Test
	public void testCallReadNewConditionOnCollection() {
		fail("write a test");
	}
	
	@Test
	public void testCallReadTryLockOnCollection() {
		mockContext.checking(new Expectations() {{
			oneOf(firstReadLock).lock();
			oneOf(secondReadLock).lock();
		}});
	
		testLocker.readLock().tryLock();
	}
	
	@Test
	public void testCallReadTryLockWithTimeoutOnCollection() {
		fail("write a test");
	}
	
	@Test
	public void testCallReadUnlockOnCollection() {
		testLocker.readLock().lock();
		mockContext.checking(new Expectations() {{
			allowing(firstReadLock).lock();
			allowing(secondReadLock).lock();
			oneOf(firstReadLock).unlock();
			oneOf(secondReadLock).unlock();
		}});
	
		testLocker.readLock().unlock();
	}
}
