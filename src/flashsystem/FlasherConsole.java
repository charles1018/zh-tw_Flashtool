package flashsystem;

import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import gui.About;
import gui.MainSWT;

import org.adb.AdbUtility;
import org.apache.log4j.Logger;
import org.logger.MyLogger;
import org.system.AdbPhoneThread;
import org.system.DeviceChangedListener;
import org.system.DeviceEntry;
import org.system.DeviceProperties;
import org.system.Devices;
import org.system.GlobalConfig;
import org.system.OS;
import org.system.FTShell;
import org.system.StatusEvent;
import org.system.StatusListener;


public class FlasherConsole {
	
	private static AdbPhoneThread phoneWatchdog;
	private static String fsep = OS.getFileSeparator();
	private static Logger logger = Logger.getLogger(FlasherConsole.class);
	
	public static void init(boolean withadb) {
			logger.info("Flashtool "+About.getVersion());
			MainSWT.guimode=false;
			if (withadb) {
			StatusListener phoneStatus = new StatusListener() {
				public void statusChanged(StatusEvent e) {
					if (!e.isDriverOk()) {
						logger.error("你需要先安裝驅動才能連接裝置");
						logger.error("在Flashtool的drivers資料夾可以找到驅動安裝檔案");
					}
					else {
						if (e.getNew().equals("adb")) {
							logger.info("裝置已通過USB偵錯模式連接");
							logger.debug("裝置已連接，正在識別裝置...");
							doIdent();
						}
						if (e.getNew().equals("none")) {
							logger.info("裝置未連接");
						}
						if (e.getNew().equals("flash")) {
							logger.info("裝置已連接為強刷模式");
						}
						if (e.getNew().equals("fastboot")) {
							logger.info("裝置已連接到fastboot模式");
						}
						if (e.getNew().equals("normal")) {
							logger.info("裝置已連接，但是未打開USB偵錯");
							logger.info("對於2011年的Xperia裝置,請確認你不是在MTP模式下進行連接");
						}
					}
				}
			};
			phoneWatchdog = new AdbPhoneThread();
			phoneWatchdog.start();
			phoneWatchdog.addStatusListener(phoneStatus);
			}
			else DeviceChangedListener.start();
	}

	public static void exit() {
		DeviceChangedListener.stop();
		if (phoneWatchdog!=null) {
			phoneWatchdog.done();
			try {
				phoneWatchdog.join();
			}
			catch (Exception e) {
			}
		}
		MyLogger.writeFile();
		System.exit(0);
	}
	
	public static void doRoot() {
		Devices.waitForReboot(false);
		if (Devices.getCurrent().getVersion().contains("2.3")) {
			if (!Devices.getCurrent().hasRoot())
				doRootzergRush();
			else logger.error("你的裝置已經root");
		}
		else 
			if (!Devices.getCurrent().hasRoot())
				doRootpsneuter();
			else logger.error("你的裝置已經root");
		exit();
	}

	public static void doRootzergRush() {
				try {
					AdbUtility.push(Devices.getCurrent().getBusybox(false), GlobalConfig.getProperty("deviceworkdir")+"/busybox");
					FTShell shell = new FTShell("busyhelper");
					shell.run(true);
					AdbUtility.push(new File("."+fsep+"custom"+fsep+"root"+fsep+"zergrush.tar.uue").getAbsolutePath(),GlobalConfig.getProperty("deviceworkdir"));
					shell = new FTShell("rootit");
					logger.info("正在運行Root的第一步動作，請稍後...");
					shell.run(true);
					Devices.waitForReboot(true);
					logger.info("正在運行Root的第二步動作");
					shell = new FTShell("rootit2");
					shell.run(false);
					logger.info("完成!.");
					logger.info("Root在重啟後生效!");		
				}
				catch (Exception e) {
					logger.error(e.getMessage());
				}
	}

	public static void doRootpsneuter() {
				try {
					AdbUtility.push(Devices.getCurrent().getBusybox(false), GlobalConfig.getProperty("deviceworkdir")+"/busybox");
					FTShell shell = new FTShell("busyhelper");
					shell.run(true);
					AdbUtility.push("."+fsep+"custom"+fsep+"root"+fsep+"psneuter.tar.uue",GlobalConfig.getProperty("deviceworkdir"));
					shell = new FTShell("rootit");
					logger.info("正在運行Root的第一步動作，請稍後...");
					shell.run(false);
					Devices.waitForReboot(true);
					logger.info("正在運行Root的第二步動作");
					shell = new FTShell("rootit2");
					shell.run(false);
					logger.info("完成");
					logger.info("Root在重啟後生效!");		
				}
				catch (Exception e) {
					logger.error(e.getMessage());
				}
	}
	
