package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.StringTokenizer;


public class Main {

	private static String readpath ;
	private static ArrayList<Sample> list  = new ArrayList<Sample>();
	//定义每分钟交易量的均值和方差
	private static float mean[] = new float [2400];
	private static float var[] = new float [2400];
	
	private static float range_H[] =  new float[2400];
	private static float range_L[] =  new float[2400];
	
	//定义交易的成功率的均值和方差
	private static double mean_sucRate ;
	private static double var_sucRate ;
	
	//定义交易反应时间的均值和方差
	private static double mean_resTime ;
	private static double var_resTime ;
	
	public static void main(String[] args) {
		//读取文件并获得参数
		readpath = "F://DATA//ATM//2.csv";
		readCSV(readpath,list);
		readpath = "F://DATA//ATM//3.csv";
		readCSV(readpath,list);
		readpath = "F://DATA//ATM//4.csv";
		readCSV(readpath,list);
		numTransactionFeature();
		sucFeature();
		responseTimeFeature();
		//读取待测文件
		String path_need_measure = "F://DATA//ATM//ATM//2n.csv";
		ArrayList<Sample> measure  = new ArrayList<Sample>();
		readCSV(path_need_measure,measure);
		beginMeasure(measure);
		
	}
	
	public static void readCSV(String readpath,ArrayList<Sample> list){
		File inFile = new File(readpath);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inFile));
							boolean sign = false;
			while(reader.ready()){
				String line = reader.readLine();
				StringTokenizer st = new StringTokenizer(line,",");
				int date,time,num_transaction,response_time;
				double sucRate;

				if (st.hasMoreTokens() && sign){
					date = Integer.valueOf(st.nextToken().trim());
					time = Integer.valueOf(st.nextToken().trim());
					num_transaction = Integer.valueOf(st.nextToken().trim());
					sucRate = Double.valueOf(st.nextToken().trim());
					response_time = Integer.valueOf(st.nextToken().trim());
					
					Sample sample = new Sample(date,time,num_transaction,sucRate,response_time);
					list.add(sample);
				}
				else{
					sign = true;
				}
			}
			reader.close();
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	public static void numTransactionFeature(){
		/*
		 * 提取不同日期相同时间（分钟）的交易量
		 * 的平均值和方差作为交易量的特征参数
		 */
		
		ListIterator<Sample> it = list.listIterator();
		while(it.hasNext()){
			Sample sample = it.next();
			int time = sample.time;
			mean[time] += sample.num_transaction;
		}
		int num_date = list.get(list.size()-1).date - list.get(0).date + 1;

		for(int i = 0;i < 2400; i++){
			if(mean[i]>0){
				mean[i] = mean[i]/num_date;
				//System.out.println("时间为"+i+"平均值"+mean[i]);
			}
		}
		
		//求方差
		
		it = list.listIterator();
		while(it.hasNext()){
			Sample sample = it.next();
			int time = sample.time;
			var [time] += (sample.num_transaction - mean[time])*(sample.num_transaction - mean[time]);
		}
		
		for(int i = 0;i < 2400; i++){
			if(var[i]>0){
				var[i] = (float)Math.sqrt(var[i]/num_date);
				//System.out.println("时间为"+i+"的方差为"+var[i]);
			}
		}
		
		for(int i = 0;i < 2400; i++){
			range_H[i] = mean[i] + 3 *var[i];
			range_L[i] = mean[i] - 3 *var[i];
		}
	}
	
	public static void sucFeature(){
		//构建成功率提取模型
		ArrayList<Double> res_combine = new ArrayList<Double>();
		ListIterator<Sample> it = list.listIterator();
		int sum = 0;double suc = 0;
		while(it.hasNext()){
			Sample sample = it.next();
			int num =  sample.num_transaction;
			sum = sum + num; suc = num*sample.sucRate;
			if(sum >= 50){
				res_combine.add(suc/sum);
				sum = 0; suc = 0;//达到阀值清零    否则没有达到阀值继续累加
			}
		}
		//成功率指标特征参数提取
		double suc_total_rate = 0.0;
		ListIterator<Double> it2 = res_combine.listIterator();
		while(it2.hasNext()){					
			double suc_rate_each = it2.next();
			suc_total_rate += suc_rate_each;	
		}
		suc_total_rate = suc_total_rate/res_combine.size();		//计算平均成功率
		//System.out.println("成功率均值为 "+suc_total_rate);
		
		double var = 0.0;
		it2 = res_combine.listIterator();
		while(it2.hasNext()){
			double suc_rate_each = it2.next();
			var += (suc_rate_each - suc_total_rate)*(suc_rate_each - suc_total_rate);	
		}
		var = var/res_combine.size();   //计算的方差
		var = Math.sqrt(var);
		//System.out.println("成功率标准方差为 "+var);
		
		//去除超出3倍标准差的极端异常值
		
		double range_H = suc_total_rate + 3 * var;
		double range_L = suc_total_rate - 3 * var;
		
		it2 = res_combine.listIterator();
		for(int i = 0;i < res_combine.size();i++){
			double suc_rate_each = res_combine.get(i);
			if(suc_rate_each > range_H || suc_rate_each < range_L){
				res_combine.remove(i);		//删除异常值
			}
		}
		
		//再次计算去除异常值后的  成功率和方差
		suc_total_rate = 0.0;
		it2 = res_combine.listIterator();
		while(it2.hasNext()){					//计算成功率总和
			double suc_rate_each = it2.next();
			suc_total_rate += suc_rate_each;	
		}
		mean_sucRate = suc_total_rate = suc_total_rate/res_combine.size();		//计算平均成功率
		System.out.println("成功率均值为 "+suc_total_rate);
		
		var = 0.0;
		it2 = res_combine.listIterator();
		while(it2.hasNext()){
			double suc_rate_each = it2.next();
			var += (suc_rate_each - suc_total_rate)*(suc_rate_each - suc_total_rate);	
		}
		var = var/res_combine.size();   //计算的方差
		var_sucRate = var = Math.sqrt(var);
		System.out.println("成功率标准方差为 "+var);
	}
	
	public static void responseTimeFeature(){
		//响应时间指标特征参数提取
		double mean_time = 0.0;
		ListIterator<Sample> it = list.listIterator();
		//ArrayList<Double> time_list = new ArrayList<Double>();
		while(it.hasNext()){					//计算响应时间的总和
			Sample sample = it.next();
			double time_each = sample.response_time;
			mean_time += time_each;	
		}
		mean_time = mean_time/list.size();
		//System.out.println("响应时间均值为 "+mean_time);
		
		//响应时间的方差
		double var = 0.0;
		it = list.listIterator();
		while(it.hasNext()){					//计算响应时间的总和
			Sample sample = it.next();
			double time_each = sample.response_time;
			var += (time_each - mean_time) * (time_each - mean_time);	
		}
		var = var/list.size();
		var = Math.sqrt(var);
		//System.out.println("响应时间标准方差为 "+ var);
		
		double range_H = mean_time + 3 * var;
		double range_L = mean_time - 3 * var;
		
		mean_time = 0.0; int n = 0;
		it = list.listIterator();
		while(it.hasNext()){					//计算响应时间的总和
			Sample sample = it.next();
			double time_each = sample.response_time;
			if(time_each <= range_H && time_each >= range_L){		
				mean_time += time_each; n++;
			}			
		}
		mean_resTime = mean_time = mean_time/n; 
		System.out.println("响应时间均值为 "+mean_time);
		
		var = 0.0;
		it = list.listIterator();
		while(it.hasNext()){					
			Sample sample = it.next();
			double time_each = sample.response_time;
			if(time_each <= range_H && time_each >= range_L){		
				var += (time_each - mean_time) * (time_each - mean_time);
			}
		}
		var = var/n;
		var_resTime = var = Math.sqrt(var);
		System.out.println("响应时间标准方差为 "+ var);
		
	}

	public static boolean checkNumTransaction(Sample sample){
		int time = sample.time;
		int num_transaction = sample.num_transaction;
		Pool.Update(num_transaction,mean[time]);
		if(range_L[time] > 0){
			if( range_L[time] <= num_transaction){
				//System.out.println("该时刻交易量正常  时间"+time+" 交易量 "+num_transaction);
				//range_H[time] >= num_transaction &&
				return true;
			}
			else{
				System.out.println("1该时刻交易量不正常  时间"+time+" 交易量 "+num_transaction+"range_L[time]"+range_L[time]);
				return false;
			}
		}
		else{
			//采用模型2		&& range_H[time] >= num_transaction
			if(Pool.check()){
				//System.out.println("该时刻交易量正常  时间"+time+" 交易量 "+num_transaction);
				return true;
				
			}
			else{
				//System.out.println("2该时刻交易量不正常  时间"+time+" 交易量 "+num_transaction);
				//System.out.println("range_H[time]:"+range_H[time]);
				return false;
			}
			
		}
	}

	public static void beginMeasure( ArrayList<Sample> measure){
		/*
		 * 1、分行侧网络传输节点故障,前端交易无法上送请求,导致业务量陡降;
		 * 2、分行侧参数数据变更或者配置错误,数据中心后端处理失败率增加,影响交易成功率指标;
		 * 3、数据中心后端处理系统异常(如操作系统 CPU 负荷过大)引起交易处理缓慢,影响交易响应时间指标;
		 * 4、数据中心后端处理系统应用进程异常,导致交易失败或响应缓慢。
		 * 
		 * 故障场景 1 对应交易量出现异常降低的数据			
		 * 故障场景 2 对应交易成功率出现异常降低的数据		
		 * 故障场景 3 对应响应时间出现异常上升的数据			
		 * 故障场景 4 对应响应时间出现异常上升的数据和交易成功率出现异常降低的数据
		 */
		int size = 1000;   int time = 5;
		int n1= 0 ,n2 = 0,n3 = 0,n4 = 0;
		int N1 = 0,N2 = 0,N3 = 0,N4 = 0;
		int Alert_1 = 0,Alert_2 = 0,Alert_3 = 0,Alert_4 = 0;
		for(int i = 0;i < size; i++){
			Sample sample = measure.get(i);
			boolean sign_NumTrans = checkNumTransaction(sample);
			boolean sign_SucRate = checkSucRate(sample);
			boolean sign_ResTime = checkResTime(sample);
			//输出当前信息
			System.out.print("日期 " +sample.date+"时间 "+sample.time+"交易量 "+sample.num_transaction+"成功率"+sample.sucRate+"响应时间"+sample.response_time);
			
			if(sign_NumTrans && sign_SucRate && sign_ResTime){
				System.out.println("该时刻正常");
				n1 = n2 = n3 = n4 =0;
			}
			else{
				System.out.println();
				if(!sign_NumTrans) {
					System.out.print("交易量 该时刻出现异常数据   "); n1 ++; N1 ++;
				}
				else{
					n1 = 0;	
				}
				if(!sign_SucRate) {
					System.out.print("成功率 该时刻出现异常数据   "); n2++; N2 ++;
				}
				else{
					n2 = 0;	
				}
				if(!sign_ResTime) {
					System.out.print("响应时间 该时刻出现异常数据   "); n3++;N3 ++;
				}
				else{
					n3 = 0;	
				}
				if(!sign_ResTime && !sign_SucRate) {
					n4 ++;N4 ++;
				}
				else{
					n4 = 0;	
				}
				System.out.println();
				
				//判断是否报警   即判断异常持续时间超过指定时间 time
				if(n4 == time){
					System.out.println("--------报警 情况4--------");Alert_4 ++;
				}
				else{
					if(n1 == time) {System.out.println("--------报警 情况1--------");Alert_1 ++;}
					if(n2 == time) {System.out.println("--------报警 情况2--------");Alert_2 ++;}
					if(n3 == time) {System.out.println("--------报警 情况3--------");Alert_3 ++;}
				}
			}
		}
		System.out.println("共检验"+size+"分钟数据 其中:");
		System.out.println("1类异常数据出现"+N1+" 次    2类异常数据出现"+N2+" 次    3类异常数据出现"+N3+" 次    4类异常数据出现"+N4+" 次");
		System.out.println("触发1类报警"+Alert_1+" 次    触发2类报警"+Alert_2+" 次    触发3类报警"+Alert_3+" 次    触发4类报警"+Alert_4+" 次");
	}

	public static boolean checkSucRate(Sample sample){
		double range_L = mean_sucRate - 2 *var_sucRate;
		//double range_H = mean_sucRate + 2 *var_sucRate;
		double sucRate = sample.sucRate;
		if( sucRate >= range_L){
			//System.out.println("该时刻交易成功率正常:"+sucRate);sucRate <= range_H &&
			return true;
			
		}
		else{
			//System.out.println("该时刻交易成功率不正常:"+sucRate);
			return false;
		}
	}
	
	public static boolean checkResTime(Sample sample){
		//double range_L = mean_resTime - 2 *var_resTime;
		double range_H = mean_resTime + 2 *var_resTime;
		double resTime = sample.response_time;
		if(resTime <= range_H ){
			//System.out.println("该时刻交易响应时间正常:"+resTime);&& resTime >= range_L
			return true;
		}
		else{
			//System.out.println("该时刻交易响应时间不正常:"+resTime);
			return false;
		}
	}
}







