package com.itl.comm.yokogawa.WT330;

import com.itl.comm.omega.OM_USB_TC.StringByReference;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;


public interface TmctlLib extends Library {
//	TmcSearchDevices(int wire, DEVICELIST* list, int max, 
//			int* num, char* option);
	
	public int TmcSearchDevices(int wire, StringByReference list, int max,
			IntByReference num, char[] option);
	
	// TmcInitialize(int wire, char* adr, int* id);
	public int TmcInitialize(int wire, StringByReference adr, IntByReference id);
	
	public int TmcSetTerm(int id, int eos, int eot);
	
	public int TmcSetTimeout(int id, int tmo);
	
	public int TmcFinish(int id);
	
	// TmcSend(int id, char* msg);
//	public int TmcSend(int id, StringByReference msg);
	public int TmcSend(int id, String msg); // this works just fine
	
	// TmcReceive(int id, char* buf, int blen, int* rlen);
	public int TmcReceive(int id, StringByReference buf, int blen, IntByReference rlen);
}
