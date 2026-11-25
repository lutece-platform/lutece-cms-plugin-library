<%@ page errorPage="../../ErrorPage.jsp" %>
<%-- Upload of images and PDF files from the rich text editors: the bean answers JSON, without the admin page around it --%>
${ editorUploadJspBean.processController( pageContext.request, pageContext.response ) }
