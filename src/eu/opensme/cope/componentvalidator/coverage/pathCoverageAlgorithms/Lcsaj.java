/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.componentvalidator.coverage.pathCoverageAlgorithms;

import eu.opensme.cope.componentvalidator.coverage.cfg.CfgMethod;
import eu.opensme.cope.componentvalidator.coverage.cfg.CfgNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

/**
 *
 * @author thanasis
 */
public class Lcsaj {
    private ArrayList<JJArc> lcsajCfg;
    private HashMap<Integer, Integer> target;
    private int max;

    public Lcsaj() {
        lcsajCfg = new ArrayList<JJArc>();
    }
    
    public void computePaths(CfgMethod method){
        formGraph(method.getCfg());
        computeTargets();
        method.setPathToBeCovered(computeJJpaths(method.getCfg()));
    }
    
    public void formGraph(TreeSet<CfgNode> cfg){
        lcsajCfg = new ArrayList<JJArc>();
        Iterator<CfgNode> iterator = cfg.iterator();
        while(iterator.hasNext()){
            CfgNode node = iterator.next();
            if(node.getConnectedNodes().size() >0){
                Iterator<CfgNode> iterator2 = node.getConnectedNodes().iterator();
                while(iterator2.hasNext()){
                    CfgNode connectedNode = iterator2.next();
                    lcsajCfg.add(new JJArc(node.getCfgHashCode(), connectedNode.getCfgHashCode()));
                }
            }
        }
    }
    
    public ArrayList<JJArc> getArcsTo(int end){
        if(lcsajCfg != null){
                ArrayList<JJArc> arcsTo = new ArrayList<JJArc>();
                for(JJArc arc : lcsajCfg){
                        if (arc.getEnd() == end){
                                arcsTo.add(arc);
                        }
                }
                return arcsTo;
        }
        else{
                return null;
        }
    }
    
    public ArrayList<JJArc> getArcsFrom(int start){
        if(lcsajCfg != null){
                ArrayList<JJArc> arcsFrom = new ArrayList<JJArc>();
                for(JJArc arc : lcsajCfg){
                        if (arc.getStart() == start){
                                arcsFrom.add(arc);
                        }
                }
                return arcsFrom;
        }
        else{
                return null;
        }
    }
    
    public void computeTargets(){
        target = new HashMap<Integer, Integer>();
        target.put(1, 1); // !!!! (1,1st node)
        max = 1;
        for (int k = 2; k <= lcsajCfg.size()-1; k++){
                for (JJArc arc : getArcsTo(k)){
                        if (arc.getStart() != arc.getEnd()-1){
                                ++max;
                                target.put(max,k);
                        }
                }
        }
    }
    
    public HashMap<Integer, JJPath> computeJJpaths(TreeSet<CfgNode> cfg){
        HashMap<Integer, JJPath> jjpaths = new HashMap<Integer, JJPath>();
        int pathcnt = 0;
        for(int k = 1; k <= max; k++){
                int targetk = target.get(k);
                int j = targetk;
                boolean alljjpathsfromtargetkfound = false;
                boolean pathextendable = false;
                while(!alljjpathsfromtargetkfound){
                        for (JJArc arc : getArcsFrom(j)){
                                if (arc.getEnd() == j+1){
                                        pathextendable = true;
                                } else{
                                    jjpaths.put(pathcnt,new JJPath(targetk,j,arc.getEnd()));
                                    pathcnt++;
                                }
                        }
                        if(pathextendable){
                                ++j;
                                pathextendable = false;
                        } else{
                                alljjpathsfromtargetkfound = true;
                        }
                }
        }
        return jjpaths;
    }
}
