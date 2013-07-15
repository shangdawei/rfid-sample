package org.demo;

import org.eclipse.core.commands.Command;

public class Response extends SerialProtocolBase
{
  private byte[] crc;
  private byte[] status;
  private byte[] result;
  private byte[] resultcrc;
 // private byte[] complement;
  private String tmpserial;
  

  public byte[] getCrc()
  {
    return this.crc;
  }

  public byte[] getStatus() {
    return this.status;
  }

  public byte[] getResult() {
    return this.result;
  }
  
  public byte[] getResultcrc(){
	  return this.resultcrc;
  }
  
  public void setTmpserial(String tmpserial){
	  this.tmpserial = tmpserial;
  }
  public String getTmpserial(){
	  return this.tmpserial;
  }
  public String getResultString(){
	  String str = CommonUtil.toHex(result);
	  if(this.length % 2 != 0){//數據長度不為偶數
		  str = str.substring(0 , str.length() - 1);
	  }
	  return str;
  }
  public void deprotocol(byte[] protocol, int index)
  {
    if ((protocol == null) || (protocol.length < 16))
      return;
    this.protocol = protocol;
    this.crc = getCrcByte(protocol);
    this.status = getStatusByte(protocol);
   // this.tmpserial = CommonUtil.toHex(getSerialByte(protocol));
   
   // CommandManager.Command c = this.cm.getCommand(this.status);
    //if ((CommonUtil.byteCompare(this.status, SEND_CRC_ERROR) > 0) || (CommonUtil.byteCompare(this.status, PATROL_AND_TRANS_ERROR) > 0))   	
    	//this.result = getResultByte(protocol, 21);
   // else
    if(CommonUtil.toHex(status).equals("A1")){
    	this.result = getResultByte2(protocol, 4);
    	this.resultcrc = getResultCrc(protocol, 26);
    }else if(CommonUtil.toHex(status).equals("A2")){
    	this.result = getResultByte(protocol, 26);
    	this.resultcrc = getResultCrc(protocol, 26);
    }
      //this.result = getResultByte(protocol, getDataLen(c , serial));
    //calcComplement(protocol);
  }
  /*private int getDataLen(Command c , int index){
	  List l= c.getRxDataLen();
	  for(int i=0;i<l.size();i++){
		  System.out.println("index="+((Integer)c.getRxDataLen().get(i)).intValue());
	  }
	  this.length = ((Integer)c.getRxDataLen().get(index)).intValue();
	  if(this.length < 0){
		  this.length = this.serial[0];
		  return this.length % 2 == 0 ? this.length / 2 : (this.length / 2 + 1);
	  }else
		  return this.length;
  }*/
  
 /* private int getDataLen(Command c , byte[] serial){
	  if(c.getRxDataLen().size()>1){
	      this.length=c.getRxDataLen().get(CommonUtil.toHex(serial));
	  }else{
		  this.length=c.getRxDataLen().get("0");
	  }
	  if(this.length < 0){
		  this.length = this.serial[0];
		  return this.length % 2 == 0 ? this.length / 2 : (this.length / 2 + 1);
	  }else
		  return this.length;
  }*/
 /* private void calcComplement(byte[] protocol) {
    int start = 0;
    if ((this.result != null) && (this.result.length > 0)) {
      start = this.result.length;
    }
    this.complement = new byte[21 - this.result.length];
    for (int i = 0; i < this.complement.length; i++)
      this.complement[i] = protocol[(3 + start + i)];
  }*/
  public void deprotocol(byte[] protocol)
  {
    deprotocol(protocol, 0);
  }

  public void add(Response ...res) {
    for (Response r : res)
      add(r);
  }

  public void add(Response res) {
    this.result = byteCombine( this.result, res.getResult() );
    this.status = res.getStatus();
    CRC16 crc16 = new CRC16();
    //this.crc = crc16.getCrcByte(CommonUtil.hex2Assic(getData()));
    this.crc = crc16.getCrcByte(getData());
  }
  public byte[] getData() {
    return byteCombine( this.status, this.resultcrc);
  }
  protected byte[] getResultByte(byte[] protocol, int len) {
    if (!validate(protocol))
      return null;
    byte[] result = new byte[len];
    for (int i = 0; i < result.length; i++) {
      result[i] = protocol[(1 + i)];
    }
    return result;
  }
  
  protected byte[] getResultCrc(byte[] protocol, int len){
	  if (!validate(protocol))
	      return null;
	    byte[] result = new byte[len];
	    for (int i = 0; i < result.length; i++) {
	      result[i] = protocol[(1 + i)];
	    }
	    return result;
  }
  
  protected byte[] getResultByte2(byte[] protocol, int len) {
	    if (!validate(protocol))
	      return null;
	    byte[] result = new byte[len];
	    for (int i = 0; i < result.length; i++) {
	      result[i] = protocol[(23 + i)];
	    }
	    return result;
	  }

  protected byte[] getStatusByte(byte[] protocol) {//获得状态字
    if (!validate(protocol))
      return null;
    return new byte[] { protocol[0] };
  }
  
  public boolean checkCrc(){
	  CRC16 crc = new CRC16();
	  //return crc.check(CommonUtil.hex2Assic(getData()), this.crc);
	  return crc.check(getData(), this.crc);
  }
  /**
   * �ж��Ƿ�����ָ��status
   * @param status
   * @return
   */
  public boolean checkStatus(byte []status){
	  return CommonUtil.byteCompare(getStatus(), status) > 0;
  }
}
