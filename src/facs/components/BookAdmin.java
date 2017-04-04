/*******************************************************************************
 * QBiC Calendar provides an infrastructure for defining calendars for specific purposes like
 * booking devices or planning resources for services and integration of relevant data into the
 * common portal infrastructure. Copyright (C) 2016 Aydın Can Polatkan
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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.liferay.portal.model.User;
import com.vaadin.data.Item;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.data.util.converter.StringToBigDecimalConverter;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickEvent;
import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.renderers.NumberRenderer;
import com.vaadin.ui.themes.ValoTheme;

import de.uni_tuebingen.qbic.main.LiferayAndVaadinUtils;
import facs.db.DBManager;
import facs.model.BookingBean;

public class BookAdmin extends CustomComponent {
  private static final long serialVersionUID = 2183973381935176872L;
  // private Grid devicesGrid;
  private Grid devicesGridConfirm;
  private Grid devicesGridTrash;

  private Map<String, Grid> gridMap = new HashMap<String, Grid>();

  private GridLayout gridLayout = new GridLayout(6, 6);

  private ListSelect userDevice;
  private ListSelect userGroup;
  private ListSelect userRole;

  public BookAdmin(User user) {

    Date dNow = new Date();
    SimpleDateFormat ft = new SimpleDateFormat("dd.MMM.yyyy HH:mm:ss");
    System.out.println(ft.format(dNow) + "  INFO  Calendar Admin accessed! - User: "
        + LiferayAndVaadinUtils.getUser().getScreenName());

    Label infoLabel =
        new Label(DBManager.getDatabaseInstance().getUserNameByUserID(
            LiferayAndVaadinUtils.getUser().getScreenName())
            + " · " + LiferayAndVaadinUtils.getUser().getScreenName());
    infoLabel.addStyleName("h4");

    String buttonRefreshTitle = "Refresh";
    Button refresh = new Button(buttonRefreshTitle);
    refresh.setIcon(FontAwesome.REFRESH);
    refresh.setSizeFull();
    refresh.setDescription("Click here to reload the data from the database!");
    refresh.addStyleName(ValoTheme.BUTTON_FRIENDLY);

    String buttonUpdateTitle = "Update";
    Button updateUser = new Button(buttonUpdateTitle);
    updateUser.setIcon(FontAwesome.WRENCH);
    updateUser.setSizeFull();
    updateUser.setDescription("Click here to update your user role and group!");

    userDevice = new ListSelect("Select Instrument");
    userDevice.addItems(DBManager.getDatabaseInstance().getDeviceNames());
    userDevice.setRows(6);
    userDevice.setNullSelectionAllowed(false);
    userDevice.setSizeFull();
    userDevice.setImmediate(true);
    /*
     * userDevice.addValueChangeListener(e -> Notification.show("Device:",
     * String.valueOf(e.getProperty().getValue()), Type.TRAY_NOTIFICATION));
     */
    userGroup = new ListSelect("Select User Group");
    userGroup.addItems(DBManager.getDatabaseInstance().getUserGroups());
    userGroup.setRows(6);
    userGroup.setNullSelectionAllowed(false);
    userGroup.setSizeFull();
    userGroup.setImmediate(true);
    /*
     * userGroup.addValueChangeListener(e -> Notification.show("User Group:",
     * String.valueOf(e.getProperty().getValue()), Type.TRAY_NOTIFICATION));
     */
    userRole = new ListSelect("Select User Role");
    userRole.addItems(DBManager.getDatabaseInstance().getUserRoles());
    userRole.setRows(6);
    userRole.setNullSelectionAllowed(false);
    userRole.setSizeFull();
    userRole.setImmediate(true);
    /*
     * userRole.addValueChangeListener(e -> Notification.show("User Role:",
     * String.valueOf(e.getProperty().getValue()), Type.TRAY_NOTIFICATION));
     */
    refresh.addClickListener(new ClickListener() {
      private static final long serialVersionUID = -3610721151565496269L;

      @Override
      public void buttonClick(ClickEvent event) {
        refreshDataSources();
      }
    });

    updateUser.addClickListener(new ClickListener() {
      private static final long serialVersionUID = -3610721151565496909L;

      @Override
      public void buttonClick(ClickEvent event) {
        try {
          if (userDevice.getValue().equals(null) || userRole.getValue().equals(null)
              || userGroup.getValue().equals(null)) {
            showErrorNotification(
                "Something's missing!",
                "Please make sure that you selected an Instrument, a Role and a Group! Each list has to have one highligthed option.'.");
            // System.out.println("Device: "+userDevice.getValue()+" Group: "+userGroup.getValue()+" Role: "+userRole.getValue());
          } else {
            DBManager.getDatabaseInstance()
                .getShitDone(
                    DBManager.getDatabaseInstance().getUserRoleIDbyDesc(
                        userRole.getValue().toString()),
                    DBManager.getDatabaseInstance().getUserIDbyLDAPID(
                        LiferayAndVaadinUtils.getUser().getScreenName()),
                    DBManager.getDatabaseInstance().getDeviceIDByName(
                        userDevice.getValue().toString()));

            DBManager.getDatabaseInstance().getShitDoneAgain(
                DBManager.getDatabaseInstance().getUserGroupIDByName(
                    userGroup.getValue().toString()),
                LiferayAndVaadinUtils.getUser().getScreenName());

          }
        } catch (Exception e) {
          showErrorNotification(
              "Something's missing!",
              "Please make sure that you selected an Instrument, a Role and a Group! Each list has to have one highligthed option.'.");
        }
        refreshDataSources();
      }
    });

    // only admins are allowed to see the admin panel ;)
    if (!DBManager.getDatabaseInstance()
        .getUserAdminPanelAccessByLDAPId(LiferayAndVaadinUtils.getUser().getScreenName())
        .equals("1")) {
      VerticalLayout errorLayout = new VerticalLayout();
      infoLabel.setValue("ACCESS DENIED");
      errorLayout.addComponent(infoLabel);
      showErrorNotification(
          "Access Denied!",
          "Sorry, you're not allowed to see anything here, at least your username told us so. Do you need assistance? Please contact 'helpdesk@qbic.uni-tuebingen.de'.");
      setCompositionRoot(errorLayout);
      return;
    }

    this.setCaption("Admin");

    final TabSheet bookAdmin = new TabSheet();
    bookAdmin.addStyleName(ValoTheme.TABSHEET_FRAMED);
    bookAdmin.addStyleName(ValoTheme.TABSHEET_EQUAL_WIDTH_TABS);

    ArrayList<String> deviceNames = new ArrayList<String>();
    deviceNames = DBManager.getDatabaseInstance().getDeviceNames();

    bookAdmin.addTab(awaitingRequestsGrid());

    for (int i = 0; i < deviceNames.size(); i++) {
      bookAdmin.addTab(newDeviceGrid(deviceNames.get(i)));
    }

    bookAdmin.addTab(deletedBookingsGrid());

    bookAdmin.addSelectedTabChangeListener(new SelectedTabChangeListener() {

      /**
       * 
       */
      private static final long serialVersionUID = 8987818794404251063L;

      @Override
      public void selectedTabChange(SelectedTabChangeEvent event) {
        userDevice.select(bookAdmin.getSelectedTab().getCaption());
        userRole.select(DBManager.getDatabaseInstance().getUserGroupDescriptionByLDAPId(
            LiferayAndVaadinUtils.getUser().getScreenName(),
            DBManager.getDatabaseInstance().getDeviceIDByName(
                bookAdmin.getSelectedTab().getCaption())));
        userGroup.select(DBManager.getDatabaseInstance().getUserRoleNameByLDAPId(
            LiferayAndVaadinUtils.getUser().getScreenName()));

      }
    });

    gridLayout.setWidth("100%");

    // add components to the grid layout
    // gridLayout.addComponent(infoLabel, 0, 0, 3, 0);
    gridLayout.addComponent(bookAdmin, 0, 1, 5, 1);
    gridLayout.addComponent(refresh, 0, 2);
    gridLayout.addComponent(userDevice, 0, 4, 1, 4);
    gridLayout.addComponent(userRole, 2, 4, 3, 4);
    gridLayout.addComponent(userGroup, 4, 4, 5, 4);
    gridLayout.addComponent(updateUser, 0, 5, 5, 5);
    gridLayout.setSizeFull();

    gridLayout.setSpacing(true);
    setCompositionRoot(gridLayout);

    /*
     * JavaScript to update the Grid try { JDBCConnectionPool connectionPool = new
     * SimpleJDBCConnectionPool("com.mysql.jdbc.Driver",
     * "jdbc:mysql://localhost:8889/facs_facility", "facs", "facs"); QueryDelegate qd = new
     * FreeformQuery("select * from facs_facility", connectionPool, "id"); final SQLContainer c =
     * new SQLContainer(qd); bookAdmin.setContainerDataSource(c); }
     * 
     * JavaScript.getCurrent().execute("setInterval(function(){refreshTable();},5000);");
     * JavaScript.getCurrent().addFunction("refreshTable", new JavaScriptFunction() {
     * 
     * @Override public void call(JsonArray arguments) { // TODO Auto-generated method stub
     * 
     * } });
     */
  }

  private Component newDeviceGrid(final String deviceName) {
    VerticalLayout devicesLayout = new VerticalLayout();
    devicesLayout.setCaption(deviceName);
    // HorizontalLayout buttonLayout = new HorizontalLayout();

    // there will now be space around the test component
    // components added to the test component will now not stick together but have space between
    // them
    devicesLayout.setMargin(true);
    devicesLayout.setSpacing(true);

    BeanItemContainer<BookingBean> booking = getBookingList(deviceName);

    GeneratedPropertyContainer gpc = new GeneratedPropertyContainer(booking);
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

    gridMap.put(deviceName, new Grid(gpc));
    // Create a grid

    gridMap.get(deviceName).setWidth("100%");
    gridMap.get(deviceName).setSelectionMode(SelectionMode.SINGLE);
    gridMap.get(deviceName).getColumn("delete").setRenderer(new HtmlRenderer());
    gridMap.get(deviceName).removeColumn("confirmation");
    gridMap.get(deviceName).removeColumn("deviceName");
    setRenderers(gridMap.get(deviceName));
    gridMap.get(deviceName).setColumnOrder("ID", "service", "start", "end", "username", "phone",
        "price");

    // Render a button that deletes the data row (item)
    gridMap.get(deviceName).getColumn("delete")
        .setRenderer(new ButtonRenderer(new ClickableRenderer.RendererClickListener() {
          /**
           * 
           */
          private static final long serialVersionUID = 3544662150370497152L;

          @Override
          public void click(RendererClickEvent event) {
            removeBooking(gridMap.get(deviceName), (BookingBean) event.getItemId());
          }
        }));

    // devicesGrid.setEditorEnabled(true);

    // devicesLayout.addComponent(buttonLayout);
    devicesLayout.addComponent(gridMap.get(deviceName));

    // TODO filtering
    // HeaderRow filterRow = devicesGrid.prependHeaderRow();

    return devicesLayout;
  }

  private Component deletedBookingsGrid() {
    VerticalLayout devicesLayout = new VerticalLayout();
    devicesLayout.setCaption("Trash");
    // HorizontalLayout buttonLayout = new HorizontalLayout();

    // there will now be space around the test component
    // components added to the test component will now not stick together but have space between
    // them
    devicesLayout.setMargin(true);
    devicesLayout.setSpacing(true);
    // buttonLayout.setMargin(true);
    // buttonLayout.setSpacing(true);

    // buttonLayout.addComponent(add);

    BeanItemContainer<BookingBean> booking = getDeletedBookings();

    GeneratedPropertyContainer gpc = new GeneratedPropertyContainer(booking);

    gpc.addGeneratedProperty("restore", new PropertyValueGenerator<String>() {
      /**
       * 
       */
      private static final long serialVersionUID = 4082425701384202280L;

      @Override
      public String getValue(Item item, Object itemId, Object propertyId) {
        // return FontAwesome.TRASH_O.getHtml(); // The caption
        return "Restore"; // The caption

      }

      @Override
      public Class<String> getType() {
        return String.class;
      }
    });


    gpc.addGeneratedProperty("delete", new PropertyValueGenerator<String>() {
      /**
       * 
       */
      private static final long serialVersionUID = 1307493624895857513L;

      @Override
      public String getValue(Item item, Object itemId, Object propertyId) {
        // return FontAwesome.TRASH_O.getHtml(); // The caption
        return "Purge"; // The caption

      }

      @Override
      public Class<String> getType() {
        return String.class;
      }
    });


    devicesGridTrash = new Grid(gpc);
    // Create a grid

    devicesGridTrash.setWidth("100%");
    devicesGridTrash.setSelectionMode(SelectionMode.SINGLE);
    devicesGridTrash.getColumn("delete").setRenderer(new HtmlRenderer());
    devicesGridTrash.getColumn("restore").setRenderer(new HtmlRenderer());
    setRenderers(devicesGridTrash);
    devicesGridTrash.setColumnOrder("ID", "deviceName", "service", "start", "end", "username",
        "phone", "price");
    devicesGridTrash.getColumn("deviceName").setHeaderCaption("Instrument");

    // Render a button that deletes the data row (item)

    /*
     * devicesGrid.addColumn("delete", FontIcon.class).setWidth(35) .setRenderer(new
     * FontIconRenderer(new RendererClickListener() {
     * 
     * @Override public void click(RendererClickEvent e) { Notification.show("Deleted item " +
     * e.getItemId()); } }));
     */

    devicesGridTrash.getColumn("delete").setRenderer(
        new ButtonRenderer(new ClickableRenderer.RendererClickListener() {
          /**
           * 
           */
          private static final long serialVersionUID = 302628105070456680L;

          @Override
          public void click(RendererClickEvent event) {

            try {

              Window cd = new Window("Purge Booking");

              cd.setHeight("200px");
              cd.setWidth("400px");
              cd.setResizable(false);

              GridLayout dialogLayout = new GridLayout(3, 3);

              Button okButton = new Button("Yes");
              okButton.addStyleName(ValoTheme.BUTTON_DANGER);
              Button cancelButton = new Button("No, I'm actually not sure!");
              cancelButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
              Label information = new Label("Are you sure you want to purge this item?");
              information.addStyleName(ValoTheme.LABEL_NO_MARGIN);

              okButton.addClickListener(new Button.ClickListener() {


                /**
                 * 
                 */
                private static final long serialVersionUID = 3739260172118651857L;

                @Override
                public void buttonClick(ClickEvent okEvent) {
                  purgeBooking((BookingBean) event.getItemId());
                  cd.close();
                  Notification("The booking was purged!",
                      "At the end, you are the admin, you have the power.", "");
                }
              });

              cancelButton.addClickListener(new Button.ClickListener() {


                /**
                 * 
                 */
                private static final long serialVersionUID = -3931200823633220160L;

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



            FieldGroup fieldGroup = devicesGridTrash.getEditorFieldGroup();
            fieldGroup.addCommitHandler(new FieldGroup.CommitHandler() {
              /**
               * 
               */
              private static final long serialVersionUID = 3799806709907688919L;



              @Override
              public void preCommit(FieldGroup.CommitEvent commitEvent)
                  throws FieldGroup.CommitException {

              }

              @Override
              public void postCommit(FieldGroup.CommitEvent commitEvent)
                  throws FieldGroup.CommitException {

                Notification(
                    "Successfully Updated",
                    "Selected values are updated in the database. If it was a mistake, please remind that there is no 'undo' functionality yet.",
                    "success");

                refreshGrid();
              }

              private void refreshGrid() {
                getDeletedBookings();
              }

            });

          }
        }));

    devicesGridTrash.getColumn("restore").setRenderer(
        new ButtonRenderer(new ClickableRenderer.RendererClickListener() {
          /**
           * 
           */
          private static final long serialVersionUID = -9104571186503913834L;

          @Override
          public void click(RendererClickEvent event) {
            restoreBooking((BookingBean) event.getItemId());
          }
        }));

    // devicesGrid.setEditorEnabled(true);

    // devicesLayout.addComponent(buttonLayout);
    devicesLayout.addComponent(devicesGridTrash);

    // TODO filtering
    // HeaderRow filterRow = devicesGrid.prependHeaderRow();

    return devicesLayout;
  }


  private Component awaitingRequestsGrid() {
    VerticalLayout devicesLayout = new VerticalLayout();
    devicesLayout.setCaption("Awaiting Requests");
    // HorizontalLayout buttonLayout = new HorizontalLayout();

    // there will now be space around the test component
    // components added to the test component will now not stick together but have space between
    // them
    devicesLayout.setMargin(true);
    devicesLayout.setSpacing(true);
    // buttonLayout.setMargin(true);
    // buttonLayout.setSpacing(true);

    // buttonLayout.addComponent(add);

    BeanItemContainer<BookingBean> booking = getAwaitingRequests();

    GeneratedPropertyContainer gpc = new GeneratedPropertyContainer(booking);

    gpc.addGeneratedProperty("confirm", new PropertyValueGenerator<String>() {
      /**
       * 
       */
      private static final long serialVersionUID = -18835252803342382L;

      @Override
      public String getValue(Item item, Object itemId, Object propertyId) {
        // return FontAwesome.TRASH_O.getHtml(); // The caption
        return "Confirm"; // The caption

      }

      @Override
      public Class<String> getType() {
        return String.class;
      }
    });

    gpc.addGeneratedProperty("delete", new PropertyValueGenerator<String>() {
      /**
       * 
       */
      private static final long serialVersionUID = 3815956364697828683L;

      @Override
      public String getValue(Item item, Object itemId, Object propertyId) {
        // return FontAwesome.TRASH_O.getHtml(); // The caption
        return "Delete"; // The caption

      }

      @Override
      public Class<String> getType() {
        return String.class;
      }
    });


    devicesGridConfirm = new Grid(gpc);
    // Create a grid

    devicesGridConfirm.setWidth("100%");
    devicesGridConfirm.setSelectionMode(SelectionMode.SINGLE);
    devicesGridConfirm.getColumn("delete").setRenderer(new HtmlRenderer());
    devicesGridConfirm.getColumn("confirm").setRenderer(new HtmlRenderer());
    setRenderers(devicesGridConfirm);
    devicesGridConfirm.setColumnOrder("ID", "confirmation", "deviceName", "service", "start",
        "end", "username", "phone", "price");
    devicesGridConfirm.getColumn("deviceName").setHeaderCaption("Instrument");

    // Render a button that deletes the data row (item)
    devicesGridConfirm.getColumn("delete").setRenderer(
        new ButtonRenderer(new ClickableRenderer.RendererClickListener() {
          /**
           * 
           */
          private static final long serialVersionUID = -5479714386381863679L;

          @Override
          public void click(RendererClickEvent event) {
            denyBooking((BookingBean) event.getItemId());
          }
        }));

    devicesGridConfirm.getColumn("confirm").setRenderer(
        new ButtonRenderer(new ClickableRenderer.RendererClickListener() {
          /**
           * 
           */
          private static final long serialVersionUID = 7944939118917004114L;

          @Override
          public void click(RendererClickEvent event) {
            confirmBooking((BookingBean) event.getItemId());
          }
        }));

    // devicesGrid.setEditorEnabled(true);

    // devicesLayout.addComponent(buttonLayout);
    devicesLayout.addComponent(devicesGridConfirm);

    // TODO filtering
    // HeaderRow filterRow = devicesGrid.prependHeaderRow();

    return devicesLayout;
  }

  /*
   * private BeanItemContainer<BookingBean> getBookingList() { BeanItemContainer<BookingBean>
   * bookingList = new BeanItemContainer<BookingBean>(BookingBean.class); List<BookingBean> bookings
   * = DBManager.getDatabaseInstance().getAllBookings(); assert bookings != null;
   * bookingList.addAll(bookings); return bookingList; }
   */

  private BeanItemContainer<BookingBean> getBookingList(String deviceName) {
    BeanItemContainer<BookingBean> bookingList =
        new BeanItemContainer<BookingBean>(BookingBean.class);
    List<BookingBean> bookings =
        DBManager.getDatabaseInstance().getAllBookingsPerDevice(deviceName);
    assert bookings != null;
    bookingList.addAll(bookings);
    // System.out.println("BeanItem Triggered: getAllBookingsPerDevice");
    return bookingList;
  }

  private BeanItemContainer<BookingBean> getDeletedBookings() {
    BeanItemContainer<BookingBean> bookingList =
        new BeanItemContainer<BookingBean>(BookingBean.class);
    List<BookingBean> bookings = DBManager.getDatabaseInstance().getDeletedBookings();
    assert bookings != null;
    bookingList.addAll(bookings);
    return bookingList;
  }

  private BeanItemContainer<BookingBean> getAwaitingRequests() {
    BeanItemContainer<BookingBean> bookingList =
        new BeanItemContainer<BookingBean>(BookingBean.class);
    List<BookingBean> bookings = DBManager.getDatabaseInstance().getAwaitingRequests();
    assert bookings != null;
    bookingList.addAll(bookings);
    return bookingList;
  }


  protected void refreshDataSources() {
    BookAdmin bookAdmin = new BookAdmin(null);
    setCompositionRoot(bookAdmin);
  }

  protected void removeBooking(Grid device, BookingBean db) {
    boolean removed = DBManager.getDatabaseInstance().removeBooking(db);
    if (removed) {
      device.getContainerDataSource().removeItem(db);
      devicesGridTrash.getContainerDataSource().addItem(db);
    } else {
      // TODO log failed operation
      showErrorNotification(
          "Jeez! It's not fair!",
          "For some reason we couldn't TRASH this booking. Maybe it's already trashed or already purged from the database.");
    }
  }

  private void setRenderers(Grid grid) {
    grid.getColumn("price").setRenderer(new NumberRenderer("%1$.2f €"));

    grid.getColumn("start").setRenderer(
        new DateRenderer("%1$tB %1$te %1$tY, %1$tH:%1$tM:%1$tS", Locale.GERMAN));

    grid.getColumn("end").setRenderer(
        new DateRenderer("%1$tB %1$te %1$tY, %1$tH:%1$tM:%1$tS", Locale.GERMAN));
  }


  /*
   * private void refresh(BeanItemContainer<BookingBean> item) { MethodProperty<String> p =
   * (MethodProperty<String>) ((Item) item).getItemProperty("stock"); p.fireValueChange(); }
   */

  protected void purgeBooking(BookingBean db) {
    boolean purged = DBManager.getDatabaseInstance().purgeBooking(db);
    if (purged) {
      devicesGridTrash.getContainerDataSource().removeItem(db);
    } else {
      // TODO log failed operation
      showErrorNotification(
          "Jeez! It's not fair!",
          "For some reason we couldn't PURGE this booking. Maybe it's already restored or already purged from the database.");
    }
  }

  protected void restoreBooking(BookingBean db) {
    boolean restored = DBManager.getDatabaseInstance().restoreBooking(db);
    boolean confirmed = DBManager.getDatabaseInstance().confirmed(db);
    if (restored) {
      devicesGridTrash.getContainerDataSource().removeItem(db);
      if (confirmed)
        devicesGridConfirm.getContainerDataSource().addItem(db);
      else
        gridMap.get(db.getDeviceName()).getContainerDataSource().addItem(db);

    } else {
      // TODO log failed operation
      showErrorNotification(
          "Jeez! It's not fair!",
          "For some reason we couldn't RESTORE this booking. Maybe it's already restored or already removed from the database.");
    }
  }

  protected void denyBooking(BookingBean db) {
    boolean denied = DBManager.getDatabaseInstance().denyBooking(db);
    if (denied) {
      devicesGridConfirm.getContainerDataSource().removeItem(db);
      devicesGridTrash.getContainerDataSource().addItem(db);
      // System.out.println("Denied");
    } else {
      // TODO log failed operation
      showErrorNotification(
          "Jeez! It's not fair!",
          "For some reason we couldn't DENY this booking. Maybe it's already denied or already removed from the database.");
    }
  }

  protected void confirmBooking(BookingBean db) {
    boolean confirmed = DBManager.getDatabaseInstance().confirmBooking(db);
    if (confirmed) {
      devicesGridConfirm.getContainerDataSource().removeItem(db);
      gridMap.get(db.getDeviceName()).getContainerDataSource().addItem(db);
    } else {
      // TODO log failed operation
      showErrorNotification(
          "Jeez! It's not fair!",
          "For some reason we couldn't CONFIRM this booking. Maybe it's already confirmed or already removed from the database.");
    }
  }

  private IndexedContainer getEmptyContainer() {
    final IndexedContainer container = new IndexedContainer();
    // some columns
    container.addContainerProperty("service", String.class, null);
    container.addContainerProperty("username", String.class, null);
    container.addContainerProperty("institute", String.class, null);
    container.addContainerProperty("start", Date.class, null);
    container.addContainerProperty("end", Date.class, null);
    return container;
  }

  private void showErrorNotification(String title, String description) {
    Notification notify = new Notification(title, description);
    notify.setDelayMsec(15000);
    notify.setPosition(Position.TOP_CENTER);
    notify.setIcon(FontAwesome.FROWN_O);
    notify.setStyleName(ValoTheme.NOTIFICATION_ERROR + " " + ValoTheme.NOTIFICATION_CLOSABLE);
    notify.show(Page.getCurrent());
  }

  private void Notification(String title, String description, String type) {
    Notification notify = new Notification(title, description);
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

  public class EuroConverter extends StringToBigDecimalConverter {
    /**
     * 
     */
    private static final long serialVersionUID = -2330030712464256062L;

    @Override
    public BigDecimal convertToModel(String value, Class<? extends BigDecimal> targetType,
        Locale locale) throws com.vaadin.data.util.converter.Converter.ConversionException {
      value = value.replaceAll("[€${symbol_escape}${symbol_escape}s]", "").trim();
      if ("".equals(value)) {
        value = "0";
      }
      return super.convertToModel(value, targetType, locale);
    }

    @Override
    protected NumberFormat getFormat(Locale locale) {
      // Always display currency with two decimals
      NumberFormat format = super.getFormat(locale);
      if (format instanceof DecimalFormat) {
        ((DecimalFormat) format).setMaximumFractionDigits(2);
        ((DecimalFormat) format).setMinimumFractionDigits(2);
      }
      return format;
    }

    @Override
    public String convertToPresentation(BigDecimal value, Class<? extends String> targetType,
        Locale locale) throws com.vaadin.data.util.converter.Converter.ConversionException {
      return super.convertToPresentation(value, targetType, locale) + " €";
    }
  }

}
