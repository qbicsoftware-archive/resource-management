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
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.Position;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.FooterCell;
import com.vaadin.ui.Grid.FooterRow;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.ui.renderers.NumberRenderer;
import com.vaadin.ui.themes.ValoTheme;

import de.uni_tuebingen.qbic.main.LiferayAndVaadinUtils;
import facs.db.DBManager;
import facs.model.MachineOccupationBean;
import facs.model.UserBean;
import facs.utils.Billing;
import facs.utils.Billing.CostEntry;
import facs.utils.Formatter;

public class Statistics extends CustomComponent {
  private static final long serialVersionUID = 4811041982287436302L;

  private final String deviceCaption = "Device";
  private final String kostenstelleCaption = "Kostenstelle";
  private final String startCaption = "Start";
  private final String endCaption = "End";
  private final String costCaption = "Cost";
  private final String nameCaption = "Name";
  private final String instituteCaption = "Institute";
  private final String CAPTION = "Usage/Statistics";

  String ReceiverPI = null;
  String ReceiverInstitute = null;
  String ReceiverStreet = null;
  String ReceiverPostcode = null;
  String ReceiverCity = null;

  String ProjectDescription = null;
  String ProjectShortDescription = null;
  String ProjectNumber = null;

  Button createInvoice = new Button("Create Invoice");
  Button downloadInvoice = new Button("Download Invoice");

  private GeneratedPropertyContainer gpcontainer;

  // private GridLayout gridLayout = new GridLayout(6, 6);


  public Statistics() {
    this.setCaption(CAPTION);
    init();
  }

  private void init() {

    TabSheet statistics = new TabSheet();

    statistics.addStyleName(ValoTheme.TABSHEET_FRAMED);
    statistics.addTab(newMatchedGrid()).setCaption("Matched");
    statistics.addTab(initialGrid()).setCaption("Not Matched");

    setCompositionRoot(statistics);

  }

