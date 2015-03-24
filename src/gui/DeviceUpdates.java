package gui;

import gui.models.TableLine;
import gui.models.TableSorter;
import gui.models.VectorContentProvider;
import gui.models.VectorLabelProvider;
import gui.tools.DecryptJob;
import gui.tools.WidgetTask;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.system.DeviceEntry;
import org.system.DeviceEntryModel;
import org.system.DeviceEntryModelUpdater;
import org.system.OS;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Label;
import org.logger.LogProgress;

import com.iagucool.xperifirm.Firmware;

public class DeviceUpdates extends Dialog {

	protected Object result;
	protected Shell shlDeviceUpdateChecker;
	protected CTabFolder tabFolder;
	protected DeviceEntry _entry;
	protected Label lblInfo;
	protected Button closeButton;
	//protected CTabItem tabItem;
	//private Table tableDevice;
	//private TableViewer tableViewer;
	String bundleResult="";
	private static Logger logger = Logger.getLogger(DeviceUpdates.class);
	private DownloadJob dj;
	private CheckJob cj;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public DeviceUpdates(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open(DeviceEntry entry) {
		_entry = entry;
		createContents();
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
		shlDeviceUpdateChecker.addListener(SWT.Close, new Listener() {
		      public void handleEvent(Event event) {
		    	  event.doit=closeButton.getText().equals("Close");
		      }
		    });

		shlDeviceUpdateChecker.setSize(450, 300);
		shlDeviceUpdateChecker.setText("裝置更新檢查");
		
		tabFolder = new CTabFolder(shlDeviceUpdateChecker, SWT.BORDER);
		tabFolder.setBounds(11, 10, 423, 223);
		tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));				
		
