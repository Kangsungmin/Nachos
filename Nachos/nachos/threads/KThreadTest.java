package nachos.threads;

public class KThreadTest {
	public static int a;
	
	public void jtest(){
		System.out.println("*************** join 테스트  시작 ******************");
		
		KThread sigma15 = new KThread(new SigmaThread(15));
		
		sigma15.setName("sigma15");
		
		sigma15.fork();
		
		
		
		KThread joinorder2 = new KThread(new JoinOrder(sigma15));
		
		joinorder2.fork();
		
		sigma15.join();
		
		joinorder2.join();
		
		System.out.println("*************** join 테스트  종료 ******************");
	}
	
	static class JoinOrder implements Runnable{//시그마 계산에대해 join명령을 내리는 스레트

		private int i,S;
		private static KThread forWaitThread;
		JoinOrder(KThread target){
			forWaitThread = target;
			i = 1;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			if (forWaitThread != null) { //쓰레드1이 끝나지 않았다면 
				while(i<21){
					S += i;
					System.out.println("<시그마"+20+"의>  "+i+" 번 째 연산  = " + S);
					if(i == 7){
						System.out.println("▶ "+ forWaitThread +"에 대한 기다림 명령 시작..");
						forWaitThread.join(); // 쓰레드1이 끝날때 까지 기다린 후
						System.out.println("▶ "+ forWaitThread +"에 대한 기다림 마침..");
						System.out.println("▶ 결과는 "+a);
					}
					i++;
				}
			}
			
		}
		
	}
	
	public static class SigmaThread implements Runnable{//시그마 계산 스레드

		private int K;
		SigmaThread(int K){
			this.K = K;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			for(int i= 1; i < K+1; i++)
			{
				KThread.yield();
				System.out.println("	<시그마"+K+"의>  "+i+" 번 째 연산  = " + a);
				a += i;
			}
			System.out.println("	<시그마"+K+"의> 최종 값  = " + a);
			System.out.println("	시그마 " + K +" 종료");
		}
		
	}
	
}
