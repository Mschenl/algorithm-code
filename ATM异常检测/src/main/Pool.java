package main;

public class Pool {
	private static final int size = 10; 		//���建�����Ĵ�С  10����
	public static int pool[] = new int [size +1];
	public static float mean_pool[] = new float [size + 1];
	private static int Old =  size , New = 0;
	public static int M = size+1;
	private static int min = 3;
	/*
	 * ����  ���� New ָ����׵���һ����Ҫ����Ԫ�ص��±�
	 * 	    ���� Old ָ���β  Old+1 Ϊ��һ�ν�Ҫ������Ԫ���±�
	 */
	
	public static void addAfter(int num,float mean){
		if(New == Old){
			System.out.println("queue �������޷����");
		}
		else{
			pool[New] = num; 
			mean_pool[New] = mean;
			//System.out.println("���뵽�±�Ϊ" + New +"��Ԫ�� "+ pool[New]);
			New = (New + 1)%M;
		}
	}
	
	public static void pollBefore(){
		if((Old + 1)% M == New){
			System.out.println("queue Ϊ�գ��޷�����");
		}
		else{
			Old = (Old +1)% M;
			//System.out.println("���� �±�Ϊ" + Old +"��Ԫ�� "+ pool[Old] );
		}
	}
	public static void Update(int num ,float mean){
		if(New == Old){		
			//���������� ���Խ�����һ�����ж�
			pollBefore();
			addAfter(num,mean);
		}
		else{
			//��Ҫ��һ���������֪����������
			addAfter(num,mean);
		}
	}
	public static boolean isFull(){
		if(New == Old){
			return true;
		}
		else{
			return false;
		}
	}
	public static boolean check(){
		if(!isFull()) return true;
		int n = 0;
		for(int i = 0;i < M; i++){
			if(i != Old){	//��ȥoldָ��Ԫ����������Ҫ���б���
				if(pool[i] >= mean_pool[i]) n++;
			}
		}
		if(n < min){
			return false;
		}
		else{
			return true;
		}
	}

}
