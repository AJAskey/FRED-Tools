package net.ajaskey.market.tools.fred.queries;

import org.junit.jupiter.api.Test;

import net.ajaskey.market.tools.fred.ApiKey;

class ObservationsTest {

  @Test
  void testObs() {

    ApiKey.set();

    final Observations obs = Observations.queryObservation("TREAST", 3, 10);

    System.out.println(obs);
  }

  @Test
  void testSeriesObs() {

    ApiKey.set();

    Series ser = Series.query("GDP", 3, 10);

    System.out.println(ser);

    final Observations obs = Observations.queryObservation("GDP", 3, 10);

    System.out.println(obs);
  }

}
