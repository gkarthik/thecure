package org.scripps.combo.weka;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
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
import org.scripps.combo.Hand;
import org.scripps.combo.Player;
import org.scripps.combo.weka.Weka.card;
import org.scripps.combo.weka.Weka.execution;
import org.scripps.combo.weka.viz.JsonTree;
import org.scripps.util.Mail;

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
			Weka mammal_weka = new Weka(train_loc);
			mammal_weka.setEval_method("training_set");
			name_dataset.put("mammal", mammal_weka);
			train_loc.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			InputStream train_loc = context.getResourceAsStream("/WEB-INF/data/zoo.arff");
			Weka zoo_weka = new Weka(train_loc);
			zoo_weka.setEval_method("training_set");
			name_dataset.put("zoo", zoo_weka);

			train_loc.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//breast cancer
		//vantveer data
		try {
			InputStream train_loc = context.getResourceAsStream("/WEB-INF/data/vantveer/breastCancer-train-filtered.arff");
			Weka vantveer_weka = new Weka(train_loc);
			vantveer_weka.loadMetadata(context.getResourceAsStream("/WEB-INF/data/vantveer/breastCancer-train_meta.txt"));
			name_dataset.put("vantveer", vantveer_weka);
			train_loc.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//griffith data
		try {
			InputStream train_loc = context.getResourceAsStream("/WEB-INF/data/griffith/full_filtered_train.arff");
			Weka griffith_weka = new Weka(train_loc);
			griffith_weka.loadMetadata(context.getResourceAsStream("/WEB-INF/data/griffith/griffith_meta.txt"));
			name_dataset.put("griffith_full_filtered", griffith_weka);
			train_loc.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Cunningham data	
		try {
			InputStream train_loc = context.getResourceAsStream("/WEB-INF/data/cranio/craniosynostosis_coronal_control.arff");
			Weka coronal_control_weka = new Weka(train_loc);
			coronal_control_weka.loadMetadata(context.getResourceAsStream("/WEB-INF/data/cranio/craniosynostosis_1_meta.txt"));
			name_dataset.put("coronal_case_control", coronal_control_weka);	
			train_loc.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
/*
		try {
			InputStream train_loc = context.getResourceAsStream("/WEB-INF/data/cranio/craniosynostosis_case_control.arff");
			Weka cranio_case_weka = new Weka(train_loc);
			cranio_case_weka.loadMetadata(context.getResourceAsStream("/WEB-INF/data/cranio/craniosynostosis_1_meta.txt"));
			name_dataset.put("cranio_case_control", cranio_case_weka);	
			train_loc.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			InputStream train_loc = context.getResourceAsStream("/WEB-INF/data/cranio/craniosynostosis_metopic_control.arff");
			Weka metopic_control_weka = new Weka(train_loc);
			metopic_control_weka.loadMetadata(context.getResourceAsStream("/WEB-INF/data/cranio/craniosynostosis_1_meta.txt"));
			name_dataset.put("metopic_case_control", metopic_control_weka);
			train_loc.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			InputStream train_loc = context.getResourceAsStream("/WEB-INF/data/cranio/craniosynostosis_sagittal_control.arff");
			Weka sagittal_control_weka = new Weka(train_loc);
			sagittal_control_weka.loadMetadata(context.getResourceAsStream("/WEB-INF/data/cranio/craniosynostosis_1_meta.txt"));
			name_dataset.put("sagittal_case_control", sagittal_control_weka);	
			train_loc.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/
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
		//System.out.println("hello wekaserver");
		String command = request.getParameter("command");
		if(command==null){
			handleBadRequest(request, response, "no command");
			return;
		}
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
		String game = request.getParameter("game");
		// handle request to score feature set
		if(command.equals("getscore")){
			String features=request.getParameter("features");
			if(features==null){
				handleBadRequest(request, response, "no features");
			}else{
				//String model = request.getParameter("wekamodel");
				J48 wekamodel = new J48();
				Weka.execution result = weka.pruneAndExecute(features, wekamodel);
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
			// initialize a random gene 'board' - a list of attributes from the training set of specified size
		}else if(command.equals("getboard")){
			String raninput = request.getParameter("ran");
			int ran = 1;
			if(raninput!=null){
				ran = Integer.parseInt(raninput);
			}else{			
				ran = (int)Math.rint(Math.random()*1000);
			}
			int nrows = Integer.parseInt(request.getParameter("y"));
			int ncols = Integer.parseInt(request.getParameter("x"));
			List<Weka.card> cards = weka.getRandomCards(nrows * ncols, ran);
			JSONArray r = new JSONArray((Collection<Weka.card>)cards);
			response.setContentType("text/json");
			PrintWriter out = response.getWriter();
			out.write(r.toString());
			out.close();
		}else if(command.equals("getspecificboard")){
			String board = request.getParameter("board");
			List<Weka.card> cards = new ArrayList<Weka.card>();
			if(board!=null){
				if(board.equals("mammal_0")){
					cards = weka.getCardsByIndices("1,10");
				}else if(board.equals("mammal_1")){
					cards = weka.getCardsByIndices("3,9");
				}else if(board.equals("mammal_2")){
					cards = weka.getCardsByIndices("9,7,6,14");
				}else if(board.equals("mammal_3")){
					cards = weka.getCardsByIndices("4,3,1,13");
				}
				/*}else if(board.equals("mammal_3")){
					cards = weka.getCardsByIndices("8,11");
				}else if(board.equals("mammal_4")){
					cards = weka.getCardsByIndices("2,16,1,12");
				}*/else if(board.equals("mammal_5")){
					cards = weka.getCardsByIndices("9,7,6,14");
				}else if(board.equals("mammal_6")){
					cards = weka.getCardsByIndices("6,11,2,15");
				}else if(board.equals("mammal_7")){
					cards = weka.getCardsByIndices("4,3,1,13");
				}else if(board.equals("zoo_0")){
					cards = weka.getCardsByIndices("4,3,1,13,6,11,2,15,9");
				}else if(board.equals("zoo_1")){
					cards = weka.getCardsByIndices("5,4,2,14,7,12,3,16,10");
				}else if(board.equals("zoo_2")){
					cards = weka.getCardsByIndices("3,2,16,12,5,10,1,14,8");
				}else if(board.equals("zoo_3")){
					cards = weka.getCardsByIndices("2,1,15,11,4,9,16,14,7");
				}else if(board.equals("zoo_4")){
					cards = weka.getCardsByIndices("1,16,14,10,3,8,15,13,6");
				}else if(board.equals("zoo_5")){
					cards = weka.getCardsByIndices("15,1,12,9,2,7,14,11,8");
				}else if(board.equals("zoo_6")){
					cards = weka.getCardsByIndices("14,16,11,8,1,6,13,10,7");
				}else if(board.equals("zoo_7")){
					cards = weka.getCardsByIndices("13,15,10,7,16,5,12,9,6");
				}else if(board.equals("zoo_8")){
					cards = weka.getCardsByIndices("12,16,9,6,15,4,11,8,5e of");
				}
			}else{
				int nrows = Integer.parseInt(request.getParameter("y"));
				int ncols = Integer.parseInt(request.getParameter("x"));
				cards = weka.getRandomCards(nrows * ncols, 0);
			}
			JSONArray r = new JSONArray((Collection<Weka.card>)cards);
			response.setContentType("text/json");
			PrintWriter out = response.getWriter();
			out.write(r.toString());
			out.close();
		}
		else if(command.equals("savehand")){
			String player_name = request.getParameter("player_name");
			String ip = request.getRemoteAddr();
			String features = request.getParameter("features");
			String feature_names = request.getParameter("feature_names");
			String phenotype = dataset_name;
			String score_s = request.getParameter("score");
			int score = -1;
			if(score_s!=null){
				score = Integer.parseInt(score_s);
			}
			String cv_accuracy_s = request.getParameter("cv_accuracy");
			String training_accuracy_s = request.getParameter("training_accuracy");
			int training_accuracy = -1;
			int cv_accuracy = -1000;
			if(cv_accuracy_s!=null){
				cv_accuracy = Integer.parseInt(cv_accuracy_s);
			}
			if(training_accuracy_s != null){
				training_accuracy = Integer.parseInt(training_accuracy_s);
			}
			String board_id_s = request.getParameter("board_id");
			int board_id = -1000;
			if(board_id_s!=null){
				board_id = Integer.parseInt(board_id_s);
			}
			String win = request.getParameter("win");
			int win_ = 0;
			if(win!=null&&win.equals("1")){
				win_ = 1;
			}
			Hand hand = new Hand();
			hand.setBoard_id(board_id);
			hand.setCv_accuracy(cv_accuracy);
			hand.setFeatures(features);
			hand.setIp(ip);
			hand.setPlayer_name(player_name);
			hand.setScore(score);
			hand.setPhenotype(phenotype);
			hand.setFeature_names(feature_names);
			hand.setTraining_accuracy(training_accuracy);
			hand.setGame_type(game);
			hand.setWin(win_);
			hand.save();
			//update player info
	
			if(game!=null&&(game.equals("training_verse_barney")||game.equals("verse_barney"))){
				//update stars
				//Player player = Player.lookupPlayer(player_name);
				HttpSession s = request.getSession();
				Player player = (Player)s.getAttribute("player");
				//check if they passed the level
				if(win!=null&&win.equals("1")){
					//update session
					List<Integer> scores = player.getLevel_tilescores().get(dataset_name);
					if(scores==null){
						scores = new ArrayList<Integer>(20);
						scores.add(0);
					}
					if(board_id>=scores.size()){
						for(int m=scores.size()-1; m<=board_id; m++){
							scores.add(0);
						}
					}
					if(game.equals("verse_barney")){
						scores.set(board_id, cv_accuracy);
					}else{
						scores.set(board_id, training_accuracy);
					}
					player.getLevel_tilescores().put(dataset_name, scores);
					s.setAttribute("player", player);
				}
			}else if(game!=null&&game.equals("barney")){
				//update stars
				Player player = Player.lookupPlayer(player_name);
				//check if they passed the level
				if(win!=null&&win.equals("1")){
					//if(score>0){
					int stars = 1;
					if(player.getBarney_levels()!=null&&player.getBarney_levels().size()>board_id){
						stars += player.getBarney_levels().get(board_id);
						player.getBarney_levels().set(board_id, stars);
					}else{
						player.getBarney_levels().add(stars);
					}
					player.updateBarneyLevelsInDatabase();
				}

			}

			System.out.println("saved a hand "+player_name+" "+score);
		}
	}

	public void handleBadRequest(HttpServletRequest request, HttpServletResponse response, String problem){
		System.out.println("Bad request: "+problem);
	}

}
