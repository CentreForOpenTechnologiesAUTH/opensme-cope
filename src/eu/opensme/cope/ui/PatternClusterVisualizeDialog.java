package eu.opensme.cope.ui;

import eu.opensme.cope.domain.ReuseProject;
import eu.opensme.cope.factgenerators.lsa.LSA;
import eu.opensme.cope.factgenerators.tagcloud.TagCloud;
import eu.opensme.cope.recommenders.PatternRecommender;
import eu.opensme.cope.recommenders.entities.ClassCluster;
import eu.opensme.cope.recommenders.entities.ClassClusterPartcipant;
import eu.opensme.cope.util.FileSystemHandlerUtil;
import japa.parser.JavaParser;
import japa.parser.ast.Comment;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.stmt.Statement;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import org.mcavallo.opencloud.Cloud;
import org.mcavallo.opencloud.Tag;

/**
 * Presents the clusters of a reuse project in a visual, human friendly way. The dialog 
 * is divided in three main parts (1) the clusters list (2) the cluster participants' list
 * (3) and the information area.
 * 
 * @author Apostolos Kritikos <akritiko@csd.auth.gr>
 * @version v0.9 (25 July 2011)
 *
 */
public class PatternClusterVisualizeDialog extends javax.swing.JPanel {

    /** 
     * Ascending sorting by name identifier 
     */
    private final static String SORT_BY_NAME_ASCENDING = "nameasc";
    /**
     * Descending sorting by name identifier  
     */
    private final static String SORT_BY_NAME_DESCENDING = "namedesc";
    /**
     * Ascending sorting by specificity identifier 
     */
    private final static String SORT_BY_SPECIFICITY_ASCENDING = "specasc";
    /**
     * Descending sorting by specificity identifier 
     */
    private final static String SORT_BY_SPECIFICITY_DESCENDING = "specdesc";
    /**
     * The reuse project that is currently in use
     */
    private ReuseProject reuseProject;
    /**
     * An {@link ArrayList} that containts the root directory for each cluster available for the selected {@link ReuseProject}
     */
    private ArrayList<File> clusterDirectories;
    private ArrayList<ClassCluster> clusters;

    /**
     * A hashtable with lsa analysis, if performed, for each cluster
     */
    private Hashtable<String, Set<String>> lsaForClusters = new Hashtable<String, Set<String>>();
    /**
     * A hashtable with lsa analysis, if performed, for each class
     */
    private Hashtable<String, Set<String>> lsaForClasses = new Hashtable<String, Set<String>>();
    /**
     * A hashtable with lsa analysis, if performed, for each cluster
     */
    private Hashtable<String, List<Tag>> tagCloudForClusters = new Hashtable<String, List<Tag>>();    
    /**
     * A hashtable with tag cloud analysis, if performed, for each class
     */
    private Hashtable<String, List<Tag>> tagCloudForClasses = new Hashtable<String, List<Tag>>();    
    /**
     * A {@link JPopupMenu} that is being initiated when the user right clicks to the cluster members' jList
     */


    /**
     * A {@link JPopupMenu} that is being initiated when the user right clicks to the cluster members' jList
     */
    private JPopupMenu popupMenu;

    /** Creates new form ClusterVisualizeDialog */
    public PatternClusterVisualizeDialog(ReuseProject reuseProject) {
        this.reuseProject = reuseProject;
        initComponents();
        clusters = new ArrayList<ClassCluster>();
        populateClusters();
        visualizeClusterInformation();
    }

    private void populateClusters() {
        for (int i=0;i<this.reuseProject.getClusters().size();i++) {
            if (this.reuseProject.getClusters().get(i).isPatternBased())
                clusters.add(this.reuseProject.getClusters().get(i));
        }
    }
    
