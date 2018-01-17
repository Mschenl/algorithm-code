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
	//����ÿ���ӽ������ľ�ֵ�ͷ���
	private static float mean[] = new float [2400];
	private static float var[] = new float [2400];
	
	private static float range_H[] =  new float[2400];
	private static float range_L[] =  new float[2400];
	
	//���彻�׵ĳɹ��ʵľ�ֵ�ͷ���
	private static double mean_sucRate ;
	private static double var_sucRate ;
	
	//���彻�׷�Ӧʱ��ľ�ֵ�ͷ���
	private static double mean_resTime ;
	private static double var_resTime ;
	
	public static void main(String[] args) {
		//��ȡ�ļ�����ò���
		readpath = "F://DATA//ATM//2.csv";
		readCSV(readpath,list);
		readpath = "F://DATA//ATM//3.csv";
		readCSV(readpath,list);
		readpath = "F://DATA//ATM//4.csv";
		readCSV(readpath,list);
		numTransactionFeature();
		sucFeature();
		responseTimeFeature();
		//��ȡ�����ļ�
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
		 * ��ȡ��ͬ������ͬʱ�䣨���ӣ��Ľ�����
		 * ��ƽ��ֵ�ͷ�����Ϊ����������������
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
				//System.out.println("ʱ��Ϊ"+i+"ƽ��ֵ"+mean[i]);
			}
		}
		
		//�󷽲�
		
		it = list.listIterator();
		while(it.hasNext()){
			Sample sample = it.next();
			int time = sample.time;
			var [time] += (sample.num_transaction - mean[time])*(sample.num_transaction - mean[time]);
		}
		
		for(int i = 0;i < 2400; i++){
			if(var[i]>0){
				var[i] = (float)Math.sqrt(var[i]/num_date);
				//System.out.println("ʱ��Ϊ"+i+"�ķ���Ϊ"+var[i]);
			}
		}
		
		for(int i = 0;i < 2400; i++){
			range_H[i] = mean[i] + 3 *var[i];
			range_L[i] = mean[i] - 3 *var[i];
		}
	}
	
	public static void sucFeature(){
		//�����ɹ�����ȡģ��
		ArrayList<Double> res_combine = new ArrayList<Double>();
		ListIterator<Sample> it = list.listIterator();
		int sum = 0;double suc = 0;
		while(it.hasNext()){
			Sample sample = it.next();
			int num =  sample.num_transaction;
			sum = sum + num; suc = num*sample.sucRate;
			if(sum >= 50){
				res_combine.add(suc/sum);
				sum = 0; suc = 0;//�ﵽ��ֵ����    ����û�дﵽ��ֵ�����ۼ�
			}
		}
		//�ɹ���ָ������������ȡ
		double suc_total_rate = 0.0;
		ListIterator<Double> it2 = res_combine.listIterator();
		while(it2.hasNext()){					
			double suc_rate_each = it2.next();
			suc_total_rate += suc_rate_each;	
		}
		suc_total_rate = suc_total_rate/res_combine.size();		//����ƽ���ɹ���
		//System.out.println("�ɹ��ʾ�ֵΪ "+suc_total_rate);
		
		double var = 0.0;
		it2 = res_combine.listIterator();
		while(it2.hasNext()){
			double suc_rate_each = it2.next();
			var += (suc_rate_each - suc_total_rate)*(suc_rate_each - suc_total_rate);	
		}
		var = var/res_combine.size();   //����ķ���
		var = Math.sqrt(var);
		//System.out.println("�ɹ��ʱ�׼����Ϊ "+var);
		
		//ȥ������3����׼��ļ����쳣ֵ
		
		double range_H = suc_total_rate + 3 * var;
		double range_L = suc_total_rate - 3 * var;
		
		it2 = res_combine.listIterator();
		for(int i = 0;i < res_combine.size();i++){
			double suc_rate_each = res_combine.get(i);
			if(suc_rate_each > range_H || suc_rate_each < range_L){
				res_combine.remove(i);		//ɾ���쳣ֵ
			}
		}
		
		//�ٴμ���ȥ���쳣ֵ���  �ɹ��ʺͷ���
		suc_total_rate = 0.0;
		it2 = res_combine.listIterator();
		while(it2.hasNext()){					//����ɹ����ܺ�
			double suc_rate_each = it2.next();
			suc_total_rate += suc_rate_each;	
		}
		mean_sucRate = suc_total_rate = suc_total_rate/res_combine.size();		//����ƽ���ɹ���
		System.out.println("�ɹ��ʾ�ֵΪ "+suc_total_rate);
		
		var = 0.0;
		it2 = res_combine.listIterator();
		while(it2.hasNext()){
			double suc_rate_each = it2.next();
			var += (suc_rate_each - suc_total_rate)*(suc_rate_each - suc_total_rate);	
		}
		var = var/res_combine.size();   //����ķ���
		var_sucRate = var = Math.sqrt(var);
		System.out.println("�ɹ��ʱ�׼����Ϊ "+var);
	}
	
	public static void responseTimeFeature(){
		//��Ӧʱ��ָ������������ȡ
		double mean_time = 0.0;
		ListIterator<Sample> it = list.listIterator();
		//ArrayList<Double> time_list = new ArrayList<Double>();
		while(it.hasNext()){					//������Ӧʱ����ܺ�
			Sample sample = it.next();
			double time_each = sample.response_time;
			mean_time += time_each;	
		}
		mean_time = mean_time/list.size();
		//System.out.println("��Ӧʱ���ֵΪ "+mean_time);
		
		//��Ӧʱ��ķ���
		double var = 0.0;
		it = list.listIterator();
		while(it.hasNext()){					//������Ӧʱ����ܺ�
			Sample sample = it.next();
			double time_each = sample.response_time;
			var += (time_each - mean_time) * (time_each - mean_time);	
		}
		var = var/list.size();
		var = Math.sqrt(var);
		//System.out.println("��Ӧʱ���׼����Ϊ "+ var);
		
		double range_H = mean_time + 3 * var;
		double range_L = mean_time - 3 * var;
		
		mean_time = 0.0; int n = 0;
		it = list.listIterator();
		while(it.hasNext()){					//������Ӧʱ����ܺ�
			Sample sample = it.next();
			double time_each = sample.response_time;
			if(time_each <= range_H && time_each >= range_L){		
				mean_time += time_each; n++;
			}			
		}
		mean_resTime = mean_time = mean_time/n; 
		System.out.println("��Ӧʱ���ֵΪ "+mean_time);
		
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
		System.out.println("��Ӧʱ���׼����Ϊ "+ var);
		
	}

	public static boolean checkNumTransaction(Sample sample){
		int time = sample.time;
		int num_transaction = sample.num_transaction;
		Pool.Update(num_transaction,mean[time]);
		if(range_L[time] > 0){
			if( range_L[time] <= num_transaction){
				//System.out.println("��ʱ�̽���������  ʱ��"+time+" ������ "+num_transaction);
				//range_H[time] >= num_transaction &&
				return true;
			}
			else{
				System.out.println("1��ʱ�̽�����������  ʱ��"+time+" ������ "+num_transaction+"range_L[time]"+range_L[time]);
				return false;
			}
		}
		else{
			//����ģ��2		&& range_H[time] >= num_transaction
			if(Pool.check()){
				//System.out.println("��ʱ�̽���������  ʱ��"+time+" ������ "+num_transaction);
				return true;
				
			}
			else{
				//System.out.println("2��ʱ�̽�����������  ʱ��"+time+" ������ "+num_transaction);
				//System.out.println("range_H[time]:"+range_H[time]);
				return false;
			}
			
		}
	}

	public static void beginMeasure( ArrayList<Sample> measure){
		/*
		 * 1�����в����紫��ڵ����,ǰ�˽����޷���������,����ҵ��������;
		 * 2�����в�������ݱ���������ô���,�������ĺ�˴���ʧ��������,Ӱ�콻�׳ɹ���ָ��;
		 * 3���������ĺ�˴���ϵͳ�쳣(�����ϵͳ CPU ���ɹ���)�����״�����,Ӱ�콻����Ӧʱ��ָ��;
		 * 4���������ĺ�˴���ϵͳӦ�ý����쳣,���½���ʧ�ܻ���Ӧ������
		 * 
		 * ���ϳ��� 1 ��Ӧ�����������쳣���͵�����			
		 * ���ϳ��� 2 ��Ӧ���׳ɹ��ʳ����쳣���͵�����		
		 * ���ϳ��� 3 ��Ӧ��Ӧʱ������쳣����������			
		 * ���ϳ��� 4 ��Ӧ��Ӧʱ������쳣���������ݺͽ��׳ɹ��ʳ����쳣���͵�����
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
			//�����ǰ��Ϣ
			System.out.print("���� " +sample.date+"ʱ�� "+sample.time+"������ "+sample.num_transaction+"�ɹ���"+sample.sucRate+"��Ӧʱ��"+sample.response_time);
			
			if(sign_NumTrans && sign_SucRate && sign_ResTime){
				System.out.println("��ʱ������");
				n1 = n2 = n3 = n4 =0;
			}
			else{
				System.out.println();
				if(!sign_NumTrans) {
					System.out.print("������ ��ʱ�̳����쳣����   "); n1 ++; N1 ++;
				}
				else{
					n1 = 0;	
				}
				if(!sign_SucRate) {
					System.out.print("�ɹ��� ��ʱ�̳����쳣����   "); n2++; N2 ++;
				}
				else{
					n2 = 0;	
				}
				if(!sign_ResTime) {
					System.out.print("��Ӧʱ�� ��ʱ�̳����쳣����   "); n3++;N3 ++;
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
				
				//�ж��Ƿ񱨾�   ���ж��쳣����ʱ�䳬��ָ��ʱ�� time
				if(n4 == time){
					System.out.println("--------���� ���4--------");Alert_4 ++;
				}
				else{
					if(n1 == time) {System.out.println("--------���� ���1--------");Alert_1 ++;}
					if(n2 == time) {System.out.println("--------���� ���2--------");Alert_2 ++;}
					if(n3 == time) {System.out.println("--------���� ���3--------");Alert_3 ++;}
				}
			}
		}
		System.out.println("������"+size+"�������� ����:");
		System.out.println("1���쳣���ݳ���"+N1+" ��    2���쳣���ݳ���"+N2+" ��    3���쳣���ݳ���"+N3+" ��    4���쳣���ݳ���"+N4+" ��");
		System.out.println("����1�౨��"+Alert_1+" ��    ����2�౨��"+Alert_2+" ��    ����3�౨��"+Alert_3+" ��    ����4�౨��"+Alert_4+" ��");
	}

	public static boolean checkSucRate(Sample sample){
		double range_L = mean_sucRate - 2 *var_sucRate;
		//double range_H = mean_sucRate + 2 *var_sucRate;
		double sucRate = sample.sucRate;
		if( sucRate >= range_L){
			//System.out.println("��ʱ�̽��׳ɹ�������:"+sucRate);sucRate <= range_H &&
			return true;
			
		}
		else{
			//System.out.println("��ʱ�̽��׳ɹ��ʲ�����:"+sucRate);
			return false;
		}
	}
	
	public static boolean checkResTime(Sample sample){
		//double range_L = mean_resTime - 2 *var_resTime;
		double range_H = mean_resTime + 2 *var_resTime;
		double resTime = sample.response_time;
		if(resTime <= range_H ){
			//System.out.println("��ʱ�̽�����Ӧʱ������:"+resTime);&& resTime >= range_L
			return true;
		}
		else{
			//System.out.println("��ʱ�̽�����Ӧʱ�䲻����:"+resTime);
			return false;
		}
	}
}







