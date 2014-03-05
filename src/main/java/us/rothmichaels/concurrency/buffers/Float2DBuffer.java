/*
 * Float2DBuffer.java
 *
 * Mar 4, 2014 
 */
package us.rothmichaels.concurrency.buffers;

import java.util.Arrays;

/**
 * 
 *
 * @author Roth Michaels 
 * (<i><a href="mailto:roth@rothmichaels.us">roth@rothmichaels.us</a></i>)
 *
 */
public class Float2DBuffer extends ArrayBuffer<float[][]> {

	private final int firstDim;
	
	/**
	 * @param type
	 * @param size
	 */
	public Float2DBuffer(int dim1, int dim2) {
		super(float[][].class, dim2);
		firstDim = dim1;
		buffer = new float[dim1][dim2];
	}

	/**
	 * @see us.rothmichaels.concurrency.buffers.ArrayBuffer#clear()
	 */
	@Override
	public void clear() {
		writeLock().lock();
		buffer = new float[firstDim][size];
		writeLock().unlock();
	}
	
	public void clear(int i) {
		writeLock().lock();
		Arrays.fill(buffer[i], 0, size, 0f);
		writeLock().unlock();
	}

	/**
	 * @see us.rothmichaels.concurrency.buffers.ArrayBuffer#setSize(int)
	 */
	@Override
	public void setSize(int size) {
		if (size < 0) {
			throw new IllegalArgumentException("Size can't be negative.");
		}
		writeLock().lock();
		this.size = size;
		clear();
		writeLock().unlock();
	}

}
