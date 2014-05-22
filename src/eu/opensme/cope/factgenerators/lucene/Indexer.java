/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.factgenerators.lucene;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import eu.opensme.cope.factgenerators.util.Properties;
import eu.opensme.cope.factgenerators.util.SourceFileInfo;
import japa.parser.JavaParser;
import japa.parser.ast.Comment;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author econst
 */
public class Indexer {

    private String indexDirectory;
    private String dataDirectory;
    private IndexWriter writer;
    private Directory toIndexDirectory;
    private Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
    /** The field of the index that maps a class to a project (via database id) */
    private String FIELD_CLASS_FULL_NAME = "class_full_name";
    private String FIELD_CLASS_NAME = "name";
    /** The field of the index for Full Text based queries */
    private String FIELD_FULL_TEXT = "full_text";
    /** The field of the index for Class Javadoc based queries */
    private String FIELD_ATTRIBUTE_NAME = "attribute";
    /** The field of the index for Method Javadoc based queries */
    private String FIELD_METHOD_NAME = "method";
    /** The field of the index for the rest Comments */
    private String FIELD_COMMENT = "comment";

    public Indexer(String projectDirectory,String toIndexDir) {
        indexDirectory = toIndexDir+"/index";
        dataDirectory = projectDirectory;
    }

    public void initializeWriter(boolean create) {

        try {
            toIndexDirectory = FSDirectory.open(new File(indexDirectory));
            writer = new IndexWriter(toIndexDirectory, analyzer, create,
                    IndexWriter.MaxFieldLength.UNLIMITED);
        } catch (IOException e) {
            System.err.println("The index has not been created!");
        }
    }
    
    public void finalize(){
        if (writer==null)
            return;
        
        try {
            writer.optimize();
            writer.close();
        } catch (CorruptIndexException ex) {
            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void startIndexing(){
        File t = new File(dataDirectory);
        startIndexing(t);
    }
    
    private void startIndexing(File temp){
        if (temp.isDirectory()){
            File[] files = temp.listFiles();
            for (int i=0;i<files.length;i++)
                startIndexing(files[i]);
        }else{
            if (temp.getAbsolutePath().endsWith(".java"))
                addToIndexFile(temp.getAbsolutePath());
        }
    }

    private void addToIndexFile(String doc){
        try{
            SourceFileInfo sfi = getFileInfo(doc);
            if (sfi==null)
                return;
            Document document = new Document();
            document.add(new Field(FIELD_CLASS_FULL_NAME,sfi.getFullName(),Field.Store.YES,Field.Index.NO));
            Vector<String> className = sfi.getClassName();
            for (int i=0;i<className.size();i++)
                document.add(new Field(FIELD_CLASS_NAME, className.elementAt(i), Field.Store.YES, Field.Index.ANALYZED));
            document.add(new Field(FIELD_FULL_TEXT, sfi.getFullText(), Field.Store.YES, Field.Index.ANALYZED));        
            Vector<String> attributes = sfi.getAttributeNames();
            for (int i=0;i<attributes.size();i++)
                document.add(new Field(FIELD_ATTRIBUTE_NAME, attributes.elementAt(i), Field.Store.YES, Field.Index.ANALYZED));        
            Vector<String> methods = sfi.getMethodNames();
            for (int i=0;i<methods.size();i++)
                document.add(new Field(FIELD_METHOD_NAME, methods.elementAt(i), Field.Store.YES, Field.Index.ANALYZED));        
            Vector<String> comments = sfi.getComments();
            for (int i=0;i<comments.size();i++)
                document.add(new Field(FIELD_COMMENT,comments.elementAt(i),Field.Store.YES,Field.Index.ANALYZED));

            writer.addDocument(document);
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
    }

    public SourceFileInfo getFileInfo(String document) {
        SourceFileInfo sfi = null;
        
        try {
            
            FileInputStream in = new FileInputStream(document);

            CompilationUnit cu = null;
            try {
                // parse the file
                cu = JavaParser.parse(in);
            } catch(Exception ee){
                ee.printStackTrace();
            }finally {
                in.close();
            }

            sfi = new SourceFileInfo();
            String full = document.replace(dataDirectory, "");
            full = full.substring(1, full.lastIndexOf("."));
            full = full.replaceAll("/", ".");
            sfi.setFullClassName(full);
            //Add method names & return values & fields
            List<TypeDeclaration> types = cu.getTypes();
            if (types==null)
                return null;
            for (TypeDeclaration type : types) {
                List<BodyDeclaration> members = type.getMembers();
                String init = type.getName();
                Vector<String> names = this.getNameSplits(init);
                for (int i=0;i<names.size();i++)
                    sfi.addToClassName(names.elementAt(i));
                if (members==null)
                    continue;
                for (BodyDeclaration member : members) {
                    if (member instanceof MethodDeclaration) {
                        MethodDeclaration method = (MethodDeclaration) member;
                        Vector<String> meth = this.getNameSplits(method.getName());
                        for (int i=0;i<meth.size();i++)
                            sfi.addMethod(meth.elementAt(i));
                    } else if (member instanceof FieldDeclaration) {
                        FieldDeclaration field = (FieldDeclaration) member;
                        if (field.getVariables()==null)
                            continue;
                        Iterator<VariableDeclarator> vars = field.getVariables().iterator();
                        while (vars.hasNext()) {
                            VariableDeclarator next = vars.next();
                            if (next==null)
                                continue;
                            Vector<String> f = this.getNameSplits(next.getId().getName());
                            for (int i = 0; i < f.size(); i++) {
                                sfi.addField(f.elementAt(i));
                            }
                        }
                    }
                }
            }

            //Add comments
            if(cu.getComments() == null)
                return sfi;
            Iterator<Comment> comments = cu.getComments().iterator();
            while (comments.hasNext()) {
                Comment com = comments.next();
                if (com == null){
                    continue;
                }
                String nextCom = com.getContent();
                Pattern notWhiteSpace = Pattern.compile("\\S+");
                Matcher matcher = notWhiteSpace.matcher(nextCom);
                while (matcher.find()) {
                    String next = nextCom.substring(matcher.start(), matcher.end());
                    Vector<String> coms = this.getNameSplits(next);
                    for (int i = 0; i < coms.size(); i++) {
                        sfi.addComment(coms.elementAt(i));
                    }
                }
            }
        } catch (IOException ee) {
            ee.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sfi;
    }
    
    private Vector<String> getNameSplits(String input){
        Vector<String> nextWords = new Vector<String>(0);
        if (input==null)
            return nextWords;
        String[] temp = input.split("(?=[A-Z])");
        String toInsert = "";
        for (int i = 0; i < temp.length; i++) {
            if (temp[i].length()==1 &&
                    Character.isUpperCase(temp[i].charAt(0))){
                toInsert+=temp[i];
                if (i==temp.length-1 &&
                        toInsert.length()>1){
                    nextWords.addElement(toInsert);
                }
                continue;
            }
            
            for (int j = 0; j < temp[i].length(); j++) {
                if (temp[i].charAt(j) < 65
                        || (temp[i].charAt(j) > 90 && temp[i].charAt(j) < 97)
                        || temp[i].charAt(j) > 122) {
                    if (toInsert.length()>1)
                        nextWords.addElement(toInsert);
                    toInsert="";
                    continue;
                }else{
                    toInsert+=temp[i].charAt(j);
                }
            }
            
            if (toInsert.length()>1){
                nextWords.addElement(toInsert);
                toInsert ="";
            }
        }
        return nextWords;
    }
}
