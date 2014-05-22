/**
 * @author Nikolaos Tsantalis

 */

package gr.pattdetection.java.pattern.gui.progress;

import java.util.EventObject;

public class DetectionFinishedEvent extends EventObject {
    
    public DetectionFinishedEvent(PatternDetectionSource source) {
        super(source);
    }
}
