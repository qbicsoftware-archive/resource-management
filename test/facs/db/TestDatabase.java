package facs.db;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import facs.model.DeviceBean;
import facs.model.MachineOccupationBean;

public class TestDatabase {
  private String hostname = "localhost";
  private String port = "3306";
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
    String jdbcUrl = "jdbc:mysql://" + this.hostname + ":"
        + this.port + "/" + this.sql_database;
    Database.Instance.init(username, password, jdbcUrl);
    db = Database.Instance;
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public void addDevice_device_true() {
    String deviceName = "Device 1";
    String deviceDescription =  "some description1";
    boolean deviceUsageIsRestricted = false;
    int deviceId = db.addDevice(deviceName,deviceDescription,deviceUsageIsRestricted );
    System.out.println(deviceId);
    assertTrue(deviceId != -1);
    DeviceBean devbean = db.getDeviceById(deviceId);
    assertEquals(devbean.getId(), deviceId);
    assertEquals(devbean.getName(), deviceName);
    assertEquals(devbean.getDescription(), deviceDescription);
    assertEquals(devbean.isRestricted(), deviceUsageIsRestricted);
  }

  @Test
  public void addDevice_missing_values_false() {
    String deviceName = "";
    String deviceDescription =  "some description1";
    boolean deviceUsageIsRestricted = false;
    int deviceId = db.addDevice(deviceName,deviceDescription,deviceUsageIsRestricted);
    assertTrue(deviceId == -1);
  }
  @Test
  public void addDevice_null_values_false() {
    String deviceName = null;
    String deviceDescription =  "some description1";
    boolean deviceUsageIsRestricted = false;
    int deviceId = db.addDevice(deviceName,deviceDescription,deviceUsageIsRestricted);
    assertTrue(deviceId == -1);
  }
  
  @Test
  public void removeDevice_true(){
    String deviceName = "delete";
    String deviceDescription =  "deleted";
    boolean deviceUsageIsRestricted = false;
    int deviceId = db.addDevice(deviceName,deviceDescription,deviceUsageIsRestricted );
    boolean success = db.removeDevice(deviceId);
    assertTrue(success);
    DeviceBean devbean = db.getDeviceById(deviceId);
    assertTrue(devbean == null);  
  }
  @Test
  public void removeDevice_false(){
    boolean success = db.removeDevice(50000);
    assertTrue(!success);
  }
  
  @Test 
  public void updateDevice_true(){
    String deviceName = "newDevice";
    String deviceDescription =  "new resource";
    boolean deviceUsageIsRestricted = false;
    int deviceId = db.addDevice(deviceName,deviceDescription,deviceUsageIsRestricted );
    DeviceBean bean = db.getDeviceById(deviceId);
    bean.setName("updatedDeviceName");
    bean.setDescription("updated resource");
    bean.setRestricted(true);
    db.updateDevice(bean);
    DeviceBean updatedBean = db.getDeviceById(deviceId);
    assertEquals(bean.getId(), updatedBean.getId());
    assertEquals(bean.getName(), updatedBean.getName());
    assertEquals(bean.getDescription(), updatedBean.getDescription());
    assertEquals(bean.isRestricted(), updatedBean.isRestricted());    
  }
  
  @Test
  public void addEvent_true(){
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
  public void getUserByid(){
    db.getUserById(6);
  }
  
  @Test
  public void getPhysicalBlocks(){
    List<MachineOccupationBean> mbeans = db.getPhysicalTimeBlocks();
    for(MachineOccupationBean bean: mbeans){
      System.out.println(bean.getDeviceId());
      System.out.println(bean.getStart());
      System.out.println(bean.getEnd());
    }
    //what kind of test is that?
    fail();
  }
  
  @Test
  public void updateEvent(){
    fail();
  }
  
  @Test
  public void removeEvent(){
    fail();
  }
  
  @Test
  public void confirmEvent(){
    fail();
  }
  
  
  
  
}
