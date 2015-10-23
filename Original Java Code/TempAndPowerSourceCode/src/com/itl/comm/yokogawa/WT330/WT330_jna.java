package com.itl.comm.yokogawa.WT330;

import java.io.File;

import com.itl.comm.omega.OM_USB_TC.StringByReference;
import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;

/**
 * This class was originally intended to implement the PowerMeter interface
 * for the Yokogawa 330 series of power meters, communicating via the USB 
 * interface. Using the USB interface required using the .dll files that
 * came with the Yokogawa 330 series of meters and using JNA to access those.
 * Unfortunately, this proved unstable as certain commands could simply not
 * be sent to the meter for some unknown reason. They wouldn't fail as such
 * or not be recognized by the meter, TmcSend simply would refuse to complete,
 * which in turn would end the program. Furthermore, there seemed to be no
 * pattern regarding which commands would fail and which would succeed!
 * <p>
 * As a result, I decided to go with a different approach. I'm leaving this
 * class (as well as the TmctlLib class) in just in case someone else wants
 * to try their hand at this...or perhaps as a warning to subsequent programmers
 * to watch out for certain things.
 * 
 * @author kgraba
 *
 */
public class WT330_jna {
	// JNA wrapper
	TmctlLib tmctlLib;
	
	public WT330_jna() {
		File libFile = new File("C:/Windows/System32/tmctl64.dll");
		System.load(libFile.getAbsolutePath());
		tmctlLib = (TmctlLib) Native.loadLibrary(
				"tmctl64", TmctlLib.class);
		System.out.println("Library loaded.");
		
		//DEVICELIST listbuff[127]
//		String[] deviceNames = new String[127];
//		Pointer deviceNames = new Pointer(); // listBuff
//		deviceNames.setChar(0, 'a');
		
		// We really ought to be passing an array of StringByReference
		// objects (or a pointer to a StringByReference!). But hopefully
		// we'll never need to look through more than one device.
		StringByReference listBuff = new StringByReference("foo");
		
		// doesn't work
//		StringByReference[] listBuff = new StringByReference[127];
		
		IntByReference num = new IntByReference(0);
		
		int wire = 7;
		tmctlLib.TmcSearchDevices(wire, listBuff, 127, num, null);
		
		System.out.println("Num devices found: " + num.getValue());
		if (num.getValue() > 0) {
			System.out.println("ListBuff[0]: " + listBuff.getValue());
			
			try {
				CheckWTSeries(wire, listBuff);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public int CheckWTSeries(int wire, StringByReference addr) 
			throws InterruptedException {
		int ret;
		
		IntByReference m_iId = new IntByReference(0);
		ret = tmctlLib.TmcInitialize(wire, addr, m_iId);
		int id = m_iId.getValue();
		
		if (ret != 0) {
			return ret;
		}
		
		System.out.println("addr: " + addr.getValue());
		System.out.println("ID: " + id);
		
		// magic numbers from DeviceSearch.cpp
		ret = tmctlLib.TmcSetTerm(id, 2, 1);
		if (ret != 0) {
			tmctlLib.TmcFinish(id);
			return ret;
		}
		
		// tmcsettimeout(1*100ms)
//		ret = tmctlLib.TmcSetTimeout(m_iId.getValue(), 1);
		
		ret = tmctlLib.TmcSetTimeout(m_iId.getValue(), 10);
		if (ret != 0) {
			tmctlLib.TmcFinish(m_iId.getValue());
			return ret;
		}
		
//		StringByReference msg = new StringByReference("*IDN?");
//		StringByReference msg = new StringByReference(":STATUS:ERROR?");
		
		// finally, test the device module connected
//		ret = tmctlLib.TmcSend(m_iId.getValue(), msg);
		ret = tmctlLib.TmcSend(m_iId.getValue(), "*IDN?");
//		ret = tmctlLib.TmcSend(m_iId.getValue(), ":STATUS:ERROR?");
		
		// DON'T NAME THIS recBuf for some reason!!
		StringByReference rec = new StringByReference();
		int maxLen = 256;
		IntByReference recLen = new IntByReference(0);
		ret = tmctlLib.TmcReceive(m_iId.getValue(), rec, maxLen, recLen);
		if (ret != 0) {
			tmctlLib.TmcFinish(m_iId.getValue());
		}
		
		String sRec = rec.getValue();
		System.out.println("Receiver buffer: " + sRec.trim());
		ret = 1;
		
		if (sRec.contains("WT3")) {
			System.out.println("WT300 device found!\n");
			ret = 1;
		} else {
			System.out.println("WT300 device not found!\n");
			ret = 0;
		}
		
		// let's make a trial query
		if (ret == 1) {
			ret = tmctlLib.TmcSend(id, ":NUMERIC:FORMAT FLOAT");
			ret = tmctlLib.TmcSend(id, ":NUMERIC:FORMAT?");
			ret = tmctlLib.TmcReceive(id, rec, maxLen, recLen);
			System.out.println("Received: " + rec.getValue().trim());
			
			ret = tmctlLib.TmcSend(id, ":NUMERIC:FORMAT ASCII");
			ret = tmctlLib.TmcSend(id, ":NUMERIC:FORMAT?");
			ret = tmctlLib.TmcReceive(id, rec, maxLen, recLen);
			System.out.println("Received: " + rec.getValue().trim());

			ret = tmctlLib.TmcSend(id, ":STATUS?");
			ret = tmctlLib.TmcReceive(id, rec, maxLen, recLen);
			System.out.println("Received: " + rec.getValue().trim());

			try {
				ret = tmctlLib.TmcSend(id, ":RATE?");
				ret = tmctlLib.TmcReceive(id, rec, maxLen, recLen);
				System.out.println("Received: " + rec.getValue().trim());
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			
//			msg = new StringByReference(":NUMERIC:NORMAL:ITEM1 U,1;ITEM2 I,1;ITEM3 P,1");
//			ret = tmctlLib.TmcSend(id, msg);
//			if (ret != 0) {
//				System.out.println("Error sending " + msg.getValue());
//			} else {
//				System.out.println("Sent " + msg.getValue());
//			}

			Thread.sleep(100);
			
//			msg = new StringByReference(":NUMERIC:NORMAL:VALUE?");
//			ret = tmctlLib.TmcSend(id, msg);
//			if (ret != 0) {
//				System.out.println("Error sending " + msg.getValue());
//			}
			
			ret = tmctlLib.TmcSend(id, ":NUMERIC:FORMAT?");
			ret = tmctlLib.TmcReceive(id, rec, maxLen, recLen);
			System.out.println("Received: " + rec.getValue().trim());
			
			ret = tmctlLib.TmcSend(id, ":NUMERIC:NORMAL:NUMBER?");
			ret = tmctlLib.TmcReceive(id, rec, maxLen, recLen);
			System.out.println("Received: " + rec.getValue().trim());
			
			ret = tmctlLib.TmcSend(id, ":NUMERIC:NORMAL:VALUE?");
			ret = tmctlLib.TmcReceive(id, rec, maxLen, recLen);
			System.out.println("Received: " + rec.getValue().trim());
		}
		
		// cleanup
		tmctlLib.TmcSetTimeout(m_iId.getValue(), 20);
		tmctlLib.TmcFinish(m_iId.getValue());
		
		return ret;
	}
	
	public static void main(String[] args) {
		System.out.println("Hello world!");
		
		WT330_jna wt330 = new WT330_jna();
	}
}
