/*
 * ReadWriteLockMap.java
 *
 * Mar 6, 2014 
 */
package us.rothmichaels.concurrency.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 *
 * @author Roth Michaels 
 * (<i><a href="mailto:roth@rothmichaels.us">roth@rothmichaels.us</a></i>)
 *
 */
public class ReadWriteLockCollection<T extends Collection<ReadWriteLock>> 
										implements ReadWriteLock {

	private final Lock readLock;
	private final Lock writeLock;
	
	private final T collection;
	
	@SuppressWarnings("unchecked")
	public ReadWriteLockCollection(T collection) {
		this.collection = (T) Collections.unmodifiableCollection(collection);
		this.readLock = new CollectionReadLock();
		this.writeLock = new CollectionWriteLock();
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
	
	
	class CollectionReadLock extends ReentrantLock {
		private static final long serialVersionUID = 3796267714597158959L;

		/**
		 * @see java.util.concurrent.locks.Lock#lock()
		 */
		@Override
		public void lock() {
			super.lock();
			for (ReadWriteLock rw : collection) {
				rw.readLock().lock();
			}
		}

		/**
		 * @see java.util.concurrent.locks.Lock#lockInterruptibly()
		 */
		@Override
		public void lockInterruptibly() throws InterruptedException {
			super.lockInterruptibly();
			for (ReadWriteLock rw : collection) {
				rw.readLock().lockInterruptibly();
			}
		}

		/**
		 * @see java.util.concurrent.locks.Lock#newCondition()
		 */
		@Override
		public Condition newCondition() {
			throw new AssertionError("Not yet implemented.");
		}

		/**
		 * @see java.util.concurrent.locks.Lock#tryLock()
		 */
		@Override
		public boolean tryLock() {
			
			if (super.tryLock()) {
				List<Lock> locked = new LinkedList<Lock>();
				for (ReadWriteLock rw : collection) {
					Lock lock = rw.readLock();
					if (lock.tryLock()) {
						locked.add(lock);
					} else {
						for (Lock l: locked) {
							l.unlock();
						}
						super.unlock();
						
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
		public boolean tryLock(long arg0, TimeUnit arg1)
				throws InterruptedException {
			throw new AssertionError("Not yet implemented.");
		}

		/**
		 * @see java.util.concurrent.locks.Lock#unlock()
		 */
		@Override
		public void unlock() {
			for (ReadWriteLock rw : collection) {
				rw.readLock().unlock();
			}
			super.unlock();
		}
		
	}
	
	class CollectionWriteLock extends ReentrantLock {
		private static final long serialVersionUID = 786220323240639993L;

		/**
		 * @see java.util.concurrent.locks.Lock#lock()
		 */
		@Override
		public void lock() {
			super.lock();
			for (ReadWriteLock rw : collection) {
				rw.writeLock().lock();
			}
		}

		/**
		 * @see java.util.concurrent.locks.Lock#lockInterruptibly()
		 */
		@Override
		public void lockInterruptibly() throws InterruptedException {
			super.lockInterruptibly();
			for (ReadWriteLock rw : collection) {
				rw.writeLock().lockInterruptibly();
			}
		}

		/**
		 * @see java.util.concurrent.locks.Lock#newCondition()
		 */
		@Override
		public Condition newCondition() {
			throw new AssertionError("Not yet implemented.");
		}

		/**
		 * @see java.util.concurrent.locks.Lock#tryLock()
		 */
		@Override
		public boolean tryLock() {
			if (super.tryLock()) {
				List<Lock> locked = new LinkedList<Lock>();
				for (ReadWriteLock rw : collection) {
					Lock lock = rw.writeLock();
					if (lock.tryLock()) {
						locked.add(lock);
					} else {
						for (Lock l: locked) {
							l.unlock();
						}
						super.unlock();
						
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
		public boolean tryLock(long arg0, TimeUnit arg1)
				throws InterruptedException {
			throw new AssertionError("Not yet implemented.");
		}

		/**
		 * @see java.util.concurrent.locks.Lock#unlock()
		 */
		@Override
		public void unlock() {
			for (ReadWriteLock rw : collection) {
				rw.writeLock().unlock();
			}
			super.unlock();
		}
	}
	
}
