<%@ page errorPage="../../ErrorPage.jsp" %>

<%@page import="fr.paris.lutece.plugins.library.web.LibraryJspBean"%>

${ libraryJspBean.init( pageContext.request, LibraryJspBean.LIBRARY_MANAGEMENT ) }
${ pageContext.setAttribute( 'strContent', libraryJspBean.getModifyAttribute( pageContext.request, pageContext.response ) ) }

<jsp:include page="../../AdminHeader.jsp" />

${ pageContext.getAttribute( 'strContent' ) }

<%@ include file="../../AdminFooter.jsp" %>