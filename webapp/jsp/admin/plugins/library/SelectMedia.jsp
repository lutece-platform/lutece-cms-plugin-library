<%@ page errorPage="../../ErrorPage.jsp" %>

${ pageContext.setAttribute( 'strContent', libraryInsertServiceJspBean.processController( pageContext.request , pageContext.response ) ) }

<jsp:include page="../../insert/InsertServiceHeader.jsp" />

${ pageContext.getAttribute( 'strContent' ) }
