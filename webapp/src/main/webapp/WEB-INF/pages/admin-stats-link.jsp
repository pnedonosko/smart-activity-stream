<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="java.util.Map" %>

<%
  Map<String, String> messages = (Map<String, String>) request.getAttribute("messages");
%>

<div id="stats-link" class="hidden">
  <span id="stats-menu-link-title">${messages["smartactivity.link.user-menu-title"]}</span>
</div>