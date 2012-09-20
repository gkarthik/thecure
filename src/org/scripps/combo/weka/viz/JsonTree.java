/**
 * 
 */
package org.scripps.combo.weka.viz;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.scripps.combo.weka.Weka;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

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
public class JsonTree {
	ObjectMapper mapper;
	ObjectNode json_root;
	int max_depth;
	int num_leaves;
	int tree_size;


	public JsonTree() {
		mapper = new ObjectMapper();
		json_root = mapper.createObjectNode();
		max_depth = 0;
		num_leaves = 0;
		tree_size = 0;
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

		String train_file = "/Users/bgood/data/zoo.arff"; //_mammals
		Weka weka = new Weka();
		weka.buildWeka(new FileInputStream(train_file), null, "mammal");
		J48 classifier = new J48();
		classifier.setUnpruned(false); 
		Evaluation eval_train = new Evaluation(weka.getTrain());
		classifier.buildClassifier(weka.getTrain());
		eval_train.evaluateModel(classifier, weka.getTrain());

		System.out.println(classifier.measureNumLeaves());
		System.out.println(classifier.measureTreeSize());

		//System.out.println(classifier.graph());
		//	FastVector nodes = new FastVector(); FastVector edges = new FastVector();  	
		JsonTree t = new JsonTree();
		String json = t.getJsonTreeString(classifier);
		System.out.println(json);
		System.out.println(t.max_depth);

	}

	public String getJsonTreeString(J48 classifier) throws Exception{
		num_leaves = (int) classifier.measureNumLeaves();
		tree_size = (int) classifier.measureTreeSize();
		TreeBuild builder = new TreeBuild();
		Node top = builder.create(new StringReader(classifier.graph()));   	
		json_root.put("name", top.getLabel());
		json_root.put("kind", "split_node");
		outputJsonTreeNode(top, json_root, 1);
		String json = mapper.writeValueAsString(json_root);
		return json;
	}


	/**
	 * Works with outputJsonTreeEdge to walk through a decision tree and convert it into a simple (edge-free) directed graph	
	 * @param root
	 * @param jroot
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void outputJsonTreeNode(Node root, ObjectNode jroot, int depth) throws JsonGenerationException, JsonMappingException, IOException {
		Edge edge;
		//handle the leaves
		if(root.getChild(0)==null){
			String leaf = root.getLabel();
			String label = leaf.substring(0,leaf.indexOf("(")).trim();
//			if(label.contains("non")){
//				label = "NR";
//			}else if(label.contains("relapse")){
//				label = "R";
//			}
			String count = leaf.substring(leaf.indexOf("("));
			float bin_size = 0; float errors = 0;
			if(count.contains("/")){
				String e = count.split("/")[1];
				e = e.substring(0,e.length()-1);
				errors = Float.parseFloat(e);
				String b = count.substring(1,count.indexOf("/"));
				bin_size = Float.parseFloat(b);
			}else{
				String b = count.substring(1,count.indexOf(")"));
				bin_size = Float.parseFloat(b);
			}
//			label += " :"+(int)bin_size+"/"+(int)errors;
			jroot.put("name", label);
			jroot.put("kind", "leaf_node");
			
			jroot.put("bin_size",bin_size);
			jroot.put("errors", errors);
			if(depth>max_depth){
				max_depth = depth;
			}
		}else{
			depth++;
			jroot.put("name", root.getLabel());
			jroot.put("kind", "split_node");
			//build the edge children
			ArrayNode edge_children = mapper.createArrayNode();
			for (int noa = 0;(edge = root.getChild(noa)) != null;noa++) {
				ObjectNode edgenode = mapper.createObjectNode();
				String edgename = edge.getLabel();
				if(edgename.contains("<")){
					edgename = "low";
				}else if(edgename.contains(">")){
					edgename = "high";
				}
				edgenode.put("name", edgename);
				edgenode.put("kind", "split_value");
				outputJsonTreeEdge(edge, edgenode, depth);	
				edge_children.add(edgenode);
			}
			if(edge_children.size()>0){
				jroot.put("children", edge_children);
			}
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
	public void outputJsonTreeEdge(Edge root, ObjectNode jroot, int depth) throws JsonGenerationException, JsonMappingException, IOException {		
		ArrayNode node_children = mapper.createArrayNode();
		Node target = root.getTarget();
		ObjectNode targetnode = mapper.createObjectNode();
		depth++;
		outputJsonTreeNode(target, targetnode, depth);	
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

	public ObjectMapper getMapper() {
		return mapper;
	}

	public void setMapper(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public ObjectNode getJson_root() {
		return json_root;
	}

	public void setJson_root(ObjectNode json_root) {
		this.json_root = json_root;
	}

	public int getMax_depth() {
		return max_depth;
	}

	public void setMax_depth(int max_depth) {
		this.max_depth = max_depth;
	}

	public int getNum_leaves() {
		return num_leaves;
	}

	public void setNum_leaves(int num_leaves) {
		this.num_leaves = num_leaves;
	}

	public int getTree_size() {
		return tree_size;
	}

	public void setTree_size(int tree_size) {
		this.tree_size = tree_size;
	}

}