	public static void doGetIMEI() throws Exception {
		X10flash f=null;
		try {
			Bundle b = new Bundle();
			b.setSimulate(false);
			f = new X10flash(b,null);
			logger.info("請連接你的手機到強刷模式");
			while (!f.deviceFound());
			f.openDevice(false);
			logger.info("IMEI : "+f.getPhoneProperty("IMEI"));
			f.closeDevice();
			exit();
		}
		catch (Exception e) {
			if (f!=null) f.closeDevice();
			throw e;
		}		
	}
	
	public static void doExtract(String file) {
		try {
			SinFile sin = new SinFile(file);
			sin.dumpImage();
		}
		catch (Exception e) {
		}
	}

	public static void doFlash(String file,boolean wipedata,boolean wipecache,boolean excludebb,boolean excludekrnl, boolean excludesys) throws Exception {
		X10flash f=null;
		try {
			File bf = new File(file);
			if (!bf.exists()) {
				logger.error("檔案"+bf.getAbsolutePath()+"不存在");
				exit();
			}
			logger.info("已選擇"+bf.getAbsolutePath());
			Bundle b = new Bundle(bf.getAbsolutePath(),Bundle.JARTYPE);
			b.setSimulate(false);
			b.getMeta().setCategEnabled("DATA", wipedata);
			b.getMeta().setCategEnabled("CACHE", wipecache);
			b.getMeta().setCategEnabled("BASEBAND", excludebb);
			b.getMeta().setCategEnabled("SYSTEM", excludesys);
			b.getMeta().setCategEnabled("KERNEL", excludekrnl);
			logger.info("正在準備檔案...");
			b.open();
			f = new X10flash(b,null);
			logger.info("請連接你的裝置到強刷模式");
			while (!f.deviceFound());
			f.openDevice(false);
			f.flashDevice();
			b.close();
			exit();
		}
		catch (Exception e) {
			if (f!=null) f.closeDevice();
			throw e;
		}		
	}

	public static void doIdent() {
    		Enumeration<Object> e = Devices.listDevices(true);
    		if (!e.hasMoreElements()) {
    			logger.error("沒有裝置適用於Flashtool");
    			logger.error("你只能進行強刷動作");
    			return;
    		}
    		boolean found = false;
    		Properties founditems = new Properties();
    		founditems.clear();
    		Properties buildprop = new Properties();
    		buildprop.clear();
    		while (e.hasMoreElements()) {
    			DeviceEntry current = Devices.getDevice((String)e.nextElement());
    			String prop = current.getBuildProp();
    			if (!buildprop.containsKey(prop)) {
    				String readprop = DeviceProperties.getProperty(prop);
    				buildprop.setProperty(prop,readprop);
    			}
    			Iterator<String> i = current.getRecognitionList().iterator();
    			String localdev = buildprop.getProperty(prop);
    			while (i.hasNext()) {
    				String pattern = i.next().toUpperCase();
    				if (localdev.toUpperCase().contains(pattern)) {
    					founditems.put(current.getId(), current.getName());
    				}
    			}
    		}
    		if (founditems.size()==1) {
    			found = true;
    			Devices.setCurrent((String)founditems.keys().nextElement());
    			if (!Devices.isWaitingForReboot())
    				logger.info("已連接裝置: " + Devices.getCurrent().getId());
    		}
    		else {
    			logger.error("無法驗證你的裝置");
        		logger.error("你只能進行強刷動作");
    		}
    		if (found) {
    			if (!Devices.isWaitingForReboot()) {
    				logger.info("已安裝的busybox版本: " + Devices.getCurrent().getInstalledBusyboxVersion(false));
    				logger.info("Android版本: "+Devices.getCurrent().getVersion()+" / kernel version : "+Devices.getCurrent().getKernelVersion());
    			}
    			if (Devices.getCurrent().isRecovery()) {
    				logger.info("裝置在recovery模式");
    				if (!Devices.isWaitingForReboot())
    					logger.info("已允許Root權限");
    			}
    			else {
    				boolean hasSU = Devices.getCurrent().hasSU();
    				if (hasSU) {
    					boolean hasRoot = Devices.getCurrent().hasRoot();
    					if (hasRoot)
    						if (!Devices.isWaitingForReboot())
    							logger.info("已允許Root權限");
    				}
    			}
    			logger.debug("停止等待裝置連接...");
    			if (Devices.isWaitingForReboot())
    				Devices.stopWaitForReboot();
    			logger.debug("結束識別");
    		}
	}

}