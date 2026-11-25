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
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.web.cdi.mvc.Models;
import fr.paris.lutece.portal.web.insert.InsertServiceSelectionBean;
import fr.paris.lutece.portal.web.insert.InsertServiceSelectorJspBean;
import fr.paris.lutece.portal.web.upload.MultipartHttpServletRequest;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.html.HtmlTemplate;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;


/**
 * Upload insert service of the library: the choice of a media type, of the space where its document is created, the form of the
 * document, then the insertion into the editor of the HTML the template of the media type renders. The core instantiates the class
 * by reflection for its first screen (getInsertServiceSelectorUI), the screens after it are the views and actions of this controller.
 */
@RequestScoped
@Named( "libraryUploadInsertServiceJspBean" )
@Controller( controllerJsp = UploadInsertServiceJspBean.CONTROLLER_JSP, controllerPath = UploadInsertServiceJspBean.CONTROLLER_PATH, right = InsertServiceSelectorJspBean.RIGHT_MANAGE_LINK_SERVICE, securityTokenEnabled = true )
public class UploadInsertServiceJspBean extends MVCAdminJspBean implements InsertServiceSelectionBean
{
    static final String CONTROLLER_JSP = "SelectSpaceForUpload.jsp";
    static final String CONTROLLER_PATH = "jsp/admin/plugins/library/";
    private static final long serialVersionUID = -7071548876864568789L;

    // Views
    private static final String VIEW_SELECT_SPACE = "selectSpace";
    private static final String VIEW_CREATE_MEDIA = "createMedia";

    // Actions
    private static final String ACTION_CREATE_DOCUMENT = "createDocument";
    private static final String ACTION_INSERT_MEDIA = "insertMedia";
    private static final String PARAMETER_PLUGIN_NAME = "plugin_name";
    private static final String PLUGIN_NAME = "library";

    //TEMPLATES
    private static final String TEMPLATE_MEDIATYPE_SELECTOR = "admin/plugins/library/upload_mediatype_selector.html";
    private static final String TEMPLATE_SPACE_SELECTOR = "admin/plugins/library/upload_space_selector.html";
    private static final String TEMPLATE_MEDIA_CREATOR = "admin/plugins/library/media_creator.html";
    private static final String TEMPLATE_EDIT_MEDIA = "admin/plugins/library/mediaedition_selector.html";

    //JSP
    private static final String JSP_INSERT_MEDIA = CONTROLLER_PATH + CONTROLLER_JSP;

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
        initService( request );

