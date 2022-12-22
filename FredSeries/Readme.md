FredSeries data has been pulled from FRED. 

Each file contains all the **series** for a specific FRED **release**.

The file has been written into column format so that it is human readable.

```
Id               = Column 0,29
Title            = Column 30,151
Units            = Column 151,152 - Values: ' ',T,M,B (thousands, millions, billions)
Seasonality      = Column 153,157 - Values: SA,NSA,SAAR
LastUpdate       = Column 158,169 - Last time series data was updated at FRED
Last Observation = Column 170,181 - Date of last data point in localfile (if it exists)
LocalUpdate      = Column 182,193 - Date of local file (if it exists)
Frequency        = Column 194,213 - How often updated at FRED
Release          = Column 214,217 - FRED Release Id
Release Name     = Column 218,    - FRED Release Name
```

---

Class **LocalFormat** has procedures to read and write to this format.
1. **LocalFormat.parseline(String s)**
2. **LocalFormat.formatline()**
