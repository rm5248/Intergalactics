<%@ page import="java.util.*,igx.stats.web.*,igx.stats.*" %>
<%
    Set games = (Set) request.getAttribute(StatsServlet.ATTRIB_GAMES);
    Player player = (Player) request.getAttribute(StatsServlet.ATTRIB_PLAYERS);
%>
<html>
<head>
<title>
Player List
</title>
</head>
<jsp:include page="header.jsp"/>
<h1>
Player: <%=player.getName()%> ( <%=StatsHelper.getGamesWon(player)%>/<%=games.size()%> )
</h1>
<p/>
<img src="/charts?action=<%=ChartServlet.ACTION_CREATE_OPPONENT_CHART%>&<%=StatsServlet.PARAM_PLAYER_NAME%>=<%=player.getName()%>"/>
<img src="/charts?action=<%=ChartServlet.ACTION_CREATE_HUMAN_CHART%>&<%=StatsServlet.PARAM_PLAYER_NAME%>=<%=player.getName()%>"/><br/>
<img src="/charts?action=<%=ChartServlet.ACTION_CREATE_WINS_CHART%>&<%=StatsServlet.PARAM_PLAYER_NAME%>=<%=player.getName()%>"/>

<h2>
Recent Games
</h2>
<table border="1" width="504">
<tr>
    <td>Game Time</td>
    <td align="center"># Plyr</td>
    <td>Players</td>
</tr>
<%  Iterator iter = games.iterator();
    while (iter.hasNext()) {
        Game game = (Game) iter.next(); %>
    <tr>
        <td width="33%"><a href="/stats?action=<%=StatsServlet.ACTION_VIEW_GAME%>&<%=StatsServlet.PARAM_GAMEID%>=<%=game.getId()%>"><%=game.getTime()%></a></td>
        <td width="10%" align="center"><%=game.getPlayers().size()%></td>
        <td>
            <%  Iterator players = game.getPlayers().iterator();
            while (players.hasNext()) {
                Player gplayer = (Player) players.next(); %>
                &nbsp;<a href="/stats?action=<%=StatsServlet.ACTION_VIEW_PLAYER%>&<%=StatsServlet.PARAM_PLAYER_NAME%>=<%=gplayer.getName()%>"><% if(game.getWinner()==gplayer) { %><font color="yellow"><%}%><%=gplayer.getName()%><% if(game.getWinner()==gplayer) { %></font><%}%></a>
            <% } %>
        </td>
    </tr>
<% } %>

<jsp:include page="footer.jsp"/>
</html>
