package eu.opensme.cope.componentmakers.visitors.source;

import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import java.util.HashMap;

public class ReplaceTypeWithVisitor extends VoidVisitorAdapter<Object> {
    private final HashMap<String, String> fullyQualifiedTypes;
    private final HashMap<String, String> simpleNames;

    public ReplaceTypeWithVisitor(HashMap<String,String> fullyQualifiedTypes) {
        this.fullyQualifiedTypes = fullyQualifiedTypes;
        this.simpleNames = new HashMap<String, String>();
    }

    
//    @Override
//    public void visit(CompilationUnit n, String[] arg) {
//        super.visit(n, arg);
//        if (arg.length != 2) {
//            return;
//        }
//    }
//    
//    @Override
//    public void visit(FieldDeclaration n, String[] arg) {
//        //Object[] args = (Object[]) arg;
//        if (n.getType().toString().equals(arg[0])) {
//            ReferenceType ref = (ReferenceType) n.getType();
//            ClassOrInterfaceType clazz = (ClassOrInterfaceType) ref.getType();
//            ref.setType(new ClassOrInterfaceType(clazz.getScope(),
//                    arg[1].toString()));
//        }
//    }
    @Override
    public void visit(ClassOrInterfaceType n, Object arg) {
        super.visit(n, arg);
        if (this.simpleNames.isEmpty()) {
            return;
        }
        if (this.simpleNames.containsKey(n.getName())) {
            n.setName(this.simpleNames.get(n.getName()));
        }
    }

    @Override
    public void visit(ImportDeclaration n, Object arg) {
        super.visit(n, arg);
        
        NameExpr name = n.getName();
        if(this.fullyQualifiedTypes.containsKey(name.toString()))
        {
            this.simpleNames.put(name.getName(), qualifiedToSimple(this.fullyQualifiedTypes.get(name.toString())));
        }
        if (this.simpleNames.containsKey(name.getName())) {
            name.setName(this.simpleNames.get(name.getName()));
        }
    }
    
    
//
//    @Override
//    public void visit(ClassOrInterfaceDeclaration n, String[] arg) {
//        super.visit(n, arg);
//        if (arg.length != 2) {
//            return;
//        }
//        if (n.getName().equals(arg[0])) {
//            n.setName(arg[1]);
//        }
////        if(n.getMembers() != null)
////        {
////            for(BodyDeclaration body : n.getMembers())
////        }
//    }
//
//    
//    @Override
//    public void visit(ClassExpr n, String[] arg) {
//        super.visit(n, arg);
//        if (arg.length != 2) {
//            return;
//        }
//        Type type = n.getType();
//        if(type instanceof ReferenceType)
//        {
//            ReferenceType referenceType = (ReferenceType)type;
//            type = referenceType.getType();
//        }
//        if (type instanceof ClassOrInterfaceType) {
//            ClassOrInterfaceType classType = (ClassOrInterfaceType) type;
//            if (classType.getName().equals(arg[0])) {
//                classType.setName(arg[1]);
//            }
//
//        }
//    }
//
    @Override
    public void visit(NameExpr n, Object arg) {
        super.visit(n, arg);
        if (this.simpleNames.isEmpty()) {
            return;
        }
        if(this.simpleNames.containsKey(n.getName()))
        {
            n.setName(this.simpleNames.get(n.getName()));
        }
    }
//
//    @Override
//    public void visit(Parameter n, String[] arg) {
//        super.visit(n, arg);
//        Type type= n.getType();
//        if(type instanceof ReferenceType)
//        {
//            ReferenceType referenceType = (ReferenceType)type;
//            type = referenceType.getType();
//        }
//        if (type instanceof ClassOrInterfaceType) {
//            ClassOrInterfaceType classType = (ClassOrInterfaceType) type;
//            if (classType.getName().equals(arg[0])) {
//                classType.setName(arg[1]);
//            }
//
//        }
//    }

//    @Override
//    public void visit(ReferenceType n, String[] arg) {
//        if (n.getType() instanceof ClassOrInterfaceType) {
//            ClassOrInterfaceType classType = (ClassOrInterfaceType) n.getType();
//            if (classType.getName().equals(arg[0])) {
//                n.setType(new ClassOrInterfaceType("test"));
//            }
//
//        }
//    }

    @Override
    public void visit(MethodCallExpr n, Object arg) {
        super.visit(n, arg);
        n.getScope();
    }

    private String qualifiedToSimple(String get) {
        return get.substring(get.lastIndexOf(".")+1);
    }
    
    
    
    
}
