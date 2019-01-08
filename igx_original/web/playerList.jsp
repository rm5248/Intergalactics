<%@ page import="java.util.*,igx.stats.web.*,igx.stats.*" %>
<%
    List players = (List) request.getAttribute(StatsServlet.ATTRIB_PLAYERS);
%>
<html>
<head>
<title>
Player List
</title>
</head>
<body>
<h1>
Player List
</h1>
<% for (int i=0; i<players.size(); i++) {
    Player player = (Player) players.get(i); %>
    <a href="/stats?action=<%=StatsServlet.ACTION_VIEW_PLAYER%>&<%=StatsServlet.PARAM_PLAYER_NAME%>=<%=player.getName()%>"><%=player.getName()%></a><br/>
<% } %>
</body>
</html>
