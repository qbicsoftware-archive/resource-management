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
import com.vaadin.ui.components.calendar.handler.BasicEventMoveHandler;
import com.vaadin.ui.components.calendar.handler.BasicEventResizeHandler;
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
  // private NativeSelect selectedProject;
  private NativeSelect selectedService;
  private Grid upcomingBookings;
  // private Grid next24HoursBookings;
  private Grid pastBookings;
  // private CheckBox bookMaintenance;
  // private String defaultKostenstelle;
  private TabSheet booking;
  private Grid next3HoursBookings;
  private ComboBox book4Users;

  private static Database db;

  public Booking(final BookingModel bookingModel, Date referenceDate) {

    /*
     * String[] sayHello = {"Kon'nichiwa", "Hello", "Halo", "Hiya", "Hej", "Hallo", "Hola",
     * "Grüezi", "Servus", "Merhaba", "Bonjour", "Ahoj", "Moi", "Ciao", "Buongiorno"};
     */

    this.bookingModel = bookingModel;
    this.referenceDate = referenceDate;

    Label infoLabel = new Label();
    infoLabel.addStyleName("h4");

    Label selectDeviceLabel = new Label();
    selectDeviceLabel.addStyleName("h4");
    selectDeviceLabel.setValue("Please Select a Device");

    // bookMaintenance = new CheckBox("maintenance");
    // bookMaintenance.setEnabled(true);

    book4Users = new ComboBox("Select Maintenance or User: ");
    book4Users
        .setDescription("FACS Admins can book on behalf of other users or Maintenance/Service");
    book4Users.setEnabled(true);

    final Label versionLabel = new Label();
    versionLabel.addStyleName("h4");
    versionLabel.setValue("Version 0.1.170331");

    Label countLabel = new Label();
    countLabel.addStyleName("h6");

    // showSuccessfulNotification(sayHello[(int) (Math.random() * sayHello.length)] + ", "
    // + bookingModel.userName() + "!", "");

    Date dNow = new Date();
    SimpleDateFormat ft = new SimpleDateFormat("dd.MMM.yyyy HH:mm:ss");
    System.out.println(ft.format(dNow) + "  INFO  Calendar initiated! - User: "
        + bookingModel.getLDAP() + " " + versionLabel);

    // only users who are allowed to book devices will be able to do so
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

    selectedService = new NativeSelect("Please select a Service:");
    selectedService.setDescription("Please select the service you would like to receive!");

    selectedKostenstelle = new NativeSelect("Please select Kostenstelle:");
    selectedKostenstelle.setDescription("Please select the Kostenstelle you would like to use!");

    book4Users.addItems(db.getAllUserNames());

    selectedKostenstelle.addItems(db.getKostenstelleCodes());

    // CheckBox bookAsAdmin = new CheckBox("Book as an Admin");
    // bookAsAdmin.setEnabled(true);

    selectedDevice.addValueChangeListener(new ValueChangeListener() {
      private static final long serialVersionUID = 8153818693511960689L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        versionLabel.setValue(db.getUserRoleDescByLDAPId(bookingModel.getLDAP(), getCurrentDevice()));

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
      infoLabel.setValue(bookingModel.userName() + " · Kostenstelle: "
          + bookingModel.getKostenstelle() + " · Institute: " + bookingModel.getInstitute());

    } else {
      infoLabel.setValue(bookingModel.userName() + " · Kostenstelle: "
          + bookingModel.getKostenstelle() + " · Project: " + bookingModel.getProject()
          + " · Institute: " + bookingModel.getInstitute());
    }

    // countLabel.setValue("Unconfirmed: " + db.getAllUnconfirmedCount() + " Booking(s)");
    countLabel.setValue("Unconfirmed: " + db.getAllUnconfirmedCount() + " - Total Bookings: "
        + db.getAllBookingTotalCount());

    selectedKostenstelle.select(db.getKostenstelleByLDAPId(bookingModel.getLDAP()));
    // System.out.println("Kost: " + db.getKostenstelleByLDAPId(bookingModel.getLDAP()));

    // bookDeviceLayout.addComponent(infoLabel);
    cal.setLocale(Locale.getDefault());
    cal.setImmediate(true);
    selectedService.setImmediate(true);
    cal.setSizeFull();

    String submitTitle = "Book";
    Button submit = new Button(submitTitle);
    submit.setIcon(FontAwesome.CALENDAR);
    submit.setDescription("Please select a device and a time frame at first then click 'BOOK'!");
    submit.setSizeFull();

    // submit.setVisible(false);

    submit.addClickListener(new ClickListener() {
      private static final long serialVersionUID = -3610721151565496269L;

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
      private static final long serialVersionUID = -3610721151565496269L;

      @Override
      public void buttonClick(ClickEvent event) {

        refreshDataSources();

      }
    });

    String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();

    // Image as a file resource
    FileResource resource = new FileResource(new File(basepath + "/WEB-INF/images/legend.png"));

    // Show the image in the application
    Image image = new Image("Color Legend:", resource);
    image.setSizeUndefined();
    // image.setWidth("100%");
    // Let the user view the file in browser or download it
    // Link link = new Link("Link to the image file", resource);
    gridLayout.setWidth("100%");
    // add components to the grid layout
    gridLayout.addComponent(infoLabel, 0, 4, 2, 4);
    gridLayout.addComponent(versionLabel, 0, 5);

    // gridLayout.addComponent(selectDeviceLabel,0,1);
    gridLayout.addComponent(selectedDevice, 0, 0);
    gridLayout.addComponent(selectedService, 1, 0);
    gridLayout.addComponent(selectedKostenstelle, 2, 0);
    // gridLayout.addComponent(bookAsAdmin, 3, 0);
    selectedService.setVisible(false);

    gridLayout.addComponent(cal, 0, 2, 5, 2);
    gridLayout.addComponent(refresh, 0, 3);
    gridLayout.addComponent(submit, 1, 3, 5, 3);
    gridLayout.addComponent(image, 0, 6, 2, 6);

    // gridLayout.addComponent(bookMaintenance, 3, 4);
    gridLayout.addComponent(book4Users, 4, 4);

    gridLayout.addComponent(countLabel, 3, 0);

    gridLayout.setMargin(true);
    gridLayout.setSpacing(true);
    gridLayout.setSizeFull();

    book.setContent(gridLayout);
    booking = new TabSheet();
    booking.addStyleName(ValoTheme.TABSHEET_FRAMED);
    booking.addTab(book).setCaption("Calendar");
    booking.addTab(myNext3HoursBookings()).setCaption("Next 3 Hours");
    booking.addTab(myUpcomingBookings()).setCaption("Upcoming");
    booking.addTab(myPastBookings()).setCaption("Past Bookings");
    // booking.addTab(myUpcomingBookingsSQLContainer()).setCaption("Test");
    setCompositionRoot(booking);

    if (bookingModel.getAdminAccess() != 1) {
      // bookMaintenance.setEnabled(false);
      book4Users.setEnabled(false);

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
    // devicesLayout.setCaption("My Bookings");
    // there will now be space around the test component
    // components added to the test component will now not stick together but have space between
    // them
    devicesLayout.setMargin(true);
    devicesLayout.setSpacing(true);

    Date serverTime = new WebBrowser().getCurrentDate();
    Date nextDayTime = new Date(serverTime.getTime() + (1000 * 60 * 60 * 3));

    BeanItemContainer<BookingBean> users =
        getMyNext3HoursBookings(bookingModel.getLDAP(), serverTime, nextDayTime);
    // System.out.println(bookingModel.getLDAP());

    GeneratedPropertyContainer gpc = new GeneratedPropertyContainer(users);

    next3HoursBookings = new Grid(gpc);
    // Create a grid
    next3HoursBookings.setStyleName("my-style");
    next3HoursBookings.setWidth("100%");
    next3HoursBookings.setSelectionMode(SelectionMode.SINGLE);
    next3HoursBookings.setEditorEnabled(false);

    next3HoursBookings.setColumnOrder("ID", "confirmation", "deviceName", "service", "start",
        "end", "username", "phone", "price");
    next3HoursBookings.getColumn("price").setHeaderCaption("Approx. Price");
    setRenderers(next3HoursBookings);
    devicesLayout.addComponent(next3HoursBookings);

    return devicesLayout;
  }

  private Component myPastBookings() {
    VerticalLayout devicesLayout = new VerticalLayout();
    // devicesLayout.setCaption("My Bookings");
    // there will now be space around the test component
    // components added to the test component will now not stick together but have space between
    // them
    devicesLayout.setMargin(true);
    devicesLayout.setSpacing(true);

    Date serverTime = new WebBrowser().getCurrentDate();

    BeanItemContainer<BookingBean> users = getMyPastBookings(bookingModel.getLDAP(), serverTime);
    // System.out.println(bookingModel.getLDAP());

    GeneratedPropertyContainer gpc = new GeneratedPropertyContainer(users);

    pastBookings = new Grid(gpc);
    // Create a grid
    pastBookings.setStyleName("my-style");
    pastBookings.setWidth("100%");
    pastBookings.setSelectionMode(SelectionMode.SINGLE);
    pastBookings.setEditorEnabled(false);

    pastBookings.setColumnOrder("ID", "confirmation", "deviceName", "service", "start", "end",
        "username", "phone", "price");

    pastBookings.getColumn("price").setHeaderCaption("Approx. Price");

    setRenderers(pastBookings);
    devicesLayout.addComponent(pastBookings);

    return devicesLayout;
  }

  /*
   * private Component myUpcomingBookingsSQLContainer() {
   * 
   * 
   * VerticalLayout devicesLayout = new VerticalLayout(); //
   * devicesLayout.setCaption("My Bookings"); // there will now be space around the test component
   * // components added to the test component will now not stick together but have space between //
   * them devicesLayout.setMargin(true); devicesLayout.setSpacing(true);
   * 
   * Date serverTime = new WebBrowser().getCurrentDate(); Date nextDayTime = new
   * Date(serverTime.getTime() + (1000 * 60 * 60 * 3));
   * 
   * try { TableQuery tq = new TableQuery("booking", DBManager.getDatabaseInstanceAlternative());
   * tq.setVersionColumn("OPTLOCK"); SQLContainer container = new SQLContainer(tq);
   * 
   * // System.out.println("Print Container: " + container.size());
   * container.setAutoCommit(isEnabled());
   * 
   * upcomingBookings = new Grid(container);
   * 
   * FieldGroup fieldGroup = upcomingBookings.getEditorFieldGroup(); fieldGroup.addCommitHandler(new
   * FieldGroup.CommitHandler() {
   * 
   * private static final long serialVersionUID = 3799806709907688919L;
   * 
   * 
   * 
   * @Override public void preCommit(FieldGroup.CommitEvent commitEvent) throws
   * FieldGroup.CommitException {
   * 
   * }
   * 
   * @Override public void postCommit(FieldGroup.CommitEvent commitEvent) throws
   * FieldGroup.CommitException {
   * 
   * Notification( "Successfully Updated",
   * "Selected values are updated in the database. If it was a mistake, please remind that there is no 'undo' functionality yet."
   * , "success");
   * 
   * refreshGrid(); }
   * 
   * private void refreshGrid() { container.refresh(); }
   * 
   * });
   * 
   * } catch (Exception e) { // TODO Auto-generated catch block Notification(
   * "Something went wrong!",
   * "Unable to update/connect the database. There may be a connection problem, please check your internet connection settings then try it again."
   * , "error"); e.printStackTrace(); }
   * 
   * upcomingBookings.clearSortOrder();
   * 
   * upcomingBookings.setStyleName("my-style"); upcomingBookings.setWidth("100%");
   * upcomingBookings.setSelectionMode(SelectionMode.SINGLE);
   * upcomingBookings.setEditorEnabled(false);
   * 
   * devicesLayout.addComponent(upcomingBookings);
   * 
   * // TODO filtering // HeaderRow filterRow = devicesGrid.prependHeaderRow();
   * 
   * return devicesLayout; }
   */

  private Component myUpcomingBookings() {
    VerticalLayout devicesLayout = new VerticalLayout();
    // devicesLayout.setCaption("My Bookings");
    // there will now be space around the test component
    // components added to the test component will now not stick together but have space between
    // them
    devicesLayout.setMargin(true);
    devicesLayout.setSpacing(true);

    Date serverTime = new WebBrowser().getCurrentDate();
    Date nextDayTime = new Date(serverTime.getTime() + (1000 * 60 * 60 * 3));

    BeanItemContainer<BookingBean> users =
        getMyUpcomingBookings(bookingModel.getLDAP(), nextDayTime);
    // System.out.println(bookingModel.getLDAP());

    GeneratedPropertyContainer gpc = new GeneratedPropertyContainer(users);
    gpc.addGeneratedProperty("delete", new PropertyValueGenerator<String>() {
      /**
       * 
       */
      private static final long serialVersionUID = 1263377339178640406L;

      @Override
      public String getValue(Item item, Object itemId, Object propertyId) {
        // return FontAwesome.TRASH_O.getHtml(); // The caption
        return "Trash"; // The caption

      }

      @Override
      public Class<String> getType() {
        return String.class;
      }
    });


    /*
     * try {
     * 
     * FreeformQuery query = new FreeformQuery(
     * "SELECT * FROM booking INNER JOIN user ON booking.user_ldap = user.user_ldap WHERE deleted IS NULL AND booking.user_ldap ='"
     * + bookingModel.getLDAP() + "';", DBManager.getDatabaseInstanceAlternative(), "booking_id");
     * SQLContainer container = new SQLContainer(query);
     * 
     * // System.out.println("Print Container: " + container.size());
     * container.setAutoCommit(isEnabled());
     * 
     * myBookings = new Grid(container);
     * 
     * } catch (Exception e) { e.printStackTrace(); }
     * 
     * myBookings.setColumnOrder("booking_id", "confirmation", "device_name", "service", "start",
     * "end", "kostenstelle", "price", "project");
     * 
     * myBookings.removeColumn("user_ldap"); myBookings.removeColumn("timestamp");
     * myBookings.removeColumn("deleted"); myBookings.removeColumn("user_name");
     * myBookings.removeColumn("group_id"); myBookings.removeColumn("workgroup_id");
     * myBookings.removeColumn("email"); myBookings.removeColumn("phone");
     * myBookings.removeColumn("admin_panel"); myBookings.removeColumn("user_id");
     * 
     * myBookings.getColumn("booking_id").setHeaderCaption("Booking ID");
     */

    upcomingBookings = new Grid(gpc);
    // Create a grid
    upcomingBookings.setStyleName("my-style");
    upcomingBookings.setWidth("100%");
    upcomingBookings.setSelectionMode(SelectionMode.SINGLE);
    upcomingBookings.setEditorEnabled(false);

    upcomingBookings.setColumnOrder("ID", "confirmation", "deviceName", "service", "start", "end",
        "username", "phone", "price");
    upcomingBookings.getColumn("price").setHeaderCaption("Approx. Price");

    // System.out.println(myBookings.getColumns());
    setRenderers(upcomingBookings);
    devicesLayout.addComponent(upcomingBookings);

    upcomingBookings.getColumn("delete").setRenderer(
        new ButtonRenderer(new ClickableRenderer.RendererClickListener() {
          /**
           * 
           */
          private static final long serialVersionUID = 302628105070456680L;


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
                private static final long serialVersionUID = 1778157399909757369L;

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
                private static final long serialVersionUID = -8957620319158438769L;

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


    // TODO filtering
    // HeaderRow filterRow = devicesGrid.prependHeaderRow();

    return devicesLayout;
  }

  /*
   * private BeanItemContainer<BookingBean> getBookings(String LDAP) {
   * BeanItemContainer<BookingBean> bookingList = new
   * BeanItemContainer<BookingBean>(BookingBean.class); List<BookingBean> bookings =
   * DBManager.getDatabaseInstance().getMyBookingsGrid(LDAP); assert bookings != null;
   * bookingList.addAll(bookings); return bookingList; }
   */

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

  /*
   * private void Notification(String title, String description, String type) { Notification notify
   * = new Notification(title, description); notify.setPosition(Position.TOP_CENTER); if
   * (type.equals("error")) { notify.setDelayMsec(16000); notify.setIcon(FontAwesome.FROWN_O);
   * notify.setStyleName(ValoTheme.NOTIFICATION_ERROR + " " + ValoTheme.NOTIFICATION_CLOSABLE); }
   * else if (type.equals("success")) { notify.setDelayMsec(8000);
   * notify.setIcon(FontAwesome.SMILE_O); notify.setStyleName(ValoTheme.NOTIFICATION_SUCCESS + " " +
   * ValoTheme.NOTIFICATION_CLOSABLE); } else { notify.setDelayMsec(8000);
   * notify.setIcon(FontAwesome.MEH_O); notify.setStyleName(ValoTheme.NOTIFICATION_TRAY + " " +
   * ValoTheme.NOTIFICATION_CLOSABLE); } notify.show(Page.getCurrent()); }
   */

  /*
   * public boolean showConfirmDialog(String title, String description) {
   * 
   * Boolean confirmed = false;
   * 
   * Label Description = new Label(description); Button confirm = new Button("Yes"); Button cancel =
   * new Button("No");
   * 
   * ConfirmDialog x = new ConfirmDialog();
   * 
   * ConfirmDialog d = getFactory().create(windowCaption, message, okCaption, cancelCaption);
   * 
   * GridLayout dialogLayout = new GridLayout(3, 3);
   * 
   * x.setCaption(" " + title);
   * 
   * dialogLayout.addComponent(Description, 0, 0); dialogLayout.addComponent(cancel, 1, 1);
   * dialogLayout.addComponent(confirm, 2, 1);
   * 
   * dialogLayout.setSpacing(true); dialogLayout.setMargin(true);
   * 
   * x.setIcon(FontAwesome.WARNING); x.setHeight("200px"); x.setWidth("450px");
   * x.setResizable(false); x.setContent(dialogLayout);
   * 
   * }
   */

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
      // TODO log failed operation
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

      // System.out.println("I am here: True - " + db.getDeviceRestriction(currentDevice));

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
        description += " new booking(s) for device ";
        description += entry.getKey();
        description +=
            ". \nPlease keep in mind that Aria, Mac and Consulting requests has to be confirmed by FACS Facility managers.";
        for (CalendarEvent event : entry.getValue()) {
          if (event instanceof BasicEvent) {
            // User user;
            try {
              // user =
              // UserLocalServiceUtil.getUser(Long.parseLong(VaadinService.getCurrent().getCurrentRequest().getRemoteUser()));

              long s = event.getStart().getTime();
              long e = event.getEnd().getTime();
              long duration = e - s;

              ((BasicEvent) event).setStyleName("color2");
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
            } catch (NumberFormatException e) {
              e.printStackTrace();
            }

          }
        }
      }
      showSuccessfulNotification(title, description);
    } else {

      // System.out.println("I am here: False? - "+db.getDeviceRestriction(currentDevice));

      Iterator<Entry<String, Set<CalendarEvent>>> it = newEvents.entrySet().iterator();

      String title = "Booking completed!";
      String description = "Congratulations!\nYou've succesfully added ";
      while (it.hasNext()) {
        Entry<String, Set<CalendarEvent>> entry = it.next();
        description += entry.getValue().size();
        description += " new booking(s) for device ";
        description += entry.getKey();
        description +=
            ". \nPlease keep in mind that Aria, Mac and Consulting requests has to be confirmed by FACS Facility managers.";
        for (CalendarEvent event : entry.getValue()) {
          if (event instanceof BasicEvent) {
            // User user;
            try {
              // user =
              // UserLocalServiceUtil.getUser(Long.parseLong(VaadinService.getCurrent().getCurrentRequest().getRemoteUser()));

              long s = event.getStart().getTime();
              long e = event.getEnd().getTime();
              long duration = e - s;

              ((BasicEvent) event).setStyleName("color2");
              if (db.getUserRoleByLDAPId(user_ldap, currentDevice).equals("V")) {

                // System.out.println("Current User: V");

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
              } else

              // admins can book slots for maintenance/service
              if (book4Users.getValue() != null) {

                String userLDAPId = db.getUserLDAPIDbyUserName(book4Users.getValue().toString());
                // System.out.println("maintenance selected!: " + book4Users.getValue()
                // + " toString: " + book4Users.getValue().toString());

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

                // System.out.println("Booking Triggered!");

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
    // newEvents.get(getCurrentDevice()).remove(event);
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

    boolean add(Date start, Date end) {

      Date startX = new Date();
      startX.setTime(start.getTime() + 1);

      Date endX = new Date();
      endX.setTime(end.getTime() - 1);

      if (start.before(referenceDate)) {
        showErrorNotification(MESSAGE_IN_THE_PAST_TITLE, MESSAGE_IN_THE_PAST_DESCRIPTION);
        // System.out.println("Test: " + start + " End: " + end + " Size: "
        // + cal.getEvents(start, end).size());
      } else if (cal.getEvents(startX, endX).size() > 0) {
        // System.out.println("Test: " + start + " End: " + end + " Size: "
        // + cal.getEvents(startX, endX).size());
        // System.out.println("StartX: " + startX + " End: " + endX);
        showErrorNotification(MESSAGE_ALREADY_TAKEN_TITLE, MESSAGE_ALREADY_TAKEN_DESCRIPTION);
      } else {
        addEvent(start, end);
        // System.out.println("Test: " + start + " End: " + end + " Size: "
        // + cal.getEvents(start, end).size());
        return true;
      }
      return false;
    }

    void setDate(com.vaadin.ui.components.calendar.event.EditableCalendarEvent event, Date start,
        Date end) {
      event.setStart(start);
      event.setEnd(end);
      event.setStyleName("color2");
      // System.out.println("Start: " + start +" End: " + end);
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

    void showError(String message) {
      Notification.show(message, Type.ERROR_MESSAGE);
    }
  }

  /**
   * context menu
   */
  class MyActionHandler extends MyEventHandler implements Action.Handler {
    private static final long serialVersionUID = -9160597832514677833L;

    public MyActionHandler(Calendar cal, BookingModel bookingModel) {
      super(cal, bookingModel);
    }

    Action addEventAction = new Action("Create Booking");
    Action deleteEventAction = new Action("Delete this booking");
    Action sendEventAction = new Action("Send an e-mail");
    Action faultyEventAction = new Action("Mark booking as 'faulty'");

    // Action editEventAction = new Action("Edit Booking");

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
        return new Action[] {deleteEventAction, sendEventAction, faultyEventAction};
      // return new Action[] {addEventAction, deleteEventAction, editEventAction};
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
          showErrorNotification(MESSAGE_ALREADY_TAKEN_TITLE, MESSAGE_ALREADY_TAKEN_DESCRIPTION);

      } else if (action == deleteEventAction) {
        // Check if the action was clicked on top of an event

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
              // TODO: ask for confirmation
            } else
              showErrorNotification(MESSAGE_24_HOURS_LIMIT, MESSAGE_24_HOURS_LIMIT_DESCRIPTION);
            // TODO: ask for confirmation
          } else if (bookingModel.getGroupID().equals("1")) { // Admin can REMOVE events
            removeEvent((CalendarEvent) target);
            db.removeBooking(((CalendarEvent) target).getStart(),
                (String) selectedDevice.getValue());
            refreshDataSources();
            showNotification(MESSAGE_ITEM_PURGED, MESSAGE_ITEM_PURGED_DESCRIPTION_ADMIN);
            // TODO: ask for confirmation
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

        // if subject line is necessary replace the lines above with this
        // new ExternalResource("mailto:"
        // + db.getEmailbyUserName(((CalendarEvent)
        // target).getCaption())+"?subject=your Flow Cytometry booking");

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

  class MyEventResizeHandler extends MyEventHandler implements EventResizeHandler {
    private static final long serialVersionUID = -1651182125779784041L;

    public MyEventResizeHandler(Calendar cal, BookingModel bookingModel) {
      super(cal, bookingModel);
    }

    protected void setDates(com.vaadin.ui.components.calendar.event.EditableCalendarEvent event,
        Date start, Date end) {

      if (!event.getCaption().startsWith(bookingModel.userName())) {
        showErrorNotification(MESSAGE_PERMISSION_DENIED_TIME_SLOT_TITLE,
            MESSAGE_PERMISSION_DENIED_TIME_SLOT_DESCRIPTION);
        // set original dates, so that it updates immediately on client side
        setDate(event, event.getStart(), event.getEnd());
        return;
      } else if (start.before(referenceDate) || event.getStart().before(referenceDate)) {
        showErrorNotification(MESSAGE_IN_THE_PAST_TITLE, MESSAGE_IN_THE_PAST_DESCRIPTION);
        setDate(event, event.getStart(), event.getEnd());
        return;
      }
      // do only allow to resize if no other events are overwritten. == 1 because the event itself
      // is found, when resizing

      List<CalendarEvent> events = cal.getEvents(start, end);
      // if(events.size() )

      if (events.size() < 2) {
        setDate(event, start, end);
      }// overlap with one. append to other event
      else if (events.size() == 2) {
        CalendarEvent overlappingEvent =
            events.get(0).equals(event) ? events.get(1) : events.get(0);
        if (start.before(overlappingEvent.getEnd()) && end.after(overlappingEvent.getEnd())) {
          setDate(event, overlappingEvent.getEnd(), end);
        } else {
          setDate(event, start, overlappingEvent.getStart());
        }
      } else {
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
  class MyEventMoveHandler extends MyEventHandler implements EventMoveHandler {
    private static final long serialVersionUID = -1801022623601064010L;

    public MyEventMoveHandler(Calendar cal, BookingModel bookingModel) {
      super(cal, bookingModel);
    }

    /**
     * handle resizing and moving of events
     */
    protected void setDates(com.vaadin.ui.components.calendar.event.EditableCalendarEvent event,
        Date start, Date end) {

      if (!bookingModel.getGroupID().equals("1")) {
        if (!event.getCaption().startsWith(bookingModel.userName())) {
          showErrorNotification(MESSAGE_PERMISSION_DENIED_TIME_SLOT_TITLE,
              MESSAGE_PERMISSION_DENIED_TIME_SLOT_DESCRIPTION);
          // set original dates, so that it updates immediately on client side
          setDate(event, event.getStart(), event.getEnd());
          return;
        } else if (start.before(referenceDate) || event.getStart().before(referenceDate)) {
          showErrorNotification(MESSAGE_IN_THE_PAST_TITLE, MESSAGE_IN_THE_PAST_DESCRIPTION);
          setDate(event, event.getStart(), event.getEnd());
          return;
        }
      }
      // do only allow to move if it does not overlap with any other event
      // System.out.println("move: "+ cal.getEvents(start, end).size());
      List<CalendarEvent> events = cal.getEvents(start, end);
      if (events.size() == 0 || (events.size() == 1 && events.get(0).equals(event))) {
        setDate(event, start, end);
        // System.out.println("BookingID: "+ event.getCaption());
      } else {
        // set original dates, so that it updates immediately on client side
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
        // Update event dates
        long length = editableEvent.getEnd().getTime() - editableEvent.getStart().getTime();
        setDates(editableEvent, newFromTime, new Date(newFromTime.getTime() + length));
      }
    }
  }
}
