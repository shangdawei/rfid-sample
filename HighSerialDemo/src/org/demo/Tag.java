package org.demo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.demo.CommonUtil;

public class Tag {
	public static final String DATE_FORMAT = "yyMMddHHmm";//巡更棒时间去掉秒
	public static final String DATE_TIME_FORMAT = "yyMMddHHmmss";
	public static final String DISPLAY_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String DISPLAY_DATE_FORMAT = "yyyy-MM-dd HH:mm";//巡更棒时间去掉秒
	private static final int EPC_LEN = 6;
	private static final int TIME_LEN = 6;
	private static final int MODEL1_LEN = 2;
	private static final int MODEL2_LEN = 2;
	private static final int ROUTER_LEN = 2;
	private static final int RANDOMS_LEN = 2;

	public static final int TYPE_CHECKPOINT = 1;
	public static final int TYPE_STAFF = 2;
	public static final int TYPE_EVENT = 3;
	private byte [] data;
	public Tag(byte[] data){
		if(data.length != EPC_LEN + TIME_LEN + TIME_LEN + MODEL1_LEN +MODEL2_LEN + ROUTER_LEN + RANDOMS_LEN + 4)
			throw new IllegalArgumentException("Incorrect length of data");
		this.data = data;
		calc();
	}
	private void calc(){
		/*for(int i = 0 ; i < TIME_LEN ; i ++){
			time[i] = data[i];
		}
		for(int i = 0 ; i < EPC_LEN ; i++)
			id[i] = data[TIME_LEN + i];*/
		System.out.println("data"+CommonUtil.toHex(data));
		for(int i = 0; i < EPC_LEN ; i++)
			id[i] = data[i];
		for(int i = 0; i < 1 ; i++)//获取年
			time[i] = data[EPC_LEN+i];
		for(int i = 0; i < 1 ; i++)//获取月
			time[i+1] = data[EPC_LEN+3+i];
		for(int i = 0; i < 1 ; i++)//获取日 
			time[i+2] = data[EPC_LEN+2+i];
		for(int i = 0; i < 1 ; i++)//获取时
			time[i+3] = data[EPC_LEN+4+i];
		for(int i = 0; i < 1 ; i++)//获取分
			time[i+4] = data[EPC_LEN+7+i];
		for(int i = 0; i < 1 ; i++)//获取秒
			time[i+5] = data[EPC_LEN+6+i];
		//stoptime
		for(int i = 0; i < 1 ; i++)//获取年
			stoptime[i] = data[EPC_LEN+TIME_LEN+i+2];
		for(int i = 0; i < 1 ; i++)//获取月
			stoptime[i+1] = data[EPC_LEN+TIME_LEN+5+i];
		for(int i = 0; i < 1 ; i++)//获取日 
			stoptime[i+2] = data[EPC_LEN+TIME_LEN+4+i];
		for(int i = 0; i < 1 ; i++)//获取时
			stoptime[i+3] = data[EPC_LEN+TIME_LEN+6+i];
		for(int i = 0; i < 1 ; i++)//获取分
			stoptime[i+4] = data[EPC_LEN+TIME_LEN+9+i];
		for(int i = 0; i < 1 ; i++)//获取秒
			stoptime[i+5] = data[EPC_LEN+TIME_LEN+8+i];
		//4为协议中的时间保留位和星期
		for(int i = 0; i< 1; i++)
			model1[i] = data[EPC_LEN+TIME_LEN*2+4+1+i];
		for(int i = 0; i< 1; i++)
			model1[i+1] = data[EPC_LEN+TIME_LEN*2+4+i];
		for(int i = 0; i< 1; i++)
			model2[i] = data[EPC_LEN+TIME_LEN*2+MODEL1_LEN+4+1+i];
		for(int i = 0; i< 1; i++)
			model2[i+1] = data[EPC_LEN+TIME_LEN*2+MODEL1_LEN+4+i];
		
		for(int i = 0 ; i < ROUTER_LEN ; i ++){
			router[i] = data[EPC_LEN+TIME_LEN*2+MODEL1_LEN+MODEL2_LEN+4+i];
		}
		for(int i = 0 ; i < RANDOMS_LEN ; i ++){
			randoms[i] = data[EPC_LEN+ROUTER_LEN+TIME_LEN*2+MODEL1_LEN+MODEL2_LEN+4+i];
		}
	}
	public Date getTime(){
		String str = CommonUtil.toHex(time);
		return getDate(str);	
	}
	
	public static Date getDate(String str){//巡更棒时间去掉秒
		//str = StringUtils.reverse(str);
		SimpleDateFormat df = new SimpleDateFormat(DATE_TIME_FORMAT);
		try {
			return df.parse(str);
		} catch (ParseException e) {
			return null;
		}
	}
	public static Date getTime(String str){
		str = StringUtils.reverse(str);
		//str = str+"00";
		SimpleDateFormat df = new SimpleDateFormat(DATE_TIME_FORMAT);
		try {
			return df.parse(str);
		} catch (ParseException e) {
			return null;
		}
	}
	public String getDisplayDatetime(){
		Date date = getTime();
		return CommonUtil.getDisplayDatetime(date);
	}
	public static String getDisplayDatetime(String str){
		Date date = getTime(str);
		return CommonUtil.getDisplayDatetime(date);
	}
	public String getId(){
		return CommonUtil.toHex(id);
	}
	public String getStoptime(){
		SimpleDateFormat df = new SimpleDateFormat(DATE_TIME_FORMAT);
		try {
			return CommonUtil.getDisplayDatetime(df.parse(CommonUtil.toHex(stoptime)));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			return null ;
		}
	}
	
	public String getModel1(){
		return CommonUtil.toHex(model1);
	}
	
	public String getModel2(){
		return CommonUtil.toHex(model2);
	}
	
	public String getRouter(){
		return CommonUtil.toHex(router);
	}
	public String getRandoms(){
		return CommonUtil.toHex(randoms);
	}
	private byte[] id = new byte[EPC_LEN];
	private byte[] time = new byte[TIME_LEN];
	private byte[] stoptime = new byte[TIME_LEN];
	private byte[] model1 = new byte[MODEL1_LEN];
	private byte[] model2 = new byte[MODEL2_LEN];
	private byte[] router = new byte[ROUTER_LEN];
	private byte[] randoms = new byte[RANDOMS_LEN];
	
	/**
	 * should be 1(Checkpoint) 2(Staff) 3(Event) 
	 */
	private int type;
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
}
