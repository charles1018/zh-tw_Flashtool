package gui;

import gui.tools.DevicesSyncJob;
import gui.tools.WidgetTask;
import gui.tools.WidgetsTool;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class WaitForDevicesSync extends Dialog {

	protected Object result;
	protected Shell shlWaiForDevicesSync;
	protected boolean canClose = false;
	protected Dialog mydial;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public WaitForDevicesSync(Shell parent, int style) {
		super(parent, style);
		setText("正在從Github同步裝置資料夾");
		mydial = this;
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		WidgetsTool.setSize(shlWaiForDevicesSync);
		
		Label lblNewLabel = new Label(shlWaiForDevicesSync, SWT.NONE);
		lblNewLabel.setBounds(10, 32, 323, 15);
		lblNewLabel.setText("請耐心等待同步完成，首次使用時所需同步時間會比較久！");
		shlWaiForDevicesSync.open();
		shlWaiForDevicesSync.layout();
		Display display = getParent().getDisplay();
		while (!shlWaiForDevicesSync.isDisposed()) {
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
		shlWaiForDevicesSync = new Shell(getParent(), getStyle());
		shlWaiForDevicesSync.addListener(SWT.Close, new Listener() {
		      public void handleEvent(Event event) {
		    	  if (canClose) {
		    		  result = "";
		    	  	event.doit = true;
		    	  }
		    	  else {
		    		  WidgetTask.openOKBox(shlWaiForDevicesSync, "Wait for end of process");
		    		  event.doit = false;
		    	  }
		      }
		    });
		shlWaiForDevicesSync.setSize(365, 128);
		shlWaiForDevicesSync.setText("正在從Github同步裝置資料夾");
		DevicesSyncJob sync = new DevicesSyncJob("GitSync");
		sync.addJobChangeListener(new IJobChangeListener() {
			public void aboutToRun(IJobChangeEvent event) {
			}

			public void awake(IJobChangeEvent event) {
			}

			public void done(IJobChangeEvent event) {
				canClose=true;
				Display.getDefault().asyncExec(
						new Runnable() {
							public void run() {
								shlWaiForDevicesSync.dispose();
							}
						}
				);					
			}

			public void running(IJobChangeEvent event) {
			}

			public void scheduled(IJobChangeEvent event) {
			}

			public void sleeping(IJobChangeEvent event) {
			}
		});
		sync.schedule();

	}
}
