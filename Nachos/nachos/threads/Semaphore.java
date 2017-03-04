package nachos.threads;

import nachos.machine.*;

/**
 * A <tt>Semaphore</tt> is a synchronization primitive with an unsigned value.
 * A semaphore has only two operations:
 *
 * <ul>
 * <li><tt>P()</tt>: waits until the semaphore's value is greater than zero,
 * then decrements it.
 * <li><tt>V()</tt>: increments the semaphore's value, and wakes up one thread
 * waiting in <tt>P()</tt> if possible.
 * </ul>
 *
 * <p>
 * Note that this API does not allow a thread to read the value of the
 * semaphore directly. Even if you did read the value, the only thing you would
 * know is what the value used to be. You don't know what the value is now,
 * because by the time you get the value, a context switch might have occurred,
 * and some other thread might have called <tt>P()</tt> or <tt>V()</tt>, so the
 * true value might now be different.
 * 
 * 
 *세마포어: 세마포어는 빈 화장실 열쇠의 갯수라고 보면 됩니다. 즉, 네 개의 화장실에 자물쇠와 열쇠가 있다고 한다면 세마포어는 열쇠의 갯수를 계산하고
 *시작할 때 4의 값을 갖습니다. 이 때는 이용할 수 있는 화장실 수가 동등하게 됩니다. 이제 화장실에 사람이 들어갈 때마다 숫자는 줄어들게 됩니다.
 *4개의 화장실에 사람들이 모두 들어가게 되면 남은 열쇠가 없게 되기 때문에 세마포어 카운트가 0이 됩니다. 이제 다시 한 사람이 화장실에서 볼일을
 *다 보고 나온다면 세마포어의 카운트는 1이 증가됩니다. 따라서 열쇠 하나가 사용가능하기 때문에 줄을 서서 기다리고 있는 다음 사람이 화장실에
 *입장할 수 있게 됩니다.
 *
 *공식적인 정의(심비안 개발자 라이브러리에서 발췌): 세마포어는 공유 리소스에 접근할 수 있는 최대 허용치만큼 동시에 사용자 접근을 할 수 있게 합니다. 
 *쓰레드들은 리소스 접근을 요청할 수 있고 세마포어에서는 카운트가 하나씩 줄어들게 되며 리소스 사용을 마쳤다는 신호를 보내면 세마포어 카운트가 
 *하나 늘어나게 됩니다.
 */
public class Semaphore {
	/**
	 * Allocate a new semaphore.
	 *
	 * @param	initialValue	the initial value of this semaphore.
	 */
	public Semaphore(int initialValue) {//세마포어 생성자
		value = initialValue;
	}

	/**
	 * Atomically wait for this semaphore to become non-zero and decrement it.
	 */
	public void P() {
		boolean intStatus = Machine.interrupt().disable();

		if (value == 0) {//남은 키가 더 이상 없을 때,
			waitQueue.waitForAccess(KThread.currentThread());//대기큐에 넣는다.
			KThread.sleep();
		}
		else {
			value--;
		}

		Machine.interrupt().restore(intStatus);
	}

	/**
	 * Atomically increment this semaphore and wake up at most one other thread
	 * sleeping on this semaphore.
	 */
	public void V() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = waitQueue.nextThread();
		if (thread != null) {
			thread.ready();
		}
		else {
			value++;
		}

		Machine.interrupt().restore(intStatus);
	}

	private static class PingTest implements Runnable {
		PingTest(Semaphore ping, Semaphore pong) {
			this.ping = ping;
			this.pong = pong;
		}

		public void run() {
			for (int i=0; i<10; i++) {
				ping.P();
				pong.V();
			}
		}

		private Semaphore ping;
		private Semaphore pong;
	}

	/**
	 * Test if this module is working.
	 */
	public static void selfTest() {
		Semaphore ping = new Semaphore(0);
		Semaphore pong = new Semaphore(0);

		new KThread(new PingTest(ping, pong)).setName("ping").fork();

		for (int i=0; i<10; i++) {
			ping.V();
			pong.P();
		}
	}

	private int value;
	private ThreadQueue waitQueue =
			ThreadedKernel.scheduler.newThreadQueue(false);
}
