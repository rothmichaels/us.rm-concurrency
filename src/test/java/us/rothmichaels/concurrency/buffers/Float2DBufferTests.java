/*
 * Float2DBufferTests.java
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests {@link us.rothmichaels.concurrency.buffers.Float2DBuffer}.
 *
 * @author Roth Michaels 
 * (<i><a href="mailto:roth@rothmichaels.us">roth@rothmichaels.us</a></i>)
 *
 */
public class Float2DBufferTests extends ArrayBufferTests {

	static final int DIM1 = 4;
	static final int DIM2 = SIZE1;

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		testBuffer = new Float2DBuffer(DIM1, DIM2);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * @see us.rothmichaels.concurrency.buffers.ArrayBufferTests#testConstruction()
	 */
	@Override
	@Test
	public void testConstruction() {
		assertEquals("Bad size returned.",
				SIZE1, testBuffer.getSize());
		assertNotNull("Buffer ref null.", testBuffer.getDataRef());
		final float[][] dataRef = ((Float2DBuffer) testBuffer).getDataRef();
		assertTrue("First dimension too small",
				(DIM1 <= dataRef.length));
		for (int i = 0; i < dataRef.length; ++i) {
			assertTrue("Internal data structure too small at index "+i,
					(SIZE1 <= dataRef[i].length));
		}
		
	}

	/**
	 * @see us.rothmichaels.concurrency.buffers.ArrayBufferTests#testSetSize()
	 */
	@Override
	@Test
	public void testSetSize() {
		testBuffer.setSize(SIZE2);
		assertEquals("Bad size returned.",
				SIZE2, testBuffer.getSize());
		final float[][] dataRef = ((Float2DBuffer) testBuffer).getDataRef();
		assertTrue("First dimension too small",
				(DIM1 <= dataRef.length));
		for (int i = 0; i < dataRef.length; ++i) {
			assertTrue("Internal data structure too small at index "+i,
					(SIZE2 <= dataRef[i].length));
		}
		
		testBuffer.setSize(SIZE3);
		assertEquals("Bad size returned.",
				SIZE3, testBuffer.getSize());
		final float[][] dataRef3 = ((Float2DBuffer) testBuffer).getDataRef();
		assertTrue("First dimension too small",
				(DIM1 <= dataRef3.length));
		for (int i = 0; i < dataRef3.length; ++i) {
			assertTrue("Internal data structure too small at index "+i,
					(SIZE3 <= dataRef3[i].length));
		}
	}


	/**
	 * @see us.rothmichaels.concurrency.buffers.ArrayBufferTests#testClear()
	 */
	@Override
	@Test
	public void testClear() {
		float[][] buf = ((Float2DBuffer) testBuffer).getDataRef();
		for (int j = 0; j < DIM1; ++j) {
			for (int i = 0, end = testBuffer.getSize(); i < end; ++i) {
				buf[j][i] = j+i;
			}
		}
		testBuffer.clear();
		buf = ((Float2DBuffer) testBuffer).getDataRef();
		for (int j = 0; j < DIM1; ++j) {
			assertTrue("Internal data structure too small at index "+j,
					(SIZE1 <= buf[j].length));
			for (int i = 0, end = testBuffer.getSize(); i < end; ++i) {
				assertEquals(0, buf[j][i], 0f);
			}
		}
		
	}

	@Test
	public void testClearRow() {
		float[][] buf = ((Float2DBuffer) testBuffer).getDataRef();
		for (int j = 0; j < DIM1; ++j) {
			for (int i = 0, end = testBuffer.getSize(); i < end; ++i) {
				buf[j][i] = 1f;
			}
		}
		((Float2DBuffer) testBuffer).clear(1);
		buf = ((Float2DBuffer) testBuffer).getDataRef();
		for (int j = 0; j < DIM1; ++j) {
			assertTrue("Internal data structure too small at index "+j,
					(SIZE1 <= buf[j].length));
			float expected = (j == 1) ? 0f : 1f;
			for (int i = 0, end = testBuffer.getSize(); i < end; ++i) {
				assertEquals(expected, buf[j][i], 0f);
			}
		}
	}

}
