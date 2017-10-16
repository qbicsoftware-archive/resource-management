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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.event.Action;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinService;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.Position;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Calendar;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.EventMoveHandler;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.EventResize;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.EventResizeHandler;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.MoveEvent;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.RangeSelectEvent;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.RangeSelectHandler;
import com.vaadin.ui.components.calendar.CalendarDateRange;
import com.vaadin.ui.components.calendar.event.BasicEvent;
import com.vaadin.ui.components.calendar.event.CalendarEvent;
import com.vaadin.ui.components.calendar.event.EditableCalendarEvent;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickEvent;
import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.ui.renderers.NumberRenderer;
import com.vaadin.ui.themes.ValoTheme;

import facs.db.DBManager;
import facs.db.Database;
import facs.model.BookingBean;
import facs.model.BookingModel;
import facs.model.Constants;
import facs.model.FacsModelUtil;

public class Booking extends CustomComponent {
  private static final long serialVersionUID = -4396068933947619408L;

  private HorizontalLayout cal = new HorizontalLayout();
  private GridLayout gridLayout = new GridLayout(6, 7);

  private BookingModel bookingModel;
  private NativeSelect selectedDevice;
  private Map<String, Calendar> bookMap = new HashMap<String, Calendar>();
  private Map<String, Set<CalendarEvent>> newEvents = new HashMap<String, Set<CalendarEvent>>();
  private int eventCounter = 0;
  private NativeSelect selectedKostenstelle;
  private Date referenceDate;
  private NativeSelect selectedService;
  private Grid upcomingBookings;
  private Grid pastBookings;
  private TabSheet booking;
  private Grid next3HoursBookings;
  private ComboBox book4Users;

  private static Database db;

  public Booking(final BookingModel bookingModel, Date referenceDate) {

    this.bookingModel = bookingModel;
    this.referenceDate = referenceDate;

    Label infoLabel = new Label();
    infoLabel.addStyleName("h4");

    Label selectDeviceLabel = new Label();
    selectDeviceLabel.addStyleName("h4");
    selectDeviceLabel.setValue("Select Instrument");

    book4Users = new ComboBox("Select User/Maintenance/Service");
    book4Users
        .setDescription("FACS Admins can book on behalf of other users or Maintenance/Service");
    book4Users.setEnabled(true);

    final Label versionLabel = new Label();
    versionLabel.setValue("Version 0.2.171016");

    Label userNameLabel = new Label();
    Label userRoleLabel = new Label();
    Label userKostenstelleLabel = new Label();
    Label userProjectLabel = new Label();
    Label userInstituteLabel = new Label();

    Label countLabel = new Label();
    Label totalCountLabel = new Label();

    Date dNow = new Date();
    SimpleDateFormat ft = new SimpleDateFormat("dd.MMM.yyyy HH:mm:ss");
    System.out.println(ft.format(dNow) + "  INFO  Calendar initiated! - User: "
        + bookingModel.getLDAP() + " " + versionLabel);

    if (bookingModel.isNotAllowed()) {
      VerticalLayout errorLayout = new VerticalLayout();
      infoLabel.setValue("ACCESS DENIED");
      errorLayout.addComponent(infoLabel);
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

    selectedService = new NativeSelect("Select Service");
    selectedService.setDescription("Please select the service you would like to receive!");

    selectedKostenstelle = new NativeSelect("Select Kostenstelle");
    selectedKostenstelle.setDescription("Please select the Kostenstelle you would like to use!");

    book4Users.addItems(db.getAllUserNames());

    selectedKostenstelle.addItems(db.getKostenstelleCodes());

    selectedDevice.addValueChangeListener(new ValueChangeListener() {


      /**
       * 
       */
      private static final long serialVersionUID = -4030281517085490538L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        userRoleLabel.setValue(db.getUserRoleDescByLDAPId(bookingModel.getLDAP(),
            getCurrentDevice()));

        selectedKostenstelle.setVisible(true);

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
        }
      }
    });

