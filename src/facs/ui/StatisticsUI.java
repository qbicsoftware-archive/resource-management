package facs.ui;

import java.util.Date;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

import facs.components.Booking;
import facs.components.Statistics;
import facs.model.BookingModel;
import facs.model.FacsModelUtil;

@SuppressWarnings("serial")
@Theme("valo")
public class StatisticsUI extends UI {
  //@WebServlet(value = "/*", asyncSupported = true)
  //@VaadinServletConfiguration(productionMode = false, ui = StatisticsUI.class)
  @Widgetset("com.vaadin.DefaultWidgetSet")
  public static class Servlet extends VaadinServlet {
    
    /**
     * See https://www.liferay.com/community/forums/-/message_boards/message/56507472
     * and https://dev.vaadin.com/ticket/18494. 
     * and https://vaadin.com/blog/-/blogs/how-we-improved-the-startup-time-in-7-5
     * for more information
     */
    /*@Override
    protected boolean allowServePrecompressedResource(javax.servlet.http.HttpServletRequest request,
        java.lang.String url){
      return false;
    }*/
  }
  @Override
  protected void init(VaadinRequest request) {
	  
	  try {
		  BookingModel bookingModel = FacsModelUtil.getNoviceBookingModel();
		  Statistics statistics = new Statistics();
		  setContent(statistics);
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
