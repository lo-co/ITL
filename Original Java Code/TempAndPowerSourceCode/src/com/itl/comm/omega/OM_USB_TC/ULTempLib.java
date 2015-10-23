package com.itl.comm.omega.OM_USB_TC;

import com.sun.jna.Library;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;

/**
 * Interface to connect to either the cbw32 or cbs64 libraries with
 * JNA
 * @author kgraba
 *
 */
public interface ULTempLib extends Library {
	// the following constants are from CBW.H
	// ugh, constants in an interface
	/* Current Revision Number */
	public static final String CURRENTREVNUM = "6.10";
	
	/* System error code */
	public static final int NOERRORS = 0;    /* No error occurred */
	public static final int BADBOARD = 1;    /* Invalid board number specified */
	public static final int DEADDIGITALDEV = 2;    /* Digital I/O device is not responding  */
	public static final int DEADCOUNTERDEV = 3;    /* Counter I/O device is not responding */
	public static final int DEADDADEV = 4;    /* D/A is not responding */
	public static final int DEADADDEV = 5;    /* A/D is not responding */
	public static final int NOTDIGITALCONF = 6;    /* Specified board does not have digital I/O */
	public static final int NOTCOUNTERCONF = 7;    /* Specified board does not have a counter */
	public static final int NOTDACONF = 8;    /* Specified board is does not have D/A */
	public static final int NOTADCONF = 9;    /* Specified board does not have A/D */
	public static final int NOTMUXCONF = 10;   /* Specified board does not have thermocouple inputs */
	public static final int BADPORTNUM = 11;   /* Invalid port number specified */
	public static final int BADCOUNTERDEVNUM = 12;   /* Invalid counter device */
	public static final int BADDADEVNUM = 13;   /* Invalid D/A device */
	public static final int BADSAMPLEMODE = 14;   /* Invalid sampling mode option specified */
	public static final int BADINT = 15;   /* Board configured for invalid interrupt level */
	public static final int BADADCHAN = 16;   /* Invalid A/D channel Specified */
	public static final int BADCOUNT = 17;   /* Invalid count specified */
	public static final int BADCNTRCONFIG = 18;   /* invalid counter configuration specified */
	public static final int BADDAVAL = 19;   /* Invalid D/A output value specified */
	public static final int BADDACHAN = 20;   /* Invalid D/A channel specified */
	public static final int ALREADYACTIVE = 22;   /* A background process is already in progress */
	public static final int PAGEOVERRUN = 23;   /* DMA transfer crossed page boundary, may have gaps in data */
	public static final int BADRATE = 24;   /* Inavlid sampling rate specified */
	public static final int COMPATMODE = 25;   /* Board switches set for "compatible" mode */
	public static final int TRIGSTATE = 26;   /* Incorrect intial trigger state D0 must=TTL low) */
	public static final int ADSTATUSHUNG = 27;   /* A/D is not responding */
	public static final int TOOFEW = 28;   /* Too few samples before trigger occurred */
	public static final int OVERRUN = 29;   /* Data lost due to overrun, rate too high */
	public static final int BADRANGE = 30;   /* Invalid range specified */
	public static final int NOPROGGAIN = 31;   /* Board does not have programmable gain */
	public static final int BADFILENAME = 32;   /* Not a legal DOS filename */
	public static final int DISKISFULL = 33;   /* Couldn't complete, disk is full */
	public static final int COMPATWARN = 34;   /* Board is in compatible mode, so DMA will be used */
	public static final int BADPOINTER = 35;   /* Invalid pointer (NULL) */
	public static final int TOOMANYGAINS = 36;   /* Too many gains */
	public static final int RATEWARNING = 37;   /* Rate may be too high for interrupt I/O */
	public static final int CONVERTDMA = 38;   /* CONVERTDATA cannot be used with DMA I/O */
	public static final int DTCONNECTERR = 39;   /* Board doesn't have DT Connect */
	public static final int FORECONTINUOUS = 40;   /* CONTINUOUS can only be used with BACKGROUND */
	public static final int BADBOARDTYPE = 41;   /* This function can not be used with this board */
	public static final int WRONGDIGCONFIG = 42;   /* Digital I/O is configured incorrectly */
	public static final int NOTCONFIGURABLE = 43;   /* Digital port is not configurable */
	public static final int BADPORTCONFIG = 44;   /* Invalid port configuration specified */
	public static final int BADFIRSTPOINT = 45;   /* First point argument is not valid */
	public static final int ENDOFFILE = 46;   /* Attempted to read past end of file */
	public static final int NOT8254CTR = 47;   /* This board does not have an 8254 counter */
	public static final int NOT9513CTR = 48;   /* This board does not have a 9513 counter */
	public static final int BADTRIGTYPE = 49;   /* Invalid trigger type */
	public static final int BADTRIGVALUE = 50;   /* Invalid trigger value */
	public static final int BADOPTION = 52;   /* Invalid option specified for this function */
	public static final int BADPRETRIGCOUNT = 53;   /* Invalid pre-trigger count sepcified */
	public static final int BADDIVIDER = 55;   /* Invalid fout divider value */
	public static final int BADSOURCE = 56;   /* Invalid source value  */
	public static final int BADCOMPARE = 57;   /* Invalid compare value */
	public static final int BADTIMEOFDAY = 58;   /* Invalid time of day value */
	public static final int BADGATEINTERVAL = 59;   /* Invalid gate interval value */
	public static final int BADGATECNTRL = 60;   /* Invalid gate control value */
	public static final int BADCOUNTEREDGE = 61;   /* Invalid counter edge value */
	public static final int BADSPCLGATE = 62;   /* Invalid special gate value */
	public static final int BADRELOAD = 63;   /* Invalid reload value */
	public static final int BADRECYCLEFLAG = 64;   /* Invalid recycle flag value */
	public static final int BADBCDFLAG = 65;   /* Invalid BCD flag value */
	public static final int BADDIRECTION = 66;   /* Invalid count direction value */
	public static final int BADOUTCONTROL = 67;   /* Invalid output control value */
	public static final int BADBITNUMBER = 68;   /* Invalid bit number */
	public static final int NONEENABLED = 69;   /* None of the counter channels are enabled */
	public static final int BADCTRCONTROL = 70;   /* Element of control array not ENABLED/DISABLED */
	public static final int BADEXPCHAN = 71;   /* Invalid EXP channel */
	public static final int WRONGADRANGE = 72;   /* Wrong A/D range selected for cbtherm */
	public static final int OUTOFRANGE = 73;   /* Temperature input is out of range */
	public static final int BADTEMPSCALE = 74;   /* Invalid temperate scale */
	public static final int BADERRCODE = 75;   /* Invalid error code specified */
	public static final int NOQUEUE = 76;   /* Specified board does not have chan/gain queue */
	public static final int CONTINUOUSCOUNT = 77;   /* CONTINUOUS can not be used with this count value */
	public static final int UNDERRUN = 78;   /* D/A FIFO hit empty while doing output */
	public static final int BADMEMMODE = 79;   /* Invalid memory mode specified */
	public static final int FREQOVERRUN = 80;   /* Measured frequency too high for gating interval */
	public static final int NOCJCCHAN = 81;   /* Board does not have CJC chan configured */
	public static final int BADCHIPNUM = 82;   /* Invalid chip number used with cbC9513Init */
	public static final int DIGNOTENABLED = 83;   /* Digital I/O not enabled */
	public static final int CONVERT16BITS = 84;   /* CONVERT option not allowed with 16 bit A/D */
	public static final int NOMEMBOARD = 85;   /* EXTMEMORY option requires memory board */
	public static final int DTACTIVE = 86;   /* Memory I/O while DT Active */
	public static final int NOTMEMCONF = 87;   /* Specified board is not a memory board */
	public static final int ODDCHAN = 88;   /* First chan in queue can not be odd */
	public static final int CTRNOINIT = 89;   /* Counter was not initialized */
	public static final int NOT8536CTR = 90;   /* Specified counter is not an 8536 */
	public static final int FREERUNNING = 91;   /* A/D sampling is not timed */
	public static final int INTERRUPTED = 92;   /* Operation interrupted with CTRL-C */
	public static final int NOSELECTORS = 93;   /* Selector could not be allocated */
	public static final int NOBURSTMODE = 94;   /* Burst mode is not supported on this board */
	public static final int NOTWINDOWSFUNC = 95;   /* This function not available in Windows lib */
	public static final int NOTSIMULCONF = 96;   /* Not configured for simultaneous update */
	public static final int EVENODDMISMATCH = 97;   /* Even channel in odd slot in the queue */
	public static final int M1RATEWARNING = 98;   /* DAS16/M1 sample rate too fast */
	public static final int NOTRS485 = 99;   /* Board is not an RS-485 board */
	public static final int NOTDOSFUNC = 100;   /* This function not avaliable in DOS */
	public static final int RANGEMISMATCH = 101;   /* Unipolar and Bipolar can not be used together in A/D que */
	public static final int CLOCKTOOSLOW = 102;   /* Sample rate too fast for clock jumper setting */
	public static final int BADCALFACTORS = 103;   /* Cal factors were out of expected range of values */
	public static final int BADCONFIGTYPE = 104;   /* Invalid configuration type information requested */
	public static final int BADCONFIGITEM = 105;   /* Invalid configuration item specified */
	public static final int NOPCMCIABOARD = 106;   /* Can't acces PCMCIA board */
	public static final int NOBACKGROUND = 107;   /* Board does not support background I/O */
	public static final int STRINGTOOSHORT = 108;   /* String passed to cbGetBoardName is to short */
	public static final int CONVERTEXTMEM = 109;   /* Convert data option not allowed with external memory */
	public static final int BADEUADD = 110;   /* e_ToEngUnits addition error */
	public static final int DAS16JRRATEWARNING = 111;   /* use 10 MHz clock for rates > 125KHz */
	public static final int DAS08TOOLOWRATE = 112;   /* DAS08 rate set too low for AInScan warning */
	public static final int AMBIGSENSORONGP = 114;   /* more than one sensor type defined for EXP-GP */
	public static final int NOSENSORTYPEONGP = 115;   /* no sensor type defined for EXP-GP */
	public static final int NOCONVERSIONNEEDED = 116;   /* 12 bit board without chan tags - converted in ISR */
	public static final int NOEXTCONTINUOUS = 117;   /* External memory cannot be used in CONTINUOUS mode */
	public static final int INVALIDPRETRIGCONVERT = 118;   /* cbAConvertPretrigData was called after failure in cbAPretrig */
	public static final int BADCTRREG = 119;   /* bad arg to CLoad for 9513 */
	public static final int BADTRIGTHRESHOLD = 120;   /* Invalid trigger threshold specified in cbSetTrigger */
	public static final int BADPCMSLOTREF = 121;   /* No PCM card in specified slot */
	public static final int AMBIGPCMSLOTREF = 122;   /* More than one MCC PCM card in slot */
	public static final int BADSENSORTYPE = 123;   /* Bad sensor type selected in Instacal */
	public static final int DELBOARDNOTEXIST = 124;   /* tried to delete board number which doesn't exist */
	public static final int NOBOARDNAMEFILE = 125;   /* board name file not found */
	public static final int CFGFILENOTFOUND = 126;   /* configuration file not found */
	public static final int NOVDDINSTALLED = 127;   /* CBUL.386 device driver not installed */
	public static final int NOWINDOWSMEMORY = 128;   /* No Windows memory available */
	public static final int OUTOFDOSMEMORY = 129;   /* ISR data struct alloc failure */
	public static final int OBSOLETEOPTION = 130;   /* Obsolete option for cbGetConfig/cbSetConfig */
	public static final int NOPCMREGKEY = 131;	  /* No registry entry for this PCMCIA board */
	public static final int NOCBUL32SYS = 132;	  /* CBUL32.SYS device driver is not loaded */
	public static final int NODMAMEMORY = 133;   /* No DMA buffer available to device driver */
	public static final int IRQNOTAVAILABLE = 134;	  /* IRQ in being used by another device */	
	public static final int NOT7266CTR = 135;   /* This board does not have an LS7266 counter */
	public static final int BADQUADRATURE = 136;   /* Invalid quadrature specified */
	public static final int BADCOUNTMODE = 137;   /* Invalid counting mode specified */
	public static final int BADENCODING = 138;   /* Invalid data encoding specified */
	public static final int BADINDEXMODE = 139;   /* Invalid index mode specified */
	public static final int BADINVERTINDEX = 140;   /* Invalid invert index specified */
	public static final int BADFLAGPINS = 141;   /* Invalid flag pins specified */
	public static final int NOCTRSTATUS = 142;	  /* This board does not support cbCStatus() */
	public static final int NOGATEALLOWED = 143;	  /* Gating and indexing not allowed simultaneously */		     
	public static final int NOINDEXALLOWED = 144;   /* Indexing not allowed in non-quadratue mode */   
	public static final int OPENCONNECTION = 145;   /* Temperature input has open connection */
	public static final int BMCONTINUOUSCOUNT = 146;   /* Count must be integer multiple of packetsize for recycle mode. */
	public static final int BADCALLBACKFUNC = 147;   /* Invalid pointer to callback function passed as arg */
	public static final int MBUSINUSE = 148;   /* MetraBus in use */
	public static final int MBUSNOCTLR = 149;   /* MetraBus I/O card has no configured controller card */
	public static final int BADEVENTTYPE = 150;   /* Invalid event type specified for this board. */
	public static final int ALREADYENABLED = 151;	  /* An event handler has already been enabled for this event type */
	public static final int BADEVENTSIZE = 152;   /* Invalid event count specified. */
	public static final int CANTINSTALLEVENT = 153;	  /* Unable to install event handler */
	public static final int BADBUFFERSIZE = 154;   /* Buffer is too small for operation */
	public static final int BADAIMODE = 155;   /* Invalid analog input mode(RSE, NRSE, or DIFF) */ 
	public static final int BADSIGNAL = 156;   /* Invalid signal type specified. */
	public static final int BADCONNECTION = 157;   /* Invalid connection specified. */
	public static final int BADINDEX = 158;   /* Invalid index specified, or reached end of internal connection list. */
	public static final int NOCONNECTION = 159;   /* No connection is assigned to specified signal. */
	public static final int BADBURSTIOCOUNT = 160;   /* Count cannot be greater than the FIFO size for BURSTIO mode. */
	public static final int DEADDEV = 161;   /* Device has stopped responding. Please check connections. */

