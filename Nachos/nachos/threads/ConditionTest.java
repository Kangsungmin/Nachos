package nachos.threads;

import java.util.ArrayList;
import java.util.Random;
/*
 *      //1. ����������� 1���� �ִ� 6���� ī�带 �����Ѵ�.
		//2. �÷��̾�1������� �÷��̾�2������� ���� �����ư��� ī�带 ��������.
		//3. �� �̻� ī���� ������ ���� ��, �� �÷��̾����� ����Ѵ�(condition2�̿�).
		//4. 1,2,3�� �ݺ��Ѵ�.
		//5. ���ھ 21�� ���� �Ѵ� �÷��̾ �¸��Ѵ�.
 */
public class ConditionTest {
	/*
	 * Create a lock variable and a condition variable
       Spawn multiple threads, each thread invoke sleep() inside the lock
       in main thread, invoke wake() to wake up the first thread in the wait queue.
       in main thread, invoke wakeAll() to wake up left sleeping threads.
	 */
	static int[] cardbox = new int[7];//6ĭ�� ī�带 ���� �ڽ�
	static Lock lock,testlock;
	static int cardcount=1;
	static int sleepcount;
	static Random r;

	static class TestThread implements Runnable{ //ù��° �׽�Ʈ ���̽� 
		int id;
		Condition2 con;
		TestThread(int id, Condition2 con){
			this.id = id;
			this.con = con;
		}
	
		@Override
		public void run() {
			if(id == 0){//�����ϴ� ������
				testlock.acquire();
				cardbox[1] = 10;
				cardbox[2] = 9;
				cardbox[3] = 8;
				System.out.println("������ �Է�"+cardbox[1]+", "+cardbox[2]+", "+cardbox[3]); 
				con.wake();
				testlock.release();
			}
			if(id == 1){//�Һ��ϴ� ������
				testlock.acquire();
				System.out.println("�Һ񽺷�������"); 
				while(cardbox[1] == 0){
					System.out.println("�����Ͱ� �����Ƿ� ���"); 
					con.sleep();
				}
				System.out.println(cardbox[1]+cardbox[2]+cardbox[3]); 
				cardbox[1] = 0;
				cardbox[2] = 0;
				cardbox[3] = 0;
				
				testlock.release();
			}
			
		}
		
	}
	static class Player implements Runnable{
		int id, myscore;
		Condition2 con;
		Player(int ID, Condition2 CON){
			id = ID;
			con = CON;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(id == 0){//������������ ��
				lock.acquire(); //������ ī�带 ���ڿ� �ִ� ���� �÷��̾ �������� ���ϵ��� ���ڸ� ��ٴ�.
				while(sleepcount >-1){
					r = new Random(System.currentTimeMillis());
					
					System.out.println("#������ ī�带 �����մϴ�.");
					for(int i = 1; i<r.nextInt(6)+1; i++){
						cardbox[i] = i;
					}

					for(int i = 1; i<cardbox.length; i++)
						System.out.print(cardbox[i]+" ");
					System.out.println();
					con.wakeAll();//������� �����带 ��� �����.
					
					
					con.sleep();//�ڽ��� ��� ���·� �����Ѵ�.

				}
				lock.release(); //�÷��̾���� ī�带 ������ �� �ֵ��� ������ ����� �����Ѵ�.
			}
			else{//�÷��̾���� �� ��
				while(sleepcount>-1){
					lock.acquire(); //������ �ٸ� �÷��̾ ���ڿ� ī�带 �ְų� �������� ���ϵ��� ��ٴ�.
					if(cardbox[cardcount] == 0){
						System.out.println("�÷��̾�"+id+" : ī�尡 ���̻� �����Ƿ� ����մϴ�.");
						if(sleepcount != 0) {//�̹� �ٸ��÷��̾ ������̶��,
							cardcount = 0;
							sleepcount = 0;
							con.wakeAll();//�ٸ� �÷��̾�� ���ʸ� �ѱ��
						}
						con.sleep(); //ī�尡 �����Ƿ� �ڽ��� ����Ѵ�.
						sleepcount++;
					}
					myscore += cardbox[cardcount]; //ī�� 1�� ������
					System.out.println("�÷��̾�"+id+"���� ����: "+myscore);
					cardbox[cardcount] = 0;
					cardcount ++;
					if(myscore >= 17){
						sleepcount = -1;
						System.out.println("�÷��̾�"+id+"�¸�");
						break;
					}
					lock.release(); //������ ����� ����
					KThread.yield();
				}
			}
		}
	}
	public static void selfTest(){
		//<�׽�Ʈ���̽� 2 wake()�̿��ϱ�..>
		testlock = new Lock();
		Condition2 c = new Condition2(testlock);
		KThread maker = new KThread(new TestThread(0, c));
		KThread consumer = new KThread(new TestThread(1, c));
		System.out.println("--------------Condition wake() Test Start----------------");
		consumer.fork();
		maker.fork();
		consumer.join();
		maker.join();
		
		
		//<�׽�Ʈ���̽� 2 wakeAll()�̿��ϱ�..>
		//1. ����������� 1���� �ִ� 6���� ī�带 �����Ѵ�.
		//2. �÷��̾�1������� �÷��̾�2������� ���� �����ư��� ī�带 ��������.
		//3. �� �̻� ī���� ������ ���� ��, �� �÷��̾����� ����Ѵ�(condition2�̿�).
		//4. 1,2,3�� �ݺ��Ѵ�.
		//5. ���ھ 17�� ���� �Ѵ� �÷��̾ �¸��Ѵ�.
		lock = new Lock();
		Condition2 condition = new Condition2(lock);
		
		KThread dealer = new KThread(new Player(0, condition)); //ù��° ���ڰ� 0�̸� �����̴�.
		KThread player1 = new KThread(new Player(1, condition)); //�÷��̾�1
		KThread player2 = new KThread(new Player(2, condition)); //�÷��̾�2

		System.out.println("--------------Condition wakeAll() Test Start----------------");
		player1.fork();
		player2.fork();
		dealer.fork();
	}
}