		closeButton = new Button(shlDeviceUpdateChecker, SWT.NONE);
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (closeButton.getText().equals("關閉"))
					shlDeviceUpdateChecker.dispose();
				else if (closeButton.getText().equals("取消")) {
					dj.Cancel();
				}
					

			}
		});
		closeButton.setBounds(359, 239, 75, 25);
		closeButton.setText("關閉");
		
		lblInfo = new Label(shlDeviceUpdateChecker, SWT.NONE);
		lblInfo.setBounds(11, 244, 342, 15);

		FillJob fj = new FillJob("搜尋更新");
		fj.schedule();
	}


	public void addTab(final DeviceEntryModel model) {
		if (model.canShowUpdates()) {
		Display.getDefault().asyncExec(
				new Runnable() {
					public void run() {
						Vector<TableLine> result = new Vector<TableLine>();
						DeviceEntryModelUpdater mu = model.getUpdater();
						CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
						tabItem.setText(model.getId());
						TableViewer tableViewer = new TableViewer(tabFolder,SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.SINGLE);						
						tableViewer.setContentProvider(new VectorContentProvider());
						tableViewer.setLabelProvider(new VectorLabelProvider());

						// Create the popup menu
						  MenuManager menuMgr = new MenuManager();
						  Menu menu = menuMgr.createContextMenu(tableViewer.getControl());
						  menuMgr.addMenuListener(new IMenuListener() {
						    @Override
						    public void menuAboutToShow(IMenuManager manager) {
						    	  if (closeButton.getText().equals("關閉")) {
								    	TableLine tl = (TableLine)tableViewer.getTable().getSelection()[0].getData();
								    	if (tl.getValueOf(2).length()==0) {
									    	manager.add(new Action("Check release") {
									            public void run() {
									            	doCheck(tableViewer,tl,mu);
									            }
									        });
								    	}
								    	else {
									    	manager.add(new Action("Download") {
									            public void run() {
									            	doDownload(tl,mu);
									            }
									        });
								    	}
						    	  }
						    }
						  });

						menuMgr.setRemoveAllWhenShown(true);
						tableViewer.getControl().setMenu(menu);

						
						TableColumn[] columns = new TableColumn[3];
						columns[0] = new TableColumn(tableViewer.getTable(), SWT.NONE);
						columns[0].setText("Id");
						columns[1] = new TableColumn(tableViewer.getTable(), SWT.NONE);
						columns[1].setText("地區");
						columns[2] = new TableColumn(tableViewer.getTable(), SWT.NONE);
						columns[2].setText("版本");
						tableViewer.getTable().setHeaderVisible(true);
						tableViewer.getTable().setLinesVisible(true);
						TableSorter sort = new TableSorter(tableViewer);
						tableViewer.getTable().setSortColumn(tableViewer.getTable().getColumn(0));
						tableViewer.getTable().setSortDirection(SWT.UP);
						tableViewer.setInput(result);
						tableViewer.getTable().addListener(SWT.DefaultSelection, new Listener() {
						      public void handleEvent(Event e) {
						    	  if (closeButton.getText().equals("關閉")) {
							    	  TableLine tl = (TableLine)tableViewer.getTable().getSelection()[0].getData();
							    	  if (tl.getValueOf(2).length()==0) {
							    		  	doCheck(tableViewer,tl,mu);
									  }
							    	  else {
							    		  	doDownload(tl,mu);
							    	  }
						    	  }
						      }
						    });

						Iterator cdflist = mu.getCustIds().getProperties().keySet().iterator();
						while (cdflist.hasNext()) {
							String id = (String)cdflist.next();
							TableLine line1 = new TableLine();
							line1.add(id);
							line1.add(mu.getCustIds().getProperty(id));
							line1.add("");
							result.add(line1);
							tableViewer.refresh();
						}
										tableViewer.setInput(result);
										for (int i = 0, n = tableViewer.getTable().getColumnCount(); i < n; i++) {
											tableViewer.getTable().getColumn(i).pack();
										}
										tableViewer.getTable().pack();
										tableViewer.refresh();
										tabItem.setControl(tableViewer.getTable());

					}
				}
		);
		}
	}

	public void doDownload(TableLine tl, DeviceEntryModelUpdater mu) {
    	dj = new DownloadJob("下載韌體");
    	dj.setCDF(tl.getValueOf(0));
    	String path = OS.getFolderFirmwaresDownloaded()+File.separator+mu.getModel()+"_"+tl.getValueOf(1).replaceAll(" ","_") + "_" + mu.getReleaseOf(tl.getValueOf(0));
    	dj.setPath(path);
    	dj.setUpdater(mu);
    	dj.schedule();		
	}

	public void doCheck(TableViewer tableViewer, TableLine tl, DeviceEntryModelUpdater mu) {
		cj = new CheckJob("檢查更新");
		cj.setTableLine(tl);
		cj.setTableViewer(tableViewer);
		cj.setModelUpdater(mu);
		cj.schedule();
	}
	
	public void fillTab() {
		Iterator<DeviceEntryModel> imodels = _entry.getModels().iterator();
		while (imodels.hasNext()) {
			addTab(imodels.next());
		}
		Display.getDefault().asyncExec(
				new Runnable() {
					public void run() {
						tabFolder.redraw();
						tabFolder.setSelection(0);
					}
				}
		);
	}

	class FillJob extends Job {

		boolean canceled = false;

		public FillJob(String name) {
			super(name);
		}
		
		public void stopSearch() {
			canceled=true;
		}
		
	    protected IStatus run(IProgressMonitor monitor) {
			    while (!canceled) {
					Display.getDefault().asyncExec(
							new Runnable() {
								public void run() {
									lblInfo.setText("正在檢查最新版本,請稍後...");
								}
							}
					);
					fillTab();
					Display.getDefault().asyncExec(
							new Runnable() {
								public void run() {
									lblInfo.setText("");
								}
							}
					);
					return Status.OK_STATUS;
			    }
			    return Status.CANCEL_STATUS;
	    }
	}

	class CheckJob extends Job {

		boolean canceled = false;
		TableLine tl=null;
		TableViewer tableViewer=null;
		DeviceEntryModelUpdater mu=null;

		public void setTableLine(TableLine ptl) {
			tl = ptl;
		}
		
		public void setTableViewer(TableViewer ptv) {
			tableViewer = ptv;
		}
		
		public void setModelUpdater(DeviceEntryModelUpdater pmu) {
			mu = pmu;
		}
		
		public CheckJob(String name) {
			super(name);
			this.addJobChangeListener(new JobChangeAdapter(){
				public void done(IJobChangeEvent event) {
					Display.getDefault().asyncExec(
							new Runnable() {
								public void run() {
									closeButton.setText("關閉");
									lblInfo.setText("");
								}
							}
					);
					LogProgress.initProgress(0);
				}
			});
		}
		
		public void stopSearch() {
			canceled=true;
		}
		
		public void setUpdater(DeviceEntryModelUpdater pmu) {
			mu=pmu;
		}
		
	    protected IStatus run(IProgressMonitor monitor) {
			Display.getDefault().asyncExec(
					new Runnable() {
						public void run() {
							closeButton.setText("取消");
							lblInfo.setText("正在檢查最新版本,請稍後...");
						}
					}
			);
	    	String release = mu.getReleaseOf(tl.getValueOf(0));
			Display.getDefault().asyncExec(
					new Runnable() {
						public void run() {
							tl.setValueOf(2, release);
							int lastsize = tableViewer.getControl().getSize().x-tableViewer.getTable().getColumn(0).getWidth()-tableViewer.getTable().getColumn(1).getWidth();
							tableViewer.getTable().getColumn(2).setWidth(lastsize-20);
						    tableViewer.refresh();
						}
					}
			);
			return Status.OK_STATUS;
	    }
	}
	
	class DownloadJob extends Job {

		boolean canceled = false;
		String cdfval;
		DeviceEntryModelUpdater mu=null;
		String _path = "";
		Firmware firm;

		public DownloadJob(String name) {
			super(name);
		}
		
		public void stopSearch() {
			canceled=true;
		}
		
		public void setUpdater(DeviceEntryModelUpdater pmu) {
			mu=pmu;
		}
		
		public void setCDF(String cdf) {
			cdfval = cdf;
		}
		
		public void Cancel() {
			firm.cancelDownload();
		}
		
		public void setPath(String path) {
			_path = path;
			logger.info("儲存韌體到" + _path);
		}
		
	    protected IStatus run(IProgressMonitor monitor) {
			Display.getDefault().asyncExec(
					new Runnable() {
						public void run() {
							closeButton.setText("取消");
							lblInfo.setText("正在下載最新版本,請稍後...");
						}
					}
			);
            	firm = mu.getFilesOf(cdfval);
            	firm.resetCancelation();
            	Vector result = firm.download(_path);
            	if (firm.isDownloaded()) { 
        			Display.getDefault().asyncExec(
        					new Runnable() {
        						public void run() {
        							lblInfo.setText("解壓轉換檔案中,請稍後...");
        						}
        					}
        			);            		
    					DecryptJob dec = new DecryptJob("解壓轉換");
    					dec.addJobChangeListener(new IJobChangeListener() {
    						public void aboutToRun(IJobChangeEvent event) {
    						}

    						public void awake(IJobChangeEvent event) {
    						}

    						public void done(IJobChangeEvent event) {
    							Display.getDefault().syncExec(
    									new Runnable() {
    										public void run() {
    											lblInfo.setText("正在建立韌體中,請稍後...");
    								    		BundleCreator cre = new BundleCreator(shlDeviceUpdateChecker,SWT.PRIMARY_MODAL | SWT.SHEET);
    								    		cre.setBranding(mu.getCustIds().getProperty(cdfval).replaceAll(" ", "_"));
    								    		cre.setVariant(mu.getDevice().getName(), mu.getModel());
    								    		cre.setVersion(mu.getReleaseOf(cdfval));
    								    		bundleResult = (String)cre.open(_path+File.separator+"decrypted");
    								    		lblInfo.setText("");
    	    									closeButton.setText("關閉");
    										}
    									}
    							);

    							if (bundleResult.equals("取消"))
    								logger.info("已取消建立韌體");
    						}

    						public void running(IJobChangeEvent event) {
    						}

    						public void scheduled(IJobChangeEvent event) {
    						}

    						public void sleeping(IJobChangeEvent event) {
    						}
    					});
    					dec.setFiles(result);
    					dec.schedule();
            	}
            	else {
					Display.getDefault().syncExec(
							new Runnable() {
								public void run() {
						    		lblInfo.setText("");
									closeButton.setText("關閉");
								}
							}
					);            		
            	}
			    return Status.OK_STATUS;
	    }
	}
}