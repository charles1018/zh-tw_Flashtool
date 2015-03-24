package gui;

import gui.tools.WidgetsTool;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.system.Elf;

public class ElfEditor extends Dialog {

	protected Object result;
	protected Shell shlElfExtractor;
	private Text sourceFile;
	private Text textNbParts;
	private Elf elfobj;
	private Button btnExtract;
	private static Logger logger = Logger.getLogger(ElfEditor.class);
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ElfEditor(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		WidgetsTool.setSize(shlElfExtractor);
		shlElfExtractor.open();
		shlElfExtractor.layout();
		Display display = getParent().getDisplay();
		while (!shlElfExtractor.isDisposed()) {
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
		shlElfExtractor = new Shell(getParent(), getStyle());
		shlElfExtractor.setSize(538, 153);
		shlElfExtractor.setText("Elf解壓轉換器");
		shlElfExtractor.setLayout(new FormLayout());
		
		Composite composite = new Composite(shlElfExtractor, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		FormData fd_composite = new FormData();
		fd_composite.top = new FormAttachment(0, 10);
		fd_composite.right = new FormAttachment(100, -9);
		composite.setLayoutData(fd_composite);
		
		Label lblElfFile = new Label(composite, SWT.NONE);
		GridData gd_lblElfFile = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblElfFile.widthHint = 62;
		lblElfFile.setLayoutData(gd_lblElfFile);
		lblElfFile.setText("Elf file :");
		
		sourceFile = new Text(composite, SWT.BORDER);
		sourceFile.setEditable(false);
		GridData gd_sourceFile = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_sourceFile.widthHint = 385;
		sourceFile.setLayoutData(gd_sourceFile);
		
		Button btnFileChoose = new Button(composite, SWT.NONE);
		btnFileChoose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(shlElfExtractor);

		        // Set the initial filter path according
		        // to anything they've selected or typed in
		        dlg.setFilterPath(sourceFile.getText());
		        dlg.setFilterExtensions(new String[]{"*.elf"});

		        // Change the title bar text
		        dlg.setText("檔案選擇");
		        // Calling open() will open and run the dialog.
		        // It will return the selected directory, or
		        // null if user cancels
		        String dir = dlg.open();
		        if (dir != null) {
		          // Set the text box to the new selection
		        	if (!sourceFile.getText().equals(dir)) {
		        		try {
		        			elfobj = new Elf(new File(dir));
		        			textNbParts.setText(Integer.toString(elfobj.getNumPrograms()));
		        			sourceFile.setText(dir);
		        			btnExtract.setEnabled(true);
		        			logger.info("現在你可以按解壓轉換鈕獲得elf檔案資料內容");
		        		}
		        		catch (Exception ex) {
		        			ex.printStackTrace();
		        		}
		        	}
		        }
			}
		});
		GridData gd_btnFileChoose = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_btnFileChoose.widthHint = 34;
		btnFileChoose.setLayoutData(gd_btnFileChoose);
		btnFileChoose.setText("...");
		btnFileChoose.setFont(SWTResourceManager.getFont("Arial", 11, SWT.NORMAL));
		
		Composite composite_1 = new Composite(shlElfExtractor, SWT.NONE);
		composite_1.setLayout(new GridLayout(3, false));
		FormData fd_composite_1 = new FormData();
		fd_composite_1.bottom = new FormAttachment(composite, 38, SWT.BOTTOM);
		fd_composite_1.top = new FormAttachment(composite, 7);
		fd_composite_1.left = new FormAttachment(composite, 0, SWT.LEFT);
		fd_composite_1.right = new FormAttachment(100, -9);
		composite_1.setLayoutData(fd_composite_1);
		
		Label lblNewLabel = new Label(composite_1, SWT.NONE);
		GridData gd_lblNewLabel = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel.widthHint = 112;
		lblNewLabel.setLayoutData(gd_lblNewLabel);
		lblNewLabel.setText("零件數量: ");
		
		textNbParts = new Text(composite_1, SWT.BORDER);
		textNbParts.setEditable(false);
		
		btnExtract = new Button(composite_1, SWT.NONE);
		btnExtract.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					elfobj.unpack();
				}
				catch (Exception ex) {
					logger.error(ex.getMessage());
				}
			}
		});
		btnExtract.setText("解壓轉換");
		btnExtract.setEnabled(false);
		
		Button btnClose = new Button(shlElfExtractor, SWT.NONE);
		btnClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlElfExtractor.dispose();
			}
		});
		FormData fd_btnClose = new FormData();
		fd_btnClose.top = new FormAttachment(composite_1, 6);
		fd_btnClose.right = new FormAttachment(composite, 0, SWT.RIGHT);
		btnClose.setLayoutData(fd_btnClose);
		btnClose.setText("關閉");

	}
}
