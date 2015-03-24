package gui;

import gui.tools.DeviceApps;
import gui.tools.WidgetTask;
import gui.tools.WidgetsTool;

import java.io.File;
import java.util.Vector;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.List;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

public class ProfileSave extends Dialog {

	protected Shell shlProfileSave;
	private Text txtProfileName;
	private Button btnsave;
	String result = null;
	DeviceApps _apps = null;

	public ProfileSave(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public String open(DeviceApps apps) {
		_apps=apps;
		createContents();
		WidgetsTool.setSize(shlProfileSave);
		shlProfileSave.open();
		shlProfileSave.layout();
		Display display = getParent().getDisplay();
		while (!shlProfileSave.isDisposed()) {
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
		shlProfileSave = new Shell(getParent(), getStyle());
		shlProfileSave.setSize(421, 130);
		shlProfileSave.setText("設定檔名稱");
		shlProfileSave.setLayout(new FormLayout());
		
		Button btnCancel = new Button(shlProfileSave, SWT.NONE);
		FormData fd_btnCancel = new FormData();
		fd_btnCancel.right = new FormAttachment(100, -10);
		btnCancel.setLayoutData(fd_btnCancel);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				result="";
				shlProfileSave.dispose();
			}
		});
		btnCancel.setText("取消");
		
		btnsave = new Button(shlProfileSave, SWT.NONE);
		btnsave.setEnabled(false);
		FormData fd_btnsave = new FormData();
		fd_btnsave.top = new FormAttachment(btnCancel, 0, SWT.TOP);
		fd_btnsave.right = new FormAttachment(btnCancel, -9);
		btnsave.setLayoutData(fd_btnsave);
		btnsave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
				if (_apps.getProfiles().contains(txtProfileName.getText().toLowerCase()))
					throw new Exception("This profile already exists.");
				if (txtProfileName.getText().toLowerCase().contains(" "))
					throw new Exception("Name cannot contain spaces.");
					_apps.saveProfile(txtProfileName.getText().toLowerCase());
					_apps.setProfile(txtProfileName.getText().toLowerCase());
					shlProfileSave.dispose();
				} catch (Exception ex) {
					WidgetTask.openOKBox(shlProfileSave, ex.getMessage());
				}
			}
		});
		btnsave.setText("儲存");
		
		Composite composite = new Composite(shlProfileSave, SWT.NONE);
		fd_btnCancel.top = new FormAttachment(0, 68);
		composite.setLayout(new GridLayout(2, false));
		FormData fd_composite = new FormData();
		fd_composite.bottom = new FormAttachment(btnCancel, -6);
		fd_composite.left = new FormAttachment(0, 10);
		fd_composite.top = new FormAttachment(0, 10);
		fd_composite.right = new FormAttachment(100, -11);
		composite.setLayoutData(fd_composite);
		
		Label lblName = new Label(composite, SWT.NONE);
		GridData gd_lblName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblName.widthHint = 92;
		lblName.setLayoutData(gd_lblName);
		lblName.setText("設定檔名稱:");
		txtProfileName = new Text(composite, SWT.BORDER);
		txtProfileName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (txtProfileName.getText().length()>0) {
					btnsave.setEnabled(true);
				}
				else
					btnsave.setEnabled(false);
			}
		});
		GridData gd_txtProfileName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtProfileName.widthHint = 270;
		txtProfileName.setLayoutData(gd_txtProfileName);
	}

}
