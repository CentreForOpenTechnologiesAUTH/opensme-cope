/**
 * @author Nikolaos Tsantalis

 */

package gr.bytecodereader.java.bytecode;

public enum Access {
    NONE, PUBLIC, PRIVATE, PROTECTED;

    public String toString() {
        switch(this) {
            case NONE: return "";
            case PUBLIC: return "public";
            case PRIVATE: return "private";
            case PROTECTED: return "protected";
            default: return "";
        }
    }
}
