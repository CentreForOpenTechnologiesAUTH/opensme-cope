/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author UserXP
 *
 */
public class CommandLineUtil {

    private String command;

    public CommandLineUtil() {
    }

    public CommandLineUtil(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void executeInConsole() {
        try {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(this.command);

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            String line = null;

            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }

            int exitVal = pr.waitFor();
            System.out.println("Exited with error code " + exitVal);

        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
}
