/*******************************************************************************
 * QBiC Calendar provides an infrastructure for defining calendars for specific purposes like booking devices or
 * planning resources for services and integration of relevant data into the common portal infrastructure.
 * Copyright (C) 2016 Aydın Can Polatkan
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
package facs.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import facs.model.MachineOccupationBean;


/**
 * It turns out that the csv files we get from facs facility are not exactly the same. This is an attempt to be somewhat generic in the parsing.
 * 
 * Note: One might make it better, with introducing a fixed width parameter. However, I did not need it so far.
 * Second note: there seems to be a library for that: http://opencsv.sourceforge.net/
 * @author wojnar
 *
 */
public class GenericFacsParser {
  String delimiter = "\"";
  String separator = ",";
  
  public GenericFacsParser(String separator, String delimiter){
    this.delimiter = delimiter;
    this.separator = separator;
  }
  /**
   * default separartor = ','  and delimiter = ' " ' 
   */
  public GenericFacsParser(){
  }
  
  /**
   * Parse instrument statistics from a BufferedReader and return a List of beans.
   * throws IOException if there is a probrem with the bufferedReader.
   * @param br
   * @param deviceId every statistical entry has to belong to a device id
   * @throws IOException 
   * @throws ParseException 
   */
  public List<MachineOccupationBean> parse(BufferedReader br, int deviceId) throws IOException, ParseException{
    List<MachineOccupationBean> ret = new ArrayList<MachineOccupationBean>();
    //read header
    String line = br.readLine();
    String [] info = lineToColumns(line);
    Map<String, Integer> headerNumbers = MachineOccupationBean.getHeaderNumbers(info);
    
    //read values
    while ((line = br.readLine()) != null) {
      info = lineToColumns(line);
      
      MachineOccupationBean bean = new MachineOccupationBean();
      bean.setBean(info, deviceId, headerNumbers);
      ret.add(bean);
    }
    return ret;
  }
  
  public String[] lineToColumns(String line) {
    String[] info = line.split(separator);
    
    //remove delimiter in all columns
    if(delimiter != null && !delimiter.isEmpty()){
      return StringUtils.stripAll(StringUtils.stripAll(info), delimiter);
    }
    return info;
  }


  public String getDelimiter() {
    return delimiter;
  }
  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }
  public String getSeparator() {
    return separator;
  }
  public void setSeparator(String separator) {
    if(separator == null) this.separator = "";
    else this.separator = separator;
  }
}
