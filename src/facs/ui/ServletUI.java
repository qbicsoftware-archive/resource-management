package facs.ui;

import java.util.Date;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

import facs.components.BookAdmin;
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
    //Date referenceDate = new java.util.Date();
    //BookingModel bookingModel = FacsModelUtil.getNoviceBookingModel(request.getRemoteUser());
	try {
		BookingModel bookingModel = FacsModelUtil.getNoviceBookingModel();
	    TabSheet tabs = new TabSheet();
	    //tabs.addComponent(new Booking(bookingModel, referenceDate));

	    // statistics
	    Statistics statistics = new Statistics();
	    tabs.addComponent(statistics);
	    tabs.addComponent(new Settings(null));
	    setContent(tabs);
	}
	
	catch(Exception e){
		  setContent(errorView());
		  e.printStackTrace();
	}
	
	
  }
  
  private Component errorView() {
	    Label label = new Label();
	    label.addStyleName(ValoTheme.LABEL_FAILURE);
	    label.setIcon(FontAwesome.FROWN_O);
	    label.setValue("Initialization has failed! Are you logged out? Please try to login! If the problem continues please contact info@qbic.uni-tuebingen.de");
	    return label;
  }
}