	public static final int INVALIDACCESS = 163;    /* Invalid access or privilege for specified operation */
	public static final int UNAVAILABLE = 164;    /* Device unavailable at time of request. Please repeat operation. */
	public static final int NOTREADY = 165;   /* Device is not ready to send data. Please repeat operation. */
	public static final int BITUSEDFORALARM = 169;    /* The specified bit is used for alarm. */
	public static final int PORTUSEDFORALARM = 170;    /* One or more bits on the specified port are used for alarm. */
	public static final int PACEROVERRUN = 171;    /* Pacer overrun, external clock rate too fast. */
	public static final int BADCHANTYPE = 172;    /* Invalid channel type specified. */
	public static final int BADTRIGSENSE = 173;    /* Invalid trigger sensitivity specified. */
	public static final int BADTRIGCHAN = 174;    /* Invalid trigger channel specified. */
	public static final int BADTRIGLEVEL = 175;    /* Invalid trigger level specified. */
	public static final int NOPRETRIGMODE = 176;    /* Pre-trigger mode is not supported for the specified trigger type. */
	public static final int BADDEBOUNCETIME = 177;    /* Invalid debounce time specified. */
	public static final int BADDEBOUNCETRIGMODE = 178;    /* Invalid debounce trigger mode specified. */
	public static final int BADMAPPEDCOUNTER = 179;    /* Invalid mapped counter specified. */
	public static final int BADCOUNTERMODE = 180;    /* This function can not be used with the current mode of the specified counter. */
	public static final int BADTCCHANMODE = 181;    /* Single-Ended mode can not be used for temperature input. */
	public static final int BADFREQUENCY = 182;    /* Invalid frequency specified. */
	public static final int BADEVENTPARAM = 183;    /* Invalid event parameter specified. */
	public static final int NONETIFC = 184;		/* No interface devices were found with required PAN ID and/or RF Channel. */
	public static final int DEADNETIFC = 185;		/* The interface device(s) with required PAN ID and RF Channel has failed. Please check connection. */
	public static final int NOREMOTEACK = 186;		/* The remote device is not responding to commands and queries. Please check device. */
	public static final int INPUTTIMEOUT = 187;		/* The device acknowledged the operation, but has not completed before the timeout. */
	public static final int MISMATCHSETPOINTCOUNT = 188;		/* Number of Setpoints not equal to number of channels with setpoint flag set */
	public static final int INVALIDSETPOINTLEVEL = 189;		/* Setpoint Level is outside channel range */
	public static final int INVALIDSETPOINTOUTPUTTYPE = 190;		/* Setpoint Output Type is invalid*/
	public static final int INVALIDSETPOINTOUTPUTVALUE = 191;		/* Setpoint Output Value is outside channel range */
	public static final int INVALIDSETPOINTLIMITS = 192;		/* Setpoint Comparison limit B greater than Limit A */
	public static final int STRINGTOOLONG = 193;	/* The string entered is too long for the operation and/or device. */
	public static final int INVALIDLOGIN = 194;   /* The account name and/or password entered is incorrect. */
	public static final int SESSIONINUSE = 195;	/* The device session is already in use. */
	public static final int NOEXTPOWER = 196;	/* External power is not connected. */
	public static final int BADDUTYCYCLE = 197; /* Invalid duty cycle specified. */
	public static final int BADINITIALDELAY = 199; /* Invalid initial delay specified */
	public static final int NOTEDSSENSOR = 1000;  /* No TEDS sensor was detected on the specified channel. */
	public static final int INVALIDTEDSSENSOR = 1001;  /* Connected TEDS sensor to the specified channel is not supported */
	public static final int CALIBRATIONFAILED = 1002;  /* Calibration failed */
	public static final int BITUSEDFORTERMINALCOUNTSTATUS = 1003;   /* The specified bit is used for terminal count stauts. */
	public static final int PORTUSEDFORTERMINALCOUNTSTATUS = 1004;    /* One or more bits on the specified port are used for terminal count stauts. */
	public static final int BADEXCITATION = 1005;    /* Invalid excitation specified */
	public static final int BADBRIDGETYPE = 1006;    /* Invalid bridge type specified */
	public static final int BADLOADVAL = 1007;    /* Invalid load value specified */
	public static final int BADTICKSIZE = 1008;    /* Invalid tick size specified */


