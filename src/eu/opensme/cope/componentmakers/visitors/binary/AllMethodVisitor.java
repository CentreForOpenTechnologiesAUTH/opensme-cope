package eu.opensme.cope.componentmakers.visitors.binary;

import eu.opensme.cope.componentmakers.common.MethodSignature;
import eu.opensme.cope.analyzers.dependencyTypeAnalyzer.SubtypeDependencyEnum;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.NEWARRAY;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Type;

/**
 *
 * @author sskalist
 */
public class AllMethodVisitor extends EmptyVisitor {

    private JavaClass javaClass;
    private Method method;
    private MethodSignature methodSignature;
    private Set<String> externalClassesWithinProject;
    /** Method generation template. */
    private MethodGen methodWrapper;
    /* The class's constant pool. */
    private ConstantPoolGen constantPoolWrapper;
    private HashSet<String> newInstances;
    private HashSet<String> localVariables;
    private HashSet<String> parameters;
    private HashSet<String> returnTypes;
    private HashMap<String, SubtypeDependencyEnum> fieldAccess;
    private HashMap<String, Set<MethodSignature>> calledMethodsOfProjectClass;

    public AllMethodVisitor() {
        calledMethodsOfProjectClass = new HashMap<String, Set<MethodSignature>>();
        newInstances = new HashSet<String>();
        localVariables = new HashSet<String>();
        parameters = new HashSet<String>();
        returnTypes = new HashSet<String>();
        fieldAccess = new HashMap<String, SubtypeDependencyEnum>();
    }

    public void visit(JavaClass javaClass, Method method, Set<String> externalClassesWithinProject) {
        this.calledMethodsOfProjectClass.clear();
        newInstances.clear();
        localVariables.clear();
        parameters.clear();
        returnTypes.clear();
        fieldAccess.clear();
        this.methodSignature = null;
        this.javaClass = javaClass;
        this.method = method;
        this.externalClassesWithinProject = externalClassesWithinProject;

        this.methodWrapper = new MethodGen(method, javaClass.getClassName(), new ConstantPoolGen(javaClass.getConstantPool()));
        this.constantPoolWrapper = methodWrapper.getConstantPool();
        if (!methodWrapper.isAbstract() && !methodWrapper.isNative()) {
            for (InstructionHandle ih = this.methodWrapper.getInstructionList().getStart();
                    ih != null; ih = ih.getNext()) {
                Instruction i = ih.getInstruction();

                if (visitInstruction(i)) {
                    i.accept(this);
                }
            }
        }
        registerMethodSignature();

        this.javaClass = null;
        this.method = null;
        this.externalClassesWithinProject = null;
        this.methodWrapper = null;
        this.constantPoolWrapper = null;

    }

//    @Override
//    public void visitTypedInstruction(TypedInstruction obj) {
//        super.visitTypedInstruction(obj);
//        if (!checkFields()) {
//            return;
//        }
//        addType(obj.getType(constantPoolWrapper));
//
//    }
    @Override
    public void visitLocalVariableInstruction(LocalVariableInstruction localVariable) {
        if (!checkFields()) {
            return;
        }
        super.visitLocalVariableInstruction(localVariable);
        String className = className(localVariable.getType(constantPoolWrapper));
        if (externalClassesWithinProject.contains(className)) {
            localVariables.add(className);
        }
    }

    @Override
    public void visitInvokeInstruction(InvokeInstruction instruction) {
        super.visitInvokeInstruction(instruction);
        Type[] parametersTypes = instruction.getArgumentTypes(constantPoolWrapper);
        Type returnType = instruction.getReturnType(constantPoolWrapper);
        addMethodCall(instruction.getReferenceType(constantPoolWrapper), instruction.getMethodName(constantPoolWrapper), parametersTypes, returnType);
    }

    @Override
    public void visitINVOKEVIRTUAL(INVOKEVIRTUAL instruction) {
        super.visitINVOKEVIRTUAL(instruction);
        Type[] parametersTypes = instruction.getArgumentTypes(constantPoolWrapper);
        Type returnType = instruction.getReturnType(constantPoolWrapper);

            addMethodCall(this.javaClass.getSuperclassName(), instruction.getMethodName(constantPoolWrapper), parametersTypes, returnType);
    }

    @Override
    public void visitGETFIELD(GETFIELD field) {
        super.visitGETFIELD(field);
        String className = className(field.getLoadClassType(constantPoolWrapper));
        if (externalClassesWithinProject.contains(className) && !className.equals(this.javaClass.getClassName())) {
            if (!fieldAccess.containsKey(className)) {
                fieldAccess.put(className, SubtypeDependencyEnum.NonStatic);
            } else {
                SubtypeDependencyEnum result = fieldAccess.get(className);
                if (!result.equals(SubtypeDependencyEnum.NonStatic)) {
                    fieldAccess.put(className, SubtypeDependencyEnum.BOTH);
                }
            }
        }
    }

    @Override
    public void visitPUTFIELD(PUTFIELD field) {
        super.visitPUTFIELD(field);
        String className = className(field.getLoadClassType(constantPoolWrapper));
        if (externalClassesWithinProject.contains(className) && !className.equals(this.javaClass.getClassName())) {
            if (!fieldAccess.containsKey(className)) {
                fieldAccess.put(className, SubtypeDependencyEnum.NonStatic);
            } else {
                SubtypeDependencyEnum result = fieldAccess.get(className);
                if (!result.equals(SubtypeDependencyEnum.NonStatic)) {
                    fieldAccess.put(className, SubtypeDependencyEnum.BOTH);
                }
            }
        }
    }

