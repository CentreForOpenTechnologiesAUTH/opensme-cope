/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentvalidator.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author barius
 */
public class DirectoryReader {

    static int spc_count = -1;
    static List<String> fileList = new ArrayList<String>();

    public DirectoryReader() {
        super();
    }
    public static void Process(File aFile) {
        spc_count++;
        String spcs = "";
        for (int i = 0; i < spc_count; i++) {
            spcs += " ";
        }
        if (aFile.isFile()) {
            fileList.add(aFile.getAbsolutePath());
//            System.out.println(spcs + "[FILE] " + aFile.getName());
        } else if (aFile.isDirectory()) {
//            System.out.println(spcs + "[DIR] " + aFile.getName());
            File[] listOfFiles = aFile.listFiles();
            if (listOfFiles != null) {
                for (int i = 0; i < listOfFiles.length; i++) {
                    Process(listOfFiles[i]);
                }
            } else {
//                System.out.println(spcs + " [ACCESS DENIED]");
            }
        }
        spc_count--;
    }

    public static List<String> getFileList() {
        return fileList;
    }
}
