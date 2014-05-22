package eu.opensme.cope.knowledgemanager.gui.management;

import eu.opensme.cope.domain.ReuseProject;
import eu.opensme.cope.knowledgemanager.api.CompareInterfaceException;
import eu.opensme.cope.knowledgemanager.api.InvalidTierException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import eu.opensme.cope.knowledgemanager.api.ReuseApi;
import eu.opensme.cope.knowledgemanager.api.actions.ArchitecturalPattern;
import eu.opensme.cope.knowledgemanager.api.actions.BusinessComponent;
import eu.opensme.cope.knowledgemanager.api.actions.Language;
import eu.opensme.cope.knowledgemanager.api.actions.OpenSMEComponent;
import eu.opensme.cope.knowledgemanager.api.actions.OpenSMEInterface;
import eu.opensme.cope.knowledgemanager.api.actions.QualityAttribute;
import eu.opensme.cope.knowledgemanager.api.actions.Role;
import eu.opensme.cope.knowledgemanager.api.actions.Technology;
import eu.opensme.cope.knowledgemanager.api.dto.ArchitecturalPatternDetails;
import eu.opensme.cope.knowledgemanager.api.dto.BusinessComponentDetails;
import eu.opensme.cope.knowledgemanager.api.dto.ComponentDetails;
import eu.opensme.cope.knowledgemanager.api.dto.ComponentTreeInfo;
import eu.opensme.cope.knowledgemanager.api.dto.KeyValue;
import eu.opensme.cope.knowledgemanager.copesync.CopeSynchronizer;
import eu.opensme.cope.knowledgemanager.gui.classification.Classification;
import eu.opensme.cope.knowledgemanager.gui.classification.tree.TreeMetaModelNodeData;
import eu.opensme.cope.knowledgemanager.utils.ProgressBarFrame;
import eu.opensme.cope.knowledgemanager.gui.management.list.SortedListKeyValueModel;
import eu.opensme.cope.knowledgemanager.gui.management.list.SortedListKeyValuePatternModel;
import eu.opensme.cope.knowledgemanager.gui.management.list.SortedListNameVersionTierDataModel;
import eu.opensme.cope.knowledgemanager.gui.management.table.ComponentRelationDataModel;
import eu.opensme.cope.knowledgemanager.gui.management.tree.CustomTree;
import eu.opensme.cope.knowledgemanager.gui.management.tree.CustomTreeModel;
import eu.opensme.cope.knowledgemanager.gui.management.tree.TreeNodeData;
import eu.opensme.cope.knowledgemanager.gui.management.table.ComponentRelationTableModel;
import eu.opensme.cope.knowledgemanager.gui.management.table.InterfaceDataModel;
import eu.opensme.cope.knowledgemanager.gui.management.table.InterfaceTableModel;
import eu.opensme.cope.knowledgemanager.gui.management.table.MethodDataModel;
import eu.opensme.cope.knowledgemanager.gui.management.table.MethodTableModel;
import eu.opensme.cope.knowledgemanager.xml.CompareSynchronizer;
import java.awt.Font;
import javax.swing.JLabel;

public class Management extends javax.swing.JFrame {

    private boolean fromLoading;
    private SortedListKeyValueModel technologyList, languageList, qualityAttributesList;
    private SortedListKeyValueModel highQualityList, mediumQualityList, lowQualityList, patternRolesList, bcomponentRoleList;
    MyGlassPane glass = new MyGlassPane();
    InterfaceAddPanel interfaceAddPanel = new InterfaceAddPanel();
    InterfaceMethodAddPanel interfaceMethodAddPanel = new InterfaceMethodAddPanel();
    QualityAttributeSelectPanel attributeSelectPanel = new QualityAttributeSelectPanel();
    private String setSelectedProvidedInterfaceID;
    private String setSelectedProvidedMethodID;
    private String setSelectedRequiredInterfaceID;
    private String setSelectedRequiredMethodID;
    private int selectedTab;
    private RoleSelectPanelAdvanced roleSelectPanelAdvanced;
    private Classification classificationFrame;
    private ReuseProject reuseProject;