    if (bookingModel.getProject().isEmpty()) {
      userNameLabel.setValue("Name: " + bookingModel.userName());
      userKostenstelleLabel.setValue("Kostenstelle: " + bookingModel.getKostenstelle());
      userProjectLabel.setValue("No project found.");
      userInstituteLabel.setValue("Institute: " + bookingModel.getInstitute());
      userRoleLabel.setValue("Cal role will appear here!");

      userNameLabel.addStyleName(ValoTheme.LABEL_SUCCESS);
      userKostenstelleLabel.addStyleName(ValoTheme.LABEL_SUCCESS);
      userProjectLabel.addStyleName(ValoTheme.LABEL_FAILURE);
      userInstituteLabel.addStyleName(ValoTheme.LABEL_SUCCESS);
      userRoleLabel.addStyleName(ValoTheme.LABEL_SUCCESS);

    } else {

      userNameLabel.setValue("Name: " + bookingModel.userName());
      userKostenstelleLabel.setValue("Kostenstelle: " + bookingModel.getKostenstelle());
      userProjectLabel.setValue("Project: " + bookingModel.getProject());
      userInstituteLabel.setValue("Institute: " + bookingModel.getInstitute());
      userRoleLabel.setValue("Cal role will appear here!");

      userNameLabel.addStyleName(ValoTheme.LABEL_SUCCESS);
      userKostenstelleLabel.addStyleName(ValoTheme.LABEL_SUCCESS);
      userProjectLabel.addStyleName(ValoTheme.LABEL_SUCCESS);
      userInstituteLabel.addStyleName(ValoTheme.LABEL_SUCCESS);
      userRoleLabel.addStyleName(ValoTheme.LABEL_SUCCESS);

    }

    countLabel.setSizeFull();
    countLabel.setValue("Unconfirmed Bookings: " + db.getAllUnconfirmedCount());
    countLabel.setDescription("FACS admins need to confirm these bookings.");
    countLabel.addStyleName(ValoTheme.LABEL_FAILURE);

    totalCountLabel.setSizeFull();
    totalCountLabel.setValue("Total # of Bookings: " + db.getAllBookingTotalCount());
    totalCountLabel.setDescription("Total # of completed bookings.");
    totalCountLabel.addStyleName(ValoTheme.LABEL_SUCCESS);

    versionLabel.addStyleName(ValoTheme.LABEL_SMALL);

    Panel deviceSelectionPanel = new Panel("");
    deviceSelectionPanel.addStyleName(ValoTheme.PANEL_WELL);
    HorizontalLayout rightDeviceSelection = new HorizontalLayout();
    rightDeviceSelection.setSpacing(true);
    rightDeviceSelection.setMargin(true);
    rightDeviceSelection.setSizeFull();
    deviceSelectionPanel.setContent(rightDeviceSelection);

    Panel bookingStatsPanel = new Panel("Booking Stats & Legend");
    VerticalLayout rightBookingStats = new VerticalLayout();
    rightBookingStats.setSpacing(true);
    rightBookingStats.setMargin(true);
    rightBookingStats.setSizeFull();
    bookingStatsPanel.setContent(rightBookingStats);

    Panel userDetailsPanel = new Panel("User Details");
    VerticalLayout rightUserDetails = new VerticalLayout();
    rightUserDetails.setSpacing(true);
    rightUserDetails.setMargin(true);
    rightUserDetails.setSizeFull();
    userDetailsPanel.setContent(rightUserDetails);

    selectedKostenstelle.select(db.getKostenstelleByLDAPId(bookingModel.getLDAP()));

    cal.setLocale(Locale.getDefault());
    cal.setImmediate(true);
    selectedService.setImmediate(true);
    cal.setSizeFull();

    String submitTitle = "Book";
    Button submit = new Button(submitTitle);
    submit.setIcon(FontAwesome.CALENDAR);
    submit
        .setDescription("Please select an instrument and a time frame at first then click 'BOOK'!");
    submit.setSizeFull();

    submit.addClickListener(new ClickListener() {

      /**
       * 
       */
      private static final long serialVersionUID = 7518184802334872415L;

      @Override
      public void buttonClick(ClickEvent event) {
        submit(bookingModel.getLDAP(), getCurrentDevice());
        newEvents.clear();
        refreshDataSources();
      }
    });

    String buttonTitle = "Refresh";
    Button refresh = new Button(buttonTitle);
    refresh.setIcon(FontAwesome.REFRESH);
    refresh.setSizeFull();
    refresh.setDescription("Click here to reload the data from the database!");
    refresh.addStyleName(ValoTheme.BUTTON_FRIENDLY);

    refresh.addClickListener(new ClickListener() {

      /**
       * 
       */
      private static final long serialVersionUID = 2646232766520616273L;

      @Override
      public void buttonClick(ClickEvent event) {

        refreshDataSources();

      }
    });

    String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();

    FileResource resource = new FileResource(new File(basepath + "/WEB-INF/images/legend.png"));

    Image image = new Image("", resource);
    image.setSizeUndefined();

    selectedDevice.setSizeFull();
    selectedService.setSizeFull();
    selectedKostenstelle.setSizeFull();
    book4Users.setSizeFull();

    submit.setSizeFull();

    rightDeviceSelection.addComponent(selectedDevice);
    rightDeviceSelection.addComponent(selectedService);
    rightDeviceSelection.addComponent(selectedKostenstelle);

    rightBookingStats.addComponent(countLabel);
    rightBookingStats.addComponent(totalCountLabel);
    rightBookingStats.addComponent(image);
    rightBookingStats.addComponent(versionLabel);

