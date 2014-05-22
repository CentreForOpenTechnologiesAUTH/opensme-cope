/**
 * @author Nikolaos Tsantalis

 */

package gr.pattdetection.java.pattern.gui.progress;

import gr.pattdetection.java.pattern.TsantalisPatternInstance;

import java.util.Vector;

public class PatternDetectionSource {

    private String patternName;
    private Vector<TsantalisPatternInstance> patternInstanceVector;

    public PatternDetectionSource(String patternName, Vector<TsantalisPatternInstance> patternInstanceVector) {
        this.patternName = patternName;
        this.patternInstanceVector = patternInstanceVector;
    }

    public String getPatternName() {
        return patternName;
    }

    public Vector<TsantalisPatternInstance> getPatternInstanceVector() {
        return patternInstanceVector;
    }

}
