package org.scripps.combo.weka;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;
import org.scripps.combo.evaluation.ClassifierEvaluation;
import org.scripps.combo.model.Board;
import org.scripps.combo.model.Card;
import org.scripps.combo.model.Feature;
import org.scripps.combo.model.Game;
import org.scripps.combo.model.Player;
import org.scripps.combo.model.Tree;
import org.scripps.combo.model.Badge;
import org.scripps.combo.weka.Weka.execution;
import org.scripps.combo.weka.Weka.metaExecution;
import org.scripps.combo.weka.viz.JsonTree;
import org.scripps.util.GenerateCSV;
import org.scripps.util.JdbcConnection;
import org.scripps.util.Mail;
import org.scripps.util.MapFun;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.type.TypeReference;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ReliefFAttributeEval;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.ManualTree;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Servlet implementation class WekaServer
 */
public class MetaServer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	Map<String, Weka> name_dataset;
	ObjectMapper mapper;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public MetaServer() {
		super();
		name_dataset = new HashMap<String, Weka>();
		mapper = new ObjectMapper();
	}

	/**
	 * Initialize the service.  This only runs the first time a request is made to this servlet.  It reads local 
	 * configuration files and loads up all the data needed to run the game including all of the annotation information
	 * that is stored in the database.  This information is held in a Weka object associated with each live dataset.
	 * This takes a long time to run when there is a lot of data to load - but once its finished, things go pretty quickly...
	 */
	public void init(ServletConfig config){		
		//load all active datasets
		ServletContext context = config.getServletContext();
		//configure this deployment
		String training_level_1_data = "";
		String training_level_1_name = "";
		String active_data = "";
		String active_data_name = "";
		String testing_data = "";
		String testing_data_name = "";
		try{
	        InputStream in = MetaServer.class.getResourceAsStream("/props/game.properties");	        
	        Properties props = new Properties();
	        props.load(in);
	        training_level_1_data = props.getProperty("training_level_1_data");
	        training_level_1_name = props.getProperty("training_level_1_name");
	        active_data = props.getProperty("active_data");
	        active_data_name = props.getProperty("active_data_name");
	        testing_data = props.getProperty("testing_data");
	        testing_data_name = props.getProperty("testing_data_name");
	       } 
	    catch(Exception e){
	        System.out.println("error" + e);
	       }	 
		
		//training game data 
		try { 
			InputStream train_loc = context.getResourceAsStream(training_level_1_data);
			Weka mammal_weka = new Weka();
			String dataset = training_level_1_name;
			mammal_weka.buildWeka(train_loc, null, dataset);
			mammal_weka.setEval_method("training_set");
			name_dataset.put(dataset, mammal_weka);
			train_loc.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//live game data
		try {
			String dataset = active_data_name;
			InputStream train_loc = context.getResourceAsStream(active_data);
			InputStream test_loc = context.getResourceAsStream(testing_data);
			Weka weka = new Weka();
			weka.buildWeka(train_loc, test_loc, dataset);	
			weka.setEval_method("test_set");
			name_dataset.put(dataset, weka);	
			train_loc.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		//Griffith Dataset
		try {
			Weka wekaGriffith = new Weka();
			InputStream live_loc = context.getResourceAsStream("/WEB-INF/pubdata/griffith/griffith_breast_cancer_1.arff");
			String dataset_name = "griffith_breast_cancer_1";
			wekaGriffith.buildWeka(live_loc, null, dataset_name);
			name_dataset.put(dataset_name, wekaGriffith);
			live_loc.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		//		Enumeration e = request.getParameterNames();
		//		while(e.hasMoreElements()){
		//			System.out.println(e.nextElement());
		//		}		
		String command = request.getParameter("command");
		if(command!=null){
			routeGet(command, request, response);
		}else{
			handleBadRequest(request, response, "no command sent as GET"); 
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String t = request.getContentType();
		//System.out.println("content type "+t);
		if(t!=null&&t.startsWith("application/json")){
			String json = extractJson(request);
			if(json!=null){
				//System.out.println(json);
				LinkedHashMap postData = mapper.readValue(json, LinkedHashMap.class);	
				String command = null;
				if(postData!=null){
					Object command_ = postData.get("command");
					if(command_!=null){
						command = (String)command_;
						//route to appropriate functions
						if(command.equals("getscore")){
							getScore(postData, request, response);
						}else if(command.equals("savehand")){
							saveHand(postData, request, response);
						}else if(command.equals("saveplayedcard")){
							savePlayedCard(postData, request, response);
						}else if(command.equals("scoretree")||(command.equals("savetree"))){
							//TODO clean this up so we aren't parsing the json twice.. 
							JsonNode treedata = mapper.readTree(json);	
							try{
								scoreSaveManualTree(treedata, request, response);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								handleBadRequest(request, response, "Failed to get score for manual tree: "+json);
							}
						}else if(command.equals("get_clinical_features")){   // //get_clinical_features //get_trees_all, get_trees_ip, get_trees_user_id
							//TODO clean this up so we aren't parsing the json twice.. 
							JsonNode data = mapper.readTree(json);	
							try{
								getClinicalFeatures(data, request, response);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								handleBadRequest(request, response, "Failed to get clinical features: "+json);
							}						
						}else if(command.equals("get_rank")){  
							getRankofTree((int)postData.get("tree_id"), request, response);
						}else if(command.startsWith("get_tree")){  //get_trees_all, get_trees_ip, get_trees_user_id
							//TODO clean this up so we aren't parsing the json twice.. 
							JsonNode data = mapper.readTree(json);	
							try{
								getTreeList(data, request, response);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								handleBadRequest(request, response, "Failed to get trees: "+json);
							}
							
						}else if(command.contains("badge")){  //get_trees_all, get_trees_ip, get_trees_user_id
							//TODO clean this up so we aren't parsing the json twice.. 
							JsonNode data = mapper.readTree(json);	
							try{
								if(command.equals("add_badge")){
									addBadge(data, request, response);
								} else if(command.equals("get_badges")){
									getBadges(data, request, response);
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								handleBadRequest(request, response, "Failed to add bagde: "+json);
							}	
						}
						/*
						 * API end points to get evaluation of random forest.
						 * 
						 	else if(command.contains("evaluate")){
							if(command.contains("evaluate_randomforest")){ 
								JsonNode data = mapper.readTree(json);	
								try{
									evaluateForest(data, request, response);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									handleBadRequest(request, response, "Failed to evaluate: "+json);
								}	
							} else if(command.contains("evaluate_TreeorSMO")){ 
								JsonNode data = mapper.readTree(json);	
								try{
									evaluateTreeorSMO(data, request, response);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									handleBadRequest(request, response, "Failed to evaluate: "+json);
								}	
							}
						}
						*/
					}else{
						handleBadRequest(request, response, "No command found in json request");
					}
				}else{
					handleBadRequest(request, response, "Posted data could not be parsed to a LinkedHashMap");
				}
			}else{
				handleBadRequest(request, response, "Posted data null or could not be parsed");
			}
		}else{
			handleBadRequest(request, response, "no json data received");
		}
	}




	private void routeGet(String command, HttpServletRequest request, HttpServletResponse response) throws IOException {
		//route to appropriate functions
		if(command.equals("getboard")){
			//works
			getBoard(request, response);
		}

	}

	private String extractJson(HttpServletRequest request) throws UnsupportedEncodingException{
		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
		} catch (Exception e) { /*report an error*/ }
		String json = jb.toString();
		return json;
	}

	/**
	 * Get a board for the game from the database
	 * Send it a valid board_id
	 * @param request
	 * @param response
	 * @param weka
	 * @throws IOException 
	 */
	private void getBoard(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String board_id = request.getParameter("board_id");
		boolean getmeta = true;
		Board board = Board.getBoardById(board_id, getmeta);
		boolean shuffle = true;
		String json = board.toJSON(shuffle);
		response.setContentType("text/json");
		PrintWriter out = response.getWriter();
		out.write(json);
		out.close();
	}

	/**
	 * Score a collection of features, a full or partial hand in a game
	 * Send it a comma delimited list of unique ids that match up with the unique ids of features in the database
	 * @param request
	 * @param response
	 * @param weka
	 * @throws IOException 
	 */
	private void getScore(LinkedHashMap data, HttpServletRequest request_, HttpServletResponse response) throws IOException {

		String board_id = (String)data.get("board_id");
		boolean getmeta = false;
		Board board = Board.getBoardById(board_id, getmeta);		
		Weka weka = name_dataset.get(board.getDataset());
		if(weka==null){
			handleBadRequest(request_, response, "no dataset loaded for name: "+board.getDataset());
			return;
		}		
		List<String> unique_ids = new ArrayList<String>();
		unique_ids = (List<String>)data.get("unique_ids");

		Classifier model = null;
		J48 j48 = new J48();
		int nruns_cv = 10;
		Weka.execution result = weka.pruneAndExecuteWithUniqueIds(unique_ids, j48, board.getDataset(), nruns_cv);
		
		if(j48.measureNumRules()==1){
//			System.out.println("Did not return a tree");
			DecisionStump stump = new DecisionStump();
			result = weka.pruneAndExecuteWithUniqueIds(unique_ids, stump, board.getDataset(), nruns_cv);
			model = stump;
		}else{
			model = j48;
		}
		JsonTree jtree = new JsonTree();
		String tree_json = "";
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
		ClassifierEvaluation short_result = new ClassifierEvaluation((int)result.eval.pctCorrect(), result.model.getClassifier().toString());
		JSONObject r = new JSONObject(short_result);
		String eval_json = r.toString();
		response.setContentType("text/json");
		PrintWriter out = response.getWriter();
		eval_json = r.toString();
		String treeoutput = "{\"evaluation\" : "+eval_json+", " +
		"\"max_depth\":\""+jtree.getMax_depth()+"\"," +
		"\"num_leaves\":\""+jtree.getNum_leaves()+"\"," +
		"\"tree_size\":\""+jtree.getTree_size()+"\"," +		
		"\"tree\":"+tree_json+"}";
		//System.out.println(treeoutput);
		out.write(treeoutput);
		out.close();
	}
	
	/**
	 * Given a manually created tree, represented as a json object, respond with the score information for the tree and each of its nodes.
	 * @param data
	 * @param request_
	 * @param response
	 * @throws Exception 
	 */
	private void scoreSaveManualTree(JsonNode data, HttpServletRequest request_, HttpServletResponse response) throws Exception {
		String command = data.get("command").asText(); //scoretree or savetree
		int prev_tree_id = -1;
		String dataset = data.get("dataset").asText();
		dataset = "metabric_with_clinical";//todo fix this so javascript and serverside agree about this..
		
		//To avoid penalizing user for genes added to his/her own tree.
		HttpSession session = request_.getSession();
		Player player = (Player) session.getAttribute("player");
		if(player!=null || command.equals("scoretree")){
			int PlayerId = -1;
			if(player!=null){
				PlayerId = player.getId();
			}
			Weka weka = name_dataset.get(dataset);	
			if(weka==null){
				handleBadRequest(request_, response, "no dataset loaded for dataset: "+dataset);
				return;
			}		
			//create the weka tree structure
			JsonTree t = new JsonTree();
			ManualTree readtree = new ManualTree();
			readtree = t.parseJsonTree(weka, data.get("treestruct"), dataset);
			List<String> entrez_ids = t.getEntrezIds(data.get("treestruct"), new ArrayList<String>());
			int numnodes = readtree.numNodes();
			//evaluate it on the data
			Evaluation eval = new Evaluation(weka.getTrain());
			eval.evaluateModel(readtree, weka.getTrain());
			HashMap distributionData = readtree.getDistributionData();
			ObjectNode result = mapper.createObjectNode();
			result.put("pct_correct", eval.pctCorrect());
			result.put("size", numnodes);
			double nov = Tree.getUniqueIdNovelty(entrez_ids, PlayerId);
			result.put("novelty", nov);//
			result.put("text_tree", readtree.toString());
			//serialize and return the result		
			JsonNode treenode = readtree.getJsontree();
			result.put("treestruct", treenode);
			response.setContentType("text/json");
			PrintWriter out = response.getWriter();
			String result_json = mapper.writeValueAsString(result);

			//now store it in the database
			String comment = "";
			int user_saved = 0;
			int privateflag = 0;
			comment = data.get("comment").asText();
			String ip = request_.getRemoteAddr();
			List<Feature> features = new ArrayList<Feature>();
			for(String entrez_id : entrez_ids){
				Feature f = weka.features.get(entrez_id);
				features.add(f);
			}
			if(command.equals("savetree")){
				user_saved = 1;
				prev_tree_id = data.get("previous_tree_id").asInt();
				privateflag = data.get("privateflag").asInt();
			}
			Tree tree = new Tree(0, PlayerId, ip, features, result_json,comment, user_saved, privateflag);
			int tid = tree.insert(prev_tree_id, privateflag);
			float score = 0; 
			score = (float) ((750 * (1 / numnodes)) + (500 * nov) + (1000 * eval.pctCorrect()));
			tree.insertScore(tid, dataset, (float)eval.pctCorrect(), (float)numnodes, (float)nov, score);
			ArrayList json_badges = new ArrayList();
			if(command.equals("savetree")){
				Badge _badge = new Badge();
				json_badges = _badge.getEarnedBadges(tid, PlayerId);
			}
			result.put("badges", mapper.valueToTree(json_badges));	
			result.put("tree_id", tid);
			//System.out.println(distributionData);
			result.put("distribution_data", mapper.valueToTree(distributionData));
			result_json = mapper.writeValueAsString(result);
			out.write(result_json);
			out.close();
		} else {
			PrintWriter out = response.getWriter();
			String result_json = "{message: 'Please login to save a tree.'}";
			out.write(result_json);
			out.close();
		}
		
	}

	private void evaluateForest(JsonNode data, HttpServletRequest request_, HttpServletResponse response) throws Exception {			
		String dataset = data.get("dataset").asText();
		Weka weka = name_dataset.get(dataset);
		if(weka==null){
			handleBadRequest(request_, response, "no dataset loaded for dataset: "+dataset);
			return;
		}
		JdbcConnection conn = new JdbcConnection();
		String outerQuery = "select json_tree from tree where user_saved=1";
		ResultSet outerRslt = conn.executeQuery(outerQuery);
		int size = 0;
		try{
			outerRslt.last();
			size = outerRslt.getRow();
			outerRslt.beforeFirst();
		} catch(SQLException e){
			e.printStackTrace();
		}
		Classifier[] classifiers = new Classifier[size];
		int i = 0;
		try {
			while(outerRslt.next()){
				JsonTree t = new JsonTree();
				ManualTree classifier = t.parseJsonTree(weka, outerRslt.getString("json_tree"), dataset);
				classifiers[i] = classifier;
				i++;
			}
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		metaExecution meta = weka.executeRandomForest(classifiers);
		String summary = "{\"Summary\":"+meta.toString()+"}";
		response.setContentType("text/json");
		PrintWriter out = response.getWriter();
		out.write(summary);
		out.close();
	}
	
	private void evaluateTreeorSMO(JsonNode data, HttpServletRequest request_, HttpServletResponse response) throws Exception {			
		String dataset = data.get("dataset").asText();
		String type = data.get("classifier").asText();
		Weka weka = name_dataset.get(dataset);
		if(weka==null){
			handleBadRequest(request_, response, "no dataset loaded for dataset: "+dataset);
			return;
		}
		JdbcConnection conn = new JdbcConnection();
		String outerQuery = "select DISTINCT feature.unique_id from tree_feature,tree,feature where tree.id = tree_feature.tree_id and tree_feature.feature_id = feature.id";
		ResultSet outerRslt = conn.executeQuery(outerQuery);
		Set<String> geneIds = new HashSet<String>();
		try {
			while(outerRslt.next()){
				geneIds.add(outerRslt.getString("unique_id"));
			}
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		weka.setEval_method("test_set");
		execution exec = weka.executeSMOorTree(geneIds, type, 10); 
		String summary = "{\"Summary\":"+exec.toString()+"}";
		response.setContentType("text/json");
		PrintWriter out = response.getWriter();
		out.write(summary);
		out.close();
	}

//	
	
	private void getClinicalFeatures(JsonNode data, HttpServletRequest request_, HttpServletResponse response) throws Exception {
		//String command = data.get("command").asText(); //get_clinical_features 
		String dataset = data.get("dataset").asText();
		dataset = "metabric_with_clinical";//todo fix this so javascript and serverside agree about this..
		ObjectNode features = null;
		if(dataset.equals("metabric_with_clinical")){
			features = Feature.getMetaBricClinicalFeatures(mapper);
			features.put("dataset", dataset);
		}
		String json_features = mapper.writeValueAsString(features);
		response.setContentType("text/json");
		PrintWriter out = response.getWriter();
		out.write(json_features);
		out.close();
	}
	
	private void getTreeList(JsonNode data, HttpServletRequest request_, HttpServletResponse response) throws Exception {
		String command = data.get("command").asText(); //get_trees_all, get_trees_ip, get_trees_user_id
		String ip = request_.getRemoteAddr();
		Tree tree_ = new Tree();
		List<Tree> trees = null;
		if(command.equals("get_trees_all")){
			trees = tree_.getAll(); 
		} else if(command.equals("get_trees_ip")){
			trees = tree_.getByIP(ip);
		} else if(command.equals("get_trees_user_id")){
			HttpSession session = request_.getSession();
			Player player = (Player) session.getAttribute("player");
			int user_id = data.get("user_id").asInt();
			boolean getPrivate = false;
			if(player.getId()==user_id){
				getPrivate = true;
			}
			tree_.setPlayer_id(user_id);
			trees = tree_.getForPlayer(user_id, getPrivate);
		} else if(command.equals("get_trees_with_range")) {
			String lowerLimit = data.get("lowerLimit").asText();
			String upperLimit = data.get("upperLimit").asText();
			String orderby = data.get("orderby").asText();
			trees = tree_.getWithLimit(lowerLimit,upperLimit, orderby);
		} else if(command.equals("get_tree_by_id")) {
			String tree_id = data.get("treeid").asText();
			trees = tree_.getById(tree_id);
		} else if(command.equals("get_trees_by_search")){
			String query = data.get("query").asText();
			trees = tree_.getBySearch(query);
		}
		ObjectNode treelist = tree_.getTreeListAsJson(trees, mapper);
		String json_trees = mapper.writeValueAsString(treelist);
		response.setContentType("text/json");
		PrintWriter out = response.getWriter();
		out.write(json_trees);
		out.close();
		
	}
	
	private void getRankofTree(int tree_id,HttpServletRequest request, HttpServletResponse response) throws IOException {
		Tree _tree = new Tree();
		int rank = _tree.get_rank(tree_id);
		String rank_json = "{\"rank\":"+rank+"}";
		response.setContentType("text/json");
		PrintWriter out = response.getWriter();
		out.write(rank_json);
		out.close();
	}

	private void saveHand(LinkedHashMap data, HttpServletRequest request, HttpServletResponse response) {
		Game game = new Game();
		game.setWin((Integer)data.get("win"));
		LinkedHashMap gdata = (LinkedHashMap)data.get("game");
		game.setP1_score((Integer)gdata.get("p1_score"));
		game.setP2_score((Integer)gdata.get("p2_score"));
		LinkedHashMap gmetadata = (LinkedHashMap)gdata.get("metadata");
		game.setGame_started(new Timestamp((Long)gmetadata.get("game_started")));
		game.setGame_finished(new Timestamp((Long)gmetadata.get("game_finished")));
		game.setSearch_term((String)gmetadata.get("search_term"));
		game.setBoard_id(Integer.parseInt((String)gmetadata.get("board_id")));
		game.setPlayer1_id(Integer.parseInt((String)gmetadata.get("player1_id")));
		game.setPlayer2_id(Integer.parseInt((String)gmetadata.get("player2_id")));
		game.setIp(request.getRemoteAddr());
		List<String> p1_features = new ArrayList<String>();
		List<LinkedHashMap> p1_hand = (List<LinkedHashMap>)gdata.get("p1_hand");
		for(LinkedHashMap obj : p1_hand){
			p1_features.add((String)obj.get("unique_id"));
		}
		List<String> p2_features = new ArrayList<String>();
		game.setPlayer1_features(p1_features);
		List<LinkedHashMap> p2_hand = (List<LinkedHashMap>)gdata.get("p2_hand");
		for(LinkedHashMap obj : p2_hand){
			p2_features.add((String)obj.get("unique_id"));
		}
		game.setPlayer2_features(p2_features);
		List<LinkedHashMap> cards = (List<LinkedHashMap>)gdata.get("cards");
		List<Game.ux> ux_list = new ArrayList<Game.ux>();
		for(LinkedHashMap card : cards){
			String uid = (String) card.get("unique_id");
			LinkedHashMap ux_meta = (LinkedHashMap)card.get("metadata");
			List<LinkedHashMap> uxes = (List<LinkedHashMap>)ux_meta.get("ux");
			for(LinkedHashMap uxe : uxes){
				long t = (Long) uxe.get("timestamp");
				String panel = (String) uxe.get("panel");
				boolean hover_board = (Boolean) uxe.get("board_hover");
				Game.ux ux = game.makeUx(uid, t, panel, hover_board);
				ux_list.add(ux);
			}
		}
		game.setFeature_ux(ux_list);
		//mouse
		List<LinkedHashMap> mice = (List<LinkedHashMap>)gmetadata.get("mouse_action");
		List<Game.mouse> mouses = new ArrayList<Game.mouse>();
		for(LinkedHashMap mouse : mice){
			long t = (Long)mouse.get("timestamp");
			int x = (Integer)mouse.get("x");
			int y = (Integer)mouse.get("y");
			Game.mouse m = game.makeMouse(t,x,y);
			mouses.add(m);
		}
		game.setMouse_actions(mouses);
		try {
			game.insert();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Every time a player clicks on a card to add it to their hand, record.
	 * @param data
	 * @param request
	 * @param response
	 */
	private void savePlayedCard(LinkedHashMap data, HttpServletRequest request, HttpServletResponse response) {
		String player_id = (String)data.get("player_id");
		Long timestamp = (Long)data.get("timestamp");
		long t = 0;
		if(timestamp!=null){
			t = timestamp;
		}
		String board_id =  (String)data.get("board_id");
		String unique_id = (String)data.get("unique_id");
		Integer display_loc_ = (Integer)data.get("display_loc");
		int display_loc = -1;
		if(display_loc_!=null){
			display_loc = display_loc_;
		}
		if(unique_id!=null){			
			Card tosave = new Card(player_id, board_id, unique_id, display_loc);
			if(t!=0){
				tosave.setTimestamp(new Timestamp(t));
			}
			tosave.insert();
		}
	}
	
	private void addBadge(JsonNode data, HttpServletRequest request, HttpServletResponse response) throws IOException{
		Badge _badge = new Badge();
		Map<String,String> map = new HashMap<String,String>();
		ObjectMapper mapper = new ObjectMapper();
		int levelid = data.get("level_id").asInt();
		String json = data.get("constraints").toString();
		String desc = data.get("description").toString();
		System.out.println(json);
		try {
			map = mapper.readValue(json, new TypeReference<HashMap<String,Object>>() {});
			_badge.insert(map, levelid, desc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void getBadges(JsonNode data, HttpServletRequest request, HttpServletResponse response) throws IOException{
		Badge _badge = new Badge();
		int userid = data.get("user_id").asInt();
		int recBadgesFlag = data.get("reccomendbadges").asInt();
		String json_badges = "";
		try{
			json_badges = _badge.getBadgesofUser(userid, recBadgesFlag);
		} catch (Exception e) {
			e.printStackTrace();
		}
		response.setContentType("text/json");
		PrintWriter out = response.getWriter();
		out.write(json_badges);
		out.close();
	}
	
	/**
	 * Respond with an error message if something went wrong
	 * @param request
	 * @param response
	 * @param problem
	 * @throws IOException
	 */
	private void handleBadRequest(HttpServletRequest request, HttpServletResponse response, String problem) throws IOException{
		String msg = "{\"error\":\""+problem+"\"}";
		System.out.println("Bad request:\n"+msg);
		response.setContentType("text/json");
		PrintWriter out = response.getWriter();
		out.write(msg);
		out.close();
	}

}
