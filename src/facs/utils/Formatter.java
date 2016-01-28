/*******************************************************************************
 * QBiC Calendar provides an infrastructure for defining calendars for specific purposes like booking devices or
 * planning resources for services and integration of relevant data into the common portal infrastructure.
 * Copyright (C) 2016 Aydın Can Polatkan
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see http://www.gnu.org/licenses/.
 *******************************************************************************/
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
