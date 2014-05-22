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
public class Adapter extends Pattern {

    private String adapter;
    private String adaptee;
    private static int validPatternCounter = 0;
    private static int inValidPatternCounter = 0;

    public Adapter() {
        super("(Object)Adapter-Command");
    }

    @Override
    public void resetPatternCounters() {
        validPatternCounter = 0;
        inValidPatternCounter = 0;
    }

    @Override
    public void addPatternBlock(ArrayList<String> block, long projectID) {
        for (int i = 0; i < block.size(); i++) {
            if (block.get(i).contains("is an adapter class")) {
                try {
                    this.adapter = this.getValidClassFullName(this.getClassName(
                            block.get(i)), projectID);
                } catch (SQLException ex) {
                    Logger.getLogger(Adapter.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
            } else if (block.get(i).contains("is the adaptee class")) {
                try {
                    this.adaptee = this.getValidClassFullName(this.getClassName(
                            block.get(i)), projectID);
                } catch (SQLException ex) {
                    Logger.getLogger(Adapter.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
            }
        }
        // Checking pattern validity..
        if (this.adapter == null || this.adaptee == null) {
            this.setValidity(false);
            inValidPatternCounter++;
        } else {
            validPatternCounter++;
        }
    }

    @Override
    public void writePatternToXml(Writer writer) throws IOException {
        writer.write("\t\t<instance>\n");
        writer.write("\t\t\t" + this.getParticipantWithXmlFormat(
                "Adapter/ConcreteCommand", this.adapter));
        writer.write("\t\t\t" + this.getParticipantWithXmlFormat(
                "Adaptee/Receiver", this.adaptee));
        writer.write("\t\t</instance>\n");
    }

    @Override
    public void writePatternToCsv(Writer writer, String projectName) throws IOException {
        writer.write(projectName + ";" + this.getPatternName());
        writer.write(";" + "Adapter/ConcreteCommand" + ";" + this.adapter);
        writer.write(";" + "Adaptee/Receiver" + ";" + this.adaptee);
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
