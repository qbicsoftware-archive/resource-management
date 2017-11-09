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
package facs.test;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestDataBaseConnection {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {}

  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  private String hostname = "localhost";
  private String port = "8889";
  private String database = "facs_facility";
  private String username = "facs";
  private String password = "facs";

  @Test
  public void test() {
    java.sql.Connection conn = null;
    String jdbcUrl = "jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database;
    try {
      conn = DriverManager.getConnection(jdbcUrl, username, password);
      try {
        conn.setAutoCommit(false); // transaction block start
        PreparedStatement statement =
            conn.prepareStatement("INSERT INTO test (device_id) VALUES ('2');");// ("UPDATE people SET lastName = ?, age = ? WHERE id = ?");
        statement.setString(1, "Operator");
        // execute the statement, data IS NOT commit yet
        statement.execute();

        // here could be more statements

        // nothing will be in the database, until you commit it!
        conn.commit();
        System.out.println("committed");
        // transaction block end


        System.out.println("connection successful");
        // don't forget to close the connection.
        conn.close();
      } catch (SQLException e) {
        // if committing fails, roll back to have the previous state and unlock locks.
        conn.rollback();
      }

    } catch (SQLException e) {
      System.out.println("connection failed");
      e.printStackTrace();
    }
  }
}
