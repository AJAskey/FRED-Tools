package net.ajaskey.market.tools.fred.queries;

import org.junit.jupiter.api.Test;

import net.ajaskey.market.tools.fred.ApiKey;

class ObservationsTest {

  @Test
  void test() {
    ApiKey.set();

    final Observations obs = Observations.queryObservation("GDP", 7, 8);

    System.out.println(obs);
  }

}
