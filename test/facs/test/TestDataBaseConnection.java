package facs.test;

import static org.junit.Assert.*;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

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
  private String port = "3306";
  private String database = "facs_facility";
  private String username = "facs";
  private String password = "facs";
  
  @Test
  public void test() {
    java.sql.Connection conn = null;
    String jdbcUrl = "jdbc:mysql://" + this.hostname + ":"
            + this.port + "/" + this.database;
    try {
      conn = DriverManager.getConnection(jdbcUrl, username, password);
      try{
        conn.setAutoCommit(false); //transaction block start
        PreparedStatement statement = conn.prepareStatement("INSERT INTO UserGroup VALUES(?)");//("UPDATE people SET lastName = ?, age = ? WHERE id = ?");
        statement.setString(1,"Operator");
        //execute the statement, data IS NOT commit yet
        statement.execute();
        
        //here could be more statements
        
        //nothing will be in the database, until you commit it!
        conn.commit();
      //transaction block end
        
        
        System.out.println("Successfully connected");
        //dont forget to close the connection.
        conn.close();
      }catch (SQLException e){
        //if commiting fails, roll back to have the previous state and unlock locks.
        conn.rollback();
      }

    } catch (SQLException e) {
      System.out.println("conntection failed");
      e.printStackTrace(); 
    }
  }
}
