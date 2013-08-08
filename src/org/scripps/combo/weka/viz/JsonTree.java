/**
 * 
 */
package org.scripps.combo.weka.viz;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openjena.atlas.json.JsonArray;
import org.scripps.combo.model.Attribute;
import org.scripps.combo.model.Feature;
import org.scripps.combo.weka.ClassifierEvaluation;
import org.scripps.combo.weka.Weka;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
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
import weka.classifiers.trees.ManualTree;
import weka.classifiers.trees.j48.C45Split;
import weka.classifiers.trees.j48.ClassifierTree;
import weka.classifiers.trees.j48.NoSplit;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.Utils;

import weka.gui.treevisualizer.Edge;
import weka.gui.treevisualizer.Node;
import weka.gui.treevisualizer.TreeBuild;

/**
 * Handle data conversions related to tree visualizations.  Focus on representing Weka J48 trees in D3.
 * D3 likes trees to look like:
 * 
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

 * @author bgood
 *
 */
public class JsonTree {
	ObjectMapper mapper;
	ObjectNode json_root;
	int max_depth;
	int num_leaves;
	int tree_size;
	Map<String, String> attname_nodename;
	Map<Integer, String> attindex_nodename;

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

		//		String train_file = "/Users/bgood/workspace/acure/WebContent/WEB-INF/pubdata/griffith/griffith_breast_cancer_1.arff";
		String train_file = "/Users/bgood/data/zoo_mammals.arff"; //_mammals
		Weka weka = new Weka();
		weka.buildWeka(new FileInputStream(train_file), null, "mammal"); //griffith_breast_cancer_1
		J48 classifier = new J48();
		//		classifier.setUnpruned(false); 
		//		Evaluation eval_train = new Evaluation(weka.getTrain());
		//		classifier.buildClassifier(weka.getTrain());
		//		eval_train.evaluateModel(classifier, weka.getTrain());
		List<String> unique_ids = new ArrayList<String>(); 
		unique_ids.add("mammal_6"); unique_ids.add("mammal_7"); unique_ids.add("mammal_8"); unique_ids.add("mammal_9");
		unique_ids.add("mammal_12"); unique_ids.add("mammal_13"); 
		weka.setEval_method("train");
		Weka.execution result = weka.pruneAndExecuteWithUniqueIds(unique_ids, classifier, "mammal");
		ClassifierEvaluation short_result = new ClassifierEvaluation((int)result.eval.pctCorrect(), result.model.getClassifier().toString());
		System.out.println("pct correct = "+short_result.getAccuracy());		

		System.out.println(classifier.toString()); //+"\n"+eval_train.pctCorrect()

		//System.out.println(classifier.graph());
		//		//	FastVector nodes = new FastVector(); FastVector edges = new FastVector();  	
		//		JsonTree t = new JsonTree();
		//		String json = t.getJsonTreeStringFromGraph(classifier, weka);
		//		System.out.println(json);
		//		System.out.println(t.max_depth);

