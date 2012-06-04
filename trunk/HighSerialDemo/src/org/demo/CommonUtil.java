package org.demo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class CommonUtil {

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
	 * 	加密
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
	 * 解密
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
	 * byte数组装成16进制字符串
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

	/**
	 * Byte数组装成16进制字符串
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
	 * 将16进制byte数组转成ASSIC码数组
	 *  @param hex
	 *  @return
	 */
	public static byte[] hex2Assic(byte[] hex){
		String hexStr = toHex(hex);
		return hexStr.getBytes();
	}
	
	/**
	 * 将ASSIC码数组转成16进制数组
	 *  @param assic
	 *  @return
	 */
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
	/**
	 * 是否返回为Assic为0的
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
}
