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
public class Decorator extends Pattern {

    private String decorator;
    private String component;
    private static int validPatternCounter = 0;
    private static int inValidPatternCounter = 0;

    public Decorator() {
        super("Decorator");
    }

    @Override
    public void resetPatternCounters() {
        validPatternCounter = 0;
        inValidPatternCounter = 0;
    }

    @Override
    public void addPatternBlock(ArrayList<String> block, long projectID) {
        for (int i = 0; i < block.size(); i++) {
            if (block.get(i).contains("is a Decorator class")) {
                try {
                    this.decorator = this.getValidClassFullName(
                            this.getClassName(block.get(i)), projectID);
                } catch (SQLException ex) {
                    Logger.getLogger(Decorator.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
            } else if (block.get(i).contains("is the Decoratee class")) {
                String temp = block.get(i).substring(block.get(i).indexOf("of type") + 8,
                        block.get(i).indexOf(" is the Decoratee class"));
                try {
                    this.component = this.getValidClassFullName(temp, projectID);
                } catch (SQLException ex) {
                    Logger.getLogger(Decorator.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
            }
        }

        // Checking pattern validity..
        if (this.decorator == null || this.component == null) {
            this.setValidity(false);
            inValidPatternCounter++;
        } else {
            validPatternCounter++;
        }
    }

    @Override
    public void writePatternToXml(Writer writer) throws IOException {
        writer.write("\t\t<instance>\n");
        writer.write("\t\t\t" + this.getParticipantWithXmlFormat("Component",
                this.component));
        writer.write("\t\t\t" + this.getParticipantWithXmlFormat("Decorator",
                this.decorator));
        writer.write("\t\t</instance>\n");
    }

    @Override
    public void writePatternToCsv(Writer writer, String projectName) throws IOException {
        writer.write(projectName + ";" + this.getPatternName());
        writer.write(";" + "Component" + ";" + this.component);
        writer.write(";" + "Decorator" + ";" + this.decorator);
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
