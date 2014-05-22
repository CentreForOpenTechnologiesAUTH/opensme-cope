/**
 * @author Nikolaos Tsantalis

 */

package gr.pattdetection.java.pattern.gui;

import gr.pattdetection.java.pattern.BehavioralData;
import gr.pattdetection.java.pattern.MatrixContainer;
import gr.pattdetection.java.pattern.PatternDescriptor;
import gr.pattdetection.java.pattern.PatternEnum;
import gr.pattdetection.java.pattern.PatternGenerator;
import gr.pattdetection.java.pattern.SystemGenerator;
import gr.pattdetection.java.pattern.TsantalisPatternInstance;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

import gr.pattdetection.java.pattern.gui.progress.ProgressListener;
import gr.pattdetection.java.pattern.gui.progress.DetectionFinishedEvent;
import gr.pattdetection.java.pattern.gui.progress.ProgressObserver;
import gr.pattdetection.java.pattern.gui.progress.PatternDetectionSource;
import gr.bytecodereader.java.bytecode.BytecodeReader;
import gr.bytecodereader.java.bytecode.FieldObject;
import gr.bytecodereader.java.bytecode.MethodObject;
import gr.bytecodereader.java.bytecode.SystemObject;

public class MatrixFrame extends JFrame implements ActionListener, InternalFrameListener, ProgressListener {
	private static JDesktopPane desktop;
	private static JFileChooser fc;
	private JMenuItem openDir;
    private JMenuItem exportXML;
    private JRadioButtonMenuItem allPatternsMenuItem;
    private JRadioButtonMenuItem singletonMenuItem;
    private JRadioButtonMenuItem templateMenuItem;
    private JRadioButtonMenuItem factoryMethodMenuItem;
    private MatrixInternalFrame activeInternalFrame;
	//Map that has as key the internalFrame title
	//and as value the SystemObject of the corresponding system
	private Map<String,SystemGenerator> systemGeneratorMap;
    //Map that has as key the internalFrame title
	//and as value the Map of all the detected pattern instances
    //(key is the pattern name and value is a vector of PatternInstance objects)
    private Map<String,LinkedHashMap<String, Vector<TsantalisPatternInstance>>> detectedPatternsMap;

    private ProgressObserver progressObserver;

    public static void main(String[] args) {
        if(args.length == 4) {
            if(args[0].equals("-target") && args[2].equals("-output")) {
                File inputDir = new File(args[1]);
                File outputXML = new File(args[3]);
                new Console(inputDir,outputXML);
            }
            else {
                System.out.println("Usage: java -Xms32m -Xmx512m -jar pattern2.jar -target \"c:\\foo\\myclasses\" -output \"c:\\foo\\build\\pattern-detector-output.xml\"");
            }
        }
        else {
            MatrixFrame frame = new MatrixFrame();
        }
    }
	
	public MatrixFrame() {
		super("Design Pattern detection v4.5");
		systemGeneratorMap = new HashMap<String,SystemGenerator>();
        detectedPatternsMap = new HashMap<String,LinkedHashMap<String, Vector<TsantalisPatternInstance>>>();
        progressObserver = new ProgressObserver();
        progressObserver.addProgressListener(this);

        JFrame.setDefaultLookAndFeelDecorated(true);
		desktop = new JDesktopPane();
		fc = new JFileChooser();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(createMenuBar());
        setContentPane(desktop);
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle bounds = env.getMaximumWindowBounds();
		this.setSize(bounds.getSize());
        //this.setLocationRelativeTo(null);
        setVisible(true);
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
	}
	
