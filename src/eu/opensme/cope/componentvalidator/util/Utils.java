/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentvalidator.util;

import eu.opensme.cope.componentvalidator.core.Util.TraceMappings;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import eu.opensme.cope.componentvalidator.core.Util.TraceParser;
import fi.vtt.noen.testgen.parser.PromParser;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.tools.ajc.Main;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.cli.CLIContext;
import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.contexts.gui.GUIContext;
import org.processmining.contexts.gui.GUIPluginContext;
import org.processmining.models.graphbased.directed.jgraph.JGraphVisualizationPanel;
import org.processmining.plugins.transitionsystem.miner.TSMiner;
import org.processmining.plugins.transitionsystem.miner.TSMinerInput;
import org.processmining.plugins.transitionsystem.miner.TSMinerOutput;
import org.processmining.plugins.transitionsystem.miner.modir.TSMinerModirInput;
import org.processmining.plugins.transitionsystem.miner.util.TSAbstractions;
import org.processmining.plugins.transitionsystem.miner.util.TSDirections;
import org.processmining.plugins.transitionsystem.miner.util.TSModes;
import org.processmining.plugins.transitionsystem.Visualization;

/**
 *
 * @author barius
 */
public class Utils {

    public final static String xml = "xml";
    public final static String BR = System.getProperty("line.separator");
    public final static String FS = System.getProperty("file.separator");

