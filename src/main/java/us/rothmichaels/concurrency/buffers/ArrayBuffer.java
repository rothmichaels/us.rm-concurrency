/*
 * ArrayBuffer.java
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
