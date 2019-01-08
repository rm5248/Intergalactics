<%@ page import="java.util.*,igx.stats.web.*,igx.stats.*" %>
<%
    Game game = (Game) request.getAttribute(StatsServlet.ATTRIB_GAMES);
    int lastTime = ((Integer)request.getAttribute(StatsServlet.ATTRIB_LAST_TURN)).intValue();
    int time = Integer.parseInt(request.getParameter(StatsServlet.PARAM_TURN));
    int galaxySize = Integer.parseInt(request.getParameter(StatsServlet.PARAM_GALAXY_SIZE));
    int turn = time / 20 + 1;
    int segment = time % 20;
    String uri = "/stats?action=" + StatsServlet.ACTION_SCREENSHOT +
                  "&" + StatsServlet.PARAM_GAMEID + "=" + game.getId() +
                  "&" + StatsServlet.PARAM_GALAXY_SIZE + "=" + galaxySize +
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
<%=turn%>:<%=segment%>
</h2>
<center>
<table border=0>
<img borderColor="gray" src="/charts?action=<%=ChartServlet.ACTION_CREATE_SCREENSHOT%>&<%=StatsServlet.PARAM_GAMEID%>=<%=game.getId()%>&<%=StatsServlet.PARAM_TURN%>=<%=time%>&<%=StatsServlet.PARAM_GALAXY_SIZE%>=<%=galaxySize%>"/>
</center>
<p align="center">
<b>
<% if (time != 0) { %><a href="<%=uri%>0">First</a><% } %>
<% if ((time - 20) >= 0) { %>&nbsp;<a href="<%=uri%><%=(time-20)%>">&lt;&lt;</a><% } %>
<% if (time != 0) { %>&nbsp;<a href="<%=uri%><%=(time-1)%>">&lt;</a><% } %>
<% if (time < lastTime) { %>&nbsp;&nbsp;&nbsp;<a href="<%=uri%><%=(time+1)%>">&gt;</a><% } %>
<% if ((time + 20) <= lastTime) { %>&nbsp;<a href="<%=uri%><%=(time+20)%>">&gt;&gt;</a><% } %>
<% if (time != lastTime) { %><a href="<%=uri%><%=lastTime%>">Last</a><% } %>
</b><br>
<br>
<a href="/stats?action=<%=StatsServlet.ACTION_VIEW_GAME%>&<%=StatsServlet.PARAM_GAMEID%>=<%=game.getId()%>">Game Stats</a>
</p>
<jsp:include page="footer.jsp"/>
</html>
