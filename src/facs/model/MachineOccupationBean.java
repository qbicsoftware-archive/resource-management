package facs.model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MachineOccupationBean implements Serializable{
  private static final long serialVersionUID = -5872162758160232199L;

  private int id;
  private int deviceId;
  private Date start;
  private Date end;
  private String userName;
  private String userFullName;
  private String application;
  private String role;
  private String department;
  private String institution;
  private String buildVersion;
  private String cytometer;
  private String serialno;
  private String custom;
  private boolean corrupted;
  public MachineOccupationBean(){
    
  }
  
  /**
   * is not generic enough. Might go wrong with different csvs from different devices
   * @param info
   * @param deviceId
   * @throws ParseException
   * @deprecated
   */
  public void setBean(String[] info, int deviceId) throws ParseException {
    if(info == null || info.length != 15){
      throw new IllegalArgumentException("info is not what it supposed to be:" + info.toString());
    }
    id = -1;
    this.deviceId = deviceId;
    userName = info[0];
    userFullName = info[1];
    application = info[2];
    role = info[3];
    department = info[4];
    institution = info[5];
    SimpleDateFormat stmp = new  SimpleDateFormat("hh:mm:ss a MMMMMM dd yyyy");
    start = stmp.parse(info[6] + " " + info[7]);
    end = stmp.parse(info[8] + " " + info[9]);
    buildVersion = info[11];
    cytometer = info[12];
    serialno = info[13];
    custom = info[14];
    corrupted = false;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(int deviceId) {
    this.deviceId = deviceId;
  }

  public Date getStart() {
    return start;
  }

  public void setStart(Date start) {
    this.start = start;
  }

  public Date getEnd() {
    return end;
  }

  public void setEnd(Date end) {
    this.end = end;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getUserFullName() {
    return userFullName;
  }

  public void setUserFullName(String userFullName) {
    this.userFullName = userFullName;
  }

  public String getApplication() {
    return application;
  }

  public void setApplication(String application) {
    this.application = application;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public String getInstitution() {
    return institution;
  }

  public void setInstitution(String institution) {
    this.institution = institution;
  }

  public String getBuildVersion() {
    return buildVersion;
  }

  public void setBuildVersion(String buildVersion) {
    this.buildVersion = buildVersion;
  }

  public String getCytometer() {
    return cytometer;
  }

  public void setCytometer(String cytometer) {
    this.cytometer = cytometer;
  }

  public String getSerialno() {
    return serialno;
  }

  public void setSerialno(String serialno) {
    this.serialno = serialno;
  }

  public String getCustom() {
    return custom;
  }

  public void setCustom(String custom) {
    this.custom = custom;
  }
  
  
  public boolean isCorrupted() {
    return corrupted;
  }

  public void setCorrupted(boolean corrupted) {
    this.corrupted = corrupted;
  }

  /**
   * maps the headers that we need or want to have on the headers of the csv file.
   * TODO
   * Maybe it is better to read those information from a properties file, in case some column headers change
   * Or any other better idea?
   * @param info
   * @return
   */
  public static Map<String, Integer> getHeaderNumbers(String[] info) {
    //User Name,Full Name,Application,Role,Department,Institution,LogIn Time,LogIn Date,LogOut Time,LogOut Date,login time,Build Version,Cytometer,Serial No,Custom
    HashMap<String, Integer> ret = new HashMap<String, Integer>(info.length);
    for(int i = 0; i < info.length; i++){
      ret.put(info[i], i);
    }
    return ret;
  }

  public void setBean(String[] row, int deviceId, Map<String, Integer> headerNumbers) {
      id = -1;
      this.deviceId = deviceId;
      userName = setParameter(row, headerNumbers, "User Name");
       
      userFullName = setParameter(row, headerNumbers, "Full Name");
      
      application =setParameter(row, headerNumbers, "Application");
      role = setParameter(row, headerNumbers, "Role");
      department =setParameter(row, headerNumbers, "Department");
      institution = setParameter(row, headerNumbers, "Institution");
      
      if(userName.isEmpty() && userFullName.isEmpty()) corrupted = true;
      
      SimpleDateFormat stmp = new  SimpleDateFormat("hh:mm:ss a MMMMMM dd yyyy");
      try{
        start = setFormat(row,headerNumbers,"LogIn Time", "LogIn Date", stmp);
      } catch(ParseException e){
        System.out.println(stmp.format(new Date())+ " log: "+ e.getMessage());
        start = null;
        corrupted = true;
      }
      try{
        end = setFormat(row,headerNumbers,"LogOut Time", "LogOut Date", stmp);
      } catch(ParseException e){
        System.out.println(stmp.format(new Date())+ " log: "+ e.getMessage());
        end = null;
        corrupted = true;       
      }
      buildVersion = setParameter(row, headerNumbers, "Build Version");
      cytometer = setParameter(row, headerNumbers, "Cytometer");
      serialno = setParameter(row, headerNumbers, "Serial No");
      custom = setParameter(row, headerNumbers, "Custom");    
  }
  //DateFormat

  private String setParameter(String[] row, Map<String, Integer> headerNumbers, String parameter) {
    Integer un = headerNumbers.get(parameter);
    return (un == null || un < 0 || un >=row.length)? "" : row[un];
  }
  
  private Date setFormat(String[] row, Map<String, Integer> headerNumbers, String time, String date, DateFormat parser) throws ParseException {
    
    Integer s = headerNumbers.get(time);
    Integer e = headerNumbers.get(date);
    return (s == null || s < 0 || s >=row.length || e == null || e < 0 || e >=row.length)? null : parser.parse(row[s] + " " + row[e]);
  }
  
}
