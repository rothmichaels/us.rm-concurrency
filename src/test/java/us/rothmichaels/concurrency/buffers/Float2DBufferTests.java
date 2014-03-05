/*
 * Float2DBufferTests.java
 *
 * Mar 4, 2014 
 */
package us.rothmichaels.concurrency.buffers;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 *
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
