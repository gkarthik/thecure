package org.scripps.combo.weka;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

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
import org.scripps.combo.model.Board;
import org.scripps.combo.model.Card;
import org.scripps.combo.model.Feature;
import org.scripps.combo.model.Hand;
import org.scripps.combo.model.Player;
import org.scripps.combo.weka.Weka.execution;
import org.scripps.combo.weka.viz.JsonTree;
import org.scripps.util.Mail;
import org.scripps.util.MapFun;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ReliefFAttributeEval;
import weka.classifiers.Classifier;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;
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

	public void init(ServletConfig config){		
		//load all active datasets
		ServletContext context = config.getServletContext();

		//training game data 
		try { 
			InputStream train_loc = context.getResourceAsStream("/WEB-INF/data/zoo_mammals.arff");
			Weka mammal_weka = new Weka();
			String dataset = "mammal";
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
		//dream data
		try {
			String dataset = "dream_breast_cancer";
			InputStream train_loc = context.getResourceAsStream("/WEB-INF/data/dream/Exprs_CNV_2500genes.arff");
			Weka dream_weka = new Weka();
			dream_weka.buildWeka(train_loc, null, dataset);			
			name_dataset.put("dream_breast_cancer", dream_weka);	
			train_loc.close();
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
		System.out.println("content type "+t);
		if(t!=null&&t.startsWith("application/json")){
			String json = extractJson(request);
			if(json!=null){
				System.out.println(json);
				LinkedHashMap postData = mapper.readValue(json, LinkedHashMap.class);	
				String command = null;
				if(postData!=null){
					Object command_ = postData.get("command");
					if(command_!=null){
						command = (String)command_;
						routePost(command, postData, request, response);
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


/**
 * Send the json request to the appropriate handler based on the command parameter	
 * @param command
 * @param postData
 * @param request
 * @param response
 * @throws IOException
 */
	private void routePost(String command, LinkedHashMap postData, HttpServletRequest request, HttpServletResponse response) throws IOException {
		//route to appropriate functions
		if(command.equals("getscore")){
			//does not work
			getScore(postData, request, response);
		}else if(command.equals("savehand")){
			//does not work
			saveHand(postData, request, response);
		}else if(command.equals("saveplayedcard")){
			savePlayedCard(postData, request, response);
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
	
		J48 wekamodel = new J48();
		Weka.execution result = weka.pruneAndExecuteWithFeatureIds(unique_ids, wekamodel);
		ClassifierEvaluation short_result = new ClassifierEvaluation((int)result.eval.pctCorrect(), result.model.getClassifier().toString());
		//serialize and return the result
		JSONObject r = new JSONObject(short_result);
		response.setContentType("text/json");
		PrintWriter out = response.getWriter();
		String eval_json = r.toString();
		String tree_json = "";
		JsonTree jtree = new JsonTree();
		try {
			tree_json = jtree.getJsonTreeString(wekamodel);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String treeoutput = "{\"evaluation\" : "+eval_json+", " +
		"\"max_depth\":\""+jtree.getMax_depth()+"\"," +
		"\"num_leaves\":\""+jtree.getNum_leaves()+"\"," +
		"\"tree_size\":\""+jtree.getTree_size()+"\"," +		
		"\"tree\":"+tree_json+"}";
		//		System.out.println(treeoutput);
		out.write(treeoutput);
		out.close();

	}


	private void saveHand(LinkedHashMap data, HttpServletRequest request, HttpServletResponse response) {
		//		String features=request.getParameter("features");
		//		String geneids=request.getParameter("geneids");
		//		if(features==null&&geneids==null){
		//			handleBadRequest(request, response, "no features");
		//		}else if(features==null){
		//			features = "";
		//			String[] gids = geneids.split(",");
		//			for(String geneid : gids){
		//				List<card> cards = weka.getGeneid_cards().get(geneid);
		//				if(cards!=null){
		//					for(card c : cards){
		//						features+=c.getAtt_index()+",";
		//					}
		//				}
		//			}
		//		}			
		//		String player_name = request.getParameter("player_name");
		//		String ip = request.getRemoteAddr();
		//		String feature_names = request.getParameter("feature_names");
		//		String phenotype = dataset_name;
		//		String score_s = request.getParameter("score");
		//		int score = -1;
		//		if(score_s!=null){
		//			score = Integer.parseInt(score_s);
		//		}
		//		String cv_accuracy_s = request.getParameter("cv_accuracy");
		//		String training_accuracy_s = request.getParameter("training_accuracy");
		//		int training_accuracy = -1;
		//		int cv_accuracy = -1000;
		//		if(cv_accuracy_s!=null){
		//			cv_accuracy = Integer.parseInt(cv_accuracy_s);
		//		}
		//		if(training_accuracy_s != null){
		//			training_accuracy = Integer.parseInt(training_accuracy_s);
		//		}
		//		String board_id_s = request.getParameter("board_id");
		//		int board_id = -1000;
		//		if(board_id_s!=null){
		//			board_id = Integer.parseInt(board_id_s);
		//		}
		//		String win = request.getParameter("win");
		//		int win_ = 0;
		//		if(win!=null&&win.equals("1")){
		//			win_ = 1;
		//		}
		//		Hand hand = new Hand();
		//		hand.setBoard_id(board_id);
		//		hand.setCv_accuracy(cv_accuracy);
		//		hand.setFeatures(features);
		//		hand.setIp(ip);
		//		hand.setPlayer_name(player_name);
		//		hand.setScore(score);
		//		hand.setPhenotype(phenotype);
		//		hand.setFeature_names(feature_names);
		//		hand.setTraining_accuracy(training_accuracy);
		//		hand.setGame_type(game);
		//		hand.setWin(win_);
		//		hand.save();
		//		//update player info
		//
		//		if(game!=null&&(game.equals("training_verse_barney")||game.equals("verse_barney"))){
		//			//update stars
		//			//Player player = Player.lookupPlayer(player_name);
		//			HttpSession s = request.getSession();
		//			Player player = (Player)s.getAttribute("player");
		//			//check if they passed the level
		//			if(win!=null&&win.equals("1")){
		//				//update session
		//				Map<Integer,Integer> scores = player.getPhenotype_board_scores().get(dataset_name);
		//				if(scores==null){
		//					scores = new HashMap<Integer,Integer>();
		//				}
		//				if(game.equals("verse_barney")){
		//					scores.put(board_id, cv_accuracy);
		//				}else{
		//					scores.put(board_id, training_accuracy);
		//				}
		//				player.getPhenotype_board_scores().put(dataset_name, scores);
		//				s.setAttribute("player", player);
		//			}
		//		}else if(game!=null&&game.equals("barney")){
		//			//update stars
		//			Player player = Player.lookupPlayer(player_name);
		//			//check if they passed the level
		//			if(win!=null&&win.equals("1")){
		//				//if(score>0){
		//				int stars = 1;
		//				if(player.getBarney_levels()!=null&&player.getBarney_levels().size()>board_id){
		//					stars += player.getBarney_levels().get(board_id);
		//					player.getBarney_levels().set(board_id, stars);
		//				}else{
		//					player.getBarney_levels().add(stars);
		//				}
		//				player.updateBarneyLevelsInDatabase();
		//			}
		//
		//		}
		//		System.out.println("saved a hand "+player_name+" "+score);

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
