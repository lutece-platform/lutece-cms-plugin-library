<%@ page errorPage="../../ErrorPage.jsp" trimDirectiveWhitespaces="true" %>

${ pageContext.response.sendRedirect( libraryInsertServiceJspBean.addAllSpace( pageContext.request )) }