	public static final int AIFUNCTION = 1;    /* Analog Input Function    */
	public static final int AOFUNCTION = 2;    /* Analog Output Function   */
	public static final int DIFUNCTION = 3;    /* Digital Input Function   */
	public static final int DOFUNCTION = 4;    /* Digital Output Function  */
	public static final int CTRFUNCTION = 5;    /* Counter Function         */
	public static final int DAQIFUNCTION = 6;    /* Daq Input Function       */
	public static final int DAQOFUNCTION = 7;    /* Daq Output Function      */
	
	/* Calibration coefficient types */
	public static final int COARSE_GAIN = 0x01;
	public static final int COARSE_OFFSET = 0x02;
	public static final int FINE_GAIN = 0x04;
	public static final int FINE_OFFSET = 0x08;
	public static final int GAIN = COARSE_GAIN;
	public static final int OFFSET = COARSE_OFFSET;
	
	/* Internal errors returned by 16 bit library */
	public static final int INTERNALERR = 200;   /* 200-299 Internal library error  */
	public static final int CANT_LOCK_DMA_BUF = 201;   /* DMA buffer could not be locked */
	public static final int DMA_IN_USE = 202;   /* DMA already controlled by another VxD */
	public static final int BAD_MEM_HANDLE = 203;   /* Invalid Windows memory handle */
	public static final int NO_ENHANCED_MODE = 204;   /* Windows Enhance mode is not running */
	public static final int MEMBOARDPROGERROR = 211;   /* Program error getting memory board source */

