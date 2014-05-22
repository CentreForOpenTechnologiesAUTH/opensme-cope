/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentvalidator.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thanasis
 */
public class JavaFileReader {

    private static ArrayList<File> javaFileList;
    
    public JavaFileReader(){
        javaFileList = new ArrayList<File>();
    }
    
    public void Process(File aFile) {
        if(aFile.isFile()) {
          javaFileList.add(aFile);
        } else if (aFile.isDirectory()) {
          File[] listOfFiles = aFile.listFiles(new JavaFileDirFilter());
          if(listOfFiles!=null) {
            for (File file : listOfFiles){
              Process(file);
            }
          }
        }
    }
    
    public ArrayList<File> getJavaFileList(){
        return javaFileList;
    }
    
    public List<String> getJavaFileStringList(){
        List<String> list = new ArrayList<String>();
        for(File file : javaFileList){
            list.add(file.toString());
        }
        return list;
    }
    
    private class JavaFileDirFilter implements FileFilter
    {
        @Override
        public boolean accept(File file) {
            if (file.toString().endsWith(".java") || file.toString().endsWith(".JAVA") || file.isDirectory())
            {
               return true;
            }
            return false;
        }
    }
}
