package org.demo;


public abstract class SerialProtocolBase {
	
	protected static final int CRC_LEN = 2;
	  protected static final int DATA_LEN = 21;
	  protected static final int CMD_LEN = 1;
	  protected static final int STATUS_LEN = 1;
	  protected static final int BASE = 255;
	  protected static final int SERIAL_LEN = 2;
	  protected static final int LENGTH = 26;
	 // protected CommandManager cm = CommandManager.getInstance();
	  protected byte[] serial = { 0, 1 };
	  protected byte[] protocol;
	  protected int length;

	  protected void setSerial(byte[] serial)
	  {
	    this.serial = serial;
	  }

	  public byte[] getProtocol() {
	    return this.protocol;
	  }

	  public void setProtocol(byte[] protocol) {
	    this.protocol = protocol;
	  }

	  protected byte[] byteCombine(byte[] ...bs) {
	    int len = 0;
	    len = calcLengthValue(bs);
	    byte[] values = new byte[len];
	    int index = 0;
	    for (byte[] by : bs) {
	      if (by != null)
	        for (byte b : by)
	          values[(index++)] = b;
	    }
	    return values;
	  }
	  protected int calcLengthValue(byte[][] bs) {
	    int len = 0;
	    for (byte[] by : bs)
	      if (by != null)
	        len += by.length;
	    return len;
	  }
	  protected byte[] int2LengthByte(int len) {
	    return new byte[] { (byte)(len / 255), (byte)(len % 255) };
	  }
	  protected byte[] calcLenthByte(byte[][] bs) {
	    int len = calcLengthValue(bs);
	    return int2LengthByte(len);
	  }
	  protected byte[] getLengthByte(byte[] protocol) {
	    if (protocol.length < 21)
	      return null;
	    return new byte[] { protocol[0], protocol[1] };
	  }
	  protected int getLengthValue(byte[] protocol) {
	    byte[] lenByte = getLengthByte(protocol);
	    if (lenByte == null)
	      return 0;
	    return (lenByte[0] << 8) + lenByte[1];
	  }
	  protected byte[] getCrcByte(byte[] protocol) {//获得校验Crc
	    return new byte[] { protocol[31], protocol[32] };
	  }
	  protected byte[] getTrayByte(byte[] protocol){//获得托盘地址
		  return new byte[]{protocol[27],protocol[28]};
	  }
	  protected byte[] getCMDByte(byte[] protocol) {
	    if (!validate(protocol))
	      return null;
	    return new byte[] { protocol[2] };
	  }

	  protected boolean validate(byte[] protocol) {
	    return protocol.length > 30;
	  }

}
