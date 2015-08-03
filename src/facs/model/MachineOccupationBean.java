package facs.model;

import java.io.Serializable;
import java.sql.Date;

public class MachineOccupationBean implements Serializable{
  private static final long serialVersionUID = -5872162758160232199L;

  private int id;
  private String device;
  private Date start;
  private Date end;
  private String user;
}
