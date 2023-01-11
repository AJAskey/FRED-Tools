# FRED Tools
 Interface with Tools into FRED Economic Data Online Library - [FRED](https://fred.stlouisfed.org)
 
 Taking some time to review the FRED API before diving into coding will save you a lot of time. [FRED API Docs](https://fred.stlouisfed.org/docs/api/fred)
  
 Previous iterations are in the net.ajaskey.market.tools.fred.legacy package. Viewers may find some insights into the FRED API and logic. This package has been deprecated.
 
 Users will need to obtain an API Key from FRED - [API Key](https://fred.stlouisfed.org/docs/api/api_key.html) - The key is free and is used by the FRED server to monitor activity.
 
 I store my key in a Windows environment variable named **FRED_APIKEY**. See class **ApiKey** for use.
 
---
 
## Status
 
 20Dec2022 - Software is not stable in this repository. It is good for viewing and test out on your local system. Expect issues for a while.
 I am working on it. 
 
 New work flow in progress to reduce time on the FRED server.
 
 1. Download all release and series data (not date/value pairs). This takes about 20 minutes to run due to multiple sleep periods when the FRED server stops responding. [My current method is to use six retries on queries with a seven second sleep when the response is null.]
 2. Retrieve all data needed for updating date/value pairs from local info gathered in step 1. Current processing requires at least 3 hits to FRED server. New processing will require only one in this step.
 
 Updated processing to consider that FRED only returns 1000 items at max. If there is more then another query must be made with the **offset** parameter set to original value (start with **offset**=0) plus 1000. Local logic is required to determine of subsequent call has sent new data or repeats. This is a work in progress. See *Release.queryReleases()* for current example - which will change.
 
---

## FRED Tools Classes
 The most useful classes are in the **net.ajaskey.market.tools.fred.queries** package. These are setup to query FRED. What you do with the queries is up to you. 
 
 The **net.ajaskey.market.tools.fred.executables** package contains examples of how I use the queries.
 
1. Release - Queries FRED for all the Release data available.
2. Series - Queries FRED for individual Series or all Series associated with a Release.
3. Observations - Queries FRED for date/value pairs associated with a Series.
4. Category - Queries FRED for Category data available. *(This one needs some more work.)*
 
---
 
## Executables

 These are usable now but still need testing (which is ongoing).
 
 **FredUpdateLocal** - Runnable from Eclipse IDE or from ANT build.xml file (target=FredUpdateLocal). This program will download date/value pairs for codes from input file. The program has an input file and output file(s). I use my tools to create an input file of Series Ids I want.
 
 Note that I am re-writing this to be more efficient with the query classes I have written.
 
---
  
 Series debug data:

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


File Date is determined from the saved file on your local system to compare vs Last Update. If you have the latest data then don't download it again.
