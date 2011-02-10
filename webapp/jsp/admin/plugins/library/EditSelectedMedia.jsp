<jsp:include page="../../insert/InsertServiceHeader.jsp" />
<jsp:useBean id="libraryInsertService" scope="session" class="fr.paris.lutece.plugins.library.web.LibraryInsertServiceJspBean" />

<%= libraryInsertService.getEditSelectedMedia( request ) %>