  private Component initialGrid() {

    VerticalLayout gridLayout = new VerticalLayout();

    createInvoice.setEnabled(false);
    downloadInvoice.setEnabled(false);

    String buttonRefreshTitle = "Refresh";
    Button refresh = new Button(buttonRefreshTitle);
    refresh.setIcon(FontAwesome.REFRESH);
    refresh.setDescription("Click here to reload the data from the database!");
    refresh.addStyleName(ValoTheme.BUTTON_FRIENDLY);

    refresh.addClickListener(new ClickListener() {
      private static final long serialVersionUID = -3610721151565496269L;

      @Override
      public void buttonClick(ClickEvent event) {
        refreshDataSources();

      }
    });

    Date dNow = new Date();
    SimpleDateFormat ft = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
    System.out.println(ft.format(dNow) + "  INFO  Statistics accessed! - User: "
        + LiferayAndVaadinUtils.getUser().getScreenName());

    // Add some generated properties
    IndexedContainer container = getEmptyContainer();
    gpcontainer = new GeneratedPropertyContainer(container);

    Grid grid = new Grid(gpcontainer);

    grid.setWidth("100%");
    setRenderers(grid);
    fillRows(grid);

    // compute total costs
    float totalCosts = 0.0f;
    for (Object itemId : gpcontainer.getItemIds())
      totalCosts +=
          ((Number) gpcontainer.getContainerProperty(itemId, costCaption).getValue()).floatValue();

    // compute total time in milliseconds
    long total = 0;
    for (Object itemId : gpcontainer.getItemIds()) {
      long s = ((Date) gpcontainer.getContainerProperty(itemId, startCaption).getValue()).getTime();
      long e = ((Date) gpcontainer.getContainerProperty(itemId, endCaption).getValue()).getTime();
      total += e - s;
    }

    // set footer to contain total cost and time in hours:minutes
    FooterRow footer = grid.appendFooterRow();
    FooterCell footerCellCost = footer.getCell(costCaption);
    footerCellCost.setText(String.format("%1$.2f € total", totalCosts));

    FooterCell footerCellEnd = footer.getCell(endCaption);
    footerCellEnd.setText(Formatter.toHoursAndMinutes(total)); // "%1$.0f hours"

    // Set up a filter for all columns
    HeaderRow filterRow = grid.appendHeaderRow();
    addRowFilter(filterRow, deviceCaption, container, footer, gpcontainer);
    addRowFilter(filterRow, kostenstelleCaption, container, footer, gpcontainer);

    Label infoLabel =
        new Label(DBManager.getDatabaseInstance().getUserNameByUserID(
            LiferayAndVaadinUtils.getUser().getScreenName())
            + " · " + LiferayAndVaadinUtils.getUser().getScreenName());
    infoLabel.addStyleName("h4");

    // createInvoice.setSizeFull();
    // downloadInvoice.setSizeFull();

    gridLayout.setWidth("100%");
    gridLayout.setCaption("Statistics");
    // add components to the grid layout
    // gridLayout.addComponent(infoLabel, 0, 0, 3, 0);
    // gridLayout.addComponent(grid, 0, 1, 5, 1);
    // gridLayout.addComponent(createInvoice, 0, 3);
    // gridLayout.addComponent(downloadInvoice, 1, 3);

    gridLayout.addComponent(grid);
    gridLayout.addComponent(refresh);
    gridLayout.addComponent(createInvoice);
    gridLayout.addComponent(downloadInvoice);

    gridLayout.setMargin(true);
    gridLayout.setSpacing(true);

    // grid.setEditorEnabled(true);
    grid.setSelectionMode(SelectionMode.SINGLE);
    grid.addSelectionListener(new SelectionListener() {


      /**
       * 
       */
      private static final long serialVersionUID = -2683274060620429050L;

      @Override
      public void select(SelectionEvent event) {
        // Notification.show("Select row: " + grid.getSelectedRow() + " Name: "
        // + gpcontainer.getContainerProperty(grid.getSelectedRow(), nameCaption).getValue());
        downloadInvoice.setEnabled(false);
        ReceiverPI =
            (String) gpcontainer.getContainerProperty(grid.getSelectedRow(), nameCaption)
                .getValue();
        createInvoice.setEnabled(true);
      }
    });

    createInvoice.addClickListener(new ClickListener() {

      /**
       * 
       */
      private static final long serialVersionUID = 5512585967145932560L;
      private File bill;
      private FileDownloader fileDownloader;

      @Override
      public void buttonClick(ClickEvent event) {
        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();

        Paths.get(basepath, "WEB-INF/billingTemplates");

        // System.out.println("Basepath: " + basepath);

        try {

          int setUserId = DBManager.getDatabaseInstance().findUserByFullName(ReceiverPI);

          if (setUserId > 0) {

            Billing billing =
                new Billing(Paths.get(basepath, "WEB-INF/billingTemplates").toFile(), "Angebot.tex");

            UserBean user =
                setUserId > 0 ? DBManager.getDatabaseInstance().getUserById(setUserId) : null;

            billing.setReceiverPI(ReceiverPI);
            billing.setReceiverInstitution(user.getInstitute());
            billing.setReceiverStreet(user.getStreet());
            billing.setReceiverPostalCode(user.getPostcode());
            billing.setReceiverCity(user.getCity());

            billing.setSenderName("Dr. rer. nat. Stella Autenrieth");
            billing.setSenderFunction("Leiterin");
            billing.setSenderPostalCode("72076");
            billing.setSenderCity("Tübingen");
            billing.setSenderStreet("Otfried-Müller-Straße 10");
            billing.setSenderPhone("+49 (0) 7071 29-83156");
            billing.setSenderEmail("stella.autenrieth@med.uni-tuebingen.de");
            billing.setSenderUrl("www.medizin.uni-tuebingen.de");
            billing.setSenderFaculty("Medizinischen Fakultät");

            if (user.getKostenstelle() != null)
              billing.setProjectDescription("Kostenstelle: " + user.getKostenstelle());
            else
              billing.setProjectDescription("Keine kostenstelle verfügbar.");

            billing.setProjectShortDescription("Dieses Angebot beinhaltet jede Menge Extras.");

            if (user.getProject() != null)
              billing.setProjectNumber("Kostenstelle: " + user.getKostenstelle());
            else
              billing.setProjectNumber("Keine project nummer verfügbar.");

            float cost =
                ((Number) gpcontainer.getContainerProperty(grid.getSelectedRow(), costCaption)
                    .getValue()).floatValue();
            long s =
                ((Date) gpcontainer.getContainerProperty(grid.getSelectedRow(), startCaption)
                    .getValue()).getTime();
            long e =
                ((Date) gpcontainer.getContainerProperty(grid.getSelectedRow(), endCaption)
                    .getValue()).getTime();
            long timeFrame = e - s;
            Date start =
                ((Date) gpcontainer.getContainerProperty(grid.getSelectedRow(), startCaption)
                    .getValue());
            SimpleDateFormat ft = new SimpleDateFormat("dd.MM.yyyy");
            String date = ft.format(start);
            String description = "No Description is Available";
            String time_frame = Formatter.toHoursAndMinutes(timeFrame);

            ArrayList<CostEntry> entries = new ArrayList<CostEntry>();
            entries.add(billing.new CostEntry(date, time_frame, description, cost));
            billing.setCostEntries(entries);
            float totalCosts = 0.0f;

            // calculates the total cost of items
            // for (Object itemId : gpcontainer.getItemIds()) {
            // totalCosts +=
            // ((Number) gpcontainer.getContainerProperty(itemId, costCaption).getValue())
            // .floatValue();
            // }

            totalCosts =
                ((Number) gpcontainer.getContainerProperty(grid.getSelectedRow(), costCaption)
                    .getValue()).floatValue();

            billing.setTotalCost(String.format("%1$.2f", totalCosts));

            bill = billing.createPdf();
            // System.out.println(bill.getAbsolutePath());
            if (fileDownloader != null)
              downloadInvoice.removeExtension(fileDownloader);
            fileDownloader = new FileDownloader(new FileResource(bill));
            fileDownloader.extend(downloadInvoice);
            downloadInvoice.setEnabled(true);
            showSuccessfulNotification("Congratulations!",
                "Invoice is created and available for download.");
            downloadInvoice.setEnabled(true);
          } else {
            createInvoice.setEnabled(false);
            downloadInvoice.setEnabled(false);
            showErrorNotification(
                "No such user found!",
                "An error occured while trying to create the invoice. The common problem occurs to be: this no such user in the database.");
          }
        }

        catch (Exception e) {
          showErrorNotification(
              "What the heck!",
              "An error occured while trying to create the invoice. The common problem occurs to be: this no such user or it can not run program 'pdflatex'.");
          e.printStackTrace();
        }

        // for all entries
        /*
         * try { Billing billing = new Billing(Paths.get(basepath,
         * "WEB-INF/billingTemplates").toFile(), "Angebot.tex");
         * billing.setRecieverInstitution("BER - Berliner Flughafen");
         * billing.setRecieverPI("Klaus Something");
         * billing.setRecieverStreet("am berliner flughafen 12");
         * billing.setRecieverPostalCode("D-12345"); billing.setRecieverCity("Berlin");
         * 
         * billing.setSenderName("Dr. rer. nat. Stella Autenrieth");
         * billing.setSenderFunction("Leiterin"); billing.setSenderPostalCode("72076");
         * billing.setSenderCity("Tübingen"); billing.setSenderStreet("Otfried-Müller-Straße 10");
         * billing.setSenderPhone("+49 (0) 7071 29-83156");
         * billing.setSenderEmail("stella.autenrieth@med.uni-tuebingen.de");
         * billing.setSenderUrl("www.medizin.uni-tuebingen.de");
         * billing.setSenderFaculty("Medizinischen Fakultät");
         * 
         * billing.setProjectDescription("Dieses Angebot beinhaltet jede Menge Extras.");
         * billing.setProjectShortDescription("jede Menge Extras.");
         * billing.setProjectNumber("QA2014016");
         * 
         * ArrayList<CostEntry> entries = new ArrayList<CostEntry>(); for (Object itemId :
         * gpcontainer.getItemIds()) { float cost = ((Number)
         * gpcontainer.getContainerProperty(itemId, costCaption).getValue()) .floatValue(); long s =
         * ((Date) gpcontainer.getContainerProperty(itemId, startCaption).getValue()) .getTime();
         * long e = ((Date) gpcontainer.getContainerProperty(itemId,
         * endCaption).getValue()).getTime(); long timeFrame = e - s; Date start = ((Date)
         * gpcontainer.getContainerProperty(itemId, startCaption).getValue()); SimpleDateFormat ft =
         * new SimpleDateFormat("dd.MM.yyyy"); String date = ft.format(start); String description =
         * "No Description is Available"; String time_frame =
         * Formatter.toHoursAndMinutes(timeFrame); entries.add(billing.new CostEntry(date,
         * time_frame, description, cost)); } billing.setCostEntries(entries); float totalCosts =
         * 0.0f; for (Object itemId : gpcontainer.getItemIds()) { totalCosts += ((Number)
         * gpcontainer.getContainerProperty(itemId, costCaption).getValue()) .floatValue(); }
         * 
         * billing.setTotalCost(String.format("%1$.2f", totalCosts));
         * 
         * bill = billing.createPdf(); System.out.println(bill.getAbsolutePath()); if
         * (fileDownloader != null) downloadBill.removeExtension(fileDownloader); fileDownloader =
         * new FileDownloader(new FileResource(bill)); fileDownloader.extend(downloadBill);
         * downloadBill.setEnabled(true); showSuccessfulNotification("Congratulations!",
         * "Invoice is created and available for download."); } catch (Exception e) {
         * showErrorNotification( "What the heck!",
         * "An error occured while trying to create the invoice. The common problem occurs to be: cannot run program 'pdflatex'"
         * ); e.printStackTrace(); }
         */

      }

    });

    return gridLayout;

  }

