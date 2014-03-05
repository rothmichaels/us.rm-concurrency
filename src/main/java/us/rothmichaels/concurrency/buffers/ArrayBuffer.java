/*
 * ArrayBuffer.java
 *
 * Mar 4, 2014 
 */
package us.rothmichaels.concurrency.buffers;

import java.lang.reflect.Array;

/**
 * Uses an array as internal storage for the buffer.
 * 
 * Clear and set size could be optimized by subclasses.
 *
 * @author Roth Michaels 
 * (<i><a href="mailto:roth@rothmichaels.us">roth@rothmichaels.us</a></i>)
 * @param <T>
 *
 */
public abstract class ArrayBuffer<T> extends ReadWriteLockedBuffer<T> {

	private final Class<?> type;
	
	/**
	 * @param buffer
	 */
	@SuppressWarnings("unchecked")
	public ArrayBuffer(Class<?> type, int size) {
		super((T) Array.newInstance(type, size),size);
		this.type = type;
	}

	/**
	 * @see us.rothmichaels.concurrency.buffers.IReadWriteLockedBuffer#clear()
	 */
	@Override
	public void clear() {
		writeLock().lock();
		@SuppressWarnings("unchecked")
		T tmp = (T) Array.newInstance(this.type, this.size);
		buffer = tmp;
		writeLock().unlock();
	}

	/**
	 * @see us.rothmichaels.concurrency.buffers.ReadWriteLockedBuffer#setSize(int)
	 */
	@Override
	public void setSize(int size) {
		try {
			super.setSize(size);
		} catch (IllegalArgumentException e) {
			if (size > 0) {
				writeLock().lock();
				@SuppressWarnings("unchecked")
				T tmp = (T) Array.newInstance(this.type, size);
				System.arraycopy(this.buffer, 0, tmp, 0, this.size);
				this.buffer = tmp;
				this.size = size; 
				writeLock().unlock();
			} else {
				throw e;
			}
		}
	}



}
