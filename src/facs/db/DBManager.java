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
package facs.db;

import java.sql.SQLException;

import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;


public class DBManager {

  // local testing
  private static String hostname = "localhost";
  private static String port = "8889";
  private static String sql_database = "facs_facility";
  private static String username = "facs";
  private static String password = "facs";

  // portal testing
  // private static String hostname = "portal-testing.am10.uni-tuebingen.de";
  // private static String port = "3306";
  // private static String sql_database = "facs_facility";
  // private static String username = "mariadbuser";
  // private static String password = "dZAmDa9-Ysq_Zv1AGygQ";

  // portal production
  // private static String hostname = "localhost";
  // private static String port = "3306";
  // private static String sql_database = "facs_facility";
  // private static String username = "iigpo01";
  // private static String password = "Eehae2ui6goaphae0ugiegh8";


  public static Database getDatabaseInstance() {
    String jdbcUrl = "jdbc:mysql://" + hostname + ":"
    /* String jdbcUrl = "jdbc:mariadb://" + hostname + ":" */
    + port + "/" + sql_database;
    // System.out.println("DBManager: ON");
    Database.Instance.init(username, password, jdbcUrl);
    return Database.Instance;
  }

  public static JDBCConnectionPool getDatabaseInstanceAlternative() throws SQLException {

    JDBCConnectionPool connectionPool =
        new SimpleJDBCConnectionPool("com.mysql.jdbc.Driver", "jdbc:mysql://" + hostname + ":"
            + port + "/" + sql_database, username, password, 2, 5);
    return connectionPool;

  }

}
