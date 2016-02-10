/*******************************************************************************
 * QBiC Calendar provides an infrastructure for defining calendars for specific purposes like booking devices or
 * planning resources for services and integration of relevant data into the common portal infrastructure.
 * Copyright (C) 2016 Aydın Can Polatkan & David Wojnar
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
package facs.components;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.swing.GroupLayout.Alignment;

import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.event.Action;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.Button;
import com.vaadin.ui.Calendar;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.components.calendar.CalendarDateRange;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.EventMoveHandler;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.EventResize;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.EventResizeHandler;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.MoveEvent;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.RangeSelectEvent;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.RangeSelectHandler;
import com.vaadin.ui.components.calendar.event.BasicEvent;
import com.vaadin.ui.components.calendar.event.CalendarEvent;
import com.vaadin.ui.components.calendar.event.EditableCalendarEvent;
import com.vaadin.ui.components.calendar.handler.BasicEventMoveHandler;
import com.vaadin.ui.components.calendar.handler.BasicEventResizeHandler;
import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.ui.renderers.NumberRenderer;
import com.vaadin.ui.themes.ValoTheme;

import facs.db.DBManager;
import facs.db.Database;
import facs.model.BookingBean;
import facs.model.BookingModel;
import facs.model.Constants;
import facs.model.FacsModelUtil;
import facs.model.UserBean;

public class Booking  extends CustomComponent{
  private static final long serialVersionUID = -4396068933947619408L;
 
  private HorizontalLayout cal = new HorizontalLayout();
  private GridLayout gridLayout = new GridLayout(6,6);
  
  
  private BookingModel bookingModel;
  private NativeSelect selectedDevice;
  private Map<String, Calendar> bookMap = new HashMap<String, Calendar>();
  private Map<String, Set<CalendarEvent>> newEvents = new HashMap<String, Set<CalendarEvent>>();
  private int eventCounter = 0;
  //private NativeSelect selectedKostenStelle;
  private Date referenceDate;
  //private NativeSelect selectedProject;
  private NativeSelect selectedService;
  private Grid myBookings;
  
  
  private static Database db;
  
  public Booking(final BookingModel bookingModel, Date referenceDate){
	  
	  String[] sayHello = { "Kon'nichiwa", "Hello", "Halo", "Hiya", "Hej", "Hallo", "Hola", "Grüezi", "Servus", "Merhaba", "Bonjour", "Ahoj", "Moi", "Ciao", "Buongiorno" };
	  
	  this.bookingModel = bookingModel;
	  this.referenceDate = referenceDate;
	  
	  Label infoLabel = new Label();
	  infoLabel.addStyleName("h3");
	  
	  Label selectDeviceLabel = new Label();
	  selectDeviceLabel.addStyleName("h4");
	  selectDeviceLabel.setValue("Please Select a Device");
	  
	  final Label versionLabel = new Label();
	  versionLabel.addStyleName("h3");
	  versionLabel.setValue("Version 0.1.160209");
	  
	  showSuccessfulNotification(sayHello[(int) (Math.random() * sayHello.length)]+", "+ bookingModel.userName()+"!","");
	  
	  Date dNow = new Date();
	  SimpleDateFormat ft = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
	  System.out.println(ft.format(dNow) + "  INFO  Calendar initiated! - User: "+ bookingModel.getLDAP() +" "+ versionLabel);
	 
	  // only users who are allowed to book devices will be able to do so
	  if (bookingModel.isNotAllowed()) {
		  VerticalLayout errorLayout = new VerticalLayout();
		  infoLabel.setValue("ACCESS DENIED");
		  errorLayout.addComponent(infoLabel);
		  showErrorNotification("Access Denied!","Sorry, you're not allowed to see anything here, at least your username told us so. Do you need assistance? Please contact 'info@qbic.uni-tuebingen.de'.");
		  setCompositionRoot(errorLayout);
		  return;
	  }
	  
	  this.setCaption("Book Instrument");
	  Panel book = new Panel("Book Instrument");
	  book.addStyleName(ValoTheme.PANEL_BORDERLESS);
	  
	  DBManager.getDatabaseInstance();  
	  db = Database.Instance;
	  db.userLogin(bookingModel.getLDAP());

	  selectedDevice = initCalendars(bookingModel.getDevicesNames());
	  
	  selectedService = new NativeSelect("Select Service");
	  selectedService.setDescription("Please select the service you would like to receive!");
	  
	  selectedDevice.addValueChangeListener(new ValueChangeListener() {
	    private static final long serialVersionUID = 8153818693511960689L;
	    
	    @Override
	    public void valueChange(ValueChangeEvent event) {
	    versionLabel.setValue(db.getUserRoleDescByLDAPId(bookingModel.getLDAP(),getCurrentDevice()));
	      if (bookMap.containsKey(getCurrentDevice())) {
	    	  cal.removeAllComponents();
	    	  setCalendar();

	    	  if(selectedDevice.getValue().equals("Aria")) {
	    		  selectedService.removeAllItems();
	    	  	  selectedService.addItems("Full Service", "Partial Service", "Self Service");
	    	      selectedService.setValue("Full Service");
	    		  selectedService.setVisible(true);
	    	  }
	    	  else if(selectedDevice.getValue().equals("Mac")) {
	    		  selectedService.removeAllItems();
	    	  	  selectedService.addItems("Self","Service");
	    	      selectedService.setValue("Service");
	    		  selectedService.setVisible(true);
	    	  }
	    	  else {
	    		  selectedService.setValue(null);
	    		  selectedService.setVisible(false);
	    	  }
	        
	      } else {
	    	  
	    	  bookMap.put(getCurrentDevice(), initCal(bookingModel,getCurrentDevice()));
	    	  cal.removeAllComponents();
	    	  setCalendar();
	        
	    	  if(selectedDevice.getValue().equals("Aria")) {
	    		  selectedService.removeAllItems();
	    	  	  selectedService.addItems("Full Service", "Partial Service", "Self Service");
	    	      selectedService.setValue("Full Service");
	    		  selectedService.setVisible(true);
	    	  } 
	    	  else if(selectedDevice.getValue().equals("Mac")) {
	    		  selectedService.removeAllItems();
	    	  	  selectedService.addItems("Self","Service");
	    	      selectedService.setValue("Service");
	    		  selectedService.setVisible(true);
	    	  }
	    	  else {
	    		  selectedService.setValue(null);
	    		  selectedService.setVisible(false);
	    	  } 
	      }	      
	    }
	  });
	  
	  if (bookingModel.getProject().isEmpty()) {
		  infoLabel.setValue(bookingModel.userName() + " · Kostenstelle: " + bookingModel.getKostenstelle() + " · Institute: "+ bookingModel.getInstitute());
		  
	  }
	  else {
		  infoLabel.setValue(bookingModel.userName() + " · Kostenstelle: " + bookingModel.getKostenstelle() + " · Project: " + bookingModel.getProject() + " · Institute: "+ bookingModel.getInstitute());
	  }
	
	  //bookDeviceLayout.addComponent(infoLabel);
	  cal.setLocale(Locale.getDefault());
	  cal.setImmediate(true);
	  selectedService.setImmediate(true);
	  cal.setSizeFull();
	
	  String submitTitle = "Book";
	  Button submit = new Button(submitTitle);
	  submit.setIcon(FontAwesome.CALENDAR);
	  submit.setDescription("Please select a device and a time frame at first then click 'BOOK'!");
	  submit.setSizeFull();
	  
	  //submit.setVisible(false);
	  
	  submit.addClickListener(new ClickListener() {
	     private static final long serialVersionUID = -3610721151565496269L;
	
	    @Override
	     public void buttonClick(ClickEvent event) {
	    	submit(bookingModel.getLDAP(),getCurrentDevice());
	      	newEvents.clear();
	     }
	  });
	  
	  	String buttonTitle = "Refresh";
	  	Button refresh = new Button(buttonTitle);
	  	refresh.setIcon(FontAwesome.REFRESH);
	  	refresh.setSizeFull();
	  	refresh.setDescription("Click here to reload the data from the database!");
	  	refresh.addStyleName(ValoTheme.BUTTON_FRIENDLY);
	  
	  	refresh.addClickListener(new ClickListener() {
			private static final long serialVersionUID = -3610721151565496269L;
			@Override
			public void buttonClick(ClickEvent event) {
				refreshDataSources();
			}
	  	});

	  gridLayout.setWidth("100%");	
	  
	  //add components to the grid layout
	  gridLayout.addComponent(infoLabel,0,0,3,0);
	  gridLayout.addComponent(versionLabel,4,0,5,0);
	  
	  gridLayout.addComponent(selectDeviceLabel,0,1);
	  gridLayout.addComponent(selectedDevice,1,1,2,1);
	  
	  gridLayout.addComponent(cal,0,2,5,2);
	  gridLayout.addComponent(refresh,0,3);
	  gridLayout.addComponent(submit,1,3,5,3);

	  gridLayout.addComponent(myBookings(),0,5,5,5);
	  
	  gridLayout.setSpacing(true);
	  gridLayout.setSizeFull();
	  
	  book.setContent(gridLayout);
	  setCompositionRoot(book);
  }
  
  private void setRenderers(Grid grid) {
	    grid.getColumn("price").setRenderer(new NumberRenderer("%1$.2f €"));

	    grid.getColumn("start").setRenderer(
	        new DateRenderer("%1$tB %1$te %1$tY, %1$tH:%1$tM:%1$tS", Locale.GERMAN));

	    grid.getColumn("end").setRenderer(
	        new DateRenderer("%1$tB %1$te %1$tY, %1$tH:%1$tM:%1$tS", Locale.GERMAN)); 
  }
  
  
  protected void setCalendar() {
	  cal.removeAllComponents();
	  cal.addComponent(bookMap.get(getCurrentDevice()));
  }
  
  private Component myBookings() {
	    VerticalLayout devicesLayout = new VerticalLayout();
	    //devicesLayout.setCaption("");
	    
	    //there will now be space around the test component
	    //components added to the test component will now not stick together but have space between them
	    devicesLayout.setMargin(true); 
	    devicesLayout.setSpacing(true); 
	    
	    BeanItemContainer<BookingBean> users = getBookings(bookingModel.getLDAP());
	    //System.out.println(bookingModel.getLDAP());
	    
	    GeneratedPropertyContainer gpc = new GeneratedPropertyContainer(users);
	    
	    myBookings = new Grid(gpc);
	    // Create a grid
	    
	    myBookings.setWidth("100%");
	    myBookings.setSelectionMode(SelectionMode.SINGLE);
	    myBookings.setEditorEnabled(true);
	    setRenderers(myBookings);
	    devicesLayout.addComponent(myBookings);
	    
	    //TODO filtering
	    //HeaderRow filterRow = devicesGrid.prependHeaderRow();
	    
	    return devicesLayout;
	}
  
  private BeanItemContainer<BookingBean> getBookings(String LDAP) {
	    BeanItemContainer<BookingBean> bookingList = new BeanItemContainer<BookingBean>(BookingBean.class);
	    List<BookingBean> bookings = DBManager.getDatabaseInstance().getMyBookingsGrid(LDAP);
	    assert bookings != null;
	    bookingList.addAll(bookings);
	    return bookingList;
}
  
  private void Notification(String title, String description, String type) {
	  Notification notify = new Notification(title,description);
	  notify.setPosition(Position.TOP_CENTER);
	  if (type.equals("error")) {
		  notify.setDelayMsec(16000);
		  notify.setIcon(FontAwesome.FROWN_O);
		  notify.setStyleName(ValoTheme.NOTIFICATION_ERROR + " " + ValoTheme.NOTIFICATION_CLOSABLE);
	  } else if (type.equals("success")) {
		  notify.setDelayMsec(8000);
		  notify.setIcon(FontAwesome.SMILE_O);
		  notify.setStyleName(ValoTheme.NOTIFICATION_SUCCESS + " " + ValoTheme.NOTIFICATION_CLOSABLE);
	  } else {
		  notify.setDelayMsec(8000);
		  notify.setIcon(FontAwesome.MEH_O);
		  notify.setStyleName(ValoTheme.NOTIFICATION_TRAY + " " + ValoTheme.NOTIFICATION_CLOSABLE);
	  }
	  notify.show(Page.getCurrent());
  }
  
  private void showErrorNotification(String title, String description) {
	  Notification notify = new Notification(title,description);
	  notify.setDelayMsec(16000);
	  notify.setPosition(Position.TOP_CENTER);
	  notify.setIcon(FontAwesome.FROWN_O);
	  notify.setStyleName(ValoTheme.NOTIFICATION_ERROR + " " + ValoTheme.NOTIFICATION_CLOSABLE);
	  notify.show(Page.getCurrent());
  }
  
  private void showNotification(String title, String description) {
	  Notification notify = new Notification(title,description);
	  notify.setDelayMsec(8000);
	  notify.setPosition(Position.TOP_CENTER);
	  notify.setIcon(FontAwesome.MEH_O);
	  notify.setStyleName(ValoTheme.NOTIFICATION_TRAY + " " + ValoTheme.NOTIFICATION_CLOSABLE);
	  notify.show(Page.getCurrent());
  }
  
  private void showSuccessfulNotification(String title, String description) {
	  Notification notify = new Notification(title,description);
	  notify.setDelayMsec(8000);
	  notify.setPosition(Position.TOP_CENTER);
	  notify.setIcon(FontAwesome.SMILE_O);
	  notify.setStyleName(ValoTheme.NOTIFICATION_SUCCESS + " " + ValoTheme.NOTIFICATION_CLOSABLE);
	  notify.show(Page.getCurrent());
  }
  
  
  public void refreshDataSources() {
	  BookingModel bookingModel = FacsModelUtil.getNoviceBookingModel();
	  setCompositionRoot(new Booking(bookingModel, referenceDate));
  }
  

  void submit(String user_ldap, String currentDevice) {
	  if (eventCounter == 0) {
		  showNotification("We couldn't find any event to add!","Did you select a time frame?\nPlease select a time frame at first then try again.");
		  return;
	  }
	  
	  
	  if(db.getDeviceRestriction(currentDevice) == true) {
		  
		  //System.out.println("I am here: True - "+db.getDeviceRestriction(currentDevice));
		  
		  if(db.getUserRoleByLDAPId(user_ldap, currentDevice).equals("V")) { 
			  showErrorNotification("Access Denied!","Sorry, you are not authorized to book. However, you can still view the calendars. Please click here to discard this message.");
		  	  return;
		  }
		  
		  Iterator<Entry<String, Set<CalendarEvent>>> it = newEvents.entrySet().iterator();

		  String title = "Booking completed!";
		  String description = "Congratulations!\nYou've succesfully added ";
		  while (it.hasNext()) {
			  Entry<String, Set<CalendarEvent>> entry = it.next();
			  description += entry.getValue().size();
			  description += " new booking(s) for device ";
			  description += entry.getKey();
			  description += ". \nPlease keep in mind that Aria, Mac and Consulting requests has to be confirmed by FACS Facility managers.";
		      for (CalendarEvent event : entry.getValue()) {
		        if (event instanceof BasicEvent) {
		          //User user;
		          try {
		            //user = UserLocalServiceUtil.getUser(Long.parseLong(VaadinService.getCurrent().getCurrentRequest().getRemoteUser()));
		        	
		            ((BasicEvent) event).setStyleName("color2"); 
		            	db.addBooking(bookingModel.getLDAP(), (String)selectedDevice.getValue(), event.getStart(), event.getEnd(), (String)selectedService.getValue(), bookingModel.cost(event.getStart(), event.getEnd(), getCost((String)selectedDevice.getValue(), (String)selectedService.getValue(), getGroupID())));
		          } catch (NumberFormatException e) {
		        	  	e.printStackTrace();
		          }

		        }
		      }
		  }
		  showSuccessfulNotification(title,description);
	  }
	  else {
		  
		  //System.out.println("I am here: False? - "+db.getDeviceRestriction(currentDevice));
		  
		  Iterator<Entry<String, Set<CalendarEvent>>> it = newEvents.entrySet().iterator();
	
		  String title = "Booking completed!";
		  String description = "Congratulations!\nYou've succesfully added ";
		  while (it.hasNext()) {
			  Entry<String, Set<CalendarEvent>> entry = it.next();
			  description += entry.getValue().size();
			  description += " new booking(s) for device ";
			  description += entry.getKey();
			  description += ". \nPlease keep in mind that Aria, Mac and Consulting requests has to be confirmed by FACS Facility managers.";
		      for (CalendarEvent event : entry.getValue()) {
		        if (event instanceof BasicEvent) {
		          //User user;
		          try {
		            //user = UserLocalServiceUtil.getUser(Long.parseLong(VaadinService.getCurrent().getCurrentRequest().getRemoteUser()));
		        	
		            ((BasicEvent) event).setStyleName("color2"); 
		            if(db.getUserRoleByLDAPId(user_ldap, currentDevice).equals("V")) { 
		            	db.addBooking(bookingModel.getLDAP(), (String)selectedDevice.getValue(), event.getStart(), event.getEnd(), (String)selectedService.getValue(), bookingModel.cost(event.getStart(), event.getEnd(), getCost((String)selectedDevice.getValue(), (String)selectedService.getValue(), getGroupID())),true);
		            }
		            else 
		            	db.addBooking(bookingModel.getLDAP(), (String)selectedDevice.getValue(), event.getStart(), event.getEnd(), (String)selectedService.getValue(), bookingModel.cost(event.getStart(), event.getEnd(), getCost((String)selectedDevice.getValue(), (String)selectedService.getValue(), getGroupID())));
		          } catch (NumberFormatException e) {
		        	  	e.printStackTrace();
		          }
	
		        }
		      }
		  }
		  
		  showSuccessfulNotification(title,description);
	  }
  }

  Calendar initCal(BookingModel bookingmodel, String currentDevice) {  
	  
	Calendar calendar;
	
	switch (db.getUserRoleByLDAPId(bookingModel.getLDAP(), currentDevice)) {
		case Constants.ADMIN_ROLE:
			calendar = adminCalendar(bookingmodel);
			break;
		case Constants.ADVANCED_ROLE:
			calendar = advancedCalendar(bookingmodel);
			break;
		case Constants.SUPER_ROLE:
			calendar = superCalendar(bookingmodel);
			break;
		case Constants.NOVICE_ROLE:
			calendar = noviceCalendar(bookingmodel);
			break;
		case Constants.BASIC_ROLE:
			default: {
				calendar = basicCalendar(bookingmodel);
			}
	}	
	
	return calendar;  
	
  }
  
  //BASIC users are allowed to see from MON-FRI from 09:00 until 16:59
  private Calendar basicCalendar(final BookingModel bookingmodel) {
	    final Calendar cal = new Calendar();
	    
	    cal.setFirstVisibleDayOfWeek(java.util.Calendar.SUNDAY);
	    cal.setLastVisibleDayOfWeek(java.util.Calendar.THURSDAY);
	    
	    cal.setFirstVisibleHourOfDay(9);
	    cal.setLastVisibleHourOfDay(17);
	    
	    for (CalendarEvent event : bookingmodel.getAllEvents(getCurrentDevice())) {
	      cal.addEvent(event);
	      //System.out.println("Booking.java 251 Current Device: " + getCurrentDevice());
	    }
	    cal.setHandler(new MyEventRangeSelectHandler(cal, bookingmodel));
	    cal.setHandler(new MyEventMoveHandler(cal, bookingmodel));
	    cal.setHandler(new MyEventResizeHandler(cal, bookingmodel));
	    cal.addActionHandler(new MyActionHandler(cal, bookingmodel));
	    
	    cal.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
	    cal.setLocale(Locale.GERMANY);
	    cal.setWidth("100%");
	    cal.setHeight("100%");

	    return cal;
  }
  
  //NOVICE users are allowed to see from MON-FRI from 09:00 to 16:59
  private Calendar noviceCalendar(final BookingModel bookingmodel) {
    final Calendar cal = new Calendar();

    cal.setFirstVisibleDayOfWeek(java.util.Calendar.SUNDAY);
    cal.setLastVisibleDayOfWeek(java.util.Calendar.THURSDAY);
    
    cal.setFirstVisibleHourOfDay(9);
    cal.setLastVisibleHourOfDay(17);
    
    for (CalendarEvent event : bookingmodel.getAllEvents(getCurrentDevice())) {
      cal.addEvent(event);
      //System.out.println("Booking.java 251 Current Device: " + getCurrentDevice());
    }
    cal.setHandler(new MyEventRangeSelectHandler(cal, bookingmodel));
    cal.setHandler(new MyEventMoveHandler(cal, bookingmodel));
    cal.setHandler(new MyEventResizeHandler(cal, bookingmodel));
    cal.addActionHandler(new MyActionHandler(cal, bookingmodel));
    
    cal.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
    cal.setLocale(Locale.GERMANY);
    cal.setWidth("100%");
    cal.setHeight("100%");
    
   

    return cal;
  }
  
  //ADVANCED users are allowed to see MON-FRI from 00:00 to 23:59
  private Calendar advancedCalendar(final BookingModel bookingmodel) {
	    final Calendar cal = new Calendar();

	    cal.setFirstVisibleDayOfWeek(java.util.Calendar.SUNDAY);
	    cal.setLastVisibleDayOfWeek(java.util.Calendar.THURSDAY);

	    for (CalendarEvent event : bookingmodel.getAllEvents(getCurrentDevice())) {
	      cal.addEvent(event);
	    }
	    cal.setHandler(new MyEventRangeSelectHandler(cal, bookingmodel));
	    cal.setHandler(new MyEventMoveHandler(cal, bookingmodel));
	    cal.setHandler(new MyEventResizeHandler(cal, bookingmodel));
	    cal.addActionHandler(new MyActionHandler(cal, bookingmodel));
	    
	    cal.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
	    cal.setLocale(Locale.GERMANY);
	    cal.setWidth("100%");
	    cal.setHeight("1000px");

	    return cal;
  }
  
  //SUPER users are allowed to see MON-SUN from 00:00 to 23:59
  private Calendar superCalendar(final BookingModel bookingmodel) {
	    final Calendar cal = new Calendar();

	    for (CalendarEvent event : bookingmodel.getAllEvents(getCurrentDevice())) {
	      cal.addEvent(event);
	    }
	    cal.setHandler(new MyEventRangeSelectHandler(cal, bookingmodel));
	    cal.setHandler(new MyEventMoveHandler(cal, bookingmodel));
	    cal.setHandler(new MyEventResizeHandler(cal, bookingmodel));
	    cal.addActionHandler(new MyActionHandler(cal, bookingmodel));
	    
	    cal.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
	    cal.setLocale(Locale.GERMANY);
	    cal.setWidth("100%");
	    cal.setHeight("1000px");

	    return cal;
  }
  
  
  //ADMIN user can see everything!
  private Calendar adminCalendar(final BookingModel bookingmodel) {
	    final Calendar cal = new Calendar();

	    for (CalendarEvent event : bookingmodel.getAllEvents(getCurrentDevice())) {
	      cal.addEvent(event);
	    }
	    cal.setHandler(new MyEventRangeSelectHandler(cal, bookingmodel));
	    cal.setHandler(new MyEventMoveHandler(cal, bookingmodel));
	    cal.setHandler(new MyEventResizeHandler(cal, bookingmodel));
	    cal.addActionHandler(new MyActionHandler(cal, bookingmodel));
	    
	    cal.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
	    cal.setLocale(Locale.GERMANY);
	    cal.setWidth("100%");
	    cal.setHeight("1000px");
	    
	    return cal;
  }

  void removeEvent(CalendarEvent event) {
	  	bookMap.get(getCurrentDevice()).removeEvent(event);
	  	//newEvents.get(getCurrentDevice()).remove(event);
	  	eventCounter--;
  }

  void addEvent(Date start, Date end) {
    try { CalendarEvent event =
	        new BasicEvent(bookingModel.userName() + " ( " + bookingModel.getKostenstelle() + " " + bookingModel.getProject() + ")",
	            "Approx. costs for this booking: €" + bookingModel.cost(start, end, getCost(getCurrentDevice(), (String)selectedService.getValue(), getGroupID()))
	                + "-", start, end);
	    ((BasicEvent) event).setStyleName("color4");
	    bookMap.get(getCurrentDevice()).addEvent(event);
	    if (!newEvents.containsKey(getCurrentDevice()) || newEvents.get(getCurrentDevice()) == null) {
	      HashSet<CalendarEvent> set = new HashSet<CalendarEvent>();
	      set.add(event);
	      newEvents.put(getCurrentDevice(), set);
	    } else {
	      newEvents.get(getCurrentDevice()).add(event);
	    }
	    eventCounter++;
    }
    catch (Exception e){
    	e.printStackTrace();
    }
  }

  protected String getCurrentDevice() {
    return (String) selectedDevice.getValue();
  }
  
  protected String getGroupID() {
	  return (String) bookingModel.getGroupID();
  }
  
  protected int getCost(String currentDevice, String currentService, String groupID) {
	  //System.out.println("getCost: " +currentDevice+" "+currentService+" "+groupID);
	  return db.getDeviceCostPerGroup(currentDevice, currentService, groupID); 
  }

  NativeSelect initCalendars(List<String> devices) {
	  String selectDeviceCaption = "";
	  String selectDeviceDescription = "Please select a device to ask for a booking request or to book!";
	  NativeSelect selectDevice = new NativeSelect();
	  selectDevice.addItems(devices);
	  selectDevice.setCaption(selectDeviceCaption);
	  selectDevice.setDescription(selectDeviceDescription);
	  selectDevice.setNullSelectionAllowed(false);
	  return selectDevice;
  }

  class MyEventHandler{
    
	final String MESSAGE_IN_THE_PAST_TITLE = "o_O we can't turn back the time!";
    final String MESSAGE_IN_THE_PAST_DESCRIPTION = "Booking failed because you selected a time frame in the past. Please select current or future dates for booking and try again!";
    final String MESSAGE_ALREADY_TAKEN_TITLE = "o_O someone else got it first!";
    final String MESSAGE_ALREADY_TAKEN_DESCRIPTION = "Booking failed because you selected an occupied time frame. Please select a new but 'free' time frame and try again!";
    final String MESSAGE_PERMISSION_DENIED_TIME_SLOT_TITLE= "Hands off, not yours.";
    final String MESSAGE_PERMISSION_DENIED_TIME_SLOT_DESCRIPTION = "Action cancelled because you tried to change someone else's booking. You can only rebook/delete your own bookings.";
    final String MESSAGE_NOTHING_TO_DELETE_TITLE = "There is no spoon.";
    final String MESSAGE_NOTHING_TO_DELETE_DESCRIPTION = "Action cancelled because you tried to delete a nonexisting booking. Do not try and bend the spoon. That's impossible.";
    final String MESSAGE_NOTHING_TO_EDIT_TITLE = "There is no spoon.";
    final String MESSAGE_NOTHING_TO_EDIT_DESCRIPTION = "Action cancelled because you tried to edit a nonexisting booking. Do not try and bend the spoon. That's impossible.";
    final String MESSAGE_OVERLAP_TITLE = "Pfoom! It's the sound of an overlap!";
    final String MESSAGE_OVERLAP_DESCRIPTION = "Unless we have a bug in the system, there is no way to overlap two bookings in the same timeframe. How did this happen now?";
   
    Calendar cal;
    BookingModel bookingModel;
        
    public MyEventHandler(Calendar cal,BookingModel bookingModel ){
      this.cal = cal;
      this.bookingModel = bookingModel;
    }
    boolean add(Date start, Date end){
      if (start.before(referenceDate)) {
    	showErrorNotification(MESSAGE_IN_THE_PAST_TITLE,MESSAGE_IN_THE_PAST_DESCRIPTION);
      } else if (cal.getEvents(start, end).size() > 0) {
    	showErrorNotification(MESSAGE_ALREADY_TAKEN_TITLE,MESSAGE_ALREADY_TAKEN_DESCRIPTION);
      } 
      else {
        addEvent(start, end);
        return true;
      }
      return false;
    }
    
    void setDate(com.vaadin.ui.components.calendar.event.EditableCalendarEvent event, Date start, Date end) {
      event.setStart(start);
      event.setEnd(end);
      event.setStyleName("color2");
      //System.out.println("Start: " + start +" End: " + end);
    }

    public void showEventPopup(CalendarEvent target) {
   // Create a sub-window and add it to the main window
      Window sub = new Window("Event");
      sub.setContent(new Label("Here's some content"));

      // Position in top-right corner
      final int width = 300;
      sub.setWidth(width + "px");
      UI.getCurrent().addWindow(sub);   
    }
    
    void showError(String message){
      Notification.show(message,Type.ERROR_MESSAGE);
    } 
  }
  
  /**
   * context menu
   */
  class MyActionHandler extends MyEventHandler implements Action.Handler{
	  private static final long serialVersionUID = -9160597832514677833L;
    
	  public MyActionHandler(Calendar cal, BookingModel bookingModel) {
		  super(cal, bookingModel);
	  }

	  Action addEventAction = new Action("Create Booking");
	  Action deleteEventAction = new Action("Delete Booking");
	  //Action editEventAction = new Action("Edit Booking");
    
	  @Override
	  public Action[] getActions(Object target, Object sender) {
		  
		  // The target should be a CalendarDateRage for the
		  // entire day from midnight to midnight.
		  if (!(target instanceof CalendarDateRange))
			  return null;
		  CalendarDateRange dateRange = (CalendarDateRange) target;

		  // The sender is the Calendar object
		  if (!(sender instanceof Calendar))
			  return null;
		  Calendar calendar = (Calendar) sender;

		  // List all the events on the requested day
		  List<CalendarEvent> events = calendar.getEvents(dateRange.getStart(), dateRange.getEnd());

		  // You can have some logic here, using the date
		  // information.
		  if (events.size() == 0)
			  return new Action[] {addEventAction};
		  else
			  return new Action[] {deleteEventAction};
			  //return new Action[] {addEventAction, deleteEventAction, editEventAction};
	  }

	  @Override
	  public void handleAction(Action action, Object sender, Object target) {
		  // The sender is the Calendar object
		  // Calendar calendar = (Calendar) sender;

		  if (action == addEventAction) {
			  // Check that the click was not done on an event
			  if (target instanceof java.util.Date) {
				  java.util.Date date = (java.util.Date) target;
				  // Add an event from now to plus one hour
				  GregorianCalendar start = new GregorianCalendar();
				  start.setTime(date);
				  GregorianCalendar end = new GregorianCalendar();
				  end.setTime(date);
				  end.add(java.util.Calendar.HOUR, 1);
				  add(start.getTime(), end.getTime());
			  } else
				  showErrorNotification(MESSAGE_ALREADY_TAKEN_TITLE,MESSAGE_ALREADY_TAKEN_DESCRIPTION);
        
		  } else if (action == deleteEventAction) {
			  // Check if the action was clicked on top of an event
			  if (target instanceof CalendarEvent) {
				  if (((CalendarEvent) target).getCaption().startsWith(bookingModel.userName())) {
					  removeEvent((CalendarEvent) target);			  
					  db.removeBooking(((CalendarEvent) target).getStart(), (String)selectedDevice.getValue());
					  //TODO: ask for confirmation
				  } else if (bookingModel.getGroupID().equals("1")) { //Admin can REMOVE events
					  removeEvent((CalendarEvent) target);
					  db.removeBooking(((CalendarEvent) target).getStart(), (String)selectedDevice.getValue());
					  //TODO: ask for confirmation
				  } else {
					  showErrorNotification(MESSAGE_PERMISSION_DENIED_TIME_SLOT_TITLE,MESSAGE_PERMISSION_DENIED_TIME_SLOT_DESCRIPTION);
				  }
			  } else
				  showErrorNotification(MESSAGE_NOTHING_TO_DELETE_TITLE,MESSAGE_NOTHING_TO_DELETE_DESCRIPTION);
		  }
	  }
  }
  
  /**
   * Add events on range selection
   */
  class MyEventRangeSelectHandler extends MyEventHandler implements RangeSelectHandler {
	  private static final long serialVersionUID = -5961040298826166829L;

	  public MyEventRangeSelectHandler(Calendar cal, BookingModel bookingModel) {
		  super(cal, bookingModel);
	  }

	  @Override
	  public void rangeSelect(RangeSelectEvent event) {
		  add(event.getStart(), event.getEnd());
	  }
  }
  
  class MyEventResizeHandler extends MyEventHandler implements EventResizeHandler{
	  private static final long serialVersionUID = -1651182125779784041L;

	  public MyEventResizeHandler(Calendar cal, BookingModel bookingModel) {
		  super(cal, bookingModel);
	  }
    
	  protected void setDates(com.vaadin.ui.components.calendar.event.EditableCalendarEvent event, Date start, Date end) {
		  
		  if (!event.getCaption().startsWith(bookingModel.userName())) {
			  showErrorNotification(MESSAGE_PERMISSION_DENIED_TIME_SLOT_TITLE,MESSAGE_PERMISSION_DENIED_TIME_SLOT_DESCRIPTION);
			  // set original dates, so that it updates immediately on client side
			  setDate(event, event.getStart(), event.getEnd());
			  return;
		  } else if (start.before(referenceDate) || event.getStart().before(referenceDate)) {
			  showErrorNotification(MESSAGE_IN_THE_PAST_TITLE,MESSAGE_IN_THE_PAST_DESCRIPTION);
			  setDate(event, event.getStart(), event.getEnd());
			  return;
		  }
		  // do only allow to resize if no other events are overwritten. == 1 because the event itself
		  // is found, when resizing
      
		  List<CalendarEvent> events = cal.getEvents(start, end);
		  // if(events.size() )
      
		  if (events.size() < 2) {
			  setDate(event, start, end);
		  }//overlap with one. append to other event
		  else if(events.size () == 2) {
			  CalendarEvent overlappingEvent = events.get(0).equals(event)?events.get(1):events.get(0);   
			  if(start.before(overlappingEvent.getEnd()) && end.after(overlappingEvent.getEnd())) {
				  setDate(event,overlappingEvent.getEnd(),end);
			  } else {
				  setDate(event,start, overlappingEvent.getStart());
			  }
		  }
		  else {
			  showError("Some other error");
			  // set original dates, so that it updates immediately on client side
			  setDate(event, event.getStart(), event.getEnd());
		  }
    }
    
    
    /**
     * based on {@link BasicEventResizeHandler.eventResize}
     */
    @Override
    public void eventResize(EventResize event) {
    	CalendarEvent calendarEvent = event.getCalendarEvent();

    	if (calendarEvent instanceof EditableCalendarEvent) {
    		Date newStartTime = event.getNewStart();
    		Date newEndTime = event.getNewEnd();
	
	        EditableCalendarEvent editableEvent = (EditableCalendarEvent) calendarEvent;

	        setDates(editableEvent, newStartTime, newEndTime);
    	}
    }
  }
  
  
  /**
   * based on {@link BasicEventMoveHandler.eventResize}
   */
  class MyEventMoveHandler extends MyEventHandler implements EventMoveHandler{
	  private static final long serialVersionUID = -1801022623601064010L;

	  public MyEventMoveHandler(Calendar cal, BookingModel bookingModel) {
		  super(cal, bookingModel);
	  }
    
	  /**
	   * handle resizing and moving of events
	   */
	  protected void setDates(com.vaadin.ui.components.calendar.event.EditableCalendarEvent event, Date start, Date end) {
		  
		  if (!bookingModel.getGroupID().equals("1")) {
			  if (!event.getCaption().startsWith(bookingModel.userName())) {
				  showErrorNotification(MESSAGE_PERMISSION_DENIED_TIME_SLOT_TITLE,MESSAGE_PERMISSION_DENIED_TIME_SLOT_DESCRIPTION);
				  // set original dates, so that it updates immediately on client side
				  setDate(event, event.getStart(), event.getEnd());
				  return;
			  } else if (start.before(referenceDate) || event.getStart().before(referenceDate)) {
				  showErrorNotification(MESSAGE_IN_THE_PAST_TITLE,MESSAGE_IN_THE_PAST_DESCRIPTION);
				  setDate(event, event.getStart(), event.getEnd());
				  return;
			  }
		  }
		  // do only allow to move if it does not overlap with any other event 
		  //System.out.println("move: "+ cal.getEvents(start, end).size());
		  List<CalendarEvent> events  = cal.getEvents(start, end);
		  if (events.size() == 0 || (events.size() == 1 && events.get(0).equals(event))) {
			  setDate(event, start, end);
			  //System.out.println("BookingID: "+ event.getCaption());
		  } else {
			  // set original dates, so that it updates immediately on client side
			  setDate(event, event.getStart(), event.getEnd());
			  showErrorNotification(MESSAGE_OVERLAP_TITLE,MESSAGE_OVERLAP_DESCRIPTION);
		  }
	  }
    
	  @Override
	  public void eventMove(MoveEvent event) {
		  CalendarEvent calendarEvent = event.getCalendarEvent();

		  if (calendarEvent instanceof EditableCalendarEvent) {
			  EditableCalendarEvent editableEvent = (EditableCalendarEvent) calendarEvent;
			  Date newFromTime = event.getNewStart();
			  // Update event dates
			  long length = editableEvent.getEnd().getTime() - editableEvent.getStart().getTime();
			  setDates(editableEvent, newFromTime, new Date(newFromTime.getTime() + length));
		  }
	  }
  }
}
