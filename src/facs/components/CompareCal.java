/*******************************************************************************
 * QBiC Calendar provides an infrastructure for defining calendars for specific purposes like
 * booking devices or planning resources for services and integration of relevant data into the
 * common portal infrastructure. Copyright (C) 2016 Aydın Can Polatkan & David Wojnar
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.Calendar;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.components.calendar.event.CalendarEvent;
import com.vaadin.ui.themes.ValoTheme;

import facs.db.DBManager;
import facs.db.Database;
import facs.model.BookingModel;

public class CompareCal extends CustomComponent {
  private static final long serialVersionUID = -4396068933947619408L;

  private HorizontalLayout cal = new HorizontalLayout();
  private GridLayout gridLayout = new GridLayout(6, 7);

  private BookingModel bookingModel;
  private NativeSelect selectedDevice;
  private Map<String, Calendar> bookMap = new HashMap<String, Calendar>();
  private Map<String, Set<CalendarEvent>> newEvents = new HashMap<String, Set<CalendarEvent>>();
  private int eventCounter = 0;
  private Date referenceDate;

  private NativeSelect selectedService;
  private TabSheet booking;

  private static Database db;

  public CompareCal(final BookingModel bookingModel, Date referenceDate) {

    this.bookingModel = bookingModel;
    this.referenceDate = referenceDate;

    Label selectDeviceLabel = new Label();
    selectDeviceLabel.addStyleName("h4");
    selectDeviceLabel.setValue("Please Select a Device");

    Label countLabel = new Label();
    countLabel.addStyleName("h6");

    Date dNow = new Date();
    SimpleDateFormat ft = new SimpleDateFormat("dd.MMM.yyyy HH:mm:ss");
    System.out.println(ft.format(dNow) + "  INFO  Compare Calendar initiated! - User: "
        + bookingModel.getLDAP());

    // only users who are allowed to book devices will be able to do so
    if (bookingModel.isNotAllowed()) {
      VerticalLayout errorLayout = new VerticalLayout();
      showErrorNotification(
          "Access Denied!",
          "Sorry, you're not allowed to see anything here, at least your username told us so. Do you need assistance? Please contact 'helpdesk@qbic.uni-tuebingen.de'.");
      setCompositionRoot(errorLayout);
      return;
    }

    Panel book = new Panel();
    book.addStyleName(ValoTheme.PANEL_BORDERLESS);

    DBManager.getDatabaseInstance();
    db = Database.Instance;
    db.userLogin(bookingModel.getLDAP(), Page.getCurrent().getWebBrowser().getBrowserApplication(),
        Page.getCurrent().getWebBrowser().getAddress());

    selectedDevice = initCalendars(bookingModel.getDevicesNames());

    selectedService = new NativeSelect("Please select a Service:");
    selectedService.setDescription("Please select the service you would like to receive!");

    selectedDevice.addValueChangeListener(new ValueChangeListener() {
      private static final long serialVersionUID = 8153818693511960689L;

      @Override
      public void valueChange(ValueChangeEvent event) {

        if (bookMap.containsKey(getCurrentDevice())) {
          cal.removeAllComponents();
          setCalendar();

          if (selectedDevice.getValue().equals("Aria")) {
            selectedService.removeAllItems();
            selectedService.addItems("Full Service", "Partial Service", "Self Service");
            selectedService.setValue("Full Service");
            selectedService.setVisible(true);
          } else if (selectedDevice.getValue().equals("Mac")) {
            selectedService.removeAllItems();
            selectedService.addItems("Self", "Service");
            selectedService.setValue("Service");
            selectedService.setVisible(true);
          } else {
            selectedService.setValue(null);
            selectedService.setVisible(false);
          }

        } else {

          bookMap.put(getCurrentDevice(), initCal(bookingModel, getCurrentDevice()));
          cal.removeAllComponents();
          setCalendar();

        }
      }
    });

    countLabel.setValue("Booking Count: " + db.getAllUnconfirmedCount() + " - Machine Output: "
        + db.getAllBookingTotalCount());

    cal.setLocale(Locale.getDefault());
    cal.setImmediate(true);
    selectedService.setImmediate(true);
    cal.setSizeFull();
    cal.isReadOnly();

    // Show the image in the application
    // image.setWidth("100%");
    // Let the user view the file in browser or download it
    // Link link = new Link("Link to the image file", resource);
    gridLayout.setWidth("100%");

    gridLayout.addComponent(selectedDevice, 0, 0);
    gridLayout.addComponent(selectedService, 1, 0);

    selectedService.setVisible(false);

    gridLayout.addComponent(cal, 0, 2, 5, 2);
    // gridLayout.addComponent(countLabel, 2, 0);

    gridLayout.setMargin(true);
    gridLayout.setSpacing(true);
    gridLayout.setSizeFull();

    book.setContent(gridLayout);
    booking = new TabSheet();
    booking.addStyleName(ValoTheme.TABSHEET_FRAMED);
    booking.addTab(book).setCaption("Calendar");
    setCompositionRoot(booking);

  }

  protected void setCalendar() {
    cal.removeAllComponents();
    cal.addComponent(bookMap.get(getCurrentDevice()));
  }

  private void showErrorNotification(String title, String description) {
    Notification notify = new Notification(title, description);
    notify.setDelayMsec(16000);
    notify.setPosition(Position.TOP_CENTER);
    notify.setIcon(FontAwesome.FROWN_O);
    notify.setStyleName(ValoTheme.NOTIFICATION_ERROR + " " + ValoTheme.NOTIFICATION_CLOSABLE);
    notify.show(Page.getCurrent());
  }

  private void showNotification(String title, String description) {
    Notification notify = new Notification(title, description);
    notify.setDelayMsec(8000);
    notify.setPosition(Position.TOP_CENTER);
    notify.setIcon(FontAwesome.MEH_O);
    notify.setStyleName(ValoTheme.NOTIFICATION_TRAY + " " + ValoTheme.NOTIFICATION_CLOSABLE);
    notify.show(Page.getCurrent());
  }

  private void showSuccessfulNotification(String title, String description) {
    Notification notify = new Notification(title, description);
    notify.setDelayMsec(8000);
    notify.setPosition(Position.TOP_CENTER);
    notify.setIcon(FontAwesome.SMILE_O);
    notify.setStyleName(ValoTheme.NOTIFICATION_SUCCESS + " " + ValoTheme.NOTIFICATION_CLOSABLE);
    notify.show(Page.getCurrent());
  }

  Calendar initCal(BookingModel bookingmodel, String currentDevice) {

    Calendar calendar = compareCalendar(bookingmodel);
    return calendar;

  }

  private Calendar compareCalendar(final BookingModel bookingmodel) {
    final Calendar cal = new Calendar();

    for (CalendarEvent event : bookingmodel.getAllEvents(getCurrentDevice())) {
      cal.addEvent(event);
    }

    cal.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
    cal.setLocale(Locale.GERMANY);
    cal.setWidth("100%");
    cal.setHeight("1000px");

    return cal;
  }

  protected String getCurrentDevice() {
    return (String) selectedDevice.getValue();
  }

  protected String getGroupID() {
    return bookingModel.getGroupID();
  }

  protected int getCost(String currentDevice, String currentService, String groupID) {
    // System.out.println("getCost: " +currentDevice+" "+currentService+" "+groupID);
    return db.getDeviceCostPerGroup(currentDevice, currentService, groupID);
  }

  NativeSelect initCalendars(List<String> devices) {
    String selectDeviceCaption = "Please select an Instrument or a Service:";
    String selectDeviceDescription =
        "Please select a device to ask for a booking request or to book!";
    NativeSelect selectDevice = new NativeSelect();
    selectDevice.addItems(devices);
    selectDevice.setCaption(selectDeviceCaption);
    selectDevice.setDescription(selectDeviceDescription);
    selectDevice.setNullSelectionAllowed(false);
    return selectDevice;
  }

  class MyEventHandler {

    final String MESSAGE_24_HOURS_LIMIT = "o_O 3 Hours Limit Counts!";
    final String MESSAGE_24_HOURS_LIMIT_DESCRIPTION =
        "It's not possible to delete this booking since it's already in the last 3 hours limit, please try to contact your facility operator!";
    final String MESSAGE_IN_THE_PAST_TITLE = "o_O we can't turn back the time!";
    final String MESSAGE_IN_THE_PAST_DESCRIPTION =
        "Booking failed because you selected a time frame in the past. Please select current or future dates for booking and try again!";
    final String MESSAGE_ALREADY_TAKEN_TITLE = "o_O someone else got it first!";
    final String MESSAGE_ALREADY_TAKEN_DESCRIPTION =
        "Booking failed because you selected an occupied time frame. Please select a new but 'free' time frame and try again!";
    final String MESSAGE_PERMISSION_DENIED_TIME_SLOT_TITLE = "Hands off, not yours.";
    final String MESSAGE_PERMISSION_DENIED_TIME_SLOT_DESCRIPTION =
        "Action cancelled because you tried to change someone else's booking. You can only mark/rebook/delete your own bookings.";
    final String MESSAGE_NOTHING_TO_DELETE_TITLE = "There is no spoon.";
    final String MESSAGE_NOTHING_TO_DELETE_DESCRIPTION =
        "Action cancelled because you tried to delete a nonexisting booking. Do not try and bend the spoon. That's impossible.";
    final String MESSAGE_NOTHING_TO_EDIT_TITLE = "There is no spoon.";
    final String MESSAGE_NOTHING_TO_EDIT_DESCRIPTION =
        "Action cancelled because you tried to edit a nonexisting booking. Do not try and bend the spoon. That's impossible.";
    final String MESSAGE_OVERLAP_TITLE = "Pfoom! It's the sound of an overlap!";
    final String MESSAGE_OVERLAP_DESCRIPTION =
        "Unless we have a bug in the system, there is no way to overlap two bookings in the same timeframe. How did this happen now?";
    final String MESSAGE_ITEM_PURGED = "The booking was deleted!";
    final String MESSAGE_ITEM_PURGED_DESCRIPTION =
        "You wanted to delete an upcoming booking and it wasn't within the next 3 hours. All good, item purged.";
    final String MESSAGE_ITEM_PURGED_DESCRIPTION_ADMIN =
        "You have the Admin Power, please use it wisely! - All good, item purged.";
    final String MESSAGE_FAULTY_TITLE = "aye aye! Booking is marked as 'Faulty'!";
    final String MESSAGE_FAULTY_DESCRIPTION =
        "Something went wrong? As you requested, we marked this booking item as 'Faulty'. Thank you for your feedback, we will take the necessary steps to clear the problem.";

    Calendar cal;
    BookingModel bookingModel;

    public MyEventHandler(Calendar cal, BookingModel bookingModel) {
      this.cal = cal;
      this.bookingModel = bookingModel;
    }

    void setDate(com.vaadin.ui.components.calendar.event.EditableCalendarEvent event, Date start,
        Date end) {
      event.setStart(start);
      event.setEnd(end);
      event.setStyleName("color1");
    }

    void showError(String message) {
      Notification.show(message, Type.ERROR_MESSAGE);
    }
  }
}
