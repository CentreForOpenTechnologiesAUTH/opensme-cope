/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentvalidator.coverage.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;
import java.util.TreeSet;

/**
 *
 * @author thanasis
 */
public class CfgBuilder {
    private ArrayList<CfgMethod> cfgList;
    private CfgMethod cfgMethod;
    private ArrayList<String> methodsThrowingExceptionsList;
    private ArrayList<String> methodsNotThrowingExceptionsList;
    private ArrayList<String> frequentMethodsNotThrowingExceptions;
    
    public CfgBuilder(){
        cfgList = new ArrayList<CfgMethod>();
        frequentMethodsNotThrowingExceptions = new ArrayList<String>();
        frequentMethodsNotThrowingExceptions.add("print");
        frequentMethodsNotThrowingExceptions.add("add");
        frequentMethodsNotThrowingExceptions.add("put");
        frequentMethodsNotThrowingExceptions.add("System.currentTimeMillis();"); 
    }
    
    public void setMethodsThrowingExceptionsList(ArrayList<String> methodsThrowingExceptionsList, ArrayList<String> methodsNotThrowingExceptionsList){
        this.methodsThrowingExceptionsList = methodsThrowingExceptionsList;
        this.methodsNotThrowingExceptionsList = methodsNotThrowingExceptionsList;
    }
    
    public void createNewCfg(String className, String packageName, String returnType, String methodSignature, String sourceCode, int beginLine, int endLine){
        cfgMethod = new CfgMethod(className, packageName, returnType, methodSignature, sourceCode, beginLine, endLine);
    }
    
    public void connectNodes(){
        if(cfgMethod != null){
            cfgMethod.combineLists();
            for(CfgNode node : cfgMethod.getCfg()){
                if(!node.isBreakStmt() && !node.isContinueStmt() && !node.isThrowStmt()){
                    connectThisNode(node);
                }
            }
            for(CfgNode node : cfgMethod.getCfg()){
                if(node.isBreakStmt() || node.isContinueStmt() || node.isThrowStmt()){
                    connectThisNode(node);
                }
            }            
            cfgList.add(cfgMethod);
        }
    }
    
