<%@ page import="java.util.*,igx.stats.web.*,igx.stats.*" %>
<%
    List games = (List) request.getAttribute(StatsServlet.ATTRIB_GAMES);
%>
<html>
<head>
<title>
Game View
</title>
</head>
<jsp:include page="header.jsp"/>
<h1>
Games played in the last 24 hours
</h1>
<center>
<table width="520" border="1">
<tr>
    <td>Date/Time</td>
    <td>Players</td>
</tr>
<% for (int i=0; i<games.size(); i++) {
    Game game = (Game) games.get(i); %>
    <tr>
        <td width="30%"><a href="/stats?action=<%=StatsServlet.ACTION_VIEW_GAME%>&<%=StatsServlet.PARAM_GAMEID%>=<%=game.getId()%>"><%=game.getTime()%></a></td>
        <td>
    <%  Iterator players = game.getPlayers().iterator();
        while (players.hasNext()) {
            Player player = (Player) players.next(); %>
            &nbsp;<a href="/stats?action=<%=StatsServlet.ACTION_VIEW_PLAYER%>&<%=StatsServlet.PARAM_PLAYER_NAME%>=<%=player.getName()%>"><% if(game.getWinner()==player) { %><font color="yellow"><%}%><%=player.getName()%><% if(game.getWinner()==player) { %></font><%}%></a>
    <% } %>
        </td>
    </tr>
<% } %>
</table>

<jsp:include page="footer.jsp"/>
</html>
