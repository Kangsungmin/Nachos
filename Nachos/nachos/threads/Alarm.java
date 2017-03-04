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
	private class KThreadMaster{//Ÿ�� �������� ������ �ð��� ���ϴ� Ŭ����
		KThread target;
		long time;
		int tag = 0;
		KThreadMaster(KThread Target, long Time){
			target = Target;
			time = Time;
		}
		
		public int getTag(){ //tag�� �����Ѵ�.
			return tag;
		}
		
		public void ticktock(){ //�������� �ð��� �ӽ� �ð��� ���Ѵ�.		
			System.out.println( "�ӽ� �ð� : "+Machine.timer().getTime()+"/ "+"�˶� �����ð� : "+time);
			if(time > Machine.timer().getTime()) tag = 0 ; //���� �ð��� �ȵ�
			else if(time <= Machine.timer().getTime()) tag = 1; //������ �ð��� ������ tag = 1;
			else tag =-1;
			
			System.out.println("��� Ȯ�� : "+tag + "  (1 = �˶��︲/0 = �˶� ���)");
		}
	}
	
	ArrayList<KThreadMaster> Master_List = new ArrayList<>();//����Ʈ ����;
	public Alarm() {
		Machine.timer().setInterruptHandler(new Runnable() {//������ ����
			public void run() { 
				timerInterrupt();// invoke this.timerInterrupt Ÿ�̸� ���ͷ�Ʈ �޼ҵ� ȣ��
				}
		});
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread
	 * that should be run.
	 * Ÿ�̸� ���ͷ�Ʈ �ڵ鷯�� �ӽ�Ÿ�̸ӿ� ���ؼ� 0.5�ʸ��� ���������� ȣ��ȴ�. 
	 * �����ؾ��ϴ� �ٸ� �����尡 ������, ���罺���尡 �纸�ϰ� ���������� context switch�� �Ѵ�.
	 * 
	 */
	public void timerInterrupt(){ 
		/*
		 *     check if threads in waiting queue are due
		 *     put due threads into ready queue
		 *     sleep������ ����Ʈ�� Ȯ���Ͽ��� ���� ����Ʈ�� Ÿ�̸Ӱ����ڰ� �����ϰ�, Ÿ�̸� �����ڰ� Ÿ�̸Ӱ� �Ϸ�Ǿ��ٰ� �� ��,
		 *     �ش� Ÿ�̸� �����ڸ� ����Ʈ���� �����ϰ�, �����ڰ� �����ϴ� �����带 ready���·� ��ȯ�Ѵ�.
		 *     ���� ���罺���尡 �纸(context switching)�Ѵ�.
		 *     
		 */
		boolean intStatus = Machine.interrupt().disable();//���ͷ�Ʈ �Ҵ�
		KThreadMaster nowMaster;
		//ť�� �����Ͱ� ���� ��,
		if(size > 0){ //����Ʈ�� i�� �����Կ� ���� ��ȸ�ϸ� �˻��Ѵ�.
			for(int i = 0; i< size; i++){ //��ũ�� ����Ʈ�� ��ȸ�Ѵ�.
				nowMaster = Master_List.get(i);
				nowMaster.ticktock();//0.5�ʸ��� �ð� üũ�Ѵ�.
				if(nowMaster.getTag() == 1 ){ //Ÿ�̸Ӱ� �� �Ǿ��ٸ�,
					nowMaster.target.ready();//����ϴ� �����带 readyť�� �ְ�
					size = size -1;
					Master_List.remove(i);//�ش� Master�� ����Ʈ���� �����Ѵ�.
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
				wakeTime); //Ÿ�̸� ȣ�������Ƿ�, �ش� ������ �����͸� �����Ѵ�. �Ķ����: Ÿ�ٰ�, ����� �ð�
		boolean intStatus = Machine.interrupt().disable();//���ͷ�Ʈ �Ҵ�
		
		Master_List.add(Master);
		size++;
		KThread.currentThread().sleep();
		
		Machine.interrupt().restore(intStatus);
	}
}

