package gui;

import gui.tools.WidgetsTool;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class LoaderSelect extends Dialog {

	protected Object result;
	protected Shell shell;
	Button btnLocked;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public LoaderSelect(Shell parent, int style) {
		super(parent, style);
		setText("選擇Loader");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		WidgetsTool.setSize(shell);
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
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
		shell = new Shell(getParent(), getStyle());
		shell.addListener(SWT.Close, new Listener() {
		      public void handleEvent(Event event) {
		    	  result = "";
		    	  event.doit = true;
		      }
		    });
		shell.setSize(229, 128);
		shell.setText(getText());
		
		btnLocked = new Button(shell, SWT.RADIO);
		btnLocked.setBounds(42, 10, 110, 16);
		btnLocked.setText("上鎖loader");
		btnLocked.setSelection(true);
		
		Button btnUnlocked = new Button(shell, SWT.RADIO);
		btnUnlocked.setBounds(42, 32, 110, 16);
		btnUnlocked.setText("解鎖loader");
		
		Button btnCancel = new Button(shell, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				result = "";
				shell.dispose();
			}
		});
		btnCancel.setBounds(138, 64, 75, 25);
		btnCancel.setText("取消");
		
		Button btnOK = new Button(shell, SWT.NONE);
		btnOK.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnLocked.getSelection()) result="L";
				else result="U";
				shell.dispose();
			}
		});
		btnOK.setBounds(57, 64, 75, 25);
		btnOK.setText("確定");

	}
}
