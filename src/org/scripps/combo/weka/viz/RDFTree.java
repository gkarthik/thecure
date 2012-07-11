/**
 * 
 */
package org.scripps.combo.weka.viz;

import java.io.ByteArrayOutputStream;
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
public class RDFTree {
	ObjectMapper mapper;
	ObjectNode json_root;
	Model tree_model;
	Property treebranch;
	Property is_top;
	Property predicted_class;
	Property n_samples_at_node;
	Property n_incorrect_at_node;
	public static String base_uri = "http://genegames.org/tree/";


	public RDFTree() {
		mapper = new ObjectMapper();
		json_root = mapper.createObjectNode();
		tree_model = ModelFactory.createDefaultModel();
		treebranch = tree_model.createProperty(base_uri+"branch");
		is_top = tree_model.createProperty(base_uri+"is_top");
		predicted_class = tree_model.createProperty(base_uri+"predicted_class");
		n_samples_at_node = tree_model.createProperty(base_uri+"n_samples_at_node");
		n_incorrect_at_node = tree_model.createProperty(base_uri+"n_incorrect_at_node");
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

		String train_file = "/Users/bgood/data/zoo_mammals.arff";
		Weka weka = new Weka(train_file);
		J48 classifier = new J48();
		classifier.setUnpruned(false); 
		Evaluation eval_train = new Evaluation(weka.getTrain());
		classifier.buildClassifier(weka.getTrain());
		eval_train.evaluateModel(classifier, weka.getTrain());
		//System.out.println(classifier.graph());
		FastVector nodes = new FastVector(); FastVector edges = new FastVector();

		TreeBuild builder = new TreeBuild();
		Node top = builder.create(new StringReader(classifier.graph()));
		//		//top = builder.create(new StringReader("digraph atree { top [label=\"the top\"] a [label=\"the first node\"] b [label=\"the second nodes\"] c [label=\"comes off of first\"] top->a top->b b->c }"));
		//		//outputTree(top);	   
		//		
		RDFTree t = new RDFTree();
		//		t.json_root.put("name", top.getLabel());
		//		t.outputJsonTreeNode(top, t.json_root);
		//		String json = t.mapper.writeValueAsString(t.json_root);
		//		System.out.println(json);
		//
		//		//		ObjectNode json_root = mapper.createObjectNode();

		t.generateTreeRDF(top);
		RDFWriter writer = new JSONJenaWriter();	
		OutputStream sout = new ByteArrayOutputStream();
		writer.write(t.tree_model, sout, null);
		String out = sout.toString();
		System.out.println(out);
	}

/**
 * Starts off the process completed by 
 * outputRdfTreeNode
 * Indicates where the top of the tree is
 * @param top
 */
	public void generateTreeRDF(Node top) {
		//add top node
		Resource treeroot = tree_model.createResource();
		treeroot.addLiteral(RDFS.label, top.getLabel());
		treeroot.addLiteral(is_top, true);
		outputRdfTreeNode(top, treeroot);
		return;
	}

/**
 * Recursively step through a decision tree and generate an RDF structure that represents it
 * @param root
 * @param curr_root
 */
	public void outputRdfTreeNode(Node root, Resource curr_root)  {
		//check if leaf
		if(root.getChild(0)==null){
			//System.out.println(root.getLabel());
			String leaf = root.getLabel();
			String label = leaf.substring(0,leaf.indexOf("(")).trim();
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
			//System.out.println(label+"__"+bin_size+"--"+errors);
			curr_root.addLiteral(n_samples_at_node, bin_size);
			curr_root.addLiteral(n_incorrect_at_node, errors);
			curr_root.addLiteral(RDFS.label, label);
		}else{
			Edge edge;
			//find the edge children
			for (int noa = 0;(edge = root.getChild(noa)) != null;noa++) {				
				Resource edgenode = tree_model.createResource();
				edgenode.addLiteral(RDFS.label, edge.getLabel());
				curr_root.addProperty(treebranch, edgenode);
				
				Resource targetnode = tree_model.createResource();				
				edgenode.addProperty(treebranch, targetnode);
				//is it a leaf?
				if(edge.getTarget().getChild(0)!=null){
					targetnode.addLiteral(RDFS.label, edge.getTarget().getLabel());
				}
				outputRdfTreeNode(edge.getTarget(), targetnode);	
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

		public void outputRdfTreeEdge(Edge root, Resource jroot) throws JsonGenerationException, JsonMappingException, IOException {		
			ArrayNode node_children = mapper.createArrayNode();
			Node target = root.getTarget();
			ObjectNode targetnode = mapper.createObjectNode();
			targetnode.put("name", target.getLabel());
			outputJsonTreeNode(target, targetnode);	
			node_children.add(targetnode);
			jroot.put("children", node_children);
		}

	 */	


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