  private Component newMatchedGrid() {

    Button createInvoiceMatched = new Button("Create Invoice");
    Button downloadInvoiceMatched = new Button("Download Invoice");

    String buttonRefreshTitle = "Refresh";
    Button refreshMatched = new Button(buttonRefreshTitle);
    refreshMatched.setIcon(FontAwesome.REFRESH);
    refreshMatched.setDescription("Click here to reload the data from the database!");
    refreshMatched.addStyleName(ValoTheme.BUTTON_FRIENDLY);

    createInvoiceMatched.setEnabled(false);
    downloadInvoiceMatched.setEnabled(false);

    Grid matchedGrid;

    refreshMatched.addClickListener(new ClickListener() {
      private static final long serialVersionUID = -3610721151565496269L;

      @Override
      public void buttonClick(ClickEvent event) {
        refreshDataSources();

      }
    });


    IndexedContainer mcontainer = getEmptyContainer();
    GeneratedPropertyContainer mpc = new GeneratedPropertyContainer(mcontainer);

    VerticalLayout matchedLayout = new VerticalLayout();

    matchedGrid = new Grid(mpc);

    setRenderers(matchedGrid);

    // compute total costs
    float totalCosts = 0.0f;
    for (Object itemId : mcontainer.getItemIds())
      totalCosts +=
          ((Number) mcontainer.getContainerProperty(itemId, costCaption).getValue()).floatValue();

    // compute total time in milliseconds
    long total = 0;
    for (Object itemId : mcontainer.getItemIds()) {
      long s = ((Date) mcontainer.getContainerProperty(itemId, startCaption).getValue()).getTime();
      long e = ((Date) mcontainer.getContainerProperty(itemId, endCaption).getValue()).getTime();
      total += e - s;
    }

    // set footer to contain total cost and time in hours:minutes
    FooterRow footer = matchedGrid.appendFooterRow();
    FooterCell footerCellCost = footer.getCell(costCaption);
    footerCellCost.setText(String.format("%1$.2f € total", totalCosts));

    FooterCell footerCellEnd = footer.getCell(endCaption);
    footerCellEnd.setText(Formatter.toHoursAndMinutes(total)); // "%1$.0f hours"

    // Set up a filter for all columns
    HeaderRow filterRow = matchedGrid.appendHeaderRow();
    addRowFilter(filterRow, deviceCaption, mcontainer, footer, mpc);
    addRowFilter(filterRow, kostenstelleCaption, mcontainer, footer, mpc);

    matchedLayout.setMargin(true);
    matchedLayout.setSpacing(true);

    fillMatchedRows(matchedGrid);

    // devicesGrid.setWidth("100%");
    matchedGrid.setSizeFull();
    matchedGrid.setSelectionMode(SelectionMode.SINGLE);
    matchedLayout.addComponent(matchedGrid);


    matchedGrid.setSelectionMode(SelectionMode.SINGLE);
    matchedGrid.addSelectionListener(new SelectionListener() {


      /**
       * 
       */
      private static final long serialVersionUID = -2683274060620429050L;

      @Override
      public void select(SelectionEvent event) {
        // Notification.show("Select row: " + grid.getSelectedRow() + " Name: "
        // + gpcontainer.getContainerProperty(grid.getSelectedRow(), nameCaption).getValue());
        downloadInvoiceMatched.setEnabled(false);
        ReceiverPI =
            (String) gpcontainer.getContainerProperty(matchedGrid.getSelectedRow(), nameCaption)
                .getValue();
        createInvoiceMatched.setEnabled(true);
      }
    });
    createInvoiceMatched.addClickListener(new ClickListener() {

      private File bill;
      private FileDownloader fileDownloader;

      @Override
      public void buttonClick(ClickEvent event) {
        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();

        Paths.get(basepath, "WEB-INF/billingTemplates");

        // System.out.println("Basepath: " + basepath);

        try {

          int setUserId = DBManager.getDatabaseInstance().findUserByFullName(ReceiverPI);

          if (setUserId > 0) {

            Billing billing =
                new Billing(Paths.get(basepath, "WEB-INF/billingTemplates").toFile(), "Angebot.tex");

            UserBean user =
                setUserId > 0 ? DBManager.getDatabaseInstance().getUserById(setUserId) : null;

            billing.setReceiverPI(ReceiverPI);
            billing.setReceiverInstitution(user.getInstitute());
            billing.setReceiverStreet(user.getStreet());
            billing.setReceiverPostalCode(user.getPostcode());
            billing.setReceiverCity(user.getCity());

            billing.setSenderName("Dr. rer. nat. Stella Autenrieth");
            billing.setSenderFunction("Leiterin");
            billing.setSenderPostalCode("72076");
            billing.setSenderCity("Tübingen");
            billing.setSenderStreet("Otfried-Müller-Straße 10");
            billing.setSenderPhone("+49 (0) 7071 29-83156");
            billing.setSenderEmail("stella.autenrieth@med.uni-tuebingen.de");
            billing.setSenderUrl("www.medizin.uni-tuebingen.de");
            billing.setSenderFaculty("Medizinischen Fakultät");

            if (user.getKostenstelle() != null)
              billing.setProjectDescription("Kostenstelle: " + user.getKostenstelle());
            else
              billing.setProjectDescription("Keine kostenstelle verfügbar.");

            billing.setProjectShortDescription("Dieses Angebot beinhaltet jede Menge Extras.");

            if (user.getProject() != null)
              billing.setProjectNumber("Kostenstelle: " + user.getKostenstelle());
            else
              billing.setProjectNumber("Keine project nummer verfügbar.");

            float cost =
                ((Number) mcontainer
                    .getContainerProperty(matchedGrid.getSelectedRow(), costCaption).getValue())
                    .floatValue();
            long s =
                ((Date) mcontainer.getContainerProperty(matchedGrid.getSelectedRow(), startCaption)
                    .getValue()).getTime();
            long e =
                ((Date) mcontainer.getContainerProperty(matchedGrid.getSelectedRow(), endCaption)
                    .getValue()).getTime();
            long timeFrame = e - s;
            Date start =
                ((Date) mcontainer.getContainerProperty(matchedGrid.getSelectedRow(), startCaption)
                    .getValue());
            SimpleDateFormat ft = new SimpleDateFormat("dd.MM.yyyy");
            String date = ft.format(start);
            String description = "No Description is Available";
            String time_frame = Formatter.toHoursAndMinutes(timeFrame);

            ArrayList<CostEntry> entries = new ArrayList<CostEntry>();
            entries.add(billing.new CostEntry(date, time_frame, description, cost));
            billing.setCostEntries(entries);
            float totalCosts = 0.0f;

            totalCosts =
                ((Number) mcontainer
                    .getContainerProperty(matchedGrid.getSelectedRow(), costCaption).getValue())
                    .floatValue();

            billing.setTotalCost(String.format("%1$.2f", totalCosts));

            bill = billing.createPdf();
            // System.out.println(bill.getAbsolutePath());
            if (fileDownloader != null)
              downloadInvoiceMatched.removeExtension(fileDownloader);
            fileDownloader = new FileDownloader(new FileResource(bill));
            fileDownloader.extend(downloadInvoiceMatched);
            downloadInvoiceMatched.setEnabled(true);
            showSuccessfulNotification("Congratulations!",
                "Invoice is created and available for download.");
            downloadInvoiceMatched.setEnabled(true);
          } else {
            createInvoiceMatched.setEnabled(false);
            downloadInvoiceMatched.setEnabled(false);
            showErrorNotification(
                "No such user found!",
                "An error occured while trying to create the invoice. The common problem occurs to be: this no such user in the database.");
          }
        }

        catch (Exception e) {
          showErrorNotification(
              "What the heck!",
              "An error occured while trying to create the invoice. The common problem occurs to be: this no such user or it can not run program 'pdflatex'.");
          e.printStackTrace();
        }

      }

    });

    matchedLayout.addComponent(refreshMatched);
    matchedLayout.addComponent(createInvoiceMatched);
    matchedLayout.addComponent(downloadInvoiceMatched);

    return matchedLayout;
  }

