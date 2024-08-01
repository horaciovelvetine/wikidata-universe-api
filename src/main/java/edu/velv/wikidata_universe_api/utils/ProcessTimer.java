package edu.velv.wikidata_universe_api.utils;

public class ProcessTimer {
  protected long startTime;
  protected long endTime;

  public ProcessTimer() {
    this.startTime = System.currentTimeMillis();
  }

  public void stop() {
    this.endTime = System.currentTimeMillis();
  }

  private long getElapsedTime() {
    return this.endTime - this.startTime;
  }

  public String getElapsedTimeFormatted() {
    long elapsedTime = getElapsedTime();
    long seconds = elapsedTime / 1000;
    long minutes = seconds / 60;
    long hours = minutes / 60;
    String format = String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    return format + " - " + elapsedTime + "ms";
  }
}
