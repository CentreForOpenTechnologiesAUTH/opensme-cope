/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentvalidator.coverage.cfg;

import eu.opensme.cope.componentvalidator.util.Utils;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.stmt.*;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thanasis
 */
public class CfgJavaParser {
    private ArrayList<String> methodsThrowingExceptionsList;
    private ArrayList<String> methodsNotThrowingExceptionsList;
    private String className;
    private String packageName;
    private CfgBuilder cfgBuilder;
    
    public CfgJavaParser(){
        methodsThrowingExceptionsList = new ArrayList<String>();
        methodsNotThrowingExceptionsList = new ArrayList<String>();
    }
    
    public ArrayList<CfgMethod> parseToFindTraceCodePositions(File classFile){
        FileInputStream in = null;
        cfgBuilder = new CfgBuilder();
        this.className = classFile.getAbsolutePath();
        this.packageName = classFile.getParent();
        try {
            in = new FileInputStream(classFile);
            CompilationUnit cu = JavaParser.parse(in);
            String compilationUnitName = classFile.getName().substring(0,classFile.getName().lastIndexOf("."));
            new ClassDeclarationVisitor("trace", compilationUnitName).visit(cu, null);
            in.close();
        } catch (ParseException ex) {
            Logger.getLogger(CfgJavaParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CfgJavaParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
                Logger.getLogger(CfgJavaParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cfgBuilder.getCfgList();
    }
    
    
    public ArrayList<CfgMethod> parse(File classFile) {
        FileInputStream in = null;
        cfgBuilder = new CfgBuilder();
        cfgBuilder.setMethodsThrowingExceptionsList(methodsThrowingExceptionsList, methodsNotThrowingExceptionsList);
        this.packageName = Utils.getThePackage(classFile.toString(), false); //classFile.getParrent();
        try {            
            in = new FileInputStream(classFile);
            
            CompilationUnit cu = JavaParser.parse(in);
            String compilationUnitName = classFile.getName().substring(0,classFile.getName().lastIndexOf("."));
            new ClassDeclarationVisitor("normal", compilationUnitName).visit(cu, null);
            in.close();
        } catch (ParseException ex) {
            System.err.println(classFile.getParent()+" "+classFile.getName());
            Logger.getLogger(CfgJavaParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CfgJavaParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
                Logger.getLogger(CfgJavaParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cfgBuilder.getCfgList();
    }
    
    public void findMethodsThrowingExceptions(ArrayList<File> javaFilesList){
        FileInputStream in = null;
        try {
            for(File javaFile : javaFilesList){
                if(javaFile.exists()){
                    in = new FileInputStream(javaFile);
                    CompilationUnit cu = JavaParser.parse(in);
                    String compilationUnitName = javaFile.getName().substring(0,javaFile.getName().lastIndexOf("."));
                    new ClassDeclarationVisitor("exception", compilationUnitName).visit(cu, null);
                    in.close();
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CfgJavaParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
                Logger.getLogger(CfgJavaParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
                Logger.getLogger(CfgJavaParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private class ClassDeclarationVisitor extends VoidVisitorAdapter {
        
        private String operation;
        private String compilationUnitName;
        private int anonymousInnerClassCnt;
        
        public ClassDeclarationVisitor(String operation, String compilationUnitName){
            super();
            this.anonymousInnerClassCnt = 0;
            this.operation = operation;
            this.compilationUnitName = compilationUnitName;
        }

        @Override
        public void visit(ObjectCreationExpr n, Object arg) {
            super.visit(n, arg);
            List<BodyDeclaration> anonymousClassBody = n.getAnonymousClassBody();
            if (anonymousClassBody != null) {
                for (BodyDeclaration body : anonymousClassBody) {
                    if (body instanceof MethodDeclaration) {
                        className = compilationUnitName + "$" + ++anonymousInnerClassCnt;
                        if(operation.endsWith("normal")){
                            new MethodDeclarationVisitor().visit((MethodDeclaration) body, null);
                        } else if(operation.endsWith("trace")){
                            new MethodDeclarationTraceVisitor().visit((MethodDeclaration) body, null);
                        } else if(operation.endsWith("exception")){
                            new MethodThrowingExceptionVisitor().visit((MethodDeclaration) body, null);
                        }
                    }
                }
            }
        }
        @Override
        public void visit(ClassOrInterfaceDeclaration n, Object arg) {
            super.visit(n, null);
            if(!n.isInterface()){
                className = n.getName();
                if(!className.equals(compilationUnitName)){
                    className = compilationUnitName + "$" + className;
                }
                if(operation.endsWith("normal")){
                    new MethodDeclarationVisitor().visit(n, null);
                } else if(operation.endsWith("trace")){
                    new MethodDeclarationTraceVisitor().visit(n, null);
                } else if(operation.endsWith("exception")){
                    new MethodThrowingExceptionVisitor().visit(n, null);
                }
            }
        }
    }
    
    private class MethodDeclarationVisitor extends VoidVisitorAdapter {

        @Override
        public void visit(MethodDeclaration n, Object arg) {
            if(n.getBody() != null){
                String methodSignature = n.getName() + "(";
                if(n.getParameters() != null){
                    int i = 1;
                    for(Parameter par : n.getParameters()){
                        String parameterType = par.getType().toString();
                        if(parameterType.contains(".")){
                            String[] parameterArray = parameterType.split("\\.");
                            parameterType = parameterArray[parameterArray.length-1];
                        }
                        if(parameterType.contains("<")){
                            parameterType = parameterType.substring(0,parameterType.indexOf("<"));
                        }
                        if(i == n.getParameters().size()){
                            methodSignature += parameterType+")";
                        } else {
                            methodSignature += parameterType+", ";
                        }
                        i++;
                    }
                } else {
                    methodSignature += ")";
                }
                String methodCode = n.toString();
                if(n.getJavaDoc() != null){
                    methodCode = methodCode.substring(n.getJavaDoc().toString().length());
                }
                String returnType = n.getType().toString();
                if(returnType.contains("<")){
                    returnType = returnType.substring(0,returnType.indexOf("<"));
                }
                if(returnType.contains(".")){
                            String[] returnArray = returnType.split("\\.");
                            returnType = returnArray[returnArray.length-1];
                }
                cfgBuilder.createNewCfg(className, packageName, returnType, methodSignature, methodCode, n.getBeginLine(), n.getEndLine());
                cfgBuilder.addToGraphWaitingList(n.getEndLine(), n.getEndLine(), 1, 1, "EndOfMethod", "EndOfMethod");
                ExpressionStatementVisitor exprStVisit = new ExpressionStatementVisitor();
                n.accept(exprStVisit, null);
                cfgBuilder.connectNodes();
                cfgBuilder.setCfgHashCodes();
            }
        }
        
        @Override
        public void visit(ConstructorDeclaration n, Object arg) {
            String constructorSignature = n.getName() + "(";
            if(n.getParameters() != null){
                int i = 1;
                for(Parameter par : n.getParameters()){
                    String parameterType = par.getType().toString();
                    if(par.getType().toString().contains(".")){
                        String[] parameterArray = parameterType.split("\\.");
                        parameterType = parameterArray[parameterArray.length-1];
                    }
                    if(parameterType.contains("<")){
                            parameterType = parameterType.substring(0,parameterType.indexOf("<"));
                    }
                    if(i == n.getParameters().size()){
                        constructorSignature += parameterType+")";
                    } else {
                        constructorSignature += parameterType+", ";
                    }
                    i++;
                }
            } else {
                constructorSignature += ")";
            }
            String constructorCode = n.toString();
            if(n.getJavaDoc() != null){
                constructorCode = constructorCode.substring(n.getJavaDoc().toString().length());
            }
            cfgBuilder.createNewCfg(className, packageName, "", constructorSignature, constructorCode, n.getBeginLine(), n.getEndLine());
            cfgBuilder.addToGraphWaitingList(n.getEndLine(), n.getEndLine(), 1, 1, "EndOfMethod", "EndOfMethod");
            ExpressionStatementVisitor exprStVisit = new ExpressionStatementVisitor();
            n.accept(exprStVisit, null);
            cfgBuilder.connectNodes();
            cfgBuilder.setCfgHashCodes();
        }
    }
    
     private class MethodThrowingExceptionVisitor extends VoidVisitorAdapter {

        @Override
        public void visit(MethodDeclaration n, Object arg) {
            if (n.getThrows() != null) {
                //TODO METHOD SIGNATURE NOT NAME
                methodsThrowingExceptionsList.add(n.getName());
            } else {
                methodsNotThrowingExceptionsList.add(n.getName());
            }
        }
    }
    
    private class MethodDeclarationTraceVisitor extends VoidVisitorAdapter {

        @Override
        public void visit(MethodDeclaration n, Object arg) {
            if(n.getBody() != null){
                String returnType = n.getType().toString();
                if(returnType.contains("<")){
                    returnType = returnType.substring(0,returnType.indexOf("<"));
                }
                cfgBuilder.createNewCfg(className, packageName, returnType, n.getName(), n.toString(), n.getBeginLine(), n.getEndLine());
                cfgBuilder.addToGraphWaitingList(n.getEndLine(), n.getEndLine(), 1, 1, "EndOfMethod", "EndOfMethod");
                ExpressionStatementVisitor exprStVisit = new ExpressionStatementVisitor();
                n.accept(exprStVisit, null);
                cfgBuilder.connectNodesForTraceAdding();
            }
        }
        
        @Override
        public void visit(ConstructorDeclaration n, Object arg) {
            cfgBuilder.createNewCfg(className, packageName, "", n.getName(), n.toString(), n.getBeginLine(), n.getEndLine());
            cfgBuilder.addToGraphWaitingList(n.getEndLine(), n.getEndLine(), 1, 1, "EndOfMethod", "EndOfMethod");
            ExpressionStatementVisitor exprStVisit = new ExpressionStatementVisitor();
            n.accept(exprStVisit, null);
            cfgBuilder.connectNodesForTraceAdding();
        }
    }
    
    private class MethodCallExpressionVisitor extends VoidVisitorAdapter {
        private boolean hasMethod;
        private String methods = "";
        
        @Override
        public void visit(MethodCallExpr n, Object arg) {
            super.visit(n, null);
            hasMethod = true;
            methods += n.getName() + "_";
        }
        
        public boolean hasMethodCall(){
            return this.hasMethod;
        }
        
        public String getMethods(){
            return this.methods;
        }
    }
    
    private class ExpressionStatementVisitor extends VoidVisitorAdapter {

        @Override
        public void visit(ExpressionStmt n, Object arg) {
            MethodCallExpressionVisitor methodCallVisistor = new MethodCallExpressionVisitor();
            methodCallVisistor.visit(n, null);
            if(methodCallVisistor.hasMethodCall()){
                if(!n.toString().equals("System.currentTimeMillis();")){ 
                    cfgBuilder.addToGraph(n.getBeginLine(), n.getEndLine(), n.getBeginColumn(), n.getEndColumn(), methodCallVisistor.getMethods(), "Method"+n.getClass().getSimpleName());
                } else {
                    cfgBuilder.addToGraph(n.getBeginLine(), n.getEndLine(), n.getBeginColumn(), n.getEndColumn(), n.toString()+"_", "Method"+n.getClass().getSimpleName());
                }
            } else {
                cfgBuilder.addToGraph(n.getBeginLine(), n.getEndLine(), n.getBeginColumn(), n.getEndColumn(), n.toString(), n.getClass().getSimpleName());
            }
        }

        @Override
        public void visit(ForStmt n, Object arg) {//TODO
            super.visit(n, null);
            String source = "for ()"; //+ n.getInit().toString() + "; " + n.getCompare().toString() + "; " + n.getUpdate().toString() + ")";
            cfgBuilder.addToGraph(n.getBeginLine(), n.getEndLine(), n.getBeginColumn(), n.getEndColumn(), source, n.getClass().getSimpleName());
            cfgBuilder.addToGraphWaitingList(n.getEndLine(), n.getEndLine(), n.getEndColumn(), n.getEndColumn(), "}", "FOREND");//Connects to forStmt
            
        }

        @Override
        public void visit(ForeachStmt n, Object arg) {
            super.visit(n, null);
            String source = "for (" + n.getVariable().toString() + " : " + n.getIterable().toString() + ")";
            cfgBuilder.addToGraph(n.getBeginLine(), n.getEndLine(), n.getBeginColumn(), n.getEndColumn(), source, n.getClass().getSimpleName());
            cfgBuilder.addToGraphWaitingList(n.getEndLine(), n.getEndLine(), n.getEndColumn(), n.getEndColumn(), "}", "FOREACHEND");//Connects to ForEach
            
        }

        @Override
        public void visit(WhileStmt n, Object arg) {
            super.visit(n, null);
            String source = "while (" + n.getCondition().toString() + ")";
            cfgBuilder.addToGraph(n.getBeginLine(), n.getEndLine(), n.getBeginColumn(), n.getEndColumn(), source, n.getClass().getSimpleName());
            cfgBuilder.addToGraphWaitingList(n.getEndLine(), n.getEndLine(), n.getEndColumn(), n.getEndColumn(), "}", "WHILEEND");//Connects to While
            
        }

        @Override
        public void visit(DoStmt n, Object arg) {
            super.visit(n, null);
            String source = "while (" + n.getCondition().toString() + ")";
            cfgBuilder.addToGraph(n.getBeginLine(), n.getEndLine(), n.getBeginColumn(), n.getEndColumn(), "do {", n.getClass().getSimpleName());
            cfgBuilder.addToGraphWaitingList(n.getEndLine(), n.getEndLine(), n.getEndColumn(), n.getEndColumn(), source, "DOEND");//Connects to Do
            
        }

        @Override
        public void visit(IfStmt n, Object arg) {
            super.visit(n, null);
            String source = "if (" + n.getCondition().toString() + ")";
            cfgBuilder.addToGraph(n.getBeginLine(), n.getEndLine(), n.getBeginColumn(), n.getEndColumn(), source, n.getClass().getSimpleName());
            if (n.getThenStmt().getEndLine() == n.getEndLine() && n.getThenStmt().getEndColumn() == n.getEndColumn()) {
                cfgBuilder.addToGraphWaitingList(n.getEndLine(), n.getEndLine(), n.getEndColumn(), n.getEndColumn(), "}", "IfEndStmt");
            } else {
                cfgBuilder.addToGraphWaitingList(n.getEndLine(), n.getEndLine(), n.getEndColumn(), n.getEndColumn(), "}", "IfEndStmt");
                cfgBuilder.addToGraphWaitingList(n.getThenStmt().getEndLine(), n.getEndLine(), n.getThenStmt().getEndColumn(), n.getEndColumn(), "}", "ThenEndStmt");
            }
            if (n.getElseStmt() != null) {
                cfgBuilder.addToGraphWaitingList(n.getElseStmt().getBeginLine(), n.getElseStmt().getEndLine(), n.getElseStmt().getBeginColumn(), n.getElseStmt().getEndColumn(), "else", "ElseStmt");
            }
            
        }

        @Override
        public void visit(SwitchStmt n, Object arg) {
            super.visit(n, null);
            String source = "Switch " + n.getSelector().toString();
            cfgBuilder.addToGraph(n.getBeginLine(), n.getEndLine(), n.getBeginColumn(), n.getEndColumn(), source, n.getClass().getSimpleName());
            if (n.getEntries() != null) {
                for (SwitchEntryStmt caseStmt : n.getEntries()) {
                    String caseSource = null;
                    if (caseStmt.getLabel() != null) {
                        caseSource = "case :" + caseStmt.getLabel().toString();
                    } else {
                        caseSource = "default";
                    }
                    cfgBuilder.addToGraphWaitingList(caseStmt.getBeginLine(), caseStmt.getEndLine(), n.getEndLine(), caseStmt.getEndColumn(), caseSource, caseStmt.getClass().getSimpleName());//trick
                    cfgBuilder.addToGraphWaitingList(caseStmt.getEndLine(), caseStmt.getEndLine(), caseStmt.getEndColumn(), caseStmt.getEndColumn(), "}", "CaseEndStmt");
                }
            }
            cfgBuilder.addToGraphWaitingList(n.getEndLine(), n.getEndLine(), n.getEndColumn(), n.getEndColumn(), "switchEnd", "SwitchEndStmt");
            
        }

        @Override
        public void visit(TryStmt n, Object arg) {
            super.visit(n, null);
            cfgBuilder.addToGraph(n.getBeginLine(), n.getTryBlock().getEndLine(), n.getBeginColumn(), n.getEndLine(), "try", n.getClass().getSimpleName()); //trick
            cfgBuilder.addToGraphWaitingList(n.getTryBlock().getEndLine(), n.getTryBlock().getEndLine(), n.getTryBlock().getEndColumn(), n.getTryBlock().getEndColumn(), "tryEnd", "TryEndStmt");
            if (n.getFinallyBlock() != null) {
                cfgBuilder.addToGraphWaitingList(n.getFinallyBlock().getBeginLine(), n.getFinallyBlock().getEndLine(), n.getFinallyBlock().getBeginColumn(), n.getFinallyBlock().getEndColumn(), "finally", "FinallyStmt");
                cfgBuilder.addToGraphWaitingList(n.getFinallyBlock().getEndLine(), n.getFinallyBlock().getEndLine(), n.getFinallyBlock().getEndColumn(), n.getFinallyBlock().getEndColumn(), "}", "FinallyEndStmt");
            }
            if (n.getCatchs() != null) {
                for (CatchClause catchStmt : n.getCatchs()) {
                    String catchSource = null;
                    if (catchStmt.getExcept() != null) {
                        catchSource = "catch (" + catchStmt.getExcept().toString() + ")";
                    }
                    cfgBuilder.addToGraphWaitingList(catchStmt.getBeginLine(), catchStmt.getEndLine(), catchStmt.getBeginColumn(), n.getEndLine(), catchSource, catchStmt.getClass().getSimpleName());
                    cfgBuilder.addToGraphWaitingList(catchStmt.getEndLine(), catchStmt.getEndLine(), catchStmt.getEndColumn(), n.getEndLine(), "}", "CatchEndStmt");
                }
            }
            
        }

        @Override
        public void visit(ThrowStmt n, Object arg) {
            super.visit(n, null);
            cfgBuilder.addToGraph(n.getBeginLine(), n.getEndLine(), n.getBeginColumn(), n.getEndColumn(), n.toString(), n.getClass().getSimpleName());
            
        }

        @Override
        public void visit(ContinueStmt n, Object arg) {
            super.visit(n, null);
            cfgBuilder.addToGraph(n.getBeginLine(), n.getEndLine(), n.getBeginColumn(), n.getEndColumn(), n.toString(), n.getClass().getSimpleName());
            
        }

        @Override
        public void visit(BreakStmt n, Object arg) {
            super.visit(n, null);
            cfgBuilder.addToGraph(n.getBeginLine(), n.getEndLine(), n.getBeginColumn(), n.getEndColumn(), n.toString(), n.getClass().getSimpleName());
            
        }

        @Override
        public void visit(ReturnStmt n, Object arg) {
            super.visit(n, null);
            cfgBuilder.addToGraph(n.getBeginLine(), n.getEndLine(), n.getBeginColumn(), n.getEndColumn(), n.toString(), n.getClass().getSimpleName());
            
        }
        
        @Override
        public void visit(ExplicitConstructorInvocationStmt n, Object arg) {
            super.visit(n, null);
            cfgBuilder.addToGraph(n.getBeginLine(), n.getEndLine(), n.getBeginColumn(), n.getEndColumn(), n.toString(), "ExpressionStmt");
        }              
    }
}
