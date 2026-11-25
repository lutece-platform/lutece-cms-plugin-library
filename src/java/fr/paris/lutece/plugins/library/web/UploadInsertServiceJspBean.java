/*
 * Copyright (c) 2002-2014, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.library.web;

import fr.paris.lutece.plugins.document.business.Document;
import fr.paris.lutece.plugins.document.business.DocumentType;
import fr.paris.lutece.plugins.document.business.DocumentTypeHome;
import fr.paris.lutece.plugins.document.business.attributes.DocumentAttribute;
import fr.paris.lutece.plugins.document.business.attributes.DocumentAttributeHome;
import fr.paris.lutece.plugins.document.business.spaces.DocumentSpace;
import fr.paris.lutece.plugins.document.business.spaces.DocumentSpaceHome;
import fr.paris.lutece.plugins.document.business.workflow.DocumentAction;
import fr.paris.lutece.plugins.document.business.workflow.DocumentActionHome;
import fr.paris.lutece.plugins.document.modules.metadatadublincore.business.DublinCoreMetadata;
import fr.paris.lutece.plugins.document.service.DocumentException;
import fr.paris.lutece.plugins.document.service.DocumentService;
import fr.paris.lutece.plugins.document.service.DocumentTypeResourceIdService;
import fr.paris.lutece.plugins.document.service.spaces.DocumentSpacesService;
import fr.paris.lutece.plugins.library.business.LibraryMapping;
import fr.paris.lutece.plugins.library.business.LibraryMapping.AttributeAssociation;
import fr.paris.lutece.plugins.library.business.LibraryMappingHome;
import fr.paris.lutece.plugins.library.business.LibraryMedia;
import fr.paris.lutece.plugins.library.business.LibraryMediaHome;
import fr.paris.lutece.plugins.library.business.MediaAttribute;
import fr.paris.lutece.plugins.library.business.MediaAttributeHome;
import fr.paris.lutece.plugins.library.service.LibraryMediaTemplateService;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.template.HtmlMarkup;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.web.insert.InsertServiceJspBean;
import fr.paris.lutece.portal.web.insert.InsertServiceSelectionBean;
import fr.paris.lutece.portal.web.upload.MultipartHttpServletRequest;
import fr.paris.lutece.util.html.HtmlTemplate;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * UploadInsertServiceJspBean
 * Used for uploading document
 *
 */
@RequestScoped
@Named( "libraryUploadInsertServiceJspBean" )
public class UploadInsertServiceJspBean extends InsertServiceJspBean implements InsertServiceSelectionBean
{
    private static final long serialVersionUID = -7071548876864568789L;
    private static final String PARAMETER_PLUGIN_NAME = "plugin_name";
    private static final String PLUGIN_NAME = "library";

    //TEMPLATES
    private static final String TEMPLATE_MEDIATYPE_SELECTOR = "admin/plugins/library/upload_mediatype_selector.html";
    private static final String TEMPLATE_SPACE_SELECTOR = "admin/plugins/library/upload_space_selector.html";
    private static final String TEMPLATE_MEDIA_CREATOR = "admin/plugins/library/media_creator.html";
    private static final String TEMPLATE_EDIT_MEDIA = "admin/plugins/library/mediaedition_selector.html";

    //JSP
    private static final String JSP_INSERT_MEDIA = "jsp/admin/plugins/library/DoInsertMediaByUpload.jsp";

    //MARKS
    private static final String MARK_FIELDS = "fields";
    private static final String MARK_DOCUMENT_TYPE = "document_type";
    private static final String MARK_MEDIA_TYPES = "mediatypes";
    private static final String MARK_METADATA = "metadata";
    private static final String MARK_MEDIA = "media";
    private static final String MARK_INPUT = "input";
    private static final String MARK_MEDIA_TYPE = "mediatype";
    private static final String MARK_MEDIA_TYPE_ATTRIBUTES = "media_attributes";
    private static final String MARK_MEDIA_ATTRIBUTES_ASSOCIATIONS = "attributes_associations";
    private static final String MARK_MEDIA_DOC = "document";
    private static final String MARK_JSP_INSERT = "jsp_insert";
    private static final String MARK_SPACE = "space";
    private static final String MARK_SPACES_TREE = "spaces_tree";
    private static final String MARK_SPACE_ID = "space_id";
    private static final String MARK_LOCALE = "locale";
    private static final String MARK_WEBAPP_URL = "webapp_url";
    private static final String MARK_IS_IMAGE = "is_image";
    private static final String MARK_USER_LAST_NAME = "user_last_name";
    private static final String MARK_SELECTED_SPACE_ID = "selected_space_id";

