/*
 * LockCollection.java
 *
 * Copyright (c) 2014 Roth Michaels. All rights reserved.
 *
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) 
 * which can be found in the file epl-v10.html at the root of this
 * distribution. By using this software in any fashion, you are agreeing
 * to be bound by the terms of this license.
 *
 * EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, THE PROGRAM IS
 * PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, EITHER EXPRESS OR IMPLIED INCLUDING, WITHOUT LIMITATION, ANY
 * WARRANTIES OR CONDITIONS OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY
 * OR FITNESS FOR A PARTICULAR PURPOSE. Each Recipient is solely
 * responsible for determining the appropriateness of using and
 * distributing the Program and assumes all risks associated with its
 * exercise of rights under this Agreement , including but not limited
 * to the risks and costs of program errors, compliance with applicable
 * laws, damage to or loss of data, programs or equipment, and
 * unavailability or interruption of operations.
 *
 * EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, NEITHER RECIPIENT
 * NOR ANY CONTRIBUTORS SHALL HAVE ANY LIABILITY FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING WITHOUT LIMITATION LOST PROFITS), HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OR DISTRIBUTION OF THE PROGRAM OR THE EXERCISE OF ANY RIGHTS
 * GRANTED HEREUNDER, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGES.
 *
 * You must not remove this notice, or any other, from this software.
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
