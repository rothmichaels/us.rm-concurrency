/*
 * IManuallyLockedBuffer.java
 *
 * Mar 4, 2014 
 */
package us.rothmichaels.concurrency.buffers;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * An interface for buffers where client threads must 
 * manually request a read or write lock.
 * 
 * The lock is provided through the 
 * {@link java.util.concurrent.locks.ReadWriteLock} interface.
 *
 * @author Roth Michaels 
 * (<i><a href="mailto:roth@rothmichaels.us">roth@rothmichaels.us</a></i>)
 *
 * @param <T> Buffer data type 
 */
public interface IReadWriteLockedBuffer<T> extends ReadWriteLock {
	
	/**
	 * Get the size of the data buffer. This will not necessarily be
	 * the size of the internal structure returned by 
	 * {@link us.rothmichaels.concurrency.buffers#getDataRef()}.
	 * 
	 * @return current size
	 */
	int getSize();
	
	/**
	 * Set the size of the buffer. Some implementations may not 
	 * allow the size to be set to a value greater than 
	 * the buffer's initial size.
	 * 
	 * @param size new size
	 */
	void setSize(int size);
	
	/**
	 * Clear the buffer, may not clear 
	 * internal data past current size for effeciency. 
	 */
	void clear();
	
	/**
	 * Get a reference to the internal data structure 
	 * maintained by this object.
	 * 
	 * Do not do without a lock.
	 * 
	 * @return the internal data
	 */
	T getDataRef();
	
	/**
	 * Get the class of the internal data structure.
	 * 
	 * @return class of the internal data structure
	 */
	Class<T> getDataClass();
}