    /** Creates new form Management */
    public Management(boolean standalone, ReuseProject reuseProject) {
        
        initComponents();

        this.reuseProject = reuseProject;
        JLabel l = new JLabel("Open Component Classification Console");
        Font font = l.getFont();
        l.setFont(new Font(font.getFontName(), Font.BOLD, font.getSize()));
        l.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/classify16x16.png")));
        jTabbedPane1.setTabComponentAt(5, l);

        initMyComponents();

        this.setGlassPane(glass);
        glass.setVisible(false);

        //this.remove(jTabbedPane1);
        if (standalone) {
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else {
            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }

        CompareSynchronizer sync = new CompareSynchronizer();
        if (sync.isSynchronized()) {
            jLabelSync.setForeground(Color.BLUE);
            jLabelSync.setText("The semantic repository is synchronized with COMPARE");
        } else {
            jLabelSync.setForeground(Color.red);
            jLabelSync.setText("The semantic repository is not synchronized with COMPARE. Click here to synchronize");
        }



    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        rightPanel = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jButtonAddUses = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        usesTable = new javax.swing.JTable();
        jButtonRemoveUses = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        callsTable = new javax.swing.JTable();
        jButtonAddCalls = new javax.swing.JButton();
        jButtonRemoveCalls = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        calledByTable = new javax.swing.JTable();
        jButtonAddCalledBy = new javax.swing.JButton();
        jButtonRemoveCalledBy = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        usedByTable = new javax.swing.JTable();
        jButtonAddUsedBy = new javax.swing.JButton();
        jButtonRemoveUsedBy = new javax.swing.JButton();
        jPanel11 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jComboBoxTechnology = new javax.swing.JComboBox();
        jPanel12 = new javax.swing.JPanel();
        jComboBoxLanguage = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButtonSetVersion = new javax.swing.JButton();
        jTextFieldVersion = new javax.swing.JTextField();
        jPanel15 = new javax.swing.JPanel();
        jButtonAddProvidedInterface = new javax.swing.JButton();
        jButtonEditProvidedInterface = new javax.swing.JButton();
        jButtonRemoveProvidedInterface = new javax.swing.JButton();
        jScrollPane7 = new javax.swing.JScrollPane();
        providedInterfaceTable = new javax.swing.JTable();
        jPanel16 = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        providedInterfaceMethodsTable = new javax.swing.JTable();
        jButtonAddProvidedInterfaceMethod = new javax.swing.JButton();
        jButtonRemoveProvidedInterfaceMethod = new javax.swing.JButton();
        jButtonEditProvidedInterfaceMethod = new javax.swing.JButton();
        jPanel17 = new javax.swing.JPanel();
        jButtonEditRequiredInterface = new javax.swing.JButton();
        jButtonRemoveRequiredInterface = new javax.swing.JButton();
        jButtonAddRequiredInterface = new javax.swing.JButton();
        jScrollPane11 = new javax.swing.JScrollPane();
        requiredInterfaceTable = new javax.swing.JTable();
        jPanel18 = new javax.swing.JPanel();
        jButtonRemoveRequiredInterfaceMethod = new javax.swing.JButton();
        jButtonAddRequiredInterfaceMethod = new javax.swing.JButton();
        jScrollPane12 = new javax.swing.JScrollPane();
        requiredInterfaceMethodsTable = new javax.swing.JTable();
        jButtonEditRequiredInterfaceMethod = new javax.swing.JButton();
        jPanel22 = new javax.swing.JPanel();
        jButtonSetLicense = new javax.swing.JButton();
        jTextFieldLicense = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jPanel33 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jTextFieldSvn = new javax.swing.JTextField();
        jButtonSetSvn = new javax.swing.JButton();
        jPanel20 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jTextFieldDomain = new javax.swing.JTextField();
        jScrollPane26 = new javax.swing.JScrollPane();
        jListConcepts = new javax.swing.JList();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jTextFieldMetaModelName = new javax.swing.JTextField();
        jScrollPane28 = new javax.swing.JScrollPane();
        jTextAreaDescription = new javax.swing.JTextArea();
        jLabel8 = new javax.swing.JLabel();
        jButtonSaveDescription = new javax.swing.JButton();
        jPanel26 = new javax.swing.JPanel();
        jPanel27 = new javax.swing.JPanel();
        jScrollPane20 = new javax.swing.JScrollPane();
        jListBusinessComponents = new javax.swing.JList();
        jLabel22 = new javax.swing.JLabel();
        jButtonRenameSelectedBusinessComponent = new javax.swing.JButton();
        jButtonAddNewBusinessComponent = new javax.swing.JButton();
        jTextFieldNewBusinessComponent = new javax.swing.JTextField();
        jButtonRemoveSelectedBusinessComponent = new javax.swing.JButton();
        jScrollPane25 = new javax.swing.JScrollPane();
        jPanel32 = new javax.swing.JPanel();
        jPanel31 = new javax.swing.JPanel();
        jButtonSelectUserTier = new javax.swing.JButton();
        jScrollPane24 = new javax.swing.JScrollPane();
        userTierTable = new javax.swing.JTable();
        jButtonRemoveUserTier = new javax.swing.JButton();
        jPanel28 = new javax.swing.JPanel();
        jButtonSelectResourceTier = new javax.swing.JButton();
        jScrollPane21 = new javax.swing.JScrollPane();
        resourceTierTable = new javax.swing.JTable();
        jButtonRemoveResourceTier = new javax.swing.JButton();
        jPanel30 = new javax.swing.JPanel();
        jButtonSelectWorkspaceTier = new javax.swing.JButton();
        jScrollPane23 = new javax.swing.JScrollPane();
        workspaceTierTable = new javax.swing.JTable();
        jButtonRemoveWorkspaceTier = new javax.swing.JButton();
        jPanel29 = new javax.swing.JPanel();
        jButtonSelectEnterpriseTier = new javax.swing.JButton();
        jScrollPane22 = new javax.swing.JScrollPane();
        enterpriseTierTable = new javax.swing.JTable();
        jButtonRemoveEnterpriseTier = new javax.swing.JButton();
        jPanel34 = new javax.swing.JPanel();
        jScrollPane27 = new javax.swing.JScrollPane();
        jListPlaysRole = new javax.swing.JList();
        jButtonSelectPlaysRole = new javax.swing.JButton();
        jButtonRemovePlaysRole = new javax.swing.JButton();
        jScrollPane17 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jTextFieldNewLanguage = new javax.swing.JTextField();
        jScrollPane8 = new javax.swing.JScrollPane();
        jListLanguages = new javax.swing.JList();
        jButtonRemoveSelectedLanguage = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jButtonAddNewLanguage = new javax.swing.JButton();
        jButtonRenameSelectedLanguage = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jTextFieldNewTechnology = new javax.swing.JTextField();
        jScrollPane9 = new javax.swing.JScrollPane();
        jListTechnologies = new javax.swing.JList();
        jButtonRemoveSelectedTechnology = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        jButtonAddNewTechnology = new javax.swing.JButton();
        jButtonRenameSelectedTechnology = new javax.swing.JButton();
        jPanel14 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane13 = new javax.swing.JScrollPane();
        jListArchitecturalPatterns = new javax.swing.JList();
        jLabel18 = new javax.swing.JLabel();
        jButtonRenameSelectedArchitecturalPattern = new javax.swing.JButton();
        jButtonAddNewArchitecturalPattern = new javax.swing.JButton();
        jTextFieldNewArchitecturalPattern = new javax.swing.JTextField();
        jButtonRemoveSelectedArchitecturalPattern = new javax.swing.JButton();
        jPanel19 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jScrollPane14 = new javax.swing.JScrollPane();
        jListHighQuality = new javax.swing.JList();
        jScrollPane15 = new javax.swing.JScrollPane();
        jListMediumQuality = new javax.swing.JList();
        jLabel19 = new javax.swing.JLabel();
        jScrollPane16 = new javax.swing.JScrollPane();
        jListLowQuality = new javax.swing.JList();
        jButtonSelectHighQuality = new javax.swing.JButton();
        jButtonRemoveHighQuality = new javax.swing.JButton();
        jButtonSelectMediumQuality = new javax.swing.JButton();
        jButtonRemoveMediumQuality = new javax.swing.JButton();
        jButtonSelectLowQuality = new javax.swing.JButton();
        jButtonRemoveLowQuality = new javax.swing.JButton();
        jPanel24 = new javax.swing.JPanel();
        jScrollPane19 = new javax.swing.JScrollPane();
        jListPatternRoles = new javax.swing.JList();
        jLabel25 = new javax.swing.JLabel();
        jButtonRenameSelectedPatternRole = new javax.swing.JButton();
        jButtonAddNewPatternRole = new javax.swing.JButton();
        jTextFieldNewPatternRole = new javax.swing.JTextField();
        jButtonRemoveSelectedPatternRole = new javax.swing.JButton();
        jPanel21 = new javax.swing.JPanel();
        jPanel23 = new javax.swing.JPanel();
        jScrollPane18 = new javax.swing.JScrollPane();
        jListQualityAttributes = new javax.swing.JList();
        jLabel23 = new javax.swing.JLabel();
        jButtonRenameQualityAttribute = new javax.swing.JButton();
        jButtonAddNewQualityAttribute = new javax.swing.JButton();
        jTextFieldNewQualityAttributeName = new javax.swing.JTextField();
        jButtonRemoveQualityAttribute = new javax.swing.JButton();
        jPanel25 = new javax.swing.JPanel();
        jLabelSync = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Component Management Console");
        setIconImage(new javax.swing.ImageIcon(getClass().getResource("/images/component.png")).getImage());

        jTabbedPane1.setAutoscrolls(true);
        jTabbedPane1.setMinimumSize(new java.awt.Dimension(0, 0));
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(0, 0));

        jPanel1.setAutoscrolls(true);
        jPanel1.setPreferredSize(new java.awt.Dimension(3, 3));

        jSplitPane1.setDividerLocation(150);
        jSplitPane1.setAutoscrolls(true);
        jSplitPane1.setPreferredSize(new java.awt.Dimension(754, 302));
        jSplitPane1.setLeftComponent(jScrollPane1);

        jScrollPane2.setAutoscrolls(true);
        jScrollPane2.setMinimumSize(new java.awt.Dimension(0, 0));

        rightPanel.setBackground(new java.awt.Color(255, 255, 255));
        rightPanel.setAutoscrolls(true);
        rightPanel.setPreferredSize(new java.awt.Dimension(745, 2000));

        jPanel6.setBackground(new java.awt.Color(245, 245, 245));
        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(61, 128, 185), 2, true), "Uses Components", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14), new java.awt.Color(238, 52, 0))); // NOI18N

        jButtonAddUses.setBackground(new java.awt.Color(255, 255, 255));
        jButtonAddUses.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonAddUses.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/add16x16.png"))); // NOI18N
        jButtonAddUses.setText("Select");
        jButtonAddUses.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonAddUses.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonAddUses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddUsesActionPerformed(evt);
            }
        });

        jScrollPane3.setPreferredSize(new java.awt.Dimension(0, 0));

        usesTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        usesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(usesTable);

        jButtonRemoveUses.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRemoveUses.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRemoveUses.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete16x16.png"))); // NOI18N
        jButtonRemoveUses.setText("Remove");
        jButtonRemoveUses.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRemoveUses.setEnabled(false);
        jButtonRemoveUses.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRemoveUses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveUsesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 731, Short.MAX_VALUE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jButtonAddUses)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveUses)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonAddUses)
                    .addComponent(jButtonRemoveUses))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.setBackground(new java.awt.Color(245, 245, 245));
        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(61, 128, 185), 2, true), "Calls Components", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14), new java.awt.Color(238, 52, 0))); // NOI18N

        callsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane5.setViewportView(callsTable);

        jButtonAddCalls.setBackground(new java.awt.Color(255, 255, 255));
        jButtonAddCalls.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonAddCalls.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/add16x16.png"))); // NOI18N
        jButtonAddCalls.setText("Select");
        jButtonAddCalls.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonAddCalls.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddCallsActionPerformed(evt);
            }
        });

        jButtonRemoveCalls.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRemoveCalls.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRemoveCalls.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete16x16.png"))); // NOI18N
        jButtonRemoveCalls.setText("Remove");
        jButtonRemoveCalls.setEnabled(false);
        jButtonRemoveCalls.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRemoveCalls.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveCallsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 731, Short.MAX_VALUE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jButtonAddCalls)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveCalls)))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonAddCalls)
                    .addComponent(jButtonRemoveCalls))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel9.setBackground(new java.awt.Color(245, 245, 245));
        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(61, 128, 185), 2, true), "Called By Components", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14), new java.awt.Color(238, 52, 0))); // NOI18N

        calledByTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane6.setViewportView(calledByTable);

        jButtonAddCalledBy.setBackground(new java.awt.Color(255, 255, 255));
        jButtonAddCalledBy.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonAddCalledBy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/add16x16.png"))); // NOI18N
        jButtonAddCalledBy.setText("Select");
        jButtonAddCalledBy.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonAddCalledBy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddCalledByActionPerformed(evt);
            }
        });

        jButtonRemoveCalledBy.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRemoveCalledBy.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRemoveCalledBy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete16x16.png"))); // NOI18N
        jButtonRemoveCalledBy.setText("Remove");
        jButtonRemoveCalledBy.setEnabled(false);
        jButtonRemoveCalledBy.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRemoveCalledBy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveCalledByActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 731, Short.MAX_VALUE)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jButtonAddCalledBy)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveCalledBy)))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonAddCalledBy)
                    .addComponent(jButtonRemoveCalledBy))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.setBackground(new java.awt.Color(245, 245, 245));
        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(61, 128, 185), 2, true), "Used By Components", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14), new java.awt.Color(238, 52, 0))); // NOI18N

        usedByTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane4.setViewportView(usedByTable);

        jButtonAddUsedBy.setBackground(new java.awt.Color(255, 255, 255));
        jButtonAddUsedBy.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonAddUsedBy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/add16x16.png"))); // NOI18N
        jButtonAddUsedBy.setText("Select");
        jButtonAddUsedBy.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonAddUsedBy.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonAddUsedBy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddUsedByActionPerformed(evt);
            }
        });

        jButtonRemoveUsedBy.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRemoveUsedBy.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRemoveUsedBy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete16x16.png"))); // NOI18N
        jButtonRemoveUsedBy.setText("Remove");
        jButtonRemoveUsedBy.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRemoveUsedBy.setEnabled(false);
        jButtonRemoveUsedBy.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRemoveUsedBy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveUsedByActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 731, Short.MAX_VALUE)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jButtonAddUsedBy)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveUsedBy)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonAddUsedBy)
                    .addComponent(jButtonRemoveUsedBy))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel11.setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setText("Technology:");

        jComboBoxTechnology.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxTechnologyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxTechnology, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel2)
                    .addComponent(jComboBoxTechnology, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel12.setBackground(new java.awt.Color(255, 255, 255));

        jComboBoxLanguage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxLanguageActionPerformed(evt);
            }
        });

        jLabel3.setText("Language:");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxLanguage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel3)
                    .addComponent(jComboBoxLanguage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setText("Version:");

        jButtonSetVersion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/save_edit16x16.png"))); // NOI18N
        jButtonSetVersion.setText("Save");
        jButtonSetVersion.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonSetVersion.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonSetVersion.setPreferredSize(new java.awt.Dimension(29, 20));
        jButtonSetVersion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSetVersionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonSetVersion, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextFieldVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonSetVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel15.setBackground(new java.awt.Color(245, 245, 245));
        jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(61, 128, 185), 2, true), "Provided Interfaces", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14), new java.awt.Color(238, 52, 0))); // NOI18N

        jButtonAddProvidedInterface.setBackground(new java.awt.Color(255, 255, 255));
        jButtonAddProvidedInterface.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonAddProvidedInterface.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/add16x16.png"))); // NOI18N
        jButtonAddProvidedInterface.setText("New");
        jButtonAddProvidedInterface.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonAddProvidedInterface.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonAddProvidedInterface.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddProvidedInterfaceActionPerformed(evt);
            }
        });

        jButtonEditProvidedInterface.setBackground(new java.awt.Color(255, 255, 255));
        jButtonEditProvidedInterface.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonEditProvidedInterface.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/edit.png"))); // NOI18N
        jButtonEditProvidedInterface.setText("Edit");
        jButtonEditProvidedInterface.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonEditProvidedInterface.setEnabled(false);
        jButtonEditProvidedInterface.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonEditProvidedInterface.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditProvidedInterfaceActionPerformed(evt);
            }
        });

        jButtonRemoveProvidedInterface.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRemoveProvidedInterface.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRemoveProvidedInterface.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete16x16.png"))); // NOI18N
        jButtonRemoveProvidedInterface.setText("Remove");
        jButtonRemoveProvidedInterface.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRemoveProvidedInterface.setEnabled(false);
        jButtonRemoveProvidedInterface.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRemoveProvidedInterface.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveProvidedInterfaceActionPerformed(evt);
            }
        });

        providedInterfaceTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane7.setViewportView(providedInterfaceTable);

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addComponent(jButtonAddProvidedInterface)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveProvidedInterface)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonEditProvidedInterface)))
                .addContainerGap())
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel15Layout.createSequentialGroup()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonAddProvidedInterface)
                    .addComponent(jButtonRemoveProvidedInterface)
                    .addComponent(jButtonEditProvidedInterface))
                .addContainerGap())
        );

        jPanel16.setBackground(new java.awt.Color(245, 245, 245));
        jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(61, 128, 185), 2, true), "Has Methods", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14), new java.awt.Color(238, 52, 0))); // NOI18N

        providedInterfaceMethodsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane10.setViewportView(providedInterfaceMethodsTable);

        jButtonAddProvidedInterfaceMethod.setBackground(new java.awt.Color(255, 255, 255));
        jButtonAddProvidedInterfaceMethod.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonAddProvidedInterfaceMethod.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/add16x16.png"))); // NOI18N
        jButtonAddProvidedInterfaceMethod.setText("New");
        jButtonAddProvidedInterfaceMethod.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonAddProvidedInterfaceMethod.setEnabled(false);
        jButtonAddProvidedInterfaceMethod.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonAddProvidedInterfaceMethod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddProvidedInterfaceMethodActionPerformed(evt);
            }
        });

        jButtonRemoveProvidedInterfaceMethod.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRemoveProvidedInterfaceMethod.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRemoveProvidedInterfaceMethod.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete16x16.png"))); // NOI18N
        jButtonRemoveProvidedInterfaceMethod.setText("Remove");
        jButtonRemoveProvidedInterfaceMethod.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRemoveProvidedInterfaceMethod.setEnabled(false);
        jButtonRemoveProvidedInterfaceMethod.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRemoveProvidedInterfaceMethod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveProvidedInterfaceMethodActionPerformed(evt);
            }
        });

        jButtonEditProvidedInterfaceMethod.setBackground(new java.awt.Color(255, 255, 255));
        jButtonEditProvidedInterfaceMethod.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonEditProvidedInterfaceMethod.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/edit.png"))); // NOI18N
        jButtonEditProvidedInterfaceMethod.setText("Edit");
        jButtonEditProvidedInterfaceMethod.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonEditProvidedInterfaceMethod.setEnabled(false);
        jButtonEditProvidedInterfaceMethod.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonEditProvidedInterfaceMethod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditProvidedInterfaceMethodActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addComponent(jButtonAddProvidedInterfaceMethod)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveProvidedInterfaceMethod)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonEditProvidedInterfaceMethod)))
                .addContainerGap())
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel16Layout.createSequentialGroup()
                .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonAddProvidedInterfaceMethod)
                    .addComponent(jButtonRemoveProvidedInterfaceMethod)
                    .addComponent(jButtonEditProvidedInterfaceMethod))
                .addContainerGap())
        );

        jPanel17.setBackground(new java.awt.Color(245, 245, 245));
        jPanel17.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(61, 128, 185), 2, true), "Required Interfaces", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14), new java.awt.Color(238, 52, 0))); // NOI18N

        jButtonEditRequiredInterface.setBackground(new java.awt.Color(255, 255, 255));
        jButtonEditRequiredInterface.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonEditRequiredInterface.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/edit.png"))); // NOI18N
        jButtonEditRequiredInterface.setText("Edit");
        jButtonEditRequiredInterface.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonEditRequiredInterface.setEnabled(false);
        jButtonEditRequiredInterface.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonEditRequiredInterface.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditRequiredInterfaceActionPerformed(evt);
            }
        });

        jButtonRemoveRequiredInterface.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRemoveRequiredInterface.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRemoveRequiredInterface.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete16x16.png"))); // NOI18N
        jButtonRemoveRequiredInterface.setText("Remove");
        jButtonRemoveRequiredInterface.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRemoveRequiredInterface.setEnabled(false);
        jButtonRemoveRequiredInterface.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRemoveRequiredInterface.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveRequiredInterfaceActionPerformed(evt);
            }
        });

        jButtonAddRequiredInterface.setBackground(new java.awt.Color(255, 255, 255));
        jButtonAddRequiredInterface.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonAddRequiredInterface.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/add16x16.png"))); // NOI18N
        jButtonAddRequiredInterface.setText("New");
        jButtonAddRequiredInterface.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonAddRequiredInterface.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonAddRequiredInterface.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddRequiredInterfaceActionPerformed(evt);
            }
        });

        requiredInterfaceTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane11.setViewportView(requiredInterfaceTable);

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addComponent(jButtonAddRequiredInterface)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveRequiredInterface)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonEditRequiredInterface)))
                .addContainerGap())
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonAddRequiredInterface)
                    .addComponent(jButtonRemoveRequiredInterface)
                    .addComponent(jButtonEditRequiredInterface))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel18.setBackground(new java.awt.Color(245, 245, 245));
        jPanel18.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(61, 128, 185), 2, true), "Has Methods", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14), new java.awt.Color(238, 52, 0))); // NOI18N

        jButtonRemoveRequiredInterfaceMethod.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRemoveRequiredInterfaceMethod.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRemoveRequiredInterfaceMethod.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete16x16.png"))); // NOI18N
        jButtonRemoveRequiredInterfaceMethod.setText("Remove");
        jButtonRemoveRequiredInterfaceMethod.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRemoveRequiredInterfaceMethod.setEnabled(false);
        jButtonRemoveRequiredInterfaceMethod.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRemoveRequiredInterfaceMethod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveRequiredInterfaceMethodActionPerformed(evt);
            }
        });

        jButtonAddRequiredInterfaceMethod.setBackground(new java.awt.Color(255, 255, 255));
        jButtonAddRequiredInterfaceMethod.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonAddRequiredInterfaceMethod.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/add16x16.png"))); // NOI18N
        jButtonAddRequiredInterfaceMethod.setText("New");
        jButtonAddRequiredInterfaceMethod.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonAddRequiredInterfaceMethod.setEnabled(false);
        jButtonAddRequiredInterfaceMethod.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonAddRequiredInterfaceMethod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddRequiredInterfaceMethodActionPerformed(evt);
            }
        });

        requiredInterfaceMethodsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane12.setViewportView(requiredInterfaceMethodsTable);

        jButtonEditRequiredInterfaceMethod.setBackground(new java.awt.Color(255, 255, 255));
        jButtonEditRequiredInterfaceMethod.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonEditRequiredInterfaceMethod.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/edit.png"))); // NOI18N
        jButtonEditRequiredInterfaceMethod.setText("Edit");
        jButtonEditRequiredInterfaceMethod.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonEditRequiredInterfaceMethod.setEnabled(false);
        jButtonEditRequiredInterfaceMethod.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonEditRequiredInterfaceMethod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditRequiredInterfaceMethodActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane12, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addComponent(jButtonAddRequiredInterfaceMethod)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveRequiredInterfaceMethod)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonEditRequiredInterfaceMethod)))
                .addContainerGap())
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonAddRequiredInterfaceMethod)
                    .addComponent(jButtonRemoveRequiredInterfaceMethod)
                    .addComponent(jButtonEditRequiredInterfaceMethod))
                .addContainerGap())
        );

        jPanel22.setBackground(new java.awt.Color(255, 255, 255));

        jButtonSetLicense.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/save_edit16x16.png"))); // NOI18N
        jButtonSetLicense.setText("Save");
        jButtonSetLicense.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonSetLicense.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonSetLicense.setPreferredSize(new java.awt.Dimension(29, 20));
        jButtonSetLicense.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSetLicenseActionPerformed(evt);
            }
        });

        jLabel4.setText("License:");

        javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldLicense, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonSetLicense, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE))
        );
        jPanel22Layout.setVerticalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextFieldLicense, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonSetLicense, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel33.setBackground(new java.awt.Color(255, 255, 255));

        jLabel13.setText("SVN:");

        jButtonSetSvn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/save_edit16x16.png"))); // NOI18N
        jButtonSetSvn.setText("Save");
        jButtonSetSvn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonSetSvn.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonSetSvn.setPreferredSize(new java.awt.Dimension(29, 20));
        jButtonSetSvn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSetSvnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel33Layout = new javax.swing.GroupLayout(jPanel33);
        jPanel33.setLayout(jPanel33Layout);
        jPanel33Layout.setHorizontalGroup(
            jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel33Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldSvn, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonSetSvn, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel33Layout.setVerticalGroup(
            jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel33Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(jTextFieldSvn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonSetSvn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel20.setBackground(new java.awt.Color(245, 245, 245));
        jPanel20.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(61, 128, 185), 2, true), "Classification Details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14), new java.awt.Color(238, 52, 0))); // NOI18N

        jLabel5.setText("Domain:");

        jTextFieldDomain.setEditable(false);

        jListConcepts.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListConcepts.setVisibleRowCount(4);
        jScrollPane26.setViewportView(jListConcepts);

        jLabel6.setText("Concepts");

        jLabel7.setText("MetaModel:");

        jTextFieldMetaModelName.setEditable(false);

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldMetaModelName, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE))
                    .addComponent(jScrollPane26, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldDomain, javax.swing.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE))
                    .addComponent(jLabel6))
                .addContainerGap())
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jTextFieldMetaModelName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTextFieldDomain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addGap(4, 4, 4)
                .addComponent(jScrollPane26, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTextAreaDescription.setColumns(20);
        jTextAreaDescription.setLineWrap(true);
        jTextAreaDescription.setRows(5);
        jScrollPane28.setViewportView(jTextAreaDescription);

        jLabel8.setText("Description");

        jButtonSaveDescription.setBackground(new java.awt.Color(255, 255, 255));
        jButtonSaveDescription.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonSaveDescription.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/save_edit16x16.png"))); // NOI18N
        jButtonSaveDescription.setText("Save");
        jButtonSaveDescription.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonSaveDescription.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonSaveDescription.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveDescriptionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout rightPanelLayout = new javax.swing.GroupLayout(rightPanel);
        rightPanel.setLayout(rightPanelLayout);
        rightPanelLayout.setHorizontalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane28, javax.swing.GroupLayout.DEFAULT_SIZE, 763, Short.MAX_VALUE)
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(rightPanelLayout.createSequentialGroup()
                                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jPanel22, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jPanel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jPanel33, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel8)
                    .addComponent(jButtonSaveDescription, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        rightPanelLayout.setVerticalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addComponent(jPanel33, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addGap(4, 4, 4)
                .addComponent(jScrollPane28, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonSaveDescription)
                .addGap(35, 35, 35)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17)
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        rightPanel.setVisible(false);

        jScrollPane2.setViewportView(rightPanel);

        jSplitPane1.setRightComponent(jScrollPane2);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 954, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 578, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Components", jPanel1);

        jPanel26.setBackground(new java.awt.Color(255, 255, 255));

        jPanel27.setBackground(new java.awt.Color(245, 245, 245));
        jPanel27.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(61, 128, 185), 2, true), "Business Components", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14), new java.awt.Color(238, 52, 0))); // NOI18N

        jListBusinessComponents.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane20.setViewportView(jListBusinessComponents);

        jLabel22.setText("New Business Component Name");

        jButtonRenameSelectedBusinessComponent.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRenameSelectedBusinessComponent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rename16x16.png"))); // NOI18N
        jButtonRenameSelectedBusinessComponent.setText("Rename");
        jButtonRenameSelectedBusinessComponent.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRenameSelectedBusinessComponent.setEnabled(false);
        jButtonRenameSelectedBusinessComponent.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRenameSelectedBusinessComponent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRenameSelectedBusinessComponentActionPerformed(evt);
            }
        });

        jButtonAddNewBusinessComponent.setBackground(new java.awt.Color(255, 255, 255));
        jButtonAddNewBusinessComponent.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonAddNewBusinessComponent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/add16x16.png"))); // NOI18N
        jButtonAddNewBusinessComponent.setText("Add");
        jButtonAddNewBusinessComponent.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonAddNewBusinessComponent.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonAddNewBusinessComponent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddNewBusinessComponentActionPerformed(evt);
            }
        });

        jButtonRemoveSelectedBusinessComponent.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRemoveSelectedBusinessComponent.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRemoveSelectedBusinessComponent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete16x16.png"))); // NOI18N
        jButtonRemoveSelectedBusinessComponent.setText("Remove");
        jButtonRemoveSelectedBusinessComponent.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRemoveSelectedBusinessComponent.setEnabled(false);
        jButtonRemoveSelectedBusinessComponent.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRemoveSelectedBusinessComponent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveSelectedBusinessComponentActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel27Layout = new javax.swing.GroupLayout(jPanel27);
        jPanel27.setLayout(jPanel27Layout);
        jPanel27Layout.setHorizontalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel27Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel22)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel27Layout.createSequentialGroup()
                        .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel27Layout.createSequentialGroup()
                                .addComponent(jTextFieldNewBusinessComponent, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonAddNewBusinessComponent, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane20, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonRenameSelectedBusinessComponent)
                            .addComponent(jButtonRemoveSelectedBusinessComponent))))
                .addContainerGap())
        );
        jPanel27Layout.setVerticalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel27Layout.createSequentialGroup()
                .addComponent(jLabel22)
                .addGap(6, 6, 6)
                .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldNewBusinessComponent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonAddNewBusinessComponent))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel27Layout.createSequentialGroup()
                        .addComponent(jButtonRenameSelectedBusinessComponent)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveSelectedBusinessComponent))
                    .addComponent(jScrollPane20, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE))
                .addContainerGap())
        );

        jScrollPane25.setAutoscrolls(true);
        jScrollPane25.setMinimumSize(new java.awt.Dimension(0, 0));
        jScrollPane25.setPreferredSize(new java.awt.Dimension(590, 500));

        jPanel32.setBackground(new java.awt.Color(255, 255, 255));
        jPanel32.setAutoscrolls(true);
        jPanel32.setPreferredSize(new java.awt.Dimension(565, 1000));

        jPanel31.setBackground(new java.awt.Color(245, 245, 245));
        jPanel31.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(61, 128, 185), 2, true), "User Tier", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14), new java.awt.Color(238, 52, 0))); // NOI18N
        jPanel31.setPreferredSize(new java.awt.Dimension(552, 170));

        jButtonSelectUserTier.setBackground(new java.awt.Color(255, 255, 255));
        jButtonSelectUserTier.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonSelectUserTier.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/add16x16.png"))); // NOI18N
        jButtonSelectUserTier.setText("Select");
        jButtonSelectUserTier.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonSelectUserTier.setEnabled(false);
        jButtonSelectUserTier.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonSelectUserTier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectUserTierActionPerformed(evt);
            }
        });

        jScrollPane24.setPreferredSize(new java.awt.Dimension(0, 0));

        userTierTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        userTierTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        userTierTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane24.setViewportView(userTierTable);
        jScrollPane24.getViewport().setBackground(Color.WHITE);

        jButtonRemoveUserTier.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRemoveUserTier.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRemoveUserTier.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete16x16.png"))); // NOI18N
        jButtonRemoveUserTier.setText("Remove");
        jButtonRemoveUserTier.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRemoveUserTier.setEnabled(false);
        jButtonRemoveUserTier.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRemoveUserTier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveUserTierActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel31Layout = new javax.swing.GroupLayout(jPanel31);
        jPanel31.setLayout(jPanel31Layout);
        jPanel31Layout.setHorizontalGroup(
            jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel31Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane24, javax.swing.GroupLayout.DEFAULT_SIZE, 533, Short.MAX_VALUE)
                    .addGroup(jPanel31Layout.createSequentialGroup()
                        .addComponent(jButtonSelectUserTier)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveUserTier)))
                .addContainerGap())
        );
        jPanel31Layout.setVerticalGroup(
            jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel31Layout.createSequentialGroup()
                .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonSelectUserTier)
                    .addComponent(jButtonRemoveUserTier))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane24, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel28.setBackground(new java.awt.Color(245, 245, 245));
        jPanel28.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(61, 128, 185), 2, true), "Resource Tier", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14), new java.awt.Color(238, 52, 0))); // NOI18N
        jPanel28.setPreferredSize(new java.awt.Dimension(552, 170));

        jButtonSelectResourceTier.setBackground(new java.awt.Color(255, 255, 255));
        jButtonSelectResourceTier.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonSelectResourceTier.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/add16x16.png"))); // NOI18N
        jButtonSelectResourceTier.setText("Select");
        jButtonSelectResourceTier.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonSelectResourceTier.setEnabled(false);
        jButtonSelectResourceTier.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonSelectResourceTier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectResourceTierActionPerformed(evt);
            }
        });

        jScrollPane21.setPreferredSize(new java.awt.Dimension(0, 0));

        resourceTierTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        resourceTierTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        resourceTierTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane21.setViewportView(resourceTierTable);
        jScrollPane21.getViewport().setBackground(Color.WHITE);

        jButtonRemoveResourceTier.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRemoveResourceTier.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRemoveResourceTier.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete16x16.png"))); // NOI18N
        jButtonRemoveResourceTier.setText("Remove");
        jButtonRemoveResourceTier.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRemoveResourceTier.setEnabled(false);
        jButtonRemoveResourceTier.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRemoveResourceTier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveResourceTierActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel28Layout = new javax.swing.GroupLayout(jPanel28);
        jPanel28.setLayout(jPanel28Layout);
        jPanel28Layout.setHorizontalGroup(
            jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel28Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane21, javax.swing.GroupLayout.DEFAULT_SIZE, 533, Short.MAX_VALUE)
                    .addGroup(jPanel28Layout.createSequentialGroup()
                        .addComponent(jButtonSelectResourceTier)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveResourceTier)))
                .addContainerGap())
        );
        jPanel28Layout.setVerticalGroup(
            jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel28Layout.createSequentialGroup()
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonSelectResourceTier)
                    .addComponent(jButtonRemoveResourceTier))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane21, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel30.setBackground(new java.awt.Color(245, 245, 245));
        jPanel30.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(61, 128, 185), 2, true), "Workspace Tier", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14), new java.awt.Color(238, 52, 0))); // NOI18N
        jPanel30.setPreferredSize(new java.awt.Dimension(552, 170));

        jButtonSelectWorkspaceTier.setBackground(new java.awt.Color(255, 255, 255));
        jButtonSelectWorkspaceTier.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonSelectWorkspaceTier.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/add16x16.png"))); // NOI18N
        jButtonSelectWorkspaceTier.setText("Select");
        jButtonSelectWorkspaceTier.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonSelectWorkspaceTier.setEnabled(false);
        jButtonSelectWorkspaceTier.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonSelectWorkspaceTier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectWorkspaceTierActionPerformed(evt);
            }
        });

        jScrollPane23.setPreferredSize(new java.awt.Dimension(0, 0));

        workspaceTierTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        workspaceTierTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        workspaceTierTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane23.setViewportView(workspaceTierTable);
        jScrollPane23.getViewport().setBackground(Color.WHITE);

        jButtonRemoveWorkspaceTier.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRemoveWorkspaceTier.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRemoveWorkspaceTier.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete16x16.png"))); // NOI18N
        jButtonRemoveWorkspaceTier.setText("Remove");
        jButtonRemoveWorkspaceTier.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRemoveWorkspaceTier.setEnabled(false);
        jButtonRemoveWorkspaceTier.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRemoveWorkspaceTier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveWorkspaceTierActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel30Layout = new javax.swing.GroupLayout(jPanel30);
        jPanel30.setLayout(jPanel30Layout);
        jPanel30Layout.setHorizontalGroup(
            jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel30Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane23, javax.swing.GroupLayout.DEFAULT_SIZE, 533, Short.MAX_VALUE)
                    .addGroup(jPanel30Layout.createSequentialGroup()
                        .addComponent(jButtonSelectWorkspaceTier)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveWorkspaceTier)))
                .addContainerGap())
        );
        jPanel30Layout.setVerticalGroup(
            jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel30Layout.createSequentialGroup()
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonSelectWorkspaceTier)
                    .addComponent(jButtonRemoveWorkspaceTier))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane23, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel29.setBackground(new java.awt.Color(245, 245, 245));
        jPanel29.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(61, 128, 185), 2, true), "Enterprise Tier", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14), new java.awt.Color(238, 52, 0))); // NOI18N
        jPanel29.setPreferredSize(new java.awt.Dimension(552, 170));

        jButtonSelectEnterpriseTier.setBackground(new java.awt.Color(255, 255, 255));
        jButtonSelectEnterpriseTier.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonSelectEnterpriseTier.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/add16x16.png"))); // NOI18N
        jButtonSelectEnterpriseTier.setText("Select");
        jButtonSelectEnterpriseTier.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonSelectEnterpriseTier.setEnabled(false);
        jButtonSelectEnterpriseTier.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonSelectEnterpriseTier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectEnterpriseTierActionPerformed(evt);
            }
        });

        jScrollPane22.setPreferredSize(new java.awt.Dimension(0, 0));

        enterpriseTierTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        enterpriseTierTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        enterpriseTierTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane22.setViewportView(enterpriseTierTable);

        jButtonRemoveEnterpriseTier.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRemoveEnterpriseTier.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRemoveEnterpriseTier.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete16x16.png"))); // NOI18N
        jButtonRemoveEnterpriseTier.setText("Remove");
        jButtonRemoveEnterpriseTier.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRemoveEnterpriseTier.setEnabled(false);
        jButtonRemoveEnterpriseTier.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRemoveEnterpriseTier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveEnterpriseTierActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel29Layout = new javax.swing.GroupLayout(jPanel29);
        jPanel29.setLayout(jPanel29Layout);
        jPanel29Layout.setHorizontalGroup(
            jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel29Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane22, javax.swing.GroupLayout.DEFAULT_SIZE, 533, Short.MAX_VALUE)
                    .addGroup(jPanel29Layout.createSequentialGroup()
                        .addComponent(jButtonSelectEnterpriseTier)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveEnterpriseTier)))
                .addContainerGap())
        );
        jPanel29Layout.setVerticalGroup(
            jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel29Layout.createSequentialGroup()
                .addGroup(jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonSelectEnterpriseTier)
                    .addComponent(jButtonRemoveEnterpriseTier))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane22, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                .addContainerGap())
        );

        jScrollPane22.getViewport().setBackground(Color.WHITE);

        jPanel34.setBackground(new java.awt.Color(245, 245, 245));
        jPanel34.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(61, 128, 185), 2, true), "Plays Roles", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14), new java.awt.Color(238, 52, 0))); // NOI18N

        jListPlaysRole.setVisibleRowCount(3);
        jScrollPane27.setViewportView(jListPlaysRole);

        jButtonSelectPlaysRole.setBackground(new java.awt.Color(255, 255, 255));
        jButtonSelectPlaysRole.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonSelectPlaysRole.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/add16x16.png"))); // NOI18N
        jButtonSelectPlaysRole.setText("Select");
        jButtonSelectPlaysRole.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonSelectPlaysRole.setEnabled(false);
        jButtonSelectPlaysRole.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonSelectPlaysRole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectPlaysRoleActionPerformed(evt);
            }
        });

        jButtonRemovePlaysRole.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRemovePlaysRole.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRemovePlaysRole.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete16x16.png"))); // NOI18N
        jButtonRemovePlaysRole.setText("Remove");
        jButtonRemovePlaysRole.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRemovePlaysRole.setEnabled(false);
        jButtonRemovePlaysRole.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRemovePlaysRole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemovePlaysRoleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel34Layout = new javax.swing.GroupLayout(jPanel34);
        jPanel34.setLayout(jPanel34Layout);
        jPanel34Layout.setHorizontalGroup(
            jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel34Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane27, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonSelectPlaysRole)
                    .addComponent(jButtonRemovePlaysRole))
                .addContainerGap())
        );
        jPanel34Layout.setVerticalGroup(
            jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel34Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane27, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                    .addGroup(jPanel34Layout.createSequentialGroup()
                        .addComponent(jButtonSelectPlaysRole)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemovePlaysRole)))
                .addContainerGap())
        );

        jEditorPane1.setContentType("text/html");
        jEditorPane1.setEditable(false);
        jEditorPane1.setFont(new java.awt.Font("Arial", 0, 12));
        jEditorPane1.setText("");
        jScrollPane17.setViewportView(jEditorPane1);

        javax.swing.GroupLayout jPanel32Layout = new javax.swing.GroupLayout(jPanel32);
        jPanel32.setLayout(jPanel32Layout);
        jPanel32Layout.setHorizontalGroup(
            jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel32Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel31, javax.swing.GroupLayout.DEFAULT_SIZE, 565, Short.MAX_VALUE)
                    .addComponent(jPanel28, javax.swing.GroupLayout.DEFAULT_SIZE, 565, Short.MAX_VALUE)
                    .addComponent(jPanel29, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 565, Short.MAX_VALUE)
                    .addComponent(jPanel30, javax.swing.GroupLayout.DEFAULT_SIZE, 565, Short.MAX_VALUE)
                    .addGroup(jPanel32Layout.createSequentialGroup()
                        .addComponent(jPanel34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane17, javax.swing.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel32Layout.setVerticalGroup(
            jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel32Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane17, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                    .addComponent(jPanel34, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jScrollPane25.setViewportView(jPanel32);
        jScrollPane25.getViewport().setBackground(Color.WHITE);

        javax.swing.GroupLayout jPanel26Layout = new javax.swing.GroupLayout(jPanel26);
        jPanel26.setLayout(jPanel26Layout);
        jPanel26Layout.setHorizontalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel26Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane25, javax.swing.GroupLayout.DEFAULT_SIZE, 604, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel26Layout.setVerticalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel26Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane25, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 574, Short.MAX_VALUE)
                    .addComponent(jPanel27, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(15, 15, 15))
        );

        jScrollPane25.getVerticalScrollBar().setUnitIncrement(16);

        jTabbedPane1.addTab("Business Components", jPanel26);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jPanel4.setBackground(new java.awt.Color(245, 245, 245));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(110, 161, 205), 2, true), "Existing Languages", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14), new java.awt.Color(238, 52, 0))); // NOI18N

        jListLanguages.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane8.setViewportView(jListLanguages);

        jButtonRemoveSelectedLanguage.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRemoveSelectedLanguage.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRemoveSelectedLanguage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete16x16.png"))); // NOI18N
        jButtonRemoveSelectedLanguage.setText("Remove");
        jButtonRemoveSelectedLanguage.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRemoveSelectedLanguage.setEnabled(false);
        jButtonRemoveSelectedLanguage.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRemoveSelectedLanguage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveSelectedLanguageActionPerformed(evt);
            }
        });

        jLabel11.setText("New Language Name");

        jButtonAddNewLanguage.setBackground(new java.awt.Color(255, 255, 255));
        jButtonAddNewLanguage.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonAddNewLanguage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/add16x16.png"))); // NOI18N
        jButtonAddNewLanguage.setText("Add");
        jButtonAddNewLanguage.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonAddNewLanguage.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonAddNewLanguage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddNewLanguageActionPerformed(evt);
            }
        });

        jButtonRenameSelectedLanguage.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRenameSelectedLanguage.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRenameSelectedLanguage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rename16x16.png"))); // NOI18N
        jButtonRenameSelectedLanguage.setText("Rename");
        jButtonRenameSelectedLanguage.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRenameSelectedLanguage.setEnabled(false);
        jButtonRenameSelectedLanguage.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRenameSelectedLanguage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRenameSelectedLanguageActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(jTextFieldNewLanguage, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonAddNewLanguage))
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonRenameSelectedLanguage)
                    .addComponent(jButtonRemoveSelectedLanguage))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldNewLanguage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonAddNewLanguage))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jButtonRenameSelectedLanguage)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveSelectedLanguage))
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel5.setBackground(new java.awt.Color(245, 245, 245));
        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(61, 128, 185), 2, true), "Existing Technologies", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14), new java.awt.Color(238, 52, 0))); // NOI18N

        jListTechnologies.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane9.setViewportView(jListTechnologies);

        jButtonRemoveSelectedTechnology.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRemoveSelectedTechnology.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRemoveSelectedTechnology.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete16x16.png"))); // NOI18N
        jButtonRemoveSelectedTechnology.setText("Remove");
        jButtonRemoveSelectedTechnology.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRemoveSelectedTechnology.setEnabled(false);
        jButtonRemoveSelectedTechnology.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRemoveSelectedTechnology.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveSelectedTechnologyActionPerformed(evt);
            }
        });

        jLabel12.setText("New Technology Name");

        jButtonAddNewTechnology.setBackground(new java.awt.Color(255, 255, 255));
        jButtonAddNewTechnology.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonAddNewTechnology.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/add16x16.png"))); // NOI18N
        jButtonAddNewTechnology.setText("Add");
        jButtonAddNewTechnology.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonAddNewTechnology.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonAddNewTechnology.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddNewTechnologyActionPerformed(evt);
            }
        });

        jButtonRenameSelectedTechnology.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRenameSelectedTechnology.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRenameSelectedTechnology.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rename16x16.png"))); // NOI18N
        jButtonRenameSelectedTechnology.setText("Rename");
        jButtonRenameSelectedTechnology.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRenameSelectedTechnology.setEnabled(false);
        jButtonRenameSelectedTechnology.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRenameSelectedTechnology.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRenameSelectedTechnologyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12)
                            .addComponent(jTextFieldNewTechnology, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonAddNewTechnology))
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonRenameSelectedTechnology)
                    .addComponent(jButtonRemoveSelectedTechnology))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldNewTechnology, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonAddNewTechnology))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jButtonRenameSelectedTechnology)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveSelectedTechnology))
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(64, 64, 64)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(66, 66, 66)
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(76, 76, 76))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(168, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Languages/Technologies", jPanel3);

        jPanel14.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(245, 245, 245));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(61, 128, 185), 2, true), "Architectural Patterns", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14), new java.awt.Color(238, 52, 0))); // NOI18N

        jListArchitecturalPatterns.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane13.setViewportView(jListArchitecturalPatterns);

        jLabel18.setText("New Arhitectural Pattern Name");

        jButtonRenameSelectedArchitecturalPattern.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRenameSelectedArchitecturalPattern.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rename16x16.png"))); // NOI18N
        jButtonRenameSelectedArchitecturalPattern.setText("Rename");
        jButtonRenameSelectedArchitecturalPattern.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRenameSelectedArchitecturalPattern.setEnabled(false);
        jButtonRenameSelectedArchitecturalPattern.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRenameSelectedArchitecturalPattern.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRenameSelectedArchitecturalPatternActionPerformed(evt);
            }
        });

        jButtonAddNewArchitecturalPattern.setBackground(new java.awt.Color(255, 255, 255));
        jButtonAddNewArchitecturalPattern.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonAddNewArchitecturalPattern.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/add16x16.png"))); // NOI18N
        jButtonAddNewArchitecturalPattern.setText("Add");
        jButtonAddNewArchitecturalPattern.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonAddNewArchitecturalPattern.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonAddNewArchitecturalPattern.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddNewArchitecturalPatternActionPerformed(evt);
            }
        });

        jButtonRemoveSelectedArchitecturalPattern.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRemoveSelectedArchitecturalPattern.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRemoveSelectedArchitecturalPattern.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete16x16.png"))); // NOI18N
        jButtonRemoveSelectedArchitecturalPattern.setText("Remove");
        jButtonRemoveSelectedArchitecturalPattern.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRemoveSelectedArchitecturalPattern.setEnabled(false);
        jButtonRemoveSelectedArchitecturalPattern.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRemoveSelectedArchitecturalPattern.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveSelectedArchitecturalPatternActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel18)
                            .addComponent(jTextFieldNewArchitecturalPattern, javax.swing.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonAddNewArchitecturalPattern))
                    .addComponent(jScrollPane13, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonRemoveSelectedArchitecturalPattern)
                    .addComponent(jButtonRenameSelectedArchitecturalPattern))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldNewArchitecturalPattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonAddNewArchitecturalPattern))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButtonRenameSelectedArchitecturalPattern)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveSelectedArchitecturalPattern))
                    .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel19.setBackground(new java.awt.Color(245, 245, 245));
        jPanel19.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(61, 128, 185), 2, true), "Quality Attributes", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14), new java.awt.Color(238, 52, 0))); // NOI18N

        jLabel21.setText("Low Quality");

        jLabel20.setText("Medium Quality");

        jListHighQuality.setVisibleRowCount(3);
        jScrollPane14.setViewportView(jListHighQuality);

        jListMediumQuality.setVisibleRowCount(3);
        jScrollPane15.setViewportView(jListMediumQuality);

        jLabel19.setText("High Quality");

        jListLowQuality.setVisibleRowCount(3);
        jScrollPane16.setViewportView(jListLowQuality);

        jButtonSelectHighQuality.setBackground(new java.awt.Color(255, 255, 255));
        jButtonSelectHighQuality.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonSelectHighQuality.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/add16x16.png"))); // NOI18N
        jButtonSelectHighQuality.setText("Select");
        jButtonSelectHighQuality.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonSelectHighQuality.setEnabled(false);
        jButtonSelectHighQuality.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonSelectHighQuality.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectHighQualityActionPerformed(evt);
            }
        });

        jButtonRemoveHighQuality.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRemoveHighQuality.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRemoveHighQuality.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete16x16.png"))); // NOI18N
        jButtonRemoveHighQuality.setText("Remove");
        jButtonRemoveHighQuality.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRemoveHighQuality.setEnabled(false);
        jButtonRemoveHighQuality.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRemoveHighQuality.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveHighQualityActionPerformed(evt);
            }
        });

        jButtonSelectMediumQuality.setBackground(new java.awt.Color(255, 255, 255));
        jButtonSelectMediumQuality.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonSelectMediumQuality.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/add16x16.png"))); // NOI18N
        jButtonSelectMediumQuality.setText("Select");
        jButtonSelectMediumQuality.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonSelectMediumQuality.setEnabled(false);
        jButtonSelectMediumQuality.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonSelectMediumQuality.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectMediumQualityActionPerformed(evt);
            }
        });

        jButtonRemoveMediumQuality.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRemoveMediumQuality.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRemoveMediumQuality.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete16x16.png"))); // NOI18N
        jButtonRemoveMediumQuality.setText("Remove");
        jButtonRemoveMediumQuality.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRemoveMediumQuality.setEnabled(false);
        jButtonRemoveMediumQuality.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRemoveMediumQuality.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveMediumQualityActionPerformed(evt);
            }
        });

        jButtonSelectLowQuality.setBackground(new java.awt.Color(255, 255, 255));
        jButtonSelectLowQuality.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonSelectLowQuality.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/add16x16.png"))); // NOI18N
        jButtonSelectLowQuality.setText("Select");
        jButtonSelectLowQuality.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonSelectLowQuality.setEnabled(false);
        jButtonSelectLowQuality.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonSelectLowQuality.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectLowQualityActionPerformed(evt);
            }
        });

        jButtonRemoveLowQuality.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRemoveLowQuality.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRemoveLowQuality.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete16x16.png"))); // NOI18N
        jButtonRemoveLowQuality.setText("Remove");
        jButtonRemoveLowQuality.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRemoveLowQuality.setEnabled(false);
        jButtonRemoveLowQuality.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRemoveLowQuality.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveLowQualityActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel19)
                    .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                    .addGroup(jPanel19Layout.createSequentialGroup()
                        .addComponent(jButtonSelectHighQuality)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveHighQuality)))
                .addGap(18, 18, 18)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane15, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                    .addComponent(jLabel20)
                    .addGroup(jPanel19Layout.createSequentialGroup()
                        .addComponent(jButtonSelectMediumQuality)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveMediumQuality)))
                .addGap(18, 18, 18)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel19Layout.createSequentialGroup()
                        .addComponent(jButtonSelectLowQuality)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveLowQuality))
                    .addComponent(jLabel21)
                    .addComponent(jScrollPane16, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(jLabel20)
                    .addComponent(jLabel21))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane16, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                    .addComponent(jScrollPane15, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                    .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonSelectLowQuality)
                    .addComponent(jButtonRemoveLowQuality)
                    .addComponent(jButtonSelectMediumQuality)
                    .addComponent(jButtonRemoveMediumQuality)
                    .addComponent(jButtonSelectHighQuality)
                    .addComponent(jButtonRemoveHighQuality))
                .addContainerGap())
        );

        jPanel24.setBackground(new java.awt.Color(245, 245, 245));
        jPanel24.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(61, 128, 185), 2, true), "Roles", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14), new java.awt.Color(238, 52, 0))); // NOI18N

        jScrollPane19.setViewportView(jListPatternRoles);

        jLabel25.setText("New Role Name");

        jButtonRenameSelectedPatternRole.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRenameSelectedPatternRole.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRenameSelectedPatternRole.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rename16x16.png"))); // NOI18N
        jButtonRenameSelectedPatternRole.setText("Rename");
        jButtonRenameSelectedPatternRole.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRenameSelectedPatternRole.setEnabled(false);
        jButtonRenameSelectedPatternRole.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRenameSelectedPatternRole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRenameSelectedPatternRoleActionPerformed(evt);
            }
        });

        jButtonAddNewPatternRole.setBackground(new java.awt.Color(255, 255, 255));
        jButtonAddNewPatternRole.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonAddNewPatternRole.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/add16x16.png"))); // NOI18N
        jButtonAddNewPatternRole.setText("Add");
        jButtonAddNewPatternRole.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonAddNewPatternRole.setEnabled(false);
        jButtonAddNewPatternRole.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonAddNewPatternRole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddNewPatternRoleActionPerformed(evt);
            }
        });

        jButtonRemoveSelectedPatternRole.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRemoveSelectedPatternRole.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRemoveSelectedPatternRole.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete16x16.png"))); // NOI18N
        jButtonRemoveSelectedPatternRole.setText("Remove");
        jButtonRemoveSelectedPatternRole.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRemoveSelectedPatternRole.setEnabled(false);
        jButtonRemoveSelectedPatternRole.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRemoveSelectedPatternRole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveSelectedPatternRoleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
        jPanel24.setLayout(jPanel24Layout);
        jPanel24Layout.setHorizontalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel24Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane19, javax.swing.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel25)
                    .addComponent(jTextFieldNewPatternRole, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel24Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(jButtonAddNewPatternRole))
                    .addComponent(jButtonRemoveSelectedPatternRole)
                    .addComponent(jButtonRenameSelectedPatternRole))
                .addContainerGap())
        );
        jPanel24Layout.setVerticalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel24Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane19, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel24Layout.createSequentialGroup()
                        .addComponent(jLabel25)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldNewPatternRole, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonAddNewPatternRole)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 98, Short.MAX_VALUE)
                        .addComponent(jButtonRenameSelectedPatternRole)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveSelectedPatternRole)))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addComponent(jPanel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(136, 136, 136)))
                .addContainerGap())
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
                        .addComponent(jPanel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Architectural Patterns", jPanel14);

        jPanel21.setBackground(new java.awt.Color(255, 255, 255));

        jPanel23.setBackground(new java.awt.Color(245, 245, 245));
        jPanel23.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(61, 128, 185), 2, true), "Quality Attributes", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14), new java.awt.Color(238, 52, 0))); // NOI18N

        jListQualityAttributes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane18.setViewportView(jListQualityAttributes);

        jLabel23.setText("New Quality Attribute Name");

        jButtonRenameQualityAttribute.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRenameQualityAttribute.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRenameQualityAttribute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rename16x16.png"))); // NOI18N
        jButtonRenameQualityAttribute.setText("Rename");
        jButtonRenameQualityAttribute.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRenameQualityAttribute.setEnabled(false);
        jButtonRenameQualityAttribute.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRenameQualityAttribute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRenameQualityAttributeActionPerformed(evt);
            }
        });

        jButtonAddNewQualityAttribute.setBackground(new java.awt.Color(255, 255, 255));
        jButtonAddNewQualityAttribute.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonAddNewQualityAttribute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/add16x16.png"))); // NOI18N
        jButtonAddNewQualityAttribute.setText("Add");
        jButtonAddNewQualityAttribute.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonAddNewQualityAttribute.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonAddNewQualityAttribute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddNewQualityAttributeActionPerformed(evt);
            }
        });

        jButtonRemoveQualityAttribute.setBackground(new java.awt.Color(255, 255, 255));
        jButtonRemoveQualityAttribute.setFont(new java.awt.Font("Tahoma", 1, 11));
        jButtonRemoveQualityAttribute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete16x16.png"))); // NOI18N
        jButtonRemoveQualityAttribute.setText("Remove");
        jButtonRemoveQualityAttribute.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonRemoveQualityAttribute.setEnabled(false);
        jButtonRemoveQualityAttribute.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRemoveQualityAttribute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveQualityAttributeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane18, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonRemoveQualityAttribute)
                    .addComponent(jTextFieldNewQualityAttributeName, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
                    .addComponent(jLabel23)
                    .addComponent(jButtonAddNewQualityAttribute, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButtonRenameQualityAttribute))
                .addContainerGap())
        );
        jPanel23Layout.setVerticalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel23Layout.createSequentialGroup()
                        .addComponent(jLabel23)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldNewQualityAttributeName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonAddNewQualityAttribute)
                        .addGap(36, 36, 36)
                        .addComponent(jButtonRenameQualityAttribute)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveQualityAttribute))
                    .addComponent(jScrollPane18, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addGap(295, 295, 295)
                .addComponent(jPanel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(327, 327, 327))
        );
        jPanel21Layout.setVerticalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(212, 212, 212))
        );

        jTabbedPane1.addTab("Quality Attributes", jPanel21);

        jPanel25.setEnabled(false);
        jPanel25.setFocusable(false);

        javax.swing.GroupLayout jPanel25Layout = new javax.swing.GroupLayout(jPanel25);
        jPanel25.setLayout(jPanel25Layout);
        jPanel25Layout.setHorizontalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 974, Short.MAX_VALUE)
        );
        jPanel25Layout.setVerticalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Open Component Classification Console", jPanel25);

        jLabelSync.setFont(new java.awt.Font("Tahoma", 1, 12));
        jLabelSync.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabelSync.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelSyncMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabelSync, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 979, Short.MAX_VALUE)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 979, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelSync, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-1015)/2, (screenSize.height-709)/2, 1015, 709);
    }// </editor-fold>//GEN-END:initComponents

