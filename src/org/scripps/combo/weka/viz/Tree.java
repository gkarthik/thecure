/**
 * 
 */
package org.scripps.combo.weka.viz;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.scripps.combo.weka.Weka;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.FastVector;

import weka.gui.treevisualizer.Edge;
import weka.gui.treevisualizer.Node;
import weka.gui.treevisualizer.TreeBuild;

/**
 * Handle data conversions related to tree visualizations
 * @author bgood
 *
 */
public class Tree {
	ObjectMapper mapper;
	ObjectNode json_root;
	
	
	
	public Tree() {
		mapper = new ObjectMapper();
		json_root = mapper.createObjectNode();
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		/*
		 D3 json tree rep
		 {
 "name": "flare",
 "children": [
  {
   "name": "analytics",
   "children": [
    {
     "name": "cluster",
     "children": [
      {"name": "AgglomerativeCluster", "size": 3938},
      {"name": "CommunityStructure", "size": 3812},
      {"name": "HierarchicalCluster", "size": 6714},
      {"name": "MergeEdge", "size": 743}
     ]
    }  
   ]
  }
 ]
}

		 */

		String train_file = "/Users/bgood/data/zoo.arff";
		Weka weka = new Weka(train_file, null);
		J48 classifier = new J48();
		classifier.setUnpruned(false); 
		Evaluation eval_train = new Evaluation(weka.getTrain());
		classifier.buildClassifier(weka.getTrain());
		eval_train.evaluateModel(classifier, weka.getTrain());
		//System.out.println(classifier.graph());
		FastVector nodes = new FastVector(); FastVector edges = new FastVector();

		TreeBuild builder = new TreeBuild();
		Node top = builder.create(new StringReader(classifier.graph()));
		//top = builder.create(new StringReader("digraph atree { top [label=\"the top\"] a [label=\"the first node\"] b [label=\"the second nodes\"] c [label=\"comes off of first\"] top->a top->b b->c }"));
		//outputTree(top);	   
		
		Tree t = new Tree();
		t.json_root.put("name", top.getLabel());
		t.outputJsonTreeNode(top, t.json_root);
		String json = t.mapper.writeValueAsString(t.json_root);
		System.out.println(json);

		//		ObjectNode json_root = mapper.createObjectNode();
	}


/**
 * Works with outputJsonTreeEdge to walk through a decision tree and convert it into a simple (edge-free) directed graph	
 * @param root
 * @param jroot
 * @throws JsonGenerationException
 * @throws JsonMappingException
 * @throws IOException
 */
	public void outputJsonTreeNode(Node root, ObjectNode jroot) throws JsonGenerationException, JsonMappingException, IOException {
		Edge edge;
	//	jroot.put("name", root.getLabel());
		//build the edge children
		ArrayNode edge_children = mapper.createArrayNode();
		for (int noa = 0;(edge = root.getChild(noa)) != null;noa++) {
			ObjectNode edgenode = mapper.createObjectNode();
			edgenode.put("name", edge.getLabel());
			outputJsonTreeEdge(edge, edgenode);	
			edge_children.add(edgenode);
		}
		if(edge_children.size()>0){
			jroot.put("children", edge_children);
		}
	}

	/**
	 * Works with outputJsonTreeNode to walk through a decision tree and convert it into a simple (edge-free) directed graph	
	 * @param root
	 * @param jroot
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */	
	public void outputJsonTreeEdge(Edge root, ObjectNode jroot) throws JsonGenerationException, JsonMappingException, IOException {		
		ArrayNode node_children = mapper.createArrayNode();
		Node target = root.getTarget();
		ObjectNode targetnode = mapper.createObjectNode();
		targetnode.put("name", target.getLabel());
		outputJsonTreeNode(target, targetnode);	
		node_children.add(targetnode);
		jroot.put("children", node_children);
	}
	
	/**
	 * Given the root node from a decision tree, recursively convert it into a jackson ObjectNode model that can be exported and used with the D3 javascript library
	 * @param root
	 * @param jroot
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */	
	public void outputJsonTree1(Node root, ObjectNode jroot) throws JsonGenerationException, JsonMappingException, IOException {
		Edge e;
		//build the edge children
		ArrayNode children = mapper.createArrayNode();
		for (int noa = 0;(e = root.getChild(noa)) != null;noa++) {
			ObjectNode jnode = mapper.createObjectNode();
			//jnode.put("edge_name", e.getLabel());
			String tlabel = e.getTarget().getLabel();
			if(tlabel.contains("(")){
				tlabel = ", then predict "+tlabel;
			}else{
				tlabel = ", then check "+tlabel;
			}
			jnode.put("name", e.getLabel()+tlabel);
			children.add(jnode);
			outputJsonTree1(e.getTarget(), jnode);				
		}
		if(children.size()>0){
			jroot.put("children", children);
		}
	}
	
	/**
	 * Pits out all of the nodes in a decision tree
	 * @param root
	 */
	public static void outputTextTree(Node root) {
		Edge e;
		Node child;
		System.out.println("Node "+root.getLabel()+" "+root.getRefer());
		for (int noa = 0;(e = root.getChild(noa)) != null;noa++) {
			System.out.println("edge "+e.getLabel());
			child = e.getTarget();
			outputTextTree(child);
		}	
	}

}
