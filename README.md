# ITL

This is the location of the repository for work with ITL.  The system being developed is a data acquisition system for retrieving infromation from a power meter and a thermocouple.  The data will be returned across a network via an http call to the web service VI ``Data`` located in the folder ``Web Service``.  The response looks like:

```xml
<StabilizationMeasurement>
   <time>3525747427</time>
   <formattedTime>00:17:06</formattedTime>
  <powerReading channel="0">
      <voltage units="Volts">122.035675</voltage>
      <amperage units="Amps">1.531968</amperage>
      <wattage units="Watts">186.954755</wattage>
      <powerFactor>0.950000</powerFactor>
      <voltageDistortion>0.000000</voltageDistortion>
      <amperageDistortion>0.000000</amperageDistortion>
   </powerReading>
   <temperatureReading channel="0" units="degC">21.506801</temperatureReading>
   <temperatureReading>Thermocouple is disconnected</temperatureReading>
</StabilizationMeasurement>
```

The format is based on a file provided by ITL.  Floating point numbers are provided to 6 decimal places and time is currently in seconds since January 1 1904 (LabVIEW time).
