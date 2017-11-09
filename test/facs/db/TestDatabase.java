/*******************************************************************************
 * QBiC Calendar provides an infrastructure for defining calendars for specific purposes like
 * booking devices or planning resources for services and integration of relevant data into the
 * common portal infrastructure. Copyright (C) 2017 Aydın Can Polatkan & David Wojnar
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
package facs.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import facs.model.DeviceBean;
import facs.model.MachineOccupationBean;

public class TestDatabase {
  private String hostname = "localhost";
  private String port = "8889";
  private String sql_database = "facs_facility";
  private String username = "facs";
  private String password = "facs";
  private Database db;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  @Before
  public void setUp() throws Exception {
    String jdbcUrl = "jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.sql_database;
    Database.Instance.init(username, password, jdbcUrl);
    db = Database.Instance;
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public void addDevice_device_true() {
    String deviceName = "Device 1";
    String deviceDescription = "some description1";
    boolean deviceUsageIsRestricted = false;
    int deviceId = db.addDevice(deviceName, deviceDescription, deviceUsageIsRestricted);
    System.out.println("DeviceId: " + deviceId);
    assertTrue(deviceId != -1);
    DeviceBean devbean = db.getDeviceById(deviceId);
    assertEquals(devbean.getId(), deviceId);
    assertEquals(devbean.getName(), deviceName);
    assertEquals(devbean.getDescription(), deviceDescription);
    assertEquals(devbean.getRestriction(), deviceUsageIsRestricted);
    System.out.println("Devbean: " + deviceName);
  }

  @Test
  public void addDevice_missing_values_false() {
    String deviceName = "";
    String deviceDescription = "some description1";
    boolean deviceUsageIsRestricted = false;
    int deviceId = db.addDevice(deviceName, deviceDescription, deviceUsageIsRestricted);
    assertTrue(deviceId == -1);
  }

  @Test
  public void addDevice_null_values_false() {
    String deviceName = null;
    String deviceDescription = "some description1";
    boolean deviceUsageIsRestricted = false;
    int deviceId = db.addDevice(deviceName, deviceDescription, deviceUsageIsRestricted);
    assertTrue(deviceId == -1);
  }

  @Test
  public void removeDevice_true() {
    String deviceName = "delete";
    String deviceDescription = "deleted";
    boolean deviceUsageIsRestricted = false;
    int deviceId = db.addDevice(deviceName, deviceDescription, deviceUsageIsRestricted);
    boolean success = db.removeDevice(deviceId);
    assertTrue(success);
    DeviceBean devbean = db.getDeviceById(deviceId);
    assertTrue(devbean == null);
  }

  @Test
  public void removeDevice_false() {
    boolean success = db.removeDevice(50000);
    assertTrue(!success);
  }

  @Test
  public void updateDevice_true() {
    String deviceName = "newDevice";
    String deviceDescription = "new resource";
    boolean deviceUsageIsRestricted = false;
    int deviceId = db.addDevice(deviceName, deviceDescription, deviceUsageIsRestricted);
    DeviceBean bean = db.getDeviceById(deviceId);
    bean.setName("updatedDeviceName");
    bean.setDescription("updated resource");
    bean.setRestriction(true);
    db.updateDevice(bean);
    DeviceBean updatedBean = db.getDeviceById(deviceId);
    assertEquals(bean.getId(), updatedBean.getId());
    assertEquals(bean.getName(), updatedBean.getName());
    assertEquals(bean.getDescription(), updatedBean.getDescription());
    assertEquals(bean.getRestriction(), updatedBean.getRestriction());
  }

  @Test
  public void addEvent_true() {
    int deviceId = 1;
    int userId = 1;
    Date start = new java.util.Date();
    GregorianCalendar end = new GregorianCalendar();
    end.setTime(start);
    end.add(java.util.Calendar.HOUR, 1);
    String name = "";
    String description = "";
    int kostenstellenId = 1;

  }

  @Test
  public void getUserByid() {
    db.getUserById(6);
  }

  @Test
  public void getCostByResourceAndUserIds() {
    float cost = db.getCostByResourceAndUserIds(7, 1);
    System.out.println(cost);
  }

  @Test
  public void getPhysicalBlocks() {
    List<MachineOccupationBean> mbeans = db.getPhysicalTimeBlocks();
    for (MachineOccupationBean bean : mbeans) {
      System.out.println(bean.getDeviceId());
      System.out.println(bean.getStart());
      System.out.println(bean.getEnd());
    }
    // what kind of test is that?
    fail();
  }

  @Test
  public void updateEvent() {
    fail();
  }

  @Test
  public void removeEvent() {
    fail();
  }

  @Test
  public void confirmEvent() {
    fail();
  }



}
