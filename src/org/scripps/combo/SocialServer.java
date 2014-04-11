package org.scripps.combo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.scripps.combo.model.Game;
import org.scripps.combo.model.Player;
import org.scripps.util.Mail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


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
			String dataset = request.getParameter("dataset");
			GameLog log = new GameLog();
			boolean winning_only = true; boolean keep_mammal = true;
			List<Game> hands = Game.getTheFirstGamePerPlayerPerBoard(winning_only, dataset, true, null, keep_mammal);
			GameLog.high_score sb = log.getScoreBoard(hands, dataset);
			String json = log.getD3CompatibleJson(sb);
			response.setContentType("text/json");
			PrintWriter out = response.getWriter();
			out.write(json);
			out.close();
		}else if(command.equals("boardroom")){
			Boardroom b = new Boardroom();
			int user_id = Integer.parseInt(request.getParameter("user_id"));
			String room = request.getParameter("room");
			String dataset = "griffith_breast_cancer_1";//request.getParameter("dataset"); //"dream_breast_cancer"
			if(dataset.equals("griffith_breast_cancer_1")){
				room = "edu1";//"5";
			}
			b.buildBoardView(user_id, dataset, room);
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
		ObjectMapper mapper = new ObjectMapper();
		String t = request.getContentType();
		HttpSession session = request.getSession();
		//System.out.println("content type "+t);
		if(t!=null&&t.startsWith("application/json")){
			String mainJson = extractJson(request);
			if(mainJson!=null){
				LinkedHashMap postData = mapper.readValue(mainJson, LinkedHashMap.class);	
				String command = null;
				if(postData!=null){
					Object command_ = postData.get("command");
					command = (String)command_;
				}
				JsonNode data = mapper.readTree(mainJson);	
		if(command==null){
			handleBadRequest(request, response, "no command");
			return;
		}
		if(command.contains("user_")){
			ObjectNode result = mapper.createObjectNode();			
			try{
				if(command.equals("user_login")){
					String username = data.get("username").asText();
					String password = data.get("password").asText();
					Player player = new Player();
					player.setName(username);
					player.setPassword(password);
					if (username == null || password == null) {
						response.setContentType("application/json");
						PrintWriter out = response.getWriter();
						result.put("success", false);
						result.put("message", "Make sure all fields are completed.");
						String json = mapper.writeValueAsString(result);
						out.write(json);
						out.close();  
					} else {
						boolean success = true;
						boolean passwordfailed = false;
							player = player.lookupByUserPassword();
							if(player==null){
								passwordfailed = true;
								success = false;
							}
						if(success){
							response.setStatus(200);
							response.setContentType("application/json");
							PrintWriter out = response.getWriter();
							result.put("success", true);
							result.put("player_name", player.getName());
							result.put("player_id", player.getId());
							String json = mapper.writeValueAsString(result);
							out.write(json);
							out.close();
							session.setAttribute("username", username);
						    session.setAttribute("player", player);
						} else if(passwordfailed) {
							response.setContentType("application/json");
							PrintWriter out = response.getWriter();
							result.put("success", false);
							result.put("message", "Username and Password do not match");
							String json = mapper.writeValueAsString(result);
							out.write(json);
							out.close();
						}
					}
				} else if(command.equals("user_signup")){
					String username = data.get("username").asText();
					String password = data.get("password").asText();
					String email = data.get("email").asText();
					Player player = new Player();
					player.setName(username);
					player.setPassword(password);
					player.setEmail(email);
					
					if (username == null || password == null || email == null) {
						response.setContentType("application/json");
						PrintWriter out = response.getWriter();
						result.put("success", false);
						result.put("message", "Make sure all fields are completed.");
						String json = mapper.writeValueAsString(result);
						out.write(json);
						out.close();  
					} else {
						//validate
						boolean success = true;
						boolean nametaken = false;
						//try to create a new user 
						player = Player.createCondensed(username, password, email); 
						//returns null if the user name is taken
						if(player==null){
							nametaken = true;
							success = false;
						}
						if(success){
							response.setContentType("application/json");
							PrintWriter out = response.getWriter();
							result.put("success", true);
							result.put("player_name", player.getName());
							result.put("player_id", player.getId());
							session.setAttribute("username", username);
						    session.setAttribute("player", player);
							String json = mapper.writeValueAsString(result);
							out.write(json);
							out.close();
						} else if(nametaken) {
							response.setContentType("application/json");
							PrintWriter out = response.getWriter();
							result.put("success", false);
							result.put("message", "Username has already been taken.");
							String json = mapper.writeValueAsString(result);
							out.write(json);
							out.close();  
						}
					}
				} else if(command.equals("user_ref_login")){
					String username = data.get("username").asText();
					String token = data.get("token").asText();
					Player player = new Player();
					player.setName(username);
					player.setToken(token);
					if (username == null || token == null) {
						response.setContentType("application/json");
						PrintWriter out = response.getWriter();
						result.put("success", false);
						result.put("message", "All parameters not sent.");
						String json = mapper.writeValueAsString(result);
						out.write(json);
						out.close();  
					} else {
						boolean success = true;
							player = player.findOrCreateWithToken(token, username);
							if(player==null){
								success = false;
							}
						if(success){
							response.setStatus(200);
							response.setContentType("application/json");
							PrintWriter out = response.getWriter();
							result.put("success", true);
							result.put("player_name", player.getName());
							result.put("player_id", player.getId());
							String json = mapper.writeValueAsString(result);
							out.write(json);
							out.close();
							session.setAttribute("username", username);
						    session.setAttribute("player", player);
						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				handleBadRequest(request, response, "Failed to validate user");
			}	
		}
			}
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
	public void handleBadRequest(HttpServletRequest request, HttpServletResponse response, String problem){
		System.out.println("Bad request: "+problem);
	}

}
