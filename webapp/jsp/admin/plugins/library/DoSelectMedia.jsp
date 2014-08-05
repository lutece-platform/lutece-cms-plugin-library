<%@ page errorPage="../../ErrorPage.jsp" trimDirectiveWhitespaces="true" %>

<jsp:useBean id="libraryInsertService" scope="session" class="fr.paris.lutece.plugins.library.web.LibraryInsertServiceJspBean" />

<% 
	response.sendRedirect( libraryInsertService.doSelectMedia( request ) );
%>

