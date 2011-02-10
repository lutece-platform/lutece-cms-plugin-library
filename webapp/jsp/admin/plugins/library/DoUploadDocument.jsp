<jsp:include page="../../insert/InsertServiceHeader.jsp" />

<jsp:useBean id="libraryuploadInsertService" scope="session" class="fr.paris.lutece.plugins.library.web.UploadInsertServiceJspBean" />

<%=  libraryuploadInsertService.doCreateDocument( request ) %>