    @Override
    public void visitGETSTATIC(GETSTATIC field) {
        super.visitGETSTATIC(field);
        String className = className(field.getLoadClassType(constantPoolWrapper));
        if (externalClassesWithinProject.contains(className)) {
            if (!fieldAccess.containsKey(className)) {
                if( !className.equals(this.javaClass.getClassName()))
                    fieldAccess.put(className, SubtypeDependencyEnum.Static);
            } else {
                SubtypeDependencyEnum result = fieldAccess.get(className);
                if (!result.equals(SubtypeDependencyEnum.Static)) {
                    fieldAccess.put(className, SubtypeDependencyEnum.BOTH);
                }
            }
        }
    }

    @Override
    public void visitPUTSTATIC(PUTSTATIC field) {
        super.visitPUTSTATIC(field);
        String className = className(field.getLoadClassType(constantPoolWrapper));
        if (externalClassesWithinProject.contains(className)) {
            if (!fieldAccess.containsKey(className)) {
                if( !className.equals(this.javaClass.getClassName()))
                    fieldAccess.put(className, SubtypeDependencyEnum.Static);
            } else {
                SubtypeDependencyEnum result = fieldAccess.get(className);
                if (!result.equals(SubtypeDependencyEnum.NonStatic)) {
                    fieldAccess.put(className, SubtypeDependencyEnum.BOTH);
                }
            }
        }
    }

//    @Override
//    public void visitINSTANCEOF(INSTANCEOF instruction) {
//        super.visitINSTANCEOF(instruction);
//        addType(instruction.getType(constantPoolWrapper));
//        addType(instruction.getLoadClassType(constantPoolWrapper));
//    }
//    @Override
//    public void visitCHECKCAST(CHECKCAST instruction) {
//        super.visitCHECKCAST(instruction);
//        addType(instruction.getType(constantPoolWrapper));
//        addType(instruction.getLoadClassType(constantPoolWrapper));
//    }
//    @Override
//    public void visitReturnInstruction(ReturnInstruction instruction) {
//        super.visitReturnInstruction(instruction);
//        addType(instruction.getType(constantPoolWrapper));
//    }
    @Override
    public void visitNEW(NEW obj) {
        super.visitNEW(obj);
        String className = className(obj.getLoadClassType(constantPoolWrapper));
        if (externalClassesWithinProject.contains(className)) {
            newInstances.add(className);
        }
    }

    @Override
    public void visitNEWARRAY(NEWARRAY obj) {
        super.visitNEWARRAY(obj);
        String className = className(obj.getType());
        if (externalClassesWithinProject.contains(className)) {
            newInstances.add(className);
        }
    }

    private void addMethodCall(Type referenceType, String methodName, Type[] parameterTypes, Type returnType) {
        addMethodCall(className(referenceType), methodName, parameterTypes, returnType);
    }

    private void addMethodCall(String className, String methodName, Type[] parameterTypes, Type returnType) {
        if (externalClassesWithinProject.contains(className)) {
            if (!this.calledMethodsOfProjectClass.containsKey(className)) {
                this.calledMethodsOfProjectClass.put(className, new HashSet<MethodSignature>());
            }
            String returnTypeName = className(returnType);
            String[] parameterTypeNames = new String[parameterTypes.length];
            for (int i = 0; i < parameterTypeNames.length; i++) {
                parameterTypeNames[i] = className(parameterTypes[i]);
            }
            this.calledMethodsOfProjectClass.get(className).add(new MethodSignature(methodName, parameterTypeNames, returnTypeName));
        }
    }

    private void registerMethodSignature() {
        Type[] parameterTypes = method.getArgumentTypes();
        String[] parameterTypeNames = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypeNames.length; i++) {
            parameterTypeNames[i] = className(parameterTypes[i]);
            if (externalClassesWithinProject.contains(parameterTypeNames[i])) {
                parameters.add(parameterTypeNames[i]);
            }
        }
        String returnType = className(method.getReturnType());
        if (externalClassesWithinProject.contains(returnType)) {
            returnTypes.add(returnType);
        }
        this.methodSignature = new MethodSignature(method.getName(), parameterTypeNames, returnType);
        this.methodSignature.setStatic(this.method.isStatic());
    }

    private boolean visitInstruction(Instruction i) {
        short opcode = i.getOpcode();

        return !((InstructionConstants.INSTRUCTIONS[opcode] != null)
                && !(i instanceof ConstantPushInstruction)
                && !(i instanceof ReturnInstruction));
    }

    private boolean checkFields() {
        if (javaClass == null || method == null || externalClassesWithinProject == null) {
            System.err.println("Fields were not set in " + AllMethodVisitor.class.toString() + ".");
            System.err.println("Use of method visit(JavaClass, Method, HashSet<String>) is required.");
            return false;
        }
        return true;
    }

    private static String className(Type type) {
        if (type == null) {
            return "java.PRIMITIVE";
        } else if (type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) type;
            return className(arrayType.getBasicType());
        } else {
            return type.toString();
        }
    }

    public Map<String, Set<MethodSignature>> getCalledMethodsOfProjectClass() {
        return calledMethodsOfProjectClass;
    }

    public MethodSignature getMethodSignature() {
        return methodSignature;
    }

    public HashMap<String, SubtypeDependencyEnum> getFieldAccess() {
        return fieldAccess;
    }

    public HashSet<String> getLocalVariables() {
        return localVariables;
    }

    public HashSet<String> getNewInstances() {
        return newInstances;
    }

    public HashSet<String> getParameters() {
        return parameters;
    }

    public HashSet<String> getReturnTypes() {
        return returnTypes;
    }
}
