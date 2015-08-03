package facs.test;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.nio.file.Paths;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.apache.velocity.runtime.log.LogSystem;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestVelocity {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {}

  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}
  //pdflatex -interaction=batchmode test.tex
  @Test
  public void test() {

    try {
      String templates = Paths.get(System.getProperty("user.dir"), "test/facs/test/templates").toFile().toString();
      Velocity.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, templates);
      Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
      MyVelocityLogChute log = new MyVelocityLogChute();
      Velocity.init();
      
      Template t = Velocity.getTemplate("Angebot.tex");
      VelocityContext context = new VelocityContext();
      context.put("institute", "BER - Berliner Flughafen");
      context.put("PI", "klaus wowereit");
      context.put("street", "am berliner flughafen 12");
      context.put("postalcode", "D-12345");
      context.put("city", "Berlin");
      
      context.put("sender_postalcode", "sender postal");
      context.put("sender_city", "davidcity");
      
      File file = new File(Paths.get(System.getProperty("user.dir"), "test/facs/test/templates", "test.tex").toFile().toString());
      // if file doesnt exists, then create it
      if (!file.exists()) {
          file.createNewFile();
      }
      FileWriter fw = new FileWriter(file.getAbsoluteFile());
      BufferedWriter bw = new BufferedWriter(fw);
      
      t.merge(context, bw);
      bw.close();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      fail();
    }
  }


  
  public class MyVelocityLogChute implements LogChute
  {
    public MyVelocityLogChute()
    {
 
      try
      {
        /*
         *  register this class as a logger with the Velocity singleton
         *  (NOTE: this would not work for the non-singleton method.)
         */
        Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, this );
      }
      catch (Exception e)
      {
        /*
         *  do something
         */
      }
    }

    /**
     *  This init() will be invoked once by the LogManager
     *  to give you the current RuntimeServices intance
     */
    public void init(RuntimeServices rsvc)
    {
      // do nothing
    }

    /**
     *  This is the method that you implement for Velocity to
     *  call with log messages.
     */
    public void log(int level, String message)
    {
      System.out.println("[" + String.valueOf(level) +"] " + message);
    }

    /**
     *  This is the method that you implement for Velocity to
     *  call with log messages.
     */
    public void log(int level, String message, Throwable t)
    {
      /*  do something useful */
    }

    /**
     *  This is the method that you implement for Velocity to
     *  check whether a specified log level is enabled.
     */
    public boolean isLevelEnabled(int level)
    {
      /*  do something useful */
      return true;
    }
  }
  
  
  
  
}
