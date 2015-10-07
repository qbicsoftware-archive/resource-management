package facs.components;

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

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Calendar;
import com.vaadin.ui.CustomComponent;
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
import com.vaadin.ui.themes.ValoTheme;

import facs.model.BookingModel;
import facs.model.Constants;




public class Booking  extends CustomComponent{
  private static final long serialVersionUID = -4396068933947619408L;
 
  private HorizontalLayout cal = new HorizontalLayout();
  private BookingModel bookingModel;
  private NativeSelect selectedDevice;
  private Map<String, Calendar> bookMap = new HashMap<String, Calendar>();
  private Map<String, Set<CalendarEvent>> newEvents = new HashMap<String, Set<CalendarEvent>>();
  private int eventCounter = 0;
  private NativeSelect selectedKostenStelle;
  private Date referenceDate;
  private NativeSelect selectedProject;
  
  
  public Booking(final BookingModel bookingModel, Date referenceDate ){
    this.bookingModel = bookingModel;
    this.referenceDate = referenceDate;
  // only users who are allowed to book devices will be able to do so
  if (!bookingModel.isAllowedToBook()) {
    showErrorView();
    return;
  }
  this.setCaption("Book Instrument");
  Panel book = new Panel("Book Instrument");
  book.addStyleName(ValoTheme.PANEL_WELL);
  VerticalLayout bookInstrument = new VerticalLayout();

  selectedDevice = initDevices(bookingModel.getDevicesNames());
  selectedDevice.addValueChangeListener(new ValueChangeListener() {
    private static final long serialVersionUID = 8153818693511960689L;

    @Override
    public void valueChange(ValueChangeEvent event) {
      if (bookMap.containsKey(getCurrentDevice())) {
        setCalendar();
      } else {
        bookMap.put(getCurrentDevice(), initCal(bookingModel));
        setCalendar();
      }
    }
  });

  selectedKostenStelle = initKostenstellen(bookingModel.getKostenStellen());
  selectedProject = initProjects(bookingModel.getProjects());

  cal.setLocale(Locale.getDefault());
  cal.setImmediate(true);
  cal.setSizeFull();


  Button submit = new Button("Book");
  submit.setDescription("Select a Device and a time frame to be able to book.");
  submit.addClickListener(new ClickListener() {
    private static final long serialVersionUID = -3610721151565496269L;

    @Override
    public void buttonClick(ClickEvent event) {
      submit();
    }
  });

  HorizontalLayout select = new HorizontalLayout();
  select.addComponent(selectedDevice);
  select.addComponent(selectedKostenStelle);
  select.addComponent(selectedProject);
  bookInstrument.addComponent(select);
  bookInstrument.addComponent(cal);
  bookInstrument.addComponent(submit);
  book.setContent(bookInstrument);
  setCompositionRoot(book);
  }



protected void setCalendar() {
    cal.removeAllComponents();
    cal.addComponent(bookMap.get(getCurrentDevice()));

  }

  private void showErrorView() {
    String message =
        "<h2>Your are not allowed to book a facs machine. Please contact the facs facility manager.</h2>";
    Label error = new Label(message, ContentMode.HTML);
    error.addStyleName(ValoTheme.LABEL_FAILURE);

    setCompositionRoot(error);

  }

