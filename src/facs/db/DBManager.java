package facs.db;

public class DBManager {

	  
  // local testing  
  private static String hostname = "localhost";
  private static String port = "8889";
  private static String sql_database = "facs_facility";
  private static String username = "facs";
  private static String password = "facs";

  /*
  // portal testing  
  private static String hostname = "portal-testing.am10.uni-tuebingen.de";
  private static String port = "3306";
  private static String sql_database = "facs_facility";
  private static String username = "mariadbuser";
  private static String password = "dZAmDa9-Ysq_Zv1AGygQ";


  // portal production  
  private static String hostname = "localhost";
  private static String port = "3306";
  private static String sql_database = "facs_facility";
  private static String username = "iigpo01";
  private static String password = "Eehae2ui6goaphae0ugiegh8";
  */
	
  public static Database getDatabaseInstance() {
    String jdbcUrl = "jdbc:mysql://" + hostname + ":"
    /*String jdbcUrl = "jdbc:mariadb://" + hostname + ":"*/
        + port + "/" + sql_database;
    //System.out.println("DBManager: ON");  
    Database.Instance.init(username, password, jdbcUrl);
     return Database.Instance;
  }
  
}