    /**
     * Provides the clusters available for the selected {@link ReuseProject} in the form of a JList items 
     */
    private void visualizeClusterInformation() {
        FileSystemHandlerUtil fsh = new FileSystemHandlerUtil();
        //the root path to check for the clusters
        String rootPath = reuseProject.getProjectLocation() + File.separator + "clusters" + File.separator + "patClusters";
        //get cluster directory names in File form
        clusterDirectories = (ArrayList<File>) fsh.getFirstLevelChildrenDirectories(rootPath);
        //sort collection in ascending order based on their names
        Collections.sort(clusterDirectories);

        DefaultListModel model = new DefaultListModel();
        jList1.setModel(model);
        for (int i=0;i<clusterDirectories.size();i++){
            for (int j=0;j<clusters.size();j++){
                if (clusterDirectories.get(i).getName().equals(clusters.get(j).getName())){
                    model.add(i, clusters.get(j).getName());
                    break;
                }
            }
        }

//        for (int i = 0; i < clusters.size(); i++) {
//            model.add(i, clusters.get(i).getName());
//        }    
    }

    /**
     * Handles the {@link JPopupMenu}. The menu contains two groups of items. (1) sorting options (2) the transfer to cluster submenu.
     * @param e 
     */
    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            popupMenu = new JPopupMenu();
            //Sorting classes submenu
            JMenu sortSubmenu = new JMenu("Sort classes: ");
            sortSubmenu.add("by Name (asc)").addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    ArrayList<ClassClusterPartcipant> cps = generateClusterMembers(jList1.getSelectedValue().toString());
                    ArrayList<ClassClusterPartcipant> sorted = sortBy(cps, SORT_BY_NAME_ASCENDING);
                    classesToJList(sorted);
                }
            });
            sortSubmenu.add("by Name (desc)").addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    ArrayList<ClassClusterPartcipant> cps = generateClusterMembers(jList1.getSelectedValue().toString());
                    ArrayList<ClassClusterPartcipant> sorted = sortBy(cps, SORT_BY_NAME_DESCENDING);
                    classesToJList(sorted);
                }
            });
            sortSubmenu.add("by Specifity (asc)").addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    ArrayList<ClassClusterPartcipant> cps = generateClusterMembers(jList1.getSelectedValue().toString());
                    ArrayList<ClassClusterPartcipant> sorted = sortBy(cps, SORT_BY_SPECIFICITY_ASCENDING);
                    classesToJList(sorted);
                }
            });
            sortSubmenu.add("by Specifity (desc)").addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    ArrayList<ClassClusterPartcipant> cps = generateClusterMembers(jList1.getSelectedValue().toString());
                    ArrayList<ClassClusterPartcipant> sorted = sortBy(cps, SORT_BY_SPECIFICITY_DESCENDING);
                    classesToJList(sorted);
                }
            });
            popupMenu.add(sortSubmenu);

            //Submenu separator
            popupMenu.addSeparator();

            //Submenu "move to cluster"
            JMenu submenu = new JMenu("Move to cluster: ");
            for (int i = 0; i < jList1.getModel().getSize(); i++) {
                if (!jList1.getModel().getElementAt(i).toString().equals(jList1.getSelectedValue())) {
                    submenu.add(new JMenuItem(jList1.getModel().getElementAt(i).toString())).addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            FileSystemHandlerUtil fsh = new FileSystemHandlerUtil();
                            PatternRecommender dr = new PatternRecommender(reuseProject);
                            //files to be transfered to a new cluster
                            ArrayList<File> nomadFiles = new ArrayList<File>();
                            //indices of the classes to be tranfered
                            int[] victimIndices = jList2.getSelectedIndices();
                            //find the names of the selected classes
                            String[] classNames = new String[victimIndices.length];
                            for (int i = 0; i < victimIndices.length; i++) {
                                String className = cleanFileNameFromSpecificity(jList2.getModel().getElementAt(victimIndices[i]).toString());
                                System.out.println(className);
                                classNames[i] = className;
                            }
                            //find the index of the cluster source
                            int sourceClusterIndex = jList1.getSelectedIndex();
                            //find the name of the cluster source
                            String sourceClustersName = jList1.getModel().getElementAt(sourceClusterIndex).toString();
                            //find the name of the cluster destination
                            String destinationClustersName = e.getActionCommand();
                            //reorganize cluster source (remove files and re-create cluster)
                            ArrayList<File> sourceClusterMembers = (ArrayList<File>) fsh.getJavaFilesWithinDirectory(clusterDirectories.get(sourceClusterIndex).getAbsolutePath());
                            ArrayList<File> sourceClusterMembersCounterPart = (ArrayList<File>) fsh.getJavaFilesWithinDirectory(clusterDirectories.get(sourceClusterIndex).getAbsolutePath());
                            
                            for (int i = 0; i < sourceClusterMembers.size(); i++) {
                                String currentClassName = cleanFileNameFromSpecificity(sourceClusterMembers.get(i).getName());
                                for (int j = 0; j < classNames.length; j++) {
                                    if (currentClassName.equals(classNames[j])) {
                                        nomadFiles.add(sourceClusterMembers.get(i));
                                        sourceClusterMembersCounterPart.remove(sourceClusterMembers.get(i));
                                    }
                                }
                            }
                            
                            sourceClusterMembers = sourceClusterMembersCounterPart;
                            
                            dr.alterClusterAfterUsersIntervension(sourceClusterMembers, sourceClustersName);
                            //reorganize cluster destination (remove files and re-create cluster)
                            ArrayList<File> alteredDestinationClusterMembers = (ArrayList<File>) fsh.getJavaFilesWithinDirectory(reuseProject.getProjectLocation() + File.separator + "clusters" + File.separator + "depClusters" + File.separator + destinationClustersName);
                            for (int i = 0; i < nomadFiles.size(); i++) {
                                alteredDestinationClusterMembers.add(nomadFiles.get(i));
                            }
                            dr.alterClusterAfterUsersIntervension(alteredDestinationClusterMembers, destinationClustersName);
                            //construct success message and present in the form of alert message box
                            String message = "Classes ";
                            for (int i = 0; i < classNames.length; i++) {
                                message += classNames[i] + ", ";
                            }
                            message += "successfully tranfered to " + destinationClustersName + " cluster";
                            JOptionPane.showMessageDialog(popupMenu.getParent(), message, "Success", JOptionPane.PLAIN_MESSAGE);
                        }
                    });
                }
            }
            popupMenu.add(submenu);
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    /**
     * Takes the name of the {@link JLit} item as a parameter and removes the (specificity) part 
     * (i.e "MyClass.java (0,2)" becomes "MyClass.java")
     * 
     * @param nameWithSpecificity
     * @return 
     */
    private String cleanFileNameFromSpecificity(String nameWithSpecificity) {
        String[] parts = nameWithSpecificity.split(" ");
        return parts[0].trim();
    }

    /**
     * Provides the classes that selected cluster contains in the form of a JList items 
     * @param clusterClasses 
     */
    private void classesToJList(ArrayList<ClassClusterPartcipant> clusterClasses) {
        DefaultListModel filesModel = new DefaultListModel();
        jList2.setModel(filesModel);
        //this contains the decimal digits to 2
        DecimalFormat df = new DecimalFormat("#.##");
        for (int i = 0; i < clusterClasses.size(); i++) {
            //TODO: need to have a thorough look to java class filename scheme (with .java || w/out .java) and unify everything
            filesModel.add(i, clusterClasses.get(i).getClassAnalysis().toFileName() + ".java (" + df.format(clusterClasses.get(i).getSpecificity()) + ")");
        }
    }

    /**
     * Generates the cluster memebers of a cluster based on the reuse project and 
     * the cluster directories objects.
     * 
     * @return {@link ArrayList<ClassClusterParticipant>}
     */
    private ArrayList<ClassClusterPartcipant> generateClusterMembers() {
        //TODO: probably needs refactoring
        FileSystemHandlerUtil fsh = new FileSystemHandlerUtil();
        ArrayList<File> clusterContent = (ArrayList<File>) fsh.getJavaFilesWithinDirectory(clusterDirectories.get(jList1.getSelectedIndex()).getAbsolutePath());
        PatternRecommender dr = new PatternRecommender(reuseProject);
        ArrayList<ClassClusterPartcipant> cps = dr.classNamesToClassParticipants(clusterContent);
        return cps;
    }
        
    /**
     * Generates the cluster memebers of a cluster based on the reuse project and 
     * the cluster directories objects.
     * 
     * @return {@link ArrayList<ClassClusterParticipant>}
     */
    private ArrayList<ClassClusterPartcipant> generateClusterMembers(String key) {
        //TODO: probably needs refactoring
        ArrayList<ClassClusterPartcipant> cps = new ArrayList<ClassClusterPartcipant>();
        for (int i=0;i<clusters.size();i++) {
            if (clusters.get(i).getName().equals(key)) {
                cps=clusters.get(i).getClusterParticipants();
            }
        }
        return cps;
    }

    /**
     * Sorts an {@link ArrayList} with {@link ClassClusterPartcipant} objects based on the mode parameter
     * 
     * @param cps
     * @param mode
     * @return {@link ArrayList<ClassClusterPartcipant>}
     */
    private ArrayList<ClassClusterPartcipant> sortBy(ArrayList<ClassClusterPartcipant> cps, String mode) {
        if (mode.equals(SORT_BY_NAME_ASCENDING)) {
            Collections.sort(cps);
        }
        if (mode.equals(SORT_BY_NAME_DESCENDING)) {
            Collections.sort(cps);
            Collections.reverse(cps);
        }
        if (mode.equals(SORT_BY_SPECIFICITY_ASCENDING)) {
            Collections.sort(cps, ClassClusterPartcipant.SpecificityComparator);
        }
        if (mode.equals(SORT_BY_SPECIFICITY_DESCENDING)) {
            Collections.sort(cps, ClassClusterPartcipant.SpecificityComparator);
            Collections.reverse(cps);
        }
        return cps;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jMenuBar2 = new javax.swing.JMenuBar();
        jMenu3 = new javax.swing.JMenu();
        jMenu4 = new javax.swing.JMenu();
        classComboBox = new javax.swing.JComboBox();
        jPanelClusterInfo = new javax.swing.JPanel();
        jPanelClassInfo = new javax.swing.JPanel();
        jScrollPaneClass = new javax.swing.JScrollPane();
        jTableClass = new javax.swing.JTable();
        clusterComboBox = new javax.swing.JComboBox();
        jScrollPaneCluster = new javax.swing.JScrollPane();
        jTableCluster = new javax.swing.JTable();
        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList();
        jSplitPane3 = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane4 = new javax.swing.JScrollPane();
        jPanelUML = new javax.swing.JPanel();

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        jMenu3.setText("File");
        jMenuBar2.add(jMenu3);

        jMenu4.setText("Edit");
        jMenuBar2.add(jMenu4);

        classComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Latent Semantic Analysis", "Tag Cloud" }));
        classComboBox.setSelectedIndex(-1);
        classComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                classComboBoxItemStateChanged(evt);
            }
        });

        jPanelClusterInfo.setLayout(new java.awt.BorderLayout());

        jPanelClassInfo.setLayout(new java.awt.BorderLayout());

        jTableClass.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPaneClass.setViewportView(jTableClass);

        clusterComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Latent Semantic Analysis", "Tag Cloud" }));
        clusterComboBox.setSelectedIndex(-1);
        clusterComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                clusterComboBoxItemStateChanged(evt);
            }
        });

        jTableCluster.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPaneCluster.setViewportView(jTableCluster);

        setMinimumSize(new java.awt.Dimension(600, 300));
        setPreferredSize(new java.awt.Dimension(800, 500));
        setLayout(new java.awt.BorderLayout());

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jList1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        jSplitPane2.setLeftComponent(jScrollPane1);

        jList2.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList2.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jList2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jList2MousePressed(evt);
            }
        });

        jScrollPane3.setViewportView(jList2);

        jSplitPane2.setRightComponent(jScrollPane3);

        jSplitPane1.setLeftComponent(jSplitPane2);

        jSplitPane3.setDividerLocation(130);
        jSplitPane3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanelClusterInfo.add(clusterComboBox, BorderLayout.PAGE_START);
        jTabbedPane1.add("Cluster Info Viewer", jPanelClusterInfo);
        jPanelClassInfo.add(classComboBox,BorderLayout.PAGE_START);
        jTabbedPane1.add("Class Info Viewer", jPanelClassInfo);
        jTabbedPane1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTabbedPane1MousePressed(evt);
            }
        });

        jTabbedPane1.repaint();
        jScrollPane2.setViewportView(jTabbedPane1);

        jSplitPane3.setRightComponent(jScrollPane2);

        jScrollPane4.setViewportView(jPanelUML);

        jSplitPane3.setLeftComponent(jScrollPane4);

        jSplitPane1.setRightComponent(jSplitPane3);

	add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseClicked
        ArrayList<ClassClusterPartcipant> cps = generateClusterMembers();
        ArrayList<ClassClusterPartcipant> sortedCps = sortBy(cps, SORT_BY_NAME_ASCENDING);
        classesToJList(sortedCps);
        //present graph for cluster
        String UMLGraphPath = clusterDirectories.get(jList1.getSelectedIndex()).getAbsolutePath() + File.separator + "graph.png";
        jPanelUML.removeAll();
        jPanelUML.repaint();
        jPanelUML.validate();
        try {
            jTabbedPane1.setSelectedIndex(0);
            resetInfoTabs();
            BufferedImage myPicture = ImageIO.read(new File(UMLGraphPath));
            JLabel picLabel = new JLabel(new ImageIcon(myPicture));
            jPanelUML.add(picLabel);
            jPanelUML.repaint();
            jPanelUML.validate();
            this.jScrollPane4.validate();
        } catch (IOException ex) {
            //in case the graph is not available, present appropriate alert message to user
            JOptionPane.showMessageDialog(this, "Graphs not available for this project", "Error displaying UML Graphs", JOptionPane.ERROR_MESSAGE);
        }
	jTabbedPane1.repaint();
    }//GEN-LAST:event_jList1MouseClicked

    private ClassClusterPartcipant generateClusterMember() {
        //TODO: probably needs refactoring
        FileSystemHandlerUtil fsh = new FileSystemHandlerUtil();
        ArrayList<File> clusterContent = (ArrayList<File>) fsh.getJavaFilesWithinDirectory(clusterDirectories.get(jList1.getSelectedIndex()).getAbsolutePath());
        PatternRecommender pr = new PatternRecommender(reuseProject);
        ArrayList<ClassClusterPartcipant> cps = pr.classNamesToClassParticipants(clusterContent);
        String jListClass = this.cleanFileNameFromSpecificity(jList2.getSelectedValue().toString());
        for (int i = 0; i < cps.size(); i++) {
            if (cps.get(i).getClassAnalysis().getName().substring(cps.get(i).getClassAnalysis().getName().lastIndexOf(".") + 1, cps.get(i).getClassAnalysis().getName().length()).equals(jListClass.substring(0, jListClass.lastIndexOf(".")))) {
                return cps.get(i);
            }
        }
        return null;
    }    
    
    private void jList2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList2MousePressed
	this.resetInfoTabs();
        maybeShowPopup(evt);
    }//GEN-LAST:event_jList2MousePressed

    private void classTagCloud() {
        if (jTabbedPane1.getSelectedIndex() == 1) {
            if (jList2.getSelectedIndex() < 0) {
                return;
            }
            ClassClusterPartcipant clas = generateClusterMember();
            if (this.tagCloudForClasses.containsKey(clas.getClassAnalysis().getName())) {
                this.visualizeClassTagCloud(tagCloudForClasses.get(clas.getClassAnalysis().getName()));
                return;
            }
            
            try {
                String rootPath = reuseProject.getProjectLocation() + File.separator + "clusters" + File.separator + "patClusters";
                String document = rootPath + File.separator + jList1.getSelectedValue().toString() + "/src/"
                        + clas.getClassAnalysis().getName().replace(".", "/") + ".java";

                String str = this.getClassValuesForLSA(document, clas.getClassAnalysis().getName().substring(clas.getClassAnalysis().getName().lastIndexOf(".") + 1, clas.getClassAnalysis().getName().length()));
                TagCloud tagCloud = new TagCloud();
                tagCloud.processDocument(str);
                Cloud cloud = tagCloud.getCloud();
                List<Tag> tags = cloud.tags();
                this.tagCloudForClasses.put(clas.getClassAnalysis().getName(), tags);
                this.visualizeClassTagCloud(tags);
          //  } catch (IOException ee) {
           //     ee.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }        
    }

    private void clusterTagCloud() {
        if (jTabbedPane1.getSelectedIndex() == 0) {
            if (jList1.getSelectedIndex() < 0) {
                return;
            }

            if (tagCloudForClusters.containsKey(clusterDirectories.get(jList1.getSelectedIndex()).getName())) {
                visualizeClusterTagCloud(tagCloudForClusters.get(clusterDirectories.get(jList1.getSelectedIndex()).getName()));
                return;
            }
            try {
                ArrayList<ClassClusterPartcipant> cps = generateClusterMembers();
                TagCloud tagCloud = new TagCloud();
                for (int i = 0; i < cps.size(); i++) {
                    String rootPath = reuseProject.getProjectLocation() + File.separator + "clusters" + File.separator + "patClusters";
                    String document = rootPath + File.separator + jList1.getSelectedValue().toString() + "/src/"
                            + cps.get(i).getClassAnalysis().getName().replace(".", "/") + ".java";
                    String text = getClassValuesForLSA(document, cps.get(i).getClassAnalysis().getName());
                    tagCloud.processDocument(text);
                }
                List<Tag> tags = tagCloud.getCloud().tags();
                tagCloudForClusters.put(clusterDirectories.get(jList1.getSelectedIndex()).getName(), tags);
                visualizeClusterTagCloud(tags);
            } catch (Exception ex) {
                ex.printStackTrace();
            }            
        }
    }

    private void visualizeClusterTagCloud(List<Tag> tags) {
        String[] columnNames = {"Tags","Weight"};
        Object[][] data = new Object[tags.size()][2];
        Iterator<Tag> tagIt = tags.iterator();
        int counter = 0;
        while (tagIt.hasNext()) {
            Tag temp = tagIt.next();
            data[counter][0] = temp.getName();
            data[counter++][1] = temp.getWeight();
        }
        Arrays.sort(data, new java.util.Comparator<Object[]>() {
            public int compare(Object[] a, Object[] b) {
                int t=0;
                if (Double.valueOf(b[1].toString()) > Double.valueOf(a[1].toString()))
                    t=1;
                else
                    t=-1;
                return t;
            }
        });

        jPanelClusterInfo.remove(jScrollPaneCluster);
        jTableCluster = new JTable(data, columnNames);
        jScrollPaneCluster = new JScrollPane(jTableCluster);
      //  jTableCluster.setFillsViewportHeight(true);

        jScrollPaneCluster.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        jPanelClusterInfo.add(jScrollPaneCluster,BorderLayout.CENTER);
        jTabbedPane1.setTitleAt(0, "Info Viewer - Tag Cloud for " + jList1.getSelectedValue().toString());
        jTabbedPane1.repaint();
    }

    private void visualizeClassTagCloud(List<Tag> tags) {
        String[] columnNames = {"Tags","Weight"};
        Object[][] data = new Object[tags.size()][2];
        Iterator<Tag> tagIt = tags.iterator();
        int counter = 0;
        while (tagIt.hasNext()) {
            Tag temp = tagIt.next();
            data[counter][0] = temp.getName();
            data[counter++][1] = temp.getWeight();
        }
        Arrays.sort(data, new java.util.Comparator<Object[]>() {
            public int compare(Object[] a, Object[] b) {
                int t=0;
                if (Double.valueOf(b[1].toString()) > Double.valueOf(a[1].toString()))
                    t=1;
                else
                    t=-1;
                return t;
            }
        });

        jPanelClassInfo.remove(jScrollPaneClass);
        jTableClass = new JTable(data, columnNames);
        jScrollPaneClass = new JScrollPane(jTableClass);
     //   jTableClass.setFillsViewportHeight(true);

        jScrollPaneClass.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        jPanelClassInfo.add(jScrollPaneClass,BorderLayout.CENTER);
        jTabbedPane1.setTitleAt(1, "Info Viewer - Tag Cloud for " + jList2.getSelectedValue().toString());
        jTabbedPane1.repaint();
    }


    private void visualizeClassLSA(Set<String> words) {
        String[] columnNames = {"Words"};
        Object[][] data = new Object[words.size()][1];
        Iterator<String> wordIt = words.iterator();
        int counter = 0;
        while (wordIt.hasNext()) {
            data[counter++][0] = wordIt.next();
        }
        Arrays.sort(data, new java.util.Comparator<Object[]>() {
            public int compare(Object[] a, Object[] b) {
                return b[0].toString().compareTo(a[0].toString())*-1;
            }
        });
        jPanelClassInfo.remove(jScrollPaneClass);
        jTableClass = new JTable(data, columnNames);
        jScrollPaneClass = new JScrollPane(jTableClass);
       // jTableClass.setFillsViewportHeight(true);
        jScrollPaneClass.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        jPanelClassInfo.add(jScrollPaneClass,BorderLayout.CENTER);
        jTabbedPane1.setTitleAt(1, "Info Viewer - LSA for " + jList2.getSelectedValue().toString());
        jTabbedPane1.repaint();
    }

    private void resetInfoTabs() {
        //jPanelClusterInfo.removeAll();
        //jPanelClusterInfo.add(clusterComboBox);
        jTabbedPane1.setSelectedIndex(0);
        clusterComboBox.setSelectedIndex(-1);
        jPanelClusterInfo.remove(jScrollPaneCluster);
        jTabbedPane1.setTitleAt(0, "Cluster Info Viewer");
        jPanelClusterInfo.repaint();
        //Tab 3
        classComboBox.setSelectedIndex(-1);
        jTabbedPane1.setTitleAt(1, "Class Info Viewer");
        jPanelClassInfo.remove(jScrollPaneClass);
        jPanelClassInfo.repaint();
        jTabbedPane1.setSize(jTabbedPane1.getComponentAt(0).getSize());
        jTabbedPane1.repaint();
    }

    private void visualizeClusterLSA(Set<String> words) {
        String[] columnNames = {"Words"};
        Object[][] data = new Object[words.size()][1];
        Iterator<String> wordIt = words.iterator();
        int counter = 0;
        while (wordIt.hasNext()) {
            data[counter++][0] = wordIt.next();
        }
        Arrays.sort(data, new java.util.Comparator<Object[]>() {
            public int compare(Object[] a, Object[] b) {
                return b[0].toString().compareTo(a[0].toString())*-1;
            }
        });        
        jPanelClusterInfo.remove(jScrollPaneCluster);
        jTableCluster = new JTable(data, columnNames);
        jScrollPaneCluster = new JScrollPane(jTableCluster);
        //jTableCluster.setFillsViewportHeight(true);
        jScrollPaneCluster.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        jPanelClusterInfo.add(jScrollPaneCluster,BorderLayout.CENTER);
        jTabbedPane1.setTitleAt(0, "Info Viewer - LSA for " + jList1.getSelectedValue().toString());
        jTabbedPane1.repaint();
    }

    private void clusterLSA() {
        if (jTabbedPane1.getSelectedIndex() == 0) {
            if (jList1.getSelectedIndex() < 0) {
                return;
            }

            if (lsaForClusters.containsKey(clusterDirectories.get(jList1.getSelectedIndex()).getName())) {
                visualizeClusterLSA(lsaForClusters.get(clusterDirectories.get(jList1.getSelectedIndex()).getName()));
                return;
            }
            Set<String> words = null;
            try {
                ArrayList<ClassClusterPartcipant> cps = generateClusterMembers();
                LSA lsa = new LSA();
                for (int i = 0; i < cps.size(); i++) {
                    String rootPath = reuseProject.getProjectLocation() + File.separator + "clusters" + File.separator + "patClusters";
                    String document = rootPath + File.separator + jList1.getSelectedValue().toString() + "/src/"
                            + cps.get(i).getClassAnalysis().getName().replace(".", "/") + ".java";
                    String text = getClassValuesForLSA(document, cps.get(i).getClassAnalysis().getName());
                    lsa.processDocument(text);
                }
                Properties props = System.getProperties();
                lsa.processSpace(props);
                words = lsa.getWords();
                lsaForClusters.put(clusterDirectories.get(jList1.getSelectedIndex()).getName(), words);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (words == null) {
                JOptionPane.showMessageDialog(this, "LSA could not be performed", "Info Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            visualizeClusterLSA(words);
        }
    }

    private void classLSA() {
        if (jTabbedPane1.getSelectedIndex() == 1) {
            if (jList2.getSelectedIndex() < 0) {
                return;
            }
            ClassClusterPartcipant clas = generateClusterMember();
            if (this.lsaForClasses.containsKey(clas.getClassAnalysis().getName())) {
                this.visualizeClassLSA(lsaForClasses.get(clas.getClassAnalysis().getName()));
                return;
            }
            try {
                String rootPath = reuseProject.getProjectLocation() + File.separator + "clusters" + File.separator + "patClusters";
                String document = rootPath + File.separator + jList1.getSelectedValue().toString() + "/src/"
                        + clas.getClassAnalysis().getName().replace(".", "/") + ".java";

                String str = this.getClassValuesForLSA(document, clas.getClassAnalysis().getName().substring(clas.getClassAnalysis().getName().lastIndexOf(".") + 1, clas.getClassAnalysis().getName().length()));
                LSA lsa = new LSA();
                lsa.processDocument(str);
                Properties props = System.getProperties();
                lsa.processSpace(props);
                Set<String> words = lsa.getWords();
                lsaForClasses.put(clas.getClassAnalysis().getName(), words);
                visualizeClassLSA(words);
            } catch (IOException ee) {
                ee.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getClassValuesForLSA(String document, String className) {
        String str = "";
        try {
            FileInputStream in = new FileInputStream(document);

            CompilationUnit cu;
            try {
                // parse the file
                cu = JavaParser.parse(in);
            } finally {
                in.close();
            }
            //cu.
            //Add class name
            str += className + " ";

            //Add method names & return values & fields
            List<TypeDeclaration> types = cu.getTypes();
            for (TypeDeclaration type : types) {
                List<BodyDeclaration> members = type.getMembers();
                for (BodyDeclaration member : members) {
                    if (member instanceof MethodDeclaration) {
                        MethodDeclaration method = (MethodDeclaration) member;
                        str += method.getName() + " ";
                        try {
                            method.getBody().getStmts();
                        } catch (Exception e1) {
                            System.out.println(e1.getMessage());
                            continue;
                        }
                        Iterator<Statement> it = method.getBody().getStmts().iterator();
                        while (it.hasNext()) {
                            Statement next = it.next();
                            String nextstr = next.toString();
                            if (nextstr.startsWith("return")) {
                                str += nextstr.substring(nextstr.indexOf(" "), nextstr.length() - 1) + " ";
                            }
                        }
                    } else if (member instanceof FieldDeclaration) {
                        FieldDeclaration field = (FieldDeclaration) member;
                        Iterator<VariableDeclarator> vars = field.getVariables().iterator();
                        while (vars.hasNext()) {
                            str += vars.next().getId().getName() + " ";
                        }
                    }
                }
            }

            //Add comments
            Iterator<Comment> comments = cu.getComments().iterator();
            while (comments.hasNext()) {
                String nextCom = comments.next().getContent();
                str += nextCom + " ";
            }
        } catch (IOException ee) {
            //ee.printStackTrace();
        } catch (Exception e) {
            //e.printStackTrace();
        }

        return str;
    }

    private void jTabbedPane1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTabbedPane1MousePressed
        // TODO add your handling code here:
jTabbedPane1.repaint();
    }//GEN-LAST:event_jTabbedPane1MousePressed

private void clusterComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clusterComboBoxItemStateChanged
// TODO add your handling code here:
    //Check if selected cluster
    if (jList1.getSelectedIndex() < 0) {
        return;
    }

    if (clusterComboBox.getSelectedIndex() == 0) {
        clusterLSA();
    } else if (clusterComboBox.getSelectedIndex() == 1) {
        clusterTagCloud();
    }
}//GEN-LAST:event_clusterComboBoxItemStateChanged

private void classComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_classComboBoxItemStateChanged
// TODO add your handling code here:
    if (jList2.getSelectedIndex() < 0) {
        return;
    }
    if (classComboBox.getSelectedIndex() == 0) {
        classLSA();
    } else if (classComboBox.getSelectedIndex() == 1) {
        classTagCloud();
    }
}//GEN-LAST:event_classComboBoxItemStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox classComboBox;
    private javax.swing.JComboBox clusterComboBox;
    private javax.swing.JList jList1;
    private javax.swing.JList jList2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JPanel jPanelClassInfo;
    private javax.swing.JPanel jPanelClusterInfo;
    private javax.swing.JPanel jPanelUML;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPaneClass;
    private javax.swing.JScrollPane jScrollPaneCluster;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableClass;
    private javax.swing.JTable jTableCluster;
    // End of variables declaration//GEN-END:variables
}

