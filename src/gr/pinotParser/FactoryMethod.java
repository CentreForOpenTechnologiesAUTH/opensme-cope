package gr.pinotParser;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
//import gr.subclassdetector.SubClassObject;
//import gr.subclassdetector.SubClassObjectList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author angor
 */
public class FactoryMethod extends Pattern {

    private String creator;
    private String product;
    private static int validPatternCounter = 0;
    private static int inValidPatternCounter = 0;

    public FactoryMethod() {
        super("Factory Method");
    }

    @Override
    public void resetPatternCounters() {
        validPatternCounter = 0;
        inValidPatternCounter = 0;
    }

    @Override
    public void addPatternBlock(ArrayList<String> block, long projectID) {
        for (int i = 0; i < block.size(); i++) {
            if (block.get(i).contains("is a Factory Method class")) {
                try {
                    this.creator = this.getValidClassFullName(
                            this.getClassName(block.get(i)), projectID);
                } catch (SQLException ex) {
                    Logger.getLogger(FactoryMethod.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
            } else if (block.get(i).contains("which extends")) {
                String temp = block.get(i).substring(block.get(i).lastIndexOf(" ") + 1);
                try {
                    this.product = this.getValidClassFullName(temp, projectID);
                } catch (SQLException ex) {
                    Logger.getLogger(FactoryMethod.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
            }
        }

        // Checking pattern validity..
        if (this.creator == null || this.product == null) {
            this.setValidity(false);
            inValidPatternCounter++;
        } else {
            validPatternCounter++;
        }
    }

    @Override
    public void writePatternToXml(Writer writer) throws IOException {
        writer.write("\t\t<instance>\n");
        writer.write("\t\t\t" + this.getParticipantWithXmlFormat("Creator",
                this.creator));
        writer.write("\t\t\t" + this.getParticipantWithXmlFormat("Product",
                this.product));
        writer.write("\t\t</instance>\n");
    }

    @Override
    public void writePatternToCsv(Writer writer, String projectName) throws IOException {
        writer.write(projectName + ";" + this.getPatternName());
        writer.write(";" + "Creator" + ";" + this.creator);
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
