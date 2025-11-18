<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%@page import="fr.paris.lutece.plugins.library.web.LibraryJspBean"%>

${ libraryJspBean.init( pageContext.request, LibraryJspBean.LIBRARY_MANAGEMENT ) }
${ libraryJspBean.getCreateMedia( pageContext.request ) }

<%@ include file="../../AdminFooter.jsp" %>