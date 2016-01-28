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
package facs.model;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.components.calendar.event.BasicEvent;
import com.vaadin.ui.components.calendar.event.CalendarEvent;

import de.uni_tuebingen.qbic.main.LiferayAndVaadinUtils;
import facs.db.DBManager;
import facs.db.Database;


public class FacsModelUtil {
	
  private static Database db;

  public static BookingModel getNoviceBookingModel() {

	DBManager.getDatabaseInstance();
	db = Database.Instance;

    ArrayList<String> deviceNames = new ArrayList<String>();

    deviceNames = db.getDeviceNames();
    UserBean user =  db.getUserByLDAPId(LiferayAndVaadinUtils.getUser().getScreenName());
    BookingModel bookingModel = new BookingModel(user);
    
    ArrayList<DeviceBean> dbs = new ArrayList<DeviceBean>(5);
    dbs = (ArrayList<DeviceBean>) db.getDevices();

    Map<String, List<CalendarEvent>> events = new HashMap<String, List<CalendarEvent>>();
    
    ArrayList<CalendarEvent> allBookings = new ArrayList<CalendarEvent>();
    
    Iterator <String> iterator = deviceNames.iterator();
    int iterator_index = 0;
    while(iterator.hasNext()) {   	
    	allBookings = db.getAllBookings((String)bookingModel.getLDAP(), deviceNames.get(iterator_index));
    	events.put(iterator.next(), allBookings);
    	iterator_index++;   	
    }
    
    bookingModel.setDeviceCalendarEvents(events);
    bookingModel.setDevices(dbs);
    return bookingModel;
    
  }
  
}
