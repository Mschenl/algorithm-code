package main;

public class Sample {
	//日期	时间		交易量	成功率	响应时间
	public int date,time,num_transaction,response_time;
	public double sucRate;

	public Sample(int date,int time,int num_transaction,double sucRate,int response_time) {
		this.date = date;
		this.time = time;
		this.num_transaction = num_transaction;
		this.sucRate = sucRate;
		this.response_time = response_time;
	}

}
