/**
 * 
 */
package eu.opensme.cope.factgenerators.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author krap
 * 
 */
public class FileSystemHandler {

	private String rootDirectory;
	private String rootPackage;
	private List<String> packages;

	public FileSystemHandler() {

	}

	public FileSystemHandler(String rootDirectory) {
		this.rootDirectory = rootDirectory;
		this.packages = new ArrayList<String>();

		String[] parts = rootDirectory.split(Pattern.quote(File.separator));
		this.rootPackage = parts[parts.length - 1];
	}

	/**
	 * @return the rootDirectory
	 */
	public String getRootDirectory() {
		return rootDirectory;
	}

	/**
	 * @param rootDirectory
	 *            the rootDirectory to set
	 */
	public void setRootDirectory(String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}

	public String getRootPackage() {
		return rootPackage;
	}

	public void setRootPackage(String rootPackage) {
		this.rootPackage = rootPackage;
	}
	
	public void printPackages() {
		for (int i = 0; i < this.packages.size(); i++) {
			System.out.println(this.packages.get(i));
		}
	}

	public List<String> getPackages() {
		visitAllDirs(new File(this.rootDirectory));
		return this.packages;
	}

	// Process only directories under dir
	private void visitAllDirs(File dir) {

		if (dir.isDirectory()) {
			File[] children = dir.listFiles();

			findPackages(children);

			for (int j = 0; j < children.length; j++) {
				visitAllDirs(children[j]);
			}
		}
	}

	private void findPackages(File[] files) {
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				boolean isPack = false;
				File[] temp = files[i].listFiles();
				for (int j = 0; j < temp.length; j++) {
					if (temp[j].isFile()) {
						isPack = true;
						String packageName = makePackage(temp[j]
								.getParentFile().toString());
						if (!this.packages.contains(packageName)) {
							this.packages.add(packageName);
						}
					}
				}
			}
		}
	}

	private String makePackage(String parentFilePath) {

		String[] parts = parentFilePath.split(Pattern.quote(File.separator));

		String finalPackageName = "";
		boolean allowWriting = false;

		for (int i = 0; i < parts.length; i++) {
			if (allowWriting) {
				finalPackageName += parts[i] + ".";
			}
			if (parts[i].equals(this.rootPackage)) {
				allowWriting = true;
			}
		}

		finalPackageName = finalPackageName.substring(0,
				(finalPackageName.length() - 1));

		return finalPackageName;
	}
}