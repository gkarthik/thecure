package org.scripps.combo.weka;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public MetaServer() {
		super();
		name_dataset = new HashMap<String, Weka>();
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
		doPost(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//make sure we have a command and a dataset
		String command = request.getParameter("command");
		if(command==null){
			handleBadRequest(request, response, "no command");
			return;
		}
		//route to appropriate functions
		if(command.equals("getscore")){
			//works
			getScore(request, response);
		}else if(command.equals("getboard")){
			//works
			getBoard(request, response);
		}else if(command.equals("savehand")){
			//does not work
			saveHand(request, response);
		}else if(command.equals("playedcard")){
			//does not work
			playedCard(request, response);
		}
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
	private void getScore(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String dataset_name = request.getParameter("dataset");
		if(dataset_name==null){
			handleBadRequest(request, response, "no dataset");
			return;
		}
		Weka weka = name_dataset.get(dataset_name);
		if(weka==null){
			handleBadRequest(request, response, "no dataset loaded for name: "+dataset_name);
			return;
		}		
		String unique_ids = request.getParameter("unique_ids");
		//TODO parse them out of json array unique_ids
		List<String> uniques = MapFun.string2list(unique_ids, ",");
		J48 wekamodel = new J48();
		Weka.execution result = weka.pruneAndExecuteWithFeatureIds(uniques, wekamodel);
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
	

	private void saveHand(HttpServletRequest request, HttpServletResponse response) {
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

	private void playedCard(HttpServletRequest request, HttpServletResponse response) {
		String player_id = request.getParameter("player_id");
//		String unique_id = request.getParameter("unique_id");	
		String cardjson = request.getParameter("card");
		String board_id = request.getParameter("board_id");
		int display_loc = Integer.parseInt(request.getParameter("display_loc"));
		//todo use their time.
		String timestamp = request.getParameter("timestamp");
//		if(unique_id!=null){			
//			Card tosave = new Card(player_id, board_id, unique_id, display_loc);
//			tosave.insert();
//		}

	}

	public void handleBadRequest(HttpServletRequest request, HttpServletResponse response, String problem){
		System.out.println("Bad request: "+problem);
	}

}
