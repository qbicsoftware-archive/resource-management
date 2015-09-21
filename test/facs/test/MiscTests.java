package facs.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import facs.db.DBManager;
import facs.model.DeviceBean;
import facs.model.InstituteBean;
import facs.model.MachineOccupationBean;
import facs.model.UserBean;
import facs.utils.GenericFacsParser;

public class MiscTests {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {}

  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void test() {
    SimpleDateFormat stmp = new SimpleDateFormat("hh:mm:ss a MMMMMM dd yyyy");
    try {

      System.out.println(stmp.parse("3:40:58 PM June 1 2015"));
      System.out.println(stmp.parse("12:22:47 PM June 10 2015"));
      System.out.println(stmp.parse("3:40:58 AM June 1 2015"));
      System.out.println(stmp.parse("12:22:47 AM June 10 2015"));
      System.out.println(stmp.parse("12:36:14 PM August 3 2015"));
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void userInfo() {

    String line = "";
    String cvsSplitBy = ",";


    try (BufferedReader in =
        new BufferedReader(new FileReader("/home/wojnar/QBiC/Facs/2015-08-19_Userliste.csv"))) {
      // skip header
      readLine(in, cvsSplitBy, 22);
      // read body
      String[] userInfo = null;
      ArrayList<UserBean> user = new ArrayList<UserBean>();
      HashSet<String> arbeitsgruppen = new HashSet<String>();
      HashSet<InstituteBean> institutes = new HashSet<InstituteBean>();
      HashSet<String> kostenstellen = new HashSet<String>();
      HashSet<String> projekts = new HashSet<String>();
      HashSet<String> categories = new HashSet<String>();

      while ((userInfo = readLine(in, cvsSplitBy, 22)) != null) {
        System.out.println(userInfo.length);
        if ("Admin".equals(userInfo[0]) || "Abteilung".equals(userInfo[0])
            || "Intern ".equals(userInfo[0]) || "Extern 1".equals(userInfo[0])
            || "Extern 2".equals(userInfo[0]))
          continue;

        UserBean ubean = new UserBean();

        ubean.setName(userInfo[0]); // name
        ubean.setWorkinggroup(userInfo[2]);// arbeitsgruppe
        ubean.setInstitute(userInfo[3]);// institute
        ubean.setEmail(userInfo[7]);// email
        ubean.setPhone(userInfo[8]);// telephon
        ubean.getKostenstelle().add(userInfo[9]);// kostenstelle
        ubean.setRole("unknown"); // make it a standard?
        user.add(ubean);

        arbeitsgruppen.add(userInfo[2]);
        kostenstellen.add(userInfo[9]);
        projekts.add(userInfo[10]);
        categories.add(userInfo[13]);
        categories.add(userInfo[16]);
        categories.add(userInfo[19]);
        categories.add(userInfo[21]);

        InstituteBean ibtmp = new InstituteBean();
        ibtmp.setName(userInfo[3]); // institute
        ibtmp.setStreet(userInfo[4]);// strasse
        ibtmp.setPostalCode(userInfo[5]);// plz
        ibtmp.setCity(userInfo[6]);// ort
        institutes.add(ibtmp);
      }
      for (InstituteBean insti : institutes) {
        System.out.println(insti.toString());
        int id =
            DBManager.getDatabaseInstance().addInstitute(insti.getName(), insti.getPostalCode(),
                insti.getCity(), insti.getStreet(), insti.getShortName());
        System.out.println(id);
      }
      for (String ag : arbeitsgruppen) {
        System.out.println(ag);
        DBManager.getDatabaseInstance().addWorkingGroup(ag);
      }
      // TODO DANGER OF ERROR SPAMMING DUE TO DUPLICATES
      for (String ag : kostenstellen) {
        System.out.println(ag);
        DBManager.getDatabaseInstance().addKostenstelle(ag, "");
      }/*
        * //TODO TOTALLY WRONG for(String ag: categories){ System.out.println(ag);
        * DBManager.getDatabaseInstance().addCategory(ag); }
        */
      /*
       * TODO ROLE MAKES ERRORS INTRODUE role unkown if it is empty might solve the issue.
       */
      // INSERT INTO role (name) VALUES ('unknown');
      DBManager.getDatabaseInstance().addRole("unknown");

      for (UserBean u : user) {
        System.out.println(u.toString());
        int userId =
            DBManager.getDatabaseInstance().addUser(u.getName(), u.getWorkinggroup(),
                u.getInstitute(), u.getEmail(), u.getRole(), u.getPhone());
        for (String k : u.getKostenstelle()) {
          DBManager.getDatabaseInstance().addKostenStelleToUser(userId, k);
        }

      }

      /*
       * //These should be added once. 
      DBManager.getDatabaseInstance().addDevice("FC500","No desc.","No desc." ,false);
      DBManager.getDatabaseInstance().addDevice("Canto", "No desc.","No desc.", false);
      DBManager.getDatabaseInstance().addDevice("LSR Fortessa", "No desc.", "No desc.", false);
      DBManager.getDatabaseInstance().addDevice("Aria1", "No desc.","No desc.", true);
      DBManager.getDatabaseInstance().addDevice("Aria2", "No desc.", "No desc.", true);
       * 
       * //init script? System.out.println(
       * "INSERT INTO resources (name, descr, short_desc, restricted) VALUES ('FC500', 'No desc.', 'No desc.', 0);"
       * ); System.out.println(
       * "INSERT INTO resources (name, descr, short_desc, restricted) VALUES ('Canto', 'No desc.', 'No desc.', 0);"
       * ); System.out.println(
       * "INSERT INTO resources (name, descr, short_desc, restricted) VALUES ('LSR Fortessa', 'No desc.', 'No desc.', 0);"
       * ); System.out.println(
       * "INSERT INTO resources (name, descr, short_desc, restricted) VALUES ('Aria1', 'No desc.', 'No desc.', 1);"
       * ); System.out.println(
       * "INSERT INTO resources (name, descr, short_desc, restricted) VALUES ('Aria2', 'No desc.', 'No desc.', 1);"
       * );
       */
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
  
  @Test
  public void addGroupsAndCosts(){
    boolean success = DBManager.getDatabaseInstance().addUserGroup("Admin");
    DBManager.getDatabaseInstance().addUserGroup("Abteilung");
    DBManager.getDatabaseInstance().addUserGroup("Intern");
    DBManager.getDatabaseInstance().addUserGroup("Extern 1");
    DBManager.getDatabaseInstance().addUserGroup("Extern 2");
    
    List<DeviceBean> devices = DBManager.getDatabaseInstance().getDevices();
    for(DeviceBean device: devices){
      switch (device.getName()){
        case "FC500":{
          DBManager.getDatabaseInstance().addResourceCostPerGroup(device.getId(),"Admin", 0);
          DBManager.getDatabaseInstance().addResourceCostPerGroup(device.getId(),"Abteilung", 5);
          DBManager.getDatabaseInstance().addResourceCostPerGroup(device.getId(),"Intern", 10);
          DBManager.getDatabaseInstance().addResourceCostPerGroup(device.getId(),"Extern 1", 20);
          DBManager.getDatabaseInstance().addResourceCostPerGroup(device.getId(),"Extern 2", 30);
        }
        case "Canto":{
          DBManager.getDatabaseInstance().addResourceCostPerGroup(device.getId(),"Admin", 0);
          DBManager.getDatabaseInstance().addResourceCostPerGroup(device.getId(),"Abteilung", 5);
          DBManager.getDatabaseInstance().addResourceCostPerGroup(device.getId(),"Intern", 10);
          DBManager.getDatabaseInstance().addResourceCostPerGroup(device.getId(),"Extern 1", 20);
          DBManager.getDatabaseInstance().addResourceCostPerGroup(device.getId(),"Extern 2", 30);          
        }
        case "LSR Fortessa":{
          DBManager.getDatabaseInstance().addResourceCostPerGroup(device.getId(),"Admin", 0);
          DBManager.getDatabaseInstance().addResourceCostPerGroup(device.getId(),"Abteilung", 7.5f);
          DBManager.getDatabaseInstance().addResourceCostPerGroup(device.getId(),"Intern", 25);
          DBManager.getDatabaseInstance().addResourceCostPerGroup(device.getId(),"Extern 1", 30);
          DBManager.getDatabaseInstance().addResourceCostPerGroup(device.getId(),"Extern 2", 40);          
        }
        case "Aria1":
        case "Aria2":
          DBManager.getDatabaseInstance().addResourceCostPerGroup(device.getId(),"Admin", 0);
          DBManager.getDatabaseInstance().addResourceCostPerGroup(device.getId(),"Abteilung", 10);
          DBManager.getDatabaseInstance().addResourceCostPerGroup(device.getId(),"Intern", 85);
          DBManager.getDatabaseInstance().addResourceCostPerGroup(device.getId(),"Extern 1", 100);
          DBManager.getDatabaseInstance().addResourceCostPerGroup(device.getId(),"Extern 2", 125);          
        default:
    }
    }
      
      //DBManager.getDatabaseInstance().addResourceCostPerGroup(device.getId(),"Admin", cost.get(new AbstractMap.SimpleEntry<Integer,String>(device.getId(), "Admin")));
    
    
  }
  @Test
  public void importPhysicalDeviceCsvs(){
    GenericFacsParser parser = new GenericFacsParser();
    String[] files =
        {"/home/wojnar/QBiC/Facs/2015_September_Canto.csv",
            "/home/wojnar/QBiC/Facs/2015_September_Fortessa.csv",
            "/home/wojnar/QBiC/Facs/2015_August_Fortessa.csv",
            "/home/wojnar/QBiC/Facs/2015_June.csv"};
      System.out.println("---------------------------------------");
      write(parser, files[0], 2);
      write(parser, files[1], 3);
      write(parser, files[2], 3);
      write(parser, files[3], 1);
  }
  
  //not a test
  void write(GenericFacsParser parser, String file, int deviceId){
    try (BufferedReader in = new BufferedReader(new FileReader(file))) {
      List<MachineOccupationBean> test = parser.parse(in, deviceId);
      for (MachineOccupationBean m : test) {
        System.out.println(m.getDeviceId() + " " + m.getUserName() + " " + m.getUserFullName()
            + " " + m.getInstitution() + " " + m.getStart() + " " + m.getEnd() + " corrupted: "
            + m.isCorrupted());
        int id = DBManager.getDatabaseInstance().isPhysicalTimeBlock(m.getDeviceId(),m.getUserName(),m.getUserFullName(),m.getStart(),m.getEnd());
        if(id == -1){
          boolean success = DBManager.getDatabaseInstance().addPhysicalTimeBlock(m.getDeviceId(),m.getUserName(),m.getUserFullName(),m.getStart(),m.getEnd());
          System.out.println(success);
        }else{
          System.out.println("Already in database with id: "+ id);
        }
      }
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  @Test
  public void mapUserToDevice() {
    String line = "";
    String split = ",";
    String[] userInfo = null;
    HashMap<String, String> users = new HashMap<String, String>();
    try (BufferedReader in =
        new BufferedReader(new FileReader("/home/wojnar/QBiC/Facs/2015_June.csv"))) {
      // skip header
      in.readLine();
      while ((line = in.readLine()) != null) {
        userInfo = line.split(split, -1);
        String user = userInfo[0]; // username
        String fullName = userInfo[1];
        users.put(user, fullName);
      }
      Iterator<Entry<String, String>> iter = users.entrySet().iterator();
      while (iter.hasNext()) {
        Entry<String, String> entry = iter.next();
        String user = entry.getKey(); // username
        String fullName = entry.getValue();
        // try to find them in the database:
        int userId = DBManager.getDatabaseInstance().findUserByDeviceUserId(user);
        Set<Integer> userIds = new HashSet<Integer>();
        if (userId == -1) {
          userId = DBManager.getDatabaseInstance().findUserByFullName(fullName);
        }
        if (userId == -1) {
          userIds = DBManager.getDatabaseInstance().matchDeviceUserIdToUserName(user);
        }

        if (userId == -1) {
          System.out.println("possible matches for: " + user + " full name: '" + fullName
              + "' are:");
          for (Integer i : userIds) {
            System.out.print(i);
            System.out.print(" ");
          }
          System.out.println();
        } else {
          System.out.println("Match for: " + user + " full name: '" + fullName + "' is: " + userId);
        }
      }

    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Test
  public void headerNumbers() {
    GenericFacsParser parser = new GenericFacsParser();
    String[] files =
        {"/home/wojnar/QBiC/Facs/2015_September_Canto.csv",
            "/home/wojnar/QBiC/Facs/2015_September_Fortessa.csv",
            "/home/wojnar/QBiC/Facs/2015_August_Fortessa.csv",
            "/home/wojnar/QBiC/Facs/2015_June.csv"};
    for (String f : files) {
      try (BufferedReader in = new BufferedReader(new FileReader(f))) {
        String line = in.readLine();
        String[] info = parser.lineToColumns(line);
        Map<String, Integer> headerNumbers = MachineOccupationBean.getHeaderNumbers(info);
        Set<Entry<String, Integer>> entrySet = headerNumbers.entrySet();
        Iterator<Entry<String, Integer>> iter = entrySet.iterator();
        while (iter.hasNext()) {
          Entry<String, Integer> entry = iter.next();
          System.out.println(entry.getKey() + "  " + entry.getValue());
        }
        System.out.println("-------------------------------------");
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  @Test
  public void mapUserToDevice2() {
    GenericFacsParser parser = new GenericFacsParser();
    String[] files =
        {"/home/wojnar/QBiC/Facs/2015_September_Canto.csv",
            "/home/wojnar/QBiC/Facs/2015_September_Fortessa.csv",
            "/home/wojnar/QBiC/Facs/2015_August_Fortessa.csv",
            "/home/wojnar/QBiC/Facs/2015_June.csv"};
    int deviceId = 0;
    for (String f : files) {
      System.out.println("---------------------------------------");
      deviceId++;
      try (BufferedReader in = new BufferedReader(new FileReader(f))) {
        List<MachineOccupationBean> test = parser.parse(in, deviceId);
        for (MachineOccupationBean m : test) {
          System.out.println(m.getDeviceId() + " " + m.getUserName() + " " + m.getUserFullName()
              + " " + m.getInstitution() + " " + m.getStart() + " " + m.getEnd() + " corrupted: "
              + m.isCorrupted());
        }



        System.out.println("#####################################");
        HashMap<String, String> users = new HashMap<String, String>();
        for (MachineOccupationBean m : test) {
          users.put(m.getUserName(), m.getUserFullName());
        }
        Iterator<Entry<String, String>> iter = users.entrySet().iterator();
        while (iter.hasNext()) {
          Entry<String, String> entry = iter.next();
          String user = entry.getKey(); // username
          String fullName = entry.getValue();
          // try to find them in the database:
          int userId = DBManager.getDatabaseInstance().findUserByDeviceUserId(user);
          Set<Integer> userIds = new HashSet<Integer>();
          if (userId == -1) {
            userId = DBManager.getDatabaseInstance().findUserByFullName(fullName);
          }
          if (userId == -1) {
            userIds = DBManager.getDatabaseInstance().matchDeviceUserIdToUserName(user);
          }

          if (userId == -1) {
            System.out.println("possible matches for: " + user + " full name: '" + fullName
                + "' are:");
            for (Integer i : userIds) {
              System.out.print(i);
              System.out.print(" ");
            }
            System.out.println();
          } else {
            System.out.println("Match for: " + user + " full name: '" + fullName + "' is: "
                + userId);
          }
        }
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ParseException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

  }

  @Test
  public void delimiter() {
    String line = "abc, \";asdf\", def";
    GenericFacsParser parser = new GenericFacsParser();
    // parser.lineToColumns(line);
    String[] info = line.split(",");
    for (String s : info) {
      System.out.print(s);
      System.out.println(",");
    }
    System.out.println();
    String[] infostripped = StringUtils.stripAll(StringUtils.stripAll(info), "\"");
    for (String s : infostripped) {
      System.out.print(s);
      System.out.println(",");
    }
  }
  
  //not a test
  /**
   * skip empty lines
   * 
   * @param in
   * @return
   * @throws IOException
   */
  private String[] readLine(BufferedReader in, String split, int numberOfColumns)
      throws IOException {
    boolean isEmpty = true;
    String[] userInfo = null;
    String line = "";
    while (isEmpty && (line = in.readLine()) != null) {
      if (line.trim().length() != numberOfColumns) {
        userInfo = line.split(split, -1);
        isEmpty = false;
      }
    }
    return userInfo;
  }

}
