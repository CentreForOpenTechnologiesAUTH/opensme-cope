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
public class Facade extends Pattern {

    private String facade;
    private ArrayList<String> hiddenTypes;
    private static int validPatternCounter = 0;
    private static int inValidPatternCounter = 0;

    public Facade() {
        super("Facade");
        this.hiddenTypes = new ArrayList<String>();
    }

    @Override
    public void resetPatternCounters() {
        validPatternCounter = 0;
        inValidPatternCounter = 0;
    }

    @Override
    public void addPatternBlock(ArrayList<String> block, long projectID) {
        for (int i = 0; i < block.size(); i++) {
            if (block.get(i).contains("is a facade class")) {
                try {
                    this.facade = this.getValidClassFullName(this.getClassName(
                            block.get(i)), projectID);
                } catch (SQLException ex) {
                    Logger.getLogger(Facade.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (block.get(i).contains("Hidden types:")) {
                if (this.facade != null) {
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
                                    this.hiddenTypes.add(currentColleague);
                                }
                            } catch (SQLException ex) {
                                Logger.getLogger(Facade.class.getName()).log(
                                        Level.SEVERE, null, ex);
                            }

                        }
                    }
                }
            }
        }

        // Checking pattern validity..
        if (this.facade == null || this.hiddenTypes.isEmpty()) {
            this.setValidity(false);
            inValidPatternCounter++;
        } else {
            validPatternCounter++;
        }
    }

    @Override
    public void writePatternToXml(Writer writer) throws IOException {
        writer.write("\t\t<instance>\n");
        writer.write("\t\t\t" + this.getParticipantWithXmlFormat("Facade", this.facade));
        for (int i = 0; i < this.hiddenTypes.size(); i++) {
            writer.write("\t\t\t" + this.getParticipantWithXmlFormat("HiddenType",
                    this.hiddenTypes.get(i)));
        }
        writer.write("\t\t</instance>\n");
    }

    @Override
    public void writePatternToCsv(Writer writer, String projectName) throws IOException {
        writer.write(projectName + ";" + this.getPatternName());
        writer.write(";" + "Facade" + ";" + this.facade);
        for (int i = 0; i < this.hiddenTypes.size(); i++) {
            writer.write(";" + "HiddenType" + ";" + this.hiddenTypes.get(i));
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