private void jButtonAddUsedByActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddUsedByActionPerformed
    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable() {

        @Override
        public void run() {
            ArrayList<String> alreadyAddedUsesComponents = new ArrayList<String>();
            for (ComponentRelationDataModel c : ((ComponentRelationTableModel) usedByTable.getModel()).getDataModel()) {
                alreadyAddedUsesComponents.add(c.getId());
            }
            alreadyAddedUsesComponents.add(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId());
            SelectComponent selectComponentDialog = new SelectComponent(
                    Management.this,
                    "usedBy",
                    languageList,
                    technologyList,
                    alreadyAddedUsesComponents);
            selectComponentDialog.pack();
            selectComponentDialog.setVisible(true);
        }
    });
}//GEN-LAST:event_jButtonAddUsedByActionPerformed

private void jButtonAddCallsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddCallsActionPerformed
    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable() {

        @Override
        public void run() {
            ArrayList<String> alreadyAddedUsesComponents = new ArrayList<String>();
            for (ComponentRelationDataModel c : ((ComponentRelationTableModel) callsTable.getModel()).getDataModel()) {
                alreadyAddedUsesComponents.add(c.getId());
            }
            alreadyAddedUsesComponents.add(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId());
            SelectComponent selectComponentDialog = new SelectComponent(
                    Management.this,
                    "calls",
                    languageList,
                    technologyList,
                    alreadyAddedUsesComponents);
            selectComponentDialog.pack();
            selectComponentDialog.setVisible(true);
        }
    });
}//GEN-LAST:event_jButtonAddCallsActionPerformed

private void jButtonAddCalledByActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddCalledByActionPerformed
    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable() {

        @Override
        public void run() {
            ArrayList<String> alreadyAddedUsesComponents = new ArrayList<String>();
            for (ComponentRelationDataModel c : ((ComponentRelationTableModel) calledByTable.getModel()).getDataModel()) {
                alreadyAddedUsesComponents.add(c.getId());
            }
            SelectComponent selectComponentDialog = new SelectComponent(
                    Management.this,
                    "calledBy",
                    languageList,
                    technologyList,
                    alreadyAddedUsesComponents);
            selectComponentDialog.pack();
            selectComponentDialog.setVisible(true);
        }
    });
}//GEN-LAST:event_jButtonAddCalledByActionPerformed

private void jButtonRemoveUsesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveUsesActionPerformed
    int n = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
    //yes -> 0, no -> 1
    if (n == 0) {
        int index = usesTable.getSelectedRow();
        if (index != -1) {
            int modelIndex = usesTable.convertRowIndexToModel(index);
            removeFromUses(((ComponentRelationTableModel) usesTable.getModel()).getDataAtRow(modelIndex), modelIndex, true);

        }
    }

}//GEN-LAST:event_jButtonRemoveUsesActionPerformed

private void jButtonRemoveUsedByActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveUsedByActionPerformed
    int n = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
    //yes -> 0, no -> 1
    if (n == 0) {
        int index = usedByTable.getSelectedRow();
        if (index != -1) {
            int modelIndex = usedByTable.convertRowIndexToModel(index);
            removeFromUsedBy(((ComponentRelationTableModel) usedByTable.getModel()).getDataAtRow(modelIndex), modelIndex, true);

        }
    }
}//GEN-LAST:event_jButtonRemoveUsedByActionPerformed

private void jButtonRemoveCallsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveCallsActionPerformed
    int n = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
    //yes -> 0, no -> 1
    if (n == 0) {
        int index = callsTable.getSelectedRow();
        if (index != -1) {
            int modelIndex = callsTable.convertRowIndexToModel(index);
            removeFromCalls(((ComponentRelationTableModel) callsTable.getModel()).getDataAtRow(modelIndex), modelIndex, true);

        }
    }
}//GEN-LAST:event_jButtonRemoveCallsActionPerformed

private void jButtonRemoveCalledByActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveCalledByActionPerformed
    int n = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
    //yes -> 0, no -> 1
    if (n == 0) {
        int index = calledByTable.getSelectedRow();
        if (index != -1) {
            int modelIndex = calledByTable.convertRowIndexToModel(index);
            removeFromCalledBy(((ComponentRelationTableModel) calledByTable.getModel()).getDataAtRow(modelIndex), modelIndex, true);

        }
    }
}//GEN-LAST:event_jButtonRemoveCalledByActionPerformed

private void jButtonAddUsesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddUsesActionPerformed
    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable() {

        @Override
        public void run() {
            ArrayList<String> alreadyAddedUsesComponents = new ArrayList<String>();
            for (ComponentRelationDataModel c : ((ComponentRelationTableModel) usesTable.getModel()).getDataModel()) {
                alreadyAddedUsesComponents.add(c.getId());
            }
            alreadyAddedUsesComponents.add(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId());
            SelectComponent selectComponentDialog = new SelectComponent(
                    Management.this,
                    "uses",
                    languageList,
                    technologyList,
                    alreadyAddedUsesComponents);
            selectComponentDialog.pack();
            selectComponentDialog.setVisible(true);


        }
    });

}//GEN-LAST:event_jButtonAddUsesActionPerformed

private void jButtonSetVersionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSetVersionActionPerformed
    TreeNodeData selected = (TreeNodeData) jTree1.getSelectionPath().getLastPathComponent();
    if (!jTextFieldVersion.getText().trim().isEmpty()) {
        this.setVersion(selected.getId(), jTextFieldVersion.getText().trim(), true);
    } else {
        this.removeVersion(selected.getId(), true);
    }
}//GEN-LAST:event_jButtonSetVersionActionPerformed

private void jComboBoxTechnologyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTechnologyActionPerformed

    if (!fromLoading) {
        KeyValue selected = (KeyValue) jComboBoxTechnology.getSelectedItem();
        if (selected == null) {
            return;
        }
        if (selected.getKey().equals("-1")) {
            removeTechnologyFromComponent(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), true);
        } else {
            if (!fromLoading) {
                setTechnology(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), selected.getKey(), true);
            }
        }
    }
}//GEN-LAST:event_jComboBoxTechnologyActionPerformed

private void jButtonRemoveSelectedLanguageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveSelectedLanguageActionPerformed
    int n = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
    //yes -> 0, no -> 1
    if (n == 0) {
        int index = jListLanguages.getSelectedIndex();

        if (index != -1) {
            String langID = ((KeyValue) jListLanguages.getModel().getElementAt(index)).getKey();
            removeLanguage(langID, index, true);
        }
    }
}//GEN-LAST:event_jButtonRemoveSelectedLanguageActionPerformed

private void jButtonAddNewLanguageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddNewLanguageActionPerformed
    if (jTextFieldNewLanguage.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(Management.this, "Give a name", "Error", JOptionPane.ERROR_MESSAGE);
    } else {
        if (((SortedListKeyValueModel) jListLanguages.getModel()).containsValue(jTextFieldNewLanguage.getText().trim())) {
            JOptionPane.showMessageDialog(Management.this, "Language already exists", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            addLanguage(jTextFieldNewLanguage.getText().trim(), true);
            jTextFieldNewLanguage.setText("");
        }
    }
}//GEN-LAST:event_jButtonAddNewLanguageActionPerformed

private void jButtonRemoveSelectedTechnologyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveSelectedTechnologyActionPerformed
    int n = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
    //yes -> 0, no -> 1
    if (n == 0) {
        int index = jListTechnologies.getSelectedIndex();

        if (index != -1) {
            String techID = ((KeyValue) jListTechnologies.getModel().getElementAt(index)).getKey();
            removeTechnology(techID, index, true);
        }
    }
}//GEN-LAST:event_jButtonRemoveSelectedTechnologyActionPerformed

private void jButtonAddNewTechnologyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddNewTechnologyActionPerformed
    if (jTextFieldNewTechnology.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(Management.this, "Give a name", "Error", JOptionPane.ERROR_MESSAGE);
    } else {
        if (((SortedListKeyValueModel) jListTechnologies.getModel()).containsValue(jTextFieldNewTechnology.getText().trim())) {
            JOptionPane.showMessageDialog(Management.this, "Technology already exists", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            addTechnology(jTextFieldNewTechnology.getText().trim(), true);
            jTextFieldNewTechnology.setText("");
        }
    }
}//GEN-LAST:event_jButtonAddNewTechnologyActionPerformed

private void jButtonRenameSelectedTechnologyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRenameSelectedTechnologyActionPerformed
    int index = jListTechnologies.getSelectedIndex();
    if (index != -1) {
        String techID = ((KeyValue) jListTechnologies.getModel().getElementAt(index)).getKey();
        String techName = ((KeyValue) jListTechnologies.getModel().getElementAt(index)).getValue();
        String name = (String) JOptionPane.showInputDialog(Management.this, "Give the new name", "Rename Technology", JOptionPane.PLAIN_MESSAGE, null, null, techName);
        while (name != null && name.trim().length() == 0) {
            name = (String) JOptionPane.showInputDialog(Management.this, "Give the new name", "Rename Technology", JOptionPane.PLAIN_MESSAGE, null, null, null);
        }

        if (name != null) {
            if (((SortedListKeyValueModel) jListTechnologies.getModel()).containsValue(name)) {
                JOptionPane.showMessageDialog(Management.this, "Technology already exists", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                renameTechnology(techID, name, index, true);
            }
        }
    }
}//GEN-LAST:event_jButtonRenameSelectedTechnologyActionPerformed

private void jButtonRenameSelectedLanguageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRenameSelectedLanguageActionPerformed
    int index = jListLanguages.getSelectedIndex();
    if (index != -1) {
        String langID = ((KeyValue) jListLanguages.getModel().getElementAt(index)).getKey();
        String langName = ((KeyValue) jListLanguages.getModel().getElementAt(index)).getValue();
        String name = (String) JOptionPane.showInputDialog(Management.this, "Give the new name", "Rename Language", JOptionPane.PLAIN_MESSAGE, null, null, langName);
        while (name != null && name.trim().length() == 0) {
            name = (String) JOptionPane.showInputDialog(Management.this, "Give the new name", "Rename Language", JOptionPane.PLAIN_MESSAGE, null, null, null);
        }

        if (name != null) {
            if (((SortedListKeyValueModel) jListLanguages.getModel()).containsValue(name)) {
                JOptionPane.showMessageDialog(Management.this, "Language already exists", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                renameLanguage(langID, name, index, true);
            }
        }
    }
}//GEN-LAST:event_jButtonRenameSelectedLanguageActionPerformed

private void jComboBoxLanguageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxLanguageActionPerformed
    if (!fromLoading) {
        KeyValue selected = (KeyValue) jComboBoxLanguage.getSelectedItem();
        if (selected == null) {
            return;
        }
        if (selected.getKey().equals("-1")) {
            removeLanguageFromComponent(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), true, isNormalPath(jTree1.getSelectionPath()));
        } else {
            if (!fromLoading) {
                setLanguage(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), selected.getKey(), true, isNormalPath(jTree1.getSelectionPath()));
            }
        }
    }
}//GEN-LAST:event_jComboBoxLanguageActionPerformed

private void jButtonAddProvidedInterfaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddProvidedInterfaceActionPerformed

    interfaceAddPanel.jTextFieldName.setText("");
    interfaceAddPanel.jTextFieldVersion.setText("");
    int answer = JOptionPane.showConfirmDialog(this, interfaceAddPanel, "Enter Interface Attributes", JOptionPane.OK_CANCEL_OPTION);

    while (answer == JOptionPane.OK_OPTION
            && (interfaceAddPanel.jTextFieldName.getText().trim().isEmpty()
            || interfaceAddPanel.jTextFieldVersion.getText().trim().isEmpty())) {
        answer = JOptionPane.showConfirmDialog(this, interfaceAddPanel, "Enter Interface Attributes", JOptionPane.OK_CANCEL_OPTION);
    }
    if (answer != JOptionPane.CANCEL_OPTION && answer != JOptionPane.CLOSED_OPTION) {
        addProvidedInterfaceToComponent(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(),
                interfaceAddPanel.jTextFieldName.getText().trim(), interfaceAddPanel.jTextFieldVersion.getText().trim(), true);
    }

}//GEN-LAST:event_jButtonAddProvidedInterfaceActionPerformed

private void jButtonRemoveProvidedInterfaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveProvidedInterfaceActionPerformed
    int n = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
    //yes -> 0, no -> 1
    if (n == 0) {
        int row = providedInterfaceTable.getSelectedRow();
        if (row != -1) {
            int modelRow = providedInterfaceTable.convertRowIndexToModel(row);
            String interfaceID = ((InterfaceTableModel) providedInterfaceTable.getModel()).getDataAtRow(modelRow).getId();
            String componentID = ((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId();
            removeProvidedInterfaceFromComponent(componentID, interfaceID, true);
        }
    }
}//GEN-LAST:event_jButtonRemoveProvidedInterfaceActionPerformed

private void jButtonEditProvidedInterfaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditProvidedInterfaceActionPerformed
    int row = providedInterfaceTable.getSelectedRow();
    if (row != -1) {
        int modelRow = providedInterfaceTable.convertRowIndexToModel(row);
        InterfaceDataModel data = ((InterfaceTableModel) providedInterfaceTable.getModel()).getDataAtRow(modelRow);
        interfaceAddPanel.jTextFieldName.setText(data.getInterfaceName());
        interfaceAddPanel.jTextFieldVersion.setText(data.getVersion());
        int answer = JOptionPane.showConfirmDialog(this, interfaceAddPanel, "Edit Interface Attributes", JOptionPane.OK_CANCEL_OPTION);

        while (answer == JOptionPane.OK_OPTION
                && (interfaceAddPanel.jTextFieldName.getText().trim().isEmpty()
                || interfaceAddPanel.jTextFieldVersion.getText().trim().isEmpty())) {
            answer = JOptionPane.showConfirmDialog(this, interfaceAddPanel, "Enter Interface Attributes", JOptionPane.OK_CANCEL_OPTION);
        }
        if (answer != JOptionPane.CANCEL_OPTION && answer != JOptionPane.CLOSED_OPTION) {
            editProvidedInterface(
                    data.getId(),
                    interfaceAddPanel.jTextFieldName.getText().trim(),
                    interfaceAddPanel.jTextFieldVersion.getText().trim(), true);
        }
    }
}//GEN-LAST:event_jButtonEditProvidedInterfaceActionPerformed

private void jButtonAddProvidedInterfaceMethodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddProvidedInterfaceMethodActionPerformed
    if (providedInterfaceTable.getSelectedRow() != -1) {
        interfaceMethodAddPanel.jTextFieldName.setText("");
        interfaceMethodAddPanel.jTextFieldParameters.setText("");
        interfaceMethodAddPanel.jTextFieldReturns.setText("");
        interfaceMethodAddPanel.jTextFieldThrows.setText("");
        int answer = JOptionPane.showConfirmDialog(this, interfaceMethodAddPanel, "Enter Method Attributes", JOptionPane.OK_CANCEL_OPTION);

        while (answer == JOptionPane.OK_OPTION
                && (interfaceMethodAddPanel.jTextFieldName.getText().trim().isEmpty()
                || interfaceMethodAddPanel.jTextFieldParameters.getText().trim().isEmpty()
                || interfaceMethodAddPanel.jTextFieldReturns.getText().trim().isEmpty()
                || interfaceMethodAddPanel.jTextFieldThrows.getText().trim().isEmpty())) {
            answer = JOptionPane.showConfirmDialog(this, interfaceMethodAddPanel, "Enter Method Attributes", JOptionPane.OK_CANCEL_OPTION);
        }
        if (answer != JOptionPane.CANCEL_OPTION && answer != JOptionPane.CLOSED_OPTION) {
            int modelView = providedInterfaceTable.convertRowIndexToModel(providedInterfaceTable.getSelectedRow());
            InterfaceDataModel dataAtRow = ((InterfaceTableModel) providedInterfaceTable.getModel()).getDataAtRow(modelView);
            addMethodToProvidedInterface(
                    dataAtRow.getId(),
                    interfaceMethodAddPanel.jTextFieldName.getText().trim(),
                    interfaceMethodAddPanel.jTextFieldParameters.getText().trim(),
                    interfaceMethodAddPanel.jTextFieldReturns.getText().trim(),
                    interfaceMethodAddPanel.jTextFieldThrows.getText().trim(),
                    true);
        }
    }
}//GEN-LAST:event_jButtonAddProvidedInterfaceMethodActionPerformed

private void jButtonRemoveProvidedInterfaceMethodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveProvidedInterfaceMethodActionPerformed
    int n = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
    //yes -> 0, no -> 1
    if (n == 0) {
        int row = providedInterfaceMethodsTable.getSelectedRow();
        if (row != -1) {
            int modelRow = providedInterfaceMethodsTable.convertRowIndexToModel(row);
            String methodID = ((MethodTableModel) providedInterfaceMethodsTable.getModel()).getDataAtRow(modelRow).getId();
            int interfaceRow = providedInterfaceTable.getSelectedRow();
            if (interfaceRow != -1) {
                int interfaceModelRow = providedInterfaceTable.convertRowIndexToModel(interfaceRow);
                String interfaceID = ((InterfaceTableModel) providedInterfaceTable.getModel()).getDataAtRow(interfaceModelRow).getId();
                removeProvidedMethodFromInterface(interfaceID, methodID, true);
            }
        }
    }
}//GEN-LAST:event_jButtonRemoveProvidedInterfaceMethodActionPerformed

private void jButtonEditProvidedInterfaceMethodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditProvidedInterfaceMethodActionPerformed
    int row = providedInterfaceMethodsTable.getSelectedRow();
    if (row != -1) {
        int modelRow = providedInterfaceMethodsTable.convertRowIndexToModel(row);
        MethodDataModel data = ((MethodTableModel) providedInterfaceMethodsTable.getModel()).getDataAtRow(modelRow);

        interfaceMethodAddPanel.jTextFieldName.setText(data.getName());
        interfaceMethodAddPanel.jTextFieldParameters.setText(data.getParameters());
        interfaceMethodAddPanel.jTextFieldReturns.setText(data.getReturns());
        interfaceMethodAddPanel.jTextFieldThrows.setText(data.getThrows());
        int answer = JOptionPane.showConfirmDialog(this, interfaceMethodAddPanel, "Edit Method Attributes", JOptionPane.OK_CANCEL_OPTION);

        while (answer == JOptionPane.OK_OPTION
                && (interfaceMethodAddPanel.jTextFieldName.getText().trim().isEmpty()
                || interfaceMethodAddPanel.jTextFieldParameters.getText().trim().isEmpty()
                || interfaceMethodAddPanel.jTextFieldReturns.getText().trim().isEmpty()
                || interfaceMethodAddPanel.jTextFieldThrows.getText().trim().isEmpty())) {
            answer = JOptionPane.showConfirmDialog(this, interfaceMethodAddPanel, "Edit Method Attributes", JOptionPane.OK_CANCEL_OPTION);
        }
        if (answer != JOptionPane.CANCEL_OPTION && answer != JOptionPane.CLOSED_OPTION) {
            editProvidedMethod(
                    data.getId(),
                    interfaceMethodAddPanel.jTextFieldName.getText().trim(),
                    interfaceMethodAddPanel.jTextFieldParameters.getText().trim(),
                    interfaceMethodAddPanel.jTextFieldReturns.getText().trim(),
                    interfaceMethodAddPanel.jTextFieldThrows.getText().trim(),
                    true);
        }

    }
}//GEN-LAST:event_jButtonEditProvidedInterfaceMethodActionPerformed

private void jButtonAddRequiredInterfaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddRequiredInterfaceActionPerformed
    interfaceAddPanel.jTextFieldName.setText("");
    interfaceAddPanel.jTextFieldVersion.setText("");
    int answer = JOptionPane.showConfirmDialog(this, interfaceAddPanel, "Enter Interface Attributes", JOptionPane.OK_CANCEL_OPTION);

    while (answer == JOptionPane.OK_OPTION
            && (interfaceAddPanel.jTextFieldName.getText().trim().isEmpty()
            || interfaceAddPanel.jTextFieldVersion.getText().trim().isEmpty())) {
        answer = JOptionPane.showConfirmDialog(this, interfaceAddPanel, "Enter Interface Attributes", JOptionPane.OK_CANCEL_OPTION);
    }
    if (answer != JOptionPane.CANCEL_OPTION && answer != JOptionPane.CLOSED_OPTION) {
        addRequiredInterfaceToComponent(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(),
                interfaceAddPanel.jTextFieldName.getText().trim(), interfaceAddPanel.jTextFieldVersion.getText().trim(), true);
    }
}//GEN-LAST:event_jButtonAddRequiredInterfaceActionPerformed

private void jButtonRemoveRequiredInterfaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveRequiredInterfaceActionPerformed
    int n = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
    //yes -> 0, no -> 1
    if (n == 0) {
        int row = requiredInterfaceTable.getSelectedRow();
        if (row != -1) {
            int modelRow = requiredInterfaceTable.convertRowIndexToModel(row);
            String interfaceID = ((InterfaceTableModel) requiredInterfaceTable.getModel()).getDataAtRow(modelRow).getId();
            String componentID = ((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId();
            removeRequiredInterfaceFromComponent(componentID, interfaceID, true);
        }
    }
}//GEN-LAST:event_jButtonRemoveRequiredInterfaceActionPerformed

private void jButtonEditRequiredInterfaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditRequiredInterfaceActionPerformed
    int row = requiredInterfaceTable.getSelectedRow();
    if (row != -1) {
        int modelRow = requiredInterfaceTable.convertRowIndexToModel(row);
        InterfaceDataModel data = ((InterfaceTableModel) requiredInterfaceTable.getModel()).getDataAtRow(modelRow);
        interfaceAddPanel.jTextFieldName.setText(data.getInterfaceName());
        interfaceAddPanel.jTextFieldVersion.setText(data.getVersion());
        int answer = JOptionPane.showConfirmDialog(this, interfaceAddPanel, "Edit Interface Attributes", JOptionPane.OK_CANCEL_OPTION);

        while (answer == JOptionPane.OK_OPTION
                && (interfaceAddPanel.jTextFieldName.getText().trim().isEmpty()
                || interfaceAddPanel.jTextFieldVersion.getText().trim().isEmpty())) {
            answer = JOptionPane.showConfirmDialog(this, interfaceAddPanel, "Enter Interface Attributes", JOptionPane.OK_CANCEL_OPTION);
        }
        if (answer != JOptionPane.CANCEL_OPTION && answer != JOptionPane.CLOSED_OPTION) {
            editRequiredInterface(
                    data.getId(),
                    interfaceAddPanel.jTextFieldName.getText().trim(),
                    interfaceAddPanel.jTextFieldVersion.getText().trim(), true);
        }
    }
}//GEN-LAST:event_jButtonEditRequiredInterfaceActionPerformed

private void jButtonAddRequiredInterfaceMethodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddRequiredInterfaceMethodActionPerformed
    if (requiredInterfaceTable.getSelectedRow() != -1) {
        interfaceMethodAddPanel.jTextFieldName.setText("");
        interfaceMethodAddPanel.jTextFieldParameters.setText("");
        interfaceMethodAddPanel.jTextFieldReturns.setText("");
        interfaceMethodAddPanel.jTextFieldThrows.setText("");
        int answer = JOptionPane.showConfirmDialog(this, interfaceMethodAddPanel, "Enter Method Attributes", JOptionPane.OK_CANCEL_OPTION);

        while (answer == JOptionPane.OK_OPTION
                && (interfaceMethodAddPanel.jTextFieldName.getText().trim().isEmpty()
                || interfaceMethodAddPanel.jTextFieldParameters.getText().trim().isEmpty()
                || interfaceMethodAddPanel.jTextFieldReturns.getText().trim().isEmpty()
                || interfaceMethodAddPanel.jTextFieldThrows.getText().trim().isEmpty())) {
            answer = JOptionPane.showConfirmDialog(this, interfaceMethodAddPanel, "Enter Method Attributes", JOptionPane.OK_CANCEL_OPTION);
        }
        if (answer != JOptionPane.CANCEL_OPTION && answer != JOptionPane.CLOSED_OPTION) {
            int modelView = requiredInterfaceTable.convertRowIndexToModel(requiredInterfaceTable.getSelectedRow());
            InterfaceDataModel dataAtRow = ((InterfaceTableModel) requiredInterfaceTable.getModel()).getDataAtRow(modelView);
            addMethodToRequiredInterface(
                    dataAtRow.getId(),
                    interfaceMethodAddPanel.jTextFieldName.getText().trim(),
                    interfaceMethodAddPanel.jTextFieldParameters.getText().trim(),
                    interfaceMethodAddPanel.jTextFieldReturns.getText().trim(),
                    interfaceMethodAddPanel.jTextFieldThrows.getText().trim(),
                    true);
        }
    }
}//GEN-LAST:event_jButtonAddRequiredInterfaceMethodActionPerformed

private void jButtonRemoveRequiredInterfaceMethodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveRequiredInterfaceMethodActionPerformed
    int n = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
    //yes -> 0, no -> 1
    if (n == 0) {
        int row = requiredInterfaceMethodsTable.getSelectedRow();
        if (row != -1) {
            int modelRow = requiredInterfaceMethodsTable.convertRowIndexToModel(row);
            String methodID = ((MethodTableModel) requiredInterfaceMethodsTable.getModel()).getDataAtRow(modelRow).getId();
            int interfaceRow = requiredInterfaceTable.getSelectedRow();
            if (interfaceRow != -1) {
                int interfaceModelRow = requiredInterfaceTable.convertRowIndexToModel(interfaceRow);
                String interfaceID = ((InterfaceTableModel) requiredInterfaceTable.getModel()).getDataAtRow(interfaceModelRow).getId();
                removeRequiredMethodFromInterface(interfaceID, methodID, true);
            }
        }
    }
}//GEN-LAST:event_jButtonRemoveRequiredInterfaceMethodActionPerformed

private void jButtonEditRequiredInterfaceMethodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditRequiredInterfaceMethodActionPerformed
    int row = requiredInterfaceMethodsTable.getSelectedRow();
    if (row != -1) {
        int modelRow = requiredInterfaceMethodsTable.convertRowIndexToModel(row);
        MethodDataModel data = ((MethodTableModel) requiredInterfaceMethodsTable.getModel()).getDataAtRow(modelRow);

        interfaceMethodAddPanel.jTextFieldName.setText(data.getName());
        interfaceMethodAddPanel.jTextFieldParameters.setText(data.getParameters());
        interfaceMethodAddPanel.jTextFieldReturns.setText(data.getReturns());
        interfaceMethodAddPanel.jTextFieldThrows.setText(data.getThrows());
        int answer = JOptionPane.showConfirmDialog(this, interfaceMethodAddPanel, "Edit Method Attributes", JOptionPane.OK_CANCEL_OPTION);

        while (answer == JOptionPane.OK_OPTION
                && (interfaceMethodAddPanel.jTextFieldName.getText().trim().isEmpty()
                || interfaceMethodAddPanel.jTextFieldParameters.getText().trim().isEmpty()
                || interfaceMethodAddPanel.jTextFieldReturns.getText().trim().isEmpty()
                || interfaceMethodAddPanel.jTextFieldThrows.getText().trim().isEmpty())) {
            answer = JOptionPane.showConfirmDialog(this, interfaceMethodAddPanel, "Edit Method Attributes", JOptionPane.OK_CANCEL_OPTION);
        }
        if (answer != JOptionPane.CANCEL_OPTION && answer != JOptionPane.CLOSED_OPTION) {
            editRequiredMethod(
                    data.getId(),
                    interfaceMethodAddPanel.jTextFieldName.getText().trim(),
                    interfaceMethodAddPanel.jTextFieldParameters.getText().trim(),
                    interfaceMethodAddPanel.jTextFieldReturns.getText().trim(),
                    interfaceMethodAddPanel.jTextFieldThrows.getText().trim(),
                    true);
        }
    }
}//GEN-LAST:event_jButtonEditRequiredInterfaceMethodActionPerformed

private void jButtonAddNewArchitecturalPatternActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddNewArchitecturalPatternActionPerformed
    if (jTextFieldNewArchitecturalPattern.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(Management.this, "Give a name", "Error", JOptionPane.ERROR_MESSAGE);
    } else {
        if (((SortedListKeyValueModel) jListArchitecturalPatterns.getModel()).containsValue(jTextFieldNewArchitecturalPattern.getText().trim())) {
            JOptionPane.showMessageDialog(Management.this, "Architectural pattern already exists", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            addArchitecturalPattern(jTextFieldNewArchitecturalPattern.getText().trim(), true);
            jTextFieldNewArchitecturalPattern.setText("");
        }
    }
}//GEN-LAST:event_jButtonAddNewArchitecturalPatternActionPerformed

private void jButtonRenameSelectedArchitecturalPatternActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRenameSelectedArchitecturalPatternActionPerformed
    int index = jListArchitecturalPatterns.getSelectedIndex();
    if (index != -1) {
        String patternID = ((KeyValue) jListArchitecturalPatterns.getModel().getElementAt(index)).getKey();
        String patternName = ((KeyValue) jListArchitecturalPatterns.getModel().getElementAt(index)).getValue();
        String name = (String) JOptionPane.showInputDialog(Management.this, "Give the new name", "Rename Architectural Pattern", JOptionPane.PLAIN_MESSAGE, null, null, patternName);
        while (name != null && name.trim().length() == 0) {
            name = (String) JOptionPane.showInputDialog(Management.this, "Give the new name", "Rename Architectural Pattern", JOptionPane.PLAIN_MESSAGE, null, null, null);
        }

        if (name != null) {
            if (((SortedListKeyValueModel) jListArchitecturalPatterns.getModel()).containsValue(name)) {
                JOptionPane.showMessageDialog(Management.this, "Architectural pattern already exists", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                renameArchitecturalPattern(patternID, name, index, true);
            }
        }
    }
}//GEN-LAST:event_jButtonRenameSelectedArchitecturalPatternActionPerformed

private void jButtonRemoveSelectedArchitecturalPatternActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveSelectedArchitecturalPatternActionPerformed
    int n = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
    //yes -> 0, no -> 1
    if (n == 0) {
        int index = jListArchitecturalPatterns.getSelectedIndex();

        if (index != -1) {
            String patternID = ((KeyValue) jListArchitecturalPatterns.getModel().getElementAt(index)).getKey();
            removeArchitecturalPattern(patternID, index, true);
        }
    }
}//GEN-LAST:event_jButtonRemoveSelectedArchitecturalPatternActionPerformed

private void jButtonSelectHighQualityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectHighQualityActionPerformed

    attributeSelectPanel.setModel(qualityAttributesList, highQualityList, mediumQualityList, lowQualityList);
    int answer = JOptionPane.showConfirmDialog(this, attributeSelectPanel, "Select Quality Attributes", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);

    while (answer == JOptionPane.OK_OPTION
            && attributeSelectPanel.jListQualityAttributeList.getSelectedValues().length == 0) {
        answer = JOptionPane.showConfirmDialog(this, attributeSelectPanel, "Select Quality Attributes", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
    }
    if (answer != JOptionPane.CANCEL_OPTION && answer != JOptionPane.CLOSED_OPTION) {
        addSelectedHighQualityAttributes(((KeyValue) jListArchitecturalPatterns.getSelectedValue()).getKey(),
                attributeSelectPanel.jListQualityAttributeList.getSelectedValues(), true);
    }
}//GEN-LAST:event_jButtonSelectHighQualityActionPerformed

private void jButtonRemoveHighQualityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveHighQualityActionPerformed
    int n = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
    //yes -> 0, no -> 1
    if (n == 0) {
        Object[] selectedValues = jListHighQuality.getSelectedValues();
        if (selectedValues.length != 0) {
            String patternID = ((KeyValue) jListArchitecturalPatterns.getSelectedValue()).getKey();
            removeHighQualityFromPattern(patternID, selectedValues, true);
        }
    }
}//GEN-LAST:event_jButtonRemoveHighQualityActionPerformed

private void jButtonSelectMediumQualityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectMediumQualityActionPerformed
    attributeSelectPanel.setModel(qualityAttributesList, highQualityList, mediumQualityList, lowQualityList);
    int answer = JOptionPane.showConfirmDialog(this, attributeSelectPanel, "Select Quality Attributes", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);

    while (answer == JOptionPane.OK_OPTION
            && attributeSelectPanel.jListQualityAttributeList.getSelectedValues().length == 0) {
        answer = JOptionPane.showConfirmDialog(this, attributeSelectPanel, "Select Quality Attributes", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
    }
    if (answer != JOptionPane.CANCEL_OPTION && answer != JOptionPane.CLOSED_OPTION) {
        addSelectedMediumQualityAttributes(((KeyValue) jListArchitecturalPatterns.getSelectedValue()).getKey(),
                attributeSelectPanel.jListQualityAttributeList.getSelectedValues(), true);
    }
}//GEN-LAST:event_jButtonSelectMediumQualityActionPerformed

private void jButtonRemoveMediumQualityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveMediumQualityActionPerformed
    int n = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
    //yes -> 0, no -> 1
    if (n == 0) {
        Object[] selectedValues = jListMediumQuality.getSelectedValues();
        if (selectedValues.length != 0) {
            String patternID = ((KeyValue) jListArchitecturalPatterns.getSelectedValue()).getKey();
            removeMediumQualityFromPattern(patternID, selectedValues, true);
        }
    }
}//GEN-LAST:event_jButtonRemoveMediumQualityActionPerformed

private void jButtonSelectLowQualityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectLowQualityActionPerformed
    attributeSelectPanel.setModel(qualityAttributesList, highQualityList, mediumQualityList, lowQualityList);
    int answer = JOptionPane.showConfirmDialog(this, attributeSelectPanel, "Select Quality Attributes", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);

    while (answer == JOptionPane.OK_OPTION
            && attributeSelectPanel.jListQualityAttributeList.getSelectedValues().length == 0) {
        answer = JOptionPane.showConfirmDialog(this, attributeSelectPanel, "Select Quality Attributes", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
    }
    if (answer != JOptionPane.CANCEL_OPTION && answer != JOptionPane.CLOSED_OPTION) {
        addSelectedLowQualityAttributes(((KeyValue) jListArchitecturalPatterns.getSelectedValue()).getKey(),
                attributeSelectPanel.jListQualityAttributeList.getSelectedValues(), true);
    }
}//GEN-LAST:event_jButtonSelectLowQualityActionPerformed

private void jButtonRemoveLowQualityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveLowQualityActionPerformed
    int n = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
    //yes -> 0, no -> 1
    if (n == 0) {
        Object[] selectedValues = jListLowQuality.getSelectedValues();
        if (selectedValues.length != 0) {
            String patternID = ((KeyValue) jListArchitecturalPatterns.getSelectedValue()).getKey();
            removeLowQualityFromPattern(patternID, selectedValues, true);
        }
    }
}//GEN-LAST:event_jButtonRemoveLowQualityActionPerformed

private void jButtonRenameQualityAttributeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRenameQualityAttributeActionPerformed
    int index = jListQualityAttributes.getSelectedIndex();
    if (index != -1) {
        String qualityID = ((KeyValue) jListQualityAttributes.getModel().getElementAt(index)).getKey();
        String qualityName = ((KeyValue) jListQualityAttributes.getModel().getElementAt(index)).getValue();
        String name = (String) JOptionPane.showInputDialog(Management.this, "Give the new name", "Rename Quality Attribute", JOptionPane.PLAIN_MESSAGE, null, null, qualityName);
        while (name != null && name.trim().length() == 0) {
            name = (String) JOptionPane.showInputDialog(Management.this, "Give the new name", "Rename Quality Attribute", JOptionPane.PLAIN_MESSAGE, null, null, null);
        }

        if (name != null) {
            if (qualityAttributesList.containsValue(name)) {
                JOptionPane.showMessageDialog(Management.this, "Quality attribute already exists", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                renameQualityAttribute(qualityID, name, index, true);
            }
        }
    }
}//GEN-LAST:event_jButtonRenameQualityAttributeActionPerformed

private void jButtonAddNewQualityAttributeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddNewQualityAttributeActionPerformed
    if (jTextFieldNewQualityAttributeName.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(Management.this, "Give a name", "Error", JOptionPane.ERROR_MESSAGE);
    } else {
        if (qualityAttributesList.containsValue(jTextFieldNewQualityAttributeName.getText().trim())) {
            JOptionPane.showMessageDialog(Management.this, "Quality attribute already exists", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            addQualityAttribute(jTextFieldNewQualityAttributeName.getText().trim(), true);
            jTextFieldNewQualityAttributeName.setText("");
        }
    }
}//GEN-LAST:event_jButtonAddNewQualityAttributeActionPerformed

private void jButtonRemoveQualityAttributeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveQualityAttributeActionPerformed
    int n = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
    //yes -> 0, no -> 1
    if (n == 0) {
        int index = jListQualityAttributes.getSelectedIndex();
        if (index != -1) {
            String attrID = ((KeyValue) jListQualityAttributes.getModel().getElementAt(index)).getKey();
            removeQualityAttribute(attrID, index, true);
        }
    }
}//GEN-LAST:event_jButtonRemoveQualityAttributeActionPerformed

private void jButtonRenameSelectedPatternRoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRenameSelectedPatternRoleActionPerformed
    int index = jListPatternRoles.getSelectedIndex();
    if (index != -1) {
        String roleID = ((KeyValue) jListPatternRoles.getModel().getElementAt(index)).getKey();
        String roleName = ((KeyValue) jListPatternRoles.getModel().getElementAt(index)).getValue();
        String name = (String) JOptionPane.showInputDialog(Management.this, "Give the new name", "Rename Role", JOptionPane.PLAIN_MESSAGE, null, null, roleName);
        while (name != null && name.trim().length() == 0) {
            name = (String) JOptionPane.showInputDialog(Management.this, "Give the new name", "Rename Role", JOptionPane.PLAIN_MESSAGE, null, null, null);
        }

        if (name != null) {
            if (patternRolesList.containsValue(name)) {
                JOptionPane.showMessageDialog(Management.this, "This role already exists", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                renameRole(roleID, name, index, true);
            }
        }
    }
}//GEN-LAST:event_jButtonRenameSelectedPatternRoleActionPerformed

private void jButtonAddNewPatternRoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddNewPatternRoleActionPerformed
    if (jTextFieldNewPatternRole.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(Management.this, "Give a name", "Error", JOptionPane.ERROR_MESSAGE);
    } else {
        if (patternRolesList.containsValue(jTextFieldNewPatternRole.getText().trim())) {
            JOptionPane.showMessageDialog(Management.this, "This role already exists", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            String patternID = ((KeyValue) jListArchitecturalPatterns.getSelectedValue()).getKey();
            addRoleToPattern(patternID, jTextFieldNewPatternRole.getText().trim(), true);
            jTextFieldNewPatternRole.setText("");
        }

    }
}//GEN-LAST:event_jButtonAddNewPatternRoleActionPerformed

private void jButtonRemoveSelectedPatternRoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveSelectedPatternRoleActionPerformed
    int n = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
    //yes -> 0, no -> 1
    if (n == 0) {
        int index = jListPatternRoles.getSelectedIndex();
        if (index != -1) {
            Object[] rolesID = jListPatternRoles.getSelectedValues();
            String patternId = ((KeyValue) jListArchitecturalPatterns.getSelectedValue()).getKey();
            removeRolesFromPattern(patternId, rolesID, true);
        }
    }
}//GEN-LAST:event_jButtonRemoveSelectedPatternRoleActionPerformed

private void jButtonRenameSelectedBusinessComponentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRenameSelectedBusinessComponentActionPerformed
    int index = jListBusinessComponents.getSelectedIndex();
    if (index != -1) {
        String bcomponentID = ((KeyValue) jListBusinessComponents.getModel().getElementAt(index)).getKey();
        String bcomponentName = ((KeyValue) jListBusinessComponents.getModel().getElementAt(index)).getValue();
        String name = (String) JOptionPane.showInputDialog(Management.this, "Give the new name", "Rename Business Component", JOptionPane.PLAIN_MESSAGE, null, null, bcomponentName);
        while (name != null && name.trim().length() == 0) {
            name = (String) JOptionPane.showInputDialog(Management.this, "Give the new name", "Rename Business Component", JOptionPane.PLAIN_MESSAGE, null, null, null);
        }
        if (name != null) {
            if (((SortedListKeyValueModel) jListBusinessComponents.getModel()).containsValue(name)) {
                JOptionPane.showMessageDialog(Management.this, "Business component already exists", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                renameBusinessComponent(bcomponentID, name, index, true);
            }
        }
    }
}//GEN-LAST:event_jButtonRenameSelectedBusinessComponentActionPerformed

private void jButtonAddNewBusinessComponentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddNewBusinessComponentActionPerformed
    if (jTextFieldNewBusinessComponent.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(Management.this, "Give a name", "Error", JOptionPane.ERROR_MESSAGE);
    } else {
        if (((SortedListKeyValueModel) jListBusinessComponents.getModel()).containsValue(jTextFieldNewBusinessComponent.getText().trim())) {
            JOptionPane.showMessageDialog(Management.this, "Business component already exists", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            addBusinessComponent(jTextFieldNewBusinessComponent.getText().trim(), true);
            jTextFieldNewBusinessComponent.setText("");
        }
    }
}//GEN-LAST:event_jButtonAddNewBusinessComponentActionPerformed

private void jButtonRemoveSelectedBusinessComponentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveSelectedBusinessComponentActionPerformed

    int n = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
    //yes -> 0, no -> 1
    if (n == 0) {
        int index = jListBusinessComponents.getSelectedIndex();

        if (index != -1) {
            String bcomponentID = ((KeyValue) jListBusinessComponents.getModel().getElementAt(index)).getKey();
            removeBusinessComponent(bcomponentID, index, true);
        }
    }

}//GEN-LAST:event_jButtonRemoveSelectedBusinessComponentActionPerformed

private void jButtonSelectResourceTierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectResourceTierActionPerformed
    java.awt.EventQueue.invokeLater(new Runnable() {

        @Override
        public void run() {
            ArrayList<String> alreadyAddedResourceComponents = new ArrayList<String>();
            for (ComponentRelationDataModel c : ((ComponentRelationTableModel) resourceTierTable.getModel()).getDataModel()) {
                alreadyAddedResourceComponents.add(c.getId());
            }

            SelectForBComponent selectComponentDialog = new SelectForBComponent(
                    Management.this,
                    "resourceTier",
                    languageList,
                    technologyList,
                    alreadyAddedResourceComponents, "Resource");
            selectComponentDialog.pack();
            selectComponentDialog.setVisible(true);
        }
    });
}//GEN-LAST:event_jButtonSelectResourceTierActionPerformed

private void jButtonRemoveResourceTierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveResourceTierActionPerformed
    int n = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
    //yes -> 0, no -> 1
    if (n == 0) {
        int index = resourceTierTable.getSelectedRow();
        if (index != -1) {
            int modelIndex = resourceTierTable.convertRowIndexToModel(index);
            removeFromResource(((ComponentRelationTableModel) resourceTierTable.getModel()).getDataAtRow(modelIndex), modelIndex, true);

        }
    }
}//GEN-LAST:event_jButtonRemoveResourceTierActionPerformed

private void jButtonSelectEnterpriseTierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectEnterpriseTierActionPerformed
    java.awt.EventQueue.invokeLater(new Runnable() {

        @Override
        public void run() {
            ArrayList<String> alreadyAddedEnterpriseComponents = new ArrayList<String>();
            for (ComponentRelationDataModel c : ((ComponentRelationTableModel) enterpriseTierTable.getModel()).getDataModel()) {
                alreadyAddedEnterpriseComponents.add(c.getId());
            }

            SelectForBComponent selectComponentDialog = new SelectForBComponent(
                    Management.this,
                    "enterpriseTier",
                    languageList,
                    technologyList,
                    alreadyAddedEnterpriseComponents, "Enterprise");
            selectComponentDialog.pack();
            selectComponentDialog.setVisible(true);
        }
    });
}//GEN-LAST:event_jButtonSelectEnterpriseTierActionPerformed

private void jButtonRemoveEnterpriseTierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveEnterpriseTierActionPerformed


    int n = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
    //yes -> 0, no -> 1
    if (n == 0) {
        int index = enterpriseTierTable.getSelectedRow();
        if (index != -1) {
            int modelIndex = enterpriseTierTable.convertRowIndexToModel(index);
            removeFromEnterprise(((ComponentRelationTableModel) enterpriseTierTable.getModel()).getDataAtRow(modelIndex), modelIndex, true);

        }
    }
}//GEN-LAST:event_jButtonRemoveEnterpriseTierActionPerformed

private void jButtonSelectWorkspaceTierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectWorkspaceTierActionPerformed
    java.awt.EventQueue.invokeLater(new Runnable() {

        @Override
        public void run() {
            ArrayList<String> alreadyAddedWorkspaceComponents = new ArrayList<String>();
            for (ComponentRelationDataModel c : ((ComponentRelationTableModel) workspaceTierTable.getModel()).getDataModel()) {
                alreadyAddedWorkspaceComponents.add(c.getId());
            }

            SelectForBComponent selectComponentDialog = new SelectForBComponent(
                    Management.this,
                    "workspaceTier",
                    languageList,
                    technologyList,
                    alreadyAddedWorkspaceComponents, "Workspace");
            selectComponentDialog.pack();
            selectComponentDialog.setVisible(true);
        }
    });
}//GEN-LAST:event_jButtonSelectWorkspaceTierActionPerformed

private void jButtonRemoveWorkspaceTierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveWorkspaceTierActionPerformed
    int n = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
    //yes -> 0, no -> 1
    if (n == 0) {
        int index = workspaceTierTable.getSelectedRow();
        if (index != -1) {
            int modelIndex = workspaceTierTable.convertRowIndexToModel(index);
            removeFromWorkspace(((ComponentRelationTableModel) workspaceTierTable.getModel()).getDataAtRow(modelIndex), modelIndex, true);

        }
    }
}//GEN-LAST:event_jButtonRemoveWorkspaceTierActionPerformed

private void jButtonSelectUserTierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectUserTierActionPerformed
    java.awt.EventQueue.invokeLater(new Runnable() {

        @Override
        public void run() {
            ArrayList<String> alreadyAddedUserComponents = new ArrayList<String>();
            for (ComponentRelationDataModel c : ((ComponentRelationTableModel) userTierTable.getModel()).getDataModel()) {
                alreadyAddedUserComponents.add(c.getId());
            }

            SelectForBComponent selectComponentDialog = new SelectForBComponent(
                    Management.this,
                    "userTier",
                    languageList,
                    technologyList,
                    alreadyAddedUserComponents, "User");
            selectComponentDialog.pack();
            selectComponentDialog.setVisible(true);
        }
    });
}//GEN-LAST:event_jButtonSelectUserTierActionPerformed

private void jButtonRemoveUserTierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveUserTierActionPerformed
    int n = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
    //yes -> 0, no -> 1
    if (n == 0) {
        int index = userTierTable.getSelectedRow();
        if (index != -1) {
            int modelIndex = userTierTable.convertRowIndexToModel(index);
            removeFromUser(((ComponentRelationTableModel) userTierTable.getModel()).getDataAtRow(modelIndex), modelIndex, true);

        }
    }
}//GEN-LAST:event_jButtonRemoveUserTierActionPerformed

private void jButtonSelectPlaysRoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectPlaysRoleActionPerformed
//    roleSelectPanel.setModel(rolesList, bcomponentRoleList);
//    int answer = JOptionPane.showConfirmDialog(this, roleSelectPanel, "Select Roles", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
//
//    while (answer == JOptionPane.OK_OPTION
//            && roleSelectPanel.jListRoleSelectList.getSelectedValues().length == 0) {
//        answer = JOptionPane.showConfirmDialog(this, roleSelectPanel, "Select Roles", JOptionPane.OK_CANCEL_OPTION);
//    }
//    if (answer != JOptionPane.CANCEL_OPTION && answer != JOptionPane.CLOSED_OPTION) {
//        addSelectedPlayRoles(((KeyValue) jListBusinessComponents.getSelectedValue()).getKey(),
//                roleSelectPanel.jListRoleSelectList.getSelectedValues(), true);
//    }


    ArrayList<String> alreadySelectedRoles = new ArrayList<String>();
    for (Iterator<KeyValue> it = ((SortedListKeyValueModel) jListPlaysRole.getModel()).iterator(); it.hasNext();) {
        KeyValue v = it.next();
        alreadySelectedRoles.add(v.getKey());
    }


    roleSelectPanelAdvanced = new RoleSelectPanelAdvanced(alreadySelectedRoles);
    int answer = JOptionPane.showConfirmDialog(Management.this, roleSelectPanelAdvanced, "Select Roles", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);

    while (answer == JOptionPane.OK_OPTION
            && (roleSelectPanelAdvanced.selectedRolesList.getSize() == 0)) {
        answer = JOptionPane.showConfirmDialog(Management.this, roleSelectPanelAdvanced, "Select Roles", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
    }
    if (answer != JOptionPane.CANCEL_OPTION && answer != JOptionPane.CLOSED_OPTION) {
        addRolesToBusinessComponent(((KeyValue) jListBusinessComponents.getSelectedValue()).getKey(), roleSelectPanelAdvanced.selectedRolesList, true);
    }



}//GEN-LAST:event_jButtonSelectPlaysRoleActionPerformed

private void jButtonRemovePlaysRoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemovePlaysRoleActionPerformed
    int n = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
    //yes -> 0, no -> 1
    if (n == 0) {

        Object[] selectedValues = jListPlaysRole.getSelectedValues();
        if (selectedValues.length != 0) {
            String bcomponentID = ((KeyValue) jListBusinessComponents.getSelectedValue()).getKey();
            removePlayRolesFromBComponent(bcomponentID, selectedValues, true);
        }
    }
}//GEN-LAST:event_jButtonRemovePlaysRoleActionPerformed

private void jButtonSetLicenseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSetLicenseActionPerformed
    TreeNodeData selected = (TreeNodeData) jTree1.getSelectionPath().getLastPathComponent();
    if (!jTextFieldLicense.getText().trim().isEmpty()) {
        this.setLicense(selected.getId(), jTextFieldLicense.getText().trim(), true, isNormalPath(jTree1.getSelectionPath()));
    } else {
        this.removeLicense(selected.getId(), true, isNormalPath(jTree1.getSelectionPath()));
    }
}//GEN-LAST:event_jButtonSetLicenseActionPerformed

