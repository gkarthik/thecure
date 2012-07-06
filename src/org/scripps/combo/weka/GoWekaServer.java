package org.scripps.combo.weka;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.scripps.combo.Hand;
import org.scripps.combo.Player;

import weka.classifiers.Classifier;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;

public class GoWekaServer extends HttpServlet{
	private static final long serialVersionUID = 1L;
	GoWeka goweka;
	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GoWekaServer() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void init(ServletConfig config){
		ServletContext context = config.getServletContext();
		InputStream train_loc = context.getResourceAsStream("/WEB-INF/data/vantveer/breastCancer-train-filtered.arff");
		InputStream test_loc = context.getResourceAsStream("/WEB-INF/data/vantveer/breastCancer-test.arff");
		InputStream meta_loc = context.getResourceAsStream("/WEB-INF/data/vantveer/breastCancer-train_meta.txt");
		InputStream anno_loc = context.getResourceAsStream("/WEB-INF/data/go2gene_3_51.txt");
		try {
			goweka = new GoWeka(train_loc, test_loc, meta_loc, anno_loc);
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
		//System.out.println("hello wekaserver");
		String command = request.getParameter("command");
		if(command==null){
			handleBadRequest(request, response, "no command");
			return;
		}
		// handle request to score feature set
		if(command.equals("getscore")){
			String accs=request.getParameter("accs");
			if(accs==null){
				handleBadRequest(request, response, "no accs");
			}else{
				String model = request.getParameter("wekamodel");
				Classifier wekamodel = null;
				if(model!=null&&model.equals("jrip")){
					wekamodel = new JRip();
				}else{
					wekamodel = new J48();
				}
				String[] acc_ = accs.split(",");
				Set<String> acc_set = new HashSet<String>();
				for(String acc : acc_){
					acc_set.add(acc);
				}
				Weka.execution result = goweka.limitByGoSetAndExecute(acc_set, wekamodel);
				ClassifierEvaluation short_result = new ClassifierEvaluation(0, "no classifier could be constructed");
				if(result!=null){
					short_result = new ClassifierEvaluation((int)result.eval.pctCorrect(), result.model.getClassifier().toString());
				}
				
				//serialize and return the result
				JSONObject r = new JSONObject(short_result);
				response.setContentType("text/json");
				PrintWriter out = response.getWriter();
				out.write(r.toString());
				out.close();
			}
			// initialize a random GO 'board' - a list of attributes from the training set of specified size	
		}else if(command.equals("getgoboard")){
			String raninput = request.getParameter("ran");
			int ran = 1;
			if(raninput!=null&&!raninput.equals("0")){
				ran = Integer.parseInt(raninput);
			}else{			
				ran = (int)Math.rint(Math.random()*1000);
			}
			int nrows = Integer.parseInt(request.getParameter("y"));
			int ncols = Integer.parseInt(request.getParameter("x"));
			List<GoWeka.card> cards = goweka.getRandomGoCards(nrows * ncols, ran);
			JSONArray r = new JSONArray((Collection<GoWeka.card>)cards);
			response.setContentType("text/json");
			PrintWriter out = response.getWriter();
			out.write(r.toString());
			out.close();
		}else if(command.equals("savehand")){
			String player_name = request.getParameter("player_name");
			String ip = request.getRemoteAddr();
			String features = request.getParameter("accs");
			String score_s = request.getParameter("score");
			int score = -1000;
			if(score_s!=null){
				score = Integer.parseInt(score_s);
			}
			String cv_accuracy_s = request.getParameter("cv_accuracy");
			int cv_accuracy = -1000;
			if(cv_accuracy_s!=null){
				cv_accuracy = Integer.parseInt(cv_accuracy_s);
			}
			String board_id_s = request.getParameter("board_id");
			int board_id = -1000;
			if(board_id_s!=null){
				board_id = Integer.parseInt(board_id_s);
			}
			Hand hand = new Hand();
			hand.setBoard_id(board_id);
			hand.setCv_accuracy(cv_accuracy);
			hand.setFeatures(features);
			hand.setIp(ip);
			hand.setPlayer_name(player_name);
			hand.setScore(score);
			hand.save();
			//update player info
			String game = request.getParameter("game");
			if(game!=null&&game.equals("barney")){
				//update stars
				Player player = Player.lookupPlayer(player_name);
				//check if they passed the level
				String win = request.getParameter("win");
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
