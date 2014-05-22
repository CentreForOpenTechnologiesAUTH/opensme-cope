/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentmakers.common;

import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import org.apache.bcel.classfile.Method;

/**
 *
 * @author sskalist
 */
public class MethodSignature implements Serializable{

    private String name;
    private String returnType;
    private String[] parameters;
    private boolean isStatic;
    private String[] throwsTypes;

    
    public String getName() {
        return name;
    }

    public String[] getParameters() {
        return parameters;
    }

    public String getReturnType() {
        return returnType;
    }

    public MethodSignature(String name, String[] parameters, String returnType) {
        this.name = name;
        if(parameters == null)
            this.parameters = new String[0];
        else
            this.parameters = parameters;
        this.returnType = returnType;
        this.isStatic = false;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof MethodSignature) {
            return this.equals((MethodSignature) object);
        } else if (object instanceof MethodDeclaration) {
            return this.equals((MethodDeclaration) object);
        } else if (object instanceof Method){
            return this.equals((Method)object);
        }
        return false;
    }

    private boolean equals(MethodSignature signature) {
        if (!this.name.equals(signature.name)) {
            return false;
        }
        if (parameters.length != signature.parameters.length) {
            return false;
        }
        for (int i = 0; i < this.parameters.length; i++) {
            if (!this.parameters[i].equals(signature.parameters[i])) {
                return false;
            }
        }
        if(signature.isStatic)
            this.isStatic = true;
        if(this.isStatic)
            signature.isStatic = true;
        return true;
    }

    private boolean equals(MethodDeclaration declaration) {
        if (!this.name.equals(declaration.getName())) {
            return false;
        }
        List<Parameter> typeParameters = declaration.getParameters();
        if (typeParameters == null) {
            if (parameters.length == 0) {
                return true;
            }
            return false;
        }

        if (parameters.length != typeParameters.size()) {
            return false;
        }
        int i = 0;
        for (Parameter parameter : typeParameters) {
            if (!compareParameter(this.parameters[i++], parameter)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean equals(Method method){
//       if(!method.getName().equals(this.name))
//           return false;
//       if(method.getArgumentTypes().length != this.parameters.length)
//           return false;
       return true;
        
    }

    private boolean compareParameter(String stringParameter, Parameter parameter) {
        if (stringParameter.endsWith(parameter.getType().toString())) {
            return true;
        }
        return false;
    }

    public void setStatic(boolean value){
        this.isStatic = value;
    }
    
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 29 * hash + Arrays.deepHashCode(this.parameters);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder singature = new StringBuilder();
        singature.append(this.returnType);
        singature.append(" ");
        singature.append(this.name);
        singature.append("(");
        for (int i = 0; i < this.parameters.length - 1; i++) {
            singature.append(parameters[i]);
            singature.append(",");
        }
        if (this.parameters.length > 0) {
            singature.append(parameters[parameters.length - 1]);
        }
        singature.append(")");
        return singature.toString();
    }

    public void setThrows(String[] throwsTypes) {
        this.throwsTypes = throwsTypes;
    }
    
    public String[] getThrows(){
        return this.throwsTypes;
    }
}