private void jButtonSetSvnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSetSvnActionPerformed

    TreeNodeData selected = (TreeNodeData) jTree1.getSelectionPath().getLastPathComponent();
    if (!jTextFieldSvn.getText().trim().isEmpty()) {
        this.setSvn(selected.getId(), jTextFieldSvn.getText().trim(), true, isNormalPath(jTree1.getSelectionPath()));
    } else {
        this.removeSvn(selected.getId(), true, isNormalPath(jTree1.getSelectionPath()));
    }
}//GEN-LAST:event_jButtonSetSvnActionPerformed

    private void jButtonSaveDescriptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveDescriptionActionPerformed
        TreeNodeData selected = (TreeNodeData) jTree1.getSelectionPath().getLastPathComponent();
        if (!jTextAreaDescription.getText().trim().isEmpty()) {
            this.setDescription(selected.getId(), jTextAreaDescription.getText().trim(), true, isNormalPath(jTree1.getSelectionPath()));
        } else {
            this.removeDescription(selected.getId(), true, isNormalPath(jTree1.getSelectionPath()));
        }
    }//GEN-LAST:event_jButtonSaveDescriptionActionPerformed

    private void jLabelSyncMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelSyncMouseClicked

        SynchronizationDialog dl = new SynchronizationDialog(this, true);
        dl.setVisible(true);
    }//GEN-LAST:event_jLabelSyncMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
            //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Management.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Management.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Management.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Management.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new Management(true, null).setVisible(true);
            }
        });

    }
    //<editor-fold defaultstate="collapsed" desc="gui fields">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable calledByTable;
    private javax.swing.JTable callsTable;
    private javax.swing.JTable enterpriseTierTable;
    private javax.swing.JButton jButtonAddCalledBy;
    private javax.swing.JButton jButtonAddCalls;
    private javax.swing.JButton jButtonAddNewArchitecturalPattern;
    private javax.swing.JButton jButtonAddNewBusinessComponent;
    private javax.swing.JButton jButtonAddNewLanguage;
    private javax.swing.JButton jButtonAddNewPatternRole;
    private javax.swing.JButton jButtonAddNewQualityAttribute;
    private javax.swing.JButton jButtonAddNewTechnology;
    private javax.swing.JButton jButtonAddProvidedInterface;
    private javax.swing.JButton jButtonAddProvidedInterfaceMethod;
    private javax.swing.JButton jButtonAddRequiredInterface;
    private javax.swing.JButton jButtonAddRequiredInterfaceMethod;
    private javax.swing.JButton jButtonAddUsedBy;
    private javax.swing.JButton jButtonAddUses;
    private javax.swing.JButton jButtonEditProvidedInterface;
    private javax.swing.JButton jButtonEditProvidedInterfaceMethod;
    private javax.swing.JButton jButtonEditRequiredInterface;
    private javax.swing.JButton jButtonEditRequiredInterfaceMethod;
    private javax.swing.JButton jButtonRemoveCalledBy;
    private javax.swing.JButton jButtonRemoveCalls;
    private javax.swing.JButton jButtonRemoveEnterpriseTier;
    private javax.swing.JButton jButtonRemoveHighQuality;
    private javax.swing.JButton jButtonRemoveLowQuality;
    private javax.swing.JButton jButtonRemoveMediumQuality;
    private javax.swing.JButton jButtonRemovePlaysRole;
    private javax.swing.JButton jButtonRemoveProvidedInterface;
    private javax.swing.JButton jButtonRemoveProvidedInterfaceMethod;
    private javax.swing.JButton jButtonRemoveQualityAttribute;
    private javax.swing.JButton jButtonRemoveRequiredInterface;
    private javax.swing.JButton jButtonRemoveRequiredInterfaceMethod;
    private javax.swing.JButton jButtonRemoveResourceTier;
    private javax.swing.JButton jButtonRemoveSelectedArchitecturalPattern;
    private javax.swing.JButton jButtonRemoveSelectedBusinessComponent;
    private javax.swing.JButton jButtonRemoveSelectedLanguage;
    private javax.swing.JButton jButtonRemoveSelectedPatternRole;
    private javax.swing.JButton jButtonRemoveSelectedTechnology;
    private javax.swing.JButton jButtonRemoveUsedBy;
    private javax.swing.JButton jButtonRemoveUserTier;
    private javax.swing.JButton jButtonRemoveUses;
    private javax.swing.JButton jButtonRemoveWorkspaceTier;
    private javax.swing.JButton jButtonRenameQualityAttribute;
    private javax.swing.JButton jButtonRenameSelectedArchitecturalPattern;
    private javax.swing.JButton jButtonRenameSelectedBusinessComponent;
    private javax.swing.JButton jButtonRenameSelectedLanguage;
    private javax.swing.JButton jButtonRenameSelectedPatternRole;
    private javax.swing.JButton jButtonRenameSelectedTechnology;
    private javax.swing.JButton jButtonSaveDescription;
    private javax.swing.JButton jButtonSelectEnterpriseTier;
    private javax.swing.JButton jButtonSelectHighQuality;
    private javax.swing.JButton jButtonSelectLowQuality;
    private javax.swing.JButton jButtonSelectMediumQuality;
    private javax.swing.JButton jButtonSelectPlaysRole;
    private javax.swing.JButton jButtonSelectResourceTier;
    private javax.swing.JButton jButtonSelectUserTier;
    private javax.swing.JButton jButtonSelectWorkspaceTier;
    private javax.swing.JButton jButtonSetLicense;
    private javax.swing.JButton jButtonSetSvn;
    private javax.swing.JButton jButtonSetVersion;
    private javax.swing.JComboBox jComboBoxLanguage;
    private javax.swing.JComboBox jComboBoxTechnology;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    public static javax.swing.JLabel jLabelSync;
    private javax.swing.JList jListArchitecturalPatterns;
    private javax.swing.JList jListBusinessComponents;
    private javax.swing.JList jListConcepts;
    private javax.swing.JList jListHighQuality;
    private javax.swing.JList jListLanguages;
    private javax.swing.JList jListLowQuality;
    private javax.swing.JList jListMediumQuality;
    private javax.swing.JList jListPatternRoles;
    private javax.swing.JList jListPlaysRole;
    private javax.swing.JList jListQualityAttributes;
    private javax.swing.JList jListTechnologies;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel29;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel32;
    private javax.swing.JPanel jPanel33;
    private javax.swing.JPanel jPanel34;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane18;
    private javax.swing.JScrollPane jScrollPane19;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane20;
    private javax.swing.JScrollPane jScrollPane21;
    private javax.swing.JScrollPane jScrollPane22;
    private javax.swing.JScrollPane jScrollPane23;
    private javax.swing.JScrollPane jScrollPane24;
    private javax.swing.JScrollPane jScrollPane25;
    private javax.swing.JScrollPane jScrollPane26;
    private javax.swing.JScrollPane jScrollPane27;
    private javax.swing.JScrollPane jScrollPane28;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextAreaDescription;
    private javax.swing.JTextField jTextFieldDomain;
    private javax.swing.JTextField jTextFieldLicense;
    private javax.swing.JTextField jTextFieldMetaModelName;
    private javax.swing.JTextField jTextFieldNewArchitecturalPattern;
    private javax.swing.JTextField jTextFieldNewBusinessComponent;
    private javax.swing.JTextField jTextFieldNewLanguage;
    private javax.swing.JTextField jTextFieldNewPatternRole;
    private javax.swing.JTextField jTextFieldNewQualityAttributeName;
    private javax.swing.JTextField jTextFieldNewTechnology;
    private javax.swing.JTextField jTextFieldSvn;
    private javax.swing.JTextField jTextFieldVersion;
    private javax.swing.JTable providedInterfaceMethodsTable;
    private javax.swing.JTable providedInterfaceTable;
    private javax.swing.JTable requiredInterfaceMethodsTable;
    private javax.swing.JTable requiredInterfaceTable;
    private javax.swing.JTable resourceTierTable;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JTable usedByTable;
    private javax.swing.JTable userTierTable;
    private javax.swing.JTable usesTable;
    private javax.swing.JTable workspaceTierTable;
    // End of variables declaration//GEN-END:variables
    //</editor-fold>
    public CustomTree jTree1;
    JPopupMenu leafPopup, nodePopup, rootPopup;
    public static ProgressBarFrame progressBar = new ProgressBarFrame("Please wait...");

    public void initUsesTable() {
        usesTable.setAutoCreateRowSorter(true);
        usesTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            int viewRow = usesTable.getSelectedRow();
                            if (viewRow < 0) {
                                jButtonRemoveUses.setEnabled(false);
                            } else {
                                jButtonRemoveUses.setEnabled(true);
                                //int modelRow = usesTable.convertRowIndexToModel(viewRow);
                                //System.out.println(String.format("Selected Row in view: %d. " + "Selected Row in model: %d.", viewRow, modelRow));
                                //System.out.println(usesDataModel.get(modelRow).getId());
                            }
                        }
                    }
                });

        usesTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = usesTable.getSelectedRow();
                    if (row != -1) {
                        int modelRow = usesTable.convertRowIndexToModel(row);
                        String clickedID = ((ComponentRelationTableModel) usesTable.getModel()).getDataAtRow(modelRow).getId();
                        CustomTreeModel treeModel = (CustomTreeModel) jTree1.getModel();
                        jTree1.setSelectionPath(treeModel.getComponentPathWithID(clickedID));
                    }
                }
            }
        });



        //empty model
        ArrayList<ComponentRelationDataModel> usesDataModel = new ArrayList<ComponentRelationDataModel>();
        ComponentRelationTableModel tableModel = new ComponentRelationTableModel(usesDataModel);
        usesTable.setModel(tableModel);
        //usesTable.setDefaultRenderer(String.class, new ImageRenderer());

        TableColumnModel columnModel = usesTable.getColumnModel();
        TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
        columnModel.getColumn(0).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(1).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(2).setCellRenderer(textAreaRenderer);

        jScrollPane3.getViewport().setBackground(Color.WHITE);

        //usesTable.getColumnModel().getColumn(0).setCellRenderer(new ImageRenderer());
//        usesTable.getColumnModel().getColumn(0).setPreferredWidth(18);
//        usesTable.getColumnModel().getColumn(1).setPreferredWidth(18);
//        usesTable.getColumnModel().getColumn(2).setPreferredWidth(18);
//        //usesTable.getColumnModel().getColumn(3).setPreferredWidth(18);


    }

    private void initUsedByTable() {
        usedByTable.setAutoCreateRowSorter(true);
        usedByTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            int viewRow = usedByTable.getSelectedRow();
                            if (viewRow < 0) {
                                jButtonRemoveUsedBy.setEnabled(false);
                            } else {
                                jButtonRemoveUsedBy.setEnabled(true);
                                //int modelRow = usesTable.convertRowIndexToModel(viewRow);
                                //System.out.println(String.format("Selected Row in view: %d. " + "Selected Row in model: %d.", viewRow, modelRow));
                                //System.out.println(usesDataModel.get(modelRow).getId());
                            }
                        }
                    }
                });

        usedByTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = usedByTable.getSelectedRow();
                    if (row != -1) {
                        int modelRow = usedByTable.convertRowIndexToModel(row);
                        String clickedID = ((ComponentRelationTableModel) usedByTable.getModel()).getDataAtRow(modelRow).getId();
                        CustomTreeModel treeModel = (CustomTreeModel) jTree1.getModel();
                        jTree1.setSelectionPath(treeModel.getComponentPathWithID(clickedID));
                    }
                }
            }
        });


        //empty model
        ArrayList<ComponentRelationDataModel> usedByDataModel = new ArrayList<ComponentRelationDataModel>();
        ComponentRelationTableModel usedByTableModel = new ComponentRelationTableModel(usedByDataModel);
        usedByTable.setModel(usedByTableModel);
        //usedByTable.setDefaultRenderer(String.class, new ImageRenderer());
        TableColumnModel columnModel = usedByTable.getColumnModel();
        TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
        columnModel.getColumn(0).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(1).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(2).setCellRenderer(textAreaRenderer);

        jScrollPane4.getViewport().setBackground(Color.WHITE);
    }

    private void initCallsTable() {
        callsTable.setAutoCreateRowSorter(true);
        callsTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            int viewRow = callsTable.getSelectedRow();
                            if (viewRow < 0) {
                                jButtonRemoveCalls.setEnabled(false);
                            } else {
                                jButtonRemoveCalls.setEnabled(true);
                                //int modelRow = usesTable.convertRowIndexToModel(viewRow);
                                //System.out.println(String.format("Selected Row in view: %d. " + "Selected Row in model: %d.", viewRow, modelRow));
                                //System.out.println(usesDataModel.get(modelRow).getId());
                            }
                        }
                    }
                });

        callsTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = callsTable.getSelectedRow();
                    if (row != -1) {
                        int modelRow = callsTable.convertRowIndexToModel(row);
                        String clickedID = ((ComponentRelationTableModel) callsTable.getModel()).getDataAtRow(modelRow).getId();
                        CustomTreeModel treeModel = (CustomTreeModel) jTree1.getModel();
                        jTree1.setSelectionPath(treeModel.getComponentPathWithID(clickedID));
                    }
                }
            }
        });

        //empty model
        ArrayList<ComponentRelationDataModel> callsDataModel = new ArrayList<ComponentRelationDataModel>();
        ComponentRelationTableModel callsTableModel = new ComponentRelationTableModel(callsDataModel);
        callsTable.setModel(callsTableModel);
        //callsTable.setDefaultRenderer(String.class, new ImageRenderer());
        TableColumnModel columnModel = callsTable.getColumnModel();
        TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
        columnModel.getColumn(0).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(1).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(2).setCellRenderer(textAreaRenderer);

        jScrollPane5.getViewport().setBackground(Color.WHITE);
    }

    private void initCalledByTable() {
        calledByTable.setAutoCreateRowSorter(true);
        calledByTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            int viewRow = calledByTable.getSelectedRow();
                            if (viewRow < 0) {
                                jButtonRemoveCalledBy.setEnabled(false);
                            } else {
                                jButtonRemoveCalledBy.setEnabled(true);
                                //int modelRow = usesTable.convertRowIndexToModel(viewRow);
                                //System.out.println(String.format("Selected Row in view: %d. " + "Selected Row in model: %d.", viewRow, modelRow));
                                //System.out.println(usesDataModel.get(modelRow).getId());
                            }
                        }
                    }
                });

        calledByTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = calledByTable.getSelectedRow();
                    if (row != -1) {
                        int modelRow = calledByTable.convertRowIndexToModel(row);
                        String clickedID = ((ComponentRelationTableModel) calledByTable.getModel()).getDataAtRow(modelRow).getId();
                        CustomTreeModel treeModel = (CustomTreeModel) jTree1.getModel();
                        jTree1.setSelectionPath(treeModel.getComponentPathWithID(clickedID));
                    }
                }
            }
        });

        //empty model
        ArrayList<ComponentRelationDataModel> calledByDataModel = new ArrayList<ComponentRelationDataModel>();
        ComponentRelationTableModel calledByTableModel = new ComponentRelationTableModel(calledByDataModel);
        calledByTable.setModel(calledByTableModel);
        //calledByTable.setDefaultRenderer(String.class, new ImageRenderer());
        TableColumnModel columnModel = calledByTable.getColumnModel();
        TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
        columnModel.getColumn(0).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(1).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(2).setCellRenderer(textAreaRenderer);


        jScrollPane6.getViewport().setBackground(Color.WHITE);
    }

    private void initProvidedInterfaceTable() {
        providedInterfaceTable.setAutoCreateRowSorter(true);
        providedInterfaceTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            int viewRow = providedInterfaceTable.getSelectedRow();
                            if (viewRow < 0) {
                                jButtonRemoveProvidedInterface.setEnabled(false);
                                jButtonEditProvidedInterface.setEnabled(false);
                                jButtonAddProvidedInterfaceMethod.setEnabled(false);
                            } else {
                                jButtonRemoveProvidedInterface.setEnabled(true);
                                jButtonEditProvidedInterface.setEnabled(true);
                                jButtonAddProvidedInterfaceMethod.setEnabled(true);
                                int modelRow = providedInterfaceTable.convertRowIndexToModel(viewRow);
                                getProvidedInterfaceMethods(((InterfaceTableModel) providedInterfaceTable.getModel()).getDataAtRow(modelRow).getId(), true);
                                //System.out.println(modelRow);
                            }
                            if (providedInterfaceTable.getSelectedRow() == -1) {
                                ((MethodTableModel) providedInterfaceMethodsTable.getModel()).clearAll();
                            }
                        }

                    }
                });

        //empty model
        ArrayList<InterfaceDataModel> providedInterfaceDataModel = new ArrayList<InterfaceDataModel>();
        InterfaceTableModel providedInterfaceTableModel = new InterfaceTableModel(providedInterfaceDataModel);
        providedInterfaceTable.setModel(providedInterfaceTableModel);
        //calledByTable.setDefaultRenderer(String.class, new ImageRenderer());
        TableColumnModel columnModel = providedInterfaceTable.getColumnModel();
        TextAreaRenderer2 textAreaRenderer = new TextAreaRenderer2();
        columnModel.getColumn(0).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(1).setCellRenderer(textAreaRenderer);

        jScrollPane7.getViewport().setBackground(Color.WHITE);
    }

    private void initRequiredInterfaceTable() {
        requiredInterfaceTable.setAutoCreateRowSorter(true);
        requiredInterfaceTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            int viewRow = requiredInterfaceTable.getSelectedRow();
                            if (viewRow < 0) {
                                jButtonRemoveRequiredInterface.setEnabled(false);
                                jButtonEditRequiredInterface.setEnabled(false);
                                jButtonAddRequiredInterfaceMethod.setEnabled(false);
                            } else {
                                jButtonRemoveRequiredInterface.setEnabled(true);
                                jButtonEditRequiredInterface.setEnabled(true);
                                jButtonAddRequiredInterfaceMethod.setEnabled(true);
                                int modelRow = requiredInterfaceTable.convertRowIndexToModel(viewRow);
                                getRequiredInterfaceMethods(((InterfaceTableModel) requiredInterfaceTable.getModel()).getDataAtRow(modelRow).getId(), true);
                                //System.out.println(modelRow);
                            }
                            if (requiredInterfaceTable.getSelectedRow() == -1) {
                                ((MethodTableModel) requiredInterfaceMethodsTable.getModel()).clearAll();
                            }
                        }

                    }
                });

        //empty model
        ArrayList<InterfaceDataModel> requiredInterfaceDataModel = new ArrayList<InterfaceDataModel>();
        InterfaceTableModel requiredInterfaceTableModel = new InterfaceTableModel(requiredInterfaceDataModel);
        requiredInterfaceTable.setModel(requiredInterfaceTableModel);
        //calledByTable.setDefaultRenderer(String.class, new ImageRenderer());
        TableColumnModel columnModel = requiredInterfaceTable.getColumnModel();
        TextAreaRenderer2 textAreaRenderer = new TextAreaRenderer2();
        columnModel.getColumn(0).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(1).setCellRenderer(textAreaRenderer);

        jScrollPane11.getViewport().setBackground(Color.WHITE);
    }

    private void initProvidedInterfaceMethodsTable() {
        providedInterfaceMethodsTable.setAutoCreateRowSorter(true);
        providedInterfaceMethodsTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            int viewRow = providedInterfaceMethodsTable.getSelectedRow();
                            if (viewRow < 0) {
                                jButtonRemoveProvidedInterfaceMethod.setEnabled(false);
                                jButtonEditProvidedInterfaceMethod.setEnabled(false);
                            } else {
                                jButtonRemoveProvidedInterfaceMethod.setEnabled(true);
                                jButtonEditProvidedInterfaceMethod.setEnabled(true);
                            }
                        }
                    }
                });

        //empty model
        ArrayList<MethodDataModel> providedInterfaceMethodDataModel = new ArrayList<MethodDataModel>();
        MethodTableModel providedInterfaceMethodTableModel = new MethodTableModel(providedInterfaceMethodDataModel);
        providedInterfaceMethodsTable.setModel(providedInterfaceMethodTableModel);

        TableColumnModel columnModel = providedInterfaceMethodsTable.getColumnModel();
        TextAreaRenderer2 textAreaRenderer = new TextAreaRenderer2();
        columnModel.getColumn(0).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(1).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(2).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(3).setCellRenderer(textAreaRenderer);

        jScrollPane10.getViewport().setBackground(Color.WHITE);
    }

    private void initRequiredInterfaceMethodsTable() {
        requiredInterfaceMethodsTable.setAutoCreateRowSorter(true);
        requiredInterfaceMethodsTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            int viewRow = requiredInterfaceMethodsTable.getSelectedRow();
                            if (viewRow < 0) {
                                jButtonRemoveRequiredInterfaceMethod.setEnabled(false);
                                jButtonEditRequiredInterfaceMethod.setEnabled(false);
                            } else {
                                jButtonRemoveRequiredInterfaceMethod.setEnabled(true);
                                jButtonEditRequiredInterfaceMethod.setEnabled(true);
                            }
                        }
                    }
                });

        //empty model
        ArrayList<MethodDataModel> requiredInterfaceMethodDataModel = new ArrayList<MethodDataModel>();
        MethodTableModel requiredInterfaceMethodTableModel = new MethodTableModel(requiredInterfaceMethodDataModel);
        requiredInterfaceMethodsTable.setModel(requiredInterfaceMethodTableModel);

        TableColumnModel columnModel = requiredInterfaceMethodsTable.getColumnModel();
        TextAreaRenderer2 textAreaRenderer = new TextAreaRenderer2();
        columnModel.getColumn(0).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(1).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(2).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(3).setCellRenderer(textAreaRenderer);

        jScrollPane12.getViewport().setBackground(Color.WHITE);
    }

    private void initMyComponents() {

        progressBar.setIndeterminate(false);
        setProgressBar(true, true);

        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                //classificationFrame = new Classification(Management.this);

                //cope sync
                CopeSynchronizer copeSync = new CopeSynchronizer(reuseProject);
                copeSync.start();


                ///////////

                initComponentsTab();
                initLanguagesAndTechnologiesTab();
                initQualityAttributesTab();
                //initRolesTab();
                initArchitecturalPatternsTab();
                handleLastTab();
                initBusinessComponentsTab();

                setProgressBar(false, true);
                progressBar.setIndeterminate(true);
                jScrollPane2.getVerticalScrollBar().setUnitIncrement(16);
                jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);

            }
        });

    }

    private void handleLastTab() {
        final int index = jTabbedPane1.getTabCount() - 1;
        //jTabbedPane1.setTabComponentAt(jTabbedPane1.getTabCount() - 1, new JLabel("Open Classification Console"));
//        jTabbedPane1.setForegroundAt(index, Color.WHITE);
//        jTabbedPane1.setBackgroundAt(index, Color.red);

        jTabbedPane1.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if (jTabbedPane1.getSelectedIndex() == index) {
                    jTabbedPane1.setSelectedIndex(selectedTab);
                    Management.this.setVisible(false);
                    if (classificationFrame == null) {
                        classificationFrame = new Classification(Management.this);
                    }
                    classificationFrame.setVisible(true);
                    //JOptionPane.showMessageDialog(Management.this, "Not implemented yet...");
                } else {
                    selectedTab = jTabbedPane1.getSelectedIndex();
                }
            }
        });
    }

    private void initComponentsTab() {

        initJTree();
        initConceptList();
        initUsesTable();
        initUsedByTable();
        initCallsTable();
        initCalledByTable();
        initProvidedInterfaceTable();
        initProvidedInterfaceMethodsTable();
        initRequiredInterfaceTable();
        initRequiredInterfaceMethodsTable();
        fromLoading = true;
        //populateTechnologyCombo();
        fromLoading = false;
        rightPanel.setVisible(false);
        jScrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    }

    private void initConceptList() {
        SortedListKeyValueModel concepts = new SortedListKeyValueModel();
        jListConcepts.setModel(concepts);
    }

    private void initBusinessComponentsTab() {
        initEnterpriseTierTable();
        initResourceTierTable();
        initUserTierTable();
        initWorkspaceTierTable();
        initBusinessComponentsList();


        bcomponentRoleList = new SortedListKeyValueModel();
        jListPlaysRole.setModel(bcomponentRoleList);
        jListPlaysRole.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            jButtonRemovePlaysRole.setEnabled(true);
                            if (jListPlaysRole.getSelectedIndex() == -1) {
                                jButtonRemovePlaysRole.setEnabled(false);
                            } else {
                                displaySummary(((KeyValue) jListPlaysRole.getSelectedValue()).getKey(), true);
                            }
                        }
                    }
                });
    }

    private void displaySummary(final String roleID, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                Role r = new Role(api);
                try {
                    ArchitecturalPatternDetails result = r.getQualityAttributes(roleID);
                    createHTML(result);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error getting quality attributes", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MalformedQueryException ex) {
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (QueryEvaluationException ex) {
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void createHTML(ArchitecturalPatternDetails result) {
        StringBuilder html = new StringBuilder();
        html.append("<html> <body style=\"font-family: Arial;\">");
        html.append("<h3>").append(result.getPatternName()).append("</h3>");
        html.append("<ul style=\"padding: 0px; margin-left:8px;\">");

        ArrayList<KeyValue> highQuality = result.getHighQuality();
        if (!highQuality.isEmpty()) {
            html.append("<li><strong>High Quality:</strong> ");
            for (int i = 0; i < highQuality.size(); i++) {
                html.append(highQuality.get(i));
                if (i < highQuality.size() - 1) {
                    html.append(", ");
                }
            }
            html.append("</li>");
        }

        ArrayList<KeyValue> mediumQuality = result.getMediumQuality();
        if (!mediumQuality.isEmpty()) {
            html.append("<li><strong>Medium Quality:</strong> ");
            for (int i = 0; i < mediumQuality.size(); i++) {
                html.append(mediumQuality.get(i));
                if (i < mediumQuality.size() - 1) {
                    html.append(", ");
                }
            }
            html.append("</li>");
        }

        ArrayList<KeyValue> lowQuality = result.getLowQuality();
        if (!lowQuality.isEmpty()) {
            html.append("<li><strong>Low Quality:</strong> ");

            for (int i = 0; i < lowQuality.size(); i++) {
                html.append(lowQuality.get(i));
                if (i < lowQuality.size() - 1) {
                    html.append(", ");
                }
            }
            html.append("</li>");
        }
        html.append("</ul></body></html>");
        jEditorPane1.setText(html.toString());
    }

    private void initBusinessComponentsList() {
        ReuseApi api = getAPI(false);
        BusinessComponent buss = new BusinessComponent(api);
        ArrayList<KeyValue> bcomponents = null;
        try {
            bcomponents = buss.getBusinessComponents();
        } catch (RepositoryException ex) {
            JOptionPane.showMessageDialog(Management.this, "Error populating business components", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            JOptionPane.showMessageDialog(Management.this, "Error populating business components", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            JOptionPane.showMessageDialog(Management.this, "Error populating business components", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            api.getDatabase().close();
        }

        //jListTechnologies.removeAll();
        SortedListKeyValueModel model = new SortedListKeyValueModel();
        model.addAll(bcomponents.toArray());
        jListBusinessComponents.setModel(model);


        jListBusinessComponents.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            int viewRow = jListBusinessComponents.getSelectedIndex();
                            //System.out.println("viewOrw: " + viewRow);
                            //System.out.println(jListBusinessComponents.getModel().getSize());
                            if (viewRow < 0) {
                            } else {
                                jButtonRenameSelectedBusinessComponent.setEnabled(true);
                                jButtonRemoveSelectedBusinessComponent.setEnabled(true);

                                jButtonSelectEnterpriseTier.setEnabled(true);
                                jButtonSelectResourceTier.setEnabled(true);
                                jButtonSelectUserTier.setEnabled(true);
                                jButtonSelectWorkspaceTier.setEnabled(true);
                                jButtonSelectPlaysRole.setEnabled(true);
                                //jButtonRemovePlaysRole.setEnabled(true);

                                getBusinessComponentDetails(((KeyValue) jListBusinessComponents.getSelectedValue()).getKey(), true);
                            }
                            if (jListBusinessComponents.getSelectedIndex() == -1) {
                                jButtonRenameSelectedBusinessComponent.setEnabled(false);
                                jButtonRemoveSelectedBusinessComponent.setEnabled(false);

                                enterpriseTierTable.clearSelection();
                                resourceTierTable.clearSelection();
                                userTierTable.clearSelection();
                                workspaceTierTable.clearSelection();

                                jListPlaysRole.clearSelection();
                                jEditorPane1.setText("<html><body></body></html>");

                                ((ComponentRelationTableModel) enterpriseTierTable.getModel()).clearAll();
                                ((ComponentRelationTableModel) resourceTierTable.getModel()).clearAll();
                                ((ComponentRelationTableModel) userTierTable.getModel()).clearAll();
                                ((ComponentRelationTableModel) workspaceTierTable.getModel()).clearAll();
                                ((SortedListKeyValueModel) jListPlaysRole.getModel()).clear();

                                jButtonSelectEnterpriseTier.setEnabled(false);
                                jButtonSelectResourceTier.setEnabled(false);
                                jButtonSelectUserTier.setEnabled(false);
                                jButtonSelectWorkspaceTier.setEnabled(false);
                                jButtonSelectPlaysRole.setEnabled(false);
                                jButtonRemovePlaysRole.setEnabled(false);
                            }
                        }

                    }
                });
    }

    private void initEnterpriseTierTable() {
        enterpriseTierTable.setAutoCreateRowSorter(true);
        enterpriseTierTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            int viewRow = enterpriseTierTable.getSelectedRow();
                            if (viewRow < 0) {
                                jButtonRemoveEnterpriseTier.setEnabled(false);
                            } else {
                                jButtonRemoveEnterpriseTier.setEnabled(true);
                            }
                        }
                    }
                });

        enterpriseTierTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = enterpriseTierTable.getSelectedRow();
                    if (row != -1) {
                        jTabbedPane1.setSelectedIndex(0);
                        int modelRow = enterpriseTierTable.convertRowIndexToModel(row);
                        String clickedID = ((ComponentRelationTableModel) enterpriseTierTable.getModel()).getDataAtRow(modelRow).getId();
                        CustomTreeModel treeModel = (CustomTreeModel) jTree1.getModel();
                        jTree1.setSelectionPath(treeModel.getComponentPathWithID(clickedID));
                    }
                }
            }
        });

        //empty model
        ArrayList<ComponentRelationDataModel> datamodel = new ArrayList<ComponentRelationDataModel>();
        ComponentRelationTableModel tableModel = new ComponentRelationTableModel(datamodel);
        enterpriseTierTable.setModel(tableModel);
        //usesTable.setDefaultRenderer(String.class, new ImageRenderer());

        TableColumnModel columnModel = enterpriseTierTable.getColumnModel();
        TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
        columnModel.getColumn(0).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(1).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(2).setCellRenderer(textAreaRenderer);


    }

    private void initResourceTierTable() {
        resourceTierTable.setAutoCreateRowSorter(true);
        resourceTierTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            int viewRow = resourceTierTable.getSelectedRow();
                            if (viewRow < 0) {
                                jButtonRemoveResourceTier.setEnabled(false);
                            } else {
                                jButtonRemoveResourceTier.setEnabled(true);
                            }
                        }
                    }
                });

        resourceTierTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = resourceTierTable.getSelectedRow();
                    if (row != -1) {
                        jTabbedPane1.setSelectedIndex(0);
                        int modelRow = resourceTierTable.convertRowIndexToModel(row);
                        String clickedID = ((ComponentRelationTableModel) resourceTierTable.getModel()).getDataAtRow(modelRow).getId();
                        CustomTreeModel treeModel = (CustomTreeModel) jTree1.getModel();
                        jTree1.setSelectionPath(treeModel.getComponentPathWithID(clickedID));
                    }
                }
            }
        });

        //empty model
        ArrayList<ComponentRelationDataModel> datamodel = new ArrayList<ComponentRelationDataModel>();
        ComponentRelationTableModel tableModel = new ComponentRelationTableModel(datamodel);
        resourceTierTable.setModel(tableModel);
        //usesTable.setDefaultRenderer(String.class, new ImageRenderer());

        TableColumnModel columnModel = resourceTierTable.getColumnModel();
        TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
        columnModel.getColumn(0).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(1).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(2).setCellRenderer(textAreaRenderer);
    }

    private void initUserTierTable() {
        userTierTable.setAutoCreateRowSorter(true);
        userTierTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            int viewRow = userTierTable.getSelectedRow();
                            if (viewRow < 0) {
                                jButtonRemoveUserTier.setEnabled(false);
                            } else {
                                jButtonRemoveUserTier.setEnabled(true);
                            }
                        }
                    }
                });


        userTierTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = userTierTable.getSelectedRow();
                    if (row != -1) {
                        jTabbedPane1.setSelectedIndex(0);
                        int modelRow = userTierTable.convertRowIndexToModel(row);
                        String clickedID = ((ComponentRelationTableModel) userTierTable.getModel()).getDataAtRow(modelRow).getId();
                        CustomTreeModel treeModel = (CustomTreeModel) jTree1.getModel();
                        jTree1.setSelectionPath(treeModel.getComponentPathWithID(clickedID));
                    }
                }
            }
        });

        //empty model
        ArrayList<ComponentRelationDataModel> datamodel = new ArrayList<ComponentRelationDataModel>();
        ComponentRelationTableModel tableModel = new ComponentRelationTableModel(datamodel);
        userTierTable.setModel(tableModel);
        //usesTable.setDefaultRenderer(String.class, new ImageRenderer());

        TableColumnModel columnModel = userTierTable.getColumnModel();
        TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
        columnModel.getColumn(0).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(1).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(2).setCellRenderer(textAreaRenderer);
    }

    private void initWorkspaceTierTable() {
        workspaceTierTable.setAutoCreateRowSorter(true);
        workspaceTierTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            int viewRow = workspaceTierTable.getSelectedRow();
                            if (viewRow < 0) {
                                jButtonRemoveWorkspaceTier.setEnabled(false);
                            } else {
                                jButtonRemoveWorkspaceTier.setEnabled(true);
                            }
                        }
                    }
                });

        workspaceTierTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = workspaceTierTable.getSelectedRow();
                    if (row != -1) {
                        jTabbedPane1.setSelectedIndex(0);
                        int modelRow = workspaceTierTable.convertRowIndexToModel(row);
                        String clickedID = ((ComponentRelationTableModel) workspaceTierTable.getModel()).getDataAtRow(modelRow).getId();
                        CustomTreeModel treeModel = (CustomTreeModel) jTree1.getModel();
                        jTree1.setSelectionPath(treeModel.getComponentPathWithID(clickedID));
                    }
                }
            }
        });


        //empty model
        ArrayList<ComponentRelationDataModel> datamodel = new ArrayList<ComponentRelationDataModel>();
        ComponentRelationTableModel tableModel = new ComponentRelationTableModel(datamodel);
        workspaceTierTable.setModel(tableModel);
        //usesTable.setDefaultRenderer(String.class, new ImageRenderer());

        TableColumnModel columnModel = workspaceTierTable.getColumnModel();
        TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
        columnModel.getColumn(0).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(1).setCellRenderer(textAreaRenderer);
        columnModel.getColumn(2).setCellRenderer(textAreaRenderer);
    }

    private void initLanguagesAndTechnologiesTab() {
        populateTechnologyListBox();
        populateLanguageListBox();
    }

    private void initQualityAttributesTab() {
        ReuseApi api = getAPI(false);
        QualityAttribute atrr = new QualityAttribute(api);
        ArrayList<KeyValue> attributes = null;
        try {
            attributes = atrr.getQualityAttributes();
        } catch (RepositoryException ex) {
            JOptionPane.showMessageDialog(Management.this, "Error populating quality attributes", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            JOptionPane.showMessageDialog(Management.this, "Error populating quality attributes", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            JOptionPane.showMessageDialog(Management.this, "Error populating quality attributes", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            api.getDatabase().close();
        }



        jListQualityAttributes.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            //System.out.println("should not print...");
                            int viewRow = jListQualityAttributes.getSelectedIndex();
                            if (viewRow < 0) {
                            } else {
                                jButtonRenameQualityAttribute.setEnabled(true);
                                jButtonRemoveQualityAttribute.setEnabled(true);
                            }
                            if (jListQualityAttributes.getSelectedIndex() == -1) {
                                jButtonRenameQualityAttribute.setEnabled(false);
                                jButtonRemoveQualityAttribute.setEnabled(false);
                            }
                        }

                    }
                });



        //jListTechnologies.removeAll();
        qualityAttributesList = new SortedListKeyValueModel();
        for (KeyValue keyValue : attributes) {
            qualityAttributesList.add(keyValue);
        }
        jListQualityAttributes.setModel(qualityAttributesList);

        //attributeSelectPanel = new QualityAttributeSelectPanel(qualityAttributesList);

    }