  void submit() {
    if (eventCounter == 0) {
      Notification.show("No event added.");
      return;
    }
    if (selectedKostenStelle.getValue() == null) {
      Notification.show("Kostenstelle not selected.");
      return;
    }


    Iterator<Entry<String, Set<CalendarEvent>>> it = newEvents.entrySet().iterator();
    String message = "You have ";
    while (it.hasNext()) {
      Entry<String, Set<CalendarEvent>> entry = it.next();
      message += entry.getValue().size();
      message += " new events for device ";
      message += entry.getKey();
      message += ". ";
      for (CalendarEvent event : entry.getValue()) {
        if (event instanceof BasicEvent) {
          User user;
          try {
            user = UserLocalServiceUtil.getUser(Long.parseLong(VaadinService.getCurrent().getCurrentRequest().getRemoteUser()));
            ((BasicEvent) event).setStyleName("color2");
          } catch (NumberFormatException | PortalException | SystemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

        }
      }
    }
    Notification.show(message, Type.HUMANIZED_MESSAGE);
  }

  Calendar initCal(BookingModel bookingmodel) {

    Calendar calendar;
    switch (bookingmodel.userRole()) {

      case Constants.ADVANCED_ROLE:
        calendar = advancedCalendar();
        break;
      case Constants.SUPER_ROLE:
        calendar = superCalendar();
        break;
      case Constants.NOVICE_ROLE:
      default:
        calendar = noviceCalendar(bookingmodel);
    }
    return calendar;
  }

  private Calendar noviceCalendar(final BookingModel bookingmodel) {
    final Calendar cal = new Calendar();

    cal.setFirstVisibleDayOfWeek(java.util.Calendar.MONDAY);
    cal.setLastVisibleDayOfWeek(java.util.Calendar.FRIDAY);
    cal.setFirstVisibleHourOfDay(8);
    cal.setLastVisibleHourOfDay(17);
    for (CalendarEvent event : bookingmodel.getAllEvents(getCurrentDevice())) {
      cal.addEvent(event);
    }
    cal.setHandler(new MyEventRangeSelectHandler(cal, bookingmodel));
    cal.setHandler(new MyEventMoveHandler(cal, bookingmodel));
    cal.setHandler(new MyEventResizeHandler(cal, bookingmodel));
    cal.addActionHandler(new MyActionHandler(cal, bookingmodel));
    /*// Add events on range selection
    cal.setHandler(new RangeSelectHandler() {
      @Override
      public void rangeSelect(RangeSelectEvent event) {
        if (event.getStart().before(referenceDate)) {
          Notification.show("You are trying to use a device in the past", Type.ERROR_MESSAGE);
        } else if (cal.getEvents(event.getStart(), event.getEnd()).size() > 0) {
          Notification.show("You are trying to select an already occupied time frame",
              Type.ERROR_MESSAGE);
        } else {
          if (selectedKostenStelle.getValue() == null) {
            Notification.show("Kostenstelle not selected.");
          } else {
            addEvent(event.getStart(), event.getEnd());
          }
        }
      }
    });

    // Handle the context menu selection
    Action.Handler actionHandler = new Action.Handler() {
      Action addEventAction = new Action("Add Event");
      Action deleteEventAction = new Action("Delete Event");

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
          return new Action[] {addEventAction, deleteEventAction};
      }

      @Override
      public void handleAction(Action action, Object sender, Object target) {
        // The sender is the Calendar object
        // Calendar calendar = (Calendar) sender;

        if (action == addEventAction) {
          // Check that the click was not done on an event
          if (target instanceof java.util.Date) {
            if (selectedKostenStelle.getValue() == null) {
              Notification.show("Kostenstelle not selected.");
            } else {
              java.util.Date date = (java.util.Date) target;
              // Add an event from now to plus one hour
              GregorianCalendar start = new GregorianCalendar();
              start.setTime(date);
              GregorianCalendar end = new GregorianCalendar();
              end.setTime(date);
              end.add(java.util.Calendar.HOUR, 1);

              addEvent(start.getTime(), end.getTime());
            }

          } else
            Notification.show("Can't add on an event");
        } else if (action == deleteEventAction) {
          // Check if the action was clicked on top of an event
          if (target instanceof CalendarEvent) {
            if (((CalendarEvent) target).getCaption().startsWith(bookingmodel.userName())) {
              removeEvent((CalendarEvent) target);
            } else {
              Notification.show("You can only delete your own time slots.", Type.ERROR_MESSAGE);
            }

          } else
            Notification.show("No event to delete");
        }
      }
    };
    cal.addActionHandler(actionHandler);

    // dragging events
    cal.setHandler(new BasicEventMoveHandler() {
      @Override
      public void setDates(com.vaadin.ui.components.calendar.event.EditableCalendarEvent event,
          Date start, Date end) {
        if (!event.getCaption().startsWith(bookingmodel.userName())) {
          Notification.show("You can only change your own, time slots.", Type.ERROR_MESSAGE);
          // set original dates, so that it updates immediately on client side
          super.setDates(event, event.getStart(), event.getEnd());
          return;
        }
        // do only allow to drag if no other events are overwritten
        if (bookMap.get(getCurrentDevice()).getEvents(start, end).size() > 0) {
          super.setDates(event, start, end);
        } else {
          // set original dates, so that it updates immediately on client side
          super.setDates(event, event.getStart(), event.getEnd());
        }
      }
    });

    // resizing of events
    cal.setHandler(new BasicEventResizeHandler() {
      protected void setDates(com.vaadin.ui.components.calendar.event.EditableCalendarEvent event,
          Date start, Date end) {
        if (!event.getCaption().startsWith(bookingmodel.userName())) {
          Notification.show("You can only change your own, time slots.", Type.ERROR_MESSAGE);
          // set original dates, so that it updates immediately on client side
          super.setDates(event, event.getStart(), event.getEnd());
          return;
        }
        // do only allow to resize if no other events are overwritten. == 1 because the event itself
        // is found, when resizing
        if (bookMap.get(getCurrentDevice()).getEvents(start, end).size() == 1) {
          super.setDates(event, start, end);
        } else {
          // set original dates, so that it updates immediately on client side
          super.setDates(event, event.getStart(), event.getEnd());
        }
      }
    });*/

    return cal;
  }

  void removeEvent(CalendarEvent event) {
    bookMap.get(getCurrentDevice()).removeEvent(event);
    newEvents.get(getCurrentDevice()).remove(event);
    eventCounter--;
  }

  void addEvent(Date start, Date end) {
    CalendarEvent event =
        new BasicEvent(bookingModel.userName() + " (" + currentKostenStelle() + ")",
            "Cost for this session: " + bookingModel.cost(start, end, getCurrentDevice())
                + " Euro.", start, end);
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

  String currentKostenStelle() {
    return (String) selectedKostenStelle.getValue();
  }

  String currentProject() {
	  return (String) selectedProject.getValue();
  }

  protected String getCurrentDevice() {
    return (String) selectedDevice.getValue();
  }

  private Calendar superCalendar() {
    throw new NotImplementedException();
  }

  private Calendar advancedCalendar() {
    throw new NotImplementedException();
  }

  NativeSelect initDevices(List<String> devices) {
    String caption = "Select Device";

    NativeSelect selectDevice = new NativeSelect();
    selectDevice.addItems(devices);

    selectDevice.setCaption(caption);
    selectDevice.setDescription(caption);
    selectDevice.setNullSelectionAllowed(false);
    return selectDevice;
  }


  NativeSelect initKostenstellen(List<String> kostenStellen) {
    String caption = "Select Kostenstelle";
    NativeSelect selectKostenstelle = new NativeSelect();
    selectKostenstelle.addItems(kostenStellen);

    selectKostenstelle.setCaption(caption);
    selectKostenstelle.setDescription(caption);
    selectKostenstelle.setNullSelectionAllowed(false);
    return selectKostenstelle;
  }
  
  NativeSelect initProjects(List<String> projects) {
	    String caption = "Select Project";
	    NativeSelect selectProject = new NativeSelect();
	    selectProject.addItems(projects);

	    selectProject.setCaption(caption);
	    selectProject.setDescription(caption);
	    selectProject.setNullSelectionAllowed(false);
	    return selectProject;
	  }

  
  class MyEventHandler{
    final String MESSAGE_IN_THE_PAST = "You are trying to use a device in the past";
    final String MESSAGE_ALREADY_TAKEN = "You are trying to select an already occupied time frame";
    final String MESSAGE_KOSTENSTELLE_NOT_SELECTED = "Kostenstelle not selected.";
    final String MESSAGE_PERMISSION_DENIED_TIME_SLOT = "You can only change your own time slots.";
    final String MESSAGE_NOTHING_TO_DELETE = "No event to delete";
    final String MESSAGE_NOTHING_TO_EDIT = "No event to edit";
    Calendar cal;
    BookingModel bookingModel;
    
    public MyEventHandler(Calendar cal,BookingModel bookingModel ){
      this.cal = cal;
      this.bookingModel = bookingModel;
    }
    boolean add(Date start, Date end){
      if (start.before(referenceDate)) {
        showError(MESSAGE_IN_THE_PAST);
      } else if (cal.getEvents(start, end).size() > 0) {
        showError(MESSAGE_ALREADY_TAKEN);
      } else if (selectedKostenStelle.getValue() == null) {
        showError(MESSAGE_KOSTENSTELLE_NOT_SELECTED);
      } else {
        addEvent(start, end);
        return true;
      }
      return false;
    }
    

    void setDate(com.vaadin.ui.components.calendar.event.EditableCalendarEvent event, Date start,
        Date end) {
      event.setStart(start);
      event.setEnd(end);
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

    Action addEventAction = new Action("Add Event");
    Action deleteEventAction = new Action("Delete Event");
    Action editEventAction = new Action("Edit Event");
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
        return new Action[] {addEventAction, deleteEventAction, editEventAction};
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
          showError(MESSAGE_ALREADY_TAKEN);
        
      } else if (action == deleteEventAction) {
        // Check if the action was clicked on top of an event
        if (target instanceof CalendarEvent) {
          if (((CalendarEvent) target).getCaption().startsWith(bookingModel.userName())) {
            removeEvent((CalendarEvent) target);
          } else {
            showError(MESSAGE_PERMISSION_DENIED_TIME_SLOT);
          }
        } else
          showError(MESSAGE_NOTHING_TO_DELETE);
      } else if(action == editEventAction){
        if (target instanceof CalendarEvent) {
          if (((CalendarEvent) target).getCaption().startsWith(bookingModel.userName())) {
            showEventPopup((CalendarEvent) target);
          } else {
            showError(MESSAGE_PERMISSION_DENIED_TIME_SLOT);
          }
        } else
          showError(MESSAGE_NOTHING_TO_EDIT);       
      }
    }
  }
  
  /**
   * Add events on range selection
   */
  class MyEventRangeSelectHandler extends MyEventHandler implements RangeSelectHandler{
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
    
    protected void setDates(com.vaadin.ui.components.calendar.event.EditableCalendarEvent event,
        Date start, Date end) {
      if (!event.getCaption().startsWith(bookingModel.userName())) {
        showError(MESSAGE_PERMISSION_DENIED_TIME_SLOT);
        // set original dates, so that it updates immediately on client side
        setDate(event, event.getStart(), event.getEnd());
        return;
      }else   if (start.before(referenceDate) || event.getStart().before(referenceDate)) {
        showError(MESSAGE_IN_THE_PAST);
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
      else if(events.size () == 2){
        
        CalendarEvent overlappingEvent = events.get(0).equals(event)?events.get(1):events.get(0);   
        if(start.before(overlappingEvent.getEnd()) && end.after(overlappingEvent.getEnd())){
          setDate(event,overlappingEvent.getEnd(),end);
        }else {
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
    protected void setDates(com.vaadin.ui.components.calendar.event.EditableCalendarEvent event,
        Date start, Date end) {
      if (!event.getCaption().startsWith(bookingModel.userName())) {
        showError(MESSAGE_PERMISSION_DENIED_TIME_SLOT);
        // set original dates, so that it updates immediately on client side
        setDate(event, event.getStart(), event.getEnd());
        return;
      }else   if (start.before(referenceDate) || event.getStart().before(referenceDate)) {
        showError(MESSAGE_IN_THE_PAST);
        setDate(event, event.getStart(), event.getEnd());
        return;
      }
      
      // do only allow to move if it does not overlap with any other event 
      System.out.println("move: "+ cal.getEvents(start, end).size());
      List<CalendarEvent> events  = cal.getEvents(start, end);
      if (events.size() == 0 || (events.size() == 1 && events.get(0).equals(event))) {
        setDate(event, start, end);
      }
       else {
        // set original dates, so that it updates immediately on client side
        setDate(event, event.getStart(), event.getEnd());
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
