package gui;


import gui.models.PropertiesFileContentProvider;
import gui.models.TableLine;
import gui.models.TableSorter;
import gui.models.VectorLabelProvider;

import java.util.Iterator;
import java.util.Properties;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.system.DeviceEntry;
import org.system.DeviceEntryModel;
import org.system.DeviceEntryModelUpdater;

import gui.tools.WidgetsTool;

public class CustIdManager extends Dialog {

	protected Object result;
	protected Shell shlDeviceUpdateChecker;
	protected CTabFolder tabFolder;
	protected Label lblInfo;
	protected Button btnApply;
	protected DeviceEntry entry;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public CustIdManager(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	
	public Object open(DeviceEntry e) {
		entry=e;
		createContents();
		WidgetsTool.setSize(shlDeviceUpdateChecker);
		shlDeviceUpdateChecker.open();
		shlDeviceUpdateChecker.layout();
		Display display = getParent().getDisplay();
		while (!shlDeviceUpdateChecker.isDisposed()) {
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
		shlDeviceUpdateChecker = new Shell(getParent(), getStyle());
		shlDeviceUpdateChecker.setSize(450, 336);
		shlDeviceUpdateChecker.setText("cdfID管理器");
		
		tabFolder = new CTabFolder(shlDeviceUpdateChecker, SWT.BORDER);
		tabFolder.setBounds(11, 10, 423, 256);
		tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));				
		
		Button btnNewButton = new Button(shlDeviceUpdateChecker, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlDeviceUpdateChecker.dispose();
			}
		});
		btnNewButton.setBounds(359, 272, 75, 25);
		btnNewButton.setText("關閉");
		
		lblInfo = new Label(shlDeviceUpdateChecker, SWT.NONE);
		lblInfo.setBounds(11, 244, 342, 15);
		btnApply = new Button(shlDeviceUpdateChecker, SWT.NONE);
		btnApply.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Iterator<DeviceEntryModel> i = entry.getModels().iterator();
				while (i.hasNext()) {
					DeviceEntryModelUpdater mu = i.next().getUpdater();
					if (mu.isModified())
						mu.save();
					btnApply.setEnabled(false);
				}
			}
		});
		btnApply.setBounds(279, 272, 75, 25);
		btnApply.setText("應用");
		btnApply.setEnabled(false);
		parseModels();
	}

	public void parseModels() {
		Iterator<DeviceEntryModel> models = entry.getModels().iterator();
		while (models.hasNext()) {
			DeviceEntryModelUpdater m = models.next().getUpdater();
			addTab(m);
		}
	}
	
	public void addTab(final DeviceEntryModelUpdater m) {
		final TableViewer tableViewer = new TableViewer(tabFolder,SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.SINGLE);
		Display.getDefault().syncExec(
				new Runnable() {
					public void run() {
						tableViewer.setContentProvider(new PropertiesFileContentProvider());
						tableViewer.setLabelProvider(new VectorLabelProvider());
						// Create the popup menu
						  MenuManager menuMgr = new MenuManager();
						  Menu menu = menuMgr.createContextMenu(tableViewer.getControl());
						  menuMgr.addMenuListener(new IMenuListener() {
						    @Override
						    public void menuAboutToShow(IMenuManager manager) {
						    	manager.add(new Action("新增") {
						            public void run() {
										AddCustId add = new AddCustId(shlDeviceUpdateChecker,SWT.PRIMARY_MODAL | SWT.SHEET);
										Properties item = (Properties)add.open(m);
										if (item.getProperty("CDA") != null) {
											m.AddCustId(item.getProperty("CDA"),item.getProperty("REGION"));
											btnApply.setEnabled(true);
							            	tableViewer.refresh();
										}
						            }
						        });						    		
						    	if (!tableViewer.getSelection().isEmpty()) {
							    	manager.add(new Action("編輯") {
							            public void run() {
											AddCustId add = new AddCustId(shlDeviceUpdateChecker,SWT.PRIMARY_MODAL | SWT.SHEET);
											TableLine line = (TableLine)tableViewer.getTable().getSelection()[0].getData();
											Properties item = (Properties)add.open(m,line.getValueOf(0),line.getValueOf(1));
											if (item.getProperty("CDA") != null) {
												m.RemoveCustId(line.getValueOf(0));
												m.AddCustId(item.getProperty("CDA"),item.getProperty("REGION"));
												line.setValueOf(0, item.getProperty("CDA"));
												line.setValueOf(1, item.getProperty("REGION"));
												btnApply.setEnabled(true);
								            	tableViewer.refresh();
											}
							            }
							        });
							    	manager.add(new Action("刪除") {
							            public void run() {
							            	m.RemoveCustId(((TableLine)tableViewer.getTable().getSelection()[0].getData()).getValueOf(0));
							            	btnApply.setEnabled(true);
							            	tableViewer.refresh();
							            }
							        });
						    	}
						    }
						  });

						menuMgr.setRemoveAllWhenShown(true);
						tableViewer.getControl().setMenu(menu);
						Table tableDevice = tableViewer.getTable();
						TableColumn[] columns = new TableColumn[2];
						columns[0] = new TableColumn(tableDevice, SWT.NONE);
						columns[0].setText("Id");
						columns[1] = new TableColumn(tableDevice, SWT.NONE);
						columns[1].setText("Name");
						tableDevice.setHeaderVisible(true);
						tableDevice.setLinesVisible(true);
						TableSorter sort = new TableSorter(tableViewer);
						tableDevice.setSortColumn(tableDevice.getColumn(0));
						tableDevice.setSortDirection(SWT.UP);
						tableViewer.setInput(m.getCustIds());
						for (int i = 0, n = tableViewer.getTable().getColumnCount(); i < n; i++) {
							tableViewer.getTable().getColumn(i).pack();
						}
						tableViewer.getTable().pack();
						tableViewer.refresh();
						final CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
						tabItem.setText(m.getModel());
						tabItem.setControl(tableViewer.getTable());
						tabFolder.setSelection(tabItem);
					}
				}
		);
	}
}