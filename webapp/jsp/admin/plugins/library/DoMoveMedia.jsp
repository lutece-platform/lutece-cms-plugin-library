<%@ page errorPage="../../ErrorPage.jsp" trimDirectiveWhitespaces="true" %>

${ pageContext.response.sendRedirect( libraryInsertServiceJspBean.doMoveMedia( pageContext.request )) }
