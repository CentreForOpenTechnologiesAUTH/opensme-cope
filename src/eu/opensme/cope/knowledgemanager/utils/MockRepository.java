package eu.opensme.cope.knowledgemanager.utils;

import eu.opensme.cope.componentmakers.common.MethodSignature;
import eu.opensme.cope.domain.GeneratedComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MockRepository {

    private int numberOfComponents;
    private int numberOfProvidedInterfaces;
    private int numberOfProvidedMethods;
    private int numberOfRequiredInterfaces;
    private int numberOfRequiredMethods;

    public MockRepository(int numberOfCOmponents, int numberOfProvidedInterfaces, int numberOfProvidedMethods, int numberOfRequiredInterfaces, int numberOfREquiredMethods) {
        this.numberOfComponents = numberOfCOmponents;
        this.numberOfProvidedInterfaces = numberOfProvidedInterfaces;
        this.numberOfProvidedMethods = numberOfProvidedMethods;
        this.numberOfRequiredInterfaces = numberOfRequiredInterfaces;
        this.numberOfRequiredMethods = numberOfREquiredMethods;
    }

    public HashMap<String,GeneratedComponent> init() {
        HashMap<String, GeneratedComponent> result = new HashMap<String, GeneratedComponent>();

        for (int i = 0; i < numberOfComponents; i++) {
            GeneratedComponent c = new GeneratedComponent("name" + i, null, null);
            for (int j = 0; j < numberOfProvidedInterfaces; j++) {
                String name = "providedInterface_" + i + "_" + j;
                c.addProvidedInterface(name, null);
                HashSet<MethodSignature> methods = new HashSet<MethodSignature>();
                for (int k = 0; k < numberOfProvidedMethods; k++) {
                    MethodSignature method = new MethodSignature("method_" + k + name, new String[]{"a", "b", "c"}, "void");
                    methods.add(method);
                }
                c.addMethodsToInterface(name, methods);
            }
            for (int j = 0; j < numberOfRequiredInterfaces; j++) {
                String name = "requiredInterface_" + i + "_" + j;
                c.addRequiredInterface(name, null);
                HashSet<MethodSignature> methods = new HashSet<MethodSignature>();
                for (int k = 0; k < numberOfRequiredMethods; k++) {
                    MethodSignature method = new MethodSignature("method_" + k + name, new String[]{"a", "b", "c"}, "void");
                    methods.add(method);
                }
                c.addMethodsToInterface(name, methods);
            }
            result.put(c.getComponentName(), c);
        }
        return result;
    }

    String flattenArray(String[] array) {
        String result = "";
        for (int i = 0; i < array.length; i++) {
            result += array[i];
            if(i < array.length - 1){
                result +=", ";
            }
        }
        return result;
    }
    public void print(GeneratedComponent c) {
        System.out.println("Name: " + c.getComponentName());
        System.out.println("Provided Interfaces: ");
        Set<String> interfaces = c.getInterfaces();
        for (String inter : interfaces) {
            System.out.println("\tInterface Name: " + inter);
            Set<MethodSignature> methods = c.getMethodsOfInterface(inter);
            for (MethodSignature method : methods) {
                System.out.println("\t\tMethod Name: " + method.getName());
                System.out.println("\t\tMethod Returns: " + method.getReturnType());
                System.out.println("\t\tMethod Parameters: " + flattenArray(method.getParameters()));
            }
        }
    }

    public static void main(String[] args) {
        MockRepository mock = new MockRepository(2, 2, 3, 5, 4);
        HashMap<String, GeneratedComponent> components = mock.init();
        for (String generatedComponentName : components.keySet()) {
            mock.print(components.get(generatedComponentName));
        }
    }
}
