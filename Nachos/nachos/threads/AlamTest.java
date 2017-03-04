package nachos.threads;

public class AlamTest {
	/*
	 * fork several threads, invoke waitUntil with different time amount.
	 * verify if all the threads waits at least minimum 
	 * amount of time and wakes up at correct time.
	 */
	
	class TestThread implements Runnable{
		String name;
		int count = 1;
		int time = 0;
		Alarm alarm;
		
		TestThread(String Name, Alarm A, int Time){
			name = Name;
			alarm = A; 
			time = Time;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			System.out.println("-----------"+name+"ī���� ����------------");
			while(count<10){
				if(count == 3) {
					System.out.println(name+"�˶� ������ ����");
					alarm.waitUntil(time);
				}
				System.out.println(name+" :"+count);
				count++;
				//KThread.yield();
			}
			System.out.println("==========="+name+"ī���� ����=============");
		}
		
	}
	public void Test(){
		Alarm al = new Alarm();
		KThread a = new KThread(new TestThread("A",al, 1000));
		KThread b = new KThread(new TestThread("B",al, 5000));
		//�׽�Ʈ ����
		System.out.println("#############�˶� �׽�Ʈ ����################");
		System.out.println("A, B������� 1���� 9���� ���ڸ� ī��Ʈ�Ѵ�.");
		System.out.println("�� ������� ī��Ʈ�� 3�϶� ���� �˶���� ���¿� �����Ѵ�.");
		System.out.println("A�� 1000 ����, B�� 5000 ���Ŀ� �ٽ� �����ϵ��� �Ѵ�.");
		System.out.println("#######################################");
		a.fork();
		b.fork();
		b.join();
	}
}
