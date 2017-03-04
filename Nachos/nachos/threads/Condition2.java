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
 *  ����� ������� ����.
 */
public class Condition2 { //ũ��Ƽ�� ���ǿ� ���� ���ؼ� ����ϰ� �ִ� ��������� ����
	/*
	 * ũ��Ƽ�� ���� �� ���� �����尡 ����ϴ� ť�� �ڵ����� sleep�����ش�.
	 */
	/**
	 * Allocate a new condition variable.
	 *
	 * @param	conditionLock	the lock associated with this condition
	 *				variable. The current thread must hold this
	 *				lock whenever it uses <tt>sleep()</tt>,
	 *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
	 */
	public Condition2(Lock conditionLock) {//������
		this.conditionLock = conditionLock;

		waitQ = new LinkedList();//������ ť ����
	}

	/**
	 * Atomically release the associated lock and go to sleep on this condition
	 * variable until another thread wakes it using <tt>wake()</tt>. The
	 * �ڵ������� �����Ǿ��ִ� lock�� Ǯ���ְ� �ٸ� �����尡 wake()�� ����ϱ� ������ ���� CV�� sleep���·� ��ȯ�Ѵ�.
	 * current thread must hold the associated lock. The thread will
	 * automatically reacquire the lock before <tt>sleep()</tt> returns.
	 * �ֱ� ������� �����Ǿ� �ִ� lock�� ����־�� �Ѵ�. ������� sleep()�� ���� �Ǳ� ������ �ڵ����� lock�� ��䱸�Ѵ�. 
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
		boolean intStatus = Machine.interrupt().disable(); //���ͷ�Ʈ ���
		KThread temp = KThread.currentThread(); //���� �����带 ������
		waitQ.add(temp);
		KThread.sleep();//���� �����带 block���·� ��ȯ ��Ų��.

		conditionLock.acquire();
		Machine.interrupt().restore(intStatus);//���ͷ�Ʈ �� Ȱ��ȭ
	}

	/**
	 * Wake up at most one thread sleeping on this condition variable. The
	 * current thread must hold the associated lock.
	 */
	public void wake() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		/*�����ڵ�
		 * if wait queue is not empty 
        disable interrupt
        remove the first element from wait queue 
        put the first element into ready queue
        restore interrupt
		 */
		KThread temp = waitQ.remove(0);//����Ʈ�� 0��° ������ �����Ͽ� temp�� ����, ����.
		if(temp != null)
		{
			boolean intStatus = Machine.interrupt().disable(); //���ͷ�Ʈ ���

			temp.ready(); //���� ť�� �ִ´�.

			Machine.interrupt().restore(intStatus); //���ͷ�Ʈ �� Ȱ��ȭ.
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
		while(!waitQ.isEmpty()){ //����Ʈ�� �����尡 ��� ���ŵ� �� ���� 
			wake(); //������� ���ؼ� wake ���ش�.
		}
	}




	private int val; 
	private Lock conditionLock;
	private static LinkedList<KThread> waitQ;
}
