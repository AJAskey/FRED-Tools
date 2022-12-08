package net.ajaskey.market.tools.fred.queries;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.ajaskey.common.Debug;
import net.ajaskey.market.tools.fred.ApiKey;

class ReleaseTest {

  @Test
  void queryReleasesTest() {

    Debug.init("debug/testQueryReleases.dbg", java.util.logging.Level.INFO);

    ApiKey.set();

    List<Release> relList = Release.queryReleases();

    for (Release rel : relList) {
      Debug.LOGGER.info(rel.toString());
    }
    System.out.println(relList.get(0).getUrl());
    System.out.println(relList.get(0).getResponse());
  }

}
