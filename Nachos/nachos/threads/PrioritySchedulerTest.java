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
			System.out.println("-----------"+name+"ī���� ����------------");
			
			System.out.println("�ֿ켱���� : " + p_schduler.getEffectivePriority());
			while(count<10){
				if(name == "A") {//A�� �켱���� ���� Ʈ����
					p_schduler.decreasePriority(); 
					System.out.println("A�� �켱���� ����(���� �켱����) : "+p_schduler.getPriority(KThread.currentThread()));
				}
				count++;
				if(p_schduler.getPriority(KThread.currentThread()) == p_schduler.getEffectivePriority()){
					System.out.println(name+"ī���� :("+count+"/10)");
				}
				else KThread.yield();
			}
			int getid = p_schduler.getId(KThread.currentThread());//���� ������ ���̵� �����´�.
			p_schduler.remove(getid);//�۾��� ��ģ ������� ť���� �����Ѵ�.
			System.out.println("==========="+name+"ī���� ����=============");
		}
	}

	public void test(){

		PriorityScheduler.ThreadState state;
		KThread a = new KThread(new TestThread("A", 5));
		KThread b = new KThread(new TestThread("B", 6));
		KThread c = new KThread(new TestThread("C", 2));
		ThreadQueue sch_Q = p_schduler.newThreadQueue(true); //�����ٷ� ť ����

		p_schduler.kangQ.add(a);
		p_schduler.prioQ.add(5);
		p_schduler.kangQ.add(b);
		p_schduler.prioQ.add(6);
		p_schduler.kangQ.add(c);
		p_schduler.prioQ.add(2);
		/*
		 * ����: �켱���� �� 3���� �����忡 �ʱ� �Ҵ� �� �� �߰��� A�� �켱������ ���ҽ�Ų��.
		 * A�� �켱������ 2���� �۾����� �Ǹ� C�����尡 A���� ���� ���� �����Ѵ�.
		 */
		System.out.println("===================�׽�Ʈ ����====================");
		System.out.println("*****\nA�� �켱���� : 5\nB�� �켱����: 6\nC�� �켱����: 2\n*****");
		
		a.fork();//������ ����
		b.fork();
		c.fork();

		a.join();
		b.join();
		c.join();
	}
}