    rightUserDetails.addComponent(book4Users);
    rightUserDetails.addComponent(userNameLabel);
    rightUserDetails.addComponent(userKostenstelleLabel);
    rightUserDetails.addComponent(userProjectLabel);
    rightUserDetails.addComponent(userInstituteLabel);
    rightUserDetails.addComponent(userRoleLabel);

    gridLayout.setWidth("100%");

    selectedService.setVisible(false);

    gridLayout.addComponent(deviceSelectionPanel, 0, 1, 5, 1);
    gridLayout.addComponent(cal, 0, 2, 5, 2);
    gridLayout.addComponent(refresh, 0, 3);
    gridLayout.addComponent(submit, 1, 3, 5, 3);
    gridLayout.addComponent(bookingStatsPanel, 0, 6, 3, 6);
    gridLayout.addComponent(userDetailsPanel, 4, 6, 5, 6);

    gridLayout.setMargin(true);
    gridLayout.setSpacing(true);
    gridLayout.setSizeFull();

    book.setContent(gridLayout);
    booking = new TabSheet();
    booking.addStyleName(ValoTheme.TABSHEET_FRAMED);
    booking.addStyleName(ValoTheme.TABSHEET_EQUAL_WIDTH_TABS);
    booking.addTab(book).setCaption("Calendar");
    booking.getTab(0).setIcon(FontAwesome.CALENDAR);
    booking.addTab(myNext3HoursBookings()).setCaption("Next 3 Hours");
    booking.getTab(1).setIcon(FontAwesome.LOCK);
    booking.addTab(myUpcomingBookings()).setCaption("Upcoming");
    booking.getTab(2).setIcon(FontAwesome.CLOCK_O);
    booking.addTab(myPastBookings()).setCaption("Past Bookings");
    booking.getTab(3).setIcon(FontAwesome.HISTORY);
    setCompositionRoot(booking);

    if (bookingModel.getAdminAccess() != 1) {
      book4Users.setEnabled(false);
      book4Users.setVisible(false);

    }

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

  private Component myNext3HoursBookings() {
    VerticalLayout devicesLayout = new VerticalLayout();

    devicesLayout.setMargin(true);
    devicesLayout.setSpacing(true);

    Date serverTime = new WebBrowser().getCurrentDate();
    Date nextDayTime = new Date(serverTime.getTime() + (1000 * 60 * 60 * 3));

    BeanItemContainer<BookingBean> users =
        getMyNext3HoursBookings(bookingModel.getLDAP(), serverTime, nextDayTime);

    GeneratedPropertyContainer gpc = new GeneratedPropertyContainer(users);

    next3HoursBookings = new Grid(gpc);

    next3HoursBookings.setStyleName("my-style");
    next3HoursBookings.setWidth("100%");
    next3HoursBookings.setSelectionMode(SelectionMode.SINGLE);
    next3HoursBookings.setEditorEnabled(false);

    next3HoursBookings.setColumnOrder("ID", "confirmation", "deviceName", "service", "start",
        "end", "username", "phone", "price");
    next3HoursBookings.getColumn("deviceName").setHeaderCaption("Instrument");
    next3HoursBookings.getColumn("price").setHeaderCaption("Approx. Price");
    setRenderers(next3HoursBookings);
    devicesLayout.addComponent(next3HoursBookings);

    return devicesLayout;
  }

  private Component myPastBookings() {
    VerticalLayout devicesLayout = new VerticalLayout();

    devicesLayout.setMargin(true);
    devicesLayout.setSpacing(true);

    Date serverTime = new WebBrowser().getCurrentDate();

    BeanItemContainer<BookingBean> users = getMyPastBookings(bookingModel.getLDAP(), serverTime);

    GeneratedPropertyContainer gpc = new GeneratedPropertyContainer(users);

    pastBookings = new Grid(gpc);

    pastBookings.setStyleName("my-style");
    pastBookings.setWidth("100%");
    pastBookings.setSelectionMode(SelectionMode.SINGLE);
    pastBookings.setEditorEnabled(false);

    pastBookings.setColumnOrder("ID", "confirmation", "deviceName", "service", "start", "end",
        "username", "phone", "price");

    pastBookings.getColumn("deviceName").setHeaderCaption("Instrument");
    pastBookings.getColumn("price").setHeaderCaption("Approx. Price");

    setRenderers(pastBookings);
    devicesLayout.addComponent(pastBookings);

    return devicesLayout;
  }