//    private void initRolesTab() {
//        ReuseApi api = null;
//        try {
//            api = new ReuseApi();
//        } catch (RepositoryException ex) {
//            JOptionPane.showMessageDialog(Management.this, "Cannot find the repository", "Error", JOptionPane.ERROR_MESSAGE);
//            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (RepositoryConfigException ex) {
//            JOptionPane.showMessageDialog(Management.this, "Cannot find the repository", "Error", JOptionPane.ERROR_MESSAGE);
//            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        Role role = new Role(api);
//        ArrayList<KeyValue> roles = null;
//        try {
//            roles = role.getRoles();
//        } catch (RepositoryException ex) {
//            JOptionPane.showMessageDialog(Management.this, "Error populating roles", "Error", JOptionPane.ERROR_MESSAGE);
//            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (MalformedQueryException ex) {
//            JOptionPane.showMessageDialog(Management.this, "Error populating roles", "Error", JOptionPane.ERROR_MESSAGE);
//            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (QueryEvaluationException ex) {
//            JOptionPane.showMessageDialog(Management.this, "Error populating roles", "Error", JOptionPane.ERROR_MESSAGE);
//            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            api.getDatabase().close();
//        }
//
//
//        jListPatternRoles.getSelectionModel().addListSelectionListener(
//                new ListSelectionListener() {
//
//                    @Override
//                    public void valueChanged(ListSelectionEvent event) {
//                        if (!event.getValueIsAdjusting()) {
//                            int viewRow = jListPatternRoles.getSelectedIndex();
//                            if (viewRow < 0) {
//                            } else {
//                                jButtonRenameSelectedPatternRole.setEnabled(true);
//                                jButtonRemoveSelectedPatternRole.setEnabled(true);
//                            }
//                            if (jListPatternRoles.getSelectedIndex() == -1) {
//                                jButtonRenameSelectedPatternRole.setEnabled(false);
//                                jButtonRemoveSelectedPatternRole.setEnabled(false);
//                            }
//                        }
//
//                    }
//                });
//
//
//        //jListTechnologies.removeAll();
//        rolesList = new SortedListKeyValueModel();
//        for (KeyValue keyValue : roles) {
//            rolesList.add(keyValue);
//        }
//        jListPatternRoles.setModel(rolesList);
//    }
    private void initArchitecturalPatternsTab() {
        ReuseApi api = getAPI(false);
        ArchitecturalPattern pattern = new ArchitecturalPattern(api);
        ArrayList<KeyValue> patterns = null;
        try {
            patterns = pattern.getArchitecturalPatterns();
        } catch (RepositoryException ex) {
            JOptionPane.showMessageDialog(Management.this, "Error populating architectural patterns", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            JOptionPane.showMessageDialog(Management.this, "Error populating architectural patterns", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            JOptionPane.showMessageDialog(Management.this, "Error populating architectural patterns", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            api.getDatabase().close();
        }

        //jListTechnologies.removeAll();
        SortedListKeyValueModel architecturalPatternsList = new SortedListKeyValueModel();
        architecturalPatternsList.addAll(patterns.toArray());
        jListArchitecturalPatterns.setModel(architecturalPatternsList);


        jListArchitecturalPatterns.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            int viewRow = jListArchitecturalPatterns.getSelectedIndex();
                            //System.out.println("viewOrw: " + viewRow);
                            //System.out.println(jListArchitecturalPatterns.getModel().getSize());
                            if (viewRow < 0) {
                            } else {
                                jButtonRenameSelectedArchitecturalPattern.setEnabled(true);
                                jButtonRemoveSelectedArchitecturalPattern.setEnabled(true);

                                jButtonSelectHighQuality.setEnabled(true);
                                jButtonSelectMediumQuality.setEnabled(true);
                                jButtonSelectLowQuality.setEnabled(true);
                                jButtonAddNewPatternRole.setEnabled(true);
                                //jButtonRemoveHighQuality.setEnabled(true);
                                //jButtonRemoveMediumQuality.setEnabled(true);
                                //jButtonRemoveLowQuality.setEnabled(true);
                                //jButtonRemoveRole.setEnabled(true);

                                getArchitecturalPatternDetails(((KeyValue) jListArchitecturalPatterns.getSelectedValue()).getKey(), true);
                            }
                            if (jListArchitecturalPatterns.getSelectedIndex() == -1) {
                                jButtonRenameSelectedArchitecturalPattern.setEnabled(false);
                                jButtonRemoveSelectedArchitecturalPattern.setEnabled(false);

                                jButtonAddNewPatternRole.setEnabled(false);

                                highQualityList.clear();
                                mediumQualityList.clear();
                                lowQualityList.clear();
                                patternRolesList.clear();

                                jButtonSelectHighQuality.setEnabled(false);
                                jButtonSelectMediumQuality.setEnabled(false);
                                jButtonSelectLowQuality.setEnabled(false);
                                jButtonRemoveHighQuality.setEnabled(false);
                                jButtonRemoveMediumQuality.setEnabled(false);
                                jButtonRemoveLowQuality.setEnabled(false);

                            }
                        }

                    }
                });


        highQualityList = new SortedListKeyValueModel();
        mediumQualityList = new SortedListKeyValueModel();
        lowQualityList = new SortedListKeyValueModel();
        patternRolesList = new SortedListKeyValueModel();

        jListHighQuality.setModel(highQualityList);
        jListHighQuality.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            jButtonRemoveHighQuality.setEnabled(true);
                        }
                        if (jListHighQuality.getSelectedIndex() == -1) {
                            jButtonRemoveHighQuality.setEnabled(false);
                        }
                    }
                });


        jListMediumQuality.setModel(mediumQualityList);
        jListMediumQuality.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            jButtonRemoveMediumQuality.setEnabled(true);
                        }
                        if (jListMediumQuality.getSelectedIndex() == -1) {
                            jButtonRemoveMediumQuality.setEnabled(false);
                        }
                    }
                });


        jListLowQuality.setModel(lowQualityList);
        jListLowQuality.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            jButtonRemoveLowQuality.setEnabled(true);
                        }
                        if (jListLowQuality.getSelectedIndex() == -1) {
                            jButtonRemoveLowQuality.setEnabled(false);
                        }
                    }
                });

        jListPatternRoles.setModel(patternRolesList);
        jListPatternRoles.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            jButtonRenameSelectedPatternRole.setEnabled(true);
                            jButtonRemoveSelectedPatternRole.setEnabled(true);
                        }
                        if (jListPatternRoles.getSelectedIndex() == -1) {
                            jButtonRenameSelectedPatternRole.setEnabled(false);
                            jButtonRemoveSelectedPatternRole.setEnabled(false);
                        }
                    }
                });


    }

    private void initJTree() {

        TreeNodeData root = getData(true);
        jTree1 = new CustomTree(root);
        jTree1.setAutoscrolls(true);
        jScrollPane1.setViewportView(jTree1);
        jTree1.setExpandsSelectedPaths(true);
        jTree1.setCellRenderer(new MyRenderer());

        jTree1.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath selectionPath = jTree1.getSelectionPath();
                if (selectionPath != null) {
                    TreeNodeData selected = (TreeNodeData) selectionPath.getLastPathComponent();
                    if (selected.getId() == null) {
                        rightPanel.setVisible(false);
                        jScrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                    } else {
                        if (((TreeNodeData) selectionPath.getPathComponent(1)).getName().equals("User")) {
                            jPanel15.setVisible(false);
                            jPanel16.setVisible(false);
                            jPanel17.setVisible(false);
                            jPanel18.setVisible(false);
                            rightPanel.setPreferredSize(new Dimension(745, 1300));
                        } else {
                            jPanel15.setVisible(true);
                            jPanel16.setVisible(true);
                            jPanel17.setVisible(true);
                            jPanel18.setVisible(true);
                            rightPanel.setPreferredSize(new Dimension(745, 1800));
                        }
                        rightPanel.setVisible(true);
                        jScrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                        clearGUI();
                        getComponentDetails(selected.getId(), true);
                    }
                }
            }
        });


        jTree1.addMouseListener(new PopupListener());


        MenuItemActionListener l = new MenuItemActionListener(this);
        leafPopup = new JPopupMenu();
        //JMenuItem menuItem = new JMenuItem("Change Tier", new javax.swing.ImageIcon(getClass().getResource("/images/change16x16.png")));
        JMenuItem menuItem = new JMenuItem("Classify", new javax.swing.ImageIcon(getClass().getResource("/images/change16x16.png")));
        menuItem.addActionListener(l);
        leafPopup.add(menuItem);
        menuItem = new JMenuItem("Declassify", new javax.swing.ImageIcon(getClass().getResource("/images/declassify.png")));
        menuItem.addActionListener(l);
        leafPopup.add(menuItem);
        menuItem = new JMenuItem("Change Tier", new javax.swing.ImageIcon(getClass().getResource("/images/change16x16.png")));
        menuItem.addActionListener(l);
        leafPopup.add(menuItem);
        leafPopup.addSeparator();
        menuItem = new JMenuItem("Rename", new javax.swing.ImageIcon(getClass().getResource("/images/rename16x16.png")));
        menuItem.addActionListener(l);
        leafPopup.add(menuItem);
        menuItem = new JMenuItem("Delete", new javax.swing.ImageIcon(getClass().getResource("/images/delete16x16.png")));
        menuItem.addActionListener(l);
        leafPopup.add(menuItem);

        nodePopup = new JPopupMenu();
        menuItem = new JMenuItem("Add Component", new javax.swing.ImageIcon(getClass().getResource("/images/add16x16.png")));
        menuItem.addActionListener(l);
        nodePopup.add(menuItem);
        menuItem = new JMenuItem("Search", new javax.swing.ImageIcon(getClass().getResource("/images/search16x16.png")));
        menuItem.addActionListener(l);
        //nodePopup.add(menuItem);

        rootPopup = new JPopupMenu();
        menuItem = new JMenuItem("Search", new javax.swing.ImageIcon(getClass().getResource("/images/search16x16.png")));
        menuItem.addActionListener(l);
        rootPopup.add(menuItem);
    }

    private void addSelectedHighQualityAttributes(final String patternID, final Object[] highQualities, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                ArchitecturalPattern pattern = new ArchitecturalPattern(api);

                try {
                    pattern.addHighQualityAttributes(patternID, highQualities);
                    getArchitecturalPatternDetails(patternID, true);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during quality addition", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }
            }
        }.start();
    }

    private void addRoleToPattern(final String patternID, final String roleName, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                ArchitecturalPattern pattern = new ArchitecturalPattern(api);

                try {
                    pattern.addRoles(patternID, roleName);
                    getArchitecturalPatternDetails(patternID, true);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during role addition", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }
            }
        }.start();
    }

//    private void addSelectedPlayRoles(final String bcomponentID, final Object[] roles, final boolean showProgress) {
//        setProgressBar(true, showProgress);
//
//        new Thread() {
//
//            @Override
//            public void run() {
//                ReuseApi api = null;
//                try {
//                    api = new ReuseApi();
//                } catch (RepositoryException ex) {
//                    setProgressBar(false, showProgress);
//                    JOptionPane.showMessageDialog(Management.this, "Cannot find the repository", "Error", JOptionPane.ERROR_MESSAGE);
//                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (RepositoryConfigException ex) {
//                    setProgressBar(false, showProgress);
//                    JOptionPane.showMessageDialog(Management.this, "Cannot find the repository", "Error", JOptionPane.ERROR_MESSAGE);
//                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                BusinessComponent bcomp = new BusinessComponent(api);
//
//                try {
//                    bcomp.addPlayRoles(bcomponentID, roles);
//                    getBusinessComponentDetails(bcomponentID, true);
//                } catch (RepositoryException ex) {
//                    setProgressBar(false, showProgress);
//                    JOptionPane.showMessageDialog(Management.this, "Error during role addition", "Error", JOptionPane.ERROR_MESSAGE);
//                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
//                } finally {
//                    api.getDatabase().close();
//                }
//
//                //setProgressBar(false, showProgress);
//
//            }
//        }.start();
//    }
    private void addSelectedMediumQualityAttributes(final String patternID, final Object[] mediumQualities, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                ArchitecturalPattern pattern = new ArchitecturalPattern(api);

                try {
                    pattern.addMediumQualityAttributes(patternID, mediumQualities);
                    getArchitecturalPatternDetails(patternID, true);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during quality addition", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }
            }
        }.start();
    }

    private void addSelectedLowQualityAttributes(final String patternID, final Object[] lowQualities, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                ArchitecturalPattern pattern = new ArchitecturalPattern(api);

                try {
                    pattern.addLowQualityAttributes(patternID, lowQualities);
                    getArchitecturalPatternDetails(patternID, true);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during quality addition", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }
            }
        }.start();
    }

    private void removeHighQualityFromPattern(final String patternID, final Object[] selectedValues, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                ArchitecturalPattern pattern = new ArchitecturalPattern(api);

                try {
                    pattern.removeHighQualityAttributes(patternID, selectedValues);
                    getArchitecturalPatternDetails(patternID, true);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during quality removal", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }
            }
        }.start();


    }

    private void removeRolesFromPattern(final String patternID, final Object[] selectedValues, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                ArchitecturalPattern pattern = new ArchitecturalPattern(api);

                try {
                    pattern.removeRoles(patternID, selectedValues);
                    getArchitecturalPatternDetails(patternID, true);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during role removal", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }
            }
        }.start();
    }

    private void removePlayRolesFromBComponent(final String bcomponentID, final Object[] selectedValues, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                BusinessComponent bcomp = new BusinessComponent(api);

                try {
                    bcomp.removeRoles(bcomponentID, selectedValues);
                    getBusinessComponentDetails(bcomponentID, true);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during role removal", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }
            }
        }.start();


    }

    private void removeMediumQualityFromPattern(final String patternID, final Object[] selectedValues, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                ArchitecturalPattern pattern = new ArchitecturalPattern(api);

                try {
                    pattern.removeMediumQualityAttributes(patternID, selectedValues);
                    getArchitecturalPatternDetails(patternID, true);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during quality removal", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }
            }
        }.start();
    }

    private void removeLowQualityFromPattern(final String patternID, final Object[] selectedValues, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                ArchitecturalPattern pattern = new ArchitecturalPattern(api);

                try {
                    pattern.removeLowQualityAttributes(patternID, selectedValues);
                    getArchitecturalPatternDetails(patternID, true);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during quality removal", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }
            }
        }.start();
    }

    private void addRolesToBusinessComponent(final String bcomponentID, final SortedListKeyValuePatternModel selectedRolesList, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                BusinessComponent bcomp = new BusinessComponent(api);

                try {
                    bcomp.addPlaysRoles(bcomponentID, selectedRolesList);
                    getBusinessComponentDetails(bcomponentID, true);

                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during method addition", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }
            }
        }.start();
    }

