/*******************************************************************************
 * QBiC Calendar provides an infrastructure for defining calendars for specific purposes like
 * booking devices or planning resources for services and integration of relevant data into the
 * common portal infrastructure. Copyright (C) 2017 Aydın Can Polatkan
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
  private Date referenceDate;

  private NativeSelect selectedService;
  private TabSheet booking;

  private static Database db;

  public CompareCal(final BookingModel bookingModel, Date referenceDate) {

    this.bookingModel = bookingModel;
    this.referenceDate = referenceDate;

    Label selectDeviceLabel = new Label();
    selectDeviceLabel.addStyleName("h4");
    selectDeviceLabel.setValue("Select Instrument");

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

    selectedDevice.addValueChangeListener(new ValueChangeListener() {
      private static final long serialVersionUID = 8153818693511960689L;

      @Override
      public void valueChange(ValueChangeEvent event) {

        if (bookMap.containsKey(getCurrentDevice())) {
          cal.removeAllComponents();
          setCalendar();

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
    cal.setSizeFull();
    cal.isReadOnly();

    // Show the image in the application
    // image.setWidth("100%");
    // Let the user view the file in browser or download it
    // Link link = new Link("Link to the image file", resource);
    gridLayout.setWidth("100%");

    gridLayout.addComponent(selectedDevice, 0, 0);

    gridLayout.addComponent(cal, 0, 2, 5, 2);

    gridLayout.setMargin(true);
    gridLayout.setSpacing(true);
    gridLayout.setSizeFull();

    book.setContent(gridLayout);
    booking = new TabSheet();
    booking.addStyleName(ValoTheme.TABSHEET_FRAMED);
    booking.addStyleName(ValoTheme.TABSHEET_EQUAL_WIDTH_TABS);
    booking.addTab(book).setCaption("Compare Instrument Usage");
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
    String selectDeviceCaption = "Select Instrument";
    String selectDeviceDescription =
        "Please select an instrument to see the device usage and booking data!";
    NativeSelect selectDevice = new NativeSelect();
    selectDevice.addItems(devices);
    selectDevice.setCaption(selectDeviceCaption);
    selectDevice.setDescription(selectDeviceDescription);
    selectDevice.setNullSelectionAllowed(false);
    return selectDevice;
  }

}
