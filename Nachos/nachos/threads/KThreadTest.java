package nachos.threads;

public class KThreadTest {
	public static int a;
	
	public void jtest(){
		System.out.println("*************** join �׽�Ʈ  ���� ******************");
		
		KThread sigma15 = new KThread(new SigmaThread(15));
		
		sigma15.setName("sigma15");
		
		sigma15.fork();
		
		
		
		KThread joinorder2 = new KThread(new JoinOrder(sigma15));
		
		joinorder2.fork();
		
		sigma15.join();
		
		joinorder2.join();
		
		System.out.println("*************** join �׽�Ʈ  ���� ******************");
	}
	
	static class JoinOrder implements Runnable{//�ñ׸� ��꿡���� join����� ������ ����Ʈ

		private int i,S;
		private static KThread forWaitThread;
		JoinOrder(KThread target){
			forWaitThread = target;
			i = 1;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			if (forWaitThread != null) { //������1�� ������ �ʾҴٸ� 
				while(i<21){
					S += i;
					System.out.println("<�ñ׸�"+20+"��>  "+i+" �� ° ����  = " + S);
					if(i == 7){
						System.out.println("�� "+ forWaitThread +"�� ���� ��ٸ� ��� ����..");
						forWaitThread.join(); // ������1�� ������ ���� ��ٸ� ��
						System.out.println("�� "+ forWaitThread +"�� ���� ��ٸ� ��ħ..");
						System.out.println("�� ����� "+a);
					}
					i++;
				}
			}
			
		}
		
	}
	
	public static class SigmaThread implements Runnable{//�ñ׸� ��� ������

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
				System.out.println("	<�ñ׸�"+K+"��>  "+i+" �� ° ����  = " + a);
				a += i;
			}
			System.out.println("	<�ñ׸�"+K+"��> ���� ��  = " + a);
			System.out.println("	�ñ׸� " + K +" ����");
		}
		
	}
	
}
