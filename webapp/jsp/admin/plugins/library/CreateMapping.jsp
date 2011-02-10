<%@ page errorPage="../../ErrorPage.jsp" %>

<jsp:include page="../../AdminHeader.jsp" />

<jsp:useBean id="library" scope="session" class="fr.paris.lutece.plugins.library.web.LibraryJspBean" />

<% library.init( request, library.LIBRARY_MANAGEMENT ); %>
<%= library.getCreateMapping( request ) %>

<%@ include file="../../AdminFooter.jsp" %>