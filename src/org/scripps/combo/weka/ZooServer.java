package org.scripps.combo.weka;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

import weka.classifiers.Classifier;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;

/**
 * Servlet implementation class WekaServer
 */
public class ZooServer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	Weka weka;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ZooServer() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void init(ServletConfig config){
		ServletContext context = config.getServletContext();
		InputStream train_loc = context.getResourceAsStream("/data/zoo_mammals.arff");
		weka = new Weka(train_loc, null);
		weka.setEval_method("training_set");

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
			String features=request.getParameter("features");
			if(features==null){
				handleBadRequest(request, response, "no features");
			}else{
				String model = request.getParameter("wekamodel");
				Classifier wekamodel = null;
				if(model!=null&&model.equals("jrip")){
					wekamodel = new JRip();
				}else{
					wekamodel = new J48();
				}
				Weka.execution result = weka.pruneAndExecute(features, wekamodel);
				ClassifierEvaluation short_result = new ClassifierEvaluation((int)result.eval.pctCorrect(), result.model.getClassifier().toString());
				//serialize and return the result
				JSONObject r = new JSONObject(short_result);
				response.setContentType("text/json");
				PrintWriter out = response.getWriter();
				out.write(r.toString());
				out.close();
			}
			// initialize a random gene 'board' - a list of attributes from the training set of specified size
		}else if(command.equals("getboard")){
			String raninput = request.getParameter("ran");
			int ran = 1;
			if(raninput!=null&&!raninput.equals("0")){
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
				if(board.equals("zoo1_l0")){
					cards = weka.getCardsByIndices("1,10");
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
			if(game!=null&&game.equals("barney_zoo")){
				//update stars
				//Player player = Player.lookupPlayer(player_name);
				HttpSession s = request.getSession();
				Player player = (Player)s.getAttribute("player");
				//check if they passed the level
				String win = request.getParameter("win");
				if(win!=null&&win.equals("1")){
					//update session
					List<Integer> mammal_scores = player.getLevel_tilescores().get("mammals");
					if(mammal_scores==null){
						mammal_scores = new ArrayList<Integer>(20);
						mammal_scores.add(0);
					}
					if(board_id>=mammal_scores.size()){
						for(int m=mammal_scores.size()-1; m<=board_id; m++){
							mammal_scores.add(0);
						}
					}
					mammal_scores.set(board_id, cv_accuracy);
					player.getLevel_tilescores().put("mammals", mammal_scores);
					s.setAttribute("player", player);
				}
			}

			System.out.println("saved a hand "+player_name+" "+score);
		}
	}

	public void handleBadRequest(HttpServletRequest request, HttpServletResponse response, String problem){
		System.out.println("Bad request: "+problem);
	}

}