    //PARAMETERS
    private static final String PARAMETER_DOCUMENT_TYPE_CODE = "document_type_code";
    private static final String PARAMETER_INPUT = "input";
    private static final String PARAMETER_MEDIA_TYPE = "media_type";
    private static final String PARAMETER_MEDIA_ID = "media";
    private static final String PARAMETER_SPACE_ID = "space_id";

    //MESSAGE
    private static final String MESSAGE_DOCUMENT_ERROR = "library.uploadinsertservice.message.documentError";
    private static final String MESSAGE_NOT_AUTHORIZED = "library.uploadinsertservice.message.notAuthorized";
    private static final String SPACE_ID_SESSION = "spaceIdSession";
    private static final String URL_DOCUMENT_RESOURCE = "servlet/plugins/document/resource?id=";
    private static final String URL_PARAMETER_ATTRIBUTE = "&id_attribute=";
    
    @Inject
    private DocumentService _documentService;
    @Inject
    private DocumentSpacesService _documentSpacesService;
    @Inject
    private LibraryMediaTemplateService _mediaTemplateService;
    private AdminUser _user;
    private Plugin _plugin;
    private String _input;

    /**
     * Get the select type of document page
     * @param request the request
     * @return the page
     */
    @Override
    public String getInsertServiceSelectorUI( HttpServletRequest request )
    {
        init( request );

        Collection<LibraryMedia> libraryMedia = LibraryMediaHome.findAllMedia( _plugin );
        HashMap<String, Object> model = getDefaultModel(  );
        model.put( MARK_MEDIA_TYPES, libraryMedia );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MEDIATYPE_SELECTOR, _user.getLocale(  ), model );

