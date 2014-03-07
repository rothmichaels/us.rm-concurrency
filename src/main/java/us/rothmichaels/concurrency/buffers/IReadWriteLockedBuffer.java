/*
 * IManuallyLockedBuffer.java
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
