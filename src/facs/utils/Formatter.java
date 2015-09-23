package facs.utils;

import java.util.Date;

public class Formatter {
  private static float milliseconds_to_hours = 0.000000277778f;
  private static int hour_in_minutes = 60;
  /**
   * returns a string in the format hours:minutes for a number of milliseconds.
   * For example toHoursAndMinutes(endDate.getTime() - startDate.getTime());
   * Values below one minute are ignored and truncated.
   * @param milliseconds  number of milliseconds
   * @return
   */
  public static String toHoursAndMinutes(long milliseconds){
    int hours = (int) (milliseconds * milliseconds_to_hours);
    float minutes = ((float) (milliseconds * milliseconds_to_hours) - hours) * hour_in_minutes;
    return  String.format("%d:%02d hours", hours, (int) minutes);
  }
  
  public static float toHours(Date start, Date end){
    return toHours(end.getTime() - start.getTime());
  }
  
  public static float toHours(long milliseconds){
    return (milliseconds * milliseconds_to_hours);
  }
  public static float toMinutes(Date start, Date end){
    return toMinutes(end.getTime() - start.getTime());
  }
  
  public static float toMinutes(long milliseconds){
    return ((float) (milliseconds * milliseconds_to_hours) - toHours(milliseconds)) * hour_in_minutes;
  }
  
  
}
