/*******************************************************************************
 * QBiC Calendar provides an infrastructure for defining calendars for specific purposes like booking devices or
 * planning resources for services and integration of relevant data into the common portal infrastructure.
 * Copyright (C) 2016 Aydın Can Polatkan
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

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.vaadin.client.ui.FontIcon;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.data.util.converter.StringToBigDecimalConverter;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.query.FreeformQuery;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.Button;
import com.vaadin.ui.Calendar;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickEvent;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.themes.ValoTheme;

import de.uni_tuebingen.qbic.main.LiferayAndVaadinUtils;
import elemental.json.JsonArray;
import facs.db.DBManager;
import facs.model.BookingBean;
import facs.model.BookingModel;
import facs.model.UserBean;

public class UserAdmin extends CustomComponent{
  private static final long serialVersionUID = 2183973381935176872L;
  //private Grid devicesGrid;
  private Grid devicesGridConfirm;
  private Grid devicesGridTrash;
  private Grid usersGrid;
  
  private Map<String, Grid> gridMap = new HashMap<String, Grid>();

  private GridLayout gridLayout = new GridLayout(6,6);
  
  public UserAdmin(User user){
	  
	  	Date dNow = new Date();
	  	SimpleDateFormat ft = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
	  	System.out.println(ft.format(dNow) + "  INFO  Calendar User Admin accessed! - User: "+LiferayAndVaadinUtils.getUser().getScreenName());
	  
	  	Label infoLabel = new Label(LiferayAndVaadinUtils.getUser().getScreenName() + " · " + DBManager.getDatabaseInstance().getUserNameByUserID(LiferayAndVaadinUtils.getUser().getScreenName()));
	  	infoLabel.addStyleName("h3");
	  
	  	String buttonTitle = "Refresh";
	  	Button refresh = new Button(buttonTitle);
	  	refresh.setIcon(FontAwesome.REFRESH);
	  	refresh.setSizeFull();
	  	refresh.setDescription("Click here to reload the data from the database!");
	  	refresh.addStyleName(ValoTheme.BUTTON_FRIENDLY);
	  	
	  	Button updateUser = new Button(buttonTitle);
	  	updateUser.setIcon(FontAwesome.WRENCH);
	  	updateUser.setDescription("Click here to update your user role and group!");
	  	
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
				refreshDataSources();
			}
	  	});
	  	
	  	// only admins are allowed to see the admin panel ;)
		if (!DBManager.getDatabaseInstance().getUserAdminPanelAccessByLDAPId(LiferayAndVaadinUtils.getUser().getScreenName()).equals("1")) {
		      VerticalLayout errorLayout = new VerticalLayout();
			  infoLabel.setValue("ACCESS DENIED");
			  errorLayout.addComponent(infoLabel);
			  showErrorNotification("Access Denied!","Sorry, you're not allowed to see anything here, at least your username told us so. Do you need assistance? Please contact 'info@qbic.uni-tuebingen.de'.");
			  setCompositionRoot(errorLayout);
			  return;
		}
		
		this.setCaption("User Manager");

	    final TabSheet userAdmin = new TabSheet();
		userAdmin.addStyleName(ValoTheme.TABSHEET_FRAMED);
		userAdmin.addTab(usersGrid());
	  	
	    userAdmin.addSelectedTabChangeListener(new SelectedTabChangeListener() {

			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {

			}
	  	}
	  	);
	  	   
	    
	    gridLayout.setWidth("100%");	
		  
		//add components to the grid layout
		gridLayout.addComponent(infoLabel,4,0,5,0);	  
		gridLayout.addComponent(userAdmin,0,1,5,1);
		gridLayout.addComponent(refresh,0,2);
		  
		gridLayout.setSpacing(true);
		gridLayout.setSizeFull();
		setCompositionRoot(gridLayout);

    
  }
  
  private Component usersGrid() {
	    VerticalLayout devicesLayout = new VerticalLayout();
	    devicesLayout.setCaption("Users");
	    //HorizontalLayout buttonLayout = new HorizontalLayout();
	    
	    //there will now be space around the test component
	    //components added to the test component will now not stick together but have space between them
	    devicesLayout.setMargin(true); 
	    devicesLayout.setSpacing(true); 
	    
	    BeanItemContainer<UserBean> users = getUsers();
	    
	    GeneratedPropertyContainer gpc = new GeneratedPropertyContainer(users);
	    
	    usersGrid = new Grid(gpc);
	    // Create a grid
	    
	    usersGrid.setWidth("100%");
	    usersGrid.setSelectionMode(SelectionMode.SINGLE);
	    
	    usersGrid.setColumnOrder("id","LDAP","name","email","phone", "workgroup","institute","kostenstelle","project");
	    usersGrid.removeColumn("status");
	    usersGrid.removeColumn("role");
	    
	    usersGrid.setEditorEnabled(true);
	    
	    devicesLayout.addComponent(usersGrid);
	    
	    //TODO filtering
	    //HeaderRow filterRow = devicesGrid.prependHeaderRow();
	    
	    return devicesLayout;
	}
  
  private BeanItemContainer<UserBean> getUsers() {
	    BeanItemContainer<UserBean> userList = new BeanItemContainer<UserBean>(UserBean.class);
	    List<UserBean> users = DBManager.getDatabaseInstance().getUsers();
	    assert users != null;
	    userList.addAll(users);
	    return userList;
}
 
  protected void refreshDataSources() {	  
	  UserAdmin bookAdmin = new UserAdmin(null);
	  setCompositionRoot(bookAdmin);
  }
  

  private void refresh(BeanItemContainer<BookingBean> item) {
      MethodProperty<String> p = (MethodProperty<String>) ((Item) item).getItemProperty("stock");
      p.fireValueChange();
  }
  
  private void showErrorNotification(String title, String description) {
	  Notification notify = new Notification(title,description);
	  notify.setDelayMsec(15000);
	  notify.setPosition(Position.TOP_CENTER);
	  notify.setIcon(FontAwesome.FROWN_O);
	  notify.setStyleName(ValoTheme.NOTIFICATION_ERROR + " " + ValoTheme.NOTIFICATION_CLOSABLE);
	  notify.show(Page.getCurrent());
  }

}
