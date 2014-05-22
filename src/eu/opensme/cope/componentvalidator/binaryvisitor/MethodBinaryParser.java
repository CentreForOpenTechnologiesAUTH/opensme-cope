package eu.opensme.cope.componentvalidator.binaryvisitor;



import eu.opensme.cope.componentmakers.common.MethodSignature;
import java.util.HashSet;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Type;

/**
 *
 * @author sskalist
 */
public class MethodBinaryParser extends EmptyVisitor {

    /* The class's constant pool. */
    private ConstantPoolGen constantPoolWrapper;

    public MethodBinaryParser() {
    }

    public void visit(JavaClass javaClass, Method method) {
        MethodGen methodWrapper = new MethodGen(method, javaClass.getClassName(), new ConstantPoolGen(javaClass.getConstantPool()));
        this.constantPoolWrapper = methodWrapper.getConstantPool();
        if (!methodWrapper.isAbstract() && !methodWrapper.isNative()) {
            for (InstructionHandle ih = methodWrapper.getInstructionList().getStart();
                    ih != null; ih = ih.getNext()) {
                Instruction i = ih.getInstruction();

                if (visitInstruction(i)) {
                    i.accept(this);
                }
            }
        }
        this.constantPoolWrapper = null;
    }

    @Override
    public void visitInvokeInstruction(InvokeInstruction instruction) {
        super.visitInvokeInstruction(instruction);
        Type[] parametersTypes = instruction.getArgumentTypes(constantPoolWrapper);
        Type returnType = instruction.getReturnType(constantPoolWrapper);
        addMethodCall(instruction.getReferenceType(constantPoolWrapper), instruction.getMethodName(constantPoolWrapper), parametersTypes, returnType);
    }

    private void addMethodCall(Type referenceType, String methodName, Type[] parameterTypes, Type returnType) {
        if(JavaBinaryParser.componentClassesQualifiedNames.contains(JavaBinaryParser.className(referenceType,true))){
            addMethodCall(JavaBinaryParser.className(referenceType,true), methodName, parameterTypes, returnType);
        }
    }

    private void addMethodCall(String className, String methodName, Type[] parameterTypes, Type returnType) {        
        String[] parameterTypeNames = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypeNames.length; i++) {
            parameterTypeNames[i] = JavaBinaryParser.className(parameterTypes[i],false);
        }
        MethodSignature methodSignature = new MethodSignature(methodName, parameterTypeNames, JavaBinaryParser.className(returnType, false));
        
        String methodSignatureToString = JavaBinaryParser.methodSignatureToString(className, methodSignature);
        JavaBinaryParser.addMethod(methodSignatureToString);
        
        String classBinaryPath = JavaBinaryParser.qualifiedNameCompiledClassDirectoryMapping(className);
        
        HashSet<MethodSignature> methodsSet = new HashSet<MethodSignature>();
        methodsSet.add(methodSignature);
        JavaBinaryParser.classesMethodsMapping.put(classBinaryPath, methodsSet);
    }

    private boolean visitInstruction(Instruction i) {
        short opcode = i.getOpcode();

        return !((InstructionConstants.INSTRUCTIONS[opcode] != null)
                && !(i instanceof ConstantPushInstruction)
                && !(i instanceof ReturnInstruction));
    }

}
