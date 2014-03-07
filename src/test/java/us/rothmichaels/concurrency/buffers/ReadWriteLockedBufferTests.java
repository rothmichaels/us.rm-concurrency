/*
 * ManuallyLockedBufferTestts.java
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

import java.util.ArrayList;

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
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testBuffer = new BufferT<float[]>(testData);
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