//    private void testFunction() {
//        OpenSMEComponent comp = new OpenSMEComponent(getAPI(false));
//        try {
//            ArrayList<SearchComponentDataModel> res = comp.getComponentsInSearchList(new HashSet<String>(), null, "", "-1", "-1", "-1", null);
//            for (SearchComponentDataModel r : res) {
//                System.out.println(r);
//            }
//        } catch (RepositoryException ex) {
//            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (MalformedQueryException ex) {
//            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (QueryEvaluationException ex) {
//            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
//    }
    private class PopupListener extends MouseAdapter {

        public PopupListener() {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            //int selRow = jTree1.getRowForLocation(e.getX(), e.getY());            
            TreePath selPath = jTree1.getPathForLocation(e.getX(), e.getY());
            if (selPath != null) {
                jTree1.setSelectionPath(selPath);
                //System.out.println(selPath);
                maybeShowPopup((TreeNodeData) selPath.getLastPathComponent(), e);
            }
        }

        private void maybeShowPopup(TreeNodeData node, MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                if (node.getName().equals("Components")) {
                    rootPopup.show(e.getComponent(), e.getX(), e.getY());
                } else if (node.getChildren() == null) {
                    if (((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).isClassified()) {
                        ((JMenuItem) leafPopup.getSubElements()[0]).setEnabled(false);
                        ((JMenuItem) leafPopup.getSubElements()[1]).setEnabled(true);
                    } else {
                        ((JMenuItem) leafPopup.getSubElements()[0]).setEnabled(true);
                        ((JMenuItem) leafPopup.getSubElements()[1]).setEnabled(false);
                    }
                    leafPopup.show(e.getComponent(), e.getX(), e.getY());
                } else {
                    nodePopup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }
    }

    private class MenuItemActionListener implements ActionListener {

        JFrame frame;

        public MenuItemActionListener(JFrame frame) {
            this.frame = frame;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JMenuItem selectedMenu = (JMenuItem) e.getSource();
            //do actions....
            if (selectedMenu.getText().equals("Add Component")) {
                TreePath tierPath = jTree1.getSelectionPath();
                String name = (String) JOptionPane.showInputDialog(frame, "Give the name of the component", "Provide a name", JOptionPane.PLAIN_MESSAGE, null, null, null);
                while (name != null && name.trim().length() == 0) {
                    name = (String) JOptionPane.showInputDialog(frame, "Give the name of the component", "Provide a name", JOptionPane.PLAIN_MESSAGE, null, null, null);
                }
                if (name != null) {
                    if (((CustomTreeModel) jTree1.getModel()).containsComponentName(name)) {
                        JOptionPane.showMessageDialog(Management.this, "Component already exists", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        addComponent(tierPath, name, true, isNormalNode(jTree1.getSelectionPath()));
                    }
                }
            } else if (selectedMenu.getText().equals("Delete")) {
                TreePath leafPath = jTree1.getSelectionPath();
                int n = JOptionPane.showConfirmDialog(frame, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
                //yes -> 0, no -> 1
                if (n == 0) {
                    deleteComponent(leafPath, true, isNormalPath(leafPath));
                }
            } else if (selectedMenu.getText().equals("Rename")) {
                TreePath leafPath = jTree1.getSelectionPath();
                String exName = ((TreeNodeData) leafPath.getLastPathComponent()).getName();
                String name = (String) JOptionPane.showInputDialog(frame, "Give the new name", "Change Name", JOptionPane.PLAIN_MESSAGE, null, null, exName);
                while (name != null && name.trim().length() == 0) {
                    name = (String) JOptionPane.showInputDialog(frame, "Give the new name", "Change Name", JOptionPane.PLAIN_MESSAGE, null, null, exName);
                }
                if (name != null) {
                    if (((CustomTreeModel) jTree1.getModel()).containsComponentName(name)) {
                        JOptionPane.showMessageDialog(Management.this, "Component already exists", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        renameComponent(leafPath, name, true, isNormalPath(leafPath));
                    }
                }
            } else if (selectedMenu.getText().equals("Classify")) {
                MetaModelSelectPanel metaModelSelectPanel = new MetaModelSelectPanel(Management.this);
                int answer = JOptionPane.showConfirmDialog(Management.this, metaModelSelectPanel, "Select a MetaModel", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
                TreePath selectionPath = metaModelSelectPanel.jTreeMetaModel.getSelectionPath();
                while (answer == JOptionPane.OK_OPTION
                        && (selectionPath == null || !((TreeMetaModelNodeData) selectionPath.getLastPathComponent()).getType().equals("leaf"))) {
                    answer = JOptionPane.showConfirmDialog(Management.this, metaModelSelectPanel, "Select a MetaModel", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
                    selectionPath = metaModelSelectPanel.jTreeMetaModel.getSelectionPath();
                }
                if (answer != JOptionPane.CANCEL_OPTION && answer != JOptionPane.CLOSED_OPTION) {
                    String componentID = ((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId();
                    String metaClassID = ((TreeMetaModelNodeData) selectionPath.getLastPathComponent()).getId();
                    addComponentToMetaClass(componentID, metaClassID, true, isNormalPath(jTree1.getSelectionPath()));
                }

            } else if (selectedMenu.getText().equals("Declassify")) {
                String componentID = ((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId();
                int n = JOptionPane.showConfirmDialog(frame, "Are you sure?", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
                //yes -> 0, no -> 1
                if (n == 0) {
                    declassifyComponent(componentID, true, isNormalPath(jTree1.getSelectionPath()));
                }
            } else if (selectedMenu.getText().equals("Change Tier")) {
                TreePath leafPath = jTree1.getSelectionPath();
                ArrayList<String> possibilities = new ArrayList<String>();
                possibilities.add("Enterprise");
                possibilities.add("Resource");
                possibilities.add("User");
                possibilities.add("Workspace");
                possibilities.add("_Unknown");
                possibilities.remove(((TreeNodeData) leafPath.getPathComponent(1)).getName());
                String newTier = (String) JOptionPane.showInputDialog(frame, "Select the new Tier", "Select Tier", JOptionPane.PLAIN_MESSAGE, null, possibilities.toArray(), null);
//                System.out.println(newTier);
//                while (newTier == null) {
//                    newTier = (String) JOptionPane.showInputDialog(frame, "Select the new tier", "Select Tier", JOptionPane.PLAIN_MESSAGE, null, possibilities.toArray(), null);
//                }
//

                if (newTier != null) {
                    changeTier(leafPath, ((TreeNodeData) leafPath.getPathComponent(1)).getName(), newTier, true, !isNormalPath(jTree1.getSelectionPath()));
                }
            } else if (selectedMenu.getText().equals("Search")) {
                SearchComponent searchComponentDialog = new SearchComponent(
                        Management.this, languageList, technologyList);
                searchComponentDialog.pack();
                searchComponentDialog.setVisible(true);
            }
        }
    }

    private class MyRenderer extends DefaultTreeCellRenderer {

        ImageIcon leafIcon1, rootIcon, enterpriseIcon, userIcon, resourceIcon, workspaceIcon;

        public MyRenderer() {
            leafIcon1 = new javax.swing.ImageIcon(getClass().getResource("/images/tree-component16x16.png"));
            rootIcon = new javax.swing.ImageIcon(getClass().getResource("/images/components.png"));
            enterpriseIcon = new javax.swing.ImageIcon(getClass().getResource("/images/enterprise.png"));
            userIcon = new javax.swing.ImageIcon(getClass().getResource("/images/user16x16.png"));
            resourceIcon = new javax.swing.ImageIcon(getClass().getResource("/images/resource.png"));
            workspaceIcon = new javax.swing.ImageIcon(getClass().getResource("/images/workspace16x16.png"));
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            if (leaf) {
                setIcon(leafIcon1);
            } else if (((TreeNodeData) value).getName().equals("Components")) {
                setIcon(rootIcon);
                setText("Components");
            } else if (((TreeNodeData) value).getName().equals("Enterprise")) {
                setText("Enterprise");
                setIcon(enterpriseIcon);
            } else if (((TreeNodeData) value).getName().equals("User")) {
                setIcon(userIcon);
                setText("User");
            } else if (((TreeNodeData) value).getName().equals("Resource")) {
                setIcon(resourceIcon);
                setText("Resource");
            } else if (((TreeNodeData) value).getName().equals("Workspace")) {
                setIcon(workspaceIcon);
                setText("Workspace");
            }
            setPreferredSize(null);
            return this;
        }
    }

    private void addComponent(final TreePath tierPath, final String name, final boolean showProgress, final boolean shouldUpdate) {

        setProgressBar(true, showProgress);

        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);
                ComponentTreeInfo result = null;

                try {
                    result = comp.addComponent(((TreeNodeData) tierPath.getLastPathComponent()).getName(), name, shouldUpdate);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error adding the component", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (CompareInterfaceException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Web service error", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }


                TreeNodeData newNode = new TreeNodeData(result.getId(), result.getName());
                ArrayList<TreeNodeData> children = ((TreeNodeData) tierPath.getLastPathComponent()).getChildren();
                children.add(newNode);
                Collections.sort(children);
                //((TreeNodeData) tierPath.getLastPathComponent()).setChildren(children);

                ((CustomTreeModel) jTree1.getModel()).fireTreeNodesInserted(tierPath, newNode);
                //jTree1.scrollPathToVisible(tierPath.pathByAddingChild(newNode));
                jTree1.setSelectionPath(tierPath.pathByAddingChild(newNode));

                setProgressBar(false, showProgress);
            }
        });
    }

    //prepei na to diaxwrisw apo to display...
    public void getComponentDetails(final String id, final boolean showProgress) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);
                ComponentDetails result = null;

                try {
                    result = comp.getComponentDetails(id);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error getting component details", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MalformedQueryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error getting component details", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (QueryEvaluationException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error getting component details", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                displayComponentDetails(result);
                setProgressBar(false, showProgress);
            }
        }.start();
    }

    private void getArchitecturalPatternDetails(final String patternID, final boolean showProgress) {
        setProgressBar(true, showProgress);
        jListHighQuality.clearSelection();
        jListMediumQuality.clearSelection();
        jListLowQuality.clearSelection();
        jListPatternRoles.clearSelection();
        ((SortedListKeyValueModel) jListHighQuality.getModel()).clear();
        ((SortedListKeyValueModel) jListMediumQuality.getModel()).clear();
        ((SortedListKeyValueModel) jListLowQuality.getModel()).clear();
        ((SortedListKeyValueModel) jListPatternRoles.getModel()).clear();


        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                ArchitecturalPattern pattern = new ArchitecturalPattern(api);
                ArchitecturalPatternDetails result = null;

                try {
                    result = pattern.getPatternDetails(patternID);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error getting component details", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MalformedQueryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error getting component details", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (QueryEvaluationException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error getting component details", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                displayPatternsDetails(result);
                setProgressBar(false, showProgress);
            }
        }.start();
    }

    private void getBusinessComponentDetails(final String bcomponentID, final boolean showProgress) {
        setProgressBar(true, showProgress);
        enterpriseTierTable.clearSelection();
        resourceTierTable.clearSelection();
        userTierTable.clearSelection();
        workspaceTierTable.clearSelection();
        jListPlaysRole.clearSelection();
        jEditorPane1.setText("<html><body></body></html>");

        ((ComponentRelationTableModel) enterpriseTierTable.getModel()).clearAll();
        ((ComponentRelationTableModel) resourceTierTable.getModel()).clearAll();
        ((ComponentRelationTableModel) userTierTable.getModel()).clearAll();
        ((ComponentRelationTableModel) workspaceTierTable.getModel()).clearAll();
        ((SortedListKeyValueModel) jListPlaysRole.getModel()).clear();

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                BusinessComponent bcomp = new BusinessComponent(api);
                BusinessComponentDetails result = null;

                try {
                    result = bcomp.getBusinessComponentDetails(bcomponentID);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error getting business component details", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MalformedQueryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error getting business component details", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (QueryEvaluationException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error getting business component details", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                displayBusinessComponentDetails(result);
                setProgressBar(false, showProgress);
            }
        }.start();
    }

    private void displayComponentDetails(ComponentDetails details) {
        //populateTechnologyCombo();
        jTextFieldVersion.setText(details.getVersion());
        jTextFieldSvn.setText(details.getSvn());
        jTextFieldLicense.setText(details.getLicense());
        jTextAreaDescription.setText(details.getDescription());
        fromLoading = true;
        jComboBoxTechnology.setSelectedItem(details.getTechnology());
        jComboBoxLanguage.setSelectedItem(details.getLanguage());
        fromLoading = false;
        ((ComponentRelationTableModel) usesTable.getModel()).clearAll();
        ArrayList<ComponentRelationDataModel> uses = details.getUses();
        ComponentRelationTableModel usesModel = (ComponentRelationTableModel) usesTable.getModel();
        for (ComponentRelationDataModel c : uses) {
            usesModel.addRow(c);
        }
        ((ComponentRelationTableModel) usedByTable.getModel()).clearAll();
        ArrayList<ComponentRelationDataModel> usedBy = details.getUsedBy();
        ComponentRelationTableModel usedByModel = (ComponentRelationTableModel) usedByTable.getModel();
        for (ComponentRelationDataModel c : usedBy) {
            usedByModel.addRow(c);
        }
        ((ComponentRelationTableModel) callsTable.getModel()).clearAll();
        ArrayList<ComponentRelationDataModel> calls = details.getCalls();
        ComponentRelationTableModel callsModel = (ComponentRelationTableModel) callsTable.getModel();
        for (ComponentRelationDataModel c : calls) {
            callsModel.addRow(c);
        }
        ((ComponentRelationTableModel) calledByTable.getModel()).clearAll();
        ArrayList<ComponentRelationDataModel> calledBy = details.getCalledBy();
        ComponentRelationTableModel calledByModel = (ComponentRelationTableModel) calledByTable.getModel();
        for (ComponentRelationDataModel c : calledBy) {
            calledByModel.addRow(c);
        }
        ((InterfaceTableModel) providedInterfaceTable.getModel()).clearAll();
        ArrayList<InterfaceDataModel> InterfaceDTO = details.getProvidedInterfaces();
        InterfaceTableModel providedInterfaceModel = (InterfaceTableModel) providedInterfaceTable.getModel();
        for (InterfaceDataModel i : InterfaceDTO) {
            providedInterfaceModel.addRow(i);
        }
        if (setSelectedProvidedInterfaceID != null) {
            int objectRowWithID = providedInterfaceModel.getObjectRowWithID(setSelectedProvidedInterfaceID);
            providedInterfaceTable.setRowSelectionInterval(objectRowWithID, objectRowWithID);
            setSelectedProvidedInterfaceID = null;
        }
        ((InterfaceTableModel) requiredInterfaceTable.getModel()).clearAll();
        ArrayList<InterfaceDataModel> InterfaceDTO1 = details.getRequiredInterfaces();
        InterfaceTableModel requiredInterfaceModel = (InterfaceTableModel) requiredInterfaceTable.getModel();
        for (InterfaceDataModel i : InterfaceDTO1) {
            requiredInterfaceModel.addRow(i);
        }
        if (setSelectedRequiredInterfaceID != null) {
            int objectRowWithID = requiredInterfaceModel.getObjectRowWithID(setSelectedRequiredInterfaceID);
            requiredInterfaceTable.setRowSelectionInterval(objectRowWithID, objectRowWithID);
            setSelectedRequiredInterfaceID = null;
        }

        jTextFieldMetaModelName.setText("");
        if (details.getMetaClass() != null) {
            jTextFieldMetaModelName.setText(details.getMetaClass().getValue());
        }
        jTextFieldDomain.setText("");
        if (details.getDomain() != null) {
            jTextFieldDomain.setText(details.getDomain().getValue());
        }
        jListConcepts.clearSelection();
        ((SortedListKeyValueModel) jListConcepts.getModel()).clear();
        if (details.getConcepts() != null) {
            ((SortedListKeyValueModel) jListConcepts.getModel()).addAll(details.getConcepts().toArray());
        }
    }

    private void displayPatternsDetails(ArchitecturalPatternDetails details) {
        //((SortedListKeyValueModel) jListHighQuality.getModel()).clear();
        ArrayList<KeyValue> highQuality = details.getHighQuality();
        SortedListKeyValueModel r1 = (SortedListKeyValueModel) jListHighQuality.getModel();
        r1.addAll(highQuality.toArray());

        ArrayList<KeyValue> mediumQuality = details.getMediumQuality();
        SortedListKeyValueModel r2 = (SortedListKeyValueModel) jListMediumQuality.getModel();
        r2.addAll(mediumQuality.toArray());

        ArrayList<KeyValue> lowQuality = details.getLowQuality();
        SortedListKeyValueModel r3 = (SortedListKeyValueModel) jListLowQuality.getModel();
        r3.addAll(lowQuality.toArray());

        ArrayList<KeyValue> roles = details.getRoles();
        SortedListKeyValueModel r4 = (SortedListKeyValueModel) jListPatternRoles.getModel();
        r4.addAll(roles.toArray());
    }

    private void displayBusinessComponentDetails(BusinessComponentDetails details) {
        //((ComponentRelationTableModel) enterpriseTierTable.getModel()).clearAll();
        ArrayList<ComponentRelationDataModel> enterprises = details.getEnterpriseTier();
        ComponentRelationTableModel enterpriseModel = (ComponentRelationTableModel) enterpriseTierTable.getModel();
        for (ComponentRelationDataModel c : enterprises) {
            enterpriseModel.addRow(c);
        }
        //((ComponentRelationTableModel) resourceTierTable.getModel()).clearAll();
        ArrayList<ComponentRelationDataModel> resources = details.getResourceTier();
        ComponentRelationTableModel resourceModel = (ComponentRelationTableModel) resourceTierTable.getModel();
        for (ComponentRelationDataModel c : resources) {
            resourceModel.addRow(c);
        }
        //((ComponentRelationTableModel) userTierTable.getModel()).clearAll();
        ArrayList<ComponentRelationDataModel> users = details.getUserTier();
        ComponentRelationTableModel userModel = (ComponentRelationTableModel) userTierTable.getModel();
        for (ComponentRelationDataModel c : users) {
            userModel.addRow(c);
        }
        //((ComponentRelationTableModel) workspaceTierTable.getModel()).clearAll();
        ArrayList<ComponentRelationDataModel> workspaces = details.getWorkspaceTier();
        ComponentRelationTableModel workspaceModel = (ComponentRelationTableModel) workspaceTierTable.getModel();
        for (ComponentRelationDataModel c : workspaces) {
            workspaceModel.addRow(c);
        }

        ArrayList<KeyValue> roles = details.getRoles();
        SortedListKeyValueModel r4 = (SortedListKeyValueModel) jListPlaysRole.getModel();
        r4.addAll(roles.toArray());

    }

//    private void populateTechnologyCombo() {
//        ReuseApi api = new ReuseApi();
//        Technology comp = new Technology(api);
//        ArrayList<KeyValue> technologies = null;
//        try {
//            technologies = comp.getTechnologies();
//        } catch (RepositoryException ex) {
//            JOptionPane.showMessageDialog(Management.this, "Error populating technologies", "Error", JOptionPane.ERROR_MESSAGE);
//            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (MalformedQueryException ex) {
//            JOptionPane.showMessageDialog(Management.this, "Error populating technologies", "Error", JOptionPane.ERROR_MESSAGE);
//            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (QueryEvaluationException ex) {
//            JOptionPane.showMessageDialog(Management.this, "Error populating technologies", "Error", JOptionPane.ERROR_MESSAGE);
//            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            api.getDatabase().close();
//        }
//
////        jComboBoxTechnology.removeAllItems();
////        jComboBoxTechnology.addItem(new KeyValue("-1", ""));
////        for (KeyValue keyValue : technologies) {
////            jComboBoxTechnology.addItem(keyValue);
////        }
//        
//    }
    private void populateTechnologyListBox() {
        ReuseApi api = getAPI(false);
        Technology tech = new Technology(api);
        ArrayList<KeyValue> technologies = null;
        try {
            technologies = tech.getTechnologies();
        } catch (RepositoryException ex) {
            JOptionPane.showMessageDialog(Management.this, "Error populating technologies", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            JOptionPane.showMessageDialog(Management.this, "Error populating technologies", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            JOptionPane.showMessageDialog(Management.this, "Error populating technologies", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            api.getDatabase().close();
        }


        jListTechnologies.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            int viewRow = jListTechnologies.getSelectedIndex();
                            if (viewRow < 0) {
                            } else {
                                jButtonRenameSelectedTechnology.setEnabled(true);
                                jButtonRemoveSelectedTechnology.setEnabled(true);
                            }
                            if (jListTechnologies.getSelectedIndex() == -1) {
                                jButtonRenameSelectedTechnology.setEnabled(false);
                                jButtonRemoveSelectedTechnology.setEnabled(false);
                            }
                        }

                    }
                });

        //jListTechnologies.removeAll();
        technologyList = new SortedListKeyValueModel();
        for (KeyValue keyValue : technologies) {
            technologyList.add(keyValue);
        }
        jListTechnologies.setModel(technologyList);
        jComboBoxTechnology.setModel(technologyList);
    }

    private void populateLanguageListBox() {
        ReuseApi api = getAPI(false);
        Language lang = new Language(api);
        ArrayList<KeyValue> languages = null;
        try {
            languages = lang.getLanguages();
        } catch (RepositoryException ex) {
            JOptionPane.showMessageDialog(Management.this, "Error populating languages", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            JOptionPane.showMessageDialog(Management.this, "Error populating languages", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            JOptionPane.showMessageDialog(Management.this, "Error populating languages", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            api.getDatabase().close();
        }


        jListLanguages.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        //System.out.println(jListLanguages.getSelectedIndex());
                        if (!event.getValueIsAdjusting()) {
                            int viewRow = jListLanguages.getSelectedIndex();
                            if (viewRow < 0) {
                            } else {
                                jButtonRenameSelectedLanguage.setEnabled(true);
                                jButtonRemoveSelectedLanguage.setEnabled(true);
                            }
                            if (jListLanguages.getSelectedIndex() == -1) {
                                jButtonRenameSelectedLanguage.setEnabled(false);
                                jButtonRemoveSelectedLanguage.setEnabled(false);
                            }
                        }

                    }
                });
        languageList = new SortedListKeyValueModel();
        for (KeyValue keyValue : languages) {
            languageList.add(keyValue);
        }
        jListLanguages.setModel(languageList);
        jComboBoxLanguage.setModel(languageList);
    }

    private void deleteComponent(final TreePath leafPath, final boolean showProgress, final boolean shouldUpdate) {

        setProgressBar(true, showProgress);

        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    TreeNodeData nodeToDelete = (TreeNodeData) leafPath.getLastPathComponent();
                    comp.deleteComponent(nodeToDelete.getId(), shouldUpdate);
                    ArrayList<TreeNodeData> children = ((TreeNodeData) leafPath.getPathComponent(1)).getChildren();
                    int index = children.indexOf(nodeToDelete);
                    children.remove(nodeToDelete);
                    //((TreeNodeData) leafPath.getPathComponent(1)).setChildren(children);

                    ((CustomTreeModel) jTree1.getModel()).fireTreeNodesDeleted(leafPath.getParentPath(), nodeToDelete, index);
                    //jTree1.scrollPathToVisible(tierPath.pathByAddingChild(newNode));
                    jTree1.setSelectionPath(leafPath.getParentPath());
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during component deletion", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (CompareInterfaceException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Web service error", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MalformedQueryException ex) {
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (QueryEvaluationException ex) {
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                setProgressBar(false, showProgress);

            }
        });
    }

    private void renameComponent(final TreePath leafPath, final String newName, final boolean showProgress, final boolean shouldUpdate) {

        setProgressBar(true, showProgress);

        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);
                TreeNodeData nodeToRename = (TreeNodeData) leafPath.getLastPathComponent();

                try {
                    comp.renameComponent(nodeToRename.getId(), newName, shouldUpdate);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during component renaming", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (CompareInterfaceException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Web service error", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                nodeToRename.setName(newName);
                ArrayList<TreeNodeData> children = ((TreeNodeData) leafPath.getPathComponent(1)).getChildren();
                Collections.sort(children);
                int index = children.indexOf(nodeToRename);
                //((TreeNodeData) tierPath.getLastPathComponent()).setChildren(children);

                ((CustomTreeModel) jTree1.getModel()).fireTreeStructureChanged(leafPath, nodeToRename);
                jTree1.setSelectionPath(leafPath);
                //jTree1.scrollPathToVisible(tierPath.pathByAddingChild(newNode));
                //jTree1.setSelectionPath(leafPath.pathByAddingChild(newNode));

                //setProgressBar(false, showProgress);

            }
        });
    }

    private void setSvn(final String id, final String svnPath, final boolean showProgress, final boolean shouldUpdate) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.setSvn(id, svnPath, shouldUpdate);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during setting component svn", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                setProgressBar(false, showProgress);
            }
        }.start();
    }

    private void setVersion(final String id, final String versionText, final boolean showProgress) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.setVersion(id, versionText);
                } catch (RepositoryException ex) {

                    setProgressBar(false, showProgress);

                    JOptionPane.showMessageDialog(Management.this, "Error during setting component version", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                setProgressBar(false, showProgress);
            }
        }.start();
    }

    private void setLicense(final String id, final String licenseText, final boolean showProgress, final boolean shouldUpdate) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.setLicense(id, licenseText, shouldUpdate);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during setting component license", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (CompareInterfaceException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Web service error", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void setDescription(final String id, final String descriptionText, final boolean showProgress, final boolean shouldUpdate) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.setDescription(id, descriptionText, shouldUpdate);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during setting component description", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (CompareInterfaceException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Web service error", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void removeVersion(final String id, final boolean showProgress) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.removeVersion(id);
                } catch (RepositoryException ex) {

                    setProgressBar(false, showProgress);

                    JOptionPane.showMessageDialog(Management.this, "Error during component version deletion", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void removeSvn(final String id, final boolean showProgress, final boolean shouldUpdate) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.removeSvn(id, shouldUpdate);
                } catch (RepositoryException ex) {

                    setProgressBar(false, showProgress);

                    JOptionPane.showMessageDialog(Management.this, "Error during component svn deletion", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void removeLicense(final String id, final boolean showProgress, final boolean shouldUpdate) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.removeLicense(id, shouldUpdate);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during component license deletion", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (CompareInterfaceException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Web service error", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void removeDescription(final String id, final boolean showProgress, final boolean shouldUpdate) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.removeDescription(id, shouldUpdate);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during component description deletion", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (CompareInterfaceException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Web service error", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void addTechnology(final String name, final boolean showProgress) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                jButtonAddNewTechnology.setEnabled(false);
                ReuseApi api = getAPI(showProgress);
                Technology tech = new Technology(api);
                KeyValue result = null;
                try {
                    result = tech.addTechnology(name);
                } catch (RepositoryException ex) {

                    setProgressBar(false, showProgress);

                    JOptionPane.showMessageDialog(Management.this, "Error during technology addition", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                technologyList.add(result);
                jListTechnologies.clearSelection();
                jListTechnologies.setSelectedValue(result, true);
                setProgressBar(false, showProgress);
                setProgressBar(false, showProgress);
                jButtonAddNewTechnology.setEnabled(true);
            }
        }.start();
    }

    private void addLanguage(final String name, final boolean showProgress) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                jButtonAddNewLanguage.setEnabled(false);
                ReuseApi api = getAPI(showProgress);
                Language lang = new Language(api);
                KeyValue result = null;
                try {
                    result = lang.addLanguage(name);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during language addition", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                languageList.add(result);
                jListLanguages.clearSelection();
                jListLanguages.setSelectedValue(result, true);
                setProgressBar(false, showProgress);
                jButtonAddNewLanguage.setEnabled(true);
            }
        }.start();
    }

