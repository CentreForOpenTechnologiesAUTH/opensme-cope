/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Apostolos Kritikos <akritiko@csd.auth.gr>
 */
public class FileSystemHandlerUtil {

    public FileSystemHandlerUtil() {
    }

    public List<File> getFirstLevelChildrenDirectories(String parentDirectoryPath) {
        File parentDirectory = new File(parentDirectoryPath);

        ArrayList<File> directories = new ArrayList<File>();

        if (parentDirectory.exists() && parentDirectory.isDirectory()) {
            String[] candidates = parentDirectory.list();

            File candidate;

            for (String candidatePath : candidates) {
                candidate = new File(parentDirectoryPath + File.separator + candidatePath);

                if (candidate.exists() && candidate.isDirectory()) {
                    directories.add(candidate);
                }
            }
        }
        return directories;
    }

    public List<File> getFilesWithinDirectory(String parentDirectoryPath) {

        ArrayList<File> files = new ArrayList<File>();
        File theRootDir = new File(parentDirectoryPath);

        //Retrieve all folders (current and any sub)
        ArrayList<File> folders = getAllDir(theRootDir);

        //For all of the folders
        for (int i = 0; i < folders.size(); i++) {

            //Get all the files in the folder
            File[] currentFile = folders.get(i).listFiles();
            for (int k = 0; k < currentFile.length; k++) {

                //If the current file isn't a directory
                //Add it to the list of all files
                if (currentFile[k].isDirectory() == false) {
                    files.add(currentFile[k]);
                }
            }
        }
        return files;
    }

    public List<File> getJavaFilesWithinDirectory(String parentDirectoryPath) {

        ArrayList<File> files = new ArrayList<File>();
        File theRootDir = new File(parentDirectoryPath);
        if(!theRootDir.exists() || !theRootDir.isDirectory())
        {
            return files;
        }
        //Retrieve all folders (current and any sub)
        ArrayList<File> folders = getAllDir(theRootDir);

        //For all of the folders
        for (int i = 0; i < folders.size(); i++) {

            //Get all the files in the folder
            File[] currentFile = folders.get(i).listFiles();
            for (int k = 0; k < currentFile.length; k++) {

                //If the current file isn't a directory
                //Add it to the list of all files
                if (currentFile[k].isDirectory() == false) {
                    if (currentFile[k].getName().contains(".java")) {
                        files.add(currentFile[k]);
                    }
                }
            }
        }
        return files;
    }

    public String convertFilePathToCOPEDBClassAnalysisName(File file) {
        String[] initialPath;
        if (System.getProperty("os.name").contains("Windows")) {
            initialPath = file.getAbsolutePath().split(File.separator+File.separator);
            
        } else {
            initialPath = file.getAbsolutePath().split(File.separator);
        }
        String correctName = "";

        int indexToStartCreatingPath = 0;

        for (int i = 0; i < initialPath.length; i++) {
            if (!initialPath[i].equals("src")) {
                indexToStartCreatingPath++;
            } else {
                break;
            }
        }
        for (int i = (indexToStartCreatingPath + 1); i < initialPath.length; i++) {
            if (initialPath[i].contains(".")) {
                String[] parts = initialPath[i].split("\\.");
                correctName += parts[0];
            } else {
                correctName += initialPath[i] + ".";
            }
        }
        return correctName;
    }

    private ArrayList<File> getAllDir(File rootURL) {

        ArrayList<File> temp = new ArrayList<File>(), //This will hold our queued folders
                fill = new ArrayList<File>(), //List of end results
                subs = new ArrayList<File>(); //Sub folders

        //Add our initial to start search (Breadth First Search)
        temp.add(rootURL);
        while (!temp.isEmpty()) {

            //Dequeue Folder
            File next = temp.remove(0);

            //Add it to the return list if not done so already and not blank
            if (!fill.contains(next) && !next.getAbsolutePath().equals("")) {
                fill.add(next);
            }

            //Get sub folders
            subs = getSubs(next);

            //for each folder, add it to temp if not done so already
            for (File s : subs) {
                if (!temp.contains(s)) {
                    temp.add(s);
                }
            }
            //clear for next iteration
            subs.clear();
        }
        return fill;
    }

    private ArrayList<File> getSubs(File cur) {

        //Get a list of all the files in folder
        ArrayList<File> temp = new ArrayList<File>();
        File[] fileList = cur.listFiles();

        //for each file in the folder
        for (int i = 0; i < fileList.length; i++) {

            //If the file is a Directory(folder) add it to return, if not done so already
            File choose = fileList[i];
            if (choose.isDirectory() && !temp.contains(choose)) {
                temp.add(choose);
            }
        }
        return temp;
    }

    public static void main(String[] args) {
        FileSystemHandlerUtil test = new FileSystemHandlerUtil();
        ArrayList<File> files = (ArrayList<File>) test.getFilesWithinDirectory("/home/akritiko/BORG/clusters/depClusters/clusterLvl3Num5");
        for (File file : files) {
            System.out.println(file.getName());
        }
    }
}
