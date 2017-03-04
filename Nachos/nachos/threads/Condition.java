package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;

/**
 * An implementation of condition variables built upon semaphores.
 *
 * <p>
 * A condition variable is a synchronization primitive that does not have
 * a value (unlike a semaphore or a lock), but threads may still be queued.
 *
 * <p><ul>
 *
 * <li><tt>sleep()</tt>: atomically release the lock and relinkquish the CPU
 * until woken; then reacquire the lock.
 *
 * <li><tt>wake()</tt>: wake up a single thread sleeping in this condition
 * variable, if possible.
 *
 * <li><tt>wakeAll()</tt>: wake up all threads sleeping inn this condition
 * variable.
 *
 * </ul>
 *
 * <p>
 * Every condition variable is associated with some lock. Multiple condition
 * variables may be associated with the same lock. All three condition variable
 * operations can only be used while holding the associated lock.
 * 
 *
 * <p>
 * In Nachos, condition variables are summed to obey <i>Mesa-style</i>
 * semantics. When a <tt>wake()</tt> or <tt>wakeAll()</tt> wakes up another
 * thread, the woken thread is simply put on the ready list, and it is the
 * responsibility of the woken thread to reacquire the lock (this reacquire is
 * taken core of in <tt>sleep()</tt>).
 * 나초스에서, condition variables는  Mesa-style 의미를 따른다.
 * wake 나 wakeAll이 다른 스레드를 깨울 때, 일어난 스레드는 ready 리스트에 들어간다. 그리고 깨어난 스레드는 lock을 다시
 * 획득할 책임이 있다.
 *
 * <p>
 * By contrast, some implementations of condition variables obey
 * <i>Hoare-style</i> semantics, where the thread that calls <tt>wake()</tt>
 * gives up the lock and the CPU to the woken thread, which runs immediately
 * and gives the lock and CPU back to the waker when the woken thread exits the
 * critical section.
 * 
 *
 * <p>
 * The consequence of using Mesa-style semantics is that some other thread
 * can acquire the lock and change data structures, before the woken thread
 * gets a chance to run. The advance to Mesa-style semantics is that it is a
 * lot easier to implement.
 * 
 */
public class Condition {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     *
     * 현재 스레드는 sleep, wake, wakeAll을 사용하기 전에 lock이어야 한다. 
     */
    public Condition(Lock conditionLock) {//생성자
	this.conditionLock = conditionLock;

	waitQueue = new LinkedList();
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     *
     * <p>
     * This implementation uses semaphores to implement this, by allocating a
     * semaphore for each waiting thread. The waker will <tt>V()</tt> this
     * semaphore, so there is no chance the sleeper will miss the wake-up, even
     * though the lock is released before caling <tt>P()</tt>.
     */
    public void sleep() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());

	Semaphore waiter = new Semaphore(0);//세마포어의 초기 값은 0
	waitQueue.add(waiter);//대기 큐에 세마포어 객체를 넣는다.

	conditionLock.release();//lock을 풀어준다.스레드를 사용 하기 위해서.
	waiter.P();//세마포어 값 -1 / 0이라면 sleep
	conditionLock.acquire();//
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());

	if (!waitQueue.isEmpty()) //크리티컬 섹션에 대해 대기중인 스레드가 있을 때
	    ((Semaphore) waitQueue.removeFirst()).V();//가장 먼저 들어간 스레드를 사용 가능 상태로 전환
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());

	while (!waitQueue.isEmpty()) //대기중인 모든 큐를 사용 가능 상태로 전환한다.
	    wake();
    }

    private Lock conditionLock;
    private LinkedList waitQueue;
    
}
