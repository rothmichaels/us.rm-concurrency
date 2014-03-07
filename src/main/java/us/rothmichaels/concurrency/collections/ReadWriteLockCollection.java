/*
 * ReadWriteLockMap.java
 *
 * Mar 6, 2014 
 */
package us.rothmichaels.concurrency.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 *
 * @author Roth Michaels 
 * (<i><a href="mailto:roth@rothmichaels.us">roth@rothmichaels.us</a></i>)
 *
 */
public class ReadWriteLockCollection implements ReadWriteLock {

	private final ReadWriteLock masterLock;
	
	private final LockCollection readLock;
	private final LockCollection writeLock;
	
	public ReadWriteLockCollection(Collection<? extends ReadWriteLock> collection) {
		this.masterLock = new ReentrantReadWriteLock();
		int size = collection.size();
		List<Lock> tmpR = new ArrayList<Lock>(size);
		List<Lock> tmpW = new ArrayList<Lock>(size);
		for (ReadWriteLock lock : collection) {
			tmpR.add(lock.readLock());
			tmpW.add(lock.writeLock());
		}
		this.readLock = new LockCollection(masterLock.readLock(), tmpR);
		this.writeLock = new LockCollection(masterLock.writeLock(), tmpW);
	}
	
	/**
	 * @see java.util.concurrent.locks.ReadWriteLock#readLock()
	 */
	@Override
	public Lock readLock() {
		return readLock;
	}

	/**
	 * @see java.util.concurrent.locks.ReadWriteLock#writeLock()
	 */
	@Override
	public Lock writeLock() {
		return writeLock;
	}
	
	
}
