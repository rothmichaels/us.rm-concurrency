/*
 * ArrayBufferTests.java
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
