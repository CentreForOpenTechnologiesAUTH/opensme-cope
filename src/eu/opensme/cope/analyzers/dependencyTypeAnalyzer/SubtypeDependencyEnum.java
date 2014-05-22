/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.analyzers.dependencyTypeAnalyzer;

/**
 *
 * @author sskalist
 */
public enum SubtypeDependencyEnum {
    Extends,
    Implements,
    Static,
    NonStatic,
    Instantiation,
    LocalVariable,
    Parameter,
    ReturnType,
    Field,
    BOTH}
