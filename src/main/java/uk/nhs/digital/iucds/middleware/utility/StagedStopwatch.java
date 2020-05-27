package uk.nhs.digital.iucds.middleware.utility;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StagedStopwatch {

  private Instant stageStart = Instant.now();

  private StagedStopwatch() {
    log.info("Timing started at {} ", stageStart);
  }

  public void finishStage(String stageName) {
    Instant now = Instant.now();
    long seconds = stageStart.until(now, ChronoUnit.SECONDS);
    long millis = stageStart.minus(seconds, ChronoUnit.SECONDS).until(now, ChronoUnit.MILLIS);
    log.info("finished {} in {}.{}s ", stageName, seconds, millis);
    stageStart = now;
  }

  public static StagedStopwatch start() {
    return new StagedStopwatch();
  }
}