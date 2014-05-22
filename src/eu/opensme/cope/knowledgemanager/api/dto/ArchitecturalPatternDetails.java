package eu.opensme.cope.knowledgemanager.api.dto;

import java.util.ArrayList;

public class ArchitecturalPatternDetails {
    String patternName;
    ArrayList<KeyValue> highQuality;
    ArrayList<KeyValue> mediumQuality;
    ArrayList<KeyValue> lowQuality;
    ArrayList<KeyValue> roles;

    public ArchitecturalPatternDetails() {
    }

    public ArchitecturalPatternDetails(ArrayList<KeyValue> highQuality, ArrayList<KeyValue> mediumQuality, ArrayList<KeyValue> lowQuality, ArrayList<KeyValue> roles) {
        this.highQuality = highQuality;
        this.mediumQuality = mediumQuality;
        this.lowQuality = lowQuality;
        this.roles = roles;
    }

    public String getPatternName() {
        return patternName;
    }

    public void setPatternName(String patternName) {
        this.patternName = patternName;
    }

    public ArrayList<KeyValue> getHighQuality() {
        return highQuality;
    }

    public void setHighQuality(ArrayList<KeyValue> highQuality) {
        this.highQuality = highQuality;
    }

    
    public ArrayList<KeyValue> getLowQuality() {
        return lowQuality;
    }

    public void setLowQuality(ArrayList<KeyValue> lowQuality) {
        this.lowQuality = lowQuality;
    }

    public ArrayList<KeyValue> getMediumQuality() {
        return mediumQuality;
    }

    public void setMediumQuality(ArrayList<KeyValue> mediumQuality) {
        this.mediumQuality = mediumQuality;
    }

    public ArrayList<KeyValue> getRoles() {
        return roles;
    }

    public void setRoles(ArrayList<KeyValue> roles) {
        this.roles = roles;
    }
    
    
    
}
