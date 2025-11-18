<%@ page errorPage="../../ErrorPage.jsp" %>

<%@page import="fr.paris.lutece.plugins.library.web.LibraryJspBean"%>

${ libraryJspBean.init( pageContext.request, LibraryJspBean.LIBRARY_MANAGEMENT ) }
${ pageContext.response.sendRedirect( libraryJspBean.doRemoveMedia( pageContext.request )) }