        return template.getHtml(  );
    }

    private void init( HttpServletRequest request )
    {
        String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
        _user = AdminUserService.getAdminUser( request );
        // The multipart forms of the upload (media_creator.html) do not carry plugin_name, and the multipart servlet keeps only their fields
        _plugin = PluginService.getPlugin( StringUtils.defaultIfEmpty( strPluginName, PLUGIN_NAME ) );
        _input = request.getParameter( PARAMETER_INPUT );
    }

    private HashMap<String, Object> getDefaultModel(  )
    {
        HashMap<String, Object> model = new HashMap<String, Object>(  );
        model.put( PARAMETER_PLUGIN_NAME, _plugin.getName(  ) );
        model.put( PARAMETER_INPUT, _input );

        return model;
    }

    /**
     * Return the form for creating a document
     * @param request The request
     * @return a form with needed fields
     */
    public String getCreateMedia( HttpServletRequest request )
    {
        init( request );

        String strMediaType = request.getParameter( PARAMETER_MEDIA_TYPE );

        String strSpaceId = StringUtils.defaultIfEmpty( request.getParameter( PARAMETER_SPACE_ID ),
                request.getParameter( DocumentSpacesService.PARAMETER_BROWSER_SELECTED_SPACE_ID ) );
        int nSpaceId = -1;

        if ( ( strSpaceId != null ) && ( strSpaceId.length(  ) > 0 ) )
        {
            nSpaceId = Integer.parseInt( strSpaceId );
            request.getSession(  ).setAttribute( SPACE_ID_SESSION, nSpaceId );
        }
        else
        {
            Integer nSpaceIdTmp = (Integer) request.getSession(  ).getAttribute( SPACE_ID_SESSION );

            if ( nSpaceIdTmp != null )
            {
                nSpaceId = nSpaceIdTmp.intValue(  );
            }
        }

        LibraryMedia mediaType = LibraryMediaHome.findByPrimaryKey( Integer.parseInt( strMediaType ), _plugin );
        Collection<LibraryMapping> libraryMappingList  = LibraryMappingHome.findAllMappingsByMedia( mediaType.getMediaId(  ), _plugin);
        
        // there should be only one mapping per media
        LibraryMapping mapping = null;
        for (LibraryMapping libMap : libraryMappingList) 
        {
            mapping = LibraryMappingHome.findByPrimaryKey( libMap.getIdMapping( ), _plugin );
        }

        DocumentType documentType = DocumentTypeHome.findByPrimaryKey( mapping.getCodeDocumentType(  ) );

        if ( ( nSpaceId == -1 ) ||
                ( !_documentService.isAuthorizedAdminDocument( nSpaceId, documentType.getCode(  ),
                    DocumentTypeResourceIdService.PERMISSION_CREATE, _user ) ) )
        {
            return getSelectSpace( request );
        }

        HashMap<String, Object> model = getDefaultModel(  );
        model.put( MARK_DOCUMENT_TYPE, documentType.getCode(  ) );
        model.put( MARK_FIELDS, HtmlMarkup.of(
            _documentService.getCreateForm( documentType.getCode(  ), _user.getLocale(  ),
                AppPathService.getBaseUrl( request ) ) ) );
        model.put( MARK_METADATA, new DublinCoreMetadata(  ) );
        model.put( MARK_MEDIA, mediaType.getMediaId(  ) );
        model.put( MARK_INPUT, _input );
        model.put( MARK_SPACE_ID, nSpaceId );
        model.put( MARK_WEBAPP_URL, AppPathService.getBaseUrl( request ) );
        model.put( MARK_LOCALE, _user.getLocale(  ).getLanguage(  ) );
        model.put( MARK_IS_IMAGE, Document.CODE_DOCUMENT_TYPE_IMAGE.equals( mediaType.getName() ) ) ;
        model.put( MARK_USER_LAST_NAME, _user.getLastName( ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MEDIA_CREATOR, _user.getLocale(  ), model );

        return template.getHtml(  );
    }

    /**
     * Return a page with space for selection
     * @param request the request
     * @return the page
     */
    public String getSelectSpace( HttpServletRequest request )
    {
        init( request );

        String strMediaType = request.getParameter( PARAMETER_MEDIA_TYPE );

        LibraryMedia mediaType = LibraryMediaHome.findByPrimaryKey( Integer.parseInt( strMediaType ), _plugin );
        Collection<LibraryMapping> libraryMappingList  = LibraryMappingHome.findAllMappingsByMedia( mediaType.getMediaId(  ), _plugin);
        
        // there should be only one mapping per media
        LibraryMapping mapping = null;
        for (LibraryMapping libMap : libraryMappingList) 
        {
            mapping = LibraryMappingHome.findByPrimaryKey( libMap.getIdMapping( ), _plugin );
        }
        DocumentType documentType = DocumentTypeHome.findByPrimaryKey( mapping.getCodeDocumentType(  ) );

        String strSpaceIdRequest = request.getParameter( DocumentSpacesService.PARAMETER_BROWSER_SELECTED_SPACE_ID );
        int nSpaceId = -1;

        if ( ( strSpaceIdRequest != null ) && ( strSpaceIdRequest.length(  ) > 0 ) )
        {
            nSpaceId = Integer.parseInt( strSpaceIdRequest );
            request.getSession(  ).setAttribute( SPACE_ID_SESSION, nSpaceId );
        }
        else
        {
            Integer nSpaceIdTmp = (Integer) request.getSession(  ).getAttribute( SPACE_ID_SESSION );

            if ( nSpaceIdTmp != null )
            {
                nSpaceId = nSpaceIdTmp.intValue(  );
            }
        }

        DocumentSpace currentSpace = DocumentSpaceHome.findByPrimaryKey( nSpaceId );

        HashMap<String, Object> model = getDefaultModel(  );

        model.put( MARK_INPUT, _input );
        model.put( MARK_SPACES_TREE, _documentSpacesService.getSpacesTreeRoots( _user ) );
        model.put( MARK_SELECTED_SPACE_ID, String.valueOf( nSpaceId ) );
        model.put( MARK_DOCUMENT_TYPE, documentType.getCode(  ) );
        model.put( MARK_SPACE, currentSpace );
        model.put( MARK_MEDIA_TYPE, strMediaType );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_SPACE_SELECTOR, _user.getLocale(  ), model );

        return template.getHtml(  );
    }

    /**
     * creation of a document
     * @param request The HTTP request
     * @return The URL to go after performing the action
     */
    public String createDocument( HttpServletRequest request , Document document )
    {
        init (request );

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

        String strDocumentTypeCode = request.getParameter( PARAMETER_DOCUMENT_TYPE_CODE );
        String strSpaceIdRequest = request.getParameter( PARAMETER_SPACE_ID );
        int nSpaceId;

        try
        {
            nSpaceId = Integer.parseInt( strSpaceIdRequest );
        }
        catch ( NumberFormatException e )
        {
            nSpaceId = -1;
        }

        if ( !_documentService.isAuthorizedAdminDocument( nSpaceId, strDocumentTypeCode,
                    DocumentTypeResourceIdService.PERMISSION_CREATE, _user ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_NOT_AUTHORIZED, AdminMessage.TYPE_ERROR );
        }

        document.setCodeDocumentType( strDocumentTypeCode );

        String strError = _documentService.getDocumentData( multipartRequest, document, _user.getLocale(  ) );

        if ( strError != null )
        {
            return strError;
        }

        document.setSpaceId( nSpaceId );
        document.setStateId( 1 );
        document.setCreatorId( _user.getUserId(  ) );

        try
        {
            _documentService.createDocument( document, _user );

            DocumentAction action = DocumentActionHome.findByPrimaryKey( DocumentAction.ACTION_VALIDATE );
            _documentService.validateDocument( document, _user, action.getFinishDocumentState(  ).getId(  ) );
        }
        catch ( DocumentException e )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_DOCUMENT_ERROR,
                new String[] { I18nService.getLocalizedString( e.getI18nMessage(  ), _user.getLocale(  ) ) },
                AdminMessage.TYPE_ERROR );
        }   
        
        
        return null;
    }
    
    /**
     * Creates a document, then shows the form of its media values
     * @param request The HTTP request
     * @param response The HTTP response, redirected to the error message when the document can not be created
     * @return the page of the media values, empty when the response is redirected
     * @throws IOException if the redirection fails
     */
    public String doCreateDocument( HttpServletRequest request, HttpServletResponse response ) throws IOException
    {
        Document document = new Document( ) ;
        String url = createDocument(request, document) ;

        if ( url != null ) 
        {
            response.sendRedirect( url );

            return StringUtils.EMPTY;
        } 

        return getEditSelectedMedia( request, document );
    }

    /**
     * Creates an image, then inserts it into the current editor without the page of the media values
     * @param request The HTTP request
     * @param response The HTTP response, redirected to the error message when the image can not be created
     * @return the page inserting the image, empty when the response is redirected
     * @throws IOException if the redirection fails
     */
    public String doCreateImage( HttpServletRequest request, HttpServletResponse response ) throws IOException
    {
        Document document = new Document( ) ;
        String url = createDocument(request, document) ;

        if ( url != null ) 
        {
            response.sendRedirect( url );

            return StringUtils.EMPTY;
        } 

        return getInsertMedia( request, document );
    }

    /**
     * Inserts the document into the current editor
     * @param request the request
     * @return the page inserting the document, rendered in the response to the form rather than reached through a redirect
     */
    public String doInsertMedia( HttpServletRequest request )
    {
        return getInsertMedia( request, null );
    }

    /**
     * Returns the page inserting the document into the current editor
     * @param request the request
     * @param document the document in case of "quick image insertion" without the "media_edition" page
     * @return the page inserting the document
     */
    private String getInsertMedia( HttpServletRequest request, Document document )
    {
        init( request );

        String strMediaTypeId = request.getParameter( PARAMETER_MEDIA_ID );
        LibraryMedia mediaType = LibraryMediaHome.findByPrimaryKey( Integer.parseInt( strMediaTypeId ), _plugin );
        mediaType.setMediaAttributeList( MediaAttributeHome.findAllAttributesForMedia( mediaType.getMediaId(  ), _plugin ) );

        Collection<LibraryMapping> allMappings = LibraryMappingHome.findAllMappingsByMedia( mediaType.getMediaId(  ), _plugin );
        LibraryMapping mapping = allMappings.iterator(  ).next(  );
        
        Map<String, String> mapAttributesAssociation = getAttributesFromMapping( mapping );
        Map<String, String> mapValues = new HashMap<>(  );

        for ( MediaAttribute attribute : mediaType.getMediaAttributeList(  ) )
        {
            String strValue = LibraryInsertPage.getMediaValue( request, attribute.getCode(  ) );

            if ( ( strValue == null ) && ( document != null ) )
            {
                strValue = getDocumentValue( document, attribute, mapAttributesAssociation );
            }

            mapValues.put( attribute.getCode(  ), strValue );
        }

        String strHtml = _mediaTemplateService.render( mediaType, List.of( mapValues ), _user.getLocale(  ) );

        return LibraryInsertPage.getHtml( _input, strHtml, _user.getLocale(  ) );
    }

    /**
     * Returns the value of a media attribute taken from the document itself, for a quick image insertion that skips the media edition page:
     * the document attribute of the same code, or, for a binary attribute, the URL of the document attribute it is mapped to
     * @param document the document just created
     * @param attribute the media attribute
     * @param mapAttributesAssociation the codes of the document attributes, keyed by the codes of the media attributes
     * @return the value, null if the document has none
     */
    private String getDocumentValue( Document document, MediaAttribute attribute, Map<String, String> mapAttributesAssociation )
    {
        DocumentAttribute documentAttribute = document.getAttribute( attribute.getCode(  ) );

        if ( documentAttribute != null )
        {
            return documentAttribute.getTextValue(  );
        }

        if ( ( attribute.getTypeId(  ) == MediaAttribute.ATTRIBUTE_TYPE_BINARY ) && ( mapAttributesAssociation != null ) )
        {
            DocumentAttribute mappedAttribute = document.getAttribute( mapAttributesAssociation.get( attribute.getCode(  ) ) );

            if ( mappedAttribute != null )
            {
                return URL_DOCUMENT_RESOURCE + document.getId(  ) + URL_PARAMETER_ATTRIBUTE + mappedAttribute.getId(  );
            }
        }

        return null;
    }

    /**
     * Return the page for edition of the created document
     * @param request the request
     * @param document the document just created
     * @return the form with document parameters
     */
    public String getEditSelectedMedia( HttpServletRequest request, Document document )
    {
        String strMediaTypeId = request.getParameter( PARAMETER_MEDIA_ID );

        LibraryMedia mediaType = LibraryMediaHome.findByPrimaryKey( Integer.parseInt( strMediaTypeId ), _plugin );
        mediaType.setMediaAttributeList( MediaAttributeHome.findAllAttributesForMedia( mediaType.getMediaId(  ), _plugin ) );

        Collection<LibraryMapping> allMappings = LibraryMappingHome.findAllMappingsByMedia( mediaType.getMediaId(  ),
                _plugin );
        LibraryMapping mapping = allMappings.iterator(  ).next(  );

        Map<String, Object> model = getDefaultModel(  );
        model.put( MARK_MEDIA_TYPE, mediaType );
        model.put( MARK_MEDIA_TYPE_ATTRIBUTES, mediaType.getMediaAttributeList(  ) );
        model.put( MARK_MEDIA_ATTRIBUTES_ASSOCIATIONS, getAttributesFromMapping( mapping ) );
        model.put( MARK_MEDIA_DOC, document );
        model.put( MARK_JSP_INSERT, JSP_INSERT_MEDIA );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_EDIT_MEDIA, _user.getLocale(  ), model );

        return template.getHtml(  );
    }

    private Map<String, String> getAttributesFromMapping( LibraryMapping m )
    {
        HashMap<String, String> result = new HashMap<String, String>(  );

        for ( AttributeAssociation assoc : m.getAttributeAssociationList(  ) )
        {
            result.put( MediaAttributeHome.findByPrimaryKey( assoc.getIdMediaAttribute(  ), _plugin ).getCode(  ),
                DocumentAttributeHome.findByPrimaryKey( assoc.getIdDocumentAttribute(  ) ).getCode(  ) );
        }

        return result;
    }
}
