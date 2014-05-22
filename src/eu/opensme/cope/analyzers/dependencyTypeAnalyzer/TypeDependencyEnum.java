/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.analyzers.dependencyTypeAnalyzer;

import java.util.HashMap;

/**
 *
 * @author sskalist
 */
public enum TypeDependencyEnum {
    
    Inheritance,MethodCall,FieldAccess,Instantiation,TypedDeclaration;
    private TypeDependencyEnum() {
    }
}
