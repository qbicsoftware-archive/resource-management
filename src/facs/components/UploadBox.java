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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.text.ParseException;
import java.util.HashMap;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.ProgressListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;

import facs.db.DBManager;
import facs.model.DeviceBean;
import facs.model.MachineOccupationBean;
import facs.utils.Formatter;

public class UploadBox extends CustomComponent implements Receiver, ProgressListener,
    FailedListener, SucceededListener {
  private final String UPLOAD_CAPTION = "Upload Statistics Here";
  private final String CAPTION = "Upload Device Statistics";
  // Put upload in this memory buffer that grows automatically
  ByteArrayOutputStream os = new ByteArrayOutputStream(10240);
  // Name of the uploaded file
  String filename;
  ProgressBar progress = new ProgressBar(0.0f);
  
  //TODO if success or failed show message
  Label finishedMessage = new Label("");
  
  final String  cvsSplitBy = ",";
  private HashMap<String, Integer> deviceNameToId;
  private NativeSelect devices;
  private Grid occupationGrid;
  
  
  public UploadBox() {
    this.setCaption(CAPTION);
    //there has to be a device selected.
    devices = new NativeSelect("Devices");
    devices.setDescription("Select a device in order to upload information for that specific devices.");
    devices.setNullSelectionAllowed(false);
    deviceNameToId = new HashMap<String, Integer>(); 
    for(DeviceBean bean : DBManager.getDatabaseInstance().getDevices()){
      deviceNameToId.put(bean.getName(), bean.getId());
      devices.addItem(bean.getName());
    }
    occupationGrid = new Grid();
    
    // Create the upload component and handle all its events
    final Upload upload = new Upload();
    upload.setReceiver(this);
    upload.addProgressListener(this);
    upload.addFailedListener(this);
    upload.addSucceededListener(this);
    upload.setVisible(false);
  
    //one can only upload csvs, if a device was selected.
    devices.addValueChangeListener(new ValueChangeListener() {
      @Override
      public void valueChange(ValueChangeEvent event) {
        upload.setVisible(event.getProperty().getValue() != null);
      }
    });
    
    // Put the upload and image display in a panel
    Panel panel = new Panel(UPLOAD_CAPTION);
    panel.setWidth("600px");
    VerticalLayout panelContent = new VerticalLayout();
    panelContent.setSpacing(true);
    panel.setContent(panelContent);
    panelContent.addComponent(devices);
    panelContent.addComponent(upload);
    panelContent.addComponent(progress);
    panelContent.addComponent(occupationGrid);
    
    panelContent.setMargin(true); 
    panelContent.setSpacing(true); 
    
    progress.setVisible(false);

    setCompositionRoot(panel);
  }
  
  
  @Override
  public OutputStream receiveUpload(String filename, String mimeType) {
    this.filename = filename;
    os.reset(); // Needed to allow re-uploading
    return os;
  }

  
  @Override
  public void updateProgress(long readBytes, long contentLength) {
    progress.setVisible(true);
    if (contentLength == -1)
      progress.setIndeterminate(true);
    else {
      progress.setIndeterminate(false);
      progress.setValue(((float) readBytes) / ((float) contentLength));
    }
  }
  
  
  @Override
  public void uploadSucceeded(SucceededEvent event) {
    progress.setVisible(false);
    String line = "";

    try {
      BeanItemContainer<MachineOccupationBean> container = new BeanItemContainer<MachineOccupationBean>(MachineOccupationBean.class);
      
      StringReader sr = new StringReader(os.toString());
      BufferedReader br = new BufferedReader(sr);
      
      //skip header
      br.readLine();
      //read body
      while ((line = br.readLine()) != null) {
        String[] userInfo = line.split(cvsSplitBy);
        MachineOccupationBean bean = new MachineOccupationBean();
        bean.setBean(userInfo, deviceNameToId.get((getCurrentDevice())));
        container.addBean(bean);
        //System.out.println(bean.getUserName() + " " + bean.getStart() + " " + bean.getEnd() + " login time: " + Formatter.toHoursAndMinutes(bean.getEnd().getTime() - bean.getStart().getTime()));
        occupationGrid.setContainerDataSource(container);
      }
      addBeansToGrid(container);
    } catch (IOException | ParseException e) {
      e.printStackTrace();
      Notification.show("Can not read uploaded file.",
          Notification.Type.ERROR_MESSAGE);
      return;
    }
    Notification.show("Upload successful",
        Notification.Type.HUMANIZED_MESSAGE);  
  }

  
  private void addBeansToGrid(BeanItemContainer<MachineOccupationBean> container) {
    
  }

  
  private String getCurrentDevice() {
    return (String)devices.getValue();
  }

  
  @Override
  public void uploadFailed(FailedEvent event) {
    Notification.show("Upload failed",
        Notification.Type.ERROR_MESSAGE);   
  }

}
