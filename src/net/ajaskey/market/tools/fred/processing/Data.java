package net.ajaskey.market.tools.fred.processing;

import java.util.ArrayList;
import java.util.List;

import net.ajaskey.common.DateTime;
import net.ajaskey.common.TextUtils;
import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.DataValue;

public class Data {

  private List<DataValue> original;
  private String          fredlib;
  private String          seridId;

  public Data(String id, String fredlib) {
    this.seridId = id;
    this.fredlib = fredlib;
    this.original = new ArrayList<>();
  }

  public static void main(String[] args) {

    String fredlib = "D:/data2/MA/CSV Data/FRED-Download";

    Data obs = new Data("GDP", fredlib);
    obs.setOriginal();
    System.out.println(obs);

  }

  public int getDaysPerPeriod(DateTime dt1, DateTime dt2) {
    int ret = 0;

    return ret;
  }

  public List<DataValue> createPerTimeSequent(int days) {
    List<DataValue> ret = new ArrayList<>();

    return ret;
  }

  @Override
  public String toString() {
    String ret = String.format("SeriesId=%s   FredLib=%s%n", this.seridId, this.fredlib);
    for (DataValue dv : this.original) {
      ret += dv + Utils.NL;
    }
    return ret;
  }

  public List<DataValue> getOriginal() {
    return original;
  }

  public void setOriginal() {

    String fname = String.format("%s/%s.csv", this.fredlib, this.seridId);

    List<String> data = TextUtils.readTextFile(fname, true);

    for (String s : data) {
      String fld[] = s.trim().split(",");
      DataValue dv = new DataValue(fld[0].trim(), fld[1].trim());
      if (dv.isValid()) {
        original.add(dv);
      }
    }

  }

  public String getFredlib() {
    return fredlib;
  }

  public String getSeridId() {
    return seridId;
  }

}