	/* Internal errors returned by 32 bit library */
	public static final int INTERNAL32_ERR = 300;   /* 300-399 32 bit library internal errors */
	public static final int NO_MEMORY_FOR_BUFFER = 301;   /* 32 bit - default buffer allocation when no user buffer used with file */
	public static final int WIN95_CANNOT_SETUP_ISR_DATA = 302; /* 32 bit - failure on INIT_ISR_DATA IOCTL call */
	public static final int WIN31_CANNOT_SETUP_ISR_DATA = 303; /* 32 bit - failure on INIT_ISR_DATA IOCTL call */
	public static final int CFG_FILE_READ_FAILURE = 304;   /* 32 bit - error reading board configuration file */
	public static final int CFG_FILE_WRITE_FAILURE = 305;   /* 32 bit - error writing board configuration file */
	public static final int CREATE_BOARD_FAILURE = 306;   /* 32 bit - failed to create board */
	public static final int DEVELOPMENT_OPTION = 307;   /* 32 bit - Config Option item used in development only */
	public static final int CFGFILE_CANT_OPEN = 308;   /* 32 bit - cannot open configuration file. */
	public static final int CFGFILE_BAD_ID = 309;   /* 32 bit - incorrect file id. */
	public static final int CFGFILE_BAD_REV = 310;   /* 32 bit - incorrect file version. */
	public static final int CFGFILE_NOINSERT = 311;  /*; */
	public static final int CFGFILE_NOREPLACE = 312;  /*; */
	public static final int BIT_NOT_ZERO = 313;  /*; */
	public static final int BIT_NOT_ONE = 314;  /*; */
	public static final int BAD_CTRL_REG = 315;     /* No control register at this location. */
	public static final int BAD_OUTP_REG = 316;     /* No output register at this location. */
	public static final int BAD_RDBK_REG = 317;     /* No read back register at this location. */
	public static final int NO_CTRL_REG = 318;     /* No control register on this board. */
	public static final int NO_OUTP_REG = 319;     /* No control register on this board. */
	public static final int NO_RDBK_REG = 320;     /* No control register on this board. */
	public static final int CTRL_REG_FAIL = 321;     /* internal ctrl reg test failed. */
	public static final int OUTP_REG_FAIL = 322;     /* internal output reg test failed. */
	public static final int RDBK_REG_FAIL = 323;     /* internal read back reg test failed. */
	public static final int FUNCTION_NOT_IMPLEMENTED = 324;
	public static final int BAD_RTD_CONVERSION = 325;     /* Overflow in RTD calculation */
	public static final int NO_PCI_BIOS = 326;     /* PCI BIOS not present in the PC */
	public static final int BAD_PCI_INDEX = 327;     /* Invalid PCI board index passed to PCI BIOS */
	public static final int NO_PCI_BOARD = 328;		/* Can't detact specified PCI board */
	public static final int PCI_ASSIGN_FAILED = 329;		/* PCI resource assignment failed */
	public static final int PCI_NO_ADDRESS = 330;     /* No PCI address returned */
	public static final int PCI_NO_IRQ = 331;		/* No PCI IRQ returned */
	public static final int CANT_INIT_ISR_INFO = 332;		/* IOCTL call failed on VDD_API_INIT_ISR_INFO */
	public static final int CANT_PASS_USER_BUFFER = 333;		/* IOCTL call failed on VDD_API_PASS_USER_BUFFER */
	public static final int CANT_INSTALL_INT = 334;		/* IOCTL call failed on VDD_API_INSTALL_INT */
	public static final int CANT_UNINSTALL_INT = 335;		/* IOCTL call failed on VDD_API_UNINSTALL_INT */
	public static final int CANT_START_DMA = 336;		/* IOCTL call failed on VDD_API_START_DMA */
	public static final int CANT_GET_STATUS = 337;		/* IOCTL call failed on VDD_API_GET_STATUS */
	public static final int CANT_GET_PRINT_PORT = 338;		/* IOCTL call failed on VDD_API_GET_PRINT_PORT */
	public static final int CANT_MAP_PCM_CIS = 339;		/* IOCTL call failed on VDD_API_MAP_PCM_CIS */
	public static final int CANT_GET_PCM_CFG = 340;     /* IOCTL call failed on VDD_API_GET_PCM_CFG */
	public static final int CANT_GET_PCM_CCSR = 341;		/* IOCTL call failed on VDD_API_GET_PCM_CCSR */
	public static final int CANT_GET_PCI_INFO = 342;		/* IOCTL call failed on VDD_API_GET_PCI_INFO */
	public static final int NO_USB_BOARD = 343;		/* Can't detect specified USB board */
	public static final int NOMOREFILES = 344;		/* No more files in the directory */
	public static final int BADFILENUMBER = 345;		/* Invalid file number */
	public static final int INVALIDSTRUCTSIZE = 346;		/* Invalid structure size */
	public static final int LOSSOFDATA = 347;		/* EOF marker not found, possible loss of data */
	public static final int INVALIDBINARYFILE = 348;		/* File is not a valid MCC binary file */

	/* DOS errors are remapped by adding DOS_ERR_OFFSET to them */
	public static final int DOS_ERR_OFFSET = 500;

	/* These are the commonly occurring remapped DOS error codes */
	public static final int DOSBADFUNC = 501;
	public static final int DOSFILENOTFOUND = 502;
	public static final int DOSPATHNOTFOUND = 503;
	public static final int DOSNOHANDLES = 504;
	public static final int DOSACCESSDENIED = 505;
	public static final int DOSINVALIDHANDLE = 506;
	public static final int DOSNOMEMORY = 507;
	public static final int DOSBADDRIVE = 515;
	public static final int DOSTOOMANYFILES = 518;
	public static final int DOSWRITEPROTECT = 519;
	public static final int DOSDRIVENOTREADY = 521;
	public static final int DOSSEEKERROR = 525;
	public static final int DOSWRITEFAULT = 529;
	public static final int DOSREADFAULT = 530;
	public static final int DOSGENERALFAULT = 531;

	/* Windows internal error codes */
	public static final int WIN_CANNOT_ENABLE_INT = 603;
	public static final int WIN_CANNOT_DISABLE_INT = 605;
	public static final int WIN_CANT_PAGE_LOCK_BUFFER = 606;
	public static final int NO_PCM_CARD = 630;

	/* Maximum length of error string */
	public static final int ERRSTRLEN = 256;

	/* Maximum length of board name */
	public static final int BOARDNAMELEN = 25;

	/* Status values */
	public static final int IDLE = 0;
	public static final int RUNNING = 1;

	/* Option Flags */
	public static final int FOREGROUND = 0x0000;    /* Run in foreground, don't return till done */
	public static final int BACKGROUND = 0x0001;    /* Run in background, return immediately */

	public static final int SINGLEEXEC = 0x0000;    /* One execution */
	public static final int CONTINUOUS = 0x0002;    /* Run continuously until cbstop() called */

	public static final int TIMED = 0x0000;    /* Time conversions with internal clock */
	public static final int EXTCLOCK = 0x0004;    /* Time conversions with external clock */

	public static final int NOCONVERTDATA = 0x0000;    /* Return raw data */
	public static final int CONVERTDATA = 0x0008;    /* Return converted A/D data */

	public static final int NODTCONNECT = 0x0000;    /* Disable DT Connect */
	public static final int DTCONNECT = 0x0010;    /* Enable DT Connect */
	public static final int SCALEDATA = 0x0010;    /* Scale scan data to engineering units */

