<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="org.scripps.combo.GameLog"%>
<%@ page import="org.scripps.combo.Player"%>
<% 
GameLog log = new GameLog();
GameLog.high_score sb = log.getScoreBoard();
System.out.println("gettign score board");
Player player = (Player) session.getAttribute("player");
boolean show_player = false;
if(player!=null&&(!player.getName().equals("anonymous_hero"))){
	show_player = true;
}
%>

			<table>
			<caption><b><u>Combo leaders</u></b></caption>
				<thead>
					<tr>
						<th>Rank</th>
						<th>Player</th>
						<th>Points</th>
					</tr>
				</thead>
				<tbody>
					<%
					int r = 0;
					for(String name : sb.getPlayer_global_points().keySet()){
						r++;
						String rowhighlight = "";
						if(show_player&&player.getName().equals(name)){
							rowhighlight = "style=\"background-color:#F5CC6C\";";
						}
						%>
						<tr align="center" <%=rowhighlight %>>
						<td><%=r%></td>
						<td><%=name %></td>
						<td><%=sb.getPlayer_global_points().get(name)%></td> 
					</tr>
					<% 
					}
					%>
				</tbody>
			</table>
