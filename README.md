# FRED Tools
 Interface with Tools into FRED Economic Data Online Library - [FRED](https://fred.stlouisfed.org)
 
 Main tools are in the net.ajaskey.market.tools.fred.executables package. These tools are works in-progress at this point.
 
 Previous iterations are in the net.ajaskey.market.tools.fred.legacy package. Viewers may find some insights into the FRED API and logic. This package has been deprecated.
 
 Users will need to obtain an API Key from FRED - [API Key](https://fred.stlouisfed.org/docs/api/api_key.html) - The key is free and is used by the FRED server to monitor activity.
 
 I store my key in a Windows environment variable named **FRED_APIKEY**. See class **ApiKey** for use.
 
 ---
 
 01Dec2022 - Software is not stable in this new repository. It is good for viewing and test out on your local system. Expect issues for a while.
 I am working on it.
 
 ---
 
 FRED API work flow:
 
 1. Retrieve DataSeriesInfo with API call. This data provides context into the latest update available at FRED.
 2. Retrieve a list of DataSeries and DataValues (list of dates and values) associated with the DataSeries Info.
 3. Do what you wish with the data programmatically. I write this to a file to be used by my charting software.
 
 DataSeriesInfo debug data:
 
    INFO: Retrieved DSI for Code TLCOMCON

    response =
    <?xml version="1.0" encoding="utf-8" ?>
    <seriess realtime_start="2022-11-30" realtime_end="2022-11-30">
      <series id="TLCOMCON" realtime_start="2022-11-30" realtime_end="2022-11-30" 
      title="Total Construction Spending: Commercial in the United States" 
      observation_start="2002-01-01" observation_end="2022-09-01" 
      frequency="Monthly" frequency_short="M" units="Millions of Dollars" 
      units_short="Mil. of $" seasonal_adjustment="Not Seasonally Adjusted" 
      seasonal_adjustment_short="NSA" 
      last_updated="2022-11-01 09:16:12-05" popularity="4" 
      notes="Definitions related to the construction data can be found at     
      https://www.census.gov/construction/c30/definitions.html
      Methodology details can be found at https://www.census.gov/construction/c30/methodology.html"/>
    </seriess>
    
    Name                : TLCOMCON
      Title             : Total Construction Spending: Commercial in the United States
      Frequency         : Monthly
      Units             : Millions of Dollars
      Adjustment        : NSA
      Type              : LIN
      Last Update       : 01-Nov-2022
      Last Observation  : 01-Sep-2022
      First Observation : 01-Jan-2002
      File Date         : 01-Jul-2022

File Date is determined from the saved file on your local system to compare vs Last Update. If you have the latest data then don't download it again.
