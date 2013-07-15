package org.demo;

import org.demo.CRC16;
import org.demo.CommonUtil;

public class Request extends SerialProtocolBase
{
  private byte[] cmd;
  private byte[] crc;
  private byte[] params;
  private byte[] complement;

  public byte[] protocol()
  {
    if (this.protocol == null) {
      CRC16 crc16 = new CRC16();
      calcComplement();
      byte[] cmd_params = (byte[])null;
      cmd_params = byteCombine( this.cmd, this.params, this.complement );
      //this.crc = crc16.getCrcByte(CommonUtil.hex2Assic(cmd_params));
      this.crc = crc16.getCrcByte(cmd_params);
      calcSerial();
      this.protocol = byteCombine(this.serial, cmd_params, this.crc );
    }
    return this.protocol;
  }
  public byte[] getCmd() {
    return this.cmd;
  }
  private void calcSerial(){
	  this.serial[0] = (byte) this.length; 
  }
  public void setCmd(byte[] cmd)
  {
    this.cmd = cmd;
  }

  public byte[] getCrc() {
    return this.crc;
  }
  public void setParams(byte[] result) {
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
    if ((this.params != null) && (this.params.length > 0))
      this.complement = new byte[23 - this.params.length - 2];
    else
      this.complement = new byte[21];
  }
}