    private void connectThisNode(CfgNode curNode){
        ArrayList<CfgNode> nodesList = new ArrayList<CfgNode>(cfgMethod.getCfg());
        Collections.sort(nodesList);
        if(curNode.isIfEndStmt()){
            connectToNextNode(curNode, nodesList);
            
            TreeSet<CfgNode> ifElseIfElseEndList = new TreeSet<CfgNode>();
            for(int i = 0; i < nodesList.indexOf(curNode); i++){
                CfgNode tmpNode = nodesList.get(i);
                if(tmpNode.getEndLine() == curNode.getBeginLine() && tmpNode.getEndColumn() == curNode.getEndColumn() && tmpNode != curNode && !tmpNode.isThenEndStmt() ){
                    tmpNode.addSequenceNode(nodesList.get(nodesList.indexOf(tmpNode)+1));
                    ifElseIfElseEndList.add(tmpNode);
                }
            }
            ifElseIfElseEndList.add(curNode);
            while(ifElseIfElseEndList.size() > 2){
                ifElseIfElseEndList.pollFirst().addJumpNode(ifElseIfElseEndList.first());
            }
            ifElseIfElseEndList.pollFirst().addJumpNode(nodesList.get(nodesList.indexOf(ifElseIfElseEndList.first())+1));
        } else if(curNode.isThenEndStmt()){
            for(int i = nodesList.indexOf(curNode); i < nodesList.size(); i++){ //connect if to thenEnd
                CfgNode tmpNode = nodesList.get(i);
                if(tmpNode.getBeginLine() == curNode.getEndLine() && tmpNode.getBeginColumn() == curNode.getEndColumn() && tmpNode.isIfEndStmt()){
                    curNode.addJumpNode(tmpNode);
                    break;
                }
            }
        } else if(curNode.isSwitchEndStmt()){
            connectToNextNode(curNode, nodesList);
            
            nodesList.get(nodesList.indexOf(curNode)-1).addJumpNode(curNode);
            
            TreeSet<CfgNode> switchCaseEndList = new TreeSet<CfgNode>();
            for(int i = 0; i < nodesList.indexOf(curNode); i++){
                CfgNode tmpNode = nodesList.get(i);
                if((tmpNode.getBeginColumn() == curNode.getEndLine() || tmpNode.getEndLine() == curNode.getBeginLine()) && tmpNode != curNode){
                    connectToNextNode(tmpNode, nodesList);
                    switchCaseEndList.add(tmpNode);
                }
            }
            switchCaseEndList.add(curNode);
            while(switchCaseEndList.size() > 2){
                switchCaseEndList.pollFirst().addJumpNode(switchCaseEndList.first());
            }
            switchCaseEndList.pollFirst().addJumpNode(nodesList.get(nodesList.indexOf(switchCaseEndList.first())+1));
        } else if(curNode.isCaseStmt()){
            CfgNode previousNode = null;
            if(nodesList.indexOf(curNode)-1 >= 0){
                previousNode = nodesList.get(nodesList.indexOf(curNode)-1);
            }
            if(!previousNode.isSwitchStmt()){
                for(CfgNode tmpNode : nodesList){
                    if(tmpNode.getBeginLine() == curNode.getBeginColumn() && tmpNode != curNode){
                        previousNode.addJumpNode(tmpNode);
                    }
                }
            }
        } else if(curNode.isForEndStmt() || curNode.isForEachEndStmt() || curNode.isWhileEndStmt() || curNode.isDoEndStmt()){
            if(curNode.isDoEndStmt()){
                connectToNextNode(curNode, nodesList);
            }
            for(CfgNode tmpNode : nodesList){
                if(tmpNode.getEndLine() == curNode.getBeginLine() && tmpNode.getEndColumn() == curNode.getBeginColumn() && ((tmpNode.isForStmt() && curNode.isForEndStmt()) || (tmpNode.isForEachStmt() && curNode.isForEachEndStmt()) || (tmpNode.isWhileStmt() && curNode.isWhileEndStmt()) || (tmpNode.isDoStmt() && curNode.isDoEndStmt()) )){
                    curNode.addJumpNode(tmpNode);
                    connectToNextNode(tmpNode, nodesList);
                    if(!curNode.isDoEndStmt()){
                        if(nodesList.indexOf(curNode)+1 < nodesList.size()){
                            tmpNode.addJumpNode(nodesList.get(nodesList.indexOf(curNode)+1));
                        }
                    }
                    break;
                }
            }
        } else if(curNode.isContinueStmt() || curNode.isBreakStmt()){
            ListIterator<CfgNode> iterator = nodesList.listIterator();
            while(iterator.hasNext()){
                CfgNode next = iterator.next();
                if(next != curNode){
                    continue;
                }
                while(iterator.hasPrevious()){
                    CfgNode previous = iterator.previous();
                    if((previous.isLoopNode() && curNode.isContinueStmt()) || (previous.isLoopNodeOrSwitch() && curNode.isBreakStmt())){
                        if((previous.isDoStmt() && curNode.isContinueStmt()) || curNode.isBreakStmt()){
                            for(CfgNode tmpNode : nodesList){
                                if(tmpNode.getBeginLine() == previous.getEndLine() && tmpNode.getBeginColumn() == previous.getEndColumn() && tmpNode.isLoopOrSwitchEndStmt()){
                                    curNode.addJumpNode(tmpNode);
                                    break;
                                }
                            }
                        } else if(curNode.isContinueStmt()){
                            curNode.addJumpNode(previous);
                        }
                       break;
                    }
                }
                break;
            }
            //nodesList.get(nodesList.indexOf(curNode)+1).removeConnetions();
        } else if(curNode.isEndOfMethod()){
            if(nodesList.indexOf(curNode)-1 >= 0){
                if(!nodesList.get(nodesList.indexOf(curNode)-1).isLoopNodeNotDoEnd()){
                    nodesList.get(nodesList.indexOf(curNode)-1).addSequenceNode(curNode);
                }
            }
            for(CfgNode tmpNode : nodesList){
                if(tmpNode.isReturnStmt() || tmpNode.isThrowStmt() || tmpNode.isFinallyEndStmt() || tmpNode.isCatchClauseEnd()){
                    tmpNode.addJumpNode(curNode);
//                    if(!tmpNode.isCatchClauseEnd()){
//                        nodesList.get(nodesList.indexOf(tmpNode)+1).removeConnetions();
//                    }
                }
            }
        } else if(curNode.isCatchClause() && curNode.getConnectedNodes().isEmpty()){
            connectToNextNode(curNode, nodesList);
            
            CfgNode catchEndNode = null;
            for(CfgNode tmpCatchEndNode : nodesList){
                if(tmpCatchEndNode.getBeginLine() == curNode.getEndLine()){
                    catchEndNode = tmpCatchEndNode;
                }
            }

            CfgNode tryEndNode = null;
            for(CfgNode tmpTryEndNode : nodesList){
                if(tmpTryEndNode.getBeginLine() == catchEndNode.getEndColumn()){
                    tryEndNode = tmpTryEndNode;
                }
            }
            catchEndNode.addJumpNode(nodesList.get(nodesList.indexOf(tryEndNode)+1));
            
            
            TreeSet<CfgNode> catchFinallyList = new TreeSet<CfgNode>();
            for(CfgNode tmpNode : nodesList){
                if((tmpNode.getBeginLine() == curNode.getEndLine() && tmpNode != curNode && (tmpNode.isCatchClause() || tmpNode.isFinallyClause())) ){
                    connectToNextNode(tmpNode, nodesList);
                    
                    
                    catchEndNode = null;
                    for(CfgNode tmpCatchEndNode : nodesList){
                        if(tmpCatchEndNode.getBeginLine() == tmpNode.getEndLine()){
                            catchEndNode = tmpCatchEndNode;
                        }
                    }
                    
                    tryEndNode = null;
                    for(CfgNode tmpTryEndNode : nodesList){
                        if(tmpTryEndNode.getBeginLine() == catchEndNode.getEndColumn()){
                            tryEndNode = tmpTryEndNode;
                        }
                    }
                    catchEndNode.addJumpNode(nodesList.get(nodesList.indexOf(tryEndNode)+1));
                    
                    catchFinallyList.add(tmpNode);
                }
            }
            catchFinallyList.add(curNode);
            while(catchFinallyList.size() > 1){
                catchFinallyList.pollFirst().addJumpNode(catchFinallyList.first());
            } 
        } else if(curNode.isTryStmt()){
            connectToNextNode(curNode, nodesList);
            CfgNode tryBlockEndNode = null;
            for(CfgNode tmpNode : nodesList){
                if((tmpNode.getBeginLine() == curNode.getEndLine() && tmpNode.isTryEndStmt())){
                    tryBlockEndNode = tmpNode;
                    break;
                }
            }
            for(int i = nodesList.indexOf(curNode)+1; i<nodesList.indexOf(tryBlockEndNode); i++){
                if(nodesList.get(i).isTryStmt()){
                    CfgNode innerTryNode = nodesList.get(i);
                    for(CfgNode tmpNode : nodesList){
                        if((tmpNode.getBeginLine() == innerTryNode.getEndLine() && tmpNode.isTryEndStmt())){
                            break;
                        }
                        if(tmpNode.getBeginLine() > innerTryNode.getBeginLine()){
                           i++; 
                        }
                    }
                }
                
                CfgNode tryEndNode = null;
                for(CfgNode tmpNode : nodesList){
                    if(tmpNode.getBeginLine() == curNode.getEndColumn()){
                        tryEndNode = tmpNode;
                    }
                }
                tryBlockEndNode.addJumpNode(nodesList.get(nodesList.indexOf(tryEndNode)+1));
                
                if(nodesList.get(i).isMethodCallExpr()){
                    boolean throwsException = false;
                    for(String methodCall : nodesList.get(i).getMethods()){
                        if(methodsThrowingExceptionsList.contains(methodCall)){
                            throwsException = true;
                            break;
                        } else if(!frequentMethodsNotThrowingExceptions.contains(methodCall) && !methodsNotThrowingExceptionsList.contains(methodCall)){
                            throwsException = true;
                            break;
                        }
                    }
                    if(throwsException){
                        nodesList.get(i).addJumpNode(nodesList.get(nodesList.indexOf(tryBlockEndNode)+1));
                    }
                }
            }
        } else if(curNode.isExpressionStmt()){
            curNode.addSequenceNode(nodesList.get(nodesList.indexOf(curNode)+1));
        }     
    }
    
