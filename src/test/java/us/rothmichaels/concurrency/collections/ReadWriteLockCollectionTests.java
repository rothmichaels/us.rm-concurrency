/*
 * ReadWriteLockCollectionTests.java
 *
 * Copyright (c) 2014 Roth Michaels. All rights reserved.
 *
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) 
 * which can be found in the file epl-v10.html at the root of this
 * distribution. By using this software in any fashion, you are agreeing
 * to be bound by the terms of this license.
 *
 * EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, THE PROGRAM IS
 * PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, EITHER EXPRESS OR IMPLIED INCLUDING, WITHOUT LIMITATION, ANY
 * WARRANTIES OR CONDITIONS OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY
 * OR FITNESS FOR A PARTICULAR PURPOSE. Each Recipient is solely
 * responsible for determining the appropriateness of using and
 * distributing the Program and assumes all risks associated with its
 * exercise of rights under this Agreement , including but not limited
 * to the risks and costs of program errors, compliance with applicable
 * laws, damage to or loss of data, programs or equipment, and
 * unavailability or interruption of operations.
 *
 * EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, NEITHER RECIPIENT
 * NOR ANY CONTRIBUTORS SHALL HAVE ANY LIABILITY FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING WITHOUT LIMITATION LOST PROFITS), HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OR DISTRIBUTION OF THE PROGRAM OR THE EXERCISE OF ANY RIGHTS
 * GRANTED HEREUNDER, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGES.
 *
 * You must not remove this notice, or any other, from this software.
 * 
 * Mar 6, 2014 
 */
package us.rothmichaels.concurrency.collections;

import static org.junit.Assert.*;

import java.util.ArrayList;
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

import us.rothmichaels.reflect.EasyReflection;
import us.rothmichaels.testing.async.AsyncTester;

/**
 *
 *
 * @author Roth Michaels 
 * (<i><a href="mailto:roth@rothmichaels.us">roth@rothmichaels.us</a></i>)
 *
 */
public class ReadWriteLockCollectionTests {

	ReadWriteLockCollection testLocker;
	Collection<ReadWriteLock> testCollection;

	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		testCollection = new ArrayList<ReadWriteLock>();
		testCollection.add(new ReentrantReadWriteLock());
		testCollection.add(new ReentrantReadWriteLock());
		testLocker = new ReadWriteLockCollection(testCollection);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	/**
	 * Proof of correctness based on behavior of
	 * {@link us.rothmichaels.concurrency.collections.LockCollection}
	 * @throws IllegalAccessException 
	 */
	@Test
	public void verifyState() throws IllegalAccessException {
		LockCollection readLock = EasyReflection.getFieldValue(testLocker, "readLock");
		LockCollection writeLock = EasyReflection.getFieldValue(testLocker, "writeLock");
		Collection<Lock> readLocks = EasyReflection.getFieldValue(readLock, "innerLocks"); 
		Collection<Lock> writeLocks = EasyReflection.getFieldValue(writeLock, "innerLocks");
		assertEquals(testCollection.size(), readLocks.size());
		assertEquals(testCollection.size(), writeLocks.size());
		for (ReadWriteLock lock : testCollection) {
			assertTrue(readLocks.contains(lock.readLock()));
			assertTrue(writeLocks.contains(lock.writeLock()));
		}
		
		assertSame(readLock, testLocker.readLock());
		assertSame(writeLock, testLocker.writeLock());
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
		
		asyncTester.runTest();
		asyncTester.verify();
		
		assertTrue(testLocker.readLock().tryLock());
		assertFalse(testLocker.writeLock().tryLock());
	}
	
	@Test
	public void testWriteLock() {
		testLocker.writeLock().lock();
		AsyncTester asyncTester = new AsyncTester(new Runnable() {
			@Override
			public void run() {
				assertFalse(testLocker.readLock().tryLock());
				assertFalse(testLocker.writeLock().tryLock());
			}
		});
		
		asyncTester.runTest();
		asyncTester.verify();
		
		assertTrue(testLocker.writeLock().tryLock());
		assertTrue(testLocker.readLock().tryLock());
	}
}