	public static final int DEFAULTIO = 0x0000;    /* Use whatever makes sense for board */
	public static final int SINGLEIO = 0x0020;    /* Interrupt per A/D conversion */
	public static final int DMAIO = 0x0040;    /* DMA transfer */
	public static final int BLOCKIO = 0x0060;    /* Interrupt per block of conversions */
	public static final int BURSTIO = 0x10000;    /* Transfer upon scan completion */
	public static final int RETRIGMODE = 0x20000;    /* Re-arm trigger upon acquiring trigger count samples */
	public static final int NONSTREAMEDIO = 0x040000;    /* Non-streamed D/A output */
	public static final int ADCCLOCKTRIG = 0x080000;    /* Output operation is triggered on ADC clock */
	public static final int ADCCLOCK = 0x100000;    /* Output operation is paced by ADC clock */
	public static final int HIGHRESRATE = 0x200000;	   /* Use high resolution rate */
	public static final int SHUNTCAL = 0x400000;	   /* Enable Shunt Calibration */

	public static final int BYTEXFER = 0x0000;    /* Digital IN/OUT a byte at a time */
	public static final int WORDXFER = 0x0100;    /* Digital IN/OUT a word at a time */

	public static final int INDIVIDUAL = 0x0000;    /* Individual D/A output */
	public static final int SIMULTANEOUS = 0x0200;    /* Simultaneous D/A output */

	public static final int FILTER = 0x0000;    /* Filter thermocouple inputs */
	public static final int NOFILTER = 0x0400;    /* Disable filtering for thermocouple */

	public static final int NORMMEMORY = 0x0000;    /* Return data to data array */
	public static final int EXTMEMORY = 0x0800;    /* Send data to memory board ia DT-Connect */

	public static final int BURSTMODE = 0x1000;    /* Enable burst mode */

	public static final int NOTODINTS = 0x2000;    /* Disbale time-of-day interrupts */

	public static final int EXTTRIGGER = 0x4000;     /* A/D is triggered externally */

	public static final int NOCALIBRATEDATA = 0x8000;    /* Return uncalibrated PCM data */
	public static final int CALIBRATEDATA = 0x0000;    /* Return calibrated PCM A/D data */

	public static final int CTR16BIT = 0x0000;	   /* Return 16-bit counter data */
	public static final int CTR32BIT = 0x0100;	   /* Return 32-bit counter data */
	public static final int CTR48BIT = 0x0200;	   /* Return 48-bit counter data */

	public static final int ENABLED = 1;
	public static final int DISABLED = 0;

	public static final int UPDATEIMMEDIATE = 0;
	public static final int UPDATEONCOMMAND = 1;
	
	/* Arguments that are used in a particular function call should be set
	   to NOTUSED */
	public static final int NOTUSED = -1;
	
	/* types of error reporting */
	public static final int DONTPRINT = 0;
	public static final int PRINTWARNINGS = 1;
	public static final int PRINTFATAL = 2;
	public static final int PRINTALL = 3;

	/* types of error handling */
	public static final int DONTSTOP = 0;
	public static final int STOPFATAL = 1;
	public static final int STOPALL = 2;
	
	/* Temperature scales */
	public static final int CELSIUS = 0;
	public static final int FAHRENHEIT = 1;
	public static final int KELVIN = 2;
	public static final int VOLTS = 4;		/* special scale for DAS-TC boards */
	public static final int NOSCALE = 5;

	/* Default option */
	public static final int DEFAULTOPTION = 0x0000;

	/* Types of configuration information */
	public static final int GLOBALINFO = 1;
	public static final int BOARDINFO = 2;
	public static final int DIGITALINFO = 3;
	public static final int COUNTERINFO = 4;
	public static final int EXPANSIONINFO = 5;
	public static final int MISCINFO = 6;
	public static final int EXPINFOARRAY = 7;
	public static final int MEMINFO = 8;

	/* Types of global configuration information */
	public static final int GIVERSION = 36;      /* Config file format version number */
	public static final int GINUMBOARDS = 38;      /* Maximum number of boards */
	public static final int GINUMEXPBOARDS = 40;      /* Maximum number of expansion boards */
	
	/* Types of board configuration information */
	public static final int BIBASEADR = 0;       /* Base Address */
	public static final int BIBOARDTYPE = 1;       /* Board Type (0x101 - 0x7FFF) */
	public static final int BIINTLEVEL = 2;       /* Interrupt level */
	public static final int BIDMACHAN = 3;       /* DMA channel */
	public static final int BIINITIALIZED = 4;       /* TRUE or FALSE */
	public static final int BICLOCK = 5;       /* Clock freq (1, 10 or bus) */
	public static final int BIRANGE = 6;       /* Switch selectable range */
	public static final int BINUMADCHANS = 7;       /* Number of A/D channels */
	public static final int BIUSESEXPS = 8;       /* Supports expansion boards TRUE/FALSE */
	public static final int BIDINUMDEVS = 9;       /* Number of digital devices */
	public static final int BIDIDEVNUM = 10;      /* Index into digital information */
	public static final int BICINUMDEVS = 11;      /* Number of counter devices */
	public static final int BICIDEVNUM = 12;      /* Index into counter information */
	public static final int BINUMDACHANS = 13;      /* Number of D/A channels */
	public static final int BIWAITSTATE = 14;      /* Wait state enabled TRUE/FALSE */
	public static final int BINUMIOPORTS = 15;      /* I/O address space used by board */
	public static final int BIPARENTBOARD = 16;      /* Board number of parent board */
	public static final int BIDTBOARD = 17;      /* Board number of connected DT board */
	public static final int BINUMEXPS = 18;      /* Number of EXP boards installed */

