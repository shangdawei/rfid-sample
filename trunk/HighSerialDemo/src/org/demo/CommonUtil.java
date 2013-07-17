package org.demo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.demo.Response;
import org.demo.Tag;
import org.demo.CommonUtil;

public class CommonUtil {
	public static final SimpleDateFormat DATE_FORMAT_4_DATABASE = new SimpleDateFormat("yyyyMMddHHmmss");
	public static List resultSetToList(ResultSet rs, Class cls)
			throws SQLException, InstantiationException,
			IllegalAccessException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, InvocationTargetException {
		List result = new ArrayList();
		Method[] methods = cls.getMethods();
		ResultSetMetaData meta = rs.getMetaData();
		Object obj = null;
		while (rs.next()) {
			obj = cls.newInstance();
			for (int i = 1; i <= meta.getColumnCount(); i++) {
				String colName = meta.getColumnName(i);
				String setMethodName = "set" + colName.replace("_", "");
				for (int j = 0; j < methods.length; j++) {
					if (methods[j].getName().equalsIgnoreCase(setMethodName)) {
						setMethodName = methods[j].getName();
						Object value = rs.getObject(colName);
						if (value == null) {
							continue;
						}

						Method setMethod = null;
						try {
							setMethod = obj.getClass().getMethod(setMethodName, value.getClass());
						} catch (Exception ex) {
							Class superClz = obj.getClass().getSuperclass();
							setMethod = superClz.getMethod(setMethodName, value.getClass());
						}
						setMethod.invoke(obj, value);
						break;
					}
				}
			}
			result.add(obj);
		}
		return result;
	}

	public static void invokeSimpleSet(Object object, String propName,
			Object value) throws SecurityException, NoSuchFieldException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		Class clz = object.getClass();
		Field field = null;
		try {
			field = clz.getDeclaredField(propName);
		} catch (Exception ex) {
			Class superClz = clz.getSuperclass();
			field = superClz.getDeclaredField(propName);
		}
		field.setAccessible(true);

		Class type = field.getType();
		if (value != null) {
			String str = String.valueOf(value);
			str = StringUtils.isBlank(str) ? null : str;
			if (Boolean.class.equals(type) || boolean.class.equals(type)) {
				value = str == null ? null : Boolean.parseBoolean(str);
			} else if (Integer.class.equals(type) || int.class.equals(type)) {
				value = str == null ? null : Integer.parseInt(str);
			} else if (Double.class.equals(type) || double.class.equals(type)) {
				value = str == null ? null : Double.parseDouble(str);
			} else if (Float.class.equals(type) || float.class.equals(type)) {
				value = str == null ? null : Float.parseFloat(str);
			}
		}

