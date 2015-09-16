package facs.utils;

public class Formatter {

  /**
   * returns a string in the format hours:minutes for a number of milliseconds.
   * For example toHoursAndMinutes(endDate.getTime() - startDate.getTime());
   * Values below one minute are ignored and truncated.
   * @param milliseconds  number of milliseconds
   * @return
   */
  public static String toHoursAndMinutes(long milliseconds){
    int hours = (int) (milliseconds * 0.000000277778f);
    float minutes = ((float) (milliseconds * 0.000000277778f) - hours) * 60;
    return  String.format("%d:%02d hours", hours, (int) minutes);
  }
}
