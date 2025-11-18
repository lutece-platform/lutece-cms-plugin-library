<jsp:include page="../../insert/InsertServiceHeader.jsp" />

${ pageContext.response.sendRedirect( libraryUploadInsertServiceJspBean.doCreateDocument( pageContext.request )) }