//    private void addRole(final String name, final boolean showProgress) {
//        setProgressBar(true, showProgress);
//
//        new Thread() {
//
//            @Override
//            public void run() {
//                jButtonAddNewPatternRole.setEnabled(false);
//                ReuseApi api = null;
//                try {
//                    api = new ReuseApi();
//                } catch (RepositoryException ex) {
//                    setProgressBar(false, showProgress);
//                    JOptionPane.showMessageDialog(Management.this, "Cannot find the repository", "Error", JOptionPane.ERROR_MESSAGE);
//                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (RepositoryConfigException ex) {
//                    setProgressBar(false, showProgress);
//                    JOptionPane.showMessageDialog(Management.this, "Cannot find the repository", "Error", JOptionPane.ERROR_MESSAGE);
//                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                Role attr = new Role(api);
//                KeyValue result = null;
//                try {
//                    result = attr.addRole(name);
//                } catch (RepositoryException ex) {
//                    setProgressBar(false, showProgress);
//                    JOptionPane.showMessageDialog(Management.this, "Error during role addition", "Error", JOptionPane.ERROR_MESSAGE);
//                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
//                } finally {
//                    api.getDatabase().close();
//                }
//
//                rolesList.add(result);
//                jListPatternRoles.clearSelection();
//                jListPatternRoles.setSelectedValue(result, true);
//                setProgressBar(false, showProgress);
//                jButtonAddNewPatternRole.setEnabled(true);
//            }
//        }.start();
//    }
    private void addQualityAttribute(final String name, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                jButtonAddNewQualityAttribute.setEnabled(false);
                ReuseApi api = getAPI(showProgress);
                QualityAttribute attr = new QualityAttribute(api);
                KeyValue result = null;
                try {
                    result = attr.addQualityAttribute(name);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during language addition", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                qualityAttributesList.add(result);
                jListQualityAttributes.clearSelection();
                jListQualityAttributes.setSelectedValue(result, true);
                setProgressBar(false, showProgress);
                //jButtonAddNewQualityAttribute.setEnabled(true);
            }
        }.start();
    }

    private void addArchitecturalPattern(final String name, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                jButtonAddNewArchitecturalPattern.setEnabled(false);
                ReuseApi api = getAPI(showProgress);
                ArchitecturalPattern pattern = new ArchitecturalPattern(api);
                KeyValue result = null;
                try {
                    result = pattern.addArchitecturalPattern(name);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during pattern addition", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                jListArchitecturalPatterns.clearSelection();
                ((SortedListKeyValueModel) jListArchitecturalPatterns.getModel()).add(result);
                jListArchitecturalPatterns.setSelectedValue(result, true);
                setProgressBar(false, showProgress);
                jButtonAddNewArchitecturalPattern.setEnabled(true);
            }
        }.start();
    }

    private void addBusinessComponent(final String name, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                jButtonAddNewBusinessComponent.setEnabled(false);
                ReuseApi api = getAPI(showProgress);
                BusinessComponent buss = new BusinessComponent(api);
                KeyValue result = null;
                try {
                    result = buss.addBusinessComponent(name);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during business component addition", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                jListBusinessComponents.clearSelection();
                ((SortedListKeyValueModel) jListBusinessComponents.getModel()).add(result);
                jListBusinessComponents.setSelectedValue(result, true);
                setProgressBar(false, showProgress);
                jButtonAddNewBusinessComponent.setEnabled(true);
            }
        }.start();
    }

    private void removeTechnology(final String techID, final int index, final boolean showProgress) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                jButtonRemoveSelectedTechnology.setEnabled(false);
                ReuseApi api = getAPI(showProgress);
                Technology tech = new Technology(api);
                try {
                    tech.removeTechnology(techID);
                } catch (RepositoryException ex) {

                    setProgressBar(false, showProgress);

                    JOptionPane.showMessageDialog(Management.this, "Error during technology deletion", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }
                KeyValue t = (KeyValue) technologyList.getElementAt(index);
                technologyList.removeElement(t);
                jListTechnologies.clearSelection();
                //jButtonRemoveSelectedTechnology.setEnabled(true);

                setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void removeLanguage(final String techID, final int index, final boolean showProgress) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                jButtonRemoveSelectedLanguage.setEnabled(false);
                ReuseApi api = getAPI(showProgress);
                Language lang = new Language(api);
                try {
                    lang.removeLanguage(techID);
                } catch (RepositoryException ex) {

                    setProgressBar(false, showProgress);

                    JOptionPane.showMessageDialog(Management.this, "Error during language deletion", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }
                KeyValue t = (KeyValue) languageList.getElementAt(index);
                languageList.removeElement(t);
                jListLanguages.clearSelection();
                //jButtonRemoveSelectedLanguage.setEnabled(true);

                setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void removeQualityAttribute(final String attrID, final int index, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                jButtonRemoveQualityAttribute.setEnabled(false);
                ReuseApi api = getAPI(showProgress);
                QualityAttribute attr = new QualityAttribute(api);
                try {
                    attr.removeQualityAttribute(attrID);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during attribute deletion", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }
                KeyValue t = (KeyValue) qualityAttributesList.getElementAt(index);
                qualityAttributesList.removeElement(t);
                jListQualityAttributes.clearSelection();
                //jButtonRemoveQualityAttribute.setEnabled(true);

                setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void removeArchitecturalPattern(final String patternID, final int index, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                jButtonRemoveSelectedArchitecturalPattern.setEnabled(false);
                ReuseApi api = getAPI(showProgress);
                ArchitecturalPattern p = new ArchitecturalPattern(api);
                try {
                    p.removePattern(patternID);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during pattern deletion", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MalformedQueryException ex) {
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (QueryEvaluationException ex) {
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }
                KeyValue t = (KeyValue) ((SortedListKeyValueModel) jListArchitecturalPatterns.getModel()).getElementAt(index);
                ((SortedListKeyValueModel) jListArchitecturalPatterns.getModel()).removeElement(t);
                jListArchitecturalPatterns.clearSelection();
                setProgressBar(false, showProgress);
            }
        }.start();
    }

    private void removeBusinessComponent(final String bcomponentID, final int index, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                jButtonRemoveSelectedBusinessComponent.setEnabled(false);
                ReuseApi api = getAPI(showProgress);
                BusinessComponent p = new BusinessComponent(api);
                try {
                    p.removeBusinessComponent(bcomponentID);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during business component deletion", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }
                KeyValue t = (KeyValue) ((SortedListKeyValueModel) jListBusinessComponents.getModel()).getElementAt(index);
                ((SortedListKeyValueModel) jListBusinessComponents.getModel()).removeElement(t);
                jListBusinessComponents.clearSelection();

                setProgressBar(false, showProgress);

            }
        }.start();
    }

//    private void removeRole(final String roleID, final int index, final boolean showProgress) {
//        setProgressBar(true, showProgress);
//
//        new Thread() {
//
//            @Override
//            public void run() {
//                jButtonRemoveSelectedPatternRole.setEnabled(false);
//                ReuseApi api = null;
//                try {
//                    api = new ReuseApi();
//                } catch (RepositoryException ex) {
//                    setProgressBar(false, showProgress);
//                    JOptionPane.showMessageDialog(Management.this, "Cannot find the repository", "Error", JOptionPane.ERROR_MESSAGE);
//                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (RepositoryConfigException ex) {
//                    setProgressBar(false, showProgress);
//                    JOptionPane.showMessageDialog(Management.this, "Cannot find the repository", "Error", JOptionPane.ERROR_MESSAGE);
//                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                Role role = new Role(api);
//                try {
//                    role.removeRole(roleID);
//                } catch (RepositoryException ex) {
//                    setProgressBar(false, showProgress);
//                    JOptionPane.showMessageDialog(Management.this, "Error during role deletion", "Error", JOptionPane.ERROR_MESSAGE);
//                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
//                } finally {
//                    api.getDatabase().close();
//                }
//                KeyValue t = (KeyValue) rolesList.getElementAt(index);
//                rolesList.removeElement(t);
//                jListPatternRoles.clearSelection();
//                //jButtonRemoveSelectedRole.setEnabled(true);
//
//                setProgressBar(false, showProgress);
//
//            }
//        }.start();
//    }
    private void renameTechnology(final String techID, final String name, final int index, final boolean showProgress) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                jButtonRenameSelectedTechnology.setEnabled(false);
                ReuseApi api = getAPI(showProgress);
                Technology tech = new Technology(api);
                try {
                    tech.renameTechnology(techID, name);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during technology renaming", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }
                KeyValue t = (KeyValue) technologyList.getElementAt(index);

                technologyList.removeElement(t);
                KeyValue newK = new KeyValue(t.getKey(), name);
                technologyList.add(newK);

                jListTechnologies.clearSelection();
                jListTechnologies.setSelectedValue(newK, true);
                //jButtonRenameSelectedTechnology.setEnabled(true);

                setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void renameArchitecturalPattern(final String patternID, final String name, final int index, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                jButtonRenameSelectedArchitecturalPattern.setEnabled(false);
                ReuseApi api = getAPI(showProgress);
                ArchitecturalPattern pattern = new ArchitecturalPattern(api);
                try {
                    pattern.renamePattern(patternID, name);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during technology renaming", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                jListArchitecturalPatterns.clearSelection();
                KeyValue t = (KeyValue) ((SortedListKeyValueModel) jListArchitecturalPatterns.getModel()).getElementAt(index);

                ((SortedListKeyValueModel) jListArchitecturalPatterns.getModel()).removeElement(t);
                KeyValue newK = new KeyValue(t.getKey(), name);
                ((SortedListKeyValueModel) jListArchitecturalPatterns.getModel()).add(newK);
                jListArchitecturalPatterns.setSelectedValue(newK, true);
                setProgressBar(false, showProgress);
            }
        }.start();
    }

    private void renameBusinessComponent(final String bcomponentID, final String name, final int index, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                jButtonRenameSelectedBusinessComponent.setEnabled(false);
                ReuseApi api = getAPI(showProgress);
                BusinessComponent bcomp = new BusinessComponent(api);
                try {
                    bcomp.renameBusinessComponent(bcomponentID, name);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during business component renaming", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                jListBusinessComponents.clearSelection();
                KeyValue t = (KeyValue) ((SortedListKeyValueModel) jListBusinessComponents.getModel()).getElementAt(index);

                ((SortedListKeyValueModel) jListBusinessComponents.getModel()).removeElement(t);
                KeyValue newK = new KeyValue(t.getKey(), name);
                ((SortedListKeyValueModel) jListBusinessComponents.getModel()).add(newK);
                jListBusinessComponents.setSelectedValue(newK, true);
                setProgressBar(false, showProgress);
            }
        }.start();
    }

    private void renameLanguage(final String techID, final String name, final int index, final boolean showProgress) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                jButtonRenameSelectedLanguage.setEnabled(false);
                ReuseApi api = getAPI(showProgress);
                Language lang = new Language(api);
                try {
                    lang.renameLanguage(techID, name);
                } catch (RepositoryException ex) {

                    setProgressBar(false, showProgress);

                    JOptionPane.showMessageDialog(Management.this, "Error during language renaming", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }
                KeyValue t = (KeyValue) languageList.getElementAt(index);

                languageList.removeElement(t);
                KeyValue newK = new KeyValue(t.getKey(), name);
                languageList.add(newK);

                jListLanguages.clearSelection();
                jListLanguages.setSelectedValue(newK, true);

                //jButtonRenameSelectedLanguage.setEnabled(true);

                setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void renameQualityAttribute(final String qualityID, final String name, final int index, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                jButtonRenameQualityAttribute.setEnabled(false);
                ReuseApi api = getAPI(showProgress);
                QualityAttribute attr = new QualityAttribute(api);
                try {
                    attr.renameLanguage(qualityID, name);
                } catch (RepositoryException ex) {

                    setProgressBar(false, showProgress);

                    JOptionPane.showMessageDialog(Management.this, "Error during language renaming", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }
                KeyValue t = (KeyValue) qualityAttributesList.getElementAt(index);

                qualityAttributesList.removeElement(t);
                KeyValue newK = new KeyValue(t.getKey(), name);
                qualityAttributesList.add(newK);

                jListQualityAttributes.clearSelection();
                jListQualityAttributes.setSelectedValue(newK, true);
                setProgressBar(false, showProgress);
            }
        }.start();
    }

    private void renameRole(final String roleID, final String name, final int index, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                jButtonRenameSelectedPatternRole.setEnabled(false);

                ReuseApi api = getAPI(showProgress);
                Role role = new Role(api);
                try {
                    role.renameRole(roleID, name);
                    getArchitecturalPatternDetails(((KeyValue) jListArchitecturalPatterns.getSelectedValue()).getKey(), true);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during role renaming", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }
//                KeyValue t = (KeyValue) rolesList.getElementAt(index);
//
//                rolesList.removeElement(t);
//                KeyValue newK = new KeyValue(t.getKey(), name);
//                rolesList.add(newK);
//
//                jListPatternRoles.clearSelection();
//                jListPatternRoles.setSelectedValue(newK, true);

                //jButtonRenameSelectedRole.setEnabled(true);

                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void setTechnology(final String id, final String key, final boolean showProgress) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.setTechnology(id, key);
                } catch (RepositoryException ex) {

                    setProgressBar(false, showProgress);

                    JOptionPane.showMessageDialog(Management.this, "Error during setting component technology", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                setProgressBar(false, showProgress);
            }
        }.start();
    }

    private void setLanguage(final String id, final String key, final boolean showProgress, final boolean shouldUpdate) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.setLanguage(id, key, shouldUpdate);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during setting component language", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (CompareInterfaceException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Web service error", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void removeTechnologyFromComponent(final String id, final boolean showProgress) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.removeTechnology(id);
                } catch (RepositoryException ex) {

                    setProgressBar(false, showProgress);

                    JOptionPane.showMessageDialog(Management.this, "Error during component technology deletion", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void removeLanguageFromComponent(final String id, final boolean showProgress, final boolean shouldUpdate) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.removeLanguage(id, shouldUpdate);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during component language deletion", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (CompareInterfaceException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Web service error", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                setProgressBar(false, showProgress);

            }
        }.start();
    }

    private TreeNodeData getData(final boolean showProgress) {
        ReuseApi api = getAPI(showProgress);
        OpenSMEComponent comp = new OpenSMEComponent(api);

        try {
            TreeNodeData root = new TreeNodeData(null, "Components");
            TreeNodeData enterprise = new TreeNodeData(null, "Enterprise");
            TreeNodeData resource = new TreeNodeData(null, "Resource");
            TreeNodeData user = new TreeNodeData(null, "User");
            TreeNodeData workspace = new TreeNodeData(null, "Workspace");
            TreeNodeData unknown = new TreeNodeData(null, "_Unknown");
            ArrayList<TreeNodeData> subroots = new ArrayList<TreeNodeData>();
            subroots.add(enterprise);
            subroots.add(resource);
            subroots.add(user);
            subroots.add(workspace);
            subroots.add(unknown);
            //Collections.sort(subroots);

            ArrayList<TreeNodeData> enterprises = comp.getComponents("Enterprise");
            Collections.sort(enterprises);

            ArrayList<TreeNodeData> resources = comp.getComponents("Resource");
            Collections.sort(resources);

            ArrayList<TreeNodeData> users = comp.getComponents("User");
            Collections.sort(users);

            ArrayList<TreeNodeData> workspaces = comp.getComponents("Workspace");
            Collections.sort(workspaces);

            ArrayList<TreeNodeData> unknowns = comp.getComponents("_Unknown");
            Collections.sort(unknowns);

            enterprise.setChildren(enterprises);
            resource.setChildren(resources);
            user.setChildren(users);
            workspace.setChildren(workspaces);
            unknown.setChildren(unknowns);

            root.setChildren(subroots);

            return root;
        } catch (RepositoryException ex) {
            JOptionPane.showMessageDialog(Management.this, "Error during fetching data", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            JOptionPane.showMessageDialog(Management.this, "Error during fetching data", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            JOptionPane.showMessageDialog(Management.this, "Error during fetching data", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            api.getDatabase().close();
        }
        return null;

    }

    private void clearGUI() {
        jTextFieldVersion.setText("");
        jTextFieldSvn.setText("");
        jTextFieldLicense.setText("");
        jTextFieldMetaModelName.setText("");
        jTextFieldDomain.setText("");
        jListConcepts.clearSelection();
        ((SortedListKeyValueModel) jListConcepts.getModel()).clear();
        fromLoading = true;
        jComboBoxTechnology.setSelectedIndex(-1);
        jComboBoxLanguage.setSelectedIndex(-1);
        fromLoading = false;
        usesTable.clearSelection();
        usedByTable.clearSelection();
        callsTable.clearSelection();
        calledByTable.clearSelection();
        providedInterfaceTable.clearSelection();
        providedInterfaceMethodsTable.clearSelection();
        requiredInterfaceTable.clearSelection();
        requiredInterfaceMethodsTable.clearSelection();


        ((ComponentRelationTableModel) usesTable.getModel()).clearAll();
        ((ComponentRelationTableModel) usedByTable.getModel()).clearAll();
        ((ComponentRelationTableModel) callsTable.getModel()).clearAll();
        ((ComponentRelationTableModel) calledByTable.getModel()).clearAll();
        ((InterfaceTableModel) providedInterfaceTable.getModel()).clearAll();
        ((MethodTableModel) providedInterfaceMethodsTable.getModel()).clearAll();
        ((InterfaceTableModel) requiredInterfaceTable.getModel()).clearAll();
        ((MethodTableModel) requiredInterfaceMethodsTable.getModel()).clearAll();
    }

    public void addSelectedComponentsUses(final List<SortedListNameVersionTierDataModel> uses, final boolean showProgress) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.setUsesComponents(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), uses);
//                    ComponentRelationTableModel usesModel = (ComponentRelationTableModel) usesTable.getModel();
//                    for (Object c : uses) {
//                        SortedListNameVersionTierDataModel c1 = (SortedListNameVersionTierDataModel) c;
//                        usesModel.addRow(new ComponentRelationDataModel(c1.getId(), c1.getName(), c1.getVersion(), c1.getTier()));
//                    }

                    Management.this.getComponentDetails(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), true);


                } catch (RepositoryException ex) {

                    setProgressBar(false, showProgress);

                    JOptionPane.showMessageDialog(Management.this, "Error during component addition", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    void addSelectedComponentsToEnterpriseTier(final List<SortedListNameVersionTierDataModel> enterpriseTier, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                BusinessComponent bcomp = new BusinessComponent(api);

                try {
                    bcomp.setEnterpriseComponents(((KeyValue) jListBusinessComponents.getSelectedValue()).getKey(), enterpriseTier);
                    Management.this.getBusinessComponentDetails(((KeyValue) jListBusinessComponents.getSelectedValue()).getKey(), true);


                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during component addition", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //should be deleted when i add the above call...
                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    void addSelectedComponentsToResourceTier(final List<SortedListNameVersionTierDataModel> resourceTier, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                BusinessComponent bcomp = new BusinessComponent(api);

                try {
                    bcomp.setResourceComponents(((KeyValue) jListBusinessComponents.getSelectedValue()).getKey(), resourceTier);
                    Management.this.getBusinessComponentDetails(((KeyValue) jListBusinessComponents.getSelectedValue()).getKey(), true);


                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during component addition", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //should be deleted when i add the above call...
                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    void addSelectedComponentsToUserTier(final List<SortedListNameVersionTierDataModel> userTier, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                BusinessComponent bcomp = new BusinessComponent(api);

                try {
                    bcomp.setUserComponents(((KeyValue) jListBusinessComponents.getSelectedValue()).getKey(), userTier);
                    Management.this.getBusinessComponentDetails(((KeyValue) jListBusinessComponents.getSelectedValue()).getKey(), true);


                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during component addition", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //should be deleted when i add the above call...
                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    void addSelectedComponentsToWorkspaceTier(final List<SortedListNameVersionTierDataModel> workspaceTier, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                BusinessComponent bcomp = new BusinessComponent(api);

                try {
                    bcomp.setWorkspaceComponents(((KeyValue) jListBusinessComponents.getSelectedValue()).getKey(), workspaceTier);
                    Management.this.getBusinessComponentDetails(((KeyValue) jListBusinessComponents.getSelectedValue()).getKey(), true);


                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during component addition", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //should be deleted when i add the above call...
                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    public void addSelectedComponentsUsedBy(final List<SortedListNameVersionTierDataModel> usedBy, final boolean showProgress) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.setUsedByComponents(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), usedBy);
//                    ComponentRelationTableModel usedByModel = (ComponentRelationTableModel) usedByTable.getModel();
//                    for (Object c : usedBy) {
//                        SortedListNameVersionTierDataModel c1 = (SortedListNameVersionTierDataModel) c;
//                        usedByModel.addRow(new ComponentRelationDataModel(c1.getId(), c1.getName(), c1.getVersion(), c1.getTier()));
//                    }

                    Management.this.getComponentDetails(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), true);

                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during adding component", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    public void addSelectedComponentsCalls(final List<SortedListNameVersionTierDataModel> calls, final boolean showProgress) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.setCallsComponents(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), calls);
//                    ComponentRelationTableModel callsModel = (ComponentRelationTableModel) callsTable.getModel();
//                    for (Object c : calls) {
//                        SortedListNameVersionTierDataModel c1 = (SortedListNameVersionTierDataModel) c;
//                        callsModel.addRow(new ComponentRelationDataModel(c1.getId(), c1.getName(), c1.getVersion(), c1.getTier()));
//                    }
                    Management.this.getComponentDetails(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), true);
                } catch (RepositoryException ex) {

                    setProgressBar(false, showProgress);

                    JOptionPane.showMessageDialog(Management.this, "Error during adding component", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    public void addSelectedComponentsCalledBy(final List<SortedListNameVersionTierDataModel> calledBy, final boolean showProgress) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.setCalledByComponents(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), calledBy);
//                    ComponentRelationTableModel calledByModel = (ComponentRelationTableModel) calledByTable.getModel();
//                    for (Object c : calledBy) {
//                        SortedListNameVersionTierDataModel c1 = (SortedListNameVersionTierDataModel) c;
//                        calledByModel.addRow(new ComponentRelationDataModel(c1.getId(), c1.getName(), c1.getVersion(), c1.getTier()));
//                    }
                    Management.this.getComponentDetails(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), true);
                } catch (RepositoryException ex) {

                    setProgressBar(false, showProgress);

                    JOptionPane.showMessageDialog(Management.this, "Error during adding component", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void removeFromUses(final ComponentRelationDataModel dataAtRow, final int modelIndex, final boolean showProgress) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.removeUses(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), dataAtRow.getId());
                    //((ComponentRelationTableModel) usesTable.getModel()).removeObjectAt(modelIndex);
                    Management.this.getComponentDetails(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), true);

                } catch (RepositoryException ex) {

                    setProgressBar(false, showProgress);

                    JOptionPane.showMessageDialog(Management.this, "Error during component removal", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void removeFromEnterprise(final ComponentRelationDataModel dataAtRow, final int modelIndex, final boolean showProgress) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                BusinessComponent bcomp = new BusinessComponent(api);

                try {
                    bcomp.removeEnterprise(((KeyValue) jListBusinessComponents.getSelectedValue()).getKey(), dataAtRow.getId());
                    Management.this.getBusinessComponentDetails(((KeyValue) jListBusinessComponents.getSelectedValue()).getKey(), true);

                } catch (RepositoryException ex) {

                    setProgressBar(false, showProgress);

                    JOptionPane.showMessageDialog(Management.this, "Error during component removal", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void removeFromResource(final ComponentRelationDataModel dataAtRow, final int modelIndex, final boolean showProgress) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                BusinessComponent bcomp = new BusinessComponent(api);

                try {
                    bcomp.removeResource(((KeyValue) jListBusinessComponents.getSelectedValue()).getKey(), dataAtRow.getId());
                    Management.this.getBusinessComponentDetails(((KeyValue) jListBusinessComponents.getSelectedValue()).getKey(), true);

                } catch (RepositoryException ex) {

                    setProgressBar(false, showProgress);

                    JOptionPane.showMessageDialog(Management.this, "Error during component removal", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void removeFromUser(final ComponentRelationDataModel dataAtRow, final int modelIndex, final boolean showProgress) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                BusinessComponent bcomp = new BusinessComponent(api);

                try {
                    bcomp.removeUser(((KeyValue) jListBusinessComponents.getSelectedValue()).getKey(), dataAtRow.getId());
                    Management.this.getBusinessComponentDetails(((KeyValue) jListBusinessComponents.getSelectedValue()).getKey(), true);

                } catch (RepositoryException ex) {

                    setProgressBar(false, showProgress);

                    JOptionPane.showMessageDialog(Management.this, "Error during component removal", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void removeFromWorkspace(final ComponentRelationDataModel dataAtRow, final int modelIndex, final boolean showProgress) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                BusinessComponent bcomp = new BusinessComponent(api);

                try {
                    bcomp.removeWorkspace(((KeyValue) jListBusinessComponents.getSelectedValue()).getKey(), dataAtRow.getId());
                    Management.this.getBusinessComponentDetails(((KeyValue) jListBusinessComponents.getSelectedValue()).getKey(), true);

                } catch (RepositoryException ex) {

                    setProgressBar(false, showProgress);

                    JOptionPane.showMessageDialog(Management.this, "Error during component removal", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void removeFromUsedBy(final ComponentRelationDataModel dataAtRow, final int modelIndex, final boolean showProgress) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.removeUsedBy(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), dataAtRow.getId());
                    //((ComponentRelationTableModel) usedByTable.getModel()).removeObjectAt(modelIndex);
                    Management.this.getComponentDetails(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), true);

                } catch (RepositoryException ex) {

                    setProgressBar(false, showProgress);

                    JOptionPane.showMessageDialog(Management.this, "Error during component removal", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void removeFromCalls(final ComponentRelationDataModel dataAtRow, final int modelIndex, final boolean showProgress) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.removeCalls(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), dataAtRow.getId());
                    //((ComponentRelationTableModel) callsTable.getModel()).removeObjectAt(modelIndex);
                    Management.this.getComponentDetails(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), true);

                } catch (RepositoryException ex) {

                    setProgressBar(false, showProgress);

                    JOptionPane.showMessageDialog(Management.this, "Error during component removal", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void removeFromCalledBy(final ComponentRelationDataModel dataAtRow, final int modelIndex, final boolean showProgress) {

        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.removeCalledBy(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), dataAtRow.getId());
                    //((ComponentRelationTableModel) calledByTable.getModel()).removeObjectAt(modelIndex);
                    Management.this.getComponentDetails(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), true);

                } catch (RepositoryException ex) {

                    setProgressBar(false, showProgress);

                    JOptionPane.showMessageDialog(Management.this, "Error during component removal", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void addProvidedInterfaceToComponent(final String id, final String name, final String version, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.setProvidedInterface(id, name, version);
                    Management.this.getComponentDetails(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), true);

                } catch (RepositoryException ex) {

                    setProgressBar(false, showProgress);

                    JOptionPane.showMessageDialog(Management.this, "Error during interface addition", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void addRequiredInterfaceToComponent(final String id, final String name, final String version, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.setRequiredInterface(id, name, version);
                    Management.this.getComponentDetails(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), true);

                } catch (RepositoryException ex) {

                    setProgressBar(false, showProgress);

                    JOptionPane.showMessageDialog(Management.this, "Error during interface addition", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void getProvidedInterfaceMethods(final String interfaceID, final boolean showProgress) {
        setProgressBar(true, showProgress);
        ((MethodTableModel) providedInterfaceMethodsTable.getModel()).clearAll();
        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEInterface inter = new OpenSMEInterface(api);

                try {
                    ArrayList<MethodDataModel> methods = inter.getProvidedInterfaceMethods(interfaceID);
                    MethodTableModel model = (MethodTableModel) providedInterfaceMethodsTable.getModel();
                    for (MethodDataModel m : methods) {
                        model.addRow(m);
                    }
                    if (setSelectedProvidedMethodID != null) {
                        int objectRowWithID = model.getObjectRowWithID(setSelectedProvidedMethodID);
                        providedInterfaceMethodsTable.setRowSelectionInterval(objectRowWithID, objectRowWithID);
                        setSelectedProvidedMethodID = null;
                    }

                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during fetching interfaces", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MalformedQueryException ex) {
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (QueryEvaluationException ex) {
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void getRequiredInterfaceMethods(final String interfaceID, final boolean showProgress) {
        setProgressBar(true, showProgress);
        ((MethodTableModel) requiredInterfaceMethodsTable.getModel()).clearAll();
        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEInterface inter = new OpenSMEInterface(api);

                try {
                    ArrayList<MethodDataModel> methods = inter.getRequiredInterfaceMethods(interfaceID);
                    MethodTableModel model = (MethodTableModel) requiredInterfaceMethodsTable.getModel();
                    for (MethodDataModel m : methods) {
                        model.addRow(m);
                    }
                    if (setSelectedRequiredMethodID != null) {
                        int objectRowWithID = model.getObjectRowWithID(setSelectedRequiredMethodID);
                        requiredInterfaceMethodsTable.setRowSelectionInterval(objectRowWithID, objectRowWithID);
                        setSelectedRequiredMethodID = null;
                    }

                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during fetching interfaces", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MalformedQueryException ex) {
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (QueryEvaluationException ex) {
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void addMethodToProvidedInterface(final String interfaceID, final String name, final String parameters, final String returns, final String thr, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEInterface inter = new OpenSMEInterface(api);

                try {
                    inter.addMethod(interfaceID, name, parameters, returns, thr);
                    getProvidedInterfaceMethods(interfaceID, true);

                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during method addition", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void addMethodToRequiredInterface(final String interfaceID, final String name, final String parameters, final String returns, final String thr, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEInterface inter = new OpenSMEInterface(api);

                try {
                    inter.addMethod(interfaceID, name, parameters, returns, thr);
                    getRequiredInterfaceMethods(interfaceID, true);

                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during method addition", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void removeRequiredMethodFromInterface(final String interfaceID, final String methodID, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEInterface inter = new OpenSMEInterface(api);

                try {
                    inter.removeMethod(methodID);
                    getRequiredInterfaceMethods(interfaceID, true);

                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during method removal", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void removeProvidedMethodFromInterface(final String interfaceID, final String methodID, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEInterface inter = new OpenSMEInterface(api);

                try {
                    inter.removeMethod(methodID);
                    getProvidedInterfaceMethods(interfaceID, true);

                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during method removal", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void removeProvidedInterfaceFromComponent(final String componentID, final String interfaceID, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEInterface inter = new OpenSMEInterface(api);

                try {
                    inter.removeProvidedInterface(interfaceID, true);
                    Management.this.getComponentDetails(componentID, true);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during interface removal", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MalformedQueryException ex) {
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (QueryEvaluationException ex) {
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void removeRequiredInterfaceFromComponent(final String componentID, final String interfaceID, final boolean showProgress) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEInterface inter = new OpenSMEInterface(api);

                try {
                    inter.removeRequiredInterface(interfaceID, true);
                    Management.this.getComponentDetails(componentID, true);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during interface removal", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MalformedQueryException ex) {
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (QueryEvaluationException ex) {
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void editProvidedInterface(final String interfaceID, final String name, final String version, final boolean showProgress) {
        setProgressBar(true, showProgress);
        setSelectedProvidedInterfaceID = interfaceID;

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEInterface inter = new OpenSMEInterface(api);

                try {
                    inter.editInterface(interfaceID, name, version);

                    Management.this.getComponentDetails(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), true);

                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during interface update", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();

    }

    private void editRequiredInterface(final String interfaceID, final String name, final String version, final boolean showProgress) {
        setProgressBar(true, showProgress);
        setSelectedRequiredInterfaceID = interfaceID;

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEInterface inter = new OpenSMEInterface(api);

                try {
                    inter.editInterface(interfaceID, name, version);

                    Management.this.getComponentDetails(((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).getId(), true);

                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during interface update", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();

    }

    private void editProvidedMethod(final String methodID, final String name, final String parameters, final String returns, final String thr, final boolean showProgress) {
        setProgressBar(true, showProgress);
        setSelectedProvidedMethodID = methodID;

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEInterface inter = new OpenSMEInterface(api);

                try {
                    inter.editMethod(methodID, name, parameters, returns, thr);

                    int row = providedInterfaceTable.getSelectedRow();
                    int modelRow = providedInterfaceTable.convertRowIndexToModel(row);
                    String interfaceID = ((InterfaceTableModel) providedInterfaceTable.getModel()).getDataAtRow(modelRow).getId();
                    getProvidedInterfaceMethods(interfaceID, true);

                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during method update removal", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();

    }

    private void editRequiredMethod(final String methodID, final String name, final String parameters, final String returns, final String thr, final boolean showProgress) {
        setProgressBar(true, showProgress);
        setSelectedRequiredMethodID = methodID;

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEInterface inter = new OpenSMEInterface(api);

                try {
                    inter.editMethod(methodID, name, parameters, returns, thr);

                    int row = requiredInterfaceTable.getSelectedRow();
                    int modelRow = requiredInterfaceTable.convertRowIndexToModel(row);
                    String interfaceID = ((InterfaceTableModel) requiredInterfaceTable.getModel()).getDataAtRow(modelRow).getId();
                    getRequiredInterfaceMethods(interfaceID, true);

                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during method update removal", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                //setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void changeTier(final TreePath leafPath, final String oldTier, final String newTier, final boolean showProgress, final boolean shouldUpdate) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {

                    comp.changeTier(((TreeNodeData) leafPath.getLastPathComponent()).getId(), oldTier, newTier, shouldUpdate);
                    TreeNodeData nodeToClassify = (TreeNodeData) leafPath.getLastPathComponent();

                    ArrayList<TreeNodeData> children = ((TreeNodeData) leafPath.getPathComponent(1)).getChildren();
                    int index = children.indexOf(nodeToClassify);
                    children.remove(nodeToClassify);
                    ((CustomTreeModel) jTree1.getModel()).fireTreeNodesDeleted(leafPath.getParentPath(), nodeToClassify, index);

                    TreeNodeData root = (TreeNodeData) ((CustomTreeModel) jTree1.getModel()).getRoot();
                    ArrayList<TreeNodeData> categories = root.getChildren();
                    for (TreeNodeData category : categories) {
                        if (category.getName().equals(newTier)) {
                            ArrayList<TreeNodeData> children1 = category.getChildren();
                            TreeNodeData newNode = new TreeNodeData(((TreeNodeData) leafPath.getLastPathComponent()).getId(), ((TreeNodeData) leafPath.getLastPathComponent()).getName());
                            children1.add(newNode);
                            Collections.sort(children1);
                            ((CustomTreeModel) jTree1.getModel()).fireTreeNodesInserted(leafPath.getParentPath().getParentPath().pathByAddingChild(category), newNode);
                            jTree1.setSelectionPath(leafPath.getParentPath().getParentPath().pathByAddingChild(category).pathByAddingChild(newNode));
                            break;
                        }
                    }

                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during component re-classification", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MalformedQueryException ex) {
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (QueryEvaluationException ex) {
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }

                setProgressBar(false, showProgress);

            }
        }.start();
    }

    private void addComponentToMetaClass(final String componentID, final String metaClassID, final boolean showProgress, final boolean shouldUpdate) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.addComponentToMetaClass(componentID, metaClassID, shouldUpdate);
                    getComponentDetails(componentID, true);
                    ((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).setClassified(true);

                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during classification", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MalformedQueryException ex) {
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (QueryEvaluationException ex) {
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvalidTierException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, ex.getMessage(), "Classification Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    api.getDatabase().close();
                }

            }
        }.start();
    }

    private void declassifyComponent(final String componentID, final boolean showProgress, final boolean shouldUpdate) {
        setProgressBar(true, showProgress);

        new Thread() {

            @Override
            public void run() {
                ReuseApi api = getAPI(showProgress);
                OpenSMEComponent comp = new OpenSMEComponent(api);

                try {
                    comp.declassifyComponent(componentID, shouldUpdate);
                    getComponentDetails(componentID, true);
                    ((TreeNodeData) jTree1.getSelectionPath().getLastPathComponent()).setClassified(false);
                } catch (MalformedQueryException ex) {
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (QueryEvaluationException ex) {
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } catch (RepositoryException ex) {
                    setProgressBar(false, showProgress);
                    JOptionPane.showMessageDialog(Management.this, "Error during classification", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    api.getDatabase().close();
                }
            }
        }.start();
    }

    private class ImageRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            int modelRow = table.convertRowIndexToModel(row);
            ComponentRelationDataModel data = ((ComponentRelationTableModel) table.getModel()).getDataAtRow(modelRow);

            if (!data.isExplicit()) {
                setForeground(Color.BLUE);
//                ToolTipManager tooltip = ToolTipManager.sharedInstance();
//                tooltip.registerComponent(this);
//                tooltip.setInitialDelay(10);
                setToolTipText("<html><body><h3>This is an inferred relationship</h3></body></html>");

            } else {
                setForeground(Color.BLACK);
                setToolTipText(null);
            }
            return cell;
        }
    }

    private void setProgressBar(boolean show, boolean showProgress) {
        if (showProgress) {
            glass.setVisible(show);
            progressBar.setVisible(show);


        }
    }

    public class TextAreaRenderer extends JTextArea implements TableCellRenderer {

        private final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        // Column heights are placed in this Map 
        private final Map<JTable, Map<Object, Map<Object, Integer>>> tablecellSizes = new HashMap<JTable, Map<Object, Map<Object, Integer>>>();

        /** 
         * Creates a text area renderer. 
         */
        public TextAreaRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            // set the Font, Color, etc. 
            renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            //setForeground(renderer.getForeground());
            setBackground(renderer.getBackground());
            setBorder(renderer.getBorder());
            setFont(renderer.getFont());
            setText(renderer.getText());


            if (isSelected) {
                setBackground(Color.LIGHT_GRAY);
            } else {
                setBackground(Color.white);
            }


            int modelRow = table.convertRowIndexToModel(row);
            ComponentRelationDataModel data = ((ComponentRelationTableModel) table.getModel()).getDataAtRow(modelRow);

            if (!data.isExplicit()) {
                setForeground(Color.BLUE);
//                ToolTipManager tooltip = ToolTipManager.sharedInstance();
//                tooltip.registerComponent(this);
//                tooltip.setInitialDelay(10);
                setToolTipText("<html><body><h3>This is an inferred relationship</h3></body></html>");

            } else {
                setForeground(Color.BLACK);
                setToolTipText(null);
            }



            TableColumnModel columnModel = table.getColumnModel();
            setSize(columnModel.getColumn(column).getWidth(), 0);
            int height_wanted = (int) getPreferredSize().getHeight();
            addSize(table, row, column, height_wanted);
            height_wanted = findTotalMaximumRowSize(table, row);

            if (height_wanted != table.getRowHeight(row)) {
                table.setRowHeight(row, height_wanted);
            }
            return this;
        }

        private void addSize(JTable table, int row, int column, int height) {
            Map<Object, Map<Object, Integer>> rowsMap = tablecellSizes.get(table);
            if (rowsMap == null) {
                tablecellSizes.put(table, rowsMap = new HashMap<Object, Map<Object, Integer>>());
            }
            Map<Object, Integer> rowheightsMap = rowsMap.get(row);
            if (rowheightsMap == null) {
                rowsMap.put(row, rowheightsMap = new HashMap<Object, Integer>());
            }
            rowheightsMap.put(column, height);
        }

        private int findTotalMaximumRowSize(JTable table, int row) {
            int maximum_height = 0;
            Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
            while (columns.hasMoreElements()) {
                TableColumn tc = columns.nextElement();
                TableCellRenderer cellRenderer = tc.getCellRenderer();
                if (cellRenderer instanceof TextAreaRenderer) {
                    TextAreaRenderer tar = (TextAreaRenderer) cellRenderer;
                    maximum_height = Math.max(maximum_height,
                            tar.findMaximumRowSize(table, row));
                }
            }
            return maximum_height;
        }

        private int findMaximumRowSize(JTable table, int row) {
            Map<Object, Map<Object, Integer>> rows = tablecellSizes.get(table);
            if (rows == null) {
                return 0;
            }
            Map<Object, Integer> rowheights = rows.get(row);
            if (rowheights == null) {
                return 0;
            }
            int maximum_height = 0;
            for (Map.Entry<Object, Integer> entry : rowheights.entrySet()) {
                int cellHeight = entry.getValue();
                maximum_height = Math.max(maximum_height, cellHeight);
            }
            return maximum_height;
        }
    }

    public class TextAreaRenderer2 extends JTextArea implements TableCellRenderer {

        private final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        // Column heights are placed in this Map 
        private final Map<JTable, Map<Object, Map<Object, Integer>>> tablecellSizes = new HashMap<JTable, Map<Object, Map<Object, Integer>>>();

        /** 
         * Creates a text area renderer. 
         */
        public TextAreaRenderer2() {
            setLineWrap(true);
            setWrapStyleWord(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            // set the Font, Color, etc. 
            renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            //setForeground(renderer.getForeground());
            setBackground(renderer.getBackground());
            setBorder(renderer.getBorder());
            setFont(renderer.getFont());
            setText(renderer.getText());

            
            if (isSelected) {
                setBackground(Color.LIGHT_GRAY);
            } else {
                setBackground(Color.white);
            }
            
            TableColumnModel columnModel = table.getColumnModel();
            setSize(columnModel.getColumn(column).getWidth(), 0);
            int height_wanted = (int) getPreferredSize().getHeight();
            addSize(table, row, column, height_wanted);
            height_wanted = findTotalMaximumRowSize(table, row);

            if (height_wanted != table.getRowHeight(row)) {
                table.setRowHeight(row, height_wanted);
            }
            return this;
        }

        private void addSize(JTable table, int row, int column, int height) {
            Map<Object, Map<Object, Integer>> rowsMap = tablecellSizes.get(table);
            if (rowsMap == null) {
                tablecellSizes.put(table, rowsMap = new HashMap<Object, Map<Object, Integer>>());
            }
            Map<Object, Integer> rowheightsMap = rowsMap.get(row);
            if (rowheightsMap == null) {
                rowsMap.put(row, rowheightsMap = new HashMap<Object, Integer>());
            }
            rowheightsMap.put(column, height);
        }

        private int findTotalMaximumRowSize(JTable table, int row) {
            int maximum_height = 0;
            Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
            while (columns.hasMoreElements()) {
                TableColumn tc = columns.nextElement();
                TableCellRenderer cellRenderer = tc.getCellRenderer();
                if (cellRenderer instanceof TextAreaRenderer2) {
                    TextAreaRenderer2 tar = (TextAreaRenderer2) cellRenderer;
                    maximum_height = Math.max(maximum_height,
                            tar.findMaximumRowSize(table, row));
                }
            }
            return maximum_height;
        }

        private int findMaximumRowSize(JTable table, int row) {
            Map<Object, Map<Object, Integer>> rows = tablecellSizes.get(table);
            if (rows == null) {
                return 0;
            }
            Map<Object, Integer> rowheights = rows.get(row);
            if (rowheights == null) {
                return 0;
            }
            int maximum_height = 0;
            for (Map.Entry<Object, Integer> entry : rowheights.entrySet()) {
                int cellHeight = entry.getValue();
                maximum_height = Math.max(maximum_height, cellHeight);
            }
            return maximum_height;
        }
    }

    private boolean isNormalPath(TreePath path) {
        return !((TreeNodeData) path.getParentPath().getLastPathComponent()).getName().equals("_Unknown");
    }

    private boolean isNormalNode(TreePath path) {
        return !((TreeNodeData) path.getLastPathComponent()).getName().equals("_Unknown");
    }

    private ReuseApi getAPI(boolean showProgress) {
        ReuseApi api = null;
        try {
            api = new ReuseApi();
        } catch (RepositoryException ex) {
            setProgressBar(false, showProgress);
            JOptionPane.showMessageDialog(Management.this, "Cannot find the repository", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RepositoryConfigException ex) {
            setProgressBar(false, showProgress);
            JOptionPane.showMessageDialog(Management.this, "Cannot find the repository", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            setProgressBar(false, showProgress);
            JOptionPane.showMessageDialog(Management.this, "Cannot find the repository", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Management.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return api;
        }
    }
}
