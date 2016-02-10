/*******************************************************************************
 * QBiC Calendar provides an infrastructure for defining calendars for specific purposes like booking devices or
 * planning resources for services and integration of relevant data into the common portal infrastructure.
 * Copyright (C) 2016 Aydın Can Polatkan & David Wojnar
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
import java.util.List;
import java.util.Set;

import com.vaadin.ui.components.calendar.event.BasicEvent;
import com.vaadin.ui.components.calendar.event.CalendarEvent;

import facs.model.DeviceBean;
import facs.model.MachineOccupationBean;
import facs.model.UserBean;
import facs.model.BookingBean;

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
	      //System.out.println("Database: " + d.toString());
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

  	public void userLogin(String user_ldap) {
		String sql = "INSERT INTO user_login (user_ldap) VALUES(?)";
	    // The following statement is an try-with-resources statement, which declares two resources,
	    // conn and statement, which will be automatically closed when the try block terminates
	    try (Connection conn = login();
	        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	
	      statement.setString(1, user_ldap);
	      statement.execute();
	      // nothing will be in the database, until you commit it!
	      // conn.commit();
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
		
	}

	public String getUserAdminPanelAccessByLDAPId(String uuid) {
		  String userrole = "V";
		  
		  String sql = "SELECT admin_panel FROM user WHERE user_ldap=?";
		  try (Connection conn = login(); 
		    		PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			  
			  statement.setString(1, uuid);
		      ResultSet rs = statement.executeQuery();
		      while (rs.next()) {
			    	  userrole = (String)rs.getString(1);
			  }
			      // nothing will be in the database, until you commit it!
			      // conn.commit();
		  } 
		  catch (SQLException e) {
			      e.printStackTrace();
		  }
		  
		  return userrole;
	}

	public String getUserRoleByLDAPId(String uuid) {
		  String userrole = "V";
		  
		  String sql = "SELECT group_id FROM user WHERE user_ldap=?";
		  try (Connection conn = login(); 
		    		PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			  
			  statement.setString(1, uuid);
		      ResultSet rs = statement.executeQuery();
		      while (rs.next()) {
			    	  userrole = (String)rs.getString(1);
			  }
			      // nothing will be in the database, until you commit it!
			      // conn.commit();
		  } 
		  catch (SQLException e) {
			      e.printStackTrace();
		  }
		  
		  return userrole;
	}
	
	public String getUserRoleNameByLDAPId(String uuid) {
		  String userrole = "V";
		  
		  String sql = "SELECT group_name FROM user INNER JOIN groups ON user.group_id = groups.group_id WHERE user_ldap=?";
		  try (Connection conn = login(); 
		    		PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			  
			  statement.setString(1, uuid);
		      ResultSet rs = statement.executeQuery();
		      while (rs.next()) {
			    	  userrole = (String)rs.getString(1);
			  }
			      // nothing will be in the database, until you commit it!
			      // conn.commit();
		  } 
		  catch (SQLException e) {
			      e.printStackTrace();
		  }
		  
		  return userrole;
	}
	
	/**
  	 * Returns the cost of the selected device according to the user's group
  	 * 
  	 * @param calendar_name
  	 * @param service_name
  	 * @param group_id
  	 * @return
  	 */
	public int getDeviceCostPerGroup(String calendar_name, String service_name, String group_id) {
		  int cost=0;
		  String sql;
		  
		  if(service_name != null) {
		      sql = "SELECT cost FROM costs INNER JOIN calendars ON costs.calendar_id = calendars.calendar_id WHERE calendars.calendar_name=? AND calendars.description=? AND costs.group_id=?";
		  }
		  else {
			  sql = "SELECT cost FROM costs INNER JOIN calendars ON costs.calendar_id = calendars.calendar_id WHERE calendars.calendar_name=? AND calendars.description IS ? AND costs.group_id=?";
		  }
		  try (Connection conn = login(); 
		    		PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			  
			  statement.setString(1, calendar_name);
			  statement.setString(2, service_name);
		      statement.setString(3, group_id);
		      ResultSet rs = statement.executeQuery();
		      //System.out.println("getDeviceCostPerGroup: "+statement);
		      while (rs.next()) {
			    	  cost = (int)rs.getInt(1);
			  }
		  } 
		  catch (SQLException e) {
			      e.printStackTrace();
		  }
		  return cost;
	}
	
	/**
  	 * Returns the device ID by querying the device name
  	 * 
  	 * @param device_name
  	 * @return
  	 */
	public String getDeviceIDByName(String device_name) {
		  String userrole = "V";
		  
		  String sql = "SELECT device_id FROM devices WHERE device_name=?";
		  try (Connection conn = login(); 
		    		PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			  
			  statement.setString(1, device_name);
		      ResultSet rs = statement.executeQuery();
		      while (rs.next()) {
			    	  userrole = (String)rs.getString(1);
			  }
			      // nothing will be in the database, until you commit it!
			      // conn.commit();
		  } 
		  catch (SQLException e) {
			      e.printStackTrace();
		  }
		  
		  return userrole;
	}
	
	/**
  	 * Returns the user name by querying the user ID
  	 * 
  	 * @param uuid
  	 * @return
  	 */
	public String getUserNameByUserID(String uuid) {
		  String userrole = "V";
		  
		  String sql = "SELECT user_name FROM user WHERE user_ldap = ?";
		  try (Connection conn = login(); 
		    		PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			  
			  statement.setString(1, uuid);
		      ResultSet rs = statement.executeQuery();
		      while (rs.next()) {
			    	  userrole = (String)rs.getString(1);
			  }
			      // nothing will be in the database, until you commit it!
			      // conn.commit();
		  } 
		  catch (SQLException e) {
			      e.printStackTrace();
		  }
		  
		  return userrole;
	}
	
	
	/**
  	 * Returns the user ID from the user table by using users LDAP ID
  	 * 
  	 * @param uuid
  	 * @return
  	 */
	public String getUserIDbyLDAPID(String uuid) {
		  String userrole = "Basic · only view the calendar and/or request.";
		  
		  String sql = "SELECT user_id FROM user WHERE user_ldap=?";
		  try (Connection conn = login(); 
		    		PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			  
			  statement.setString(1, uuid);
		      ResultSet rs = statement.executeQuery();
		      while (rs.next()) {
			    	  userrole = (String)rs.getString(1);
			  }
			      // nothing will be in the database, until you commit it!
			      // conn.commit();
		  } 
		  catch (SQLException e) {
			      e.printStackTrace();
		  }

		  return userrole;
	}	
	
	/**
  	 * Returns the user group ID
  	 * 
  	 * @param userGroupName
  	 * @return
  	 */
	public String getUserGroupIDByName(String userGroupName) {
		  String userrole = "Basic · only view the calendar and/or request.";
		  
		  String sql = "SELECT group_id FROM groups WHERE group_name=?";
		  try (Connection conn = login(); 
		    		PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			  
			  statement.setString(1, userGroupName);
		      ResultSet rs = statement.executeQuery();
		      while (rs.next()) {
			    	  userrole = (String)rs.getString(1);
			  }
			      // nothing will be in the database, until you commit it!
			      // conn.commit();
		  } 
		  catch (SQLException e) {
			      e.printStackTrace();
		  }
		  
		  return userrole;
	}

	/**
  	 * Returns the specified user's Group Description by using user's LDAP ID
  	 * 
  	 * @param uuid
  	 * @param device_id
  	 * @return
  	 */
	public String getUserGroupDescriptionByLDAPId(String uuid, String device_id) {
		  String userrole = "V";
		  
		  String sql = "SELECT role_description FROM roles INNER JOIN user_roles ON roles.role_id = user_roles.role_id INNER JOIN user ON user_roles.user_id = user.user_id WHERE user_ldap=? AND device_id=?";
		  try (Connection conn = login(); 
		    		PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			  
			  statement.setString(1, uuid);
			  statement.setString(2, device_id);
		      ResultSet rs = statement.executeQuery();
		      while (rs.next()) {
			    	  userrole = (String)rs.getString(1);
			  }
			      // nothing will be in the database, until you commit it!
			      // conn.commit();
		  } 
		  catch (SQLException e) {
			      e.printStackTrace();
		  }
		  
		  return userrole;
	}
	
	/**
  	 * Returns the user role ID of the user for the specified device 
  	 * 
  	 * @param userRoleDesc
  	 * @return
  	 */
	public String getUserRoleIDbyDesc(String userRoleDesc) {
		  String userrole = "Basic · only view the calendar and/or request.";
		  
		  String sql = "SELECT role_id FROM roles WHERE role_description=?";
		  try (Connection conn = login(); 
		    		PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			  
			  statement.setString(1, userRoleDesc);
		      ResultSet rs = statement.executeQuery();
		      while (rs.next()) {
			    	  userrole = (String)rs.getString(1);
			  }
			      // nothing will be in the database, until you commit it!
			      // conn.commit();
		  } 
		  catch (SQLException e) {
			      e.printStackTrace();
		  }
		  
		  return userrole;
	  }
	
	/**
  	 * Returns the user role description of the user for the specified device 
  	 * the user role description defines the user role in detail
  	 * 
  	 * @param uuid
  	 * @param device_name
  	 * @return
  	 */
	public String getUserRoleDescByLDAPId(String uuid, String device_name) {
		  String userrole = "Basic · only view the calendar and/or request.";
		  
		  String sql = "SELECT role_description FROM user_roles INNER JOIN user ON user_roles.user_id = user.user_id INNER JOIN devices ON user_roles.device_id = devices.device_id INNER JOIN roles on user_roles.role_id = roles.role_id WHERE user_ldap=? AND device_name=?";
		  try (Connection conn = login(); 
		    		PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			  
			  statement.setString(1, uuid);
		      statement.setString(2, device_name);
		      ResultSet rs = statement.executeQuery();
		      while (rs.next()) {
			    	  userrole = (String)rs.getString(1);
			  }
			      // nothing will be in the database, until you commit it!
			      // conn.commit();
		  } 
		  catch (SQLException e) {
			      e.printStackTrace();
		  }
		  
		  return userrole;
	  }
	
	/**
  	 * Returns the user's role name for the specified device like 'Admin', 'A', 'B', 'C' or 'V'
  	 * 
  	 * @param uuid
  	 * @param device_name
  	 * @return
  	 */
	public String getUserRoleByLDAPId(String uuid, String device_name) {
	  String userrole = "V";
	  
	  String sql = "SELECT role_name FROM user_roles INNER JOIN user ON user_roles.user_id = user.user_id INNER JOIN devices ON user_roles.device_id = devices.device_id INNER JOIN roles on user_roles.role_id = roles.role_id WHERE user_ldap=? AND device_name=?";
	  try (Connection conn = login(); 
	    		PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
		  
		  statement.setString(1, uuid);
	      statement.setString(2, device_name);
	      ResultSet rs = statement.executeQuery();
	      while (rs.next()) {
		    	  userrole = (String)rs.getString(1);
		  }
		      // nothing will be in the database, until you commit it!
		      // conn.commit();
	      
		  if(userrole.isEmpty())
			  userrole = "V";
		  
	  } 
	 
	  catch (SQLException e) {
		      e.printStackTrace();
	  }
	  
	  return userrole;
	}
  
  	/**
  	 * Sets the user role for the selected device for the specified user. 
  	 * Returns a boolean value in case of success or fail.
  	 * 
  	 * @param user_role
  	 * @param uuid
  	 * @param device_name
  	 * @return
  	 */
	public boolean getShitDone(String user_role, String uuid, String device_name) {
	    boolean success = false;

	    String sql = "UPDATE user_roles SET role_id=?  WHERE user_id=? AND device_id=?";
	    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
	      statement.setString(1, user_role);
	      statement.setString(2, uuid);
	      statement.setString(3, device_name);
	      int result = statement.executeUpdate();
	      //System.out.println("getShitDone: "+sql);
	      success = (result > 0);
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	    return success;
	}
	
	/**
  	 * Sets the user's group for the specified user. 
  	 * Returns a boolean value in case of success or fail.
  	 * 
  	 * @param user_group
  	 * @param uuid
  	 * @return
  	 */
	public boolean getShitDoneAgain(String user_group, String uuid) {
	    boolean success = false;

	    String sql = "UPDATE user SET group_id=?  WHERE user_ldap=?";
	    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
	      statement.setString(1, user_group);
	      statement.setString(2, uuid);
	      int result = statement.executeUpdate();
	      //System.out.println("getShitDone: "+sql);
	      success = (result > 0);
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	    return success;
	}
 
	public int addBooking(String uuid, String device_name, Date start, Date end, String service, double cost) {
	  	int booking_id = -1;
	    if (uuid == null || uuid.isEmpty() || start == null || end == null) {
	      return booking_id;
	    }
	    //System.out.println("Database.java 131 util start: " + start);
	    java.sql.Timestamp sqlStart = new java.sql.Timestamp(start.getTime());
	    java.sql.Timestamp sqlEnd = new java.sql.Timestamp(end.getTime());
	    //System.out.println("Database.java 131 sql start: " + sqlStart);
	    //java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(timestamp.getTime());
	    String sql =
	        "INSERT INTO booking (user_ldap, device_name, start, end, service, price) VALUES(?, ?, ?, ?, ?, ?)";
	    // The following statement is an try-with-devices statement, which declares two devices,
	    // conn and statement, which will be automatically closed when the try block terminates
	    try (Connection conn = login();
	        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	      statement.setString(1, uuid);
	      statement.setString(2, device_name);
	      statement.setTimestamp(3, sqlStart);
	      statement.setTimestamp(4, sqlEnd);
	      statement.setString(5, service);
	      statement.setDouble(6, cost);
	      // execute the statement, data IS NOT commit yet
	      statement.execute();
	      ResultSet rs = statement.getGeneratedKeys();
	      if (rs.next()) {
	        booking_id = rs.getInt(1);
	      }
	      // nothing will be in the database, until you commit it!
	      // conn.commit();
	      
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	    return booking_id;
	}
  
	public int addBooking(String uuid, String device_name, Date start, Date end, String service, double cost, boolean confirmation) {
	  	int booking_id = -1;
	    if (uuid == null || uuid.isEmpty() || start == null || end == null) {
	      return booking_id;
	    }
	    //System.out.println("Database.java 131 util start: " + start);
	    java.sql.Timestamp sqlStart = new java.sql.Timestamp(start.getTime());
	    java.sql.Timestamp sqlEnd = new java.sql.Timestamp(end.getTime());
	    //System.out.println("Database.java 131 sql start: " + sqlStart);
	    //java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(timestamp.getTime());
	    String sql =
	        "INSERT INTO booking (user_ldap, device_name, start, end, service, price, confirmation) VALUES(?, ?, ?, ?, ?, ?, ?)";
	    // The following statement is an try-with-devices statement, which declares two devices,
	    // conn and statement, which will be automatically closed when the try block terminates
	    try (Connection conn = login();
	        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	      statement.setString(1, uuid);
	      statement.setString(2, device_name);
	      statement.setTimestamp(3, sqlStart);
	      statement.setTimestamp(4, sqlEnd);
	      statement.setString(5, service);
	      statement.setDouble(6, cost);
	      statement.setBoolean(7, confirmation);
	      // execute the statement, data IS NOT commit yet
	      statement.execute();
	      ResultSet rs = statement.getGeneratedKeys();
	      if (rs.next()) {
	        booking_id = rs.getInt(1);
	      }
	      // nothing will be in the database, until you commit it!
	      // conn.commit();
	      
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	    return booking_id;
	}

  	public ArrayList<CalendarEvent> getAllBookings(String uuid, String device_name) {
	   ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
	    String sql = "SELECT * FROM booking INNER JOIN user ON booking.user_ldap = user.user_ldap WHERE deleted IS NULL AND device_name = ?";
	    //device_id = device_id+1;

	    try (Connection conn = login(); 
	    		PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	    	  statement.setString(1, device_name);
		      ResultSet rs = statement.executeQuery();
		      
		      while (rs.next()) {
		    	  
		    	String service = rs.getString("service")+" · ";
		    	if (rs.getString("service") == null)
		    		service = "";
		    	
			  	if (uuid.equals(rs.getString("user_ldap")) && rs.getString("confirmation") == null) {
			  		BasicEvent canbedeleted = new BasicEvent(rs.getString("user.user_name"), service + "K: " + rs.getString("kostenstelle") + " · " + "Approx. Cost: €" + rs.getString("price") + "-", rs.getTimestamp("start"), rs.getTimestamp("end"));
			    	canbedeleted.setStyleName("color5");
			    	events.add(canbedeleted); 
				}
			  	else if (uuid.equals(rs.getString("user_ldap")) && rs.getString("confirmation") != null) {
			  		BasicEvent cannotbedeleted = new BasicEvent(rs.getString("user.user_name"), service + "K: " + rs.getString("kostenstelle") + " · " + "Approx. Cost: €" + rs.getString("price") + "-", rs.getTimestamp("start"), rs.getTimestamp("end"));
			    	cannotbedeleted.setStyleName("color1");
			    	events.add(cannotbedeleted); 
			 	}
			  	else {
			  		BasicEvent cannotbedeleted = new BasicEvent(rs.getString("user.user_name"),"Contact: " + rs.getString("email") + " · Tel: "+rs.getString("phone"), rs.getTimestamp("start"), rs.getTimestamp("end"));
			    	cannotbedeleted.setStyleName("color3");
			    	events.add(cannotbedeleted); 
			 	}

			}
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	    return events;    
	}
  
  	public ArrayList<CalendarEvent> getAllBookings(String device_name) {
	   ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
	    String sql = "SELECT * FROM booking INNER JOIN user ON booking.user_ldap = user.user_ldap WHERE device_name = ?";

	    try (Connection conn = login(); 
	    		PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	    	statement.setString(1,device_name);
	      ResultSet rs = statement.executeQuery();
	      while (rs.next()) {  		
	    		BasicEvent canbedeleted = new BasicEvent(rs.getString("user.user_name"),"Contact: " + rs.getString("email"), rs.getTimestamp("start"), rs.getTimestamp("end"));
	    		canbedeleted.setStyleName("color5");
	    		events.add(canbedeleted); 
	      }
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	    return events;    
	}
  
  	public java.util.List<BookingBean> getAllBookings() {
	   ArrayList<BookingBean> bookings = new ArrayList<BookingBean>();
	    String sql = "SELECT * FROM booking INNER JOIN user ON booking.user_ldap = user.user_ldap";

	    try (Connection conn = login(); Statement statement = conn.createStatement()) {
		      ResultSet rs = statement.executeQuery(sql);
		      while (rs.next()) {
		    	  bookings.add(new BookingBean(rs.getInt("booking_id"), rs.getString("user_name"), rs.getString("phone"), rs.getString("device_name"), rs.getTimestamp("start"), 
		    			  rs.getTimestamp("end"), rs.getString("service"), rs.getDouble("price"), rs.getBoolean("confirmation")));
		      }
		    } catch (SQLException e) {
		      e.printStackTrace();
		    }
	    return bookings;
	}
  
	public java.util.List<BookingBean> getAllBookingsPerDevice(String device_name) {
	   ArrayList<BookingBean> bookings = new ArrayList<BookingBean>();
	    String sql = "SELECT * FROM booking INNER JOIN user ON booking.user_ldap = user.user_ldap WHERE deleted IS NULL AND confirmation IS NULL AND device_name = ?";

	    try (Connection conn = login(); 
	    		PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
		  
	    	  statement.setString(1,device_name);

		      ResultSet rs = statement.executeQuery();
		      while (rs.next()) {
		    	  bookings.add(new BookingBean(rs.getInt("booking_id"), rs.getString("user_name"), rs.getString("phone"), rs.getString("device_name"), rs.getTimestamp("start"), 
		    			  rs.getTimestamp("end"), rs.getString("service"), rs.getDouble("price"), rs.getBoolean("confirmation")));
		      }
		    } catch (SQLException e) {
		      e.printStackTrace();
		    }
	    return bookings;
	}
  
  	public java.util.List<UserBean> getUsers() {
	   ArrayList<UserBean> users = new ArrayList<UserBean>();
	    String sql = "SELECT * FROM user INNER JOIN groups ON user.group_id=groups.group_id INNER JOIN workgroups ON user.workgroup_id=workgroups.workgroup_id;";

	    try (Connection conn = login(); 
	    		PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

		      ResultSet rs = statement.executeQuery();
		      while (rs.next()) {
		    	  users.add(new UserBean(rs.getString("user_ldap"), rs.getInt("user_id"), rs.getString("user_name"), rs.getString("group_name"), rs.getString("workgroup_name"), rs.getString("institute_name") ,rs.getString("kostenstelle"),rs.getString("project"), rs.getString("email"), rs.getString("phone")));
		      
		      }
		    conn.close();
		    } catch (SQLException e) {
		      e.printStackTrace();
		    }
	    return users;
	}
  
  	public java.util.List<BookingBean> getDeletedBookings() {
	   ArrayList<BookingBean> bookings = new ArrayList<BookingBean>();
	    String sql = "SELECT * FROM booking INNER JOIN user ON booking.user_ldap = user.user_ldap WHERE deleted IS NOT NULL";

	    try (Connection conn = login(); 
	    		PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

		      ResultSet rs = statement.executeQuery();
		      while (rs.next()) {
		    	  bookings.add(new BookingBean(rs.getInt("booking_id"), rs.getString("user_name"), rs.getString("phone"), rs.getString("device_name"), rs.getTimestamp("start"), 
		    			  rs.getTimestamp("end"), rs.getString("service"), rs.getDouble("price"), rs.getBoolean("confirmation")));
		      }
		    conn.close();
		    } catch (SQLException e) {
		      e.printStackTrace();
		    }
	    return bookings;
	}
  
  	public java.util.List<BookingBean> getAwaitingRequests() {
	   ArrayList<BookingBean> bookings = new ArrayList<BookingBean>();
	    String sql = "SELECT * FROM booking INNER JOIN user ON booking.user_ldap = user.user_ldap WHERE confirmation IS NOT NULL AND deleted IS NULL";

	    try (Connection conn = login(); 
	    		PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

		      ResultSet rs = statement.executeQuery();
		      while (rs.next()) {
		    	  bookings.add(new BookingBean(rs.getInt("booking_id"), rs.getString("user_name"), rs.getString("phone"), rs.getString("device_name"), rs.getTimestamp("start"), 
		    			  rs.getTimestamp("end"), rs.getString("service"), rs.getDouble("price"), rs.getBoolean("confirmation")));
		      }
		    conn.close();
		    } catch (SQLException e) {
		      e.printStackTrace();
		    }
	    return bookings;
	}
  
  	public ArrayList<CalendarEvent> getMyBookings(String uuid) {
	  	ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
	    //System.out.println("Database.java 165 getAllBookings: ");
	    String sql = "SELECT * FROM booking WHERE user_ldap = ?";
	    String sql2 = "SELECT * FROM booking WHERE user_ldap != ?";

	    try (Connection conn = login(); 
	    		PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	    	statement.setString(1, uuid);
	      ResultSet rs = statement.executeQuery();
	      while (rs.next()) {  		
	    		BasicEvent canbedeleted = new BasicEvent(uuid, "This time frame is already occupied.", rs.getTimestamp("start"), rs.getTimestamp("end"));
	    		canbedeleted.setStyleName("color4");
	    		events.add(canbedeleted); 
		    //System.out.println("uuid: " + uuid + " user_ldap: " +rs.getString("user_ldap"));
  		//System.out.println("getAllBookings:" + rs.getTime("start") + " " + rs.getTime("end"));
	      }
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	    
	    try (Connection conn = login(); 
	    		PreparedStatement statement = conn.prepareStatement(sql2, Statement.RETURN_GENERATED_KEYS)) {
	    	
	      statement.setString(1, uuid);
	      ResultSet rs = statement.executeQuery();
	      while (rs.next()) {
	    		BasicEvent reserved = new BasicEvent(uuid, "This time frame is already occupied.", rs.getTimestamp("start"), rs.getTimestamp("end"));
	    		reserved.setStyleName("color5");
	    		events.add(4, reserved);
	      }
	      
	      conn.close();
	      
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	    return events;    
	}
  
	public java.util.List<BookingBean> getMyBookingsGrid(String uuid) {
	   ArrayList<BookingBean> bookings = new ArrayList<BookingBean>();
	    String sql = "SELECT * FROM booking INNER JOIN user ON booking.user_ldap = user.user_ldap WHERE deleted IS NULL AND booking.user_ldap = ?";

	    try (Connection conn = login(); 
	    		PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	    	  statement.setString(1,uuid);
		      ResultSet rs = statement.executeQuery();
		      while (rs.next()) {
		    	  bookings.add(new BookingBean(rs.getInt("booking_id"), rs.getString("user_name"), rs.getString("phone"), rs.getString("device_name"), rs.getTimestamp("start"), 
		    			  rs.getTimestamp("end"), rs.getString("service"), rs.getDouble("price"), rs.getBoolean("confirmation")));
		      }
		    conn.close();
		    } catch (SQLException e) {
		      e.printStackTrace();
		    }
	    return bookings;
	}

	public ArrayList<CalendarEvent> getAllMyBookings(String uuid, String device_name) {
	   ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
	    //System.out.println("Database.java 192 getAllBookings: ");
	    String sql = "SELECT * FROM booking WHERE user_ldap = ? AND device_name = ?";
	    //device_id = device_id+1;

	    try (Connection conn = login(); 
	    		PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	    	statement.setString(1,uuid);
	    	statement.setString(2,device_name);
	      ResultSet rs = statement.executeQuery();
	      while (rs.next()) {  		
	    		BasicEvent canbedeleted = new BasicEvent(uuid, "This time frame is already occupied.", rs.getTimestamp("start"), rs.getTimestamp("end"));
	    		canbedeleted.setStyleName("color5");
	    		events.add(canbedeleted); 
		    //System.out.println("uuid: " + uuid + " user_ldap: " +rs.getString("user_ldap"));
	    	//System.out.println("getAllBookings:" + rs.getTime("start") + " " + rs.getTime("end"));
	      }
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	    return events;    
	}
  
	public ArrayList<CalendarEvent> getOtherBookings(String uuid) {
	  	ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
	  	
	  	String sql = "SELECT * FROM booking WHERE user_ldap != ?";

	    try (Connection conn = login(); 
	    		PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	    	
	      statement.setString(1, uuid);
	      ResultSet rs = statement.executeQuery();
	      while (rs.next()) {
	    		BasicEvent reserved = new BasicEvent(uuid, "This time frame is already occupied.", rs.getTimestamp("start"), rs.getTimestamp("end"));
	    		reserved.setStyleName("color5");
	    		events.add(reserved);
	      }
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	    return events;
	}

	public int addInstitute(String name, String institute, String street, String postalCode, String city, String country) {
	    int instituteId = -1;
	    if (name == null || name.isEmpty()) {
	      return instituteId;
	    }
	    String sql =
	        "INSERT INTO workgroups (workgroup_name, instite_name, street, postcode, city, country) VALUES(?, ?, ?, ?, ?, ?)";
	    // The following statement is an try-with-devices statement, which declares two devices,
	    // conn and statement, which will be automatically closed when the try block terminates
	    try (Connection conn = login();
	        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	
	      statement.setString(1, name);
	      statement.setString(2, institute);
	      statement.setString(3, street);
	      statement.setString(4, postalCode);
	      statement.setString(5, city);
	      statement.setString(6, country);
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
	    String sql = "INSERT INTO workgroups (workgroup_name) VALUES(?)";
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
	    String sql = "INSERT INTO kostenstelle (kostenstelle_code, kostenstelle_name) VALUES(?, ?)";
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

	public String getKostenstelleByUserId(int userId){
	    String sql = "SELECT kostenstelle.kostenstelle_code FROM kostenstelle INNER JOIN user ON user.kostenstelle=kostenstelle.kostenstelle_code WHERE user.user_id = ?";
	    String kostenstelle = "";
	    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
	      statement.setInt(1, userId);
	      ResultSet rs = statement.executeQuery();
	      if (rs.next()) {
	        kostenstelle = rs.getString("kostenstelle_code");
	      }
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	    
	    return kostenstelle;
	}

	public void addCategory(String name) {
	    String sql = "INSERT INTO kostenstelle (kostenstelle_code) VALUES(?)";
	    // The following statement is an try-with-resources statement, which declares two resources,
	    // conn and statement, which will be automatically closed when the try block terminates
	    try (Connection conn = login();
	        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	    	System.out.print("Triggered: " + sql);
	      statement.setString(1, name);
	      statement.execute();
	      // nothing will be in the database, until you commit it!
	      conn.commit();
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	}

	public int addUser(String name, String workinggroup, String institute, String email, String role, String phone) {
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
  
	public boolean getDeviceRestriction(String device_name, String selected_service) {
	    boolean restriction = false;
	    String sql = "SELECT restriction FROM calendars WHERE device_name = ? AND description = ?";
	    // The following statement is an try-with-resources statement, which declares two resources,
	    // conn and statement, which will be automatically closed when the try block terminates
	    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
	      statement.setString(1, device_name);
	      statement.setString(2, selected_service);
	      ResultSet rs = statement.executeQuery();
	      if (rs.next()) {
	        restriction = rs.getBoolean("restriction");
	      }
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	    return restriction;
	}
  
	public boolean getDeviceRestriction(String device_name) {
	    boolean restriction = false;
	    String sql = "SELECT restriction FROM devices WHERE device_name = ?";
	    // The following statement is an try-with-resources statement, which declares two resources,
	    // conn and statement, which will be automatically closed when the try block terminates
	    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
	      statement.setString(1, device_name);
	      ResultSet rs = statement.executeQuery();
	      if (rs.next()) {
	        restriction = rs.getBoolean("restriction");
	      }
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	    return restriction;
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

	public int getKostenstelleIdByName(String kostenstelle_code) {
	    int kostenstelleId = -1;
	    String sql = "SELECT kostenstelle_id FROM kostenstelle WHERE name = ?";
	    // The following statement is an try-with-resources statement, which declares two resources,
	    // conn and statement, which will be automatically closed when the try block terminates
	    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
	      statement.setString(1, kostenstelle_code);
	      ResultSet rs = statement.executeQuery();
	      if (rs.next()) {
	        kostenstelleId = rs.getInt("kostenstelle_id");
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
	 * adds a device/resource to the database and returns its id if operation succeeded. returns -1
	 * else. Note that if label or description are empty or null, nothing is written to the database
	 * and -1 is returned.
	 * 
	 * @param label
	 * @param description
	 * @param restricted
	 * @return
	 */
	public int addDevice(String label, String description, boolean restricted) {
	    int deviceId = -1;
	    if (label == null || label.isEmpty() || description == null || description.isEmpty()) {
	      return deviceId;
	    }
	    String sql = "INSERT INTO devices (device_name, description, restriction) VALUES (?, ?, ?)";
	    // The following statement is an try-with-resources statement, which declares two resources,
	    // conn and statement, which will be automatically closed when the try block terminates
	    try (Connection conn = login();
	        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	
	      statement.setString(1, label);
	      statement.setString(2, description);
	      statement.setBoolean(3, restricted);
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
  
	public boolean removeBooking(BookingBean db) {
		return removeBooking(db.getID());
	}
  
	public boolean purgeBooking(BookingBean db) {
	    return purgeBooking(db.getID());
	}
  
	public boolean restoreBooking(BookingBean db) {
	    return restoreBooking(db.getID());
	}
  
	public boolean denyBooking(BookingBean db) {
	    return denyBooking(db.getID());
	}

	public boolean confirmBooking(BookingBean db) {
	    return confirmBooking(db.getID());
	}
  
	public boolean confirmed(BookingBean db) {
		return confirmedBooking(db.getID());
	}


	/**
	 * removes the given booking bean if its id can be found in the database.
	 * 
	 * @param db
	 * @return
	 */
	public boolean removeBooking(int booking_id) {
	    boolean success = false;
	    if (booking_id < 0)
	      return success;
	    String sql = "UPDATE booking SET deleted = 1 WHERE booking_id = ?";
	    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
	      statement.setInt(1, booking_id);
	      int result = statement.executeUpdate();
	      success = (result > 0);
	      //System.out.println("Database 728: "+ success);
	    } catch (SQLException e) {
	    	//System.out.println("Database 730: "+ success);
	      e.printStackTrace();
	    }
	    //System.out.println("Database 733: "+ success);
	    return success;
	}
  
	public boolean removeBooking(Date start, String device_name) {
	    boolean success = false;

	    java.sql.Timestamp sqlStart = new java.sql.Timestamp(start.getTime());

	    String sql = "UPDATE booking SET deleted = 1 WHERE start = ? AND device_name = ?";
	    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
	      statement.setTimestamp(1, sqlStart);
	      statement.setString(2, device_name);
	      int result = statement.executeUpdate();
	      success = (result > 0);
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	    return success;
	}
  
	public boolean restoreBooking(int booking_id) {
	    boolean success = false;
	    if (booking_id < 0)
	      return success;
	    String sql = "UPDATE booking SET deleted = NULL WHERE booking_id = ?";
	    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
	      statement.setInt(1, booking_id);
	      int result = statement.executeUpdate();
	      success = (result > 0);
	      //System.out.println("Database 746: "+ success);
	    } catch (SQLException e) {
	    	//System.out.println("Database 748: "+ success);
	      e.printStackTrace();
	    }
	    //System.out.println("Database 751: "+ success);
	    return success;
	}
  
	public boolean purgeBooking(int booking_id) {
		    boolean success = false;
	    if (booking_id < 0)
	      return success;
	    String sql = "DELETE from booking WHERE booking_id = ?";
	    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
	      statement.setInt(1, booking_id);
	      int result = statement.executeUpdate();
	      success = (result > 0);
	      //System.out.println("Database 764: "+ success);
	    } catch (SQLException e) {
	    	//System.out.println("Database 766: "+ success);
	      e.printStackTrace();
	    }
	    //System.out.println("Database 769: "+ success);
	    return success;
	}
  
	public boolean denyBooking(int booking_id) {
	    boolean success = false;
	    if (booking_id < 0)
	      return success;
	    String sql = "UPDATE booking SET deleted = 1 WHERE booking_id = ?";
	    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
	      statement.setInt(1, booking_id);
	      int result = statement.executeUpdate();
	      success = (result > 0);
	      //System.out.println("Database 782: "+ success);
	    } catch (SQLException e) {
	    	//System.out.println("Database 784: "+ success);
	      e.printStackTrace();
	    }
	    //System.out.println("Database 787: "+ success);
	    return success;
	    
	}
  
	public boolean confirmBooking(int booking_id) {
	    boolean success = false;
	    if (booking_id < 0)
	      return success;
	    String sql = "UPDATE booking SET confirmation = NULL WHERE booking_id = ?";
	    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
	      statement.setInt(1, booking_id);
	      int result = statement.executeUpdate();
	      success = (result > 0);
	      //System.out.println(result + " Database 801: "+ success);
	    } catch (SQLException e) {
	    	//System.out.println("Database 803: "+ success);
	      e.printStackTrace();
	    }
	    //System.out.println(" Database 806: "+ success);
	    return success;
	}
  
	public boolean confirmedBooking(int booking_id) {
	    boolean success = false;
	    String sql = "SELECT confirmation FROM booking WHERE booking_id = ?";
	    
	    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
		      statement.setInt(1, booking_id);
		      ResultSet rs = statement.executeQuery();
		      if (rs.next()) {
		        success = rs.getBoolean("confirmation");
		      }
		      //System.out.println("id: "+booking_id+" bool:"+success);
		    } catch (SQLException e) {
		      e.printStackTrace();
		    }
		return success;
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
	    String sql = "DELETE FROM devices WHERE device_id = ?";
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

	    String sql = "SELECT * FROM devices WHERE device_id = ?";
	    // The following statement is an try-with-devices statement, which declares two devices,
	    // conn and statement, which will be automatically closed when the try block terminates
	    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
	      statement.setInt(1, deviceId);
	      ResultSet rs = statement.executeQuery();
	      if (rs.next()) {
	        devbean =
	            new DeviceBean(deviceId, rs.getString("device_name"), rs.getString("description"), rs.getBoolean("restriction"));
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
	        "UPDATE devices SET device_name = ?, description = ?, restriction = ? WHERE device_id = ?";
	    // The following statement is an try-with-devices statement, which declares two devices,
	    // conn and statement, which will be automatically closed when the try block terminates
	    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
	      statement.setString(1, bean.getName());
	      statement.setString(2, bean.getDescription());
	      statement.setBoolean(3, bean.getRestriction());
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
	@SuppressWarnings("unused")
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
	    String sql = "SELECT * FROM devices";
	    // The following statement is an try-with-devices statement, which declares two devices,
	    // conn and statement, which will be automatically closed when the try block terminates
	    try (Connection conn = login(); Statement statement = conn.createStatement()) {
	      ResultSet rs = statement.executeQuery(sql);
	      while (rs.next()) {
	        list.add(new DeviceBean(rs.getInt("device_id"), rs.getString("device_name"), rs
	            .getString("description"), rs.getBoolean("restriction")));
	      }
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	    return list;
	}
  
	/**
	 * return all device names to display inside the drop-down menu
	 * 
	 * @return
	 */  
	public ArrayList<String> getDeviceNames() {
	    ArrayList<String> list = new ArrayList<String>();
	    String sql = "SELECT device_name FROM devices";
	    // The following statement is an try-with-devices statement, which declares two devices,
	    // conn and statement, which will be automatically closed when the try block terminates
	    try (Connection conn = login(); Statement statement = conn.createStatement()) {
	      ResultSet rs = statement.executeQuery(sql);
	      while (rs.next()) {
	        list.add(rs.getString("device_name"));
	      }
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	    return list;
	}
  
	/**
	 * return all role names to display inside the drop-down menu
  	 * 
  	 * @return
  	 */  
	public ArrayList<String> getUserRoles() {
	    ArrayList<String> list = new ArrayList<String>();
	    String sql = "SELECT role_description FROM roles";
	    // The following statement is an try-with-devices statement, which declares two devices,
	    // conn and statement, which will be automatically closed when the try block terminates
	    try (Connection conn = login(); Statement statement = conn.createStatement()) {
	      ResultSet rs = statement.executeQuery(sql);
	      while (rs.next()) {
	        list.add(rs.getString("role_description"));
	      }
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	    return list;
	}
  
	/**
	 * return all group names to display inside the drop-down menu
	 * 
	 * @return
	 */  
	public ArrayList<String> getUserGroups() {
	    ArrayList<String> list = new ArrayList<String>();
	    String sql = "SELECT group_name FROM groups";
	    // The following statement is an try-with-devices statement, which declares two devices,
	    // conn and statement, which will be automatically closed when the try block terminates
	    try (Connection conn = login(); Statement statement = conn.createStatement()) {
	      ResultSet rs = statement.executeQuery(sql);
	      while (rs.next()) {
	        list.add(rs.getString("group_name"));
	      }
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	    return list;
	}
  
	/**
	 * return all device names to display inside the drop-down menu
	 * 
	 * @return
	 */  
	public ArrayList<String> getKostenstelleCodes() {
	    ArrayList<String> list = new ArrayList<String>();
	    String sql = "SELECT kostenstelle_code FROM kostenstelle";
	    // The following statement is an try-with-devices statement, which declares two devices,
	    // conn and statement, which will be automatically closed when the try block terminates
	    try (Connection conn = login(); Statement statement = conn.createStatement()) {
	      ResultSet rs = statement.executeQuery(sql);
	      while (rs.next()) {
	        list.add(rs.getString("kostenstelle_code"));
	      }
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	    return list;
	}
  
	public ArrayList<String> getProjects() {
	    ArrayList<String> list = new ArrayList<String>();
	    String sql = "SELECT DISTINCT project FROM user WHERE project IS NOT NULL";
	    // The following statement is an try-with-devices statement, which declares two devices,
	    // conn and statement, which will be automatically closed when the try block terminates
	    try (Connection conn = login(); Statement statement = conn.createStatement()) {
	      ResultSet rs = statement.executeQuery(sql);
	      while (rs.next()) {
	        list.add(rs.getString("project"));
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
	    // The following statement is an try-with-devices statement, which declares two devices,
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
	
	    String sql = "SELECT user_id FROM user WHERE user_name = ?";
	    // The following statement is an try-with-devices statement, which declares two devices,
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
	 * try to find device_id in table that connects possible user_ids with resources or -1 if user
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
	
	    String sql = "SELECT user_id FROM user WHERE user_name LIKE ?";
	    // The following statement is an try-with-devices statement, which declares two devices,
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
	public int isPhysicalTimeBlock(int deviceId, String userName, String userFullName, Date start, Date end) {
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
	    // The following statement is an try-with-devices statement, which declares two devices,
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
	public boolean addPhysicalTimeBlock(int deviceId, String userName, String userFullName, Date start, Date end) {
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
  
	public boolean addUserGroup(String name) {
	    String sql = "INSERT INTO usergroups (name) VALUES(?)";
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
	      return false;
	    }   
	    return true;
	}

	public boolean addResourceCostPerGroup(int resourceId, String usergroup, float cost) {
		// INSERT INTO group_resource_cost (usergroup,resource_id, cost) VALUES ('test2',1,25.24);
	    String sql = "INSERT INTO group_resource_cost (usergroup,resource_id, cost) VALUES (?,?,?)";
	    // The following statement is an try-with-resources statement, which declares two resources,
	    // conn and statement, which will be automatically closed when the try block terminates
	    try (Connection conn = login();
	        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	
	      statement.setString(1, usergroup);
	      statement.setInt(2,resourceId);
	      statement.setFloat(3, cost);
	      statement.execute();
	      // nothing will be in the database, until you commit it!
	      // conn.commit();
	    } catch (SQLException e) {
	      e.printStackTrace();
	      return false;
	    }   
	  return true;   
	    
	}

	public List<MachineOccupationBean> getPhysicalTimeBlocks(){
	    String sql = "SELECT * FROM logs";
	    List<MachineOccupationBean> obean = new ArrayList<MachineOccupationBean>();
	    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
	
	      ResultSet rs = statement.executeQuery();
	      while(rs.next()) {
	        MachineOccupationBean m = new MachineOccupationBean();
	        m.setDeviceId(rs.getInt("device_id"));
	        m.setUserFullName(rs.getString("user_full_name"));
	        m.setUserName(rs.getString("user_name"));
	        m.setStart(rs.getTimestamp("start"));
	        m.setEnd(rs.getTimestamp("end"));
	        obean.add(m);
	      }
	      statement.close();
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }
	    return obean;
	}



	public UserBean getUserByLDAPId(String userId) {
	    String sql = "SELECT user.user_id, user.user_ldap, user.user_name, user.group_id, workgroups.workgroup_name, workgroups.institute_name, user.kostenstelle, user.project FROM user INNER JOIN workgroups ON user.workgroup_id=workgroups.workgroup_id WHERE user.user_ldap = ?";
	    //1 2 3 4
	    UserBean ret = new UserBean();
	    try (Connection conn = login();
	        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	      
	      statement.setString(1, userId);
	      ResultSet rs = statement.executeQuery();
	      if(rs.next()){
	        ret.setId(rs.getInt(1));
	        ret.setLDAP(rs.getString(2));
	        ret.setName(rs.getString(3));
	        ret.setGroupID(rs.getString(4));
	        ret.setWorkgroup(rs.getString(5));
	        ret.setInstitute(rs.getString(6));
	        ret.setKostenstelle(rs.getString(7));
	        ret.setProject(rs.getString(8));
	        //TODO get the correct ones
	        List<String> kostenStelle = new ArrayList<String>();
	        String k = getKostenstelleByUserId(ret.getId());        
	        kostenStelle.add(k.isEmpty()?"unknown":k);
	        //ret.setKostenstelle(kostenStelle);
	      }
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }   
	    return ret;
	}

	public UserBean getUserById(int userId) {
	    String sql = "SELECT user.user_id, user.user_name, workgroups.workgroup_name, workgroups.institute_name FROM user INNER JOIN workgroups on user.workgroup_id = workgroups.workgroup_id WHERE user.user_id = ?";
	    //1 2 3 4
	    UserBean ret = new UserBean();
	    try (Connection conn = login();
	        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	      
	      statement.setInt(1, userId);
	      ResultSet rs = statement.executeQuery();
	      if(rs.next()){
	        ret.setId(rs.getInt(1));
	        ret.setName(rs.getString(2));
	        ret.setWorkgroup(rs.getString(3));
	        ret.setInstitute(rs.getString(4));
	        //TODO get the correct ones
	        List<String> kostenStelle = new ArrayList<String>();
	        String k = getKostenstelleByUserId(ret.getId());        
	        kostenStelle.add(k.isEmpty()?"unknown":k);
	        //ret.setKostenstelle(kostenStelle);
	      }
	    } catch (SQLException e) {
	      e.printStackTrace();
	    }   
	    return ret;
	}

	/**
	 * Trys to get cost (per hour?) of a resource for a given user. User have to have a usergroup which has a cost value for that resource.
	 * Returns -1 if it can not find a value in the database.
	 * @param uuid
	 * @param calendar_id
	 * @return
	 */
	public float getCostByResourceAndUserIds(int uuid, int calendar_id) {
	  // select group_resource_cost.cost from group_resource_cost INNER JOIN user_usergroup ON group_resource_cost.usergroup=user_usergroup.usergroup WHERE user_usergroup.user_id=7 AND group_resource_cost.resource_id=3;
	  String sql = "SELECT cost from costs INNER JOIN user ON costs.group_id = user.group_id WHERE user.user_id=? AND costs.calendar_id=?";
	  float cost = -1f; 
	  try (Connection conn = login();
	      PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	   
	    statement.setInt(1, uuid);
	    statement.setInt(2, calendar_id);
	    ResultSet rs = statement.executeQuery();
	    if(rs.next()){     
	      cost = rs.getFloat("cost");
	    }
	  } catch (SQLException e) {
	    e.printStackTrace();
	  }   
	  return cost;
	}

}
