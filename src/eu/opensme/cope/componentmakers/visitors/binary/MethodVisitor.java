package eu.opensme.cope.componentmakers.visitors.binary;

import eu.opensme.cope.componentmakers.common.MethodSignature;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.CHECKCAST;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.INSTANCEOF;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Type;
import org.apache.bcel.generic.TypedInstruction;

/**
 *
 * @author sskalist
 */
public class MethodVisitor extends EmptyVisitor {

    private JavaClass javaClass;
    private Method method;
    private MethodSignature methodSignature;
    private Set<String> externalClassesWithinProject;
    /** Method generation template. */
    private MethodGen methodWrapper;
    /* The class's constant pool. */
    private ConstantPoolGen constantPoolWrapper;
    private HashSet<String> externalClassesUsed;
    private HashMap<String, Set<MethodSignature>> calledMethodsOfProjectClass;
//    private HashMap<String, HashSet<String[]>> parameterTypesOfMethod;

    public MethodVisitor() {
        this.calledMethodsOfProjectClass = new HashMap<String, Set<MethodSignature>>();
        this.externalClassesUsed = new HashSet<String>();
    }

    public void visit(JavaClass javaClass, Method method, Set<String> externalClassesWithinProject) {
        this.calledMethodsOfProjectClass.clear();
//        this.parameterTypesOfMethod.clear();
        this.externalClassesUsed.clear();
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

    @Override
    public void visitTypedInstruction(TypedInstruction obj) {
        super.visitTypedInstruction(obj);
        if (!checkFields()) {
            return;
        }
        addType(obj.getType(constantPoolWrapper));

    }

    @Override
    public void visitLocalVariableInstruction(LocalVariableInstruction localVariable) {
        if (!checkFields()) {
            return;
        }
        super.visitLocalVariableInstruction(localVariable);
        addType(localVariable.getType(constantPoolWrapper));
    }

    @Override
    public void visitArrayInstruction(ArrayInstruction instruction) {
        if (!checkFields()) {
            return;
        }
        super.visitArrayInstruction(instruction);
        addType(instruction.getType(constantPoolWrapper));
    }

    @Override
    public void visitInvokeInstruction(InvokeInstruction instruction) {
        super.visitInvokeInstruction(instruction);
        Type[] parametersTypes = instruction.getArgumentTypes(constantPoolWrapper);
        Type returnType = instruction.getReturnType(constantPoolWrapper);
        for (int i = 0; i < parametersTypes.length; i++) {
            addType(parametersTypes[i]);
        }
        addType(returnType);
        addType(instruction.getReferenceType(constantPoolWrapper));
        addMethodCall(instruction.getReferenceType(constantPoolWrapper), instruction.getMethodName(constantPoolWrapper), parametersTypes, returnType);
    }

    @Override
    public void visitINVOKEVIRTUAL(INVOKEVIRTUAL instruction) {
        super.visitINVOKEVIRTUAL(instruction);
        Type[] parametersTypes = instruction.getArgumentTypes(constantPoolWrapper);
        Type returnType = instruction.getReturnType(constantPoolWrapper);
        for (int i = 0; i < parametersTypes.length; i++) {
            addType(parametersTypes[i]);
        }
        try {
            if(this.externalClassesWithinProject.contains(this.javaClass.getSuperclassName()))
                addMethodCall(this.javaClass.getSuperClass().getClassName(), instruction.getMethodName(constantPoolWrapper), parametersTypes, returnType);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MethodVisitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void visitFieldInstruction(FieldInstruction instruction) {
        addType(instruction.getType(constantPoolWrapper));
        addType(instruction.getFieldType(constantPoolWrapper));
    }

    @Override
    public void visitINSTANCEOF(INSTANCEOF instruction) {
        super.visitINSTANCEOF(instruction);
        addType(instruction.getType(constantPoolWrapper));
        addType(instruction.getLoadClassType(constantPoolWrapper));
    }

    @Override
    public void visitCHECKCAST(CHECKCAST instruction) {
        super.visitCHECKCAST(instruction);
        addType(instruction.getType(constantPoolWrapper));
        addType(instruction.getLoadClassType(constantPoolWrapper));
    }

    @Override
    public void visitReturnInstruction(ReturnInstruction instruction) {
        super.visitReturnInstruction(instruction);
        addType(instruction.getType(constantPoolWrapper));
    }

    private void addType(Type type) {
        String className = className(type);
        if (externalClassesWithinProject.contains(className)) {
            externalClassesUsed.add(className);
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
            externalClassesUsed.add(className);

        }
    }

    private void registerMethodSignature() {
        Type[] parameterTypes = method.getArgumentTypes();
        String[] parameterTypeNames = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypeNames.length; i++) {
            addType(parameterTypes[i]);
            parameterTypeNames[i] = className(parameterTypes[i]);
        }
        addType(method.getReturnType());
        this.methodSignature = new MethodSignature(method.getName(), parameterTypeNames, className(method.getReturnType()));
    }

    private boolean visitInstruction(Instruction i) {
        short opcode = i.getOpcode();

        return !((InstructionConstants.INSTRUCTIONS[opcode] != null)
                && !(i instanceof ConstantPushInstruction)
                && !(i instanceof ReturnInstruction));
    }

    private boolean checkFields() {
        if (javaClass == null || method == null || externalClassesWithinProject == null) {
            System.err.println("Fields were not set in " + MethodVisitor.class.toString() + ".");
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

    public Set<String> getExternalClassesUsed() {
        return externalClassesUsed;
    }

    public MethodSignature getMethodSignature() {
        return methodSignature;
    }
}
