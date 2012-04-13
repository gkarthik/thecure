package org.scripps.combo.weka;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.scripps.combo.weka.Weka.card;
import org.scripps.combo.weka.Weka.execution;

import weka.classifiers.Classifier;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;

/**
 * Servlet implementation class WekaServer
 */
public class WekaServer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	Weka weka;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public WekaServer() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void init(ServletConfig config){
		weka = new Weka();
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
			// initialize a random 'board' - a list of attributes from the training set of specified size	
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
		}
	}

	public void handleBadRequest(HttpServletRequest request, HttpServletResponse response, String problem){
		System.out.println("Bad request: "+problem);
	}

}
