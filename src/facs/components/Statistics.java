package facs.components;

import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.vaadin.data.Property;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinService;
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
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.ui.renderers.NumberRenderer;
import com.vaadin.ui.themes.ValoTheme;

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

  private final String CAPTION = "Usage/Statistics";

  Button createBill = new Button("create Bill");
  Button downloadBill = new Button("download Bill");
  private GeneratedPropertyContainer gpcontainer;
  
  
  public Statistics() {
    this.setCaption(CAPTION);

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


    VerticalLayout layout = new VerticalLayout();
    layout.addComponent(grid);
    layout.addComponent(createBill);
    downloadBill.setEnabled(false);
    layout.addComponent(downloadBill);
    
    layout.setMargin(true); 
    layout.setSpacing(true); 
    
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
          Notification.show("Bill is ready");
        } catch (Exception e) {
          Notification.show("Error occured while trying to create bill. Please log out and contact your sysadmin",Notification.Type.ERROR_MESSAGE);
          e.printStackTrace();
        }
        
        
      }
      
    });
    setCompositionRoot(layout);

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

  
  public void addRowFilter(Grid grid, final String propertyId) {

  }


}
