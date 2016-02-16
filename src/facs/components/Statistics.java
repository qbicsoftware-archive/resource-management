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

import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.liferay.portal.kernel.jmx.model.MBean;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.FooterCell;
import com.vaadin.ui.Grid.FooterRow;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
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
  private final String instituteCaption = "Institute"; 
  private final String CAPTION = "Usage/Statistics";

  Button createBill = new Button("Invoice");
  Button downloadBill = new Button("Download Invoice");
  private GeneratedPropertyContainer gpcontainer;
  
  private GridLayout gridLayout = new GridLayout(6,6);
  
  public Statistics() {
    this.setCaption(CAPTION);
    init();
  }
  
  private void init(){
	  
	Date dNow = new Date();
	SimpleDateFormat ft = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
	System.out.println(ft.format(dNow) + "  INFO  Statistics accessed! - User: "+LiferayAndVaadinUtils.getUser().getScreenName());
	  
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

  	Label infoLabel = new Label(DBManager.getDatabaseInstance().getUserNameByUserID(LiferayAndVaadinUtils.getUser().getScreenName()) + " · " + LiferayAndVaadinUtils.getUser().getScreenName());
  	infoLabel.addStyleName("h3");
  	
    createBill.setSizeFull();
    downloadBill.setSizeFull();
      
    gridLayout.setWidth("100%");	
	  
	//add components to the grid layout
	gridLayout.addComponent(infoLabel,0,0,3,0); 
	gridLayout.addComponent(grid,0,1,5,1);
	gridLayout.addComponent(createBill,0,3);
	gridLayout.addComponent(downloadBill,1,3);
	  
	gridLayout.setSpacing(true);
    
    createBill.addClickListener(new ClickListener(){
      private File bill;
      private FileDownloader fileDownloader;

      @Override
      public void buttonClick(ClickEvent event) {
        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
        Paths.get(basepath, "WEB-INF/billingTemplates");
        
        try {
          Billing billing = new Billing(Paths.get(basepath, "WEB-INF/billingTemplates").toFile(), "Angebot.tex");
          billing.setRecieverInstitution("BER - Berliner Flughafen");
          billing.setRecieverPI("Klaus Wowereit");
          billing.setRecieverStreet("am berliner flughafen 12");
          billing.setRecieverPostalCode("D-12345");
          billing.setRecieverCity("Berlin");
          
          billing.setSenderName("Dr. Stella Autenrieth");
          billing.setSenderFunction("Geschaeftsfuehrerin");
          billing.setSenderPostalCode("sender postal");
          billing.setSenderCity("Tuebingen");
          billing.setSenderStreet("Auf der Morgenstelle 42");
          billing.setSenderPhone("+49-7071-29-72163");
          billing.setSenderEmail("qbic@qbic.uni");
          billing.setSenderUrl("qbic.uni-tuebingen.de");
          billing.setSenderFaculty("Medizinischen Fakultät");
          
          billing.setProjectDescription("Dieses Angebot beinhaltet jede Menge Extras.");
          billing.setProjectShortDescription("jede Menge Extras.");
          billing.setProjectNumber("QA2014016");
          
          ArrayList<CostEntry> entries = new ArrayList<CostEntry>();
          for (Object itemId : gpcontainer.getItemIds()){
            float cost = ((Number) gpcontainer.getContainerProperty(itemId, costCaption).getValue()).floatValue();
            long s = ((Date) gpcontainer.getContainerProperty(itemId, startCaption).getValue()).getTime();
            long e = ((Date) gpcontainer.getContainerProperty(itemId, endCaption).getValue()).getTime();
            long timeFrame = e - s;
            Date start  = ((Date) gpcontainer.getContainerProperty(itemId, startCaption).getValue());
            SimpleDateFormat ft = new SimpleDateFormat(
                "dd.MM.yyyy");
            String date = ft.format(start);
            String description = "no description available";
            String time_frame = Formatter.toHoursAndMinutes(timeFrame);
            entries.add(billing.new CostEntry(date, time_frame, description, cost));  
          }
          billing.setCostEntries(entries);
          float totalCosts = 0.0f;
          for (Object itemId : gpcontainer.getItemIds()) {
            totalCosts +=
                ((Number) gpcontainer.getContainerProperty(itemId, costCaption).getValue())
                    .floatValue();
          }
          
          billing.setTotalCost(String.format("%1$.2f", totalCosts));
          
          bill = billing.createPdf();
          System.out.println(bill.getAbsolutePath());
          if(fileDownloader != null) downloadBill.removeExtension(fileDownloader);
          fileDownloader = new FileDownloader(new FileResource(bill));
          fileDownloader.extend(downloadBill);
          downloadBill.setEnabled(true);
          showSuccessfulNotification("Congratulations!","Bill is ready");
        } catch (Exception e) {
          showErrorNotification("What the heck!","An error occured while trying to create the invoice. The common problem occurs to be: cannot run program 'pdflatex'");
          e.printStackTrace();
        }
        
        
      }
      
    });
    setCompositionRoot(gridLayout);

  }
 
  private void setRenderers(Grid grid) {
    grid.getColumn(costCaption).setRenderer(new NumberRenderer("%1$.2f €"));

    grid.getColumn(startCaption).setRenderer(
        new DateRenderer("%1$tB %1$te %1$tY, %1$tH:%1$tM:%1$tS", Locale.GERMAN));

    grid.getColumn(endCaption).setRenderer(
        new DateRenderer("%1$tB %1$te %1$tY, %1$tH:%1$tM:%1$tS", Locale.GERMAN));
    
  }
  
  /**
   * fills the rows with values from the database
   * NOTE: time block can have null values. Probably it happens only to end. In that case start == end
   * @param grid
   */
  private void fillRows(Grid grid) {
    List<MachineOccupationBean> mobeans = DBManager.getDatabaseInstance().getPhysicalTimeBlocks();
    for(MachineOccupationBean mobean: mobeans){
      int userId = DBManager.getDatabaseInstance().findUserByFullName(mobean.getUserFullName());
      List<String> kostenStelle = new ArrayList<String>();
      //String kostenStelle;
      kostenStelle.add("unknown");
      String institute = "unknown";
      UserBean user = userId>0?DBManager.getDatabaseInstance().getUserById(userId):null;
      float cost = -1.f;
      Date end = mobean.getEnd() == null?mobean.getStart():mobean.getEnd();
      if(user != null){
        //kostenStelle = user.getKostenstelle();
        institute = user.getInstitute();
        
        System.out.println(user.getId() + " ************************************* " + mobean.getDeviceId());
        
        cost = getCost(user.getId(), mobean.getStart(), end, mobean.getDeviceId());
      }
      grid.addRow(DBManager.getDatabaseInstance().getDeviceById(mobean.getDeviceId()).getName(), kostenStelle.get(0), mobean.getStart(), end, cost, institute);
    }
    
  }
  
  private float getCost(int userId, Date start, Date end, int resourceId){
    float cost = 0f;
    float costPerHour = DBManager.getDatabaseInstance().getCostByResourceAndUserIds(userId, resourceId);
    if(costPerHour > 0){
      float hoursUsed = Formatter.toHours(start, end);
      System.out.println(hoursUsed);
      cost = hoursUsed*costPerHour;
      System.out.println(cost);
    }
    return cost;
  }
  
  /**
   * create an empty container for the grid.
   * Different containers might follow
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
    container.addContainerProperty(instituteCaption, String.class, null);
    return container;
  }
  /**
   * this method is just a mockup, which was used for testing. Can be deleted soon
   *
   */
  @Deprecated
  private void initMockUp() {
    final IndexedContainer container = new IndexedContainer();
    // some columns
    container.addContainerProperty(deviceCaption, String.class, null);
    container.addContainerProperty(kostenstelleCaption, String.class, null);
    container.addContainerProperty(startCaption, Date.class, null);
    container.addContainerProperty(endCaption, Date.class, null);
    container.addContainerProperty(costCaption, Float.class, null);
    // Add some generated properties
    gpcontainer = new GeneratedPropertyContainer(container);

    Grid grid = new Grid(gpcontainer);
    grid.setWidth("100%");
    grid.setHeightByRows(5);
    grid.setHeightMode(HeightMode.ROW);

    grid.getColumn(costCaption).setRenderer(new NumberRenderer("%1$.2f €"));

    grid.getColumn(startCaption).setRenderer(
        new DateRenderer("%1$tB %1$te %1$tY, %1$tH:%1$tM:%1$tS", Locale.GERMAN));

    grid.getColumn(endCaption).setRenderer(
        new DateRenderer("%1$tB %1$te %1$tY, %1$tH:%1$tM:%1$tS", Locale.GERMAN));

    java.util.Date date = new java.util.Date();
    GregorianCalendar start = new GregorianCalendar();
    start.setTime(date);
    GregorianCalendar end = new GregorianCalendar();
    end.setTime(date);
    end.add(java.util.Calendar.HOUR, 1);

    // Add some data rows
    grid.addRow("device 1", "QBiC", start.getTime(), end.getTime(), 125.2f);
    end.add(java.util.Calendar.MINUTE, 20);
    grid.addRow("device 2", "QBiC", start.getTime(), end.getTime(), 541.23f);
    start.add(java.util.Calendar.MINUTE, 13);
    grid.addRow("device 1", "Sand", start.getTime(), end.getTime(), 125.2f);
    grid.addRow("device 2", "Sand", start.getTime(), end.getTime(), 521.2f);
    grid.addRow("device 1", "QBiC", start.getTime(), end.getTime(), 125.2f);
    grid.addRow("device 2", "Sand", start.getTime(), end.getTime(), 521.2f);
    grid.addRow("device 1", "Sand", start.getTime(), end.getTime(), 125.2f);
    grid.addRow("device 2", "QBiC", start.getTime(), end.getTime(), 521.2f);

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

  	Label infoLabel = new Label(LiferayAndVaadinUtils.getUser().getScreenName() + " · " + DBManager.getDatabaseInstance().getUserNameByUserID(LiferayAndVaadinUtils.getUser().getScreenName()));
  	infoLabel.addStyleName("h3");
    
    gridLayout.setWidth("100%");	
	  
	//add components to the grid layout
	gridLayout.addComponent(infoLabel,0,0,3,0); 
	gridLayout.addComponent(grid,0,1,5,1);
	gridLayout.addComponent(createBill,0,3,2,3);
	gridLayout.addComponent(downloadBill,3,4,5,4);
	  
	gridLayout.setSpacing(true);
	
    //downloadBill.setEnabled(false);
    
    createBill.addClickListener(new ClickListener(){
      private File bill;
      private FileDownloader fileDownloader;

      
      @Override
      public void buttonClick(ClickEvent event) {
        String basepath = VaadinService.getCurrent()
            .getBaseDirectory().getAbsolutePath();
       Paths.get(basepath, "WEB-INF/billingTemplates");
        
        try {
          Billing billing = new Billing(Paths.get(basepath, "WEB-INF/billingTemplates").toFile(), "Angebot.tex");
          billing.setRecieverInstitution("BER - Berliner Flughafen");
          billing.setRecieverPI("Klaus Wowereit");
          billing.setRecieverStreet("am berliner flughafen 12");
          billing.setRecieverPostalCode("D-12345");
          billing.setRecieverCity("Berlin");
          
          billing.setSenderName("Dr. Stella Autenrieth");
          billing.setSenderFunction("Geschaeftsfuehrerin");
          billing.setSenderPostalCode("sender postal");
          billing.setSenderCity("Tuebingen");
          billing.setSenderStreet("Auf der Morgenstelle 42");
          billing.setSenderPhone("+49-7071-29-72163");
          billing.setSenderEmail("qbic@qbic.uni");
          billing.setSenderUrl("qbic.uni-tuebingen.de");
          billing.setSenderFaculty("Medizinischen Fakultät");
          
          billing.setProjectDescription("Dieses Angebot beinhaltet jede Menge Extras.");
          billing.setProjectShortDescription("jede Menge Extras.");
          billing.setProjectNumber("QA2014016");
          
          ArrayList<CostEntry> entries = new ArrayList<CostEntry>();
          for (Object itemId : gpcontainer.getItemIds()){
            float cost = ((Number) gpcontainer.getContainerProperty(itemId, costCaption).getValue()).floatValue();
            long s = ((Date) gpcontainer.getContainerProperty(itemId, startCaption).getValue()).getTime();
            long e = ((Date) gpcontainer.getContainerProperty(itemId, endCaption).getValue()).getTime();
            long timeFrame = e - s;
            Date start  = ((Date) gpcontainer.getContainerProperty(itemId, startCaption).getValue());
            SimpleDateFormat ft = new SimpleDateFormat(
                "dd.MM.yyyy");
            String date = ft.format(start);
            String description = "no description available";
            String time_frame = Formatter.toHoursAndMinutes(timeFrame);
            entries.add(billing.new CostEntry(date, time_frame, description, cost));  
          }
          billing.setCostEntries(entries);
          float totalCosts = 0.0f;
          for (Object itemId : gpcontainer.getItemIds()) {
            totalCosts +=
                ((Number) gpcontainer.getContainerProperty(itemId, costCaption).getValue())
                    .floatValue();
          }
          
          billing.setTotalCost(String.format("%1$.2f", totalCosts));
          
          bill = billing.createPdf();
          System.out.println(bill.getAbsolutePath());
          if(fileDownloader != null) downloadBill.removeExtension(fileDownloader);
          fileDownloader = new FileDownloader(new FileResource(bill));
          fileDownloader.extend(downloadBill);
          downloadBill.setEnabled(true);
          showSuccessfulNotification("Congratulations!","Bill is ready");
        } catch (Exception e) {
          showErrorNotification("What the heck!","Error occured while trying to create bill. Please log out and contact your sysadmin");
          e.printStackTrace();
        }
        
        
      }
      
    });
    setCompositionRoot(gridLayout);
    
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
        float minutes = ((float) (total * 0.000000277778f) - hours) * 60;
        footerCellEnd.setText(String.format("%d:%02d hours", hours, (int) minutes)); // "%1$.0f hours"

      }
    });
    headerCellDevice.setComponent(filterField);
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
  
  public void addRowFilter(Grid grid, final String propertyId) {

  }


}