    private void connectToNextNode(CfgNode curNode, ArrayList<CfgNode> nodesList){
        if(nodesList.indexOf(curNode)+1 < nodesList.size()){
                curNode.addSequenceNode(nodesList.get(nodesList.indexOf(curNode)+1));
        }
    }
    
    public void setCfgHashCodes(){
        for(CfgMethod method : cfgList){
            int i = 1;
            for(CfgNode node : method.getCfg()){
                node.setCfgHashCode(i);
                if(i == method.getCfg().size()-1){
                    i++;
                }
                i++;
            }
        }
    }
    
    public ArrayList<CfgMethod> getCfgList(){
        return cfgList;
    }
    
    public void addToGraph(int beginLine, int endLine, int beginColumn, int endColumn, String source, String type){
        CfgNode node = new CfgNode(beginLine, endLine, beginColumn, endColumn, source, type);  
        if(cfgMethod.getWaitingListSize() > 1){
            if(beginLine < cfgMethod.readFirstFromWaitingList().getBeginLine()){
                cfgMethod.addNodeToCfg(node);
            } else if(beginLine > cfgMethod.readFirstFromWaitingList().getBeginLine()){
                while(beginLine > cfgMethod.readFirstFromWaitingList().getBeginLine()) {
                    cfgMethod.addNodeToCfg(cfgMethod.getFirstFromWaitingList());
                }
                addToGraph(beginLine, endLine, beginColumn, endColumn, source, type);
            } else {
                if(type.equals("IfStmt") && cfgMethod.readFirstFromWaitingList().getType().equals("ElseStmt")){
                    cfgMethod.getFirstFromWaitingList();
                    addToGraph(beginLine, endLine, beginColumn, endColumn, "else "+source,"ElseIfStmt");
                } else if(type.equals("IfStmt") && cfgMethod.readFirstFromWaitingList().getType().equals("ThenEndStmt")){
                    cfgMethod.addNodeToCfg(cfgMethod.getFirstFromWaitingList());
                    addToGraph(beginLine, endLine, beginColumn, endColumn, source,type);
                } else if(cfgMethod.readFirstFromWaitingList().isCaseEndStmt()){
                    cfgMethod.getFirstFromWaitingList(); 
                    cfgMethod.addNodeToCfg(node);
                }
            }
        } else {
            cfgMethod.addNodeToCfg(node);
        }
    }
    
    public void addToGraphWaitingList(int beginLine, int endLine, int beginColumn, int endColumn, String source, String type){
        CfgNode node = new CfgNode(beginLine, endLine, beginColumn, endColumn, source, type);
        cfgMethod.addNodeToWaitingList(node);
    }
    
    public void connectNodesForTraceAdding(){
        if(cfgMethod != null){
            cfgMethod.combineLists();
            for(CfgNode node : cfgMethod.getCfg()){
                if(node.isExpressionStmt()){
                    connectThisNode(node);
                }
            }
            for(CfgNode node : cfgMethod.getCfg()){
                if(node.isBreakStmt() || node.isContinueStmt() || node.isThrowStmt()){
                    connectThisNode(node);
                }
            }            
            cfgList.add(cfgMethod);
        }
    }

}
