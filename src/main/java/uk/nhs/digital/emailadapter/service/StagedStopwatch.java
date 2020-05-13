package uk.nhs.digital.emailadapter.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StagedStopwatch {

  private Instant stageStart = Instant.now();

  private StagedStopwatch() {
    log.info("Timing started at {} ", stageStart , '\n');
  }

  private String getTimeUntilNow(Instant now) {
    long seconds = stageStart.until(now, ChronoUnit.SECONDS);
    long millis = stageStart
        .minus(seconds, ChronoUnit.SECONDS)
        .until(now, ChronoUnit.MILLIS);

    return String.format("%d.%ds {} {} ", seconds, millis);
  }

  public void finishStage(String stageName) {
    Instant now = Instant.now();
    log.info(String.format("finished %s in %s\n {} {} ", stageName, getTimeUntilNow(now)));
    stageStart = now;
  }

  public static StagedStopwatch start() {
    return new StagedStopwatch();
  }
}
