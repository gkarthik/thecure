package org.scripps.combo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.scripps.combo.model.Hand;
import org.scripps.combo.model.Player;
import org.scripps.util.Mail;


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
		//load up a mail client
		mail = new Mail("/props/EmailCredentials.properties");

	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
			boolean winning_only = true;
			List<Hand> hands = Hand.getTheFirstHandPerPlayerPerBoard(winning_only);
			GameLog.high_score sb = log.getScoreBoard(hands);
			String json = log.getD3CompatibleJson(sb);
			response.setContentType("text/json");
			PrintWriter out = response.getWriter();
			out.write(json);
			out.close();
		}else if(command.equals("boardroom")){
			Boardroom b = new Boardroom();
			int user_id = Integer.parseInt(request.getParameter("user_id"));
			String phenotype = "dream_breast_cancer";//request.getParameter("dataset"); //"dream_breast_cancer"
			String room = request.getParameter("room");
			b.buildBoardView(user_id, phenotype, room);
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

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	public void handleBadRequest(HttpServletRequest request, HttpServletResponse response, String problem){
		System.out.println("Bad request: "+problem);
	}

}
