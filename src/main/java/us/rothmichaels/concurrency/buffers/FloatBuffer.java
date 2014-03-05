/*
 * FloatBuffer.java
 *
 * Mar 4, 2014 
 */
package us.rothmichaels.concurrency.buffers;

/**
 * Read/Write lockable primitive float buffer.
 *
 * @author Roth Michaels 
 * (<i><a href="mailto:roth@rothmichaels.us">roth@rothmichaels.us</a></i>)
 *
 */
public class FloatBuffer extends ArrayBuffer<float[]> {

	/**
	 * @param size initial buffer size
	 */
	public FloatBuffer(int size) {
		super(float[].class, size);
	}
}
