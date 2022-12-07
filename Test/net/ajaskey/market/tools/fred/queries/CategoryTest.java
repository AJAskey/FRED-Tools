package net.ajaskey.market.tools.fred.queries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import net.ajaskey.common.Debug;
import net.ajaskey.market.tools.fred.ApiKey;

class CategoryTest {

  @Test
  void queryCategoryTest() {
    ApiKey.set();
    Debug.init("debug/Category.dbg", java.util.logging.Level.INFO);

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
