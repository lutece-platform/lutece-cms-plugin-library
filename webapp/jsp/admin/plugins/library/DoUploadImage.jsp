<jsp:include page="../../insert/InsertServiceHeader.jsp" />

<jsp:useBean id="libraryuploadInsertService" scope="session" class="fr.paris.lutece.plugins.library.web.UploadInsertServiceJspBean" />

<% response.sendRedirect( libraryuploadInsertService.doCreateImage( request ) );%>