	/* NEW CONFIG ITEMS for 32 bit library */
	public static final int BINOITEM = 99;      /* NO-OP return no data and returns DEVELOPMENT_OPTION error code */
	public static final int BIDACSAMPLEHOLD = 100;     /* DAC sample and hold jumper state */
	public static final int BIDIOENABLE = 101;     /* DIO enable */
	public static final int BI330OPMODE = 102;     /* DAS16-330 operation mode (ENHANCED/COMPATIBLE) */
	public static final int BI9513CHIPNSRC = 103;     /* 9513 HD CTR source (DevNo = ctr no.)*/
	public static final int BICTR0SRC = 104;     /* CTR 0 source */
	public static final int BICTR1SRC = 105;     /* CTR 1 source */
	public static final int BICTR2SRC = 106;     /* CTR 2 source */
	public static final int BIPACERCTR0SRC = 107;     /* Pacer CTR 0 source */
	public static final int BIDAC0VREF = 108;     /* DAC 0 voltage reference */
	public static final int BIDAC1VREF = 109;     /* DAC 1 voltage reference */
	public static final int BIINTP2LEVEL = 110;     /* P2 interrupt for CTR10 and CTR20HD */
	public static final int BIWAITSTATEP2 = 111;     /* Wait state 2 */
	public static final int BIADPOLARITY = 112;     /* DAS1600 Polarity state(UNI/BI) */
	public static final int BITRIGEDGE = 113;     /* DAS1600 trigger edge(RISING/FALLING) */
	public static final int BIDACRANGE = 114;     /* DAC Range (DevNo is channel) */
	public static final int BIDACUPDATE = 115;     /* DAC Update (INDIVIDUAL/SIMULTANEOUS) (DevNo) */
	public static final int BIDACINSTALLED = 116;     /* DAC Installed */
	public static final int BIADCFG = 117;     /* AD Config (SE/DIFF) (DevNo) */
	public static final int BIADINPUTMODE = 118;     /* AD Input Mode (Voltage/Current) */
	public static final int BIDACPOLARITY = 119;     /* DAC Startup state (UNI/BI) */
	public static final int BITEMPMODE = 120;     /* DAS-TEMP Mode (NORMAL/CALIBRATE) */
	public static final int BITEMPREJFREQ = 121;     /* DAS-TEMP reject frequency */
	public static final int BIDISOFILTER = 122;     /* DISO48 line filter (EN/DIS) (DevNo) */
	public static final int BIINT32SRC = 123;     /* INT32 Intr Src */
	public static final int BIINT32PRIORITY = 124;     /* INT32 Intr Priority */
	public static final int BIMEMSIZE = 125;     /* MEGA-FIFO module size */
	public static final int BIMEMCOUNT = 126;     /* MEGA-FIFO # of modules */
	public static final int BIPRNPORT = 127;     /* PPIO series printer port */
	public static final int BIPRNDELAY = 128;     /* PPIO series printer port delay */
	public static final int BIPPIODIO = 129;     /* PPIO digital line I/O state */
	public static final int BICTR3SRC = 130;     /* CTR 3 source */
	public static final int BICTR4SRC = 131;     /* CTR 4 source */
	public static final int BICTR5SRC = 132;     /* CTR 5 source */
	public static final int BICTRINTSRC = 133;     /* PCM-D24/CTR3 interrupt source */
	public static final int BICTRLINKING = 134;     /* PCM-D24/CTR3 ctr linking */
	public static final int BISBX0BOARDNUM = 135;     /* SBX #0 board number */
	public static final int BISBX0ADDRESS = 136;     /* SBX #0 address */
	public static final int BISBX0DMACHAN = 137;     /* SBX #0 DMA channel */
	public static final int BISBX0INTLEVEL0 = 138;     /* SBX #0 Int Level 0 */
	public static final int BISBX0INTLEVEL1 = 139;     /* SBX #0 Int Level 1 */
	public static final int BISBX1BOARDNUM = 140;     /* SBX #0 board number */
	public static final int BISBX1ADDRESS = 141;     /* SBX #0 address */
	public static final int BISBX1DMACHAN = 142;     /* SBX #0 DMA channel */
	public static final int BISBX1INTLEVEL0 = 143;     /* SBX #0 Int Level 0 */
	public static final int BISBX1INTLEVEL1 = 144;     /* SBX #0 Int Level 1 */
	public static final int BISBXBUSWIDTH = 145;     /* SBX Bus width */
	public static final int BICALFACTOR1 = 146;     /* DAS08/Jr Cal factor */
	public static final int BICALFACTOR2 = 147;     /* DAS08/Jr Cal factor */
	public static final int BIDACTRIG = 148;     /* PCI-DAS1602 Dac trig edge */
	public static final int BICHANCFG = 149;     /* 801/802 chan config (devno =ch) */
	public static final int BIPROTOCOL = 150;     /* 422 protocol */
	public static final int BICOMADDR2 = 151;     /* dual 422 2nd address */
	public static final int BICTSRTS1 = 152;     /* dual 422 cts/rts1 */
	public static final int BICTSRTS2 = 153;     /* dual 422 cts/rts2 */
	public static final int BICTRLLINES = 154;     /* pcm com 422 ctrl lines */
	public static final int BIWAITSTATEP1 = 155;     /* Wait state P1 */
	public static final int BIINTP1LEVEL = 156;     /* P1 interrupt for CTR10 and CTR20HD */
	public static final int BICTR6SRC = 157;     /* CTR 6 source */
	public static final int BICTR7SRC = 158;     /* CTR 7 source */
	public static final int BICTR8SRC = 159;     /* CTR 8 source */
	public static final int BICTR9SRC = 160;     /* CTR 9 source */
	public static final int BICTR10SRC = 161;     /* CTR 10 source */
	public static final int BICTR11SRC = 162;     /* CTR 11 source */
	public static final int BICTR12SRC = 163;     /* CTR 12 source */
	public static final int BICTR13SRC = 164;     /* CTR 13 source */
	public static final int BICTR14SRC = 165;     /* CTR 14 source */
	public static final int BITCGLOBALAVG = 166;	 /* DASTC global average */
	public static final int BITCCJCSTATE = 167;	 /* DASTC CJC State(=ON or OFF) */
	public static final int BITCCHANRANGE = 168;	 /* DASTC Channel Gain */
	public static final int BITCCHANTYPE = 169;	 /* DASTC Channel thermocouple type */
	public static final int BITCFWVERSION = 170;	 /* DASTC Firmware Version */
	public static final int BIFWVERSION = BITCFWVERSION; /* Firmware Version */
	public static final int BIPHACFG = 180;     /* Quad PhaseA config (devNo =ch) */
	public static final int BIPHBCFG = 190;     /* Quad PhaseB config (devNo =ch) */
	public static final int BIINDEXCFG = 200;     /* Quad Index Ref config (devNo =ch) */
	public static final int BISLOTNUM = 201;     /* PCI/PCM card slot number */
	public static final int BIAIWAVETYPE = 202;     /* analog input wave type (for demo board) */
	public static final int BIPWRUPSTATE = 203;     /* DDA06 pwr up state jumper */
	public static final int BIIRQCONNECT = 204;     /* DAS08 pin6 to 24 jumper */
	public static final int BITRIGPOLARITY = 205; 	 /* PCM DAS16xx Trig Polarity */
	public static final int BICTLRNUM = 206;     /* MetraBus controller board number */
	public static final int BIPWRJMPR = 207;     /* MetraBus controller board Pwr jumper */
	public static final int BINUMTEMPCHANS = 208;     /* Number of Temperature channels */
	public static final int BIADTRIGSRC = 209;     /* Analog trigger source */
	public static final int BIBNCSRC = 210;     /* BNC source */ 
	public static final int BIBNCTHRESHOLD = 211;     /* BNC Threshold 2.5V or 0.0V */
	public static final int BIBURSTMODE = 212;     /* Board supports BURSTMODE */
	public static final int BIDITHERON = 213;     /* A/D Dithering enabled */
	public static final int BISERIALNUM = 214;    /* Serial Number for USB boards */
	public static final int BIDACUPDATEMODE = 215;    /* Update immediately or upon AOUPDATE command */
	public static final int BIDACUPDATECMD = 216;    /* Issue D/A UPDATE command */
	public static final int BIDACSTARTUP = 217;    /* Store last value written for startup */ 
	public static final int BIADTRIGCOUNT = 219;    /* Number of samples to acquire per trigger in retrigger mode */
	public static final int BIADFIFOSIZE = 220;    /* Set FIFO override size for retrigger mode */
	public static final int BIADSOURCE = 221;    /* Set source to internal reference or external connector(-1) */
	public static final int BICALOUTPUT = 222;    /* CAL output pin setting */ 
	public static final int BISRCADPACER = 223;    /* Source A/D Pacer output */
	public static final int BIMFGSERIALNUM = 224;    /* Manufacturers 8-byte serial number */
	public static final int BIPCIREVID = 225;    /* Revision Number stored in PCI header */
	public static final int BIDIALARMMASK = 230;

