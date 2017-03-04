package nachos.threads;

import java.util.ArrayList;

import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 *
	 * <p><b>Note</b>: Nachos will not function correctly with more than one
	 * alarm.
	 */
	static int size = 0;
	private class KThreadMaster{//타겟 스레드의 지정된 시간을 비교하는 클래스
		KThread target;
		long time;
		int tag = 0;
		KThreadMaster(KThread Target, long Time){
			target = Target;
			time = Time;
		}
		
		public int getTag(){ //tag를 리턴한다.
			return tag;
		}
		
		public void ticktock(){ //마스터의 시간과 머신 시간을 비교한다.		
			System.out.println( "머신 시간 : "+Machine.timer().getTime()+"/ "+"알람 지정시간 : "+time);
			if(time > Machine.timer().getTime()) tag = 0 ; //아직 시간이 안됨
			else if(time <= Machine.timer().getTime()) tag = 1; //지정된 시간이 지나면 tag = 1;
			else tag =-1;
			
			System.out.println("대기 확인 : "+tag + "  (1 = 알람울림/0 = 알람 대기)");
		}
	}
	
	ArrayList<KThreadMaster> Master_List = new ArrayList<>();//리스트 생성;
	public Alarm() {
		Machine.timer().setInterruptHandler(new Runnable() {//스레드 만듦
			public void run() { 
				timerInterrupt();// invoke this.timerInterrupt 타이머 인터럽트 메소드 호출
				}
		});
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread
	 * that should be run.
	 * 타이머 인터럽트 핸들러는 머신타이머에 의해서 0.5초마다 정기적으로 호출된다. 
	 * 동작해야하는 다른 스레드가 있으면, 현재스레드가 양보하고 강제적으로 context switch를 한다.
	 * 
	 */
	public void timerInterrupt(){ 
		/*
		 *     check if threads in waiting queue are due
		 *     put due threads into ready queue
		 *     sleep스레드 리스트를 확인하여서 만약 리스트에 타이머관리자가 존재하고, 타이머 관리자가 타이머가 완료되었다고 할 시,
		 *     해당 타이머 관리자를 리스트에서 제거하고, 관리자가 관리하는 스레드를 ready상태로 전환한다.
		 *     그후 현재스레드가 양보(context switching)한다.
		 *     
		 */
		boolean intStatus = Machine.interrupt().disable();//인터럽트 불능
		KThreadMaster nowMaster;
		//큐에 마스터가 있을 때,
		if(size > 0){ //리스트를 i가 증가함에 따라 순회하며 검사한다.
			for(int i = 0; i< size; i++){ //마크터 리스트를 순회한다.
				nowMaster = Master_List.get(i);
				nowMaster.ticktock();//0.5초마다 시간 체크한다.
				if(nowMaster.getTag() == 1 ){ //타이머가 다 되었다면,
					nowMaster.target.ready();//대기하던 스레드를 ready큐에 넣고
					size = size -1;
					Master_List.remove(i);//해당 Master를 리스트에서 제거한다.
				}
			}
		}
		Machine.interrupt().restore(intStatus);
		KThread.currentThread().yield();
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks,
	 * waking it up in the timer interrupt handler. The thread must be
	 * woken up (placed in the scheduler ready set) during the first timer
	 * interrupt where
	 *
	 * <p><blockquote>
	 * (current time) >= (WaitUntil called time)+(x)
	 * </blockquote>
	 *
	 * @param	x	the minimum number of clock ticks to wait.
	 *
	 * @see	nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		// for now, cheat just to get something working (busy waiting is bad)
		long wakeTime = Machine.timer().getTime() + x;
		
		KThreadMaster Master = new KThreadMaster(KThread.currentThread(),
				wakeTime); //타이머 호출했으므로, 해당 스레드 마스터를 생성한다. 파라미터: 타겟과, 대기할 시간
		boolean intStatus = Machine.interrupt().disable();//인터럽트 불능
		
		Master_List.add(Master);
		size++;
		KThread.currentThread().sleep();
		
		Machine.interrupt().restore(intStatus);
	}
}

