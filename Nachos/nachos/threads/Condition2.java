package nachos.threads;

import java.util.LinkedList;

import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
/*
 *  #Condition Variable: a queue of threads waiting for something inside a
 *  critical sections. Allow sleeping inside critical section by atomically 
 *  releasing lock at time we go to sleep.
 *  컨디션 베리어블 설명.
 */
public class Condition2 { //크리티컬 섹션에 들어가지 못해서 대기하고 있는 스레드들을 관리
	/*
	 * 크리티컬 섹션 에 못들어간 스레드가 대기하는 큐를 자동으로 sleep시켜준다.
	 */
	/**
	 * Allocate a new condition variable.
	 *
	 * @param	conditionLock	the lock associated with this condition
	 *				variable. The current thread must hold this
	 *				lock whenever it uses <tt>sleep()</tt>,
	 *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
	 */
	public Condition2(Lock conditionLock) {//생성자
		this.conditionLock = conditionLock;

		waitQ = new LinkedList();//스레드 큐 선언
	}

	/**
	 * Atomically release the associated lock and go to sleep on this condition
	 * variable until another thread wakes it using <tt>wake()</tt>. The
	 * 자동적으로 연관되어있는 lock를 풀어주고 다른 스레드가 wake()를 사용하기 전까지 현재 CV는 sleep상태로 전환한다.
	 * current thread must hold the associated lock. The thread will
	 * automatically reacquire the lock before <tt>sleep()</tt> returns.
	 * 최근 스레드는 연관되어 있는 lock를 쥐고있어야 한다. 스레드는 sleep()이 리턴 되기 전까지 자동으로 lock을 재요구한다. 
	 */
	public void sleep() {
		/*
		 * release condition lock
     	disable interrupt
     	add current thread to wait queue
     	make current thread sleep
     	restore interrupt 
     	acquire condition lock
		 */

		Lib.assertTrue(conditionLock.isHeldByCurrentThread()); 

		conditionLock.release();
		boolean intStatus = Machine.interrupt().disable(); //인터럽트 잠금
		KThread temp = KThread.currentThread(); //현재 스레드를 가져옴
		waitQ.add(temp);
		KThread.sleep();//현재 스레드를 block상태로 전환 시킨다.

		conditionLock.acquire();
		Machine.interrupt().restore(intStatus);//인터럽트 재 활성화
	}

	/**
	 * Wake up at most one thread sleeping on this condition variable. The
	 * current thread must hold the associated lock.
	 */
	public void wake() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		/*수도코드
		 * if wait queue is not empty 
        disable interrupt
        remove the first element from wait queue 
        put the first element into ready queue
        restore interrupt
		 */
		KThread temp = waitQ.remove(0);//리스트의 0번째 스레드 리턴하여 temp에 저장, 제거.
		if(temp != null)
		{
			boolean intStatus = Machine.interrupt().disable(); //인터럽트 잠금

			temp.ready(); //레디 큐에 넣는다.

			Machine.interrupt().restore(intStatus); //인터럽트 재 활성화.
		}
	}

	/**
	 * Wake up all threads sleeping on this condition variable. The current
	 * thread must hold the associated lock.
	 */
	public void wakeAll() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		/*
		 * while wait queue is not empty
        invoke wake() 
		 */
		while(!waitQ.isEmpty()){ //리스트의 스레드가 모두 제거될 때 까지 
			wake(); //스레드애 대해서 wake 해준다.
		}
	}




	private int val; 
	private Lock conditionLock;
	private static LinkedList<KThread> waitQ;
}
