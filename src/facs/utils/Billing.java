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
package facs.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;


/**
 * Create a bill with given information.
 * 
 */
public class Billing {
  // Path to template(s) folder
  File templatesPath;
  // name of the current template file
  String templateFileName;

  // pdflatex path for local development
  String pdflatexPath = "/Library/TeX/texbin/pdflatex";

  // pdflatex path for testing and production
  // String pdflatexPath = "pdflatex";

  Template template;
  File tempTexFile;
  MyVelocityLogChute log;

  private final String RESOURCE_LOADER = "file";
  private VelocityContext context;


  public Billing(File templatesPath, String currentTemplateFileName)
      throws ResourceNotFoundException, ParseErrorException, Exception {
    this.templatesPath = generateTempDirectory(templatesPath);


    Velocity.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, this.templatesPath.toString());
    Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, RESOURCE_LOADER);
    log = new MyVelocityLogChute();
    Velocity.init();

    setTemplate(currentTemplateFileName);
  }

  public void setTemplate(String templateFileName) throws ResourceNotFoundException,
      ParseErrorException, Exception {
    this.templateFileName = templateFileName;
    template = Velocity.getTemplate(templateFileName);
    tempTexFile =
        new File(Paths.get(this.templatesPath.getAbsolutePath(), templateFileName + "tmp.tex")
            .toFile().toString());
    context = new VelocityContext();
  }

  public String getTemplateFileName() {
    return templateFileName;
  }

  public void setReceiverInstitution(String institution) {
    context.put("institute", institution);
  }

  public void setReceiverPI(String pi) {
    context.put("PI", pi);
  }

  public void setReceiverStreet(String street) {
    context.put("street", street);
  }

  public void setReceiverPostalCode(String postalCode) {
    context.put("postalcode", postalCode);
  }

  public void setReceiverCity(String city) {
    context.put("city", city);
  }

  public void setSenderName(String name) {
    context.put("sender_name", name);
  }

  public void setSenderFunction(String func) {
    context.put("sender_function", func);
  }

  public void setSenderPostalCode(String postalcode) {
    context.put("sender_postalcode", postalcode);
  }

  public void setSenderInstitute(String institute) {
    context.put("sender_institute", institute);
  }

  public void setSenderCity(String city) {
    context.put("sender_city", city);
  }

  public void setSenderStreet(String street) {
    context.put("sender_street", street);
  }

  public void setSenderPhone(String phone) {
    context.put("sender_phone", phone);
  }

  public void setSenderEmail(String emial) {
    context.put("sender_email", emial);
  }

  public void setSenderUrl(String url) {
    context.put("sender_url", url);
  }

  public void setSenderTitle(String title) {
    context.put("sender_title", title);
  }

  public void setSenderFaculty(String fac) {
    context.put("sender_faculty", fac);
  }

  public void setInvoiceNumber(String invoiceNumber) {
    context.put("invoice_number", invoiceNumber);
  }

  public void setProjectDescription(String desc) {
    context.put("project_description", desc);
  }

  public void setProjectShortDescription(String shortDesc) {
    context.put("project_short_description", shortDesc);
  }

  public void setProjectNumber(String number) {
    context.put("project_number", number);
  }

  public void setTotalCost(String costs) {
    context.put("total_cost", costs);
  }

  public void setCostEntries(List<CostEntry> entries) {
    ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
    for (CostEntry entry : entries) {
      HashMap<String, String> map = new HashMap<String, String>();
      map.put("date", entry.getDate());
      map.put("time_frame", entry.getTime_frame());
      map.put("description", entry.getDescription());
      map.put("cost", String.format("%1$.2f", entry.getCost()));
      list.add(map);
    }
    context.put("costs", list);
  }

  /**
   * creates temporary tex file and executes pdflatex in order to create the final pdf file WARNING:
   * Be sure that you have set all parameters before executing that method. Otherwise you will get a
   * apache velocity error.
   * 
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  public File createPdf() throws IOException, InterruptedException {
    if (!tempTexFile.exists()) {
      tempTexFile.createNewFile();
    }
    FileWriter fw = new FileWriter(tempTexFile.getAbsoluteFile());
    BufferedWriter bw = new BufferedWriter(fw);

    template.merge(context, bw);
    bw.close();

    List<String> cmd =
        new ArrayList<String>(Arrays.asList(pdflatexPath, "-interaction=nonstopmode",
            tempTexFile.getName()));
    String basename = FilenameUtils.getBaseName(tempTexFile.getName());

    String fileNamme = basename + ".pdf";
    File resultFile = Paths.get(tempTexFile.getParent(), fileNamme).toFile();
    // Runtime rt = Runtime.getRuntime();
    // Process p = rt.exec(cmd);
    ProcessBuilder pb = new ProcessBuilder(cmd);
    pb.directory(tempTexFile.getParentFile());
    System.out.println("Basename: " + basename + " fileNamme: " + fileNamme);
    Process p = pb.start();

    int exitValue = p.waitFor();
    if (exitValue != 0 && !resultFile.exists()) {
      StringBuffer sb = new StringBuffer();
      BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

      String line = "";
      while ((line = reader.readLine()) != null) {
        sb.append(line + "\n");
      }
      System.out.println(sb.toString());


      // TODO There is for sure a better exception to say that pdflatex failed?
      throw new FileNotFoundException("result file " + resultFile.getAbsolutePath()
          + "does not exist.");
    } else {
      return resultFile;
    }
  }


  /**
   * Format example: date 21.12.2015 time frame 40:20h description 'for real this will be a longer
   * exxample. I am a veeeerrrryyyyyy llooooonnnnggggg description, that basically describes
   * nothing' cost 3.50
   * 
   */
  public class CostEntry {
    private String date;
    private String time_frame;
    private String description;
    private float cost;

    public CostEntry(String date, String time_frame, String description, float cost) {
      super();
      this.date = date;
      this.time_frame = time_frame;
      this.description = description;
      this.cost = cost;
    }

    public String getDate() {
      return date;
    }

    public void setDate(String date) {
      this.date = date;
    }

    public String getTime_frame() {
      return time_frame;
    }

    public void setTime_frame(String time_frame) {
      this.time_frame = time_frame;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public float getCost() {
      return cost;
    }

    public void setCost(float cost) {
      this.cost = cost;
    }

  }


  public class MyVelocityLogChute implements LogChute {
    public MyVelocityLogChute() {

      try {
        /*
         * register this class as a logger with the Velocity singleton (NOTE: this would not work
         * for the non-singleton method.)
         */
        Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, this);
      } catch (Exception e) {
        /*
         * do something
         */
      }
    }

    /**
     * This init() will be invoked once by the LogManager to give you the current RuntimeServices
     * intance
     */
    @Override
    public void init(RuntimeServices rsvc) {
      // do nothing
    }

    /**
     * This is the method that you implement for Velocity to call with log messages.
     */
    @Override
    public void log(int level, String message) {
      System.out.println("[" + String.valueOf(level) + "] " + message);
    }

    /**
     * This is the method that you implement for Velocity to call with log messages.
     */
    @Override
    public void log(int level, String message, Throwable t) {
      /* do something useful */
    }

    /**
     * This is the method that you implement for Velocity to check whether a specified log level is
     * enabled.
     */
    @Override
    public boolean isLevelEnabled(int level) {
      /* do something useful */
      return true;
    }
  }

  /**
   * CODE DUPLICATION!! This code is copied from workflow_api guse.impl.GuseWorkflowFileSystem
   */

  /**
   * creates a random temporary directory for the given workflowPath. And copies the original
   * workflow directory to the temp one.
   * 
   * @param extension
   * @return returns the created File name. Be aware that this file does not exists.
   */
  public File generateTempDirectory(File workflowPath) {
    File tmp = null;
    try {
      tmp = File.createTempFile(workflowPath.getName(), "tmp", null);
      tmp.deleteOnExit();
      copy(Paths.get(workflowPath.toURI()), Paths.get(tmp.toURI()));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return tmp;
  }

  /**
   * Helper method. copies files and directories from source to target source and target can be
   * created via the command: FileSystems.getDefault().getPath(String path, ... String more) or
   * Paths.get(String path, ... String more) Note: overrides existing folders
   * 
   * @param source
   * @param target
   * @return true if copying was successful
   */
  public boolean copy(final Path source, final Path target) {
    try {
      Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
          new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {
              Path targetdir = target.resolve(source.relativize(dir));
              try {
                Files.copy(dir, targetdir, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
              } catch (FileAlreadyExistsException e) {
                if (!Files.isDirectory(targetdir)) {
                  throw e;
                }
              }

              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
              Files.copy(file, target.resolve(source.relativize(file)));
              return FileVisitResult.CONTINUE;

            }

          });
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
      return false;
    }
    return true;
  }

}
