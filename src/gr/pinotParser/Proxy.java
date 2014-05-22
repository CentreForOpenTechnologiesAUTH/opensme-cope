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
public class Proxy extends Pattern {

    private String proxy;
    private String subject;
    private ArrayList<String> realSubjects;
    private static int validPatternCounter = 0;
    private static int inValidPatternCounter = 0;

    public Proxy() {
        super("Proxy");
        this.realSubjects = new ArrayList<String>();
    }

    @Override
    public void resetPatternCounters() {
        validPatternCounter = 0;
        inValidPatternCounter = 0;
    }

    @Override
    public void addPatternBlock(ArrayList<String> block, long projectID) {
        for (int i = 0; i < block.size(); i++) {
            if (block.get(i).contains("is a proxy.")) {
                try {
                    this.proxy = this.getValidClassFullName(this.getClassName(
                            block.get(i)), projectID);
                } catch (SQLException ex) {
                    Logger.getLogger(Proxy.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (block.get(i).contains("is a proxy interface")) {
                try {
                    this.subject = this.getValidClassFullName(this.getClassName(
                            block.get(i)), projectID);
                } catch (SQLException ex) {
                    Logger.getLogger(Proxy.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (block.get(i).contains("The real object")) {
                if (this.proxy != null && this.subject != null) {
                    ArrayList<String> temp = this.getClassNamesFromLine(
                            block.get(i).substring(block.get(i).indexOf(":") + 1));
                    if (!temp.isEmpty()) {
                        for (int j = 0; j < temp.size(); j++) {
                            if (temp.get(j) != null) {
                                String currentSubject;
                                try {
                                    currentSubject = this.getValidClassFullName(temp.get(j), projectID);
                                    if (currentSubject != null) {
                                        this.realSubjects.add(currentSubject);
                                    }
                                } catch (SQLException ex) {
                                    Logger.getLogger(Proxy.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Checking pattern validity..
        if (this.proxy == null
                || this.subject == null
                || this.realSubjects.isEmpty()) {
            this.setValidity(false);
            inValidPatternCounter++;
            System.out.println("\t..Proxy pattern is invalid");

        } else {
            validPatternCounter++;
        }
    }

    @Override
    public void writePatternToXml(Writer writer) throws IOException {
        writer.write("\t\t<instance>\n");
        writer.write("\t\t\t" + this.getParticipantWithXmlFormat("Proxy", this.proxy));
        writer.write("\t\t\t" + this.getParticipantWithXmlFormat("Subject", this.subject));
        for (int i = 0; i < this.realSubjects.size(); i++) {
            writer.write("\t\t\t" + this.getParticipantWithXmlFormat("RealSubject",
                    this.realSubjects.get(i)));
        }
        writer.write("\t\t</instance>\n");
    }

    @Override
    public void writePatternToCsv(Writer writer, String projectName) throws IOException {
        writer.write(projectName + ";" + this.getPatternName());
        writer.write(";" + "Proxy" + ";" + this.proxy);
        writer.write(";" + "Subject" + ";" + this.subject);
        for (int i = 0; i < this.realSubjects.size(); i++) {
            writer.write(";" + "RealSubject" + ";" + this.realSubjects.get(i));
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