	private JMenuBar createMenuBar() {
		JMenuBar menuBar;
		JMenu fileMenu;
		
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		
		openDir = new JMenuItem("Open Directory");
		openDir.addActionListener(this);
        fileMenu.add(openDir);

        exportXML = new JMenuItem("Export as XML");
		exportXML.addActionListener(this);
        fileMenu.add(exportXML);

        JMenu patternMenu  = new JMenu("Patterns");
        menuBar.add(patternMenu);
        
        ButtonGroup patternGroup = new ButtonGroup();

        JMenu structuralPatternMenu = new JMenu("Structural");

        JRadioButtonMenuItem adapterMenuItem = new JRadioButtonMenuItem(PatternEnum.ADAPTER_COMMAND.toString());
        adapterMenuItem.addActionListener(this);
        patternGroup.add(adapterMenuItem);
        structuralPatternMenu.add(adapterMenuItem);

        JRadioButtonMenuItem compositeMenuItem = new JRadioButtonMenuItem(PatternEnum.COMPOSITE.toString());
        compositeMenuItem.addActionListener(this);
        patternGroup.add(compositeMenuItem);
        structuralPatternMenu.add(compositeMenuItem);

        JRadioButtonMenuItem decoratorMenuItem = new JRadioButtonMenuItem(PatternEnum.DECORATOR.toString());
        decoratorMenuItem.addActionListener(this);
        patternGroup.add(decoratorMenuItem);
        structuralPatternMenu.add(decoratorMenuItem);
        
        JRadioButtonMenuItem proxyMenuItem = new JRadioButtonMenuItem(PatternEnum.PROXY.toString());
        proxyMenuItem.addActionListener(this);
        patternGroup.add(proxyMenuItem);
        structuralPatternMenu.add(proxyMenuItem);
        
        JRadioButtonMenuItem proxy2MenuItem = new JRadioButtonMenuItem(PatternEnum.PROXY2.toString());
        proxy2MenuItem.addActionListener(this);
        patternGroup.add(proxy2MenuItem);
        structuralPatternMenu.add(proxy2MenuItem);
        
        /*JRadioButtonMenuItem redirectInFamilyMenuItem = new JRadioButtonMenuItem(PatternEnum.REDIRECT_IN_FAMILY.toString());
        redirectInFamilyMenuItem.addActionListener(this);
        patternGroup.add(redirectInFamilyMenuItem);
        structuralPatternMenu.add(redirectInFamilyMenuItem);*/

        JMenu behavioralPatternMenu = new JMenu("Behavioral");

        JRadioButtonMenuItem observerMenuItem = new JRadioButtonMenuItem(PatternEnum.OBSERVER.toString());
        observerMenuItem.addActionListener(this);
        patternGroup.add(observerMenuItem);
        behavioralPatternMenu.add(observerMenuItem);

        JRadioButtonMenuItem stateMenuItem = new JRadioButtonMenuItem(PatternEnum.STATE_STRATEGY.toString());
        stateMenuItem.addActionListener(this);
        patternGroup.add(stateMenuItem);
        behavioralPatternMenu.add(stateMenuItem);

        templateMenuItem = new JRadioButtonMenuItem(PatternEnum.TEMPLATE_METHOD.toString());
        templateMenuItem.addActionListener(this);
        patternGroup.add(templateMenuItem);
        behavioralPatternMenu.add(templateMenuItem);

        JRadioButtonMenuItem visitorMenuItem = new JRadioButtonMenuItem(PatternEnum.VISITOR.toString());
        visitorMenuItem.addActionListener(this);
        patternGroup.add(visitorMenuItem);
        behavioralPatternMenu.add(visitorMenuItem);

        JMenu creationalPatternMenu = new JMenu("Creational");

        factoryMethodMenuItem = new JRadioButtonMenuItem(PatternEnum.FACTORY_METHOD.toString());
        factoryMethodMenuItem.addActionListener(this);
        patternGroup.add(factoryMethodMenuItem);
        creationalPatternMenu.add(factoryMethodMenuItem);

        JRadioButtonMenuItem prototypeMenuItem = new JRadioButtonMenuItem(PatternEnum.PROTOTYPE.toString());
        prototypeMenuItem.addActionListener(this);
        patternGroup.add(prototypeMenuItem);
        creationalPatternMenu.add(prototypeMenuItem);

        singletonMenuItem = new JRadioButtonMenuItem(PatternEnum.SINGLETON.toString());
        singletonMenuItem.addActionListener(this);
        patternGroup.add(singletonMenuItem);
        creationalPatternMenu.add(singletonMenuItem);

        allPatternsMenuItem = new JRadioButtonMenuItem("ALL");
        allPatternsMenuItem.addActionListener(this);

        patternMenu.add(allPatternsMenuItem);
        patternMenu.add(creationalPatternMenu);
        patternMenu.add(structuralPatternMenu);
        patternMenu.add(behavioralPatternMenu);

        return menuBar;
	}
	
	public void actionPerformed(ActionEvent e) {		
	}

    public void internalFrameActivated(InternalFrameEvent e) {
		activeInternalFrame = (MatrixInternalFrame)e.getInternalFrame();
		//System.out.println(e.getInternalFrame().getTitle() + " activated");
	}

 	public void internalFrameClosed(InternalFrameEvent e) {
 		//System.out.println(e.getInternalFrame().getTitle() + " closed");
 	}

 	public void internalFrameClosing(InternalFrameEvent e) {
 		//System.out.println(e.getInternalFrame().getTitle() + " closing");
 	}

 	public void internalFrameDeactivated(InternalFrameEvent e) {
 		//System.out.println(e.getInternalFrame().getTitle() + " deactivated");
 	}

 	public void internalFrameDeiconified(InternalFrameEvent e) {
 		//System.out.println(e.getInternalFrame().getTitle() + " deiconified");
 	}

 	public void internalFrameIconified(InternalFrameEvent e) {
 		//System.out.println(e.getInternalFrame().getTitle() + " iconified");
 	}

 	public void internalFrameOpened(InternalFrameEvent e) {
 		//System.out.println(e.getInternalFrame().getTitle() + " opened");
 	}

    public void detectionFinished(DetectionFinishedEvent event) {
        String activeInternalFrameTitle = activeInternalFrame.getTitle();
        PatternDetectionSource source = (PatternDetectionSource)event.getSource();
        if(detectedPatternsMap.containsKey(activeInternalFrameTitle)) {
            LinkedHashMap<String, Vector<TsantalisPatternInstance>> map = detectedPatternsMap.get(activeInternalFrameTitle);
            map.put(source.getPatternName(),source.getPatternInstanceVector());
        }
        else {
            LinkedHashMap<String, Vector<TsantalisPatternInstance>> map = new LinkedHashMap<String, Vector<TsantalisPatternInstance>>();
            map.put(source.getPatternName(),source.getPatternInstanceVector());
            detectedPatternsMap.put(activeInternalFrameTitle, map);
        }
        //activeInternalFrame.addRow(source.getPatternName(), new JComboBox(source.getPatternInstanceVector()));
        activeInternalFrame.addPatternNode(source.getPatternName(), source.getPatternInstanceVector());
    }
}