  private void setRenderers(Grid grid) {
    grid.getColumn(costCaption).setRenderer(new NumberRenderer("%1$.2f €"));

    grid.getColumn(startCaption).setRenderer(
        new DateRenderer("%1$tB %1$te %1$tY, %1$tH:%1$tM:%1$tS", Locale.GERMAN));

    grid.getColumn(endCaption).setRenderer(
        new DateRenderer("%1$tB %1$te %1$tY, %1$tH:%1$tM:%1$tS", Locale.GERMAN));

  }

  /**
   * fills the rows with values from the database NOTE: time block can have null values. Probably it
   * happens only to end. In that case start == end
   * 
   * @param grid
   */
  private void fillRows(Grid grid) {
    List<MachineOccupationBean> mobeans = DBManager.getDatabaseInstance().getPhysicalTimeBlocks();

    for (MachineOccupationBean mobean : mobeans) {
      int userId = DBManager.getDatabaseInstance().findUserByFullName(mobean.getUserFullName());

      // List<String> kostenStelle = new ArrayList<String>();
      // String kostenStelle;
      // kostenStelle.add("unknown");

      String kostenStelle = "unknown";
      String institute = "unknown";
      UserBean user = userId > 0 ? DBManager.getDatabaseInstance().getUserById(userId) : null;
      float cost = -1.f;
      Date end = mobean.getEnd() == null ? mobean.getStart() : mobean.getEnd();
      if (user != null) {
        // System.out.println("userId: " + userId);
        // System.out.println("name: " + mobean.getUserFullName());
        kostenStelle = user.getKostenstelle();
        institute = user.getInstitute();
        // System.out.println(user.getId() + " ************************************* "
        // + mobean.getDeviceId());
        cost = getCost(user.getId(), mobean.getStart(), end, mobean.getDeviceId());
      }
      grid.addRow(DBManager.getDatabaseInstance().getDeviceById(mobean.getDeviceId()).getName(),
          kostenStelle, mobean.getStart(), end, cost, mobean.getUserFullName(), institute);
      // System.out.println("User: " + user.getName() + " Kostenstelle:" + kostenStelle);
      // kostenStelle.get(0), mobean.getStart(), end, cost, institute);
    }

  }

