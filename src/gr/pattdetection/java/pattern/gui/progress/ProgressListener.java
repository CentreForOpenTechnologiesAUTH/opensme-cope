/**
 * @author Nikolaos Tsantalis

 */

package gr.pattdetection.java.pattern.gui.progress;

import java.util.EventListener;

public interface ProgressListener extends EventListener {

    public void detectionFinished(DetectionFinishedEvent event);
}
