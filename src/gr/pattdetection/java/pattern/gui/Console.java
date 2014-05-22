/**
 * @author Nikolaos Tsantalis

 */

package gr.pattdetection.java.pattern.gui;

import gr.pattdetection.java.pattern.BehavioralData;
import gr.pattdetection.java.pattern.ClusterResult;
import gr.pattdetection.java.pattern.ClusterSet;
import gr.pattdetection.java.pattern.MatrixContainer;
import gr.pattdetection.java.pattern.PatternDescriptor;
import gr.pattdetection.java.pattern.PatternEnum;
import gr.pattdetection.java.pattern.PatternGenerator;
import gr.pattdetection.java.pattern.TsantalisPatternInstance;
import gr.pattdetection.java.pattern.SimilarityAlgorithm;
import gr.pattdetection.java.pattern.SystemGenerator;
import gr.bytecodereader.java.bytecode.BytecodeReader;
import gr.bytecodereader.java.bytecode.FieldObject;
import gr.bytecodereader.java.bytecode.MethodObject;
import gr.bytecodereader.java.bytecode.SystemObject;
import gr.pattdetection.java.pattern.inheritance.Enumeratable;

import java.io.File;
import java.util.*;

public class Console {
    public Console(File inputDir, File outputXML) {
        BytecodeReader br = new BytecodeReader(inputDir);
        SystemObject so = br.getSystemObject();
        SystemGenerator sg = new SystemGenerator(so);
        SortedSet<ClusterSet.Entry> clusterSet = sg.getClusterSet().getInvokingClusterSet();
        List<Enumeratable> hierarchyList = sg.getHierarchyList();
        LinkedHashMap<String, Vector<TsantalisPatternInstance>> map = new LinkedHashMap<String, Vector<TsantalisPatternInstance>>();

        PatternEnum[] patternEnum = PatternEnum.values();
        for(int i=0; i<patternEnum.length; i++) {
            String patternName = patternEnum[i].toString();
            PatternDescriptor patternDescriptor = PatternGenerator.getPattern(patternName);
            if(patternDescriptor.getNumberOfHierarchies() == 0) {
                MatrixContainer systemContainer = sg.getMatrixContainer();
                double[][] systemMatrix = null;
                BehavioralData behavioralData = null;
                if(patternName.equals(PatternEnum.SINGLETON.toString())) {
                    systemMatrix = systemContainer.getSingletonMatrix();
                    behavioralData = systemContainer.getSingletonBehavioralData();
                }
                else if(patternName.equals(PatternEnum.TEMPLATE_METHOD.toString())) {
                    systemMatrix = systemContainer.getTemplateMethodMatrix();
                    behavioralData = systemContainer.getTemplateMethodBehavioralData();
                }
                else if(patternName.equals(PatternEnum.FACTORY_METHOD.toString())) {
                    systemMatrix = systemContainer.getFactoryMethodMatrix();
                    behavioralData = systemContainer.getFactoryMethodBehavioralData();
                }

                Vector<TsantalisPatternInstance> patternInstanceVector = new Vector<TsantalisPatternInstance>();
                for(int j=0; j<systemMatrix.length; j++) {
                    if(systemMatrix[j][j] == 1.0) {
                        TsantalisPatternInstance patternInstance = new TsantalisPatternInstance();
                        patternInstance.addEntry(patternInstance.new Entry(patternDescriptor.getClassNameList().get(0),systemContainer.getClassNameList().get(j),j));
                        if(behavioralData != null) {
                        	if(patternDescriptor.getFieldRoleName() != null) {
                        		Set<FieldObject> fields = behavioralData.getFields(j, j);
                        		if(fields != null) {
                        			for(FieldObject field : fields) {
                        				patternInstance.addEntry(patternInstance.new Entry(patternDescriptor.getFieldRoleName(), field.toString(), -1));
                        			}
                        		}
                        	}
                        	if(patternDescriptor.getMethodRoleName() != null) {
                        		Set<MethodObject> methods = behavioralData.getMethods(j, j);
                        		if(methods != null) {
                        			for(MethodObject method : methods) {
                        				patternInstance.addEntry(patternInstance.new Entry(patternDescriptor.getMethodRoleName(), method.getSignature().toString(), -1));
                        			}
                        		}
                        	}
                        }
                        patternInstanceVector.add(patternInstance);
                    }
                }
                map.put(patternName,patternInstanceVector);
            }
            else if(patternDescriptor.getNumberOfHierarchies() == 1) {
                Vector<TsantalisPatternInstance> patternInstanceVector = new Vector<TsantalisPatternInstance>();
                for(Enumeratable ih : hierarchyList) {
                    List<Enumeratable> tempList = new ArrayList<Enumeratable>();
                    tempList.add(ih);
                    MatrixContainer hierarchyMatrixContainer = sg.getHierarchiesMatrixContainer(tempList);
                    generateResults(hierarchyMatrixContainer, patternDescriptor, patternInstanceVector);
                }
                map.put(patternName,patternInstanceVector);
            }
            else if(patternDescriptor.getNumberOfHierarchies() == 2) {
                Iterator<ClusterSet.Entry> it = clusterSet.iterator();
                Vector<TsantalisPatternInstance> patternInstanceVector = new Vector<TsantalisPatternInstance>();
                while(it.hasNext()) {
                    ClusterSet.Entry entry = it.next();
                    MatrixContainer hierarchiesMatrixContainer = sg.getHierarchiesMatrixContainer(entry.getHierarchyList());
                    generateResults(hierarchiesMatrixContainer, patternDescriptor, patternInstanceVector);
                }
                map.put(patternName,patternInstanceVector);
            }
        }

        new XMLExporter(map,outputXML);
    }

    private void generateResults(MatrixContainer systemContainer ,PatternDescriptor patternDescriptor, Vector<TsantalisPatternInstance> patternInstanceVector) {
        double[][] results = SimilarityAlgorithm.getTotalScore(systemContainer,patternDescriptor);
        if(results != null) {
            ClusterResult clusterResult = new ClusterResult(results, patternDescriptor, systemContainer);
            List<TsantalisPatternInstance> list = clusterResult.getPatternInstanceList();
            for (TsantalisPatternInstance pi : list) {
                if (!patternInstanceVector.contains(pi))
                    patternInstanceVector.add(pi);
            }
        }
    }
}
