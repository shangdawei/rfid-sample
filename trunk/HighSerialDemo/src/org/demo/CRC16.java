package org.demo;

import org.demo.Response;

public class CRC16 {
	private static final int poly = 0x1021;
	private static final int BASE = 0xFF;
	/**
	 * �õ�CRC����
	 *  @param message
	 *  @return
	 */
	public byte[] getCrcByte(byte[] message){
		int crcValue = getCrc(message);
		return new byte[]{(byte) (crcValue >> 8) , (byte) (crcValue & BASE)};
	}
	
	/**
	 * �õ�CRCֵ
	 *  @param message
	 *  @return
	 */
	public int getCrc(byte[] message)
	{
	    int i, j;
	    short crc_reg = 0;
	    short current;
	    for (i = 0; i < message.length; i++)
	    {
	        current = (short)(message[i] << 8);
	        for (j = 0; j < 8; j++)
	        {
	            if ((short)(crc_reg ^ current) < 0)
	                crc_reg = (short) ((crc_reg << 1) ^ poly);
	            else
	                crc_reg <<= 1;
	            current <<= 1;
	        }
	    }
	    return crc_reg;
	}
	
	/**
	 * ��֤CRC
	 *  @param message
	 *  @param crcByte
	 *  @return
	 */
	public boolean check(byte[] message , byte[]crcByte){
		if(crcByte.length != 2 || message.length < 0)
			return false;
		byte crc[] = getCrcByte(message);
		System.out.println(CommonUtil.toHex(crcByte)+"=="+CommonUtil.toHex(crc));
		if(crc.length != 2)
			return false;
		if(crc[0] == crcByte[0] && crc[1] == crcByte[1])
			return true;
		else
			return false;
	}
	
}
