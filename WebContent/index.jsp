<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>

<%
String username = request.getParameter("username");
if(username!=null){
	session.setAttribute("username", username);
}else{
	session.setAttribute("username", null);
}
%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-type" content="text/html; charset=utf-8">
<title>Welcome to COMBO, games of prediction and discovery</title>
<link rel="stylesheet" href="styles/styles.css" type="text/css"
	media="screen">
</head>
<body>
	<div id="content" class="container">
		<% if(username==null){ %>
		<div id="login">
			<form target="index.jsp">
				Enter name to start: <input type="text" name="username" value=""/><input type="submit" value="Submit" />
			</form>
		</div>
		<%}else{ %>
		<div id="header">
			Welcome <%=username %>! <a href="index.jsp">logout</a>
		</div>
		<div id="games">
		Play:
		<ul>
			<li>Breast Cancer prognosis game
				<ol>
					<li>Version 1 <a href="genecard1.jsp">Play!</a>
					</li>
				</ol>
			</li>
		</ul>
		</div>
		<%} %>
		
	</div>
</body>
</html>