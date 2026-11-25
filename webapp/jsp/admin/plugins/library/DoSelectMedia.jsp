<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../insert/InsertServiceHeader.jsp" />

${ libraryInsertServiceJspBean.doSelectMedia( pageContext.request ) }
