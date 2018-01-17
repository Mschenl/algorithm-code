package main;

public class Pool {
	private static final int size = 10; 		//定义缓冲区的大小  10分钟
	public static int pool[] = new int [size +1];
	public static float mean_pool[] = new float [size + 1];
	private static int Old =  size , New = 0;
	public static int M = size+1;
	private static int min = 3;
	/*
	 * 其中  变量 New 指向队首的下一个将要插入元素的下标
	 * 	    变量 Old 指向队尾  Old+1 为下一次将要弹出的元素下标
	 */
	
	public static void addAfter(int num,float mean){
		if(New == Old){
			System.out.println("queue 已满，无法添加");
		}
		else{
			pool[New] = num; 
			mean_pool[New] = mean;
			//System.out.println("插入到下标为" + New +"的元素 "+ pool[New]);
			New = (New + 1)%M;
		}
	}
	
	public static void pollBefore(){
		if((Old + 1)% M == New){
			System.out.println("queue 为空，无法弹出");
		}
		else{
			Old = (Old +1)% M;
			//System.out.println("弹出 下标为" + Old +"的元素 "+ pool[Old] );
		}
	}
	public static void Update(int num ,float mean){
		if(New == Old){		
			//缓冲区已满 可以进行下一步的判断
			pollBefore();
			addAfter(num,mean);
		}
		else{
			//需要进一步添加数据知道缓冲区满
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
			if(i != Old){	//除去old指向元素外其他都要进行遍历
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
