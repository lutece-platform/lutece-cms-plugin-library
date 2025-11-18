<jsp:include page="../../insert/InsertServiceHeader.jsp" />

${ pageContext.response.sendRedirect( libraryUploadInsertServiceJspBean.doCreateImage( pageContext.request )) }