  private Component myUpcomingBookings() {
    VerticalLayout devicesLayout = new VerticalLayout();

    devicesLayout.setMargin(true);
    devicesLayout.setSpacing(true);

    Date serverTime = new WebBrowser().getCurrentDate();
    Date nextDayTime = new Date(serverTime.getTime() + (1000 * 60 * 60 * 3));

    BeanItemContainer<BookingBean> users =
        getMyUpcomingBookings(bookingModel.getLDAP(), nextDayTime);


    GeneratedPropertyContainer gpc = new GeneratedPropertyContainer(users);
    gpc.addGeneratedProperty("delete", new PropertyValueGenerator<String>() {


      /**
       * 
       */
      private static final long serialVersionUID = 2319976637634488204L;

      @Override
      public String getValue(Item item, Object itemId, Object propertyId) {
        return "Trash"; // The caption

      }

      @Override
      public Class<String> getType() {
        return String.class;
      }
    });

    upcomingBookings = new Grid(gpc);

    upcomingBookings.setStyleName("my-style");
    upcomingBookings.setWidth("100%");
    upcomingBookings.setSelectionMode(SelectionMode.SINGLE);
    upcomingBookings.setEditorEnabled(false);

    upcomingBookings.setColumnOrder("ID", "confirmation", "deviceName", "service", "start", "end",
        "username", "phone", "price");
    upcomingBookings.getColumn("deviceName").setHeaderCaption("Instrument");
    upcomingBookings.getColumn("price").setHeaderCaption("Approx. Price");

    setRenderers(upcomingBookings);
    devicesLayout.addComponent(upcomingBookings);

    upcomingBookings.getColumn("delete").setRenderer(
        new ButtonRenderer(new ClickableRenderer.RendererClickListener() {

          /**
           * 
           */
          private static final long serialVersionUID = 8236764262983894939L;

          @Override
          public void click(RendererClickEvent event) {

            try {

              Window cd = new Window("Delete Booking?");

              cd.setHeight("200px");
              cd.setWidth("400px");
              cd.setResizable(false);

              GridLayout dialogLayout = new GridLayout(3, 3);

              Button okButton = new Button("Yes");
              okButton.addStyleName(ValoTheme.BUTTON_DANGER);
              Button cancelButton = new Button("No, I'm actually not sure!");
              cancelButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
              Label information = new Label("Are you sure you want to trash this item?");
              information.addStyleName(ValoTheme.LABEL_NO_MARGIN);

              okButton.addClickListener(new Button.ClickListener() {

                /**
                 * 
                 */
                private static final long serialVersionUID = 6619791395651961832L;

                @Override
                public void buttonClick(ClickEvent okEvent) {

                  purgeBooking((BookingBean) event.getItemId());

                  booking.setSelectedTab(myUpcomingBookings());

                  cd.close();

                  showNotification(
                      "The booking was deleted!",
                      "You wanted to delete an upcoming booking and it wasn't within the next 3 hours. All good, item purged.");
                }
              });

              cancelButton.addClickListener(new Button.ClickListener() {

                /**
                 * 
                 */
                private static final long serialVersionUID = 827588709488118131L;

                @Override
                public void buttonClick(ClickEvent okEvent) {
                  cd.close();
                }
              });

              dialogLayout.addComponent(information, 0, 0, 2, 0);
              dialogLayout.addComponent(okButton, 0, 1);
              dialogLayout.addComponent(cancelButton, 1, 1);
              dialogLayout.setMargin(true);
              dialogLayout.setSpacing(true);
              cd.setContent(dialogLayout);
              cd.center();
              UI.getCurrent().addWindow(cd);

            } catch (Exception e) {
              e.printStackTrace();
            }

          }

        }));

    return devicesLayout;
  }

  private BeanItemContainer<BookingBean> getMyNext3HoursBookings(String LDAP, Date start, Date end) {
    BeanItemContainer<BookingBean> bookingList =
        new BeanItemContainer<BookingBean>(BookingBean.class);
    List<BookingBean> bookings =
        DBManager.getDatabaseInstance().getMyNext3HoursBookings(LDAP, start, end);
    assert bookings != null;
    bookingList.addAll(bookings);
    return bookingList;
  }

  private BeanItemContainer<BookingBean> getMyUpcomingBookings(String LDAP, Date start) {
    BeanItemContainer<BookingBean> bookingList =
        new BeanItemContainer<BookingBean>(BookingBean.class);
    List<BookingBean> bookings = DBManager.getDatabaseInstance().getMyUpcomingBookings(LDAP, start);
    assert bookings != null;
    bookingList.addAll(bookings);
    return bookingList;
  }

