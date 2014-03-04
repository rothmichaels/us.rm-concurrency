/*
 * ManuallyLockedBuffer.java
 *
 * Mar 4, 2014 
 */
package us.rothmichaels.concurrency.buffers;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Abstract superclass for implementers of IReadWriteLockedBuffer.
 * 
 * Provides locks and data buffer access but performs no locking or unlocking.
 *
 * @author Roth Michaels 
 * (<i><a href="mailto:roth@rothmichaels.us">roth@rothmichaels.us</a></i>)
 *
 */
abstract class ReadWriteLockedBuffer<T> implements IReadWriteLockedBuffer<T> {

	/** Initial size of the buffer */
	final int initSize;
	/** The locks */
	private final ReadWriteLock lock;
	/** Data buffer */
	T buffer;
	/** Current size of the buffer */
	int size;
	
	/**
	 * Create a read/write lock for a data buffer.
	 * 
	 * @param buffer
	 *  Data object to manage with read/write lock.
	 * @param size 
	 * 	Initial size of the buffer. Under this default implementation, 
	 *  this is also the maximum size of the buffer. Subclasses may 
	 *  override this behavior.
	 */
	public ReadWriteLockedBuffer(T buffer, int size) {
		this.buffer = buffer;
		this.initSize = size;
		this.size = size;
		this.lock = new ReentrantReadWriteLock(); // TODO true for fair ordering?
	}


	/**
	 * @see java.util.concurrent.locks.ReadWriteLock#readLock()
	 */
	@Override
	public Lock readLock() {
		return lock.readLock();
	}


	/**
	 * @see java.util.concurrent.locks.ReadWriteLock#writeLock()
	 */
	@Override
	public Lock writeLock() {
		return lock.writeLock();
	}


	/**
	 * @see us.rothmichaels.concurrency.buffers.IReadWriteLockedBuffer#getDataRef()
	 */
	@Override
	public T getDataRef() {
		return buffer;
	}

	/**
	 * @see us.rothmichaels.concurrency.buffers.IReadWriteLockedBuffer#getDataClass()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<T> getDataClass() {
		return (Class<T>) buffer.getClass();
	}


	/**
	 * @see us.rothmichaels.concurrency.buffers.IReadWriteLockedBuffer#getSize()
	 */
	@Override
	public int getSize() {
		return size;
	}


	/**
	 * Set the size of the buffer.
	 * 
	 * In this default implementation, the internal data structure
	 * representing the buffer is not resized and therefore the 
	 * new size cannot exceed the initial size of the buffer.
	 * Subclasses may override this behavior here.
	 * 
	 * @param size 
	 *  The new size of the buffer
	 * 
	 * @see us.rothmichaels.concurrency.buffers.IReadWriteLockedBuffer#setSize(int)
	 */
	@Override
	public void setSize(int size) {
		if (size < this.initSize && size > 0){
			this.size = size;
		} else {
			throw new IllegalArgumentException("Bad size");
		}
	}

}
