package gr.pinotParser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author angor
 */
public class PatternList {

    private ArrayList<Pattern> patterns;
    private String[] patternList = {"Abstract Factory", "(Object)Adapter-Command",
        "Bridge", "Chain of Responsibility", "Composite", "Decorator", "Facade",
        "Factory Method", "Flyweight", "Mediator", "Observer", "Proxy",
        "Singleton", "State", "Strategy", "Template Method", "Visitor"};

    public PatternList() {
        this.patterns = new ArrayList<Pattern>();
    }

    public ArrayList<Pattern> getPatternList() {
        return this.patterns;
    }

    public void addPatternBlock(ArrayList<String> patternBlock, long projectID) {
        String patternType =
                patternBlock.get(0).substring(0, patternBlock.get(0).lastIndexOf(" "));
        Pattern newPattern = null;
        System.out.println("..adding "+ patternType);
        if (patternType.equals("Abstract Factory")) {
            newPattern = new AbstractFactory();
        } else if (patternType.equals("Adapter")) {
            newPattern = new Adapter();
        } else if (patternType.equals("Bridge")) {
            newPattern = new Bridge();
        } else if (patternType.equals("Chain of Responsibility")) {
            newPattern = new ChainOfResponsibility();
        } else if (patternType.equals("Composite")) {
            newPattern = new Composite();
        } else if (patternType.equals("Decorator")) {
            newPattern = new Decorator();
        } else if (patternType.equals("Facade")) {
            newPattern = new Facade();
        } else if (patternType.equals("Factory Method")) {
            newPattern = new FactoryMethod();
        } else if (patternType.equals("Flyweight")) {
            newPattern = new Flyweight();
        } else if (patternType.equals("Mediator")) {
            newPattern = new Mediator();
        } else if (patternType.equals("Observer")) {
            newPattern = new Observer();
        } else if (patternType.equals("Proxy")) {
            newPattern = new Proxy();
        } else if (patternType.equals("Singleton")) {
            newPattern = new Singleton();
        } else if (patternType.equals("State")) {
            newPattern = new State();
        } else if (patternType.equals("Strategy")) {
            newPattern = new Strategy();
        } else if (patternType.equals("Template Method")) {
            newPattern = new TemplateMethod();
        } else if (patternType.equals("Visitor")) {
            newPattern = new Visitor();
        } else {
            System.out.println("##ERROR## -- Pattern with type " + patternType + " not found -- ##ERROR##");
        }
        newPattern.addPatternBlock(patternBlock, projectID);

        if (newPattern.isPatternValid()) {
            this.patterns.add(newPattern);
//            System.out.println("\tAdding pattern " + newPattern.getPatternName());
        }
    }

    public void resetPatternCounters() {
        for (int j = 0; j < this.patterns.size(); j++) {
            this.patterns.get(j).resetPatternCounters();
        }
    }

    public void writeXmlFile(String path) throws IOException {
        System.out.println("\t\t..writing xml file " + path);
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        writer.write("<system>\n");
        for (int i = 0; i < this.patternList.length; i++) {
            writer.write("\t<pattern name=\"" + this.patternList[i] + "\">\n");
            for (int j = 0; j < this.patterns.size(); j++) {
                if (this.patterns.get(j).getPatternName().equals(this.patternList[i])) {
                    //Write the current VALID pattern to the xml file
                    if (this.patterns.get(j).isPatternValid()) {
//                        System.out.println("Writing Pattern: " + this.patterns.get(j).getPatternName());
                        this.patterns.get(j).writePatternToXml(writer);
                    }
                }
            }
            writer.write("\t</pattern>\n");
        }
        writer.write("</system>");
        writer.close();
    }

