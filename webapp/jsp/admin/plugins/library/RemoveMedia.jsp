<%@ page errorPage="../../ErrorPage.jsp" %>

<jsp:useBean id="library" scope="session" class="fr.paris.lutece.plugins.library.web.LibraryJspBean" />

<% 
library.init( request, library.LIBRARY_MANAGEMENT ); 
response.sendRedirect( library.getRemoveMedia( request ) );
%>