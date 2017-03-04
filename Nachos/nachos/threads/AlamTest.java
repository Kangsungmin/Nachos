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
			System.out.println("-----------"+name+"카운팅 시작------------");
			while(count<10){
				if(count == 3) {
					System.out.println(name+"알람 대기상태 진입");
					alarm.waitUntil(time);
				}
				System.out.println(name+" :"+count);
				count++;
				//KThread.yield();
			}
			System.out.println("==========="+name+"카운팅 종료=============");
		}
		
	}
	public void Test(){
		Alarm al = new Alarm();
		KThread a = new KThread(new TestThread("A",al, 1000));
		KThread b = new KThread(new TestThread("B",al, 5000));
		//테스트 설명
		System.out.println("#############알람 테스트 시작################");
		System.out.println("A, B스레드는 1부터 9까지 숫자를 카운트한다.");
		System.out.println("두 스레드는 카운트가 3일때 각각 알람대기 상태에 진입한다.");
		System.out.println("A는 1000 이후, B는 5000 이후에 다시 동작하도록 한다.");
		System.out.println("#######################################");
		a.fork();
		b.fork();
		b.join();
	}
}
