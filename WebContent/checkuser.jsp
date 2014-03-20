<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
	<%@ page import="org.scripps.combo.model.Player"%>
	<%@ page import="java.security.MessageDigest"%>
<%
/*
---- To use md5 hash on password -----

StringBuffer hexString = new StringBuffer();
MessageDigest md = MessageDigest.getInstance("MD5");
byte[] hash = md.digest(request.getParameter("password").getBytes());

        for (int i = 0; i < hash.length; i++) {
            if ((0xff & hash[i]) < 0x10) {
                hexString.append("0"
                        + Integer.toHexString((0xFF & hash[i])));
            } else {
                hexString.append(Integer.toHexString(0xFF & hash[i]));
            }
        }
        String password = hexString.toString();
 */
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
boolean nametaken = false;
boolean passwordfailed = false;
//try to create a new user 
if(newuser!=null){
	player = Player.create(username, ip, password, email, degree, cancer, biologist); 
	//returns null if the user name is taken
	if(player==null){
		nametaken = true;
		success = false;
	}
}else{
	player = player.lookupByUserPassword();
	if(player==null){
		passwordfailed = true;
		success = false;
	}
}
//take them to the games area
if (success) {	
    session.setAttribute("username", username);
    session.setAttribute("player", player);
    //if they've passed training, they will be taken to the main game area
    response.sendRedirect("cure2%2E0/index.jsp"); 
}else if(nametaken==true){ //something went wrong 
	response.sendRedirect("login.jsp?bad=nametaken");    
}else if(passwordfailed){
	response.sendRedirect("login.jsp?bad=pw"); 
}else{
	response.sendRedirect("login.jsp"); 
}
%>