  private void fillMatchedRows(Grid grid) {
    List<MachineOccupationBean> mobeans = DBManager.getDatabaseInstance().getMatchedTimeBlocks();

    for (MachineOccupationBean mobean : mobeans) {
      int userId = DBManager.getDatabaseInstance().findUserByFullName(mobean.getUserFullName());
      float cost = -1;
      String kostenStelle = "unknown";
      String institute = "unknown";
      UserBean user = userId > 0 ? DBManager.getDatabaseInstance().getUserById(userId) : null;
      Date end = mobean.getEnd() == null ? mobean.getStart() : mobean.getEnd();
      if (user != null) {
        // System.out.println("userId: " + userId);
        // System.out.println("name: " + mobean.getUserFullName());
        kostenStelle = user.getKostenstelle();
        institute = user.getInstitute();
        cost = getCost(user.getId(), mobean.getStart(), end, mobean.getDeviceId());
      }
      grid.addRow(mobean.getDeviceName(), kostenStelle, mobean.getStart(), end, cost,
          mobean.getUserFullName(), institute);
    }

  }



  private float getCost(int userId, Date start, Date end, int resourceId) {
    // System.out.println("Statistics: UserId: " + userId + " ResourceId: " + resourceId);
    float cost = 0f;
    float costPerHour =
        DBManager.getDatabaseInstance().getCostByResourceAndUserIds(userId, resourceId);
    // System.out.println("Cost per Hour: " + costPerHour);
    if (costPerHour > 0) {
      float hoursUsed = Formatter.toHours(start, end);
      // System.out.println("Hours Used: " + hoursUsed);
      cost = hoursUsed * costPerHour;
      // System.out.println("Cost: " + cost);
    }
    return cost;
  }

