<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
	<%@ page import="org.scripps.combo.Player"%>
<%

Player player = (Player) session.getAttribute("player");
if(player!=null){
	session.removeAttribute("player");
}
response.sendRedirect("./");   
%>

