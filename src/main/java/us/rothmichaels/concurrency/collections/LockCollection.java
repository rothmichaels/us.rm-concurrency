/*
 * LockCollection.java
 *
 * Mar 6, 2014 
 */
package us.rothmichaels.concurrency.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implements a Lock over a Collection of Locks.
 * 
 * Acquiring the lock on the collection acquires the lock 
 * on all locks in the collection.
 *
 * @author Roth Michaels 
 * (<i><a href="mailto:roth@rothmichaels.us">roth@rothmichaels.us</a></i>)
 *
 */
public class LockCollection implements Lock {

	private final Lock outterLock;
	private final Collection<Lock> innerLocks;
	
	public LockCollection(Lock lock, Collection<Lock> lockCollection) {
		outterLock = lock;
		innerLocks = new ArrayList<Lock>(lockCollection);
	}

	/**
	 * @see java.util.concurrent.locks.Lock#lock()
	 */
	@Override
	public void lock() {
		outterLock.lock();
		for (Lock lock : innerLocks) {
			lock.lock();
		}
	}

	/**
	 * @see java.util.concurrent.locks.Lock#lockInterruptibly()
	 */
	@Override
	public void lockInterruptibly() throws InterruptedException {
		outterLock.lockInterruptibly();
		for (Lock lock : innerLocks) {
			lock.lockInterruptibly();
		}
	}

	/**
	 * @see java.util.concurrent.locks.Lock#tryLock()
	 */
	@Override
	public boolean tryLock() {
		if (outterLock.tryLock()) {
			List<Lock> locked = new ArrayList<Lock>(innerLocks.size());
			locked.add(this);
			for (Lock lock : innerLocks) {
				if (lock.tryLock()) {
					locked.add(lock);
				} else {
					for (Lock lockedLock : locked) {
						lockedLock.unlock();
					}
					return false;
				}
			}
			
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @see java.util.concurrent.locks.Lock#tryLock(long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public boolean tryLock(long time, TimeUnit unit)
			throws InterruptedException {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see java.util.concurrent.locks.Lock#unlock()
	 */
	@Override
	public void unlock() {
		for (Lock lock : innerLocks) {
			lock.unlock();
		}
		outterLock.unlock();
	}

	/**
	 * @see java.util.concurrent.locks.Lock#newCondition()
	 */
	@Override
	public Condition newCondition() {
		return outterLock.newCondition();
	}

}
