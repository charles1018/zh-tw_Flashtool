package gui;

import flashsystem.TaEntry;
import flashsystem.X10flash;
import gui.tools.BLUnlockJob;
import gui.tools.WidgetTask;
import gui.tools.WidgetsTool;
import gui.tools.WriteTAJob;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.system.ULCodeFile;

public class BLUWizard extends Dialog {

	protected Object result;
	protected Shell shlBootloaderUnlockWizard;
	private Text textIMEI;
	private Text textULCODE;
	private Button btnGetUnlock;
	private Button btnUnlock;
	private X10flash _flash;
	private String _action;
	private String _serial;
	private static Logger logger = Logger.getLogger(BLUWizard.class);

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public BLUWizard(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open(String serial, String imei, String ulcode,X10flash flash, String action) {
		_action = action;
		_flash = flash;
		_serial = serial;
		createContents();
		textIMEI.setText(imei);
		textULCODE.setText(ulcode);
		if (ulcode.length()>0) {
			btnUnlock.setEnabled(true);
			if (_action.equals("R")) {
				btnUnlock.setText("上鎖");
			}
			btnGetUnlock.setEnabled(false);
			textULCODE.setEditable(false);
		}
		WidgetsTool.setSize(shlBootloaderUnlockWizard);
		shlBootloaderUnlockWizard.open();
		shlBootloaderUnlockWizard.layout();
		Display display = getParent().getDisplay();
		while (!shlBootloaderUnlockWizard.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlBootloaderUnlockWizard = new Shell(getParent(), getStyle());
		shlBootloaderUnlockWizard.addListener(SWT.Close, new Listener() {
		      public void handleEvent(Event event) {
		    	  result = "";
		    	  event.doit = true;
		      }
		    });
		shlBootloaderUnlockWizard.setSize(286, 183);
		if (_action.equals("R"))
			shlBootloaderUnlockWizard.setText("上鎖導引");
		else
			shlBootloaderUnlockWizard.setText("解鎖導引");
		
		Label lblImei = new Label(shlBootloaderUnlockWizard, SWT.NONE);
		lblImei.setBounds(10, 10, 55, 15);
		lblImei.setText("IMEI : ");
		
		textIMEI = new Text(shlBootloaderUnlockWizard, SWT.BORDER);
		textIMEI.setEditable(false);
		textIMEI.setBounds(106, 7, 164, 21);
		
		btnGetUnlock = new Button(shlBootloaderUnlockWizard, SWT.NONE);
		btnGetUnlock.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch("http://unlockbootloader.sonymobile.com/");
			}
		});
		btnGetUnlock.setBounds(127, 34, 118, 25);
		btnGetUnlock.setText("取得解鎖碼");
		
		Label lblUnlockCode = new Label(shlBootloaderUnlockWizard, SWT.NONE);
		lblUnlockCode.setBounds(10, 68, 85, 15);
		lblUnlockCode.setText("解鎖碼:");
		
		textULCODE = new Text(shlBootloaderUnlockWizard, SWT.BORDER);
		textULCODE.setBounds(106, 65, 164, 21);
		
		btnUnlock = new Button(shlBootloaderUnlockWizard, SWT.NONE);
		btnUnlock.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (textULCODE.getText().length()==0) {
					showErrorMessageBox("請輸入解鎖碼");
					return;
				}
				if (_flash==null) {
					BLUnlockJob bj = new BLUnlockJob("Unlock Job");
					final String ulcode = textULCODE.getText();
					bj.setULCode(ulcode);
					bj.addJobChangeListener(new IJobChangeListener() {
						public void aboutToRun(IJobChangeEvent event) {}
						public void awake(IJobChangeEvent event) {}
						public void running(IJobChangeEvent event) {}
						public void scheduled(IJobChangeEvent event) {}
						public void sleeping(IJobChangeEvent event) {}

						public void done(IJobChangeEvent event) {
							BLUnlockJob res = (BLUnlockJob) event.getJob();
							WidgetTask.setEnabled(btnUnlock,!res.unlockSuccess());
							if (res.unlockSuccess()) {
								try {
									ULCodeFile uc = new ULCodeFile(_serial);
									uc.setCode(ulcode);
								}
								catch (Exception e) {
									e.printStackTrace();
								}
							}
						}

					});
					bj.schedule();
					btnUnlock.setEnabled(false);
				}
				else {
					if (_action.equals("R")) {
						TaEntry ta = new TaEntry();
						ta.setPartition(2226);
						//byte[] data = new byte[2];data[0]=0;data[1]=0;
						//ta.setData(data);
						logger.info("正在上鎖裝置");
						WriteTAJob tj = new WriteTAJob("寫入TA分區");
						tj.addJobChangeListener(new IJobChangeListener() {
							public void aboutToRun(IJobChangeEvent event) {}
							public void awake(IJobChangeEvent event) {}
							public void running(IJobChangeEvent event) {}
							public void scheduled(IJobChangeEvent event) {}
							public void sleeping(IJobChangeEvent event) {}
							public void done(IJobChangeEvent event) {
								logger.info("上鎖完成");
								WriteTAJob res = (WriteTAJob) event.getJob();
								WidgetTask.setEnabled(btnUnlock,!res.writeSuccess());
							}
						});
						tj.setFlash(_flash);
						tj.setTA(ta);
						tj.schedule();
					}
					else {
						TaEntry ta = new TaEntry();
						ta.setPartition(2226);
						ta.setData(textULCODE.getText().getBytes());
						logger.info("正在解鎖裝置");
						WriteTAJob tj = new WriteTAJob("寫入TA分區");
						tj.addJobChangeListener(new IJobChangeListener() {
							public void aboutToRun(IJobChangeEvent event) {}
							public void awake(IJobChangeEvent event) {}
							public void running(IJobChangeEvent event) {}
							public void scheduled(IJobChangeEvent event) {}
							public void sleeping(IJobChangeEvent event) {}
							public void done(IJobChangeEvent event) {
								logger.info("解鎖完成");
								WriteTAJob res = (WriteTAJob) event.getJob();
								WidgetTask.setEnabled(btnUnlock,!res.writeSuccess());
							}
						});
						tj.setFlash(_flash);
						tj.setTA(ta);
						tj.schedule();
					}
				}
			}
		});
		btnUnlock.setBounds(144, 92, 75, 25);
		btnUnlock.setText("解鎖");
		
		Button btnNewButton_2 = new Button(shlBootloaderUnlockWizard, SWT.NONE);
		btnNewButton_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlBootloaderUnlockWizard.dispose();
			}
		});
		btnNewButton_2.setBounds(195, 123, 75, 25);
		btnNewButton_2.setText("關閉");

	}

	public void showErrorMessageBox(String message) {
		MessageBox mb = new MessageBox(shlBootloaderUnlockWizard,SWT.ICON_ERROR|SWT.OK);
		mb.setText("錯誤");
		mb.setMessage(message);
		int result = mb.open();
	}

}