  private BeanItemContainer<BookingBean> getMyPastBookings(String LDAP, Date start) {
    BeanItemContainer<BookingBean> bookingList =
        new BeanItemContainer<BookingBean>(BookingBean.class);
    List<BookingBean> bookings = DBManager.getDatabaseInstance().getMyPastBookings(LDAP, start);
    assert bookings != null;
    bookingList.addAll(bookings);
    return bookingList;
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

  protected void purgeBooking(BookingBean db) {
    boolean purged = DBManager.getDatabaseInstance().purgeBooking(db);
    if (purged) {
      upcomingBookings.getContainerDataSource().removeItem(db);
      showNotification(
          "The booking was deleted!",
          "You wanted to delete an upcoming booking and it wasn't within the next 3 hours. All good, item purged.");
    } else {
      showErrorNotification(
          "Jeez! It's not fair!",
          "For some reason we couldn't PURGE this booking. Maybe it's already restored or already purged from the database.");
    }
  }

  public void refreshDataSources() {
    BookingModel bookingModel = FacsModelUtil.getNoviceBookingModel();
    setCompositionRoot(new Booking(bookingModel, referenceDate));

  }

  public void refreshDataSourcesGrid() {
    BookingModel bookingModel = FacsModelUtil.getNoviceBookingModel();
    setCompositionRoot(new Booking(bookingModel, referenceDate));
  }


  void submit(String user_ldap, String currentDevice) {
    if (eventCounter == 0) {
      showNotification("We couldn't find any event to add!",
          "Did you select a time frame?\nPlease select a time frame at first then try again.");
      return;
    }

    if (db.getDeviceRestriction(currentDevice) == true) {

      if (db.getUserRoleByLDAPId(user_ldap, currentDevice).equals("V")) {
        showErrorNotification(
            "Access Denied!",
            "Sorry, you are not authorized to book. However, you can still view the calendars. Please click here to discard this message.");
        return;
      }

      Iterator<Entry<String, Set<CalendarEvent>>> it = newEvents.entrySet().iterator();

      String title = "Booking completed!";
      String description = "Congratulations!\nYou've succesfully added ";
      while (it.hasNext()) {
        Entry<String, Set<CalendarEvent>> entry = it.next();
        description += entry.getValue().size();
        description += " new booking(s) for instrument ";
        description += entry.getKey();
        description +=
            ". \nPlease keep in mind that Aria, Mac and Consulting requests has to be confirmed by FACS Facility managers.";
        for (CalendarEvent event : entry.getValue()) {
          if (event instanceof BasicEvent) {

            try {

              long s = event.getStart().getTime();
              long e = event.getEnd().getTime();
              long duration = e - s;

              ((BasicEvent) event).setStyleName("color2");

              if (book4Users.getValue() != null) {

                String userLDAPId = db.getUserLDAPIDbyUserName(book4Users.getValue().toString());

                db.addBooking(
                    userLDAPId,
                    (String) selectedDevice.getValue(),
                    event.getStart(),
                    event.getEnd(),
                    duration,
                    (String) selectedService.getValue(),
                    (String) selectedKostenstelle.getValue(),
                    bookingModel.cost(
                        event.getStart(),
                        event.getEnd(),
                        getCost((String) selectedDevice.getValue(),
                            (String) selectedService.getValue(), getGroupID())));
              } else {

                db.addBooking(
                    bookingModel.getLDAP(),
                    (String) selectedDevice.getValue(),
                    event.getStart(),
                    event.getEnd(),
                    duration,
                    (String) selectedService.getValue(),
                    (String) selectedKostenstelle.getValue(),
                    bookingModel.cost(
                        event.getStart(),
                        event.getEnd(),
                        getCost((String) selectedDevice.getValue(),
                            (String) selectedService.getValue(), getGroupID())), true);
              }
            } catch (NumberFormatException e) {
              e.printStackTrace();
            }

          }
        }
      }
      showSuccessfulNotification(title, description);
    } else {

      Iterator<Entry<String, Set<CalendarEvent>>> it = newEvents.entrySet().iterator();

      String title = "Booking completed!";
      String description = "Congratulations!\nYou've succesfully added ";
      while (it.hasNext()) {
        Entry<String, Set<CalendarEvent>> entry = it.next();
        description += entry.getValue().size();
        description += " new booking(s) for instrument ";
        description += entry.getKey();
        description +=
            ". \nPlease keep in mind that Aria, Mac and Consulting requests has to be confirmed by FACS Facility managers.";
        for (CalendarEvent event : entry.getValue()) {
          if (event instanceof BasicEvent) {

            try {

              long s = event.getStart().getTime();
              long e = event.getEnd().getTime();
              long duration = e - s;

              ((BasicEvent) event).setStyleName("color2");
              if (db.getUserRoleByLDAPId(user_ldap, currentDevice).equals("V")) {

                db.addBooking(
                    bookingModel.getLDAP(),
                    (String) selectedDevice.getValue(),
                    event.getStart(),
                    event.getEnd(),
                    duration,
                    (String) selectedService.getValue(),
                    (String) selectedKostenstelle.getValue(),
                    bookingModel.cost(
                        event.getStart(),
                        event.getEnd(),
                        getCost((String) selectedDevice.getValue(),
                            (String) selectedService.getValue(), getGroupID())), true);
              } else if (book4Users.getValue() != null) {
                String userLDAPId = db.getUserLDAPIDbyUserName(book4Users.getValue().toString());

                db.addBooking(
                    userLDAPId,
                    (String) selectedDevice.getValue(),
                    event.getStart(),
                    event.getEnd(),
                    duration,
                    (String) selectedService.getValue(),
                    (String) selectedKostenstelle.getValue(),
                    bookingModel.cost(
                        event.getStart(),
                        event.getEnd(),
                        getCost((String) selectedDevice.getValue(),
                            (String) selectedService.getValue(), getGroupID())));
              } else {

                db.addBooking(
                    bookingModel.getLDAP(),
                    (String) selectedDevice.getValue(),
                    event.getStart(),
                    event.getEnd(),
                    duration,
                    (String) selectedService.getValue(),
                    (String) selectedKostenstelle.getValue(),
                    bookingModel.cost(
                        event.getStart(),
                        event.getEnd(),
                        getCost((String) selectedDevice.getValue(),
                            (String) selectedService.getValue(), getGroupID())));
              }
            } catch (NumberFormatException e) {
              e.printStackTrace();
            }

          }
        }
      }

      showSuccessfulNotification(title, description);
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

  // BASIC users are allowed to see from MON-FRI from 08:00 until 17:59

  private Calendar basicCalendar(final BookingModel bookingmodel) {
    final Calendar cal = new Calendar();

    cal.setFirstVisibleDayOfWeek(java.util.Calendar.SUNDAY);
    cal.setLastVisibleDayOfWeek(java.util.Calendar.THURSDAY);

    cal.setFirstVisibleHourOfDay(8);
    cal.setLastVisibleHourOfDay(18);

    for (CalendarEvent event : bookingmodel.getAllEvents(getCurrentDevice())) {
      cal.addEvent(event);
      // System.out.println("Booking.java 251 Current Device: " + getCurrentDevice());
    }
    cal.setHandler(new MyEventRangeSelectHandler(cal, bookingmodel));
    cal.setHandler(new MyEventMoveHandler(cal, bookingmodel));
    cal.setHandler(new MyEventResizeHandler(cal, bookingmodel));
    cal.addActionHandler(new MyActionHandler(cal, bookingmodel));

    cal.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
    cal.setLocale(Locale.GERMANY);
    cal.setWidth("100%");
    // cal.setHeight("100%");
    cal.setHeight("1000px");

    return cal;
  }

  // NOVICE users are allowed to see from MON-FRI from 09:00 to 16:59

  private Calendar noviceCalendar(final BookingModel bookingmodel) {
    final Calendar cal = new Calendar();

    cal.setFirstVisibleDayOfWeek(java.util.Calendar.SUNDAY);
    cal.setLastVisibleDayOfWeek(java.util.Calendar.THURSDAY);

    cal.setFirstVisibleHourOfDay(9);
    cal.setLastVisibleHourOfDay(17);

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
    // cal.setHeight("100%");
    cal.setHeight("1000px");



    return cal;
  }

  // ADVANCED users are allowed to see MON-FRI from 00:00 to 23:59

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

  // SUPER users are allowed to see MON-SUN from 00:00 to 23:59

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


  // ADMIN user can see everything!

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
    eventCounter--;
  }

  void addEvent(Date start, Date end) {
    try {
      CalendarEvent event =
          new BasicEvent(bookingModel.userName() + " ( " + bookingModel.getKostenstelle() + " "
              + bookingModel.getProject() + ")", "Approx. costs for this booking: €"
              + bookingModel.cost(start, end,
                  getCost(getCurrentDevice(), (String) selectedService.getValue(), getGroupID()))
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
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected String getCurrentDevice() {
    return (String) selectedDevice.getValue();
  }

  protected String getGroupID() {
    return bookingModel.getGroupID();
  }

  protected int getCost(String currentDevice, String currentService, String groupID) {
    return db.getDeviceCostPerGroup(currentDevice, currentService, groupID);
  }

  NativeSelect initCalendars(List<String> devices) {
    String selectDeviceCaption = "Select Instrument";
    String selectDeviceDescription =
        "Please select an instrument to ask for a booking request or to book!";
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

    boolean add(Date start, Date end) {

      Date startX = new Date();
      startX.setTime(start.getTime() + 1);

      Date endX = new Date();
      endX.setTime(end.getTime() - 1);

      if (start.before(referenceDate)) {
        showErrorNotification(MESSAGE_IN_THE_PAST_TITLE, MESSAGE_IN_THE_PAST_DESCRIPTION);

      } else if (cal.getEvents(startX, endX).size() > 0) {
        showErrorNotification(MESSAGE_ALREADY_TAKEN_TITLE, MESSAGE_ALREADY_TAKEN_DESCRIPTION);
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
      event.setStyleName("color2");
    }

    public void showEventPopup(CalendarEvent target) {

      Window sub = new Window("Event");
      sub.setContent(new Label("Here's some content"));

      final int width = 300;
      sub.setWidth(width + "px");
      UI.getCurrent().addWindow(sub);
    }

    void showError(String message) {
      Notification.show(message, Type.ERROR_MESSAGE);
    }
  }

  /**
   * context menu
   */
  class MyActionHandler extends MyEventHandler implements Action.Handler {

    /**
     * 
     */
    private static final long serialVersionUID = 1726108611654552992L;

    public MyActionHandler(Calendar cal, BookingModel bookingModel) {
      super(cal, bookingModel);
    }

    Action addEventAction = new Action("Create Booking");
    Action deleteEventAction = new Action("Delete this booking");
    Action sendEventAction = new Action("Send an e-mail");
    Action faultyEventAction = new Action("Mark booking as 'faulty'");

    @Override
    public Action[] getActions(Object target, Object sender) {

      if (!(target instanceof CalendarDateRange))
        return null;
      CalendarDateRange dateRange = (CalendarDateRange) target;

      if (!(sender instanceof Calendar))
        return null;
      Calendar calendar = (Calendar) sender;

      List<CalendarEvent> events = calendar.getEvents(dateRange.getStart(), dateRange.getEnd());

      if (events.size() == 0)
        return new Action[] {addEventAction};
      else
        return new Action[] {deleteEventAction, sendEventAction, faultyEventAction};
    }

    @Override
    public void handleAction(Action action, Object sender, Object target) {

      if (action == addEventAction) {

        if (target instanceof java.util.Date) {
          java.util.Date date = (java.util.Date) target;
          GregorianCalendar start = new GregorianCalendar();
          start.setTime(date);
          GregorianCalendar end = new GregorianCalendar();
          end.setTime(date);
          end.add(java.util.Calendar.HOUR, 1);
          add(start.getTime(), end.getTime());
        } else
          showErrorNotification(MESSAGE_ALREADY_TAKEN_TITLE, MESSAGE_ALREADY_TAKEN_DESCRIPTION);

      } else if (action == deleteEventAction) {

        long localTime = System.currentTimeMillis();
        long eventTime = ((CalendarEvent) target).getStart().getTime();
        long twentyFourHoursLimit = 10800000;

        if (target instanceof CalendarEvent) {
          if (((CalendarEvent) target).getCaption().startsWith(bookingModel.userName())) {
            if (eventTime - localTime > twentyFourHoursLimit) {
              removeEvent((CalendarEvent) target);
              db.removeBooking(((CalendarEvent) target).getStart(),
                  (String) selectedDevice.getValue());
              refreshDataSources();
              showNotification(MESSAGE_ITEM_PURGED, MESSAGE_ITEM_PURGED_DESCRIPTION);
            } else if (bookingModel.getGroupID().equals("1")) { // Admin can REMOVE events
              removeEvent((CalendarEvent) target);
              db.removeBooking(((CalendarEvent) target).getStart(),
                  (String) selectedDevice.getValue());
              refreshDataSources();
              showNotification(MESSAGE_ITEM_PURGED, MESSAGE_ITEM_PURGED_DESCRIPTION_ADMIN);
            } else
              showErrorNotification(MESSAGE_24_HOURS_LIMIT, MESSAGE_24_HOURS_LIMIT_DESCRIPTION);
          } else if (bookingModel.getGroupID().equals("1")) { // Admin can REMOVE events
            removeEvent((CalendarEvent) target);
            db.removeBooking(((CalendarEvent) target).getStart(),
                (String) selectedDevice.getValue());
            refreshDataSources();
            showNotification(MESSAGE_ITEM_PURGED, MESSAGE_ITEM_PURGED_DESCRIPTION_ADMIN);
          } else {
            showErrorNotification(MESSAGE_PERMISSION_DENIED_TIME_SLOT_TITLE,
                MESSAGE_PERMISSION_DENIED_TIME_SLOT_DESCRIPTION);
          }
        } else
          showErrorNotification(MESSAGE_NOTHING_TO_DELETE_TITLE,
              MESSAGE_NOTHING_TO_DELETE_DESCRIPTION);
      } else if (action == sendEventAction) {

        Resource res =
            new ExternalResource("mailto:"
                + db.getEmailbyUserName(((CalendarEvent) target).getCaption()));

        Page.getCurrent().open(((ExternalResource) res).getURL(), null);

      } else if (action == faultyEventAction) {
        if (target instanceof CalendarEvent) {
          if (((CalendarEvent) target).getCaption().startsWith(bookingModel.userName())) {
            db.markAsFaulty(((CalendarEvent) target).getStart(), (String) selectedDevice.getValue());
            showNotification(MESSAGE_FAULTY_TITLE, MESSAGE_FAULTY_DESCRIPTION);
            refreshDataSources();
          } else {
            showErrorNotification(MESSAGE_PERMISSION_DENIED_TIME_SLOT_TITLE,
                MESSAGE_PERMISSION_DENIED_TIME_SLOT_DESCRIPTION);
          }
        }
      }
    }
  }

  class MyEventRangeSelectHandler extends MyEventHandler implements RangeSelectHandler {

    /**
     * 
     */
    private static final long serialVersionUID = -5961040298826166829L;

    public MyEventRangeSelectHandler(Calendar cal, BookingModel bookingModel) {
      super(cal, bookingModel);
    }

    @Override
    public void rangeSelect(RangeSelectEvent event) {
      add(event.getStart(), event.getEnd());
    }
  }

  class MyEventResizeHandler extends MyEventHandler implements EventResizeHandler {

    /**
     * 
     */
    private static final long serialVersionUID = -1651182125779784041L;

    public MyEventResizeHandler(Calendar cal, BookingModel bookingModel) {
      super(cal, bookingModel);
    }

    protected void setDates(com.vaadin.ui.components.calendar.event.EditableCalendarEvent event,
        Date start, Date end) {

      if (!event.getCaption().startsWith(bookingModel.userName())) {
        showErrorNotification(MESSAGE_PERMISSION_DENIED_TIME_SLOT_TITLE,
            MESSAGE_PERMISSION_DENIED_TIME_SLOT_DESCRIPTION);
        setDate(event, event.getStart(), event.getEnd());
        return;
      } else if (start.before(referenceDate) || event.getStart().before(referenceDate)) {
        showErrorNotification(MESSAGE_IN_THE_PAST_TITLE, MESSAGE_IN_THE_PAST_DESCRIPTION);
        setDate(event, event.getStart(), event.getEnd());
        return;
      }

      List<CalendarEvent> events = cal.getEvents(start, end);

      if (events.size() < 2) {
        setDate(event, start, end);
      } else if (events.size() == 2) {
        CalendarEvent overlappingEvent =
            events.get(0).equals(event) ? events.get(1) : events.get(0);
        if (start.before(overlappingEvent.getEnd()) && end.after(overlappingEvent.getEnd())) {
          setDate(event, overlappingEvent.getEnd(), end);
        } else {
          setDate(event, start, overlappingEvent.getStart());
        }
      } else {
        showError("Some other error");
        setDate(event, event.getStart(), event.getEnd());
      }
    }

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

  class MyEventMoveHandler extends MyEventHandler implements EventMoveHandler {
    private static final long serialVersionUID = -1801022623601064010L;

    public MyEventMoveHandler(Calendar cal, BookingModel bookingModel) {
      super(cal, bookingModel);
    }

    protected void setDates(com.vaadin.ui.components.calendar.event.EditableCalendarEvent event,
        Date start, Date end) {

      if (!bookingModel.getGroupID().equals("1")) {
        if (!event.getCaption().startsWith(bookingModel.userName())) {
          showErrorNotification(MESSAGE_PERMISSION_DENIED_TIME_SLOT_TITLE,
              MESSAGE_PERMISSION_DENIED_TIME_SLOT_DESCRIPTION);
          setDate(event, event.getStart(), event.getEnd());
          return;
        } else if (start.before(referenceDate) || event.getStart().before(referenceDate)) {
          showErrorNotification(MESSAGE_IN_THE_PAST_TITLE, MESSAGE_IN_THE_PAST_DESCRIPTION);
          setDate(event, event.getStart(), event.getEnd());
          return;
        }
      }

      List<CalendarEvent> events = cal.getEvents(start, end);
      if (events.size() == 0 || (events.size() == 1 && events.get(0).equals(event))) {
        setDate(event, start, end);
      } else {
        setDate(event, event.getStart(), event.getEnd());
        showErrorNotification(MESSAGE_OVERLAP_TITLE, MESSAGE_OVERLAP_DESCRIPTION);
      }
    }

    @Override
    public void eventMove(MoveEvent event) {
      CalendarEvent calendarEvent = event.getCalendarEvent();

      if (calendarEvent instanceof EditableCalendarEvent) {
        EditableCalendarEvent editableEvent = (EditableCalendarEvent) calendarEvent;
        Date newFromTime = event.getNewStart();
        long length = editableEvent.getEnd().getTime() - editableEvent.getStart().getTime();
        setDates(editableEvent, newFromTime, new Date(newFromTime.getTime() + length));
      }
    }
  }
}
