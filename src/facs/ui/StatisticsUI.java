package facs.ui;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;

import facs.components.Statistics;

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
  Statistics statistics = new Statistics();
  setContent(statistics);
  }
}
