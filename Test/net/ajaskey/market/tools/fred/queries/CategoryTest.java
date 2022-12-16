package net.ajaskey.market.tools.fred.queries;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import net.ajaskey.common.Debug;
import net.ajaskey.market.tools.fred.ApiKey;
import net.ajaskey.market.tools.fred.LocalFormat;

class CategoryTest {

  private static Set<String> uniqCategories   = new HashSet<>();
  private static Set<String> parentCategories = new HashSet<>();

  @Test
  void queryCategoryTest() throws FileNotFoundException {

    ApiKey.set();
    Debug.init("debug/CategoryTest.dbg", java.util.logging.Level.INFO);

    List<LocalFormat> lfList = LocalFormat.readSeriesList("out/filteredSeriesSummary.txt", "FredLib");

    for (LocalFormat lf : lfList) {
      List<Category> catList = Category.queryCategoriesPerSeries(lf.getId());
      for (Category cat : catList) {
        int id = Integer.parseInt(cat.getId().trim());
        uniqCategories.add(String.format("%d\t%s", id, cat.getName().trim()));
        parentCategories.add(cat.getParentId().trim());
      }
    }

    for (String parent : parentCategories) {
      List<Category> catList = Category.queryCategoriesPerSeries(parent);
      for (Category cat : catList) {
        int id = Integer.parseInt(cat.getId().trim());
        uniqCategories.add(String.format("%d\t%s", id, cat.getName().trim()));
      }
    }

    List<String> cats = new ArrayList<>(uniqCategories);
    Collections.sort(cats);

    try (PrintWriter pw = new PrintWriter("out/categories_found.txt")) {
      pw.println("UniqCategories -->");
      for (String s : cats) {
        pw.printf("Category : %s%n", s);
      }
    }
  }

  @Test
  void queryCategoryScrumTest() {
    ApiKey.set();
    Debug.init("debug/CategoryTest.dbg", java.util.logging.Level.INFO);

    List<Category> catList = new ArrayList<>();

    final Set<Integer> uniqPids = new HashSet<>();

    for (int i = 0; i < 115; i++) {

      Category cat = Category.queryCategory(i);
      if (cat.isValid()) {
        catList.add(cat);
        try {
          int pid = Integer.parseInt(cat.getParentId());
          if (pid > 115) {
            uniqPids.add(pid);
          }
        }
        catch (Exception e) {
        }
        System.out.println(cat);
      }
    }

    List<Integer> pidList = new ArrayList<>(uniqPids);
    Collections.sort(pidList);

    for (int pid : pidList) {
      Category cat = Category.queryCategory(pid);
      if (cat.isValid()) {
        catList.add(cat);
        System.out.println(cat);
      }
    }
  }

}
