/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentvalidator.coverage.cfg;

import java.util.ArrayList;

/**
 *
 * @author thanasis
 */
public class CfgNode implements Comparable {
    private int beginLine;
    private int endLine;
    private int beginColumn;
    private int endColumn;
    private int cfgHashCode;
    private String source;
    private String type;
    private boolean covered;
    private ArrayList<CfgNode> connectedNodes;
    
    public CfgNode(int beginLine, int endLine, int beginColumn, int endColumn, String source, String type){
        this.beginLine = beginLine;
        this.endLine = endLine;
        this.beginColumn = beginColumn;
        this.endColumn = endColumn;
        this.source = source;
        this.type = type;
        connectedNodes = new ArrayList<CfgNode>();
    }
    
    public CfgNode(int beginLine, int endLine, int beginColumn, int endColumn, String source, String type, CfgNode node){
        this.beginLine = beginLine;
        this.endLine = endLine;
        this.beginColumn = beginColumn;
        this.endColumn = endColumn;
        this.source = source;
        this.type = type;
        connectedNodes = new ArrayList<CfgNode>();
        connectedNodes.add(node);
    }
    
    public int getBeginLine() {
        return beginLine;
    }

    public int getEndLine() {
        return endLine;
    }
    
    public void setCovered(boolean covered){
        this.covered = covered;
    }

