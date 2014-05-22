package eu.opensme.cope.componentvalidator.core.genmodel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;

import fi.vtt.noen.testgen.model.daikon.DaikonModel;
import fi.vtt.noen.testgen.model.fsm.FSMModel;
import fi.vtt.noen.testgen.parser.DaikonParser;
import fi.vtt.noen.testgen.parser.PromParser;

public class genEFSM {

	private static String efsmPackageName = null;
	private static String efsmClassName = null;
	private static PromParser promParser = new PromParser();
	private static Properties configuration = null;
        private static String extractedDir;
	
	public genEFSM(HashMap<String, String> statesHashMap, HashMap<String, String> variablesHashMap) throws Exception {
		efsmPackageName = "extracted";
		efsmClassName = "test";

		String dfileName = extractedDir + "DaikonTrace.txt";
		String daikonOutput = "";
		System.out.println("Parsin Daikon model");
		daikonOutput = stringFromFile(dfileName);
		DaikonParser parser = new DaikonParser(daikonOutput);
		DaikonModel dm = parser.parseAll();

		String pfileName = extractedDir + "PromTrace.xml";
		System.out.println("Parsin ProM model");
		InputStream in = new FileInputStream(pfileName);
		FSMModel fsm = promParser.parse(in);

		generateEFSM(dm, fsm, statesHashMap, variablesHashMap);
		System.out.println("EFSM created and saved to " + efsmClassName);

	}

        public static void setExtracedDir(String extractedDirectory){
            extractedDir = extractedDirectory;
        }
        
	private static void generateEFSM(DaikonModel dm, FSMModel fsm, HashMap<String, String> statesHashMap, HashMap<String, String> variablesHashMap) throws Exception {
		EFSMGenerator generator = new EFSMGenerator(classUnderTest(), fsm, dm, inputs(), outputs());
		System.out.println("Generating EFSM");
		String efsm = generator.generateEFSM(efsmPackageName, efsmClassName, statesHashMap, variablesHashMap);
                saveToFile(extractedDir + efsmClassName, efsm);
	}

	private static Class classUnderTest() throws Exception {
		return classForProperty("ClassUnderTest");
	}

	private static Class classForProperty(String property) throws Exception {
		if (configuration == null) {
			configuration = new Properties();
			configuration.load(new FileInputStream(extractedDir + "testgen.properties"));
		}
		String className = configuration.getProperty(property);
		if (className == null) {
			return null;
		}
		System.out.println("creating class for:" + className);
                return Class.forName(className);
	}

	private static void saveToFile(String fileName, String content)
			throws Exception {
		fileName += ".java";
		File outFile = new File(fileName);
		FileWriter out = new FileWriter(outFile);
		out.write(content);
		out.close();
	}

	public static Collection<Class> inputs() throws Exception {
		return classesForMultipleProperties("InputInterface");
	}

	public static Collection<Class> outputs() throws Exception {
		return classesForMultipleProperties("OutputInterface");
	}

	private static Collection<Class> classesForMultipleProperties(String prefix)
			throws Exception {
		Collection<Class> classes = new ArrayList<Class>();
		int index = 1;
		while (true) {
			Class clazz = classForProperty(prefix + index);
			index++;
			if (clazz == null) {
				break;
			}
			classes.add(clazz);
		}
		return classes;
	}

	public static String stringFromFile(String fileName) throws IOException {

		File file = new File(fileName);

		FileInputStream fis = new FileInputStream(file);
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		byte[] bytes = new byte[512];

		int readBytes;
		while ((readBytes = fis.read(bytes)) > 0) {
			out.write(bytes, 0, readBytes);
		}

		return new String(out.toByteArray());
	}
}
