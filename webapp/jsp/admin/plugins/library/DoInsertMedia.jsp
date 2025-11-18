<jsp:include page="../../insert/InsertServiceHeader.jsp" />

${ pageContext.response.sendRedirect( libraryInsertServiceJspBean.doInsertUrl( pageContext.request )) }
