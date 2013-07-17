package org.demo;

import org.demo.CRC16;
import org.demo.CommonUtil;

public class Request extends SerialProtocolBase
{
  private byte[] header;
  private byte[] tray;
  private byte[] random;
  private byte[] cmd;
  private byte[] crc;
  private byte[] params;
  private byte[] complement;
  private int LENGTH = 31;
  private int random_LEN = 2;

  public byte[] protocol()
  {
    if (this.protocol == null) {
      CRC16 crc16 = new CRC16();
      this.random = CommonUtil.str2Hex("0000");
      calcComplement();
      byte[] cmd_params = (byte[])null;
      cmd_params = byteCombine( this.cmd, this.params, this.complement ,this.tray ,this.random );
      //this.crc = crc16.getCrcByte(CommonUtil.hex2Assic(cmd_params));
      this.crc = crc16.getCrcByte(cmd_params);
     // calcSerial();
      this.protocol = byteCombine(this.header, cmd_params, this.crc );
    }
    return this.protocol;
  }
  
  public void setHeader(byte[] header){
	  this.header = header;
  }
  public byte[] getHeader(){
	  return this.header;
  }
  
  public void setTray(byte[] tray){
	  this.tray = tray;
  }
  public byte[] getTray(){
	  return this.tray;
  }
  
  public byte[] getCmd() {
    return this.cmd;
  }
  /*private void calcSerial(){
	  this.serial[0] = (byte) this.length; 
  }*/
  public void setCmd(byte[] cmd)
  {
    this.cmd = cmd;
  }

  public byte[] getCrc() {
    return this.crc;
  }
  public void setParams(byte[] result) {
	  System.out.println("TX1:"+CommonUtil.toHex(result));
    this.params = result;
  }
  public void setParamsString(String str){
	  this.length = str.length();
	  if(this.length % 2 != 0){
		  str += "0";
	  }
	 //setParams(CommonUtil.str2Hex(str));
	 setParams(str.getBytes());
  }
  private void calcComplement()
  {
    if ((this.params != null) && (this.params.length > 0)){
      this.complement = new byte[LENGTH -this.cmd.length- this.params.length - random_LEN - this.tray.length];}
    //this.complement=new byte[26];}
    	else
      this.complement = new byte[LENGTH-CRC_LEN -this.cmd.length - random_LEN - this.tray.length];
  }
}