	public static final int BINETIOTIMEOUT = 247;

	public static final int BISYNCMODE = 251;    /* Sync mode */

	public static final int BIDIDEBOUNCESTATE = 255;    /* Digital inputs reset state */
	public static final int BIDIDEBOUNCETIME = 256;      /* Digital inputs debounce Time */

	public static final int BIPANID = 258;
	public static final int BIRFCHANNEL = 259;

	public static final int BIRSS = 261;
	public static final int BINODEID = 262;
	public static final int BIDEVNOTES = 263;

	public static final int BIADCSETTLETIME = 270;

	public static final int BIFACTORYID = 272;
	public static final int BIHTTPPORT = 273;
	public static final int BIHIDELOGINDLG = 274;
	public static final int BIDACTRIGCOUNT = 284;	/* Number of samples to generate per trigger in retrigger mode */
	public static final int BIADTIMINGMODE = 285;
	public static final int BIRTDCHANTYPE = 286;

	public static final int BIADRES = 291;
	public static final int BIDACRES = 292;


	/* Type of digital device information */
	public static final int DIBASEADR = 0;       /* Base address */
	public static final int DIINITIALIZED = 1;       /* TRUE or FALSE */
	public static final int DIDEVTYPE = 2;       /* AUXPORT or xPORTA - CH */
	public static final int DIMASK = 3;       /* Bit mask for this port */
	public static final int DIREADWRITE = 4;       /* Read required before write */
	public static final int DICONFIG = 5;      /* Current configuration */
	public static final int DINUMBITS = 6;      /* Number of bits in port */
	public static final int DICURVAL = 7;      /* Current value of outputs */
	public static final int DIINMASK = 8;      /* Input bit mask for port */
	public static final int DIOUTMASK = 9;      /* Output bit mask for port */

	/* Types of counter device information */
	public static final int CIBASEADR = 0;       /* Base address */
	public static final int CIINITIALIZED = 1;       /* TRUE or FALSE */
	public static final int CICTRTYPE = 2;       /* Counter type 8254, 9513 or 8536 */
	public static final int CICTRNUM = 3;       /* Which counter on chip */
	public static final int CICONFIGBYTE = 4;       /* Configuration byte */

	/* Types of expansion board information */
	public static final int XIBOARDTYPE = 0;       /* Board type */
	public static final int XIMUX_AD_CHAN1 = 1;       /* 0 - 7 */
	public static final int XIMUX_AD_CHAN2 = 2;       /* 0 - 7 or NOTUSED */
	public static final int XIRANGE1 = 3;       /* Range (gain) of low 16 chans */
	public static final int XIRANGE2 = 4;       /* Range (gain) of high 16 chans */
	public static final int XICJCCHAN = 5;       /* TYPE_8254_CTR or TYPE_9513_CTR */
	public static final int XITHERMTYPE = 6;       /* TYPEJ, TYPEK, TYPET, TYPEE, TYPER, or TYPES*/
	public static final int XINUMEXPCHANS = 7;       /* Number of expansion channels on board*/
	public static final int XIPARENTBOARD = 8;       /* Board number of parent A/D board*/
	public static final int XISPARE0 = 9;       /* 16 words of misc options */

	public static final int XI5VOLTSOURCE = 100;     /* ICAL DATA - 5 volt source */
	public static final int XICHANCONFIG = 101;     /* exp Data - chan config 2/4 or 3-wire devNo=chan */
	public static final int XIVSOURCE = 102;     /* ICAL DATA - voltage source*/
	public static final int XIVSELECT = 103;     /* ICAL Data - voltage select*/
	public static final int XICHGAIN = 104;     /* exp Data - individual ch gain */
	public static final int XIGND = 105;     /* ICAL DATA - exp grounding */
	public static final int XIVADCHAN = 106;     /* ICAL DATA - Vexe A/D chan */
	public static final int XIRESISTANCE = 107;     /* exp Data - resistance @0 (devNo =ch) */
	public static final int XIFACGAIN = 108;	    /* ICAL DATA - RTD factory gain */
	public static final int XICUSTOMGAIN = 109; 	/* ICAL DATA - RTD custom gain */
	public static final int XICHCUSTOM = 110;		/* ICAL DATA - RTD custom gain setting*/
	public static final int XIIEXE = 111; 	/* ICAL DATA - RTD Iexe */

	/* Types of memory board information */
	public static final int MIBASEADR = 100; 	/* mem data - base address */
	public static final int MIINTLEVEL = 101; 	/* mem data - intr level */
	public static final int MIMEMSIZE = 102;		/* MEGA-FIFO module size */
	public static final int MIMEMCOUNT = 103;		/* MEGA-FIFO # of modules */
	
	/**
	 * <b>Configuration</b>
	 * <p>
	 * Returns a configuration option for a board. The configuration 
	 * information for all boards is stored in the CB.CFG file. This 
	 * information is loaded from CB.CFG by all programs that use the 
	 * library. You can change the current configuration within a 
	 * running program with the cbSetConfig() function. The 
	 * cbGetConfig() function returns the current configuration 
	 * information.
	 * 
	 * @param infoType The configuration information for each board 
	 * is grouped into different categories. This argument specifies
	 * which category you want. Set it to one of the constants listed 
	 * in the "InfoType argument values" below.
	 * @param boardNum Refers to the board number associated with a 
	 * board when it was installed with InstaCal. BoardNum may be 0 
	 * to 99.
	 * @param devNum Selects a particular device. If InfoType = 
	 * DIGITALINFO, then DevNum specifies which of the board's digital 
	 * devices you want information on. If InfoType = COUNTERINFO, 
	 * then DevNum specifies which of the board's counter devices 
	 * you want information from.
	 * @param configItem Specifies which configuration item you wish 
	 * to retrieve. Set it in conjunction with the InfoType argument 
	 * using one of the constants listed in the "ConfigItem argument 
	 * values" below.
	 * @param configVal The specified configuration item is returned 
	 * to this variable.
	 * @return Error code or 0 if no errors. 
	 */
	public int cbGetConfig(int infoType, int boardNum, int devNum, 
			int configItem, IntByReference configVal);
	
