package gui;

import gui.tools.FastBootToolBoxJob;
import gui.tools.WidgetsTool;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class FastbootToolbox extends Dialog {

	protected Object result;
	protected Shell shlFastbootToolbox;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public FastbootToolbox(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		WidgetsTool.setSize(shlFastbootToolbox);
		shlFastbootToolbox.open();
		shlFastbootToolbox.layout();
		Display display = getParent().getDisplay();
		while (!shlFastbootToolbox.isDisposed()) {
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
		shlFastbootToolbox = new Shell(getParent(), getStyle());
		shlFastbootToolbox.setSize(673, 244);
		shlFastbootToolbox.setText("Fastboot工具箱");
		shlFastbootToolbox.setLayout(new GridLayout(3, false));
		new Label(shlFastbootToolbox, SWT.NONE);
		new Label(shlFastbootToolbox, SWT.NONE);
		new Label(shlFastbootToolbox, SWT.NONE);
		
		Label lblVersion = new Label(shlFastbootToolbox, SWT.NONE);
		lblVersion.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblVersion.setText("Version 1.0");
		
		Button btnCheckStatus = new Button(shlFastbootToolbox, SWT.NONE);
		btnCheckStatus.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doCheckDeviceStatus();
			}
		});
		btnCheckStatus.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnCheckStatus.setText("檢查目前裝置狀態");
		
		Label lblByDooMLoRD = new Label(shlFastbootToolbox, SWT.NONE);
		lblByDooMLoRD.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		lblByDooMLoRD.setText("By DooMLoRD");
		
		Button btnrRebootFBAdb = new Button(shlFastbootToolbox, SWT.NONE);
		btnrRebootFBAdb.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doRebootFastbootViaAdb();
			}
		});
		btnrRebootFBAdb.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnrRebootFBAdb.setText("重啟到fastboot模式(通過ADB)");
		new Label(shlFastbootToolbox, SWT.NONE);
		
		Button btnRebootFBFB = new Button(shlFastbootToolbox, SWT.NONE);
		btnRebootFBFB.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doRebootBackIntoFastbootMode();
			}
		});
		btnRebootFBFB.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		btnRebootFBFB.setText("重啟到fastboot模式(通過Fastboot)");
		
		Button btnHotboot = new Button(shlFastbootToolbox, SWT.NONE);
		btnHotboot.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(shlFastbootToolbox);
		        dlg.setFilterExtensions(new String[]{"*.sin","*.elf","*.img"});
		        dlg.setText("選擇核心");
		        String dir = dlg.open();
		        if (dir!=null)
		        	doHotBoot(dir);
			}
		});
		btnHotboot.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnHotboot.setText("選擇核心來暖開機");
		
		Button btnFlashSystem = new Button(shlFastbootToolbox, SWT.NONE);
		btnFlashSystem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(shlFastbootToolbox);
		        dlg.setFilterExtensions(new String[]{"*.sin","*.img","*.ext4","*.yaffs2"});
		        dlg.setText("選擇系統");
		        String dir = dlg.open();
		        if (dir!=null)
		        	doFlashSystem(dir);
			}
		});
		btnFlashSystem.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnFlashSystem.setText("選擇系統來寫入");
		
		Button btnFlashKernel = new Button(shlFastbootToolbox, SWT.NONE);
		btnFlashKernel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(shlFastbootToolbox);
		        dlg.setFilterExtensions(new String[]{"*.sin","*.elf","*.img"});
		        dlg.setText("選擇核心");
		        String dir = dlg.open();
		        if (dir!=null)
		        	doFlashKernel(dir);
			}
		});
		btnFlashKernel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		btnFlashKernel.setText("選擇核心來寫入");
		
		Button btnGetVerInfo = new Button(shlFastbootToolbox, SWT.NONE);
		btnGetVerInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doGetFastbootVerInfo();
			}
		});
		btnGetVerInfo.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnGetVerInfo.setText("讀取版本資料");
		new Label(shlFastbootToolbox, SWT.NONE);
		
		Button btnGetDeviceInfo = new Button(shlFastbootToolbox, SWT.NONE);
		btnGetDeviceInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doGetConnectedDeviceInfo();
			}
		});
		btnGetDeviceInfo.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		btnGetDeviceInfo.setText("讀取裝置資料");
		new Label(shlFastbootToolbox, SWT.NONE);
		
		Button btnReboot = new Button(shlFastbootToolbox, SWT.NONE);
		btnReboot.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doFastbootReboot();
			}
		});
		btnReboot.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnReboot.setText("重啟裝置進入系統");
		new Label(shlFastbootToolbox, SWT.NONE);
		new Label(shlFastbootToolbox, SWT.NONE);
		new Label(shlFastbootToolbox, SWT.NONE);
		
		Button btnClose = new Button(shlFastbootToolbox, SWT.NONE);
		btnClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlFastbootToolbox.dispose();
			}
		});
		btnClose.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		btnClose.setText("關閉");

	}

	public void doRebootFastbootViaAdb() {
		FastBootToolBoxJob job = new FastBootToolBoxJob("重啟到fastboot模式通過ADB");
		job.setAction("doRebootFastbootViaAdb");
		job.schedule();
	}
	
	public void doCheckDeviceStatus(){
		FastBootToolBoxJob job = new FastBootToolBoxJob("檢查裝置狀態");
		job.setAction("doCheckDeviceStatus");
		job.schedule();
	}

	public void doGetConnectedDeviceInfo() {
		FastBootToolBoxJob job = new FastBootToolBoxJob("讀取裝置資料");
		job.setAction("doGetConnectedDeviceInfo");
		job.schedule();
	}

	public void doGetFastbootVerInfo() {
		FastBootToolBoxJob job = new FastBootToolBoxJob("讀取裝置版本資料");
		job.setAction("doGetFastbootVerInfo");
		job.schedule();
	}
	
	public void doRebootBackIntoFastbootMode() {
		FastBootToolBoxJob job = new FastBootToolBoxJob("重啟裝置到fastboot模式");
		job.setAction("doRebootBackIntoFastbootMode");
		job.schedule();
	}

	public void doFastbootReboot() {
		FastBootToolBoxJob job = new FastBootToolBoxJob("重啟裝置");
		job.setAction("doFastbootReboot");
		job.schedule();
	}

	public void doHotBoot(String kernel) {
		FastBootToolBoxJob job = new FastBootToolBoxJob("熱開機裝置");
		job.setAction("doHotbootKernel");
		job.setImage(kernel);
		job.schedule();
	}

	public void doFlashKernel(String kernel) {
		FastBootToolBoxJob job = new FastBootToolBoxJob("寫入核心到裝置");
		job.setAction("doFlashKernel");
		job.setImage(kernel);
		job.schedule();
	}

	public void doFlashSystem(String system) {
		FastBootToolBoxJob job = new FastBootToolBoxJob("寫入系統到裝置");
		job.setAction("doFlashSystem");
		job.setImage(system);
		job.schedule();
	}

}
