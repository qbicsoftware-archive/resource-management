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

import java.util.List;
import java.util.Set;

import com.liferay.portal.model.User;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickEvent;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.themes.ValoTheme;

import facs.db.DBManager;
import facs.model.DeviceBean;

public class Settings extends CustomComponent {
  private static final long serialVersionUID = 2183973381935176872L;
  private Grid devicesGrid;


  public Settings(User user) {

    this.setCaption("Settings");
    TabSheet settings = new TabSheet();
    settings.addStyleName(ValoTheme.TABSHEET_FRAMED);

    settings.addTab(newDeviceGrid());
    // upload csv files of devices
    settings.addTab(new UploadBox());

    setCompositionRoot(settings);
  }


  private Component newDeviceGrid() {

    VerticalLayout devicesLayout = new VerticalLayout();
    devicesLayout.setCaption("Devices");

    HorizontalLayout buttonLayout = new HorizontalLayout();
    Button add = new Button("Add");

    add.setIcon(FontAwesome.PLUS);

    // there will now be space around the test component
    // components added to the test component will now not stick together but have space between
    // them
    devicesLayout.setMargin(true);
    devicesLayout.setSpacing(true);
    buttonLayout.setMargin(true);
    buttonLayout.setSpacing(true);

    add.addClickListener(new ClickListener() {

      @Override
      public void buttonClick(ClickEvent event) {
        addNewDevice();
      }

    });
    buttonLayout.addComponent(add);
    BeanItemContainer<DeviceBean> devices = getDevices();

    GeneratedPropertyContainer gpc = new GeneratedPropertyContainer(devices);
    gpc.addGeneratedProperty("delete", new PropertyValueGenerator<String>() {
      @Override
      public String getValue(Item item, Object itemId, Object propertyId) {
        // return FontAwesome.TRASH_O.getHtml(); // The caption
        return "Delete"; // The caption

      }

      @Override
      public Class<String> getType() {
        return String.class;
      }
    });


    devicesGrid = new Grid(gpc);
    // Create a grid

    // devicesGrid.setWidth("100%");
    devicesGrid.setSizeFull();
    devicesGrid.setSelectionMode(SelectionMode.SINGLE);
    devicesGrid.getColumn("delete").setRenderer(new HtmlRenderer());
    // Render a button that deletes the data row (item)
    devicesGrid.getColumn("delete").setRenderer(
        new ButtonRenderer(new ClickableRenderer.RendererClickListener() {
          @Override
          public void click(RendererClickEvent event) {
            removeDevice((DeviceBean) event.getItemId());
          }
        }));

    // devicesGrid.setEditorEnabled(true);

    devicesLayout.addComponent(buttonLayout);
    devicesLayout.addComponent(devicesGrid);

    // TODO filtering
    // HeaderRow filterRow = devicesGrid.prependHeaderRow();

    return devicesLayout;
  }


  private BeanItemContainer<DeviceBean> getDevices() {
    BeanItemContainer<DeviceBean> devices = new BeanItemContainer<DeviceBean>(DeviceBean.class);
    List<DeviceBean> devs = DBManager.getDatabaseInstance().getDevices();
    assert devs != null;
    devices.addAll(devs);
    return devices;
  }


  protected void removeDevice(DeviceBean db) {
    boolean removed = DBManager.getDatabaseInstance().removeDevice(db);
    if (removed) {
      devicesGrid.getContainerDataSource().removeItem(db);
    } else {
      // TODO log failed operation
      Notification.show("Failed to remove device from database.", Type.ERROR_MESSAGE);
    }
  }


  private void addNewDevice() {
    final Window subWindow = new Window("Add Device");
    FormLayout form = new FormLayout();
    form.setMargin(true);
    final TextField name = new TextField();
    name.setImmediate(true);
    name.addValidator(new StringLengthValidator("The name must be 1-85 letters long (Was {0}).", 1,
        85, true));
    name.setCaption("Name of new device");
    form.addComponent(name);
    final TextArea description = new TextArea();
    description.setImmediate(true);
    description.addValidator(new StringLengthValidator(
        "The name must be 1-255 letters long (Was {0}).", 1, 255, true));
    description.setCaption("Description");
    form.addComponent(description);
    final OptionGroup restricted = new OptionGroup("Is Device restricted by operators?");
    restricted.addItem("yes");
    restricted.setMultiSelect(true);
    form.addComponent(restricted);
    HorizontalLayout buttons = new HorizontalLayout();
    Button save = new Button("save");
    buttons.addComponent(save);
    Button discard = new Button("discard");
    discard
        .setDescription("discarding will abort the process of adding a new device into the databse.");
    buttons.addComponent(discard);
    buttons.setSpacing(true);
    form.addComponent(buttons);
    subWindow.setContent(form);

    form.setMargin(true);
    form.setSpacing(true);
    buttons.setMargin(true);
    buttons.setSpacing(true);

    // Center it in the browser window
    subWindow.center();
    subWindow.setModal(true);
    subWindow.setWidth("50%");
    // Open it in the UI
    UI.getCurrent().addWindow(subWindow);

    discard.addClickListener(new ClickListener() {
      @Override
      public void buttonClick(ClickEvent event) {
        subWindow.close();
      }
    });
    save.addClickListener(new ClickListener() {
      @Override
      public void buttonClick(ClickEvent event) {
        if (name.isValid() && description.isValid()) {
          Set<String> restr = (Set<String>) restricted.getValue();
          int deviceId =
              DBManager.getDatabaseInstance().addDevice(name.getValue(), description.getValue(),
                  (restr.size() == 1));
          DeviceBean bean =
              new DeviceBean(deviceId, name.getValue(), description.getValue(), (restr.size() == 1));
          devicesGrid.addRow(bean);
        } else {
          Notification.show("Failed to add device to database.");
        }
      }
    });

    // DeviceBean db = new DeviceBean(0, "Device 1","some description1", false);
    // TODO
    // add to database
    /*
     * boolean added = false;//DBManager.getDatabaseInstance().addDevice(db); //TODO test //add to
     * grid if(added){ devicesGrid.addRow(db); }else{ //TODO log failed operation
     * Notification.show("Failed to add device to database.", Type.ERROR_MESSAGE); }
     */
  }


}
