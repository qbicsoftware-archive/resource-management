package facs.model;

import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.liferay.portal.model.User;
import com.vaadin.shared.ui.calendar.CalendarState.Event;
import com.vaadin.ui.components.calendar.event.CalendarEvent;

public class BookingModel implements Serializable {
  private static final long serialVersionUID = -3559766268838702415L;
 
  private UserBean user;
  private User liferayUser;
  private List<DeviceBean> devices;

  private Map<String, List<CalendarEvent>> deviceCalendarEvents;
  
  
  public BookingModel(UserBean user) {
    this.user = user;
  }

  public BookingModel(User user) {
    liferayUser = user;
  }

  public boolean isNotAllowed() {
    return user.getLDAP().isEmpty();
  }

  public List<DeviceBean> getDevices() {
    return devices;
  }
  
  public List<String> getDevicesNames() {
    ArrayList<String> names = new ArrayList<String>();
    for(DeviceBean d: devices){
      names.add(d.getName());
    }
    return names;
  }

  /**
   * get calendar events +- days of the given date 
   * 
   * @param date
   * @int days
   * @return
   */
  public List<Event> getCalEvents(Date date, int days) {
    return null;
  }

  public String userRole() {
    return user.getRole();
  }

  public void setDevices(List<DeviceBean> dbs) {
    this.devices = dbs;    
  }

  public double cost(java.util.Date start, java.util.Date end, int deviceCost) {
    double frame = ((end.getTime() - start.getTime())/360000);
    //System.out.println("Cost: "+ deviceCost + " Frame: "+ frame);
    double calcCost = (frame * deviceCost)/10;
    //System.out.println("CalcCost: " +calcCost);
    return (frame * deviceCost)/10;
  }

  public String userName() {
    return user.getName();
  }
  
  public String getLDAP() {
	  return user.getLDAP();
  }
  
  public String getKostenstelle() {
	  return user.getKostenstelle();
  }
  
  public String getGroupID() {
	  return user.getGroupID();
  }

  public List<CalendarEvent> getAllEvents(String device) {
	    if(deviceCalendarEvents.containsKey(device)){
	      return deviceCalendarEvents.get(device);
	    }
	    return null;
  }
  
  public List<CalendarEvent> getAllEvents(String device, String userLDAP) {
    if(deviceCalendarEvents.containsKey(device)){
      return deviceCalendarEvents.get(device);
    }
    return null;
  }
  
  /**
   * sets a map for devices and 
   * @param deviceCalendarEvents
   */
  public void setDeviceCalendarEvents(Map<String, List<CalendarEvent>> deviceCalendarEvents){
    this.deviceCalendarEvents = deviceCalendarEvents;
  }
  
  public void putDeviceCalendarEvents(String device, List<CalendarEvent> deviceCalendarEvents){
    this.deviceCalendarEvents.put(device, deviceCalendarEvents);
  }

  public String getProject() {
	if (user.getProject()==null)
		return "";
	else
		return user.getProject();
  }

  public String getInstitute() {
	 return user.getInstitute();
  }

  
}