		String methodName = "set" + StringUtils.capitalize(propName);
		Method method = clz.getMethod(methodName, new Class[] { type });
		method.invoke(object, new Object[] { value });
	}

	public static Object invokeSimpleGet(Object object, String propName)
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		Class clz = object.getClass();
		String methodName = "get" + StringUtils.capitalize(propName);
		Object value = null;
		Method method = clz.getMethod(methodName, null);
		value = method.invoke(object, null);
		return value;
	}

	public static String invokeSimpleGetStr(Object object, String propName)
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		String value = "";
		Object v = invokeSimpleGet(object, propName);
		if (v != null) {
			value = String.valueOf(v);
		}
		return value;
	}
	/**
	 * 	����
	 *  @param s
	 *  @return
	 */
	public static String obfuscate(String s) {
		StringBuffer buf = new StringBuffer();
		byte[] b = s.getBytes();
		synchronized (buf) {
			for (int i = 0; i < b.length; ++i) {
				byte b1 = b[i];
				byte b2 = b[(s.length() - (i + 1))];
				int i1 = 127 + b1 + b2;
				int i2 = 127 + b1 - b2;
				int i0 = i1 * 256 + i2;
				String x = Integer.toString(i0, 36);
				switch (x.length()) {
				case 1:
					buf.append('0');
				case 2:
					buf.append('0');
				case 3:
					buf.append('0');
				}
				buf.append(x);
			}

			return buf.toString();
		}
	}
	/**
	 * ����
	 *  @param s
	 *  @return
	 */
	public static String deobfuscate(String s) {
		if (s == null)
			return "";
		byte[] b = new byte[s.length() / 2];
		int l = 0;
		for (int i = 0; i < s.length(); i += 4) {
			String x = s.substring(i, i + 4);
			int i0 = Integer.parseInt(x, 36);
			int i1 = i0 / 256;
			int i2 = i0 % 256;
			b[(l++)] = (byte) ((i1 + i2 - 254) / 2);
		}
		return new String(b, 0, l);
	}
	
	/**
	 * byte����װ��16�����ַ�
	 *  @param data
	 *  @param off
	 *  @param length
	 *  @return
	 */
	public static String toHex(byte[] data, int off, int length) {
		StringBuffer buf = new StringBuffer(data.length * 2);
		for (int i = off; i < length; i++) {
			if ((data[i] & 0xff) < 0x10) {
				buf.append("0");
			}
			buf.append(Long.toString(data[i] & 0xff, 16));
		}
		return buf.toString().toUpperCase();
	}
	
	public static String toDHex(byte[] data, int off, int length) {   
		    // double size, two bytes (hex range) for one byte   
		    StringBuffer buf = new StringBuffer(data.length * 2);   
		    for (int i = off; i < length; i++) {   
		        // don't forget the second hex digit   
		        if (((int) data[i] & 0xff) < 0x10) {   
		            buf.append("0");   
		        }   
		        buf.append(Long.toString((int) data[i] & 0xff, 16));   
		        if (i < data.length - 1) {   
		            buf.append(" ");   
		        }   
	     }   
		    return buf.toString();   
		}  


	/**
	 * Byte����װ��16�����ַ�
	 *  @param data
	 *  @return
	 */
	public static String toHex(byte[] data) {
		return toHex(data, 0, data.length);
	}
	
	public static String toHex(List<Byte> data){
		byte[] by = new byte[data.size()];
		for(int i = 0 ; i < data.size() ;  i++){
			by[i] = data.get(i);
		}
		return toHex(by);
	}
	
	/**
	 * ��16����byte����ת��ASSIC������
	 *  @param hex
	 *  @return
	 */
	/*public static byte[] hex2Assic(byte[] hex){
		String hexStr = toHex(hex);
		return hexStr.getBytes();
	}
	
	/**
	 * ��ASSIC������ת��16��������
	 *  @param assic
	 *  @return
	 */
	/*public static byte[] assic2Hex(byte[] assic) throws NumberFormatException , ArrayIndexOutOfBoundsException{
		byte [] hex = new byte[assic.length / 2];
		int j = 0;
		for(int i = 0 ; i < assic.length ; i += 2){
			hex[j++] = (byte) (Integer.parseInt(((char)assic[i]) + "" , 16) * 16 + Integer.parseInt(((char)assic[i + 1]) + "" , 16));

		}
		return hex;
	}
	
	public static byte[] str2Hex(String str){
		return assic2Hex(str.getBytes());
	}*/
	/**
	 * �Ƿ񷵻�ΪAssicΪ0��
	 *  @param assic
	 *  @return
	 */
	public static boolean hexCheck(byte assic){
		try{
			Integer.parseInt((char)assic + "" , 16);
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}
	public static int byteCompare(byte []b1 , byte [] b2){
		 if(b1 == null || b2 == null)
			 return -1;
		 if(b1.length != b2.length)
			 return 0;
		 for(int i = 0 ; i < b1.length ; i ++){
			 if(b1[i] != b2[i])
				 return 0;
		 }
		 return 1;
	 }
	
	public static String getDisplayDatetime(Date date){
		SimpleDateFormat df = new SimpleDateFormat(Tag.DISPLAY_DATE_TIME_FORMAT);
		return df.format(date);
	}
	public static String getDisplayDate(Date date){
		SimpleDateFormat df = new SimpleDateFormat(Tag.DISPLAY_DATE_FORMAT);
		return df.format(date);
	}
	public static String getDisplayDatetime(String str){
		return getDisplayDatetime(str2Date(str));
	}
	public static Date str2Date(String str){
		try {
			return DATE_FORMAT_4_DATABASE.parse(str);
		} catch (ParseException e) {
			return null;
		}
	}
	public static String date2Str(Date date){
		return DATE_FORMAT_4_DATABASE.format(date);
	}
	public static void main(String []args) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException{
		System.out.println(CommonUtil.toHex(new byte[]{(byte)0x80}));
		Response res = new Response();
		//me 注释
		//res.deprotocol(CommonUtil.str2Hex("030075009D1000109066F60000000000000000000000000008D9"));
		//System.out.println(new String("A5A5A5A5A205008A94D265130004071304020613000407130403060100813228F9".getBytes()));
		/*res.deprotocol("A205008A94D265130004071304020613000407130403060100813228F9".getBytes());
		int numOfCards = ((res.getResult()[0] & 0xFF) << 8);
		numOfCards |= (res.getResult()[1] & 0xFF);
		System.out.println(CommonUtil.toHex(res.getResult()) + "," + numOfCards);*/
		/*SimpleDateFormat df = new SimpleDateFormat("yyMMddHHmmss");
		Date now = new Date();
		System.out.println(df.format(now));
		String yy = df.format(now).substring(0,2);
		String dd = df.format(now).substring(4,6);
		String MM = df.format(now).substring(2,4);
		String hh = df.format(now).substring(6,8);
		String mm = df.format(now).substring(8,10);
		String ss = df.format(now).substring(10,12);
		String cd = "A5A5A5A5A1"+yy+"00"+dd+MM+hh+"00"+ss+mm;
		//byte b = new byte();
		byte[] b = new byte[CommonUtil.str2Hex(cd).length];
		b=CommonUtil.str2Hex(cd).clone();
		System.out.println("b="+CommonUtil.toHex(b));*/  
	
           
		
	} 	

	public static byte[] assic2Hex(byte[] assic) throws NumberFormatException , ArrayIndexOutOfBoundsException{
		byte [] hex = new byte[assic.length / 2];
		int j = 0;
		for(int i = 0 ; i < assic.length ; i += 2){
			hex[j++] = (byte) (Integer.parseInt(((char)assic[i]) + "" , 16) * 16 + Integer.parseInt(((char)assic[i + 1]) + "" , 16));
		}
		return hex;
	}
	
	public static byte[] str2Hex(String str){
		return assic2Hex(str.getBytes());
	}
	
	/*public static byte[] int2Hex(int str){
		byte b = {ox12};
		byte[] hex = new byte[String.valueOf(str).length()];
			hex = new byte[]{(byte) str};
	}*/

	
	public static boolean gettt(String m){
		if(m.equals("3"))
			return true;
		else
		    return false;
	}
	
	public static List<Byte> cloneList(List<Byte> arr){
		List<Byte> list = new ArrayList<Byte>(arr.size());
		for(Byte b : arr){
			list.add(b.byteValue());
		}
		return list;
	}
}
