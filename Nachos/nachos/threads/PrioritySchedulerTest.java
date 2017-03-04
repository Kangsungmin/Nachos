package nachos.threads;

import nachos.threads.AlamTest.TestThread;
import nachos.threads.PriorityScheduler.PriorityQueue;

public class PrioritySchedulerTest {
	PriorityScheduler p_schduler = new PriorityScheduler();
	class TestThread implements Runnable{
		String name;
		int count = 1;
		int priority;

		TestThread(String Name, int priority){
			name = Name;
			this.priority = priority;
		}
		@Override
		public void run() {
			System.out.println("-----------"+name+"카운팅 시작------------");
			
			System.out.println("최우선순위 : " + p_schduler.getEffectivePriority());
			while(count<10){
				if(name == "A") {//A의 우선순위 감소 트리거
					p_schduler.decreasePriority(); 
					System.out.println("A의 우선순위 감소(현재 우선순위) : "+p_schduler.getPriority(KThread.currentThread()));
				}
				count++;
				if(p_schduler.getPriority(KThread.currentThread()) == p_schduler.getEffectivePriority()){
					System.out.println(name+"카운팅 :("+count+"/10)");
				}
				else KThread.yield();
			}
			int getid = p_schduler.getId(KThread.currentThread());//현재 스레드 아이디 가져온다.
			p_schduler.remove(getid);//작업을 마친 스레드는 큐에서 제거한다.
			System.out.println("==========="+name+"카운팅 종료=============");
		}
	}

	public void test(){

		PriorityScheduler.ThreadState state;
		KThread a = new KThread(new TestThread("A", 5));
		KThread b = new KThread(new TestThread("B", 6));
		KThread c = new KThread(new TestThread("C", 2));
		ThreadQueue sch_Q = p_schduler.newThreadQueue(true); //스케줄러 큐 생성

		p_schduler.kangQ.add(a);
		p_schduler.prioQ.add(5);
		p_schduler.kangQ.add(b);
		p_schduler.prioQ.add(6);
		p_schduler.kangQ.add(c);
		p_schduler.prioQ.add(2);
		/*
		 * 설명: 우선순위 를 3개의 스레드에 초기 할당 한 후 중간에 A의 우선순위를 감소시킨다.
		 * A의 우선순위가 2보다 작아지게 되면 C스레드가 A보다 먼저 돌기 시작한다.
		 */
		System.out.println("===================테스트 시작====================");
		System.out.println("*****\nA의 우선순위 : 5\nB의 우선순위: 6\nC의 우선순위: 2\n*****");
		
		a.fork();//스레드 실행
		b.fork();
		c.fork();

		a.join();
		b.join();
		c.join();
	}
}
