package org.demo;

//import javax.comm.*;

import gnu.io.CommPortIdentifier;
import gnu.io.CommPortOwnershipListener;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;

/**
 * A class that handles the details of a serial connection. Reads from one
 * TextArea and writes to a second TextArea. Holds the state of the connection.
 */
public class SerialConnection extends Thread implements
		SerialPortEventListener, CommPortOwnershipListener {
	//private static Logger logger = Logger.getLogger(SerialConnection.class);
	private SerialParameters parameters;
	private OutputStream os;
	private InputStream is;
	private CommPortIdentifier portId;
	private SerialPort sPort;;
	private boolean opened;
	private int timeout = 200;
	private int loop = 25;
	private boolean receivedBlocked = false;
	private boolean plugged = true;
	private List<List<Byte>> bufferList = new ArrayList<List<Byte>>();
	List<Byte> buffer = new ArrayList<Byte>();
	private boolean isInterrupt = false;
	/**
	 * Creates a SerialConnection object and initilizes variables passed in as
	 * params.
	 * 
	 * @param parent
	 *            A SerialDemo object.
	 * @param parameters
	 *            A SerialParameters object.
	 * @param messageAreaOut
	 *            The TextArea that messages that are to be sent out of the
	 *            serial port are entered into.
	 * @param messageAreaIn
	 *            The TextArea that messages comming into the serial port are
	 *            displayed on.
	 */
	public SerialConnection(SerialParameters parameters) {
		this.parameters = parameters;
		opened = false;
	}
	
	public void setSerialParameters(SerialParameters parameters){
		this.parameters = parameters;
	}

	/**
	 * Attempts to open a serial connection and streams using the parameters in
	 * the SerialParameters object. If it is unsuccesfull at any step it returns
	 * the port to a closed state, throws a
	 * <code>SerialConnectionException</code>, and returns.
	 * 
	 * Gives a timeout of 30 seconds on the portOpen to allow other applications
	 * to reliquish the port if have it open and no longer need it.
	 */
	public void openConnection() throws SerialConnectionException {
		// Obtain a CommPortIdentifier object for the port you want to open.
		try {
			portId = CommPortIdentifier.getPortIdentifier(parameters
					.getPortName());
		} catch (NoSuchPortException e) {
			throw new SerialConnectionException("No such port");
		}

		// Open the port represented by the CommPortIdentifier object. Give
		// the open call a relatively long timeout of 30 seconds to allow
		// a different application to reliquish the port if the user
		// wants to.
		try {
			try {
				sPort = (SerialPort) portId.open("SerialDemo", 30000);
			} catch (PortInUseException e) {
				throw new SerialConnectionException(e.getMessage());
			}
		} catch (UnsatisfiedLinkError e) {
			throw new SerialConnectionException(e.getMessage());
		}

		// Set the parameters of the connection. If they won't set, close the
		// port before throwing an exception.
		try {
			setConnectionParameters();
		} catch (SerialConnectionException e) {
			sPort.close();
			throw e;
		}

		// Open the input and output streams for the connection. If they won't
		// open, close the port before throwing an exception.
		try {
			os = sPort.getOutputStream();
			is = sPort.getInputStream();
		} catch (IOException e) {
			sPort.close();
			throw new SerialConnectionException("Error opening i/o streams");

		}
		try {
			sPort.addEventListener(this);
		} catch (TooManyListenersException e) {
			sPort.close();
			throw new SerialConnectionException("too many listeners added");
		}

		// Set notifyOnDataAvailable to true to allow event driven input.
		sPort.notifyOnDataAvailable(true);

		// Set notifyOnBreakInterrup to allow event driven break handling.
		sPort.notifyOnBreakInterrupt(true);

		// Set receive timeout to allow breaking out of polling loop during
		// input handling.
		try {
			sPort.enableReceiveTimeout(30);
		} catch (UnsupportedCommOperationException e) {
		}

		// Add ownership listener to allow ownership event handling.
		portId.addPortOwnershipListener(this);
		opened = true;
		plugged = true;
	}

	/**
	 * Sets the connection parameters to the setting in the parameters object.
	 * If set fails return the parameters object to origional settings and throw
	 * exception.
	 */
	public void setConnectionParameters() throws SerialConnectionException {

		// Save state of parameters before trying a set.
		int oldBaudRate = sPort.getBaudRate();
		int oldDatabits = sPort.getDataBits();
		int oldStopbits = sPort.getStopBits();
		int oldParity = sPort.getParity();
		int oldFlowControl = sPort.getFlowControlMode();

		// Set connection parameters, if set fails return parameters object
		// to original state.
		try {
			sPort.setSerialPortParams(parameters.getBaudRate(), parameters
					.getDatabits(), parameters.getStopbits(), parameters
					.getParity());
		} catch (UnsupportedCommOperationException e) {
			parameters.setBaudRate(oldBaudRate);
			parameters.setDatabits(oldDatabits);
			parameters.setStopbits(oldStopbits);
			parameters.setParity(oldParity);
			throw new SerialConnectionException("Unsupported parameter");
		}

		// Set flow control.
		try {
			sPort.setFlowControlMode(parameters.getFlowControlIn()
					| parameters.getFlowControlOut());
		} catch (UnsupportedCommOperationException e) {
			throw new SerialConnectionException("Unsupported flow control");
		}
	}

	/**
	 * Close the port and clean up associated elements.
	 */
	public void closeConnection() {
		// If port is alread closed just return.
		if (!opened) {
			return;
		}

		// Remove the key listener.
		// messageAreaOut.removeKeyListener(keyHandler);

		// Check to make sure sPort has reference to avoid a NPE.
		if (sPort != null) {
			try {
				// close the i/o streams.
				os.close();
				is.close();
			} catch (IOException e) {
				System.err.println(e);
			}

			// Close the port.
			try{
				sPort.close();
			}catch (Exception e) {
			}
			// Remove the ownership listener.
			portId.removePortOwnershipListener(this);
		}
		opened = false;
	}
	public void stopRead(){
		isInterrupt = true;
	}
	public static byte[] str2ByteArr(String str) {
		byte[] b = new byte[str.length() / 2];
		for (int i = 0; i < str.length() / 2; i++) {
			b[i] = (byte) (0xff & Integer.parseInt(str.substring(i * 2,
					i * 2 + 2), 16));
		}
		return b;
	}

	/**
	 * 读取一整条
	 * 
	 * @param req
	 * @return
	 * @throws IOException
	 * @throws SerialConnectionException
	 * @throws ReceivedException
	 */
	public Response readSingle(Request req) throws IOException,
			SerialConnectionException, ReceivedException {
		read(req);
		Response res = new Response();
		res.deprotocol(getSingleReceived());
		if (res.getResult() == null)
			throw new ReceivedException("srs.device.revceived.null");
		return res;
	}

	/**
	 * 读取被拆分的一整条
	 * 
	 * @param req
	 * @throws SerialConnectionException
	 * @throws IOException
	 */
	private void read(Request req) throws SerialConnectionException,
			IOException {
		if (!opened)
			throw new SerialConnectionException("Serial port was closed!");
		clearBuffer();
		//os.write(CommonUtil.hex2Assic(req.protocol()));
		os.write(req.protocol());
	}
	
	/*
	 * 发送
	 */

	public void read(byte[] cmd) throws SerialConnectionException,
	  IOException {
		if (!opened)
			throw new SerialConnectionException("Serial port was closed!");
		clearBuffer();
		os.write(cmd);
	}
	/*public Response readSeqeratedSingle(Request req)
			throws SerialConnectionException, IOException, ReceivedException {
		read(req);
		List<byte[]> received = getSeperatedReceived();
		Response res = new Response();
		List<Response> mutil = new ArrayList<Response>();
		int index = 0;
		for (byte[] by : received) {
			Response r = new Response();
			if (r.validate(by)) {
				r.deprotocol(by, index++);
				mutil.add(r);
				logger.info(CommonUtil.toHex(r.getResult()));
			}
		}
		int size = mutil.size();
		switch (size) {
		case 0:
			throw new ReceivedException("接受的字串为空或不符合规范！");
		case 1:
			return mutil.get(0);
		default:
			for (Response r : mutil) {
				res.add(r);
			}
			return res;
		}
	}*/

	public Response[] readMutil(Request req, int seperatedNum)
			throws SerialConnectionException, IOException, ReceivedException {
		read(req);
		List<byte[]> received = getSeperatedReceived(seperatedNum);
		List<Response> mutil = new ArrayList<Response>();
		for (byte[] by : received) {
			Response r = new Response();
			if (r.validate(by)) {
				r.deprotocol(by);
				mutil.add(r);
			}
		}
		Response[] reses = new Response[mutil.size()];
		return (Response[]) mutil.toArray(reses);
	}
	
	public void continueRead(Request req ,int seperatedNum , final ReaderDeviceCallback callback) throws SerialConnectionException, IOException{
		read(req);
		continueReceived(seperatedNum, new SerialConnectionCallback() {
			
			@Override
			public void afterRead(List<byte[]> list, int from) {
				Response [] reses = new Response[list.size() - from];
				for(int i = from ; i < list.size() ; i++){
					byte []by = list.get(i);
					Response r = new Response();
					if(r.validate(by)){
						r.deprotocol(by);
						reses[i - from] = r;
					}
				}
				callback.afterRead(reses, from);
			}
		});
	}
	
	public void waitingRead(final ReaderDeviceCallback callback){
		clearBuffer();
		waitingReceived(new SerialConnectionCallback() {
			
			@Override
			public void afterRead(List<byte[]> list, int from) {
				Response [] reses = new Response[list.size() - from];
				for(int i = from ; i < list.size() ; i++){
					byte []by = list.get(i);
					Response r = new Response();
					if(r.validate(by)){
						r.deprotocol(by);
						reses[i - from] = r;
					}
				}
				callback.afterRead(reses, from);
			}
		});
	}

	public Response readMulti(byte[] protocol) {
		return null;
	}

	/**
	 * 连续读取数据知道再也收不到数据
	 * @param seperatedNum
	 * @return
	 * @throws ReceivedException
	 */
	public List<byte[]> getSeperatedReceived(int seperatedNum)
			throws ReceivedException {
		List<byte[]> list = new ArrayList<byte[]>();
		try {
			int i = 0;
			int start = 0;
			while (i++ < this.loop)
				try {
					sleep(this.timeout);
					if (!receivedBlocked && this.bufferList.size() > start) {
						start = getMultiReceivedArray(list);
						i = 0;
					}
					if (start < seperatedNum)
						continue;
					if(start >= seperatedNum)
						break;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		} catch (NumberFormatException e) {
			throw new ReceivedException("srs.device.revceived.nonstandard", e);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ReceivedException("srs.device.revceived.len_error", e);
		}
		return list;
	}
	
	public void continueReceived(int seperatedNum , SerialConnectionCallback callback){
		int i = 0;
		int start = 0;
		int from = 0;
		List<byte[]> list = new ArrayList<byte[]>();
		while (i++ < this.loop){
			try {
				sleep(this.timeout);
				if (!receivedBlocked) {
					from = start;
					start = getMultiReceivedArray(list);
					callback.afterRead(list, from);
					i = 0;					
				}
				if(start >= seperatedNum)
					break;
				if(isInterrupt){
					break;
				}
				if (start < seperatedNum)
					continue;
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		isInterrupt = false;
	}
	public void waitingReceived(SerialConnectionCallback callback){
		List<byte[]> list = new ArrayList<byte[]>();
		while(true){
			try{
				sleep(this.timeout);
				//logger.info("receivedBlocked : " + receivedBlocked);
				if (!receivedBlocked) {
					getMultiReceivedArray(list);
					callback.afterRead(list, 0);
					list.clear();
				}
				if(isInterrupt){
					break;
				}
			}catch (InterruptedException e) {
				//logger.error("WaitingReceived",e);
				System.out.println("WaitingReceived");
			}
		}
		isInterrupt = false;
	}
	private int getMultiReceivedArray(List<byte[]> list)
			throws NumberFormatException, ArrayIndexOutOfBoundsException {
		int start = list.size();
		int end = this.bufferList.size();
		for (int s = 0; s < end; s++) {
			List<Byte> by = this.bufferList.get(s);
			byte[] b = new byte[by.size()];
			for (int i = 0; i < b.length; i++){
				b[i] = ((Byte) by.get(i)).byteValue();
			}
			/*logger.info("RX:" + CommonUtil.toHex(CommonUtil.assic2Hex(b)) + "("
					+ CommonUtil.toHex(b) + ")");*/
			//logger.info("RX:" + CommonUtil.toHex(CommonUtil.assic2Hex(b)));
			//list.add(CommonUtil.assic2Hex(b));
			list.add(b);
		}
		clearBuffer(0 , end);
		return start + end;
	}
	private void clearBuffer(int from , int end){
		for(int i = 0 ; i < end ; i ++){
			this.bufferList.remove(from);
		}
	}
	public byte[] getSingleReceived() throws ReceivedException {
		try {
			int i = 0;
			while ((i++) < loop) {
				try {
					sleep(timeout);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (!receivedBlocked && !bufferList.isEmpty())
					return getSingleReceivedArray();
			}
		} catch (NumberFormatException e) {
			throw new ReceivedException("srs.device.revceived.nonstandard", e);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ReceivedException("srs.device.revceived.len_error", e);
		}
		return null;
		// logger.info(CommonUtil.toHex(CommonUtil.assic2Hex(CommonUtil.str2Hex("30303031363330303030303131303130313031303130313031303130313031303130313031303130313031303130313031464538"))));
		// return
		// CommonUtil.assic2Hex(CommonUtil.str2Hex("30303031363330303030303131303130313031303130313031303130313031303130313031303130313031303130313031464538"));
	}

	/*public List<byte[]> getSeperatedReceived() throws ReceivedException {
		try {
			int i = 0;
			while ((i++) < loop) {
				try {
					sleep(timeout);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return getMultiReceivedArray();
		} catch (NumberFormatException e) {
			throw new ReceivedException("接受的字串不符合规范(" + e.getLocalizedMessage()
					+ ")", e);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ReceivedException("接受的字串长度不正确(" + e.getLocalizedMessage()
					+ ")", e);
		}
	}*/

	private byte[] getSingleReceivedArray() throws NumberFormatException,
			ArrayIndexOutOfBoundsException {
		List<Byte> list = bufferList.get(0);
		byte[] by = new byte[list.size()];
		for (int i = 0; i < list.size(); i++) {
			by[i] = list.get(i);
		}
		/*logger.info("RX:" + CommonUtil.toHex(CommonUtil.assic2Hex(by)) + "("
				+ CommonUtil.toHex(by) + ")");*/
		
		//logger.info("RX:" + CommonUtil.toHex(CommonUtil.assic2Hex(by)));
		//return CommonUtil.assic2Hex(by);
		return by;
	}

	private List<byte[]> getMultiReceivedArray() throws NumberFormatException,
			ArrayIndexOutOfBoundsException {
		List<byte[]> list = new ArrayList<byte[]>();
		bufferList.add(new ArrayList<Byte>());
		for (List<Byte> by : bufferList) {
			byte[] b = new byte[by.size()];
			for (int i = 0; i < b.length; i++)
				b[i] = by.get(i);
			//logger.info("RX:" + CommonUtil.toHex(CommonUtil.assic2Hex(b)) + "("
					//+ CommonUtil.toHex(b) + ")");
			//list.add(CommonUtil.assic2Hex(b));
			list.add(b);
		}
		// list.add(CommonUtil.assic2Hex(new
		// byte[]{0x30,0x30,0x30,0x32,0x36,0x30,0x30,0x30,0x38,0x35,0x41,0x33}));
		// list.add(CommonUtil.assic2Hex(new
		// byte[]{0x30,0x30,0x30,0x32,0x36,0x32,0x33,0x41,0x44,0x30,0x32,0x36}));
		// list.add(new byte[]{0x00,0x04,0x62,0x00,0x22,0x33,0x00,0x00});
		return list;

	}

	/**
	 * Send a one second break signal.
	 */
	public void sendBreak() {
		sPort.sendBreak(1000);
	}

	/**
	 * Reports the open status of the port.
	 * 
	 * @return true if port is open, false if port is closed.
	 */
	public boolean isOpened() {
		return opened;
	}

	/**
	 * Handles SerialPortEvents. The two types of SerialPortEvents that this
	 * program is registered to listen for are DATA_AVAILABLE and BI. During
	 * DATA_AVAILABLE the port buffer is read until it is drained, when no more
	 * data is availble and 30ms has passed the method returns. When a BI event
	 * occurs the words BREAK RECEIVED are written to the messageAreaIn.
	 */
	@SuppressWarnings("unused")
	public void serialEvent(SerialPortEvent e) {
		// Create a StringBuffer and int to receive input data.
			
			int n = 1;
			// Determine type of event.
			switch (e.getEventType()) {
	
			// Read data until -1 is returned. If \r is received substitute
			// \n for correct newline handling.
			case SerialPortEvent.DATA_AVAILABLE:
				receivedBlocked = true;
				
				while (n > 0) {
					try {
						byte[] b = new byte[1];
						if (is.available() <= 0)
							break;
						n= is.read(b,0,1);
						
						//n= is.read(b);
						if (n >0) {
							buffer.add(b[0]);	
						}
					} catch (IOException ie) {
						plugged = false;
						//if(readerDeviceListener != null)
							//readerDeviceListener.plugOff();
						System.exit(-1);
						break;
					}
				}
				if(!buffer.isEmpty()){
					appendBuffer2List();
				}
				receivedBlocked = false;
				break;
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
				//logger.info("SerialPortEvent.OUTPUT_BUFFER_EMPTY");
				break;
			// If break event append BREAK RECEIVED message.
			case SerialPortEvent.BI:
				// messageAreaIn.append("\n--- BREAK RECEIVED ---\n");
				break;
			}
	}
	
	private void appendBuffer2List(){
		int LEN = 37;
		int DLEN = 4;
			while(true){
				int s = 0 ;
				int n =0, m = 0;
				n = CommonUtil.toHex(buffer).indexOf("A5A5A5A5");
				m = n/2;
	            if(buffer.isEmpty()){
	            	break;
	            }
				if(n<0 || (buffer.size()-m)<LEN ){
					break;
				}
				
				List<Byte> l = CommonUtil.cloneList(buffer.subList(m + DLEN, LEN + m));
				System.out.println("RX:" + CommonUtil.toHex(l));
				bufferList.add(l);
				for(int i = 0 ; i < LEN + m; i++){
					buffer.remove(s);
				}
			}
	}
	
	public byte[] getRes(byte[] by){
		return by;
	}
	private void clearBuffer() {
		if (!bufferList.isEmpty()) {
			bufferList.clear();
		}
	}

	/**
	 * Handles ownership events. If a PORT_OWNERSHIP_REQUESTED event is received
	 * a dialog box is created asking the user if they are willing to give up
	 * the port. No action is taken on other types of ownership events.
	 */
	public void ownershipChange(int type) {
		if (type == CommPortOwnershipListener.PORT_OWNERSHIP_REQUESTED) {
			// PortRequestedDialog prd = new PortRequestedDialog(parent);
		}
	}

	/**
	 * A class to handle <code>KeyEvent</code>s generated by the messageAreaOut.
	 * When a <code>KeyEvent</code> occurs the <code>char</code> that is
	 * generated by the event is read, converted to an <code>int</code> and
	 * writen to the <code>OutputStream</code> for the port.
	 */
	class KeyHandler extends KeyAdapter {
		OutputStream os;

		/**
		 * Creates the KeyHandler.
		 * 
		 * @param os
		 *            The OutputStream for the port.
		 */
		public KeyHandler(OutputStream os) {
			super();
			this.os = os;
		}

		/**
		 * Handles the KeyEvent. Gets the
		 * <code>char</char> generated by the <code>KeyEvent</code>, converts it
		 * to an <code>int</code>, writes it to the <code>
	OutputStream</code> for the
		 * port.
		 */
		@Override
		public void keyTyped(KeyEvent evt) {
			char newCharacter = evt.getKeyChar();
			try {
				os.write(newCharacter);
			} catch (IOException e) {
				System.err.println("OutputStream write error: " + e);
			}
		}
	}
	public synchronized int getSubBufferList(int from,List<List<Byte>> list){
		synchronized (bufferList) {
			int index = bufferList.size();
			list = bufferList.subList(from, index);
			return index;
		}
	}
	/*public void addReaderDeviceListener(ReaderDeviceListener listener){
		this.readerDeviceListener = listener;
	}
	public void removeReaderDeviceListener(ReaderDeviceListener listener){
		this.readerDeviceListener = null;
	}*/
}
