package gr.pinotParser;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author angor
 */
public class Strategy extends Pattern {

    private String contextClass;
    private String strategyInterface;
    private ArrayList<String> concreteStrategies;
    private static int validPatternCounter = 0;
    private static int inValidPatternCounter = 0;

    public Strategy() {
        super("Strategy");
        this.concreteStrategies = new ArrayList<String>();
    }

    @Override
    public void resetPatternCounters() {
        validPatternCounter = 0;
        inValidPatternCounter = 0;
    }

    @Override
    public void addPatternBlock(ArrayList<String> block, long projectID) {
        for (int i = 0; i < block.size(); i++) {
            if (block.get(i).contains("is the Context class")) {
                try {
                    this.contextClass = this.getValidClassFullName(
                            this.getClassName(block.get(i)), projectID);
                } catch (SQLException ex) {
                    Logger.getLogger(Strategy.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
            } else if (block.get(i).contains("is the Strategy interface")) {
                try {
                    this.strategyInterface = this.getValidClassFullName(
                            this.getClassName(block.get(i)), projectID);
                } catch (SQLException ex) {
                    Logger.getLogger(Strategy.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
            } else if (block.get(i).contains("Concrete Strategy classes:")) {
                if (this.contextClass != null && this.strategyInterface != null) {
                    ArrayList<String> temp = this.getClassNamesFromLine(
                            block.get(i).substring(block.get(i).indexOf(":") + 1));
                    if (!temp.isEmpty()) {
                        for (int j = 0; j < temp.size(); j++) {
                            if (temp.get(j) != null) {
                                String currentSubject;
                                try {
                                    currentSubject = this.getValidClassFullName(
                                            temp.get(j), projectID);
                                    if (currentSubject != null) {
                                        this.concreteStrategies.add(currentSubject);
                                    }
                                } catch (SQLException ex) {
                                    Logger.getLogger(
                                            Strategy.class.getName()).log(Level.SEVERE,
                                            null, ex);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Checking pattern validity..
        if (this.contextClass == null
                || this.strategyInterface == null
                || this.concreteStrategies.isEmpty()) {
            this.setValidity(false);
            inValidPatternCounter++;
        } else {
            validPatternCounter++;
        }
    }

    @Override
    public void writePatternToXml(Writer writer) throws IOException {
        writer.write("\t\t<instance>\n");
        writer.write("\t\t\t" + this.getParticipantWithXmlFormat("Context",
                this.contextClass));
        writer.write("\t\t\t" + this.getParticipantWithXmlFormat("Strategy",
                this.strategyInterface));
        for (int i = 0; i < this.concreteStrategies.size(); i++) {
            writer.write("\t\t\t" + this.getParticipantWithXmlFormat("Subclass",
                    this.concreteStrategies.get(i)));
        }
        writer.write("\t\t</instance>\n");
    }

    @Override
    public void writePatternToCsv(Writer writer, String projectName) throws IOException {
        writer.write(projectName + ";" + this.getPatternName());
        writer.write(";" + "Context" + ";" + this.contextClass);
        writer.write(";" + "Strategy" + ";" + this.strategyInterface);
        for (int i = 0; i < this.concreteStrategies.size(); i++) {
            writer.write(";" + "Subclass" + ";" + this.concreteStrategies.get(i));
        }
        writer.write("\n");
    }

    @Override
    public void printPatternStatistics() {
        System.out.printf("%-30s %-10s %-10s %-10s\n",
                this.getPatternName(), validPatternCounter, inValidPatternCounter,
                (validPatternCounter + inValidPatternCounter));
    }

    @Override
    public void writePatternStatistics(PrintWriter out) {
        out.printf("%-30s %-10s %-10s %-10s\n",
                this.getPatternName(), validPatternCounter, inValidPatternCounter,
                (validPatternCounter + inValidPatternCounter));
    }
}
