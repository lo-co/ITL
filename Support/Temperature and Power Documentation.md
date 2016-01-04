# ITL Temperaturand Power Server

The ITL Temperature and Power Server is a data acquisition program that provides temperature and power measurements for broadcast across a network connection (including ``localhost``).  The server is configurable and provides the user with the opportunity for running in several different configurations.  The server can obtain temperature data either from an NI USB TC-01 (single channel) or an [OMEGA HH802U](http://www.omega.com/pptst/HH802_803.html) (dual channel) and power data from the [Yokogawa WT330](http://tmi.yokogawa.com/us/products/digital-power-analyzers/digital-power-analyzers/digital-power-meter-wt300/) or the [Xitron 2503AH](http://www.xitrontech.com/assets/002/5820.pdf).  

## Configuration

The system is configured via the INI file provided.  The INI file may be found in the executable directory ([PROGRAM FILES]\ITL) under a folder called *support*.  The file is called **itl.ini**.  A sample INI file is given below.

```ini
[temperature]
; This can be HH802U or TC01
device = HH802U
; The device name is identified in MAX, the channel name is generally ai0...
tc01 = Dev1/ai0
; possible value are: J (10072), K(10073), N(10077), R(10082), S(10085), T(10086), B(10047), E(10055)
type = 10073
max = 100
min = 0
; Port for Omega communication
port = COM5
ID = 0

[power meter]
; Type is the type of power meter.  Currently, two are defined: Xitron and Yokogawa
type = Xitron
; gpib = GPIB0::1::INSTR
gpib = 2
; Acquisition rate in seconds
rate = 0.250

[general]
; Time between wait iterations in ms
texe = 10
; Loop clock time in seconds
twait = 1

```

The file contains three sections: ``general`` which provides some general settings for system timings, ``power meter`` which allows the user to configure the operation of the power meter and ``temperature`` which provides access to the functionality of the temperature meters.  These sections are described below.  Sections and keys appearing in the file not described here are not used and have no impact on the system.

### General

This section provides access to timing functionality.  The program runs an event loop that monitors the front panel settings while also polling the system at the appropriate times.  The system is designed to operate at 1 Hz in general but this maybe changed via the key in the configuration file ``twait``; this is a time in seconds between data acquisition cylces.

The ``texe`` is a period that the system is put to sleep to allow other processes to proceed.  This time is in milliseconds.  The default 10 ms is generally sufficient for the needs of the processor.  Setting this value to low will cause excessive CPU usage while setting it too high will prevent the system from returning data at regular intervals.

### Power Meter

This section provides access to the settings for the individual power meters.  The first key is ``type`` and this describes which device will be used.  This value **must be** either Xitron or Yokogawa.  The spelling and capitalization are enforced so **if these values are not as shown in the configuration file, then the system will not work properly!**

The key ``gpib`` is defined as above.  For the Xitron meter, the user simply has to supply the address (in this case the address is 2).  In the case of the Yokogawa meter, the user must define the full device name as defined in the commented section (GPIB0::1::INSTR); in this case, the address of this device is 1.  The address for both devices may be obtained and modified via the respective device front panels.

**The key rate is not currently used by the system.**

### Temperature

The temperature section provides the keys for configuring the operation of the two different possible temperature meters.  Similar to the power meter section, the user may specify which type of device the system will use.  In this case, the key to be used is ``device`` and the user may supply one of the two strings: HH802U or TC01.  The former defines the OMEGA dual channel meter while the latter defines the NI USB TC-01 functionality.  As with the power meter section, the device name must be spelled and capitalized as in the example above.

If the user is acquiring temperature data via the NI device, the user will have to appropriately fill out the keys pertaining to this device.  These are:

* ``tc01`` - this will tell the system the device name (in the above example Dev1) and the channel (ai0 in the above example; this should always be the correct channel).  The device information may be obtained via the Measurement and Automation Explorer (MAX) which should be provided with the installation files.
* ``type`` - defines the type of thermocouple used.  The value is expected to be an integer as defined in the above file.
* ``max`` - maximum measurable temperature expected in degrees Celsius.
* ``min`` - minimum expected temperature in degrees Celsius.

It is important to note that the system will not measure beyond the ``max`` and ``min`` values defined in the file, so ensure that these are in line with expected measured values.

The OMEGA HH802U device only has two entries currently.  The key ``port`` defines the serial COM port that the device is measuring on while the key ``ID`` defines the device ID.  The value of the port may be ascertained either via MAX or the Windows Device Manager.  The ID is 0 by default.

## Data Acquisition

The system acquires data at a rate defined within the configuration file (default of 1 Hz).  The data returned from the power meters is the same, but the Yokogawa will return only two channels of data while the Xitron is capable of returning 3.  The data returned by the power meters is 

* Voltage (V)
* Amperage (A)
* Power (W)
* Power Factor (ratio)
* Total Voltage Harmonic Distortion (%)
* Total Amperage Harmonic Distortion (%)

Each temperature device simply returns temperature.  The TC-01 returns only one channel while the HH802U returns two channels.

All data (including time) is displayed via a series of graphs on the front panel of the server program.  The data that is displayed may be changed via changing the ``Display Variable`` setting on the front panel.  In addition to the output, the user may also change the update rates for the system via the front panel also.

Currently, a button containing two states (Actual and Simulated) indicates the state of the data.  This button has no affect on the system but simply indicates whether there is good data or not.  This button should be removed in future iterations as there is no simulated data to be displayed (to avoid confusion).

### An Important Note about the HH802U Temperature Acquisition

The HH802U seems to regularly not respond to commands.  This is notable by the return of NaNs as well as 0s.

### Updating Firmware on the TC-01

The data acquisition system for the server was built using LabVIEW 2015.  The USB TC-01 appears to be a legacy device and in the initial state was not compatible with this version of LabVIEW.  As such, the firmware was updated as part of the installation process.  An article entitled [**How Can I Update My NI USB TC01 Firmware to the Latest Version**](http://digital.ni.com/public.nsf/allkb/E6EC96A7B4ABD175862578730075BF22) provided the resources and methodology for updating this software.

## Networking

The data acquisition system utilizes LabVIEW webservices to broadcast data to the network.  The web service provided is called ``daq``  and the resource is called ``Data``.  To access the web service, the user will point the browser to ``http://[ip]:[port]/daq/``.  In the executable, the port for the service is set to 13986.  This value is hard coded and a new install must be built if the user desires to change the port.

The service will return data as follows for the Yokogawa meter with the NI USB TC-01:

```xml
<StabilizationMeasurement>
   <time>0</time>
   <formattedTime>5:00 PM</formattedTime>
   <powerReading channel="0">
      <voltage units="Volts">0.000000</voltage>
      <amperage units="Amps">0.000000</amperage>
      <wattage units="Watts">0.000000</wattage>
      <powerFactor>0.000000</powerFactor>
      <voltageDistortion>0.000000</voltageDistortion>
      <amperageDistortion>0.000000</amperageDistortion>
   </powerReading>
   <powerReading channel="1">
      <voltage units="Volts">0.000000</voltage>
      <amperage units="Amps">0.000000</amperage>
      <wattage units="Watts">0.000000</wattage>
      <powerFactor>0.000000</powerFactor>
      <voltageDistortion>0.000000</voltageDistortion>
      <amperageDistortion>0.000000</amperageDistortion>
   </powerReading>
   <temperatureReading channel="0" units="degC">0.000000</temperatureReading>
</StabilizationMeasurement>

```

If the Xitron meter is used, the number of channels of power data returned will be 3.  If the HH802U is used, then 
the system will return two channels of temperature data.


