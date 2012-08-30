<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
	<%@ page import="org.scripps.combo.Player"%>
<%

String username = request.getParameter("username");
String password = request.getParameter("password");
String newuser = request.getParameter("newuser");
String email = request.getParameter("email");
String ip = request.getRemoteAddr();
String degree = request.getParameter("degree");
String cancer = request.getParameter("cancer");
String biologist = request.getParameter("biologist");
Player player = new Player();
player.setName(username);
player.setPassword(password);
player.setIp(ip);
player.setEmail(email);
%>
Hello <%=username %>
<% 
//try again
if (username == null || password == null) {
	response.sendRedirect("login.jsp");   
}
//validate
boolean success = true;

//try to create a new user 
if(newuser!=null){
	player = Player.create(username, ip, password, email, degree, cancer, biologist); 
}else{
	player = player.lookupByUserPassword();
}
//look them up again to set their id
if(player==null){
	//TODO informative error messages
	success=false;
}

//take them to the games area
//optionally add a "how to play' screen here if{newuser}
if (success) {
    session.setAttribute("username", username);
    session.setAttribute("player", player);
 	response.sendRedirect("boardroom.jsp");   
}else{ //something went wrong 
	response.sendRedirect("login.jsp");    
}
%>
