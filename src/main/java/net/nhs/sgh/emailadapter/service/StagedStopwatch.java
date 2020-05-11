package net.nhs.sgh.emailadapter.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StagedStopwatch {

  private static final Logger logger = LoggerFactory.getLogger(StagedStopwatch.class);
  private Instant stageStart = Instant.now();

  private StagedStopwatch() {
    logger.info("Timing started at " + stageStart + '\n');
  }

  private String getTimeUntilNow(Instant now) {
    long seconds = stageStart.until(now, ChronoUnit.SECONDS);
    long millis = stageStart
        .minus(seconds, ChronoUnit.SECONDS)
        .until(now, ChronoUnit.MILLIS);

    return String.format("%d.%ds", seconds, millis);
  }

  public void finishStage(String stageName) {
    Instant now = Instant.now();
    logger.info(String.format("finished %s in %s\n", stageName, getTimeUntilNow(now)));
    stageStart = now;
  }

  public static StagedStopwatch start() {
    return new StagedStopwatch();
  }
}
