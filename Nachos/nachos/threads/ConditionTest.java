package nachos.threads;

import java.util.ArrayList;
import java.util.Random;
/*
 *      //1. 딜러스레드는 1부터 최대 6개의 카드를 제시한다.
		//2. 플레이어1스레드와 플레이어2스레드는 서로 번갈아가며 카드를 가져간다.
		//3. 더 이상 카드의 개수가 없을 때, 각 플레이어스레드는 대기한다(condition2이용).
		//4. 1,2,3을 반복한다.
		//5. 스코어가 21을 먼저 넘는 플레이어가 승리한다.
 */
public class ConditionTest {
	/*
	 * Create a lock variable and a condition variable
       Spawn multiple threads, each thread invoke sleep() inside the lock
       in main thread, invoke wake() to wake up the first thread in the wait queue.
       in main thread, invoke wakeAll() to wake up left sleeping threads.
	 */
	static int[] cardbox = new int[7];//6칸의 카드를 담을 박스
	static Lock lock,testlock;
	static int cardcount=1;
	static int sleepcount;
	static Random r;

	static class TestThread implements Runnable{ //첫번째 테스트 케이스 
		int id;
		Condition2 con;
		TestThread(int id, Condition2 con){
			this.id = id;
			this.con = con;
		}
	
		@Override
		public void run() {
			if(id == 0){//생성하는 스레드
				testlock.acquire();
				cardbox[1] = 10;
				cardbox[2] = 9;
				cardbox[3] = 8;
				System.out.println("데이터 입력"+cardbox[1]+", "+cardbox[2]+", "+cardbox[3]); 
				con.wake();
				testlock.release();
			}
			if(id == 1){//소비하는 스레드
				testlock.acquire();
				System.out.println("소비스레드진입"); 
				while(cardbox[1] == 0){
					System.out.println("데이터가 없으므로 대기"); 
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
			if(id == 0){//딜러스레드일 때
				lock.acquire(); //딜러가 카드를 상자에 넣는 도중 플레이어가 가져가지 못하도록 상자를 잠근다.
				while(sleepcount >-1){
					r = new Random(System.currentTimeMillis());
					
					System.out.println("#딜러가 카드를 나열합니다.");
					for(int i = 1; i<r.nextInt(6)+1; i++){
						cardbox[i] = i;
					}

					for(int i = 1; i<cardbox.length; i++)
						System.out.print(cardbox[i]+" ");
					System.out.println();
					con.wakeAll();//대기중인 스레드를 모두 깨운다.
					
					
					con.sleep();//자신은 대기 상태로 진입한다.

				}
				lock.release(); //플레이어들이 카드를 가져갈 수 있도록 상자의 잠금을 해제한다.
			}
			else{//플레이어스레드 일 때
				while(sleepcount>-1){
					lock.acquire(); //딜러나 다른 플레이어가 상자에 카드를 넣거나 가져가지 못하도록 잠근다.
					if(cardbox[cardcount] == 0){
						System.out.println("플레이어"+id+" : 카드가 더이상 없으므로 대기합니다.");
						if(sleepcount != 0) {//이미 다른플레이어가 대기중이라면,
							cardcount = 0;
							sleepcount = 0;
							con.wakeAll();//다른 플레이어에게 차례를 넘기고
						}
						con.sleep(); //카드가 없으므로 자신은 대기한다.
						sleepcount++;
					}
					myscore += cardbox[cardcount]; //카드 1장 가져옴
					System.out.println("플레이어"+id+"현재 점수: "+myscore);
					cardbox[cardcount] = 0;
					cardcount ++;
					if(myscore >= 17){
						sleepcount = -1;
						System.out.println("플레이어"+id+"승리");
						break;
					}
					lock.release(); //상자의 잠금을 해제
					KThread.yield();
				}
			}
		}
	}
	public static void selfTest(){
		//<테스트케이스 2 wake()이용하기..>
		testlock = new Lock();
		Condition2 c = new Condition2(testlock);
		KThread maker = new KThread(new TestThread(0, c));
		KThread consumer = new KThread(new TestThread(1, c));
		System.out.println("--------------Condition wake() Test Start----------------");
		consumer.fork();
		maker.fork();
		consumer.join();
		maker.join();
		
		
		//<테스트케이스 2 wakeAll()이용하기..>
		//1. 딜러스레드는 1부터 최대 6개의 카드를 제시한다.
		//2. 플레이어1스레드와 플레이어2스레드는 서로 번갈아가며 카드를 가져간다.
		//3. 더 이상 카드의 개수가 없을 때, 각 플레이어스레드는 대기한다(condition2이용).
		//4. 1,2,3을 반복한다.
		//5. 스코어가 17을 먼저 넘는 플레이어가 승리한다.
		lock = new Lock();
		Condition2 condition = new Condition2(lock);
		
		KThread dealer = new KThread(new Player(0, condition)); //첫번째 인자가 0이면 딜러이다.
		KThread player1 = new KThread(new Player(1, condition)); //플레이어1
		KThread player2 = new KThread(new Player(2, condition)); //플레이어2

		System.out.println("--------------Condition wakeAll() Test Start----------------");
		player1.fork();
		player2.fork();
		dealer.fork();
	}
}
