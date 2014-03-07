/*
 * ManuallyLockedBuffer.java
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
	 * @return T 
	 * 	reference to the internal data structure,
	 *  may be larger than the current buffer size.
	 *  
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
