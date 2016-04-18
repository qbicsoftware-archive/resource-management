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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.ProgressListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import facs.db.DBManager;
import facs.model.DeviceBean;
import facs.model.MachineOccupationBean;

public class UploadBox extends CustomComponent implements Receiver, ProgressListener,
    FailedListener, SucceededListener {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private final String UPLOAD_CAPTION = "Upload Statistics Here";
  private final String CAPTION = "Upload Device Statistics";
  // Put upload in this memory buffer that grows automatically
  ByteArrayOutputStream os = new ByteArrayOutputStream(10240);
  // Name of the uploaded file
  String filename;
  ProgressBar progress = new ProgressBar(0.0f);

  // TODO if success or failed show message
  Label finishedMessage = new Label("Finito!");

  final String cvsSplitBy = ",";
  private HashMap<String, Integer> deviceNameToId;
  private NativeSelect devices;
  private Grid occupationGrid;


  public UploadBox() {
    this.setCaption(CAPTION);
    // there has to be a device selected.
    devices = new NativeSelect("Devices");
    devices
        .setDescription("Select a device in order to upload information for that specific devices.");
    devices.setNullSelectionAllowed(false);
    deviceNameToId = new HashMap<String, Integer>();
    for (DeviceBean bean : DBManager.getDatabaseInstance().getDevices()) {
      deviceNameToId.put(bean.getName(), bean.getId());
      devices.addItem(bean.getName());
      // System.out.println("Bean.getName: " + bean.getName() + " Bean.getId: " + bean.getId());
    }
    occupationGrid = new Grid();
    occupationGrid.setSizeFull();

    // Create the upload component and handle all its events
    final Upload upload = new Upload();
    upload.setReceiver(this);
    upload.addProgressListener(this);
    upload.addFailedListener(this);
    upload.addSucceededListener(this);
    upload.setVisible(false);

    // one can only upload csvs, if a device was selected.
    devices.addValueChangeListener(new ValueChangeListener() {

      /**
       * 
       */
      private static final long serialVersionUID = 7890499571475184208L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        upload.setVisible(event.getProperty().getValue() != null);
      }
    });

    // Put the upload and image display in a panel
    // Panel panel = new Panel(UPLOAD_CAPTION);
    // panel.setWidth("100%");
    VerticalLayout panelContent = new VerticalLayout();
    panelContent.setSpacing(true);
    // panel.setContent(panelContent);
    panelContent.addComponent(devices);
    panelContent.addComponent(upload);
    panelContent.addComponent(progress);
    panelContent.addComponent(occupationGrid);

    panelContent.setMargin(true);
    panelContent.setSpacing(true);

    progress.setVisible(false);

    setCompositionRoot(panelContent);
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
      BeanItemContainer<MachineOccupationBean> container =
          new BeanItemContainer<MachineOccupationBean>(MachineOccupationBean.class);

      StringReader sr = new StringReader(os.toString());
      BufferedReader br = new BufferedReader(sr);

      // skip header
      br.readLine();
      // read body
      while ((line = br.readLine()) != null) {
        String[] userInfo = line.split(cvsSplitBy);
        MachineOccupationBean bean = new MachineOccupationBean();
        bean.setBean(userInfo, deviceNameToId.get((getCurrentDevice())), deviceNameToId);
        // System.out.println("UserInfo: " + userInfo + " deviceNameToId: "
        // + deviceNameToId.get((getCurrentDevice())) + " " + deviceNameToId);
        try {
          bean.setBean(userInfo, deviceNameToId.get((getCurrentDevice())));
        } catch (Exception e) {
          // e.printStackTrace();
        }
        container.addBean(bean);
        System.out.println(bean.getUserName() + " " + bean.getStart() + " " + bean.getEnd() + " "
            + bean.getDepartment());

        // TODO: write the function to register the data into the DB.

        // System.out.println(" login time: "+ Formatter.toHoursAndMinutes(bean.getEnd().getTime() -
        // bean.getStart().getTime()));
        occupationGrid.setContainerDataSource(container);
      }
      // addBeansToGrid(container);
    } catch (IOException e) {
      e.printStackTrace();
      showErrorNotification("Can't read uploaded file.",
          "An error occured during the upload process. Make sure that you didn't upload a corrupted file.");
      return;
    }
    showSuccessfulNotification("Nailed it! Upload successful.",
        "Hopefully all good! Successful uploads expectedly lead to successful parsing of the documents.");
  }

  private void If(boolean b) {
    // TODO Auto-generated method stub

  }


  private void showErrorNotification(String title, String description) {
    Notification notify = new Notification(title, description);
    notify.setDelayMsec(16000);
    notify.setPosition(Position.TOP_CENTER);
    notify.setIcon(FontAwesome.FROWN_O);
    notify.setStyleName(ValoTheme.NOTIFICATION_ERROR + " " + ValoTheme.NOTIFICATION_CLOSABLE);
    notify.show(Page.getCurrent());
  }

  private void showNotification(String title, String description) {
    Notification notify = new Notification(title, description);
    notify.setDelayMsec(8000);
    notify.setPosition(Position.TOP_CENTER);
    notify.setIcon(FontAwesome.MEH_O);
    notify.setStyleName(ValoTheme.NOTIFICATION_TRAY + " " + ValoTheme.NOTIFICATION_CLOSABLE);
    notify.show(Page.getCurrent());
  }

  private void showSuccessfulNotification(String title, String description) {
    Notification notify = new Notification(title, description);
    notify.setDelayMsec(8000);
    notify.setPosition(Position.TOP_CENTER);
    notify.setIcon(FontAwesome.SMILE_O);
    notify.setStyleName(ValoTheme.NOTIFICATION_SUCCESS + " " + ValoTheme.NOTIFICATION_CLOSABLE);
    notify.show(Page.getCurrent());
  }


  private void addBeansToGrid(BeanItemContainer<MachineOccupationBean> container) {

  }


  private String getCurrentDevice() {
    return (String) devices.getValue();
  }


  @Override
  public void uploadFailed(FailedEvent event) {
    showErrorNotification(
        "oops! Upload failed, reason unknown!",
        "Obviously there is a problem, please ask yourself the following questions: Did I do everything correctly? Did I focus on my work? If the answers are 'Yes' please call a technician.");
  }

}