		JsonTree t = new JsonTree();
		String json1 = t.getJsonTreeAllInfo(classifier, weka);
		System.out.println(json1);
		ManualTree readtree = t.parseJsonJ48(weka, json1);
		Evaluation maneval = new Evaluation(weka.getTrain());
		maneval.evaluateModel(readtree, weka.getTrain());
		System.out.println(readtree.toString()+"\npct correct = "+maneval.pctCorrect());

	}

	/**
	 * script for testing the life cycle json tree (from web site)-> weka tree -> evaluation -> output for website
	 //TODO if desired, finish this with an identity test of the two tree outputs and evaluations
		//right now eyeballing it and its close but not identical 
		//- tree building process choosing its own, sometimes slightly different split points.
		//- sometimes goes deeper than starting tree...
	 //TODO decide on and implement an evaluation response for web client		
	 */
	public void testManualTreeParseCreate(){
		/**
		 * Test on Mammal dataset - nominal and numeric
		 */
		System.out.println("testing json parse, tree build, evaluate on mammal classification dataset.");
		String train_file = "/Users/bgood/data/zoo_mammals.arff"; //_mammals
		Weka weka = new Weka();
		try {
			weka.buildWeka(new FileInputStream(train_file), null, "mammal");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		J48 classifier = new J48();
		//filter for a set that will give a more interesting tree than if milk then mammal..
		List<String> unique_ids = new ArrayList<String>(); 
		unique_ids.add("mammal_6"); unique_ids.add("mammal_7"); unique_ids.add("mammal_8"); unique_ids.add("mammal_9");
		unique_ids.add("mammal_12"); unique_ids.add("mammal_13"); 
		weka.setEval_method("train");
		Weka.execution result = weka.pruneAndExecuteWithUniqueIds(unique_ids, classifier, "mammal");
		ClassifierEvaluation short_result = new ClassifierEvaluation((int)result.eval.pctCorrect(), result.model.getClassifier().toString());
		System.out.println("pct correct = "+short_result.getAccuracy());		
		System.out.println(classifier.toString());
		String json1;
		try {
			//here is the test json
			json1 = getJsonTreeAllInfo(classifier, weka);
			System.out.println(json1);
			//build the weka tree
			ManualTree readtree = parseJsonJ48(weka, json1);
			//evaluate it
			Evaluation maneval = new Evaluation(weka.getTrain());
			maneval.evaluateModel(readtree, weka.getTrain());	
			System.out.println(readtree.toString()+"\npct correct = "+maneval.pctCorrect());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/**
		 * Test on gene expression dataset
		 */
		System.out.println("testing json parse, tree build, evaluate on gene expression dataset.");
		train_file = "/Users/bgood/workspace/acure/WebContent/WEB-INF/pubdata/griffith/griffith_breast_cancer_1.arff";
		weka = new Weka();
		try {
			weka.buildWeka(new FileInputStream(train_file), null, "griffith_breast_cancer_1");
			classifier = new J48();
			classifier.setUnpruned(false); 
			Evaluation eval_train = new Evaluation(weka.getTrain());
			classifier.buildClassifier(weka.getTrain());
			eval_train.evaluateModel(classifier, weka.getTrain());
			System.out.println(classifier.toString()+"\n"+eval_train.pctCorrect());

			json1 = getJsonTreeAllInfo(classifier, weka);
			System.out.println(json1);
			ManualTree readtree = parseJsonJ48(weka, json1);
			Evaluation maneval = new Evaluation(weka.getTrain());
			maneval.evaluateModel(readtree, weka.getTrain());
			System.out.println(readtree.toString()+"\npct correct = "+maneval.pctCorrect());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Given the structure of a decision tree, create and evaluate it using weka
	 * Assumes the weka instance that it receives has already been populated with an 
	 * appropriate dataset
	 * @param weka
	 * @param jsontree
	 * @return
	 */
	public ManualTree parseJsonJ48(Weka weka, String jsontree){
		ManualTree tree = new ManualTree();
		try {
			JsonNode rootNode = mapper.readTree(jsontree);
			tree.setTreeStructure(rootNode);
			tree.buildClassifier(weka.getTrain());
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tree;
	}


	/**
	 * Render a trained J48 decision tree as a json object, including size and quality data for each split.	
	 * @param classifier
	 * @return
	 * @throws Exception
	 */
	public String getJsonTreeAllInfo(J48 classifier, Weka weka) throws Exception{
		if(classifier==null){
			return "";
		}
		//init the name mappings
		//get map from attributes to desired output name
		attname_nodename = new HashMap<String, String>();
		attindex_nodename = new HashMap<Integer, String>();
		for(Feature f : weka.getFeatures().values()){
			for(Attribute att : f.getDataset_attributes()){
				String displayname = f.getShort_name();
				attname_nodename.put(att.getName(), displayname);			
			}
		}
		ClassifierTree tree = classifier.getM_root();
		if(tree==null){
			return "";
		}
		num_leaves = (int) classifier.measureNumLeaves();
		tree_size = (int) classifier.measureTreeSize();  	
		//		boolean ismammal = false;
		//		if(weka.getDataset().equals("mammal")){
		//			ismammal = true;
		//		}
		getJsonTree(tree, json_root, 0);
		String json = mapper.writeValueAsString(json_root);
		return json;
	}

	/**
	 * Recursively build a jackson Object model for a weka tree.
	 * Each split node in the tree is represented by a single ClassifierTree object
	 * Note that this requires some modification to the weka source.  Had to add accessor methods to 
	 * get to private data about the node like its distribution object.
	 * Incorporated (forked) the relevant weka source files into the cure codebase so I could change them.
	 * @param tree
	 * @param jroot
	 * @param depth
	 * @throws Exception
	 */
	public void getJsonTree(ClassifierTree tree, ObjectNode jroot, int depth) throws Exception{
		depth++; 
		if(tree.isM_isLeaf()){
			NoSplit split = (NoSplit)tree.getM_localModel();			
			String label = ((Instances)tree.getM_train()).classAttribute().value(split.getM_distribution().maxClass(0));
			double bin_size = Utils.roundDouble(split.getM_distribution().perBag(0),2);
			double errors = Utils.roundDouble(split.getM_distribution().numIncorrect(0),2);
			jroot.put("kind", "leaf_node");
			jroot.put("name", label);
			jroot.put("bin_size", bin_size);
			jroot.put("errors", errors);		
			if(depth>max_depth){
				max_depth = depth;
			}
			return;
		}else{
			C45Split split = (C45Split)tree.getM_localModel();	
			//collect features to describe this split node
			//1.7976931348623157E308
			//1.7976931348623157E308
			double split_point = split.getM_splitPoint();
			String split_tag = split_point+"";
			boolean nominal = false;
			if(split_point == Double.MAX_VALUE){
				//then we have a nominal data type
				nominal = true;
				split_tag = "nominal";
			}

			double gain_ratio = split.gainRatio();
			double infogain = split.infoGain();
			double total_below = split.getM_distribution().getTotaL();
			//TODO assuming again binary splits (or there would be more than 0,1 bags)
			double total_below_left = Utils.roundDouble(split.getM_distribution().perBag(0),2);
			double total_below_right = Utils.roundDouble(split.getM_distribution().perBag(1),2);

			//errors are estimates right here - like if this node was a leaf.
			//these do not account for downstream
			double errors_left = Utils.roundDouble(split.getM_distribution().numIncorrect(0),2);
			double errors_right = Utils.roundDouble(split.getM_distribution().numIncorrect(1),2);
			double pct_correct_below = 100*(total_below-errors_right-errors_left)/total_below;

			jroot.put("split_point", split_tag);
			String name = "";//attindex_nodename.get(split.attIndex()); //TODO fix this when att)index has changed because of a filter.
			String attribute_name = tree.getM_train().attribute(split.attIndex()).name();
			name = attname_nodename.get(attribute_name);
			//String longname = attindex_longnodename.get(split.attIndex());
			jroot.put("name", name);
			//jroot.put("longname", longname);
			jroot.put("id", name);
			jroot.put("attribute_name", attribute_name);
			jroot.put("kind", "split_node");	
			jroot.put("gain_ratio", gain_ratio);
			jroot.put("infogain", infogain);
			jroot.put("total_below_left", total_below_left);
			jroot.put("total_below_right", total_below_right);
			jroot.put("bin_size", split.getM_distribution().getTotaL());
			jroot.put("errors_from_left", errors_left);
			jroot.put("errors_from_right", errors_right);
			jroot.put("total_errors_here", errors_right+errors_left);
			jroot.put("pct_correct_here", pct_correct_below);
			double[] bag_sizes = split.getM_distribution().getM_perBag();
			// walk down to collect outgoing edges
			// each "bag" corresponds to a group of instances following down one edge
			ArrayNode edge_children = mapper.createArrayNode();
			for(int i=0; i<tree.getM_sons().length; i++){
				ObjectNode edgenode = mapper.createObjectNode();
				String full_edgename = ""; String short_edgename = "";
				//custom for binary split situation - //TODO needs work to generalize
				if(i==0){
					full_edgename = "<= "+split_point;
					short_edgename = "low";
				}else if(i==1){
					full_edgename = "> "+split_point;
					short_edgename = "high";
				}
				//TODO generalize
				//cure1 specific 
				if(name.equals("legs")){
					short_edgename = full_edgename;
				}
				//check for nominal values
				if(nominal){
					short_edgename = ((Instances)tree.getM_train()).attribute(split.attIndex()).value(i);
					full_edgename = short_edgename;
				}				
				//add to json rep
				edgenode.put("name", short_edgename);
				edgenode.put("threshold", full_edgename);
				edgenode.put("bin_size", bag_sizes[i]);
				edgenode.put("kind", "split_value");
				//TODO figure out bag_class -> displayable value about purity here

				//hook up to edge targets
				ArrayNode edge_target = mapper.createArrayNode();
				ObjectNode target = mapper.createObjectNode();
				ClassifierTree son = tree.getM_sons()[i];		
				//recurse!
				getJsonTree(son, target,depth+1);
				edge_target.add(target);
				edgenode.put("children", edge_target);				
				edge_children.add(edgenode);
			}
			if(edge_children.size()>0){
				jroot.put("children", edge_children);
			}

		}
		return;
	}

	/**
	 * This uses the graph constructed by weka to produce a json view of the tree.
	 * Good because it does not require changes to weka source.
	 * Bad because it makes it impossible to get to a lot of useful information about split nodes.
	 * @param classifier
	 * @param weka
	 * @return
	 * @throws Exception
	 */
	public String getJsonTreeStringFromGraph(J48 classifier, Weka weka) throws Exception {
		//get map from attributes to desired output name
		Map<String, String> att_dname = new HashMap<String, String>();
		for(Feature f : weka.getFeatures().values()){
			for(Attribute att : f.getDataset_attributes()){
				String displayname = f.getShort_name();
				att_dname.put(att.getName(), displayname);
			}
		}		
		num_leaves = (int) classifier.measureNumLeaves();
		tree_size = (int) classifier.measureTreeSize();
		TreeBuild builder = new TreeBuild();
		Node top = builder.create(new StringReader(classifier.graph()));   	
		json_root.put("name", att_dname.get(top.getLabel()));
		json_root.put("id", top.getLabel());
		json_root.put("kind", "split_node");
		outputJsonTreeNode(top, json_root, 1, att_dname);
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
	public void outputJsonTreeNode(Node root, ObjectNode jroot, int depth, Map<String, String> att_dname) throws JsonGenerationException, JsonMappingException, IOException {
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
			jroot.put("name", att_dname.get(root.getLabel()));
			jroot.put("id", root.getLabel());
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
				edgenode.put("threshold", edge.getLabel());
				outputJsonTreeEdge(edge, edgenode, depth, att_dname);	
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
	public void outputJsonTreeEdge(Edge root, ObjectNode jroot, int depth, Map<String, String> att_dname) throws JsonGenerationException, JsonMappingException, IOException {		
		ArrayNode node_children = mapper.createArrayNode();
		Node target = root.getTarget();
		ObjectNode targetnode = mapper.createObjectNode();
		depth++;
		outputJsonTreeNode(target, targetnode, depth, att_dname);	
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
	public void outputJsonTreeJacksonStruct(Node root, ObjectNode jroot) throws JsonGenerationException, JsonMappingException, IOException {
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
			outputJsonTreeJacksonStruct(e.getTarget(), jnode);				
		}
		if(children.size()>0){
			jroot.put("children", children);
		}
	}

	/**
	 * Prints out all of the nodes in a decision tree
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
