package facs.db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import facs.model.DeviceBean;

public enum Database {
  Instance;

  private String password;
  private String user;
  private String host;
  Connection conn = null;

  public void init(String user, String password, String host) {
    // check if com.mysql.jdbc.Driver exists. If not try to add it
    String mysqlDriverName = "com.mysql.jdbc.Driver";
    Enumeration<Driver> tmp = DriverManager.getDrivers();
    boolean existsDriver = false;
    while (tmp.hasMoreElements()) {
      Driver d = tmp.nextElement();
      if (d.toString().equals(mysqlDriverName)) {
        existsDriver = true;
        break;
      }
      // System.out.println(d.toString());
    }
    if (!existsDriver) {
      // Register JDBC driver
      // According http://docs.oracle.com/javase/6/docs/api/java/sql/DriverManager.html
      // this should not be needed anymore. But without it I get the following error:
      // java.sql.SQLException: No suitable driver found for
      // jdbc:mysql://localhost:3306/facs_facility
      // Does not work for serlvets, just for portlets :(
      try {
        Class.forName(mysqlDriverName);
      } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    this.password = password;
    this.user = user;
    this.host = host;
  }


  public void changeTimeBlock() {

  }

  public void removeTimeBlock() {

  }

  public void addTimeBlock() {

  }

  public int addUsedTimeBlock(String userName, String fullName, String application, String role,
      String departement, String institution, Date login, Date logout, String buildVersion,
      String cytometer, String serialNo, String custom) {
    int usedTimeBlockId = -1;
    if (userName == null || userName.isEmpty() || institution == null || institution.isEmpty()) {
      return usedTimeBlockId;
    }
    String sql =
        "INSERT INTO resource_occupation (deviceId, userId, device_user_name, full_name, application, role, departement, institution, login, logout, buildversion, cytometer, serialno, custom) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, userName);
      // TODO
      // !!!
      statement.setString(2, stringOrEmpty(fullName));
      statement.setString(3, stringOrEmpty(fullName));
      statement.setString(4, stringOrEmpty(fullName));
      statement.setString(5, stringOrEmpty(fullName));
      statement.setString(6, stringOrEmpty(fullName));
      statement.setString(7, stringOrEmpty(fullName));
      statement.setString(8, stringOrEmpty(fullName));
      statement.setString(9, stringOrEmpty(fullName));
      statement.setString(10, stringOrEmpty(fullName));
      statement.setString(11, stringOrEmpty(fullName));
      // execute the statement, data IS NOT commit yet
      statement.execute();
      ResultSet rs = statement.getGeneratedKeys();
      if (rs.next()) {
        usedTimeBlockId = rs.getInt(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return usedTimeBlockId;
  }

  /**
   * returns either str or an empty string if str is null
   * 
   * @param str
   * @return
   */
  private String stringOrEmpty(String str) {
    // TODO Auto-generated method stub
    return str == null ? new String() : str;
  }


  public int addInstitute(String name, String postalCode, String city, String street,
      String shortName) {
    int instituteId = -1;
    if (name == null || name.isEmpty()) {
      return instituteId;
    }
    String sql =
        "INSERT INTO institute (name, postal_code, city, street, short_name) VALUES(?, ?, ?, ?, ?)";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, name);
      statement.setString(2, postalCode);
      statement.setString(3, city);
      statement.setString(4, street);
      statement.setString(5, shortName);
      // execute the statement, data IS NOT commit yet
      statement.execute();
      ResultSet rs = statement.getGeneratedKeys();
      if (rs.next()) {
        instituteId = rs.getInt(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return instituteId;
  }

  public void addWorkingGroup(String name) {
    String sql = "INSERT INTO workgroup (name) VALUES(?)";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, name);
      statement.execute();
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


  public int addKostenstelle(String name, String abbreviation) {
    int costLocId = -1;
    if (name == null || name.isEmpty()) {
      return costLocId;
    }
    String sql = "INSERT INTO cost_locations (name, abbreviation) VALUES(?, ?)";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, name);
      statement.setString(2, abbreviation);
      // execute the statement, data IS NOT commit yet
      statement.execute();
      ResultSet rs = statement.getGeneratedKeys();
      if (rs.next()) {
        costLocId = rs.getInt(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return costLocId;

  }


  public void addCategory(String name) {
    String sql = "INSERT INTO category (name) VALUES(?)";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, name);
      statement.execute();
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


  public int addUser(String name, String workinggroup, String institute, String email, String role,
      String phone) {
    int userId = -1;
    if (name == null || name.isEmpty()) {
      return userId;
    }
    String sql =
        "INSERT INTO users (name, workgroup, institute_id, email, role, phone) VALUES(?, ?, ?, ?, ?, ?)";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, name);
      statement.setString(2, workinggroup);
      statement.setInt(3, getInstituteIdByName(institute));
      statement.setString(4, email);
      statement.setString(5, role);
      statement.setString(6, phone);
      // execute the statement, data IS NOT commit yet
      statement.execute();
      ResultSet rs = statement.getGeneratedKeys();
      if (rs.next()) {
        userId = rs.getInt(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return userId;

  }

  public void addKostenStelleToUser(int userId, String kostenstelle) {
    int kostenstelleId = getKostenstelleIdByName(kostenstelle);
    if (kostenstelleId == -1) {
      throw new IllegalArgumentException("Kostenstelle: " + kostenstelle
          + " does not exist in the database (or database is not reachable).");
    }

    addKostenStelleToUser(userId, kostenstelleId);
  }

  public int getInstituteIdByName(String institute) {
    int kostenstelleId = -1;
    String sql = "SELECT institute_id FROM institute WHERE name = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, institute);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        kostenstelleId = rs.getInt("institute_id");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return kostenstelleId;
  }

  public int getKostenstelleIdByName(String kostenstelle) {
    int kostenstelleId = -1;
    String sql = "SELECT cost_location_id FROM cost_locations WHERE name = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, kostenstelle);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        kostenstelleId = rs.getInt("cost_location_id");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return kostenstelleId;
  }


  public boolean addKostenStelleToUser(int userId, int kostenstelleId) {
    boolean added = true;
    String sql =
        "INSERT INTO users_cost_locations_junction (user_id, cost_location_id) VALUES(?, ?)";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setInt(1, userId);
      statement.setInt(2, kostenstelleId);
      // execute the statement, data IS NOT commit yet
      statement.execute();
      ResultSet rs = statement.getGeneratedKeys();
      if (rs.next()) {
        userId = rs.getInt(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      added = false;
    }
    return added;
  }


  /**
   * adds a device/resource to the database and returns its id if operation succeded. returns -1
   * else. Note that if label or description are empty or null, nothing is written to the database
   * and -1 is returned.
   * 
   * @param label
   * @param description
   * @param restricted
   * @return
   */
  public int addDevice(String label, String description, String shortDescription, boolean restricted) {
    int deviceId = -1;
    if (label == null || label.isEmpty() || description == null || description.isEmpty()) {
      return deviceId;
    }
    String sql = "INSERT INTO resources (name, descr, short_desc, restricted) VALUES(?, ?, ?, ?)";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, label);
      statement.setString(2, description);
      statement.setString(3, shortDescription);
      statement.setBoolean(4, restricted);
      // execute the statement, data IS NOT commit yet
      statement.execute();
      ResultSet rs = statement.getGeneratedKeys();
      if (rs.next()) {
        deviceId = rs.getInt(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return deviceId;
  }

  /**
   * removes the given device bean if its id can be found in the database.
   * 
   * @param db
   * @return
   */
  public boolean removeDevice(DeviceBean db) {
    return removeDevice(db.getId());
  }

  /**
   * removes the given device bean if its id can be found in the database.
   * 
   * @param db
   * @return
   */
  public boolean removeDevice(int deviceId) {
    boolean success = false;
    if (deviceId < 0)
      return success;
    String sql = "DELETE FROM resources WHERE resource_id = ?";
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, deviceId);
      int result = statement.executeUpdate();
      success = (result > 0);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return success;
  }


  /**
   * returns a DeviceBean for the given Id. If it does not exits or an error occurs null is returned
   * 
   * @param deviceId
   * @return
   */
  public DeviceBean getDeviceById(int deviceId) {
    DeviceBean devbean = null;
    if (deviceId < 0)
      return devbean;

    String sql = "SELECT * FROM resources WHERE resource_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, deviceId);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        devbean =
            new DeviceBean(deviceId, rs.getString("name"), rs.getString("description"),
                rs.getBoolean("restricted"));
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return devbean;
  }

  public boolean updateDevice(DeviceBean bean) {
    boolean success = false;
    if (bean.getId() < 0 || bean.getName() == null || bean.getName().isEmpty()
        || bean.getDescription() == null || bean.getDescription().isEmpty())
      return success;
    String sql =
        "UPDATE resources SET name = ?, description = ?, restricted = ? WHERE resource_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, bean.getName());
      statement.setString(2, bean.getDescription());
      statement.setBoolean(3, bean.isRestricted());
      statement.setInt(4, bean.getId());
      int result = statement.executeUpdate();
      success = (result > 0);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return success;
  }


  // direct usage of connection to database. Should not be visible to the outside world
  /**
   * 
   * Undoes all changes made in the current transaction. Does not undo, if conn IS in auto commit
   * mode
   * 
   * @param conn
   * @param closeConnection
   */
  private void rollback(Connection conn, boolean closeConnection) {

    try {
      if (!conn.getAutoCommit()) {
        conn.rollback();
      }
      if (closeConnection) {
        logout(conn);
      }
    } catch (SQLException e) {
      if (conn != null && closeConnection) {
        logout(conn);
      }
      // TODO log everything
      e.printStackTrace();
    }
  }

  /**
   * logs into database with the parameters given in {@link Database.init}
   * 
   * @return Connection, otherwise null if connecting to the database fails
   */
  private Connection login() {
    try {
      return DriverManager.getConnection(host, user, password);

    } catch (SQLException e) {
      // TODO log login failure
      e.printStackTrace();
    }
    return null;
  }

  /**
   * trys to close the given connection and release it
   * 
   * From java documentation: It is strongly recommended that an application explicitly commits or
   * rolls back an active transaction prior to calling the close method. If the close method is
   * called and there is an active transaction, the results are implementation-defined.
   * 
   * 
   * @param conn
   */
  private void logout(Connection conn) {
    try {
      conn.close();
    } catch (SQLException e) {
      // TODO log logout failure
      e.printStackTrace();
    }
    conn = null;
  }

  /**
   * return all devices that are in the database.
   * 
   * @return
   */
  public java.util.List<DeviceBean> getDevices() {
    ArrayList<DeviceBean> list = new ArrayList<DeviceBean>();
    String sql = "SELECT * FROM resources";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); Statement statement = conn.createStatement()) {
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        list.add(new DeviceBean(rs.getInt("resource_id"), rs.getString("name"), rs
            .getString("description"), rs.getBoolean("restricted")));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return list;
  }

  // INSERT INTO role (name) VALUES ('unknown');
  public boolean addRole(String role) {
    boolean isSuccess = true;
    if (role == null) {
      return !isSuccess;
    }
    String sql = "INSERT INTO role (name) VALUES(?)";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, role);

      // execute the statement, data IS NOT commit yet
      statement.execute();
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      isSuccess = false;
    }
    return isSuccess;

  }

  /**
   * retuns the user id for a given user or -1 if user can not be found.
   * 
   * @param fullName
   * @return
   */
  public int findUserByFullName(String fullName) {
    // select user_id from users where name = '?';
    int userId = -1;
    if (fullName == null)
      return userId;

    String sql = "SELECT user_id FROM users WHERE name = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, fullName);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        userId = rs.getInt("user_id");
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return userId;
  }

  /**
   * try to find deviceUserId in table that connects possible userids with resources or -1 if user
   * can not be found
   * 
   * @param deviceUserId
   * @return
   */
  public int findUserByDeviceUserId(String deviceUserId) {
    // TODO Auto-generated method stub
    return -1;
  }

  /**
   * Try to match a device user id to a full name in the database. Returns the user ids of all
   * matched users or an empty set
   * 
   * @param deviceUserId
   * @return
   */
  public Set<Integer> matchDeviceUserIdToUserName(String deviceUserId) {
    // select * from users where name like '%name%';
    HashSet<Integer> userIds = new HashSet<Integer>();

    if (deviceUserId == null)
      return userIds;

    String sql = "SELECT user_id FROM users WHERE name LIKE ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      // % is an sql wild card used to match zero or more characters
      statement.setString(1, "%" + deviceUserId + "%");
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        userIds.add(rs.getInt("user_id"));
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return userIds;
  }

  /**
   * returns -1 if that specific timeblock can not be found or the first timeblock that can be found
   * with that settings.
   * 
   * @param deviceId
   * @param userName
   * @param userFullName
   * @param start
   * @param end
   * @return
   */
  public int isPhysicalTimeBlock(int deviceId, String userName, String userFullName, Date start,
      Date end) {
    int userId = -1;
    String startStatement = "";
    String endStatement = "";
    if (start == null) {
      startStatement = "start_time IS NULL";
    } else {
      startStatement = "start_time = ?";
    }
    if (end == null) {
      endStatement = "end_time IS NULL";
    } else {
      endStatement = "end_time = ?";
    }

    String sql =
        "SELECT id FROM physical_time_blocks WHERE resource_id = ? AND resource_user_name = ? AND resource_specific_id = ? AND "
            + startStatement + "  AND " + endStatement;
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, deviceId);
      statement.setString(2, userFullName);
      statement.setString(3, userName);
      if (start != null) {
        statement.setTimestamp(4, new Timestamp(start.getTime()));
      }
      if (end != null) {
        statement.setTimestamp(5, new Timestamp(end.getTime()));
      }

      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        userId = rs.getInt("id");
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return userId;
  }

  /**
   * Adds a physical time block into the database. BE AWARE: It is added without further checking.
   * If you want to be sure that this time block was not added check it with isPhysicalTimeBlock
   * 
   * @param deviceId
   * @param userName
   * @param userFullName
   * @param start
   * @param end
   * @return
   */
  public boolean addPhysicalTimeBlock(int deviceId, String userName, String userFullName,
      Date start, Date end) {
    // select user_id from users where name = '?';
    boolean id = false;
    if (deviceId == -1 || userName == null) {
      return id;
    }


    String sql =
        "INSERT INTO physical_time_blocks (resource_id, resource_user_name, resource_specific_id, start_time, end_time) VALUES(?, ?, ?, ?, ?)";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, deviceId);
      statement.setString(2, userFullName);
      statement.setString(3, userName);
      if (start == null) {
        statement.setNull(4, java.sql.Types.TIMESTAMP);
      } else {
        statement.setTimestamp(4, new Timestamp(start.getTime()));
      }
      if (end == null) {
        statement.setNull(5, java.sql.Types.TIMESTAMP);
      } else {
        statement.setTimestamp(5, new Timestamp(end.getTime()));
      }
      statement.executeUpdate();
      // if (rs.next()) {
      // id =rs.getInt("id");
      // }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

}
