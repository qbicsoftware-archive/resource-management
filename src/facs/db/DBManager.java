package facs.db;

public class DBManager {
  private static String hostname = "localhost";
  private static String port = "3306";
  private static String sql_database = "facs_facility";
  private static String username = "facs";
  private static String password = "facs";
  
  public static Database getDatabaseInstance() {
    String jdbcUrl = "jdbc:mysql://" + hostname + ":"
    /*String jdbcUrl = "jdbc:mariadb://" + hostname + ":"*/
        + port + "/" + sql_database;
    Database.Instance.init(username, password, jdbcUrl);
     return Database.Instance;
  }
}