    public int getBeginColumn() {
        return beginColumn;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public String getSource() {
        return this.source;
    }

    public String getType() {
        return type;
    }

    public int getCfgHashCode() {
        return cfgHashCode;
    }

    public ArrayList<CfgNode> getConnectedNodes() {
        return connectedNodes;
    }

    public void setBeginLine(int beginLine) {
        this.beginLine = beginLine;
    }

    public void setBeginColumn(int beginColumn) {
        this.beginColumn = beginColumn;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public void setEndColumn(int endColumn) {
        this.endColumn = endColumn;
    }

    public void setCfgHashCode(int cfgHashCode) {
        this.cfgHashCode = cfgHashCode;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setConnectedNodes(ArrayList<CfgNode> connectedNodes) {
        this.connectedNodes = connectedNodes;
    }

    public void addSequenceNode(CfgNode node){
        if(!connectedNodes.contains(node)){
            connectedNodes.add(0,node);
        }
    }
    
    public void addJumpNode(CfgNode node){
        if(!connectedNodes.contains(node)){
            connectedNodes.add(node);
        }
    }
    
    @Override
    public int compareTo(Object object) {
        CfgNode secNode = (CfgNode) object;
        if (beginLine < secNode.getBeginLine()) {
            return -1;
        } else if (beginLine > secNode.getBeginLine()) {
            return 1;
        } else {
            if (beginColumn < secNode.getBeginColumn()) {
                return -1;
            } else if (beginColumn > secNode.getBeginColumn()) {
                return 1;
            } else {
                return 0;
            }
        }
    }
    
    @Override
    public boolean equals(Object n) {
        CfgNode node = (CfgNode) n;
        return (beginLine == node.getBeginLine() && beginColumn == node.getBeginColumn() && endLine == node.getEndLine() && endColumn == node.endColumn && type.equals(node.getType()));
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + this.beginLine;
        hash = 29 * hash + this.beginColumn;
        hash = 29 * hash + this.endLine;
        hash = 29 * hash + this.endColumn;
        hash = 29 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        String node = "CodeLine: " + beginLine + " --> source: " + source;
        if (connectedNodes.size() > 0) {
            node = node.concat(" connects to: ");
            for (CfgNode curNode : connectedNodes) {
                node = node.concat(curNode.getBeginLine() + "  ");
            }
        }
        node = node.concat("\n");
        return node;
    }
    
    
    public boolean isBreakStmt() {
        return type.equals("BreakStmt");
    }

    public boolean isBreakContinueThrowReturn() {
        return type.equals("BreakStmt") || type.equals("ContinueStmt") || type.equals("ThrowStmt") || type.equals("ReturnStmt");
    }

    public boolean isCaseStmt() {
        return type.equals("SwitchEntryStmt");
    }

    public boolean isCaseEndStmt() {
        return type.equals("CaseEndStmt");
    }

    public boolean isCatchClause() {
        return type.equals("CatchClause");
    }
    
    public boolean isCatchClauseEnd() {
        return type.equals("CatchEndStmt");
    }

    public boolean isContinueStmt() {
        return type.equals("ContinueStmt");
    }

    public boolean isDoStmt() {
        return type.equals("DoStmt");
    }
    
    public boolean isDoEndStmt() {
        return type.equals("DOEND");
    }
    
    public boolean isCovered(){
        return covered;
    }

    public boolean isElseIfStmt() {
        return type.equals("ElseIfStmt");
    }

    public boolean isElseIfOrElseStmt() {
        return type.equals("ElseIfStmt") || type.equals("ElseStmt");
    }
    
    public boolean isEndOfMethod() {
        return type.equals("EndOfMethod");
    }

    public boolean isElseStmt() {
        return type.equals("ElseStmt");
    }

    public boolean isExpressionStmt() {
        return type.equals("ExpressionStmt") || type.equals("MethodExpressionStmt");
    }
    
    boolean isMethodCallExpr() {
        return type.equals("MethodExpressionStmt");
    }
    
    public boolean isFinallyClause() {
        return type.equals("FinallyStmt");
    }
    
    public boolean isFinallyEndStmt() {
        return type.equals("FinallyEndStmt");
    }
    
    public boolean isForEndStmt() {
        return type.equals("FOREND");
    }
    
    public boolean isForEachEndStmt() {
        return type.equals("FOREACHEND");
    }
    
    public boolean isForEachStmt() {
        return type.equals("ForeachStmt");
    }
    
    public boolean isForStmt() {
        return type.equals("ForStmt");
    }

    boolean isIfEndStmt() {
        return type.equals("IfEndStmt");
    }

    public boolean isIfOrThenOrCaseEnd() {
        return type.equals("ThenEndStmt") || type.equals("CaseEndStmt") || type.equals("IfEndStmt");
    }

    public boolean isIfStmt() {
        return type.equals("IfStmt");
    }

    public boolean isLoopNode() {
        return (type.equals("ForStmt") || type.equals("ForeachStmt") || type.equals("WhileStmt") || type.equals("DoStmt"));
    }

    public boolean isLoopNodeOrSwitch() {
        return (type.equals("ForStmt") || type.equals("ForeachStmt") || type.equals("WhileStmt") || type.equals("DoStmt") || type.equals("SwitchStmt"));
    }

    public boolean isLoopNodeNotDo() {
        return (type.equals("ForStmt") || type.equals("ForeachStmt") || type.equals("WhileStmt"));
    }
    
    boolean isLoopNodeNotDoEnd() {
        return (type.equals("FOREND") || type.equals("FOREACHEND") || type.equals("WHILEEND"));
    }
    
    public boolean isLoopOrSwitchEndStmt() {
        return (type.equals("FOREND") || type.equals("FOREACHEND") || type.equals("WHILEEND") || type.equals("DOEND") || type.equals("SwitchEndStmt"));
    }
    
    public boolean isReturnStmt() {
        return type.equals("ReturnStmt");
    }
    
    public boolean isStatement(){
        return !(type.contains("END") || type.contains("End"));
    }
    
    public boolean isSwitchStmt() {
        return type.equals("SwitchStmt");
    }
    
    public boolean isSwitchEndStmt() {
        return type.equals("SwitchEndStmt");
    }
    
    public boolean isThenEndStmt() {
        return type.equals("ThenEndStmt");
    }

    public boolean isThrowStmt() {
        return type.equals("ThrowStmt");
    }

    public boolean isTryStmt() {
        return type.equals("TryStmt");
    }
    
    public boolean isWhileEndStmt() {
        return type.equals("WHILEEND");
    }
    
    public boolean isTryEndStmt() {
        return type.equals("TryEndStmt");
    }
    
    public boolean isWhileStmt() {
        return type.equals("WhileStmt");
    }
    
    public boolean isElseIf(CfgNode node) {
        return (beginLine == node.getBeginLine() && beginColumn == node.getBeginColumn() && endLine == node.getEndLine() && endColumn == node.endColumn && node.type.equals("ElseStmt"));
    }

    public boolean isThenIfEnd(CfgNode node) {
        return (beginLine == node.getBeginLine() && beginColumn == node.getBeginColumn() && endLine == node.getEndLine() && endColumn == node.endColumn && type.equals("ThenEndStmt"));
    }

    public void removeConnetions() {
        connectedNodes = new ArrayList<CfgNode>();
    }
    
    public String[] getMethods(){
        if(isMethodCallExpr()){
            return source.split("_");
        } else {
            return new String[0];
        }
    }
}
