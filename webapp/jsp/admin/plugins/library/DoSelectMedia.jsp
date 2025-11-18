<%@ page errorPage="../../ErrorPage.jsp" trimDirectiveWhitespaces="true" %>

${ pageContext.response.sendRedirect( libraryInsertServiceJspBean.doSelectMedia( pageContext.request )) }
