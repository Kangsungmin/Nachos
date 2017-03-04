package nachos.threads;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import nachos.machine.Lib;
import nachos.machine.Machine;

/**
 * A scheduler that chooses threads based on their priorities.
 * 
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the thread
 * that has been waiting longest.
 * 
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has the
 * potential to starve a thread if there's always a thread waiting with higher
 * priority.
 * 
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
	/**
	 * Allocate a new priority scheduler.
	 */
	ThreadState lockHolder = null;
	static ArrayList<KThread> kangQ = new ArrayList<>();
	static ArrayList<Integer> prioQ = new ArrayList<>();
	public PriorityScheduler() {
	}

	/**
	 * Allocate a new priority thread queue.
	 * 
	 * @param transferPriority
	 *            <tt>true</tt> if this queue should transfer priority from
	 *            waiting threads to the owning thread.
	 * @return a new priority thread queue.
	 */
	public ThreadQueue newThreadQueue(boolean transferPriority) {

		return new PriorityQueue(transferPriority);
	}

	public int getPriority(KThread thread) {//스레드의 우선순위 가져온다.
		boolean intStatus = Machine.interrupt().disable();
		Lib.assertTrue(Machine.interrupt().disabled());
		for(int i=0; i<kangQ.size(); i++){
			if(kangQ.get(i) == thread) return prioQ.get(i);
		}
		Machine.interrupt().restore(intStatus);
		//return getThreadState(thread).getPriority();
		return -1;
	}

	public int getEffectivePriority(KThread thread) {
		boolean intStatus = Machine.interrupt().disable();
		Lib.assertTrue(Machine.interrupt().disabled());
		Machine.interrupt().restore(intStatus);
		return getThreadState(thread).getEffectivePriority();

	}

	public void setPriority(KThread thread, int priority) {
		boolean intStatus = Machine.interrupt().disable();
		Lib.assertTrue(Machine.interrupt().disabled());

		Lib.assertTrue(priority >= priorityMinimum
				&& priority <= priorityMaximum);

		getThreadState(thread).setPriority(priority);
		Machine.interrupt().restore(intStatus);
	}

	public boolean increasePriority() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = KThread.currentThread();
		// implement me

		int Id = getId(thread);
		prioQ.set(Id, prioQ.get(Id)+1);

		Machine.interrupt().restore(intStatus);
		return true;
	}

	public boolean decreasePriority() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = KThread.currentThread();
		// implement me 
		int Id = getId(thread);
		if(prioQ.get(Id)>0)
			prioQ.set(Id, prioQ.get(Id)-1);

		Machine.interrupt().restore(intStatus);
		return true;
	}
	public int getId(KThread thread){
		for(int i=0; i<kangQ.size(); i++){
			if(kangQ.get(i) == thread) return i;
		}
		return -1;
	}

	public void remove(int id){
		kangQ.remove(id);
		prioQ.remove(id);
	}

	/**
	 * The default priority for a new thread. Do not change this value.
	 */
	public static final int priorityDefault = 1;
	/**
	 * The minimum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMinimum = 0;
	/**
	 * The maximum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMaximum = 7;

	/**
	 * Return the scheduling state of the specified thread.
	 * 
	 * @param thread
	 *            the thread whose scheduling state to return.
	 * @return the scheduling state of the specified thread.
	 */
	protected ThreadState getThreadState(KThread thread) {
		if (thread.schedulingState == null)
			thread.schedulingState = new ThreadState(thread);

		return (ThreadState) thread.schedulingState;
	}

	/**
	 * A <tt>ThreadQueue</tt> that sorts threads by priority.
	 */
	protected class PriorityQueue extends ThreadQueue {
		PriorityQueue(boolean transferPriority) {
			this.transferPriority = transferPriority;
		}

		public void waitForAccess(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			getThreadState(thread).waitForAccess(this);
		}

		public void acquire(KThread thread) {
			boolean intStatus = Machine.interrupt().disable();
			Lib.assertTrue(Machine.interrupt().disabled());
			getThreadState(thread).acquire(this);
			Machine.interrupt().restore(intStatus);
		}

		public KThread nextThread() { //락홀더스레드를 큐에서 제거하고 새로운 스레드에 할당
			boolean intStatus = Machine.interrupt().disable();
			Lib.assertTrue(Machine.interrupt().disabled());


			if (lockHolder != null) {
				lockHolder.donationQueue.remove(this);
				lockHolder.update();
			}
			ThreadState threadState = pickNextThread();
			if (threadState != null) {
				threadState.acquire(this);
				Machine.interrupt().restore(intStatus);
				return threadState.thread;
			}
			else{
				Machine.interrupt().restore(intStatus);
				return null;
			}

		}

		/**
		 * Return the next thread that <tt>nextThread()</tt> would return,
		 * without modifying the state of this queue.
		 * 
		 * @return the next thread that <tt>nextThread()</tt> would return.
		 */
		protected ThreadState pickNextThread() {//다음 스레드를 선택한다.

			KThread result = null;
			int maxPriority = -1;
			for (KThread thread : waitQueue) //웨잇 큐안의
				if (result == null
				|| getEffectivePriority(thread) > maxPriority) {
					result = thread;
					maxPriority = getEffectivePriority(thread);
				}
			if (result == null)
				return null;
			return getThreadState(result);
		}

		public void print() {
			Lib.assertTrue(Machine.interrupt().disabled());
			// implement me (if you want)

		}


		/**
		 * <tt>true</tt> if this queue should transfer priority from waiting
		 * threads to the owning thread.
		 */
		public boolean transferPriority;

		LinkedList<KThread> waitQueue = new LinkedList<KThread>();


	}

	/**
	 * The scheduling state of a thread. This should include the thread's
	 * priority, its effective priority, any objects it owns, and the queue it's
	 * waiting for, if any.
	 * 
	 * @see nachos.threads.KThread#schedulingState
	 */
	protected class ThreadState {
		//////////////////////////스레드 상태 객체
		/**
		 * Allocate a new <tt>ThreadState</tt> object and associate it with the
		 * specified thread.
		 * 
		 * @param thread
		 *            the thread this state belongs to.
		 */
		public ThreadState(KThread thread) {
			this.thread = thread;

			setPriority(priorityDefault);
		}

		/**
		 * Return the priority of the associated thread.
		 * 
		 * @return the priority of the associated thread.
		 */
		public int getPriority() {
			return priority;
		}

		/**
		 * Return the effective priority of the associated thread.
		 * 
		 * @return the effective priority of the associated thread.
		 */
		public int getEffectivePriority() {
			// implement me
			/*
			 if (dirty) {
    		 effective = non-donated priority
    		 for each PriorityQueue pq that I am currently holding
       		 effective = MAX(effective, pq.getEffectivePriority)
 			 }
			 return effective;*/

			//			//for(int i=0; i < donationQueue.size(); i++){
			//
			//				if(kangQ.lockHolder ==  getThreadState(this.thread)){
			//					while(true){//큐에 대해서 반복한다.
			//						KThread check = kangQ.nextThread();
			//						if(check != null)
			//							effectivePriority = Math.max(effectivePriority, 
			//									getThreadState(check).effectivePriority);
			//						else break; // check가 없다면 반복문 탈출
			//					}
			//				}
			//			  }
			priority = -1;
			for(int i = 0; i < kangQ.size(); i++){
				//if(lockHolder == getThreadState(KThread.currentThread())){
				//System.out.println(prioQ.get(i));
				priority = Math.max(priority, prioQ.get(i)); //가장 높은 우선순위 대입
				//}
			}
			return priority;
		}

		/**
		 * Set the priority of the associated thread to the specified value.
		 * 
		 * @param priority
		 *            the new priority.
		 */
		public void setPriority(int priority) {
			if (this.priority == priority)
				return;

			this.priority = priority;

			update();
		}

		/**
		 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
		 * the associated thread) is invoked on the specified priority queue.
		 * The associated thread is therefore waiting for access to the resource
		 * guarded by <tt>waitQueue</tt>. This method is only called if the
		 * associated thread cannot immediately obtain access.
		 * 
		 * @param waitQueue
		 *            the queue that the associated thread is now waiting on.
		 * 
		 * @see nachos.threads.ThreadQueue#waitForAccess
		 */
		//스레드 스테이트 내부 waitForAccess()는 큐에 현재 스레드를 넣는다.
		public void waitForAccess(PriorityQueue waitQueue) {
			waitQueue.waitQueue.add(thread);
			if (lockHolder == null)
				return;
			lockHolder.update();
		}

		/**
		 * Called when the associated thread has acquired access to whatever is
		 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
		 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
		 * <tt>thread</tt> is the associated thread), or as a result of
		 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
		 * 
		 * @see nachos.threads.ThreadQueue#acquire
		 * @see nachos.threads.ThreadQueue#nextThread
		 */
		public void acquire(PriorityQueue waitQueue) {
			waitQueue.waitQueue.remove(thread);
			lockHolder = this;
			donationQueue.add(waitQueue);
			update();
		}
		public void update() {
			effectivePriority = expiredEffectivePriority;
			getEffectivePriority();
		}

		/** The thread with which this object is associated. */
		protected KThread thread;
		/** The priority of the associated thread. */
		protected boolean dirty = false;
		protected int priority = priorityDefault;
		protected int effectivePriority = expiredEffectivePriority;
		protected static final int expiredEffectivePriority = -1;
		protected LinkedList<PriorityQueue> donationQueue = new LinkedList<PriorityQueue>();
	}
}