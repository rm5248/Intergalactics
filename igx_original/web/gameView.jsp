<%@ page import="java.util.*,igx.stats.web.*,igx.stats.*" %>
<%
    Game game = (Game) request.getAttribute(StatsServlet.ATTRIB_GAMES);
    List finalStates = (List) request.getAttribute(StatsServlet.ATTRIB_FINAL_STATES);
    TimeState sampleFinal = (TimeState) finalStates.get(0);
    String gameLength = sampleFinal.getTime()/20 + ":" + sampleFinal.getTime()%20;
    String mapUrl = "/charts?action="+ChartServlet.ACTION_CREATE_GAME_IMAGEMAP+"&"+StatsServlet.PARAM_GAMEID+"="+game.getId();
    String screenshotURI = "/stats?action=" + StatsServlet.ACTION_SCREENSHOT +
                  "&" + StatsServlet.PARAM_GAMEID + "=" + game.getId() +
                  "&" + StatsServlet.PARAM_GALAXY_SIZE + "=600" +
                  "&" + StatsServlet.PARAM_TURN + "=";
%>
<html>
<head>
<title>
Game View
</title>
</head>
<jsp:include page="header.jsp"/>
<h1>
<%=game.getTime()%><br/>
</h1>
<h2>
Winner:&nbsp;<a href="/stats?action=<%=StatsServlet.ACTION_VIEW_PLAYER%>&<%=StatsServlet.PARAM_PLAYER_NAME%>=<%=game.getWinner().getName()%>"><%=game.getWinner().getName()%></a> after <%=gameLength%> turns.<br/>
</h2>
<center>
<img src="/charts?action=<%=ChartServlet.ACTION_CREATE_CHART%>&<%=StatsServlet.PARAM_GAMEID%>=<%=game.getId()%>" usemap="#gameMap"/>
<jsp:include page="<%=mapUrl%>"/>
<p/>
<table border="1">
<tr>
    <td>Place</td>
    <td>Player</td>
    <td>Final Score</td>
    <td>Final Planets</td>
    <td>Final Ships</td>
    <td>Final Production</td>
</tr>
<%  int place = 0;
    int lastScore = Integer.MAX_VALUE;

    for (int i=0; i<finalStates.size(); i++) {
        TimeState state = (TimeState) finalStates.get(i);
        if (state.getScore() < lastScore) {
            place++;
        }
        lastScore = state.getScore();
        Player player = state.getPlayer(); %>
        <tr>
            <td><%=place%></td>
            <td>&nbsp;<a href="/stats?action=<%=StatsServlet.ACTION_VIEW_PLAYER%>&<%=StatsServlet.PARAM_PLAYER_NAME%>=<%=player.getName()%>"><%=player.getName()%></a><%=player.isRobot()?"&nbsp;(R)":""%></td>
            <td><%=state.getScore()%></td>
            <td><%=state.getNumPlanets()%></td>
            <td><%=state.getNumShips()%></td>
            <td><%=state.getTotalProduction()%></td>
        </tr>
<% } %>
</table>
<p>
<img src="/charts?action=<%=ChartServlet.ACTION_CREATE_GAME_PIE%>&<%=StatsServlet.PARAM_GAMEID%>=<%=game.getId()%>&<%=ChartServlet.PIE_TYPE%>=<%=ChartServlet.PIE_PLANETS%>"/>
<img src="/charts?action=<%=ChartServlet.ACTION_CREATE_GAME_PIE%>&<%=StatsServlet.PARAM_GAMEID%>=<%=game.getId()%>&<%=ChartServlet.PIE_TYPE%>=<%=ChartServlet.PIE_SHIPS%>"/>
</center>
<% if (!game.isSave()) { %>
<a href="/stats?action=<%=StatsServlet.ACTION_SAVE_GAME%>&<%=StatsServlet.PARAM_GAMEID%>=<%=game.getId()%>">Save this game</a><br/>
<% } else {%>
This game is saved.
<% } %>
<a href="<%=screenshotURI%>0">View Game</a>
<jsp:include page="footer.jsp"/>
</html>