        return getMediaTypeSelectorPage( null );
    }

    /**
     * The insert services list of the core reads the sub categories of each service
     * @return an empty list: the service has none
     */
    public ReferenceList getSubCategories(  )
    {
        return new ReferenceList(  );
    }

    /**
     * {@inheritDoc}
     * The bean serves the library whatever the request carries
     */
    @Override
    public Plugin getPlugin(  )
    {
        return PluginService.getPlugin( PLUGIN_NAME );
    }

    /**
     * Returns the page choosing the type of the media to upload
     * @param model the model of the view, null when the core asks for the first screen outside the controller
     * @return the page
     */
    private String getMediaTypeSelectorPage( Models model )
    {
        Map<String, Object> mapModel = getDefaultModel(  );
        mapModel.put( MARK_MEDIA_TYPES, LibraryMediaHome.findAllMedia( _plugin ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MEDIATYPE_SELECTOR, _user.getLocale(  ), toModel( model, mapModel ) );

        return template.getHtml(  );
    }

    /**
     * Sets the user, the plugin and the editor field of the insertion
     * @param request the request
     */
    private void initService( HttpServletRequest request )
    {
        _user = AdminUserService.getAdminUser( request );
        _plugin = PluginService.getPlugin( PLUGIN_NAME );
        _input = request.getParameter( PARAMETER_INPUT );
    }

    /**
     * Returns the values shared by the pages of the service
     * @return the values
     */
    private Map<String, Object> getDefaultModel(  )
    {
        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( PARAMETER_PLUGIN_NAME, PLUGIN_NAME );
        model.put( PARAMETER_INPUT, _input );

        return model;
    }

    /**
     * Returns the model a template of the service is rendered with: the model of the view, which carries the token of its forms, filled
     * with the values of the page; the values alone outside a view
     * @param model the model of the view, null outside a view
     * @param mapValues the values of the page
     * @return the model
     */
    private static Object toModel( Models model, Map<String, Object> mapValues )
    {
        if ( model == null )
        {
            return mapValues;
        }

        mapValues.forEach( model::put );

        return model;
    }

    /**
     * Returns the media type the request names by its media_type, else by its media
     * @param request the request
     * @return the media type, null when the identifier is missing, not a number or unknown
     */
    private LibraryMedia findMediaType( HttpServletRequest request )
    {
        String strMediaTypeId = StringUtils.defaultIfBlank( request.getParameter( PARAMETER_MEDIA_TYPE ), request.getParameter( PARAMETER_MEDIA_ID ) );
        int nMediaTypeId = NumberUtils.toInt( strMediaTypeId, -1 );

        return ( nMediaTypeId < 0 ) ? null : LibraryMediaHome.findByPrimaryKey( nMediaTypeId, _plugin );
    }

    /**
     * Returns the document type a media type uploads: the one of its mapping
     * @param mediaType the media type
     * @return the document type, null when the media type has no mapping
     */
    private DocumentType findDocumentType( LibraryMedia mediaType )
    {
        Collection<LibraryMapping> listMappings = LibraryMappingHome.findAllMappingsByMedia( mediaType.getMediaId(  ), _plugin );

        return listMappings.isEmpty(  ) ? null : DocumentTypeHome.findByPrimaryKey( listMappings.iterator(  ).next(  ).getCodeDocumentType(  ) );
    }

    /**
     * Returns the space the request names, else the one kept in the session, and keeps it there
     * @param request the request
     * @param strSpaceId the identifier of the space the request carries, may be null
     * @return the identifier, -1 when there is none
     */
    private static int getSpaceId( HttpServletRequest request, String strSpaceId )
    {
        if ( NumberUtils.isDigits( strSpaceId ) )
        {
            int nSpaceId = NumberUtils.toInt( strSpaceId, -1 );
            request.getSession(  ).setAttribute( SPACE_ID_SESSION, nSpaceId );

            return nSpaceId;
        }

        Integer nSpaceIdSession = (Integer) request.getSession(  ).getAttribute( SPACE_ID_SESSION );

        return ( nSpaceIdSession == null ) ? -1 : nSpaceIdSession;
    }

    /**
     * Returns the form creating the document of the media, in the space chosen; its token is the one of the creation
     * @param model the model of the view
     * @param request The request, carrying the media type and the space
     * @return the form, or the choice of the space when none is chosen or when the user may not create the document there
     */
    @View( value = VIEW_CREATE_MEDIA, securityTokenAction = ACTION_CREATE_DOCUMENT )
    public String getCreateMedia( Models model, HttpServletRequest request )
    {
        initService( request );

        LibraryMedia mediaType = findMediaType( request );
        DocumentType documentType = ( mediaType == null ) ? null : findDocumentType( mediaType );

        if ( documentType == null )
        {
            return getMediaTypeSelectorPage( model );
        }

        int nSpaceId = getSpaceId( request, StringUtils.defaultIfEmpty( request.getParameter( PARAMETER_SPACE_ID ),
                request.getParameter( DocumentSpacesService.PARAMETER_BROWSER_SELECTED_SPACE_ID ) ) );

        if ( ( nSpaceId == -1 ) ||
                ( !_documentService.isAuthorizedAdminDocument( nSpaceId, documentType.getCode(  ),
                    DocumentTypeResourceIdService.PERMISSION_CREATE, _user ) ) )
        {
            return redirect( request, VIEW_SELECT_SPACE, PARAMETER_MEDIA_TYPE, mediaType.getMediaId(  ) );
        }

        Map<String, Object> mapModel = getDefaultModel(  );
        mapModel.put( MARK_DOCUMENT_TYPE, documentType.getCode(  ) );
        mapModel.put( MARK_FIELDS, HtmlMarkup.of(
            _documentService.getCreateForm( documentType.getCode(  ), _user.getLocale(  ),
                AppPathService.getBaseUrl( request ) ) ) );
        mapModel.put( MARK_METADATA, new DublinCoreMetadata(  ) );
        mapModel.put( MARK_MEDIA, mediaType.getMediaId(  ) );
        mapModel.put( MARK_INPUT, _input );
        mapModel.put( MARK_SPACE_ID, nSpaceId );
        mapModel.put( MARK_WEBAPP_URL, AppPathService.getBaseUrl( request ) );
        mapModel.put( MARK_LOCALE, _user.getLocale(  ).getLanguage(  ) );
        mapModel.put( MARK_IS_IMAGE, isImage( mediaType ) );
        mapModel.put( MARK_USER_LAST_NAME, _user.getLastName( ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MEDIA_CREATOR, _user.getLocale(  ), toModel( model, mapModel ) );

        return template.getHtml(  );
    }

    /**
     * Returns the page choosing the space where the document of the media is created
     * @param model the model of the view
     * @param request the request, carrying the media type
     * @return the page, or the choice of the media type when the media type is missing, unknown or has no mapping
     */
    @View( value = VIEW_SELECT_SPACE, defaultView = true )
    public String getSelectSpace( Models model, HttpServletRequest request )
    {
        initService( request );

        LibraryMedia mediaType = findMediaType( request );
        DocumentType documentType = ( mediaType == null ) ? null : findDocumentType( mediaType );

        if ( documentType == null )
        {
            return getMediaTypeSelectorPage( model );
        }

        int nSpaceId = getSpaceId( request, request.getParameter( DocumentSpacesService.PARAMETER_BROWSER_SELECTED_SPACE_ID ) );

        Map<String, Object> mapModel = getDefaultModel(  );
        mapModel.put( MARK_INPUT, _input );
        mapModel.put( MARK_SPACES_TREE, _documentSpacesService.getSpacesTreeRoots( _user ) );
        mapModel.put( MARK_SELECTED_SPACE_ID, String.valueOf( nSpaceId ) );
        mapModel.put( MARK_DOCUMENT_TYPE, documentType.getCode(  ) );
        mapModel.put( MARK_SPACE, DocumentSpaceHome.findByPrimaryKey( nSpaceId ) );
        mapModel.put( MARK_MEDIA_TYPE, String.valueOf( mediaType.getMediaId(  ) ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_SPACE_SELECTOR, _user.getLocale(  ), toModel( model, mapModel ) );

        return template.getHtml(  );
    }

    /**
     * Creates and publishes a document from the form of its type
     * @param request The HTTP request, multipart
     * @param document the document to fill
     * @return The URL of the error message, null when the document is created
     */
    private String createDocument( HttpServletRequest request, Document document )
    {
        if ( !( request instanceof MultipartHttpServletRequest ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_NOT_AUTHORIZED, AdminMessage.TYPE_ERROR );
        }

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

        String strDocumentTypeCode = request.getParameter( PARAMETER_DOCUMENT_TYPE_CODE );
        int nSpaceId = NumberUtils.toInt( request.getParameter( PARAMETER_SPACE_ID ), -1 );

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
     * Creates the document of the media: an image is inserted at once into the editor, any other document shows the form of the
     * values of its media first
     * @param request The HTTP request, multipart: its action and its fields come in its body
     * @return the page inserting the image or the form of the values, rendered in the response to the form; or the redirection to the
     *         error message when the document can not be created
     */
    @Action( ACTION_CREATE_DOCUMENT )
    public String doCreateDocument( HttpServletRequest request )
    {
        initService( request );

        LibraryMedia mediaType = findMediaType( request );

        if ( ( mediaType == null ) || ( findDocumentType( mediaType ) == null ) )
        {
            return redirectView( request, VIEW_SELECT_SPACE );
        }

        Document document = new Document( );
        String strErrorUrl = createDocument( request, document );

        if ( strErrorUrl != null )
        {
            return redirect( request, strErrorUrl );
        }

        return isImage( mediaType ) ? getInsertMedia( request, mediaType, document ) : getEditSelectedMedia( mediaType, document );
    }

    /**
     * Inserts the document into the current editor, with the values of its media the form carries
     * @param request the request
     * @return the page inserting the document, rendered in the response to the form rather than reached through a redirect; it writes
     *         nothing, so it takes no token (the values page that posts it is sent by its own script)
     */
    @Action( value = ACTION_INSERT_MEDIA, securityTokenDisabled = true )
    public String doInsertMedia( HttpServletRequest request )
    {
        initService( request );

        LibraryMedia mediaType = findMediaType( request );

        if ( ( mediaType == null ) || ( findDocumentType( mediaType ) == null ) )
        {
            return redirectView( request, VIEW_SELECT_SPACE );
        }

        return getInsertMedia( request, mediaType, null );
    }

    /**
     * Tells whether a media type inserts an image, uploaded and inserted without the form of its values
     * @param mediaType the media type
     * @return true for the image media type
     */
    private static boolean isImage( LibraryMedia mediaType )
    {
        return Document.CODE_DOCUMENT_TYPE_IMAGE.equals( mediaType.getName(  ) );
    }

    /**
     * Returns the page inserting the document into the current editor
     * @param request the request
     * @param mediaType the media type, which has a mapping
     * @param document the document in case of "quick image insertion" without the "media_edition" page
     * @return the page inserting the document
     */
    private String getInsertMedia( HttpServletRequest request, LibraryMedia mediaType, Document document )
    {
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
     * @param mediaType the media type, which has a mapping
     * @param document the document just created
     * @return the form with document parameters
     */
    private String getEditSelectedMedia( LibraryMedia mediaType, Document document )
    {
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
