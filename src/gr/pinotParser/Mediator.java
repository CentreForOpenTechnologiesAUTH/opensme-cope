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
public class Mediator extends Pattern {

    private String mediator = null;
    private ArrayList<String> colleagues = null;
    private static int validPatternCounter = 0;
    private static int inValidPatternCounter = 0;

    public Mediator() {
        super("Mediator");
        this.colleagues = new ArrayList<String>();
    }

    @Override
    public void resetPatternCounters() {
        validPatternCounter = 0;
        inValidPatternCounter = 0;
    }

    @Override
    public void addPatternBlock(ArrayList<String> block, long projectID) {
        for (int i = 0; i < block.size(); i++) {
            if (block.get(i).contains("is the mediator class")) {
                try {
                    this.mediator = this.getValidClassFullName(
                            this.getClassName(block.get(i)), projectID);
                } catch (SQLException ex) {
                    Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
            } else if (block.get(i).contains("Mediator:")) {
                try {
                    this.mediator = this.getValidClassFullName(
                            block.get(i).substring(block.get(i).indexOf(" ") + 1),
                            projectID);
                } catch (SQLException ex) {
                    Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
            } else if (block.get(i).contains("Colleagues:")
                    || block.get(i).contains("Subtype(s) of colleague(s):")) {
                ArrayList<String> temp = this.getClassNamesFromLine(
                        block.get(i).substring(block.get(i).indexOf(":") + 1,
                        block.get(i).length()));
                if (temp != null && !temp.isEmpty()) {
                    for (int j = 0; j < temp.size(); j++) {
                        String currentColleague;
                        try {
                            currentColleague = this.getValidClassFullName(
                                    temp.get(j), projectID);
                            if (currentColleague != null) {
                                this.colleagues.add(currentColleague);
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger(Mediator.class.getName()).log(
                                    Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }

        // Checking pattern validity..
        if (this.mediator == null || this.colleagues.isEmpty()) {
            this.setValidity(false);
            inValidPatternCounter++;
        } else {
            validPatternCounter++;
        }
    }

    @Override
    public void writePatternToXml(Writer writer) throws IOException {
        writer.write("\t\t<instance>\n");
        writer.write("\t\t\t" + this.getParticipantWithXmlFormat("Mediator",
                this.mediator));
        for (int i = 0; i < this.colleagues.size(); i++) {
            writer.write("\t\t\t" + this.getParticipantWithXmlFormat("Colleague",
                    this.colleagues.get(i)));
        }
        writer.write("\t\t</instance>\n");
    }

    @Override
    public void writePatternToCsv(Writer writer, String projectName) throws IOException {
        writer.write(projectName + ";" + this.getPatternName());
        writer.write(";" + "Mediator" + ";" + this.mediator);
        for (int i = 0; i < this.colleagues.size(); i++) {
            writer.write(";" + "Colleague" + ";" + this.colleagues.get(i));
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
