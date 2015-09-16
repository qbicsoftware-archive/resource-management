package facs.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
      
      context.put("sender_name", "Dr. Stella Autenrieth");
      context.put("sender_function","Geschaeftsfuehrerin");
      context.put("sender_postalcode", "sender postal");
      context.put("sender_city", "davidcity");
      context.put("sender_street", "Auf der Morgenstelle 42");
      context.put("sender_phone","+49-7071-29-72163");
      context.put("sender_email","qbic@qbic.uni");
      context.put("sender_url","qbic.uni-tuebingen.de");
      context.put("sender_faculty","Medizinischen Fakultät");
      context.put("project_description","Dieses Angebot beinhaltet die Etablierungsarbeiten für die proteomeomische Analyse von Formalin-fixed, paraffin-embedded (FFPE) Gewebe. Desweiteren beinhaltet das Angebot die Kosten für proteomische Analysen für insgesamt 12 Proben (4 Herzen mit je 3 Bereiche). Im Vorfeld werden Metadaten zu den jeweiligen Proben ausgetauscht. Die Proben werden dann an das Labor zur massenspektrometrischen Messung durchgereicht. Das Angebot umfasst massenspektrometrische Analysen des Proteoms, sowie die bioinformatische Auswertung und eine Langzeitspeicherung der Rohdaten und Resultate.");
      context.put("project_short_description", "FFPE Proteomics: Methodenetablierung und Experiment");
      context.put("project_number", "QA2014016");
      
      ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
      HashMap<String,String> map = new HashMap<String,String>();
      map.put("date", "21.12.2015");
      map.put("time_frame", "40:20h");
      map.put("description", "blablablabalbalbalbal");
      map.put("cost", "3.50");
      list.add( map );
    
      map =  new HashMap<String,String>(); 
      map.put("date", "21.12.2015");
      map.put("time_frame", "40:20h");
      map.put("description", "blablablabalbalbalbal");
      map.put("cost", "3.50");
      list.add( map );
      map = new HashMap<String,String>();
      map.put("date", "21.11.2015");
      map.put("time_frame", "00:20h");
      map.put("description", "for real this will be a longer exxample. I am a veeeerrrryyyyyy llooooonnnnggggg   description, that basically describes nothing for real!!!");
      map.put("cost", "1.50");
      list.add( map );
      context.put("costs", list);
      context.put("total_cost", "512");
      
      File file = new File(Paths.get(System.getProperty("user.dir"), "test/facs/test/templates", "test.tex").toFile().toString());
      // if file doesnt exists, then create it
      if (!file.exists()) {
          file.createNewFile();
      }
      FileWriter fw = new FileWriter(file.getAbsoluteFile());
      BufferedWriter bw = new BufferedWriter(fw);
      
      t.merge(context, bw);
      bw.close();
      
      //create pdf
      List<String> cmd = new ArrayList<String>(
          Arrays.asList("pdflatex","-interaction=nonstopmode", file.getName()));
      File resultFile = Paths.get(file.getParent(),"test.pdf").toFile();
      //Runtime rt = Runtime.getRuntime();
      //Process p = rt.exec(cmd);
      ProcessBuilder pb = new ProcessBuilder(cmd);
      pb.directory(file.getParentFile());
      Process p = pb.start();
      
      int exitValue = p.waitFor();
      System.out.println(exitValue);
      if(exitValue != 0 && !resultFile.exists()){
        StringBuffer sb = new StringBuffer();
        BufferedReader reader = 
            new BufferedReader(new InputStreamReader(p.getInputStream()));

       String line = "";           
       while ((line = reader.readLine())!= null) {
       sb.append(line + "\n");
       }
        System.out.println(sb.toString());
        fail();
      }
      
      
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