    /*
     * Get the extension of a file.
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }
    
    public static List<String> getJavaFileList(File aFile) {
        JavaFileReader javaReader = new JavaFileReader();
        javaReader.Process(aFile);
        return javaReader.getJavaFileStringList();
    }

    public static void addToClassPath(String path) {
        if(System.getProperty("java.class.path").length() == 0){
            System.setProperty("java.class.path", path);
        } else {
            System.setProperty("java.class.path", System.getProperty("java.class.path") + ":" + path);
        }
    }

    public static String compileScenarioAjc(String fileName, String aspectJFile, List<String> classes, String compiledDir) throws Exception {       
        classes = excludeExecScenarioFromComponentSrc(fileName, classes);
        List<String> command = new ArrayList<String>();
        //command.add(AspectJBinPath + "ajc");
        command.add("-1.6");
        command.add("-classpath");
        command.add(System.getProperty("java.class.path"));
        command.add("-d");
        command.add(compiledDir);
        command.add(fileName);
        command.add(aspectJFile);
        command.addAll(classes);
        
        int i = 1;
        for(String parameter : command){
            if(i < command.size()){
                System.out.print(parameter + " ");
            } else {
                System.out.println(parameter);
            }
            i++;
        } 
        
        Main compiler = new Main();
        MessageHandler m = new MessageHandler();
        compiler.run(command.toArray(new String[0]), m);
        IMessage[] ms = m.getMessages(null, true);
        System.out.println("messages: " + Arrays.asList(ms));
//        Process p = Runtime.getRuntime().exec(command.toArray(new String[0]));
//        int terminationCode = p.waitFor();
//        if(terminationCode != 0){
//            System.out.println("compile error: " + terminationCode);
//        } 
//        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        
//        String line = null;
//        while ((line = input.readLine()) != null) {
//            System.out.println(line);
//        }
        
        return null;
    }

    public static String getJarFolder(){
        String path = Utils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            String decodedPath = URLDecoder.decode(path, "UTF-8");
            decodedPath = decodedPath.substring(0, decodedPath.lastIndexOf("/") + 1);
            if(decodedPath.contains("/build/classes/")){
                decodedPath = decodedPath.substring(0, decodedPath.lastIndexOf("/build/classes/")+1);
            }
           return decodedPath;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    public static String execPlainScenario(String fileName, String compiledDir, List<String> classes, Boolean isJunit) throws Exception {
        BufferedReader output = compilePlainScenario(fileName, compiledDir, classes, isJunit);
        if (output == null) {
            return null;
        }
//        String line = null;
//        while ((line = output.readLine()) != null) {
//            System.out.println(line);
//        }
        String qualifiedExecName = getThePackage(fileName, true);
        BufferedReader input = executeScenario(qualifiedExecName, isJunit);
        if(input == null){
            return null;
        }
        String results = "";
        String line = null;
        while ((line = input.readLine()) != null) {
            //System.out.println(line);
            results += line+BR;
        }
        
        return results;
    }
    
    public static BufferedReader daikonExecScenario(String fileName, List<String> classes, Boolean isJunit, String compiledDir, String extractedDir) throws Exception {
        BufferedReader output = compilePlainScenario(fileName, compiledDir, classes, isJunit);
//        String line = null;
//        while ((line = output.readLine()) != null) {
//            System.out.println(line);
//        }
        
        /*
        String jUnitRunner = "";
        if (isJunit) {
            jUnitRunner = " org.junit.runner.JUnitCore";
        }
        
        System.out.println("java -cp " + System.getProperty("java.class.path")
                + " daikon.Chicory --dtrace-file="+extractedDir+"fullDaikon.dtrace"
                + jUnitRunner
                + " " + qualifiedExecName);

        p = Runtime.getRuntime().exec("java -cp " + System.getProperty("java.class.path")
                + " daikon.Chicory --dtrace-file="+extractedDir+"fullDaikon.dtrace"
                + jUnitRunner
                + " " + qualifiedExecName);

        input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        line = null;
        while ((line = input.readLine()) != null) {
            System.out.println(line);
        }
        System.out.println("Daikon Executed");
         * 
         */

        
        return output;
    }
    
    public static BufferedReader compilePlainScenario(String fileName, String compiledDir, List<String> classes, Boolean isJunit) throws IOException, InterruptedException{       
        List<String> command = new ArrayList<String>();
        command.add("javac");
        command.add("-classpath");
        command.add(System.getProperty("java.class.path"));
        command.add("-d");
        command.add(compiledDir);
        if(!fileName.equals("")){
            command.add(fileName);
            classes = excludeExecScenarioFromComponentSrc(fileName, classes);
        }
        command.addAll(classes);
        
        int i = 1;
        for(String parameter : command){
            if(i < command.size()){
                System.out.print(parameter + " ");
            } else {
                System.out.println(parameter);
            }
            i++;
        } 
        
        Process p = Runtime.getRuntime().exec(command.toArray(new String[0]));
        
        int terminationCode = p.waitFor();
        if(terminationCode != 0){
            System.out.println("execution error: " + terminationCode);
            return null;
        }        
        
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        return input;
    }
    
    public static BufferedReader executeScenario(String fileName, Boolean isJunit) throws Exception { 
        List<String> command = new ArrayList<String>();
        command.add("java");
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
//        if (isJunit) {
//            command.add("org.junit.runner.JUnitCore");
//        }
        command.add(fileName);
        
        int i = 1;
        for(String parameter : command){
            if(i < command.size()){
                System.out.print(parameter + " ");
            } else {
                System.out.println(parameter);
            }
            i++;
        } 
        Process p = Runtime.getRuntime().exec(command.toArray(new String[0]));

        int terminationCode = p.waitFor();
        if(terminationCode != 0){
            System.out.println("execution error: " + terminationCode);
            return null;
        }
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
//        String line = null;
//        while ((line = input.readLine()) != null) {
//            System.out.println(line);
//        }
        return input;
    }

    public static String getAbsPathFromFile(String fileName) {
        File f = new File(fileName);
        return f.getAbsolutePath();
    }

    public static String getRelPathFromFile(String fileName) {
        String path = fileName;
        String base = System.getProperty("user.dir");
        String relative = new File(base).toURI().relativize(new File(path).toURI()).getPath();

        System.out.println(path + " based on " + base);
        return relative;

    }

    public static void runExecutable(String executable) {
        try {
            String cmd = executable;
            Runtime run = Runtime.getRuntime();
            Process pr = run.exec(cmd);
            pr.waitFor();
            BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = "";
            while ((line = buf.readLine()) != null) {
                System.out.println(line);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static TraceMappings TraceParser(String extractedDir) {
        TraceMappings tm = new TraceMappings();
        try {
            TraceParser tParser = new TraceParser(extractedDir);
            tm = tParser.parse();
        } catch (Exception ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

        return tm;

    }

    public static void addtoURLClassLoader(List<String> externalJars) throws Exception {
        Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
        addURL.setAccessible(true);//you're telling the JVM to override the default visibility
        File[] files = getExternalJars(externalJars);//some method returning the jars to add
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        for (int i = 0; i < files.length; i++) {
            URL url = files[i].toURL();
            addURL.invoke(cl, new Object[]{url});
        }
    }

    private static File[] getExternalJars(List<String> externalJars) {
        File[] files = new File[externalJars.size()];

        for (int i = 0; i < externalJars.size(); i++) {
            files[i] = new File(externalJars.get(i));
        }

        return files;
    }

    public static JComponent getFSM(String text, String promTraceFile) throws FileNotFoundException {

        PromParser promParser = new PromParser();
        InputStream in = new FileInputStream(promTraceFile);


        XLog log = parseLog(in);
        XLogInfo summary = XLogInfoFactory.createLogInfo(log);

        CLIPluginContext context = new CLIPluginContext(new CLIContext(), "PROMTest");

        TSMinerInput input = new TSMinerInput(context, log, summary);
//    System.out.println("converter:"+input.getConverterSettings().getUse(TSConversions.KILLSELFLOOPS));
//    input.getConverterSettings().setUse(TSConversions.KILLSELFLOOPS, false);
        TSMinerModirInput modirInput = new TSMinerModirInput();
        modirInput.setUse(true);
        Collection<String> filters = createFilters(summary);
        modirInput.getFilter().addAll(filters);

        modirInput.setAbstraction(TSAbstractions.SEQUENCE);

        modirInput.setHorizon(1);
        modirInput.setFilteredHorizon(1);
        input.setModirSettings(TSDirections.BACKWARD, TSModes.MODELELEMENT, modirInput);
        TSMiner miner = new TSMiner(context);
        TSMinerOutput output = miner.mine(input);

        GUIPluginContext guiContext = new GUIPluginContext(new GUIContext(), "FSMTest");

        JGraphVisualizationPanel panel = (JGraphVisualizationPanel) Visualization.visualize(guiContext, output.getTransitionSystem());
        
        return panel;
    }

    public static XLog parseLog(InputStream in) {
        try {
            Set<XLog> logs = new XMxmlParser().parse(in);
            Iterator<XLog> i = logs.iterator();
            XLog log = i.next();
            return log;
        } catch (Exception e) {
            throw new RuntimeException("Problem with PROM MXML parser.", e);
        }
    }

    public static Collection<String> createFilters(XLogInfo summary) {
        Collection<String> filters = new ArrayList<String>();
        for (XEventClass xe : summary.getIdClasses().getClasses()) {
            filters.add(xe.toString());
        }
        return filters;
    }

    public static JTabbedPane HashMapToJtable(HashMap<String, String> statesHashMap, HashMap<String, String> variablesHashMap) {
        JTable t1  = new JTable();
        t1 = autoResizeColWidth(t1, toTableModel(statesHashMap, "State", "Function"));
        JScrollPane scrollPane1 = new JScrollPane(t1);
//        JPanel p1 = new JPanel();
//        p1.add(scrollPane1);
        
        JTable t2  = new JTable();
        t2 = autoResizeColWidth(t2, toTableModel(variablesHashMap, "Variable", "Name"));
        JScrollPane scrollPane2 = new JScrollPane(t2);  
//        JPanel p2 = new JPanel();
//        p2.add(scrollPane2);

        JTabbedPane jtp = new JTabbedPane();
        jtp.addTab("State Mappings" ,scrollPane1);
        jtp.addTab("Variable Mappings", scrollPane2);
        
        return jtp;
    }

    public static DefaultTableModel toTableModel(HashMap<String, String> map, String valueDescr, String keyDescr) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{valueDescr, keyDescr}, 0);
        for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
            Entry entry = (Entry) it.next();
            model.addRow(new Object[]{entry.getValue(), entry.getKey()});
        }
        return model;
    }

    public static JTable autoResizeColWidth(JTable table, DefaultTableModel model) {
        //table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setModel(model);
//
//        int margin = 5;
//
//        for (int i = 0; i < table.getColumnCount(); i++) {
//            int vColIndex = i;
//            DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
//            TableColumn col = colModel.getColumn(vColIndex);
//            int width = 0;
//
//            // Get width of column header
//            TableCellRenderer renderer = col.getHeaderRenderer();
//
//            if (renderer == null) {
//                renderer = table.getTableHeader().getDefaultRenderer();
//            }
//
//            Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);
//
//            width = comp.getPreferredSize().width;
//
//            // Get maximum width of column data
//            for (int r = 0; r < table.getRowCount(); r++) {
//                renderer = table.getCellRenderer(r, vColIndex);
//                comp = renderer.getTableCellRendererComponent(table, table.getValueAt(r, vColIndex), false, false,
//                        r, vColIndex);
//                width = Math.max(width, comp.getPreferredSize().width);
//            }
//
//            // Add margin
//            width += 2 * margin;
//
//            // Set the width
//            col.setPreferredWidth(width);
//        }
//
//        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(
//                SwingConstants.LEFT);
//
//        // table.setAutoCreateRowSorter(true);
//        table.getTableHeader().setReorderingAllowed(false);

        return table;
    }
    
    public static String getThePackage(String aClass, boolean qualifiedClassName){
        FileInputStream in = null;
        String pName = "";
        try {            
            in = new FileInputStream(aClass);
            CompilationUnit cu = JavaParser.parse(in);
            if(cu.getPackage() != null){
                pName = cu.getPackage().getName().toString();
            }
            in.close();
        } catch (ParseException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(qualifiedClassName){
            File classFile = new File(aClass);
            String className = classFile.getName().substring(0, classFile.getName().lastIndexOf("."));
            if(pName.length() != 0){
                return pName + "." + className;
            } else {
                return className;
            }
        } else {
            return pName;
        }
        
    }
    
    public static void copyDirectory(File sourceLocation, File targetLocation) {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }
            String[] children = sourceLocation.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {
            InputStream in = null;
            try {
                in = new FileInputStream(sourceLocation);
                OutputStream out = new FileOutputStream(targetLocation);
                // Copy the bits from instream to outstream
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static boolean deleteDirectory(File path) {
        if( path.exists() ) {
          File[] files = path.listFiles();
          for(int i=0; i<files.length; i++) {
             if(files[i].isDirectory()) {
               deleteDirectory(files[i]);
             }
             else {
               files[i].delete();
             }
          }
        }
        return( path.delete() ); 
    }
    
    public static void formatSource(String formaterPath, String sourcePath){
        try {
            Process p = null;
            if(!System.getProperty("os.name").contains("Windows")){
                Runtime.getRuntime().exec("chmod 777 " + formaterPath + "astyle");
                if(new File(sourcePath).isDirectory()){
                    p = Runtime.getRuntime().exec(new String[] { formaterPath + "astyle","-r","-j","--mode=java","--style=java","--suffix=none",sourcePath+"/*.java"});
                } else{
                    p = Runtime.getRuntime().exec(new String[] { formaterPath + "astyle","-j","--mode=java","--style=java","--suffix=none",sourcePath});
                }
            }
            try {
                p.waitFor();
                int terminationCode = p.waitFor();
                if(terminationCode != 0){
                    System.out.println("astyle error can't beautify: " + sourcePath + " \nexit code:" + terminationCode);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    private static List<String> excludeExecScenarioFromComponentSrc(String fileName, List<String> classes) {
        String qualifiedExecName = getThePackage(fileName, true);
        String execFileName = fileName;
        
        if(execFileName.contains(FS)){
            execFileName = execFileName.substring(execFileName.lastIndexOf(FS)+1);
        }
        ArrayList<String> toBeRemoved = new ArrayList<String>();
        for (int i = 0; i < classes.size(); i++) {
            String aclass = classes.get(i);
            if(aclass.contains(execFileName)){
                if(getThePackage(aclass, true).equals(qualifiedExecName)){
                    toBeRemoved.add(aclass);
                }
            }
        }
        classes.removeAll(toBeRemoved);
        return classes;
    }
    
    public static  void createDir(String dir){
        File dirFile = new File(dir);
        if(!dirFile.exists()){
            dirFile.mkdir();
        }
    }
    
    public static void setClassPath(String newClassPath){
        try {
            System.setProperty("java.class.path", newClassPath);
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            fieldSysPath.set( null, null );
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void generateImage(JComponent panel, String saveName, String fileType) {
        int w = panel.getWidth();
        int h = panel.getHeight();
        if(w == 0){
            JPanel tmpPanel = new JPanel();
            tmpPanel.setLayout(new BorderLayout());
            tmpPanel.add(panel, BorderLayout.CENTER);
            JFrame frame = new JFrame();
            frame.getContentPane().add(tmpPanel, BorderLayout.CENTER);
            frame.pack();
            w = panel.getWidth();
            h = panel.getHeight();
        }
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        panel.paint(g);
        try {
            OutputStream out = new FileOutputStream(new File(saveName + "." + fileType));
            ImageIO.write(bi, fileType, out);
            out.flush();
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