	/**
	 * <b>Configuration</b>
	 * <p>
	 * Sets a configuration option for a board. The configuration 
	 * information for all boards is stored in the CB.CFG file. All 
	 * programs that use the library read this file. You can use 
	 * this function to override the configuration information 
	 * stored in the CB.CFG file.
	 * <p>
	 * Note: untested!
	 * @param infoType The configuration information for each board 
	 * is grouped into different categories. InfoType specifies 
	 * which category you want. Set it to one of the constants 
	 * listed in the InfoType argument values section below.
	 * @param boardNum Refers to the board number associated with a 
	 * board when it was installed. BoardNum may be 0 to 99.
	 * @param devNum Selects a particular device. If InfoType = 
	 * DIGITALINFO, then DevNum specifies which of the board's digital 
	 * devices you want to set information on. If InfoType = 
	 * COUNTERINFO then DevNum specifies which of the board's counter 
	 * devices you want to set information on.
	 * @param configItem Specifies which configuration item you wish 
	 * to set. Set it in conjunction with the InfoType argument using 
	 * the table under ConfigItem argument values section below.
	 * @param configVal The value to set the specified configuration 
	 * item to.
	 * @return Error code or 0 if no errors. 
	 */
	public int cbSetConfig(int infoType, int boardNum, int devNum, 
			int configItem, int configVal);
	
	/**
	 * <b>Temperature Input</b>
	 * <p>
	 * Reads an analog input channel, linearizes it according to
	 * the selected temperature sensor type, if required, and returns
	 * the temperature in units determined by the Scale argument. 
	 * The CJC channel, the gain, and sensor type, are read from the 
	 * InstaCal configuration file, and shoule be set by running 
	 * InstaCal
	 * 
	 * @param boardNum The number associated with the board when 
	 * it was installed with InstaCal. BoardNum may be 0 to 99.
	 * @param chan Input channel to read
	 * @param scale Specifies the temperature scale that the input
	 * will be converted to. Choises are CELSIUS, FAHRENHEIT,
	 * KELVIN, VOLTS, and NOSCALE. <p>
	 * Specify the NOSCALE option to 
	 * retrive raw data from the devices. When NOSCALE is specified, 
	 * calibrated data is returned, although a cold junction 
	 * compensation (CJC) correction factor is not applied to the 
	 * return values.<p>
	 * Specify the VOLTS option to read the voltage input of a
	 * thermocouple. 
	 * 
	 * @param tempValue The temperature in units determined by the 
	 * Scale argument is returned here
	 * @param options Bit fields that control various options. Refer
	 * to the constants in the "Options argument values" section
	 * on page 390 of the Universal Library Help pdf.
	 * @return Error code (as defined on page 688 of the Universal 
	 * Library Help pdf) or 0 if no errors.<p>
	 * Note that if an OUTOFRANGE or OPENCONNECTION error occurs, 
	 * the value returned in TempVal is -9999.0. If a NOTREADY 
	 * error occurs, the value returned in TempVal is -9000.
	 */
	public int cbTIn(int boardNum, int chan, int scale, 
			FloatByReference tempValue, int options);
	
	/**
	 * <b>Temperature Input</b>
	 * <p>
	 * Reads a range of channels from an analog input board, linearizes 
	 * them according to temperature sensor type, if required, and 
	 * returns the temperatures to an array in units determined by the 
	 * Scale argument. The CJC channel, the gain, and temperature
	 *  sensor type are read from the configuration file. Use the 
	 *  InstaCal configuration program to change any of these options.
	 * <p>
	 * Note: untested!
	 * 
	 * @param boardNum The number associated with the board when it was 
	 * installed with InstaCal. BoardNum may be 0 to 99.
	 * @param lowChan Low channel of the scan.
	 * @param highChan High channel of the scan.
	 * @param scale Specifies the temperature scale that the input will 
	 * be converted to. Choices are CELSIUS, FAHRENHEIT, KELVIN, VOLTS, 
	 * and NOSCALE.
	 * @param dataBuffer The temperature is returned in units determined 
	 * by the Scale argument. Each element in the array corresponds to a 
	 * channel in the scan. DataBuffer must be at least large enough to 
	 * hold HighChan – LowChan + 1 temperature values.
	 * @param options Bit fields that control various options. Refer to 
	 * the constants in the Options argument values section below.
	 * @return
	 */
	public int cbTInScan(int boardNum, int lowChan, int highChan, 
			int scale, float dataBuffer[], int options);
	
	/**
	 * <b>Error Handling</b>
	 * <p>
	 * Sets the error handling for all subsequent function calls. Most 
	 * functions return error codes after each call. In addition, 
	 * other error handling features are built into the library. This 
	 * function controls these features. If the Universal Library 
	 * cannot find the configuration file CB.CFG, it always terminates 
	 * the program, regardless of the cbErrHandling() setting.
	 * 
	 * @param errReporting This argument controls when the library will 
	 * print error messages on the screen. The default is DONTPRINT. 
	 * Set it to one of the constants in the "ErrReporting argument 
	 * values" section of page 366 of the Universal Library Help pdf.
	 * @param errHandling This argument specifies what class of error 
	 * will cause the program to halt. The default is DONTSTOP. Set it 
	 * to one of the constants in the "ErrHandling argument values" on
	 * page 366 of the Universal Library Help pdf. 
	 * @return Always returns 0. Thanks, Measurement Computing!
	 */
	public int cbErrHandling(int errReporting, int errHandling);
	
	/**
	 * <b>Error Handling</b>
	 * <p>
	 * Returns the error message associated with an error code. Each 
	 * function returns an error code. And error code that is not equal 
	 * to 0 indicates than an error occurred. Call this function to 
	 * convert the returned error code to a descriptive error message.
	 * 
	 * @param errCode The error code that is returned by any function 
	 * in library.
	 * @param errMsg The error message is returned here. The ErrMesg 
	 * variable must be pre-allocated to be at least as lage as ERRSTRLEN. 
	 * This size is guaranteed to be large enough to hold the longest 
	 * error message.
	 * @return Error code or 0 if no errors.
	 */
	public int cbGetErrMsg(int errCode, StringByReference errMsg);
	
	/**
	 * <b>Miscellaneous</b>
	 * <p>
	 * Returns the board name of a specified board.
	 * @param boardNum Refers either to the number associated with a 
	 * board when it was installed with InstaCal, GETFIRST, or GETNEXT. 
	 * BoardNum may be 0 to 99, or GETFIRST or GETNEXT.
	 * <p>
	 * There are two distinct ways of using this function:
	 * <p>
	 * Pass a board number as the BoardNum argument. The string that is 
	 * returned describes the board type of the installed board.
	 * <p>Set BoardNum to GETFIRST or GETNEXT to get a lits of all 
	 * board types that are supported by the library. Set BoardNum to 
	 * GETFIRST to get the first board type in the list of supported 
	 * boards. Subsequent calls with Board=GETNEXT returns each of the 
	 * other board types supported by the library. When you reach the 
	 * end of the list, BoardName is set to an empty string. Refer to 
	 * the ulgt04 example program for more details.
	 * 
	 * @param boardName A null-terminated string variable that the 
	 * board name will be returned to. This string variable must be 
	 * pre-allocated to at least as large as BOARDNAMELEN. This size 
	 * is guaranteed to be large enough to hold the longest board name 
	 * string. Refer also to the board type codes in the "Measurement 
	 * Computing Devices IDs" section on page 237 of the Universal 
	 * Library Help pdf.
	 * @return Error code or 0 if no errors.
	 */
	public int cbGetBoardName(int boardNum, StringByReference boardName);
}
