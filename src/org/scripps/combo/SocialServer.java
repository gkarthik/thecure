package org.scripps.combo;

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
public class SocialServer extends HttpServlet {
	private static final long serialVersionUID = 1L;

	Mail mail; 
	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SocialServer() {
		super();
	}

	public void init(ServletConfig config){		
		//ServletContext context = config.getServletContext();	
		//load up a mail client
		mail = new Mail("/props/EmailCredentials.properties");
//		String messageText ="hello one more time again ben"; String subject = "testing 123"; String addrFrom = "bgood@scripps.edu"; String nameFrom = "Ben Good";
//		String nameto = "Benjamin"; String addrto = "ben.mcgee.good@gmail.com";
//		m.sendMail(messageText, subject, addrFrom, nameFrom, nameto, addrto);
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
		String command = request.getParameter("command");
		if(command==null){
			handleBadRequest(request, response, "no command");
			return;
		}
		if(command.equals("invite")){
			String invited = request.getParameter("invited");
			String by = request.getParameter("by");
			String message = by+" has invited "+invited+" to play the cure.";
			mail.sendMail(message, "Invite", "bgood@scripps.edu", "Ben", "Ben", "ben.mcgee.good@gmail.com");
			response.sendRedirect("/cure/");
		}else if(command.equals("gamelogs")){
			GameLog log = new GameLog();
			GameLog.high_score sb = log.getScoreBoard();
			String json = log.getD3CompatibleJson(sb);
			response.setContentType("text/json");
			PrintWriter out = response.getWriter();
			out.write(json);
			out.close();
		}else if(command.equals("boardroom")){
			Boardroom b = new Boardroom();
			String username = request.getParameter("username");
			String phenotype = request.getParameter("phenotype"); //"dream_breast_cancer"
			b.buildBoardView(username, phenotype);
			String json = b.renderjsonBoardViews();
			response.setContentType("text/json");
			PrintWriter out = response.getWriter();
			out.write(json);
			out.close();
		}else if(command.equals("iforgot")){
			String email = request.getParameter("mail");
			System.out.println("password requested for "+email);
			List<Player> players = Player.lookupByEmail(email);
			String message = "";
			if(players==null||players.size()==0){
				message = "A password request has been issued by The Cure Game for this email address, but no account was found.  Please go to http://genegames.org/cure/login.jsp to create a new account.";
			}else{
				message = "Your user/password for The Cure Game is: \n";
				for(Player player : players){
					message += player.getName()+"/"+player.getPassword()+" \n";
				}
				message += "Please go to http://genegames.org/cure/login.jsp to play.";
			}
			
			mail.sendMail(message, "The Cure Game password", "bgood@scripps.edu", "The Cure Game", "", email);
			response.sendRedirect("/cure/");
		}
	}

	public void handleBadRequest(HttpServletRequest request, HttpServletResponse response, String problem){
		System.out.println("Bad request: "+problem);
	}

}
