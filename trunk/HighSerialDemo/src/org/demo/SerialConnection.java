package org.demo;

//import javax.comm.*;

import java.io.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;


//import javax.comm.*;
import gnu.io.*;

/**
 * A class that handles the details of a serial connection. Reads from one
 * TextArea and writes to a second TextArea. Holds the state of the connection.
 */
public class SerialConnection extends Thread implements
		SerialPortEventListener, CommPortOwnershipListener {

	private SerialParameters parameters;
	private OutputStream os;
	private InputStream is;
	private CommPortIdentifier portId;
	private SerialPort sPort;;
	private boolean open;
	private int timeout = 100;
	private int loop = 5;
	private boolean receivedBlocked = false;
	private List<List<Byte>> bufferList = new ArrayList<List<Byte>>();

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
		open = false;
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
		open = true;
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
		if (!open) {
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
			sPort.close();

			// Remove the ownership listener.
			portId.removePortOwnershipListener(this);
		}
		open = false;
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
	public byte[] readSingle(byte[] cmd) throws IOException,
			SerialConnectionException {
		read(cmd);
		return getSingleReceived();
	}
	
	public List<byte[]> readMulti(byte[] cmd) throws SerialConnectionException, IOException{
		read(cmd);
		return getMultiReveived();
	}

	/**
	 * 读取被拆分的一整条
	 * 
	 * @param req
	 * @throws SerialConnectionException
	 * @throws IOException
	 */ 
	private void read(byte[] cmd) throws SerialConnectionException,
			IOException {
		if (!open)
			throw new SerialConnectionException("Serial port was closed!");
		clearBuffer();
		os.write(cmd);
	}

	

	public byte[] getSingleReceived() {
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
			throw e;
		}
		return null;
		// logger.info(CommonUtil.toHex(CommonUtil.assic2Hex(CommonUtil.str2Hex("30303031363330303030303131303130313031303130313031303130313031303130313031303130313031303130313031464538"))));
		// return
		// CommonUtil.assic2Hex(CommonUtil.str2Hex("30303031363330303030303131303130313031303130313031303130313031303130313031303130313031303130313031464538"));
	}

	public List<byte[]> getMultiReveived(){
		int i = 0 ;
		while((i ++) < loop){
			try {
				sleep(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return getMultiReveivedArray();
	}

	private byte[] getSingleReceivedArray() throws NumberFormatException,
			ArrayIndexOutOfBoundsException {
		List<Byte> list = bufferList.get(0);
		byte[] by = new byte[list.size()];
		for (int i = 0; i < list.size(); i++) {
			by[i] = list.get(i);
		}
		return by;
	}
	
	private List<byte[]> getMultiReveivedArray(){
		List<byte[]> list = new ArrayList<byte[]>();
		for(List<Byte> l : bufferList){
			byte []by = new byte[l.size()];
			for(int i = 0 ; i < l.size() ; i ++){
				by[i] = l.get(i);
			}
			list.add(by);
		}
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
	public boolean isOpen() {
		return open;
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

		List<Byte> buffer = new ArrayList<Byte>();
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
					n = is.read(b, 0, 1);
					if (n > 0 ) {
						buffer.add(b[0]);
					}
				} catch (IOException ie) {
					ie.printStackTrace();
				}
			}
			bufferList.add(buffer);
			receivedBlocked = false;
			break;

		// If break event append BREAK RECEIVED message.
		case SerialPortEvent.BI:
			// messageAreaIn.append("\n--- BREAK RECEIVED ---\n");
			break;
		}

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

}