  /**
   * create an empty container for the grid. Different containers might follow
   * 
   * @return
   */
  private IndexedContainer getEmptyContainer() {
    final IndexedContainer container = new IndexedContainer();
    // some columns
    container.addContainerProperty(deviceCaption, String.class, null);
    container.addContainerProperty(kostenstelleCaption, String.class, null);
    container.addContainerProperty(startCaption, Date.class, null);
    container.addContainerProperty(endCaption, Date.class, null);
    container.addContainerProperty(costCaption, Float.class, null);
    container.addContainerProperty(nameCaption, String.class, null);
    container.addContainerProperty(instituteCaption, String.class, null);
    return container;
  }

  /**
   * adds a new filter to the header row
   * 
   * @param headerRow
   * @param propertyId
   * @param container
   */
  public void addRowFilter(HeaderRow headerRow, final String propertyId,
      final IndexedContainer container, final FooterRow footer,
      final GeneratedPropertyContainer gpcontainer) {
    HeaderCell headerCellDevice = headerRow.getCell(propertyId);
    // Have an input field to use for filter
    TextField filterField = new TextField();
    filterField.setColumns(8);
    filterField.setInputPrompt("Filter");
    filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
    // Update filter When the filter input is changed
    filterField.addTextChangeListener(new TextChangeListener() {
      private static final long serialVersionUID = -6252315973584227301L;

      @Override
      public void textChange(TextChangeEvent change) {
        // Can't modify filters so need to replace
        container.removeContainerFilters(propertyId);
        // (Re)create the filter if necessary
        if (!change.getText().isEmpty())
          container.addContainerFilter(new SimpleStringFilter(propertyId, change.getText(), true,
              false));
        // compute total costs
        float totalCosts = 0.0f;
        for (Object itemId : gpcontainer.getItemIds()) {
          totalCosts +=
              ((Number) gpcontainer.getContainerProperty(itemId, costCaption).getValue())
                  .floatValue();
        }

        // compute total time in milliseconds
        long total = 0;
        for (Object itemId : gpcontainer.getItemIds()) {
          long s =
              ((Date) gpcontainer.getContainerProperty(itemId, startCaption).getValue()).getTime();
          long e =
              ((Date) gpcontainer.getContainerProperty(itemId, endCaption).getValue()).getTime();
          total += e - s;
        }

        FooterCell footerCellCost = footer.getCell(costCaption);
        footerCellCost.setText(String.format("%1$.2f € total", totalCosts));

        FooterCell footerCellEnd = footer.getCell(endCaption);
        int hours = (int) (total * 0.000000277778f);
        float minutes = (total * 0.000000277778f - hours) * 60;
        footerCellEnd.setText(String.format("%d:%02d hours", hours, (int) minutes)); // "%1$.0f hours"

      }
    });
    headerCellDevice.setComponent(filterField);
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

  public void refreshDataSources() {
    init();

  }

}
