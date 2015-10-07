package facs.model;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.liferay.portal.kernel.exception.SystemException;
import com.vaadin.ui.components.calendar.event.BasicEvent;
import com.vaadin.ui.components.calendar.event.CalendarEvent;


public class FacsModelUtil {

  public static BookingModel getNoviceBookingModel(String remoteUser) {
    ArrayList<String> kostenstelle =new ArrayList<String>();
    ArrayList<String> projectName = new ArrayList<String>();
    kostenstelle.add("Kostenstelle A");
    kostenstelle.add("Kostenstelle B");
    projectName.add("Pro1");
    projectName.add("Pro2");
    UserBean user = new UserBean(0, "David Novice", Constants.NOVICE_ROLE, "active", kostenstelle, projectName);
    
    BookingModel bm = new BookingModel(user);
    DeviceBean db1 = new DeviceBean(0, "Device 1","desc1", false);
    DeviceBean db2 = new DeviceBean(1, "Device 2","desc2", true);
    ArrayList<DeviceBean> dbs = new ArrayList<DeviceBean>(5);
    
    Map<String, List<CalendarEvent>> events = new HashMap<String, List<CalendarEvent>>();
    ArrayList<CalendarEvent> c = new ArrayList<CalendarEvent>();
    java.util.Date date = new java.util.Date();
    GregorianCalendar start = new GregorianCalendar();
    start.setTime(date);
    GregorianCalendar end = new GregorianCalendar();
    end.setTime(date);
    end.add(java.util.Calendar.HOUR, 1);
 
    BasicEvent reserved = new BasicEvent("occupied" , "This time frame is already occupied.", start.getTime(), end.getTime());
    reserved.setStyleName("color1");
    c.add(reserved);
    
    date = new java.util.Date();
    start = new GregorianCalendar();
    start.setTime(date);
    start.add(java.util.Calendar.HOUR, 24);
    end = new GregorianCalendar();
    end.setTime(date);
    end.add(java.util.Calendar.HOUR, 25);
    
    BasicEvent canbedeleted = new BasicEvent("David Novice (QBIC) - Untersuchung" , "costs 24 Euro.", start.getTime(), end.getTime());
    canbedeleted.setStyleName("color2");
    c.add(canbedeleted);
 
    
    date = new java.util.Date();
    date.setHours(8);
    start = new GregorianCalendar();
    start.setTime(date);
    end = new GregorianCalendar();
    date.setHours(10);
    end.setTime(date);
    System.out.println(start + " " + end );
    BasicEvent canNotbedeleted = new BasicEvent("David Novice (QBIC) - Do we need/want a description?" , "costs 25 Euro.", start.getTime(), end.getTime());
    canbedeleted.setStyleName("color5");
    c.add(canNotbedeleted);
    
    
    events.put("Device 1",c );
    events.put("Device 2", new ArrayList<CalendarEvent>());
    events.put("Device 3", new ArrayList<CalendarEvent>());
    events.put("Device 4", new ArrayList<CalendarEvent>());
    events.put("Device 5", new ArrayList<CalendarEvent>());
    bm.setDeviceCalendarEvents(events);
    
    dbs.add(db1);dbs.add(db2);
    bm.setDevices(dbs);
    
    
    
    
    
    
    return bm;
  }
  
}
