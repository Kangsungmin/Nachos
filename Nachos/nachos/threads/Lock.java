package nachos.threads;

import nachos.machine.*;

/**
 * A <tt>Lock</tt> is a synchronization primitive that has two states,
 * <i>busy</i> and <i>free</i>. There are only two operations allowed on a
 * lock:
 * 
 *
 * <ul>
 * <li><tt>acquire()</tt>: atomically wait until the lock is <i>free</i> and
 * then set it to <i>busy</i>.
 * acquire()는 자동으로 lock이 free가 될 때 까지 대기한다.그리고 상태를 busy로 설정한다.
 * <li><tt>release()</tt>: set the lock to be <i>free</i>, waking up one
 * waiting thread if possible.
 * release()는 lock을 free상태로 설정한다. 그리고 사용가능한 하나의 스레드를 깨운다.
 * </ul>
 *
 * <p>
 * Also, only the thread that acquired a lock may release it. As with
 * semaphores, the API does not allow you to read the lock state (because the
 * value could change immediately after you read it).
 * lock의 busy상태 에서 API는 읽을 수 없다.!!! 사용하기 전 반드시 free상태로 전환 필요함. 
 */
public class Lock {
    /**
     * Allocate a new lock. The lock will initially be <i>free</i>.
     */
    public Lock() {
    }

    /**
     * Atomically acquire this lock. The current thread must not already hold
     * this lock.
     */
    public void acquire() {
	Lib.assertTrue(!isHeldByCurrentThread());

	boolean intStatus = Machine.interrupt().disable();
	KThread thread = KThread.currentThread();

	if (lockHolder != null) {
	    waitQueue.waitForAccess(thread); //현재 스레드를 block시킨다.
	    KThread.sleep();
	}
	else {
	    waitQueue.acquire(thread);
	    lockHolder = thread;
	}

	Lib.assertTrue(lockHolder == thread); //다음 스레드를 lockHolder로 지정

	Machine.interrupt().restore(intStatus);
    }

    /**
     * Atomically release this lock, allowing other threads to acquire it.
     */
    public void release() {
	Lib.assertTrue(isHeldByCurrentThread());

	boolean intStatus = Machine.interrupt().disable();

	if ((lockHolder = waitQueue.nextThread()) != null)
	    lockHolder.ready(); //대기 큐에서 스레드를 꺼낸 후, ready 큐에 넣는다.
	
	Machine.interrupt().restore(intStatus);
    }

    /**
     * Test if the current thread holds this lock.
     *
     * @return	true if the current thread holds this lock.
     */
    public boolean isHeldByCurrentThread() {
	return (lockHolder == KThread.currentThread());
    }

    private KThread lockHolder = null;
    private ThreadQueue waitQueue =
	ThreadedKernel.scheduler.newThreadQueue(true);
}
