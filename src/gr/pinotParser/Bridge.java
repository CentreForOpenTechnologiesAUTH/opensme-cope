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
public class Bridge extends Pattern {

    private String abstractPart;
    private String interfacePart;
    private static int validPatternCounter = 0;
    private static int inValidPatternCounter = 0;

    public Bridge() {
        super("Bridge");
    }

    @Override
    public void addPatternBlock(ArrayList<String> block, long projectID) {
        for (int i = 0; i < block.size(); i++) {
            if (block.get(i).contains("is abstract")) {
                try {
                    this.abstractPart = this.getValidClassFullName(
                            this.getClassName(block.get(i)), projectID);
                } catch (SQLException ex) {
                    Logger.getLogger(Bridge.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
            } else if (block.get(i).contains("is an interface")) {
                try {
                    this.interfacePart = this.getValidClassFullName(
                            this.getClassName(block.get(i)), projectID);
                } catch (SQLException ex) {
                    Logger.getLogger(Bridge.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
            }
        }
        // Checking pattern validity..
        if (this.abstractPart == null || this.interfacePart == null) {
            this.setValidity(false);
            inValidPatternCounter++;
        } else {
            validPatternCounter++;
        }

    }

    public String getAbstractWithWmlFormat() {
        return "<role name=\"Abstraction\" element=\"" + this.abstractPart + "\" />";
    }

    public String getInterfaceWithXmlFormat() {
        return "<role name=\"Implementor\" element=\"" + this.interfacePart + "\" />";
    }

    @Override
    public void writePatternToXml(Writer writer) throws IOException {
        writer.write("\t\t<instance>\n");
        writer.write("\t\t\t" + this.getParticipantWithXmlFormat("Abstraction",
                this.abstractPart));
        writer.write("\t\t\t" + this.getParticipantWithXmlFormat("Implementor",
                this.interfacePart));
        writer.write("\t\t</instance>\n");
    }

    @Override
    public void writePatternToCsv(Writer writer, String projectName) throws IOException {
        writer.write(projectName + ";" + this.getPatternName());
        writer.write(";" + "Abstraction" + ";" + this.abstractPart);
        writer.write(";" + "Implementor" + ";" + this.interfacePart);
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
