package net.ajaskey.market.tools.fred.queries;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import net.ajaskey.common.Debug;
import net.ajaskey.market.tools.fred.ApiKey;
import net.ajaskey.market.tools.fred.LocalFormat;

class CategoryTest {

  public class CatSorter implements Comparator<CategoryTest> {
    /*
     * (non-Javadoc)
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(final CategoryTest c1, final CategoryTest c2) {

      if (c1 == null || c2 == null) {
        return 0;
      }
      try {
        int ret = 0;
        if (c1.id > c2.id) {
          ret = 1;
        }
        else if (c1.id < c2.id) {
          ret = -1;
        }
        return ret;
      }
      catch (final Exception e) {
        return 0;
      }
    }
  }

  static Set<String>        uniqCategories   = new HashSet<>();
  static Set<Integer>       parentCategories = new HashSet<>();
  static List<CategoryTest> ctList           = new ArrayList<>();

  /**
   *
   * @param id
   * @param name
   */
  private static boolean addCat(Category cat) {

    final int id = Integer.parseInt(cat.getId().trim());

    final boolean isnew = CategoryTest.uniqCategories.add(String.format("%d\t%s\t%s", id, cat.getName(), cat.getParentId()));
    if (isnew) {
      final String dbg = String.format("Adding unique category: %-10d %-60s parent_id=%s", id, cat.getName(), cat.getParentId());
      Debug.LOGGER.info(dbg);
      System.out.println(dbg);
    }
    return isnew;
  }

  /**
   *
   * @param cat
   * @return
   */
  private static CategoryTest BuildCt(String cat) {

    final CategoryTest ct = new CategoryTest();

    final String fld[] = cat.split("\t");
    ct.id = Integer.parseInt(fld[0].trim());
    ct.name = fld[1].trim();
    ct.pid = Integer.parseInt(fld[2].trim());

    return ct;
  }

  int    id;
  String name;

  int pid;

  /**
   * Constructor
   */
  public CategoryTest() {
  }

  /**
   *
   * @throws FileNotFoundException
   */
  @Test
  void queryCategoryListTest() throws FileNotFoundException {

    ApiKey.set();
    Debug.init("debug/CategoryListTest.dbg", java.util.logging.Level.INFO);

    final int MAXCATS = 5000;

    final List<LocalFormat> lfList = LocalFormat.readSeriesList("out/filteredSeriesSummary.txt", "FredLib");

    int knt = 0;

    // Prime the pump with a few parents
    parentCategories.add(32265);
    parentCategories.add(32999);
    parentCategories.add(32998);
    parentCategories.add(32263);
    parentCategories.add(32991);
    parentCategories.add(3008);

    // Add Categories
    for (final LocalFormat lf : lfList) {
      final List<Category> catList = Category.queryCategoriesPerSeries(lf.getId(), 3, 10);
      for (final Category cat : catList) {
        final boolean b = CategoryTest.addCat(cat);
        final int pid = Integer.parseInt(cat.getParentId().trim());
        CategoryTest.parentCategories.add(pid);
        if (b) {
          knt++;
        }
      }
      if (knt >= MAXCATS) {
        break;
      }
    }

    knt = 0;

    System.out.println("Adding Parent Categories");
    Debug.LOGGER.info("Adding Parent Categories");

    // Add Parent Categories
    for (final int parent : CategoryTest.parentCategories) {
      final Category cat = Category.queryCategory(parent, 3, 10);
      CategoryTest.addCat(cat);
    }

    final List<String> cats = new ArrayList<>(CategoryTest.uniqCategories);
    // Collections.sort(cats);

    for (final String s : cats) {
      final CategoryTest ct = CategoryTest.BuildCt(s);
      CategoryTest.ctList.add(ct);
    }

    Collections.sort(CategoryTest.ctList, new CategoryTest.CatSorter());

    try (PrintWriter pw = new PrintWriter("out/categories_found.txt")) {
      pw.println("UniqCategories -->");
      for (final CategoryTest ct : CategoryTest.ctList) {
        pw.printf("    %-9d %-65s parent=%d%n", ct.id, ct.name, ct.pid);
      }
    }
  }

  /**
   *
   */
  @Test
  void queryOneCategoryTest() {

    ApiKey.set();
    Debug.init("debug/CategoryOneTest.dbg", java.util.logging.Level.INFO);

    final int id = 3008;

    final Category cat = Category.queryCategory(id, 3, 10);
    System.out.println(cat);

  }

}