    public void writeCsvFile(String projectName) throws IOException {
        System.out.println("\t\t..writing csv file " + projectName);
        BufferedWriter writer = new BufferedWriter(new FileWriter(projectName));
        for (int i = 0; i < this.patternList.length; i++) {
            for (int j = 0; j < this.patterns.size(); j++) {
                if (this.patterns.get(j).getPatternName().equals(this.patternList[i])) {
                    //Write the current VALID pattern to the csv file
                    if (this.patterns.get(j).isPatternValid()) {
                        this.patterns.get(j).writePatternToCsv(writer, projectName);
                    }
                }
            }
        }
        writer.close();
    }

    public void writeOutputStatistics(FileWriter outFile, String projectName, PrintWriter out) {
        out.println("\n------------- Project: " + projectName + " --------------");
        out.printf("%-30s %-10s %-10s %-10s\n", "Pattern Type", "Valid", "Invalid", "Total");
        out.println("---------------------------------------------------------");

        Pattern currentPattern = null;
        currentPattern = new AbstractFactory();
        currentPattern.writePatternStatistics(out);
        currentPattern = new Adapter();
        currentPattern.writePatternStatistics(out);
        currentPattern = new Bridge();
        currentPattern.writePatternStatistics(out);
        currentPattern = new ChainOfResponsibility();
        currentPattern.writePatternStatistics(out);
        currentPattern = new Composite();
        currentPattern.writePatternStatistics(out);
        currentPattern = new Decorator();
        currentPattern.writePatternStatistics(out);
        currentPattern = new Facade();
        currentPattern.writePatternStatistics(out);
        currentPattern = new FactoryMethod();
        currentPattern.writePatternStatistics(out);
        currentPattern = new Flyweight();
        currentPattern.writePatternStatistics(out);
        currentPattern = new Mediator();
        currentPattern.writePatternStatistics(out);
        currentPattern = new Observer();
        currentPattern.writePatternStatistics(out);
        currentPattern = new Proxy();
        currentPattern.writePatternStatistics(out);
        currentPattern = new Singleton();
        currentPattern.writePatternStatistics(out);
        currentPattern = new State();
        currentPattern.writePatternStatistics(out);
        currentPattern = new Strategy();
        currentPattern.writePatternStatistics(out);
        currentPattern = new TemplateMethod();
        currentPattern.writePatternStatistics(out);
        currentPattern = new Visitor();
        currentPattern.writePatternStatistics(out);
    }

    public void printSatistics(String projectName) {
        System.out.println("------------- Project: " + projectName + " --------------");
        System.out.printf("%-30s %-10s %-10s %-10s\n", "Pattern Type", "Valid", "Invalid", "Total");
        System.out.println("-------------------------------------------------------------");

        Pattern currentPattern = null;
        currentPattern = new AbstractFactory();
        currentPattern.printPatternStatistics();
        currentPattern = new Adapter();
        currentPattern.printPatternStatistics();
        currentPattern = new Bridge();
        currentPattern.printPatternStatistics();
        currentPattern = new ChainOfResponsibility();
        currentPattern.printPatternStatistics();
        currentPattern = new Composite();
        currentPattern.printPatternStatistics();
        currentPattern = new Decorator();
        currentPattern.printPatternStatistics();
        currentPattern = new Facade();
        currentPattern.printPatternStatistics();
        currentPattern = new FactoryMethod();
        currentPattern.printPatternStatistics();
        currentPattern = new Flyweight();
        currentPattern.printPatternStatistics();
        currentPattern = new Mediator();
        currentPattern.printPatternStatistics();
        currentPattern = new Observer();
        currentPattern.printPatternStatistics();
        currentPattern = new Proxy();
        currentPattern.printPatternStatistics();
        currentPattern = new Singleton();
        currentPattern.printPatternStatistics();
        currentPattern = new State();
        currentPattern.printPatternStatistics();
        currentPattern = new Strategy();
        currentPattern.printPatternStatistics();
        currentPattern = new TemplateMethod();
        currentPattern.printPatternStatistics();
        currentPattern = new Visitor();
        currentPattern.printPatternStatistics();
    }
}
