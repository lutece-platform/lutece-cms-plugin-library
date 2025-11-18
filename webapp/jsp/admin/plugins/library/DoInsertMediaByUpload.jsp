<jsp:include page="../../insert/InsertServiceHeader.jsp" />

${ pageContext.response.sendRedirect( libraryUploadInsertServiceJspBean.doInsertUrl( pageContext.request )) }
