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
package facs.ui;

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

import facs.components.Settings;
import facs.components.Statistics;
import facs.model.BookingModel;
import facs.model.FacsModelUtil;

@SuppressWarnings("serial")
@Theme("valo")
public class ServletUI extends UI {

  @WebServlet(value = "/*", asyncSupported = true)
  @VaadinServletConfiguration(productionMode = false, ui = ServletUI.class,
      widgetset = "facs.ui.widgetset.FacsWidgetset")
  public static class Servlet extends VaadinServlet {
  }

  @Override
  protected void init(VaadinRequest request) {
    // Date referenceDate = new java.util.Date();
    // BookingModel bookingModel = FacsModelUtil.getNoviceBookingModel(request.getRemoteUser());
    try {
      BookingModel bookingModel = FacsModelUtil.getNoviceBookingModel();
      TabSheet tabs = new TabSheet();
      // tabs.addComponent(new Booking(bookingModel, referenceDate));

      // statistics
      Statistics statistics = new Statistics();
      tabs.addComponent(statistics);
      tabs.addComponent(new Settings(null));
      setContent(tabs);
    }

    catch (Exception e) {
      setContent(errorView());
      e.printStackTrace();
    }


  }

  private Component errorView() {
    Label label = new Label();
    label.addStyleName(ValoTheme.LABEL_FAILURE);
    label.setIcon(FontAwesome.FROWN_O);
    label
        .setValue("Initialization has failed! Are you logged out? Please try to login! If the problem continues please contact info@qbic.uni-tuebingen.de");
    return label;
  }
}
