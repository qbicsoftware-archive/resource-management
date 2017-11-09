/*******************************************************************************
 * QBiC Calendar provides an infrastructure for defining calendars for specific purposes like
 * booking devices or planning resources for services and integration of relevant data into the
 * common portal infrastructure. Copyright (C) 2017 Aydın Can Polatkan & David Wojnar
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
package facs.model;

import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
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

  public int hasAdminPanelAccess() {
    return user.getAdminPanel();
  }

  public List<DeviceBean> getDevices() {
    return devices;
  }

  public List<String> getDevicesNames() {
    ArrayList<String> names = new ArrayList<String>();
    for (DeviceBean d : devices) {
      names.add(d.getName());
    }
    return names;
  }

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
    double frame = ((end.getTime() - start.getTime()) / 360000);
    double calcCost = (frame * deviceCost) / 10;
    return (frame * deviceCost) / 10;
  }

  public String userName() {
    return user.getName();
  }

  public String getLDAP() {
    return user.getLDAP();
  }

  public int getAdminAccess() {
    return user.getAdminPanel();
  }

  public String getKostenstelle() {
    return user.getKostenstelle();
  }

  public String getGroupID() {
    return user.getGroupID();
  }

  public List<CalendarEvent> getAllEvents(String device) {
    if (deviceCalendarEvents.containsKey(device)) {
      return deviceCalendarEvents.get(device);
    }
    return null;
  }

  public List<CalendarEvent> getAllEvents(String device, String userLDAP) {
    if (deviceCalendarEvents.containsKey(device)) {
      return deviceCalendarEvents.get(device);
    }
    return null;
  }

  public void setDeviceCalendarEvents(Map<String, List<CalendarEvent>> deviceCalendarEvents) {
    this.deviceCalendarEvents = deviceCalendarEvents;
  }

  public void putDeviceCalendarEvents(String device, List<CalendarEvent> deviceCalendarEvents) {
    this.deviceCalendarEvents.put(device, deviceCalendarEvents);
  }

  public String getProject() {
    if (user.getProject() == null)
      return "";
    else
      return user.getProject();
  }

  public String getInstitute() {
    return user.getInstitute();
  }

  public String getPhone() {
    return user.getPhone();
  }


}
