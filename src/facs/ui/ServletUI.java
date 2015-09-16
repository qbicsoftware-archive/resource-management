package facs.ui;

import java.util.Date;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;

import facs.components.Booking;
import facs.components.Settings;
import facs.components.Statistics;
import facs.model.BookingModel;
import facs.model.FacsModelUtil;


@SuppressWarnings("serial")
@Theme("valo")
public class ServletUI extends UI {

  @WebServlet(value = "/*", asyncSupported = true)
  @VaadinServletConfiguration(productionMode = false, ui = ServletUI.class, widgetset = "facs.ui.widgetset.FacsWidgetset")
  public static class Servlet extends VaadinServlet {
  }

  @Override
  protected void init(VaadinRequest request) {
    Date referenceDate = new java.util.Date();
    BookingModel bookingModel = FacsModelUtil.getNoviceBookingModel(request.getRemoteUser());

    TabSheet tabs = new TabSheet();
    tabs.addComponent(new Booking(bookingModel, referenceDate));

    // statistics
    Statistics statistics = new Statistics();
    tabs.addComponent(statistics);
    
    tabs.addComponent(new Settings(null));

    setContent(tabs);
  }
}
