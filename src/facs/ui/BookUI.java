package facs.ui;

import java.util.Date;

import javax.servlet.annotation.WebServlet;

import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

import facs.components.Booking;
import facs.components.Statistics;
import facs.model.BookingModel;
import facs.model.FacsModelUtil;

@SuppressWarnings("serial")
@Theme("valo")
public class BookUI extends UI {

  //@WebServlet(value = "/*", asyncSupported = true)
  //@VaadinServletConfiguration(productionMode = false, ui = BookUI.class)
  @Widgetset("com.vaadin.DefaultWidgetSet")
  public static class Servlet extends VaadinServlet {
    
    /*@Override
    protected boolean allowServePrecompressedResource(javax.servlet.http.HttpServletRequest request,
        java.lang.String url){
      return false;
    }*/
  }

  /**
   * color1: lighter blue color2: green color3: orange color4: red color5: blue
   * 
   * 
   */

  @Override
  protected void init(VaadinRequest request) {
    Date referenceDate = new java.util.Date();
    try{
      User user = UserLocalServiceUtil.getUserById(Long.valueOf(request.getRemoteUser()));
      
      BookingModel bookingModel = FacsModelUtil.getNoviceBookingModel(request.getRemoteUser());
      BookingModel model = new BookingModel(user);
      setContent(new Booking(bookingModel, referenceDate));
    }catch(Exception e){
      setContent(errorView());
      e.printStackTrace();
    }
  }

  private Component errorView() {
    Label label = new Label();
    label.addStyleName(ValoTheme.LABEL_FAILURE);
    label.setValue("We are really sorry, but initilization failed.");
    return label;
  }
  
  

}
