package facs.model;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

public class BookingBean implements Serializable {
  private static final long serialVersionUID = -6717244163406823687L;

  private int booking_id;
  private String user_name;
  private Timestamp start;
  private Timestamp end;
  private String service;
  private double price;
  private String confirmation;
  private String phone;
  private String device_name;

  public BookingBean (int booking_id, String user_name, String phone, Timestamp start, Timestamp end, String service, double price) {
    super();
    this.booking_id = booking_id;
    this.user_name = user_name;
    this.start = start;
    this.end = end;
    //this.service = service;
    this.price = price;
    this.phone = phone;
  }
  
  public BookingBean (int booking_id, String user_name, String phone, String device_name, Timestamp start, Timestamp end, String service, double price, boolean confirmation) {
	super();
	this.booking_id = booking_id;
	this.user_name = user_name;
	this.start = start;
	this.end = end;
	this.service = service;
	this.price = price;
	this.phone = phone;
	this.device_name = device_name;
	if (confirmation==false)
		this.setConfirmation("Confirmed");
	else
		this.setConfirmation("Not Confirmed");
  }

  public int getID() {
	  return booking_id;
  }

  public String getUsername() {
    return user_name;
  }
  
  public Timestamp getStart() {
    return start;
  }
  
  public Timestamp getEnd() {
    return end;
  }
  
  public String getPhone() {
	    return phone;
  }  
  
  public String getDeviceName() {
    return device_name;
  }
  
  public String getService() {
	    return service;
  }
  
  public double getPrice() {
    return price;
  }

  public String getConfirmation() {
	return confirmation;
  }

  public void setConfirmation(String confirmation) {
	this.confirmation = confirmation;
  }
  
}
