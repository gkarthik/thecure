/**
 * 
 */
package org.scripps.combo.weka;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.scripps.combo.weka.Weka.execution;
import org.scripps.combo.weka.viz.JsonTree;

import weka.classifiers.Classifier;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;

/**
 * @author bgood
 *
 */
public class TestWeka {

	/**
	 * @param args
	 * @throws Exception 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, Exception {
		String dataset = "griffith_breast_cancer_1";
		String train_file = "/Users/bgood/workspace/aacure/WebContent/WEB-INF/pubdata/griffith/griffith_breast_cancer_1.arff";
		Weka weka = new Weka();
		weka.buildWeka(new FileInputStream(train_file), null, dataset);
		List<String> unique_ids = new ArrayList<String>();	
		unique_ids.add("2261"); //fgfr3 //("5984"); //rfc4 
		int nruns_cv = 1;
		Classifier model = null;
		J48 j48 = new J48();
		j48.setMinNumObj(1);
		j48.setReducedErrorPruning(false);
		j48.setSubtreeRaising(false);
		j48.setUnpruned(true);
		Weka.execution result = weka.pruneAndExecuteWithUniqueIds(unique_ids, j48, dataset, nruns_cv);	
		System.out.println("j48_4\n"+result.avg_percent_correct+"\n"+result.model.graph());
		//check if it gave up..
		if(j48.measureNumRules()==1){
			System.out.println("Did not return a tree");
			DecisionStump stump = new DecisionStump();
			result = weka.pruneAndExecuteWithUniqueIds(unique_ids, stump, dataset, nruns_cv);
			model = stump;
		}else{
			model = j48;
		}
		ClassifierEvaluation short_result = new ClassifierEvaluation((int)result.eval.pctCorrect(), result.model.getClassifier().toString());
		JSONObject r = new JSONObject(short_result);
		String eval_json = r.toString();
		String tree_json = "";
		JsonTree jtree = new JsonTree();
		try {		
			if(model.getClass().equals(J48.class)){
				tree_json = jtree.getJsonJ48AllInfo((J48) model, weka); 
			}else if(model.getClass().equals(DecisionStump.class)){
				tree_json = jtree.getJsonStumpAllInfo((DecisionStump) model, weka); 
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Died trying to get tree");
		}
		String treeoutput = "{\"evaluation\" : "+eval_json+", " +
		"\"max_depth\":\""+jtree.getMax_depth()+"\"," +
		"\"num_leaves\":\""+jtree.getNum_leaves()+"\"," +
		"\"tree_size\":\""+jtree.getTree_size()+"\"," +		
		"\"tree\":"+tree_json+"}";
		System.out.println(treeoutput);
	
//		DecisionStump stump = new DecisionStump();
//		result = weka.pruneAndExecuteWithUniqueIds(unique_ids, stump, dataset, nruns_cv);		
//		System.out.println("stump\n"+result+"\n"+result.model.toString());
	}

	
}
