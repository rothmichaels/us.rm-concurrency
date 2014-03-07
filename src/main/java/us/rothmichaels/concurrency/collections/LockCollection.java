/*
 * LockCollection.java
 *
 * Mar 6, 2014 
 */
package us.rothmichaels.concurrency.collections;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
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
	private final int size;
	
	public LockCollection(Lock lock, Collection<Lock> lockCollection) {
		outterLock = lock;
		innerLocks = new ArrayList<Lock>(lockCollection);
		size = innerLocks.size();
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
			List<Lock> locked = new ArrayList<Lock>(size);
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
		// queues of locks to try / have been tried / have been locked
		Deque<Lock> locksToBeTried = new ArrayDeque<Lock>(innerLocks);
		locksToBeTried.push(outterLock);
		Deque<Lock> locksTried = new ArrayDeque<Lock>(size+1);
		List<Lock> locksLocked = new ArrayList<Lock>(size+1);
		
		// start timing try
		final long start = System.nanoTime();
		final long end = start + TimeUnit.NANOSECONDS.convert(time, unit);
		
		// try to unlock
		while (!locksToBeTried.isEmpty() || !locksTried.isEmpty()) {
			while (!locksToBeTried.isEmpty()) {
				Lock lock = locksToBeTried.pop();
				if (lock.tryLock()) {
					locksLocked.add(lock);
				} else {
					locksTried.add(lock);
				}
			}
			
			while (!locksTried.isEmpty()) {
				final long timeout = end - System.nanoTime();
				if (timeout > 0) {
					Lock lock = locksTried.pop();
					if (lock.tryLock(timeout, TimeUnit.NANOSECONDS)) {
						locksLocked.add(lock);
					} else {
						locksToBeTried.add(lock);
					}
				} else { // took too long
					for (Lock lock : locksLocked) {
						lock.unlock();
					}
					return false; 
				}
			}
		}
		
		return true;
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
