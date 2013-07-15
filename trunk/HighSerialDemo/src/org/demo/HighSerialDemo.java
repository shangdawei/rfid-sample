package org.demo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class HighSerialDemo {
	SimpleDateFormat dateformat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	protected Shell shlSerialDemohigh;
	private Text text;
	BufferedWriter out;
	private boolean read = true;
	SerialConnection conn;
	java.util.List<String[]> list = new ArrayList<String[]>();
	private Combo comboPortName;
	private Combo comboBaudRate;
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			HighSerialDemo window = new HighSerialDemo();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlSerialDemohigh.open();
		shlSerialDemohigh.layout();
		shlSerialDemohigh.addListener(SWT.Close, new Listener() {
			
			@Override
			public void handleEvent(Event e) {
				if(conn != null)
					conn.closeConnection();
				Display.getDefault().dispose();
				e.doit = true;
			}
		});
		while (!shlSerialDemohigh.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlSerialDemohigh = new Shell();
		shlSerialDemohigh.setSize(450, 414);
		shlSerialDemohigh.setText("Serial Demo(High)");
		shlSerialDemohigh.setLayout(new GridLayout(1, false));
		
		Group group = new Group(shlSerialDemohigh, SWT.NONE);
		group.setLayout(new GridLayout(5, false));
		GridData gd_group = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
		gd_group.widthHint = 419;
		group.setLayoutData(gd_group);
		
		Label lblNewLabel = new Label(group, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("\u4E32\u53E3\u53F7");
		
		comboPortName = new Combo(group, SWT.NONE);
		comboPortName.setItems(new String[] {"COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9"});
		comboPortName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboPortName.select(0);
		
		Label lblNewLabel_1 = new Label(group, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("\u6CE2\u7279\u7387");
		
		comboBaudRate = new Combo(group, SWT.NONE);
		comboBaudRate.setItems(new String[] {"1200", "2400", "4800", "9600"});
		comboBaudRate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboBaudRate.select(3);
		
		btnConnect = new Button(group, SWT.NONE);
		btnConnect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(btnConnect.getText().equals("连接")){
					String portName = comboPortName.getText();
					int baudRate = Integer.parseInt(comboBaudRate.getText());
					conn = new SerialConnection(new SerialParameters(portName, baudRate));
					
					try {
						FileWriter fileWriter = new FileWriter("db.txt" ,true);
						out = new BufferedWriter(fileWriter);
					} catch (IOException e2) {
						MessageDialog.openError(shlSerialDemohigh, "Error", e2.getLocalizedMessage());
						return;
					}
					try {
						conn.openConnection();
						try {
							FileWriter fileWriter = new FileWriter("db.txt" ,true);
							out = new BufferedWriter(fileWriter);
						} catch (IOException e2) {
							MessageDialog.openError(shlSerialDemohigh, "Error", e2.getLocalizedMessage());
							return;
						}
						new Thread(readWorker).start();
						btnConnect.setText("断开");
					} catch (SerialConnectionException e1) {
						MessageDialog.openError(shlSerialDemohigh, "Error", e1.getLocalizedMessage());
					}
					
				}else{
					btnConnect.setText("连接");
					if(conn != null )
						conn.closeConnection();
					if(out != null)
						try {
							out.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				}
			}
		});
		btnConnect.setText("\u8FDE\u63A5");
		
		text = new Text(shlSerialDemohigh, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_text = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_text.heightHint = 275;
		text.setLayoutData(gd_text);
		
		Composite composite = new Composite(shlSerialDemohigh, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		GridData gd_composite = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_composite.heightHint = 33;
		composite.setLayoutData(gd_composite);

	}
	
	
	private Runnable readWorker = new Runnable() {
		
		@Override
		public void run() {
			try {
				conn.waitingRead(new ReaderDeviceCallback(){

					@Override
					public void afterRead(Response[] res, int from) {
						for(Response r : res){

							String status = CommonUtil.toHex(r.getStatus());
							if(status.equals("A1")){
								appendNewSession();
								SimpleDateFormat df = new SimpleDateFormat("yyMMddHHmmss");
								Date now = new Date();
								String nowdate = df.format(now);
								String yy = nowdate.substring(0,2);
								String dd = nowdate.substring(4,6);
								String MM = nowdate.substring(2,4);
								String hh = nowdate.substring(6,8);
								String mm = nowdate.substring(8,10);
								String ss = nowdate.substring(10,12);
								String addnum = "0000000000000000000000000000";
								CRC16 crc16 = new CRC16();
								String routt = r.getResultString();
								String cd = "A5A5A5A5A1"+yy+"00"+dd+MM+hh+"00"+ss+mm+addnum+routt + CommonUtil.toHex(crc16.getCrcByte(CommonUtil.str2Hex(yy+"00"+dd+MM+hh+"00"+ss+mm+addnum+routt)));
								byte[] b = new byte[CommonUtil.str2Hex(cd).length];
								b=CommonUtil.str2Hex(cd).clone();
                                try {
                                	System.out.println("TX: " + CommonUtil.toHex(b));
									conn.read(b);
								} catch (SerialConnectionException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								break;
								
							}
							if(!r.checkCrc()){
								System.out.println("CRC Error for");
								continue;
							}
							final Tag tag = new Tag(r.getResult());
							new Thread(){
								public void run(){
									if(!shlSerialDemohigh.getDisplay().isDisposed()){
										shlSerialDemohigh.getDisplay().syncExec(new Runnable() {	
											@Override
											public void run() {
												text.append("Session: "+tag.getRouter()+"-"+tag.getRandoms()+"\n");
												text.append("From: "+tag.getDisplayDatetime()+"\n");
												text.append("To: "+tag.getStoptime()+"\n");
												text.append("Card: "+tag.getId()+"\n\n");
											}
										});
									}
								}
							}.start();
						}
						
					}
					
				});
			} catch (final Exception e1) {
				new Thread(){
					public void run(){
						if(!shlSerialDemohigh.getDisplay().isDisposed()){
							shlSerialDemohigh.getDisplay().syncExec(new Runnable() {
								
								@Override
								public void run() {
									MessageDialog.openError(shlSerialDemohigh, "Error", e1.getLocalizedMessage());
								}
							});
						}
						
					}
				}.start();
				
			}
			
		}
	};
	private void appendNewSession(){
		new Thread(){
			public void run(){
				if(!shlSerialDemohigh.getDisplay().isDisposed()){
					shlSerialDemohigh.getDisplay().syncExec(new Runnable() {	
						@Override
						public void run() {
							text.append("Create new session \n");

						}
					});
				}
				
			}
		}.start();
	}
	//private Button btnRead;
	private Button btnConnect;
	
	
	/*private boolean check() throws IOException, SerialConnectionException{
		byte[] r1 = conn.readSingle(new byte[]{0x02,0x0B,0x0F});
		if(r1 == null)
			return false;
		if(CommonUtil.toHex(r1).equalsIgnoreCase("0100"))
			return true;
		return false;
	}
	private boolean searchCards() throws IOException, SerialConnectionException{
		byte[] r = conn.readSingle(new byte[]{0x02,0x02,0x26});
		if(r == null)
			return false;
		if(CommonUtil.toHex(r).equalsIgnoreCase("0101"))
			return false;
		if(CommonUtil.toHex(r).equalsIgnoreCase("03000400"))
			return true;
		return false;
	}*/
	
	/*private boolean readCards() throws IOException, SerialConnectionException{
		System.out.println("进入方法");
		Response[] res = conn.getRead();
		for(Response r : res){
			System.out.println("r==="+r.getResult());
			final Tag tag = new Tag(r.getResult());
			new Thread(){
				public void run(){
					if(!shlSerialDemohigh.getDisplay().isDisposed()){
						shlSerialDemohigh.getDisplay().syncExec(new Runnable() {
							
							@Override
							public void run() {
								text.append(tag.getDisplayDatetime() + ": " + tag.getId());
							}
						});
					}
				}
			}.start();
			if(!r.checkCrc()){
				text.append("有误!");
			}
		}
		return true;
		
	}*/
	
	/*private boolean readCards() throws IOException, SerialConnectionException{
		java.util.List<byte[]> r = conn.readMulti((new byte[]{0x01,0x03}));
		if(r == null || r.isEmpty())
			return false;
		if(CommonUtil.toHex(r.get(0)).equalsIgnoreCase("0101"))
			return false;
		for(byte [] b : r){
			System.out.println("r=="+CommonUtil.toHex(r.get(0)));
			final String [] item = new String[]{CommonUtil.toHex(b) , dateformat.format(new Date())};
			list.add(item);
			new Thread(){
				public void run(){
					if(!shlSerialDemohigh.getDisplay().isDisposed()){
						shlSerialDemohigh.getDisplay().syncExec(new Runnable() {
							
							@Override
							public void run() {
								text.append(item[1] + ": " + item[0] + "\n");
							}
						});
					}
					
				}
			}.start();
			
			
		}
		return true;
		
	}*/
	private boolean insertCards(){
		try {
			for(String[] strs : list){
				out.write(strs[0] + "," + strs[1]);
				out.newLine();
				out.flush();
			}
			list.clear();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return true;
	}
}
