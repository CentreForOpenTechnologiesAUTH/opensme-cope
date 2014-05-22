package eu.opensme.cope.knowledgemanager.gui.configtool;

import eu.opensme.cope.componentvalidator.util.Utils;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class ConfigUtils {
    
    public static void writePropertyFile(Properties p_prop) throws Exception {
        FileOutputStream fos = null;
        try {
//            fos = new FileOutputStream(ConfigUtils.class.getResource("repository.properties").getFile());
            fos = new FileOutputStream(Utils.getJarFolder() + "repository.properties");
            p_prop.store(fos, "Properties file updated");
        } catch (Exception e) {
            System.err.println("Error in writing Property file.");
            throw new Exception();
        }
    }

    public static Properties readPropertyFile() throws Exception {
        FileInputStream fis = null;
        Properties prop = null;
        try {
            fis = new FileInputStream(Utils.getJarFolder() + "repository.properties");
            if (fis != null) {
                prop = new Properties();
                prop.load(fis);
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            System.err.println("Error in reading Property file. Exception Message = " + e.getMessage());
            prop = null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception ie) {
                    System.err.println("Error in closing the File Input Stream; Exception Message = " + ie.getMessage());
                }
            }
        }
        return prop;
    }
    
}
