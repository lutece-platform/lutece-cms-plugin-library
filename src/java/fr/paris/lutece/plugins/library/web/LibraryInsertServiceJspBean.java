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
import fr.paris.lutece.plugins.document.business.DocumentFilter;
import fr.paris.lutece.plugins.document.business.DocumentHome;
import fr.paris.lutece.plugins.document.business.attributes.DocumentAttributeHome;
import fr.paris.lutece.plugins.document.business.spaces.DocumentSpace;
import fr.paris.lutece.plugins.document.service.spaces.DocumentSpacesService;
import fr.paris.lutece.plugins.library.business.LibraryMapping;
import fr.paris.lutece.plugins.library.business.LibraryMapping.AttributeAssociation;
import fr.paris.lutece.plugins.library.business.LibraryMappingHome;
import fr.paris.lutece.plugins.library.business.LibraryMedia;
import fr.paris.lutece.plugins.library.business.LibraryMediaHome;
import fr.paris.lutece.plugins.library.business.MediaAttribute;
import fr.paris.lutece.plugins.library.business.MediaAttributeHome;
import fr.paris.lutece.plugins.library.business.SelectedMedia;
import fr.paris.lutece.plugins.library.service.LibraryMediaTemplateService;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.web.cdi.mvc.Models;
import fr.paris.lutece.portal.web.insert.InsertServiceSelectionBean;
import fr.paris.lutece.portal.web.insert.InsertServiceSelectorJspBean;
import fr.paris.lutece.portal.web.util.LocalizedPaginator;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.html.AbstractPaginator;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import org.eclipse.microprofile.config.inject.ConfigProperty;


/**
 * Insert service of the library: the choice of a media type, of its documents in the spaces, of the values of the media, then
 * the insertion into the editor of the HTML its template renders. The core instantiates the class by reflection for its first
 * screen (getInsertServiceSelectorUI), the screens after it are the views and actions of this controller.
 */
@SessionScoped
@Named
@Controller( controllerJsp = LibraryInsertServiceJspBean.CONTROLLER_JSP, controllerPath = LibraryInsertServiceJspBean.CONTROLLER_PATH, right = InsertServiceSelectorJspBean.RIGHT_MANAGE_LINK_SERVICE, securityTokenEnabled = true )
public class LibraryInsertServiceJspBean extends MVCAdminJspBean implements InsertServiceSelectionBean
{
    static final String CONTROLLER_JSP = "SelectMedia.jsp";
    static final String CONTROLLER_PATH = "jsp/admin/plugins/library/";
    private static final long serialVersionUID = -7071548876864568789L;
    private static final String PLUGIN_NAME = "library";

    // Views
    private static final String VIEW_SELECT_MEDIA = "selectMedia";
    private static final String VIEW_EDIT_SELECTED_MEDIA = "editSelectedMedia";

    // Actions
    private static final String ACTION_SELECT_DOCUMENT = "selectDocument";
    private static final String ACTION_INSERT_MEDIA = "insertMedia";

    // Templates
    private static final String TEMPLATE_MEDIATYPE_SELECTOR = "admin/plugins/library/mediatype_selector.html";
    private static final String TEMPLATE_MEDIA_SELECTOR = "admin/plugins/library/media_selector.html";
    private static final String TEMPLATE_MEDIA_SELECTOR_MULTIPLE = "admin/plugins/library/media_selector_multiple.html";
    private static final String TEMPLATE_EDIT_MEDIA = "admin/plugins/library/mediaedition_selector.html";
    private static final String TEMPLATE_EDIT_MEDIA_MULTIPLE = "admin/plugins/library/mediaedition_selector_multiple.html";

    // Parameters
    private static final String PARAMETER_PLUGIN_NAME = "plugin_name";
    private static final String PARAMETER_MEDIA_TYPE = "media_type";
    private static final String PARAMETER_MEDIA_ID = "media";
    private static final String PARAMETER_DOCUMENT_ID = "id_document";
    private static final String PARAMETER_SPACE_ID = "space_id";
    private static final String PARAMETER_INPUT = "input";
    private static final String PARAMETER_MAPPING_ID = "id_mapping";
    private static final String PARAMETER_NB_ITEMS_PER_PAGE = "nb_items";
    private static final String PARAMETER_PAGE_INDEX = "page_index";
    private static final String PARAMETER_REMOVE_SELECTED = "remove_selected";
    private static final String PARAMETER_MOVE_UP = "move_up";
    private static final String PARAMETER_ADD_ALL = "add_all";


    // JSP
    private static final String JSP_CONTROLLER = CONTROLLER_PATH + CONTROLLER_JSP;

    // Marks
    private static final String MARK_MEDIA_DOC = "document";
    private static final String MARK_JSP_INSERT = "jsp_insert";
    private static final String MARK_MEDIA_TYPES = "mediatypes";
    private static final String MARK_MEDIA_TYPE = "mediatype";
    private static final String MARK_MEDIA_TYPE_ATTRIBUTES = "media_attributes";
    private static final String MARK_MEDIA_ATTRIBUTES_ASSOCIATIONS = "attributes_associations";
    private static final String MARK_SPACES_TREE = "spaces_tree";
    private static final String MARK_DOCUMENT_TYPES = "document_types";
    private static final String MARK_SELECTED_SPACE = "selected_space";
    private static final String MARK_PAGINATOR = "paginator";
    private static final String MARK_NB_ITEMS_PER_PAGE = "nb_items_per_page";
    private static final String MARK_SELECTED_MEDIAS = "selected_medias";
    private static final String MARK_ALL_MEDIA_ATTRIBUTES_ASSOCIATIONS = "all_attributes_associations";
    private static final String MARK_ALL_DOCUMENTS = "all_documents";
    private static final String MARK_MEDIA_DOC_LIST = "document_list";
    private static final String MARK_IS_DOCUMENT_CREATION_ALLOWED = "is_document_creation_allowed" ;

    // Constants
    private static final String SPACE_ID_SESSION = "spaceIdSession";
    private static final String DEFAULT_PAGE_INDEX = "1";
    private static final int APPROVED_DOCUMENT_STATE = 3;

    @Inject
    private DocumentSpacesService _documentSpacesService;
    @Inject
    private LibraryMediaTemplateService _mediaTemplateService;

    // Local session variables
    private transient AdminUser _user;
    private transient Plugin _plugin;
    private String _input;
    private String _strCurrentPageIndex;
    private List<SelectedMedia> _listSelectedMedia;
    private int _nItemsPerPage;
    @Inject
    @ConfigProperty( name = "library.nbdocsperpage", defaultValue = "10" )
    private int _nDefaultItemsPerPage;

    /**
     * {@inheritDoc}
     * The core instantiates the bean by reflection to show the first screen: the choice of the media type
     */
    @Override
    public String getInsertServiceSelectorUI( HttpServletRequest request )
    {
        initService( request );
        _listSelectedMedia = null;

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
     * Returns the page choosing the media type, keeping the current selection
     * @param model the model of the page, null when the core asks for the first screen outside the controller
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
     * Returns the page selecting the documents of a media type; a missing or unknown media type (an editor whose insert service
     * is not configured) leads back to the choice of the media type. Its token is the one of the selection of a document.
     * @param model the model of the page
     * @param request the request, carrying the media type
     * @return the page
     */
    @View( value = VIEW_SELECT_MEDIA, defaultView = true, securityTokenAction = ACTION_SELECT_DOCUMENT )
    public String getSelectMedia( Models model, HttpServletRequest request )
    {
        initService( request );

        String strMediaType = request.getParameter( PARAMETER_MEDIA_TYPE );
        LibraryMedia mediaType = StringUtils.isNumeric( strMediaType )
            ? LibraryMediaHome.findByPrimaryKey( Integer.parseInt( strMediaType ), _plugin ) : null;

        if ( mediaType == null )
        {
            return getMediaTypeSelectorPage( model );
        }

        String strSpaceIdRequest = request.getParameter( DocumentSpacesService.PARAMETER_BROWSER_SELECTED_SPACE_ID );

        if ( StringUtils.isBlank( strSpaceIdRequest ) )
        {
            strSpaceIdRequest = request.getParameter( DocumentSpacesService.PARAMETER_BROWSER_SPACE_ID );
        }

        int nSpaceId = -1;

        if ( NumberUtils.isDigits( strSpaceIdRequest ) )
        {
            nSpaceId = NumberUtils.toInt( strSpaceIdRequest, -1 );
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

        mediaType.setMediaAttributeList( MediaAttributeHome.findAllAttributesForMedia( mediaType.getMediaId(  ), _plugin ) );

        Collection<LibraryMapping> allMappings = LibraryMappingHome.findAllMappingsByMedia( mediaType.getMediaId(  ),
                _plugin );
        Collection<DocumentSpace> spaces = _documentSpacesService.getUserAllowedSpaces( _user );

        List<Pair<String, Document>> listDocuments = new ArrayList<Pair<String, Document>>(  );
        HashMap<String, Map<String, String>> mapAssociationAttributes = new HashMap<String, Map<String, String>>(  );
        Map<Integer, String> mapDocumentMapping = new LinkedHashMap<>(  );
        List<String> listDocumentTypes = new ArrayList<>(  );

        for ( LibraryMapping mapping : allMappings )
        {
            for ( Integer nIdDocument : getIdDocumentsFromMapping( mapping, nSpaceId ) )
            {
                mapDocumentMapping.putIfAbsent( nIdDocument, String.valueOf( mapping.getIdMapping(  ) ) );
            }

            mapAssociationAttributes.put( String.valueOf( mapping.getIdMapping(  ) ),
                getAttributesFromMapping( mapping ) );
            listDocumentTypes.add( mapping.getCodeDocumentType(  ) );
        }

        List<Integer> listDocumentsId = new ArrayList<>( mapDocumentMapping.keySet(  ) );

        String idSpace = Integer.toString(nSpaceId);
        String baseUrl = getBaseUrl( strMediaType, idSpace );
        LocalizedPaginator<Integer> paginator = getPaginator( request, listDocumentsId, baseUrl );

        for ( Integer nIdDocument : paginator.getPageItems(  ) )
        {
            Document document = DocumentHome.findByPrimaryKey( nIdDocument );

            if ( document != null )
            {
                listDocuments.add( new Pair<String, Document>( mapDocumentMapping.get( nIdDocument ), document ) );
            }
        }

        DocumentSpace selectedSpace = null ;
        if ( nSpaceId > 0 )
        {
            for ( DocumentSpace space : spaces )
            {
                if ( space.getId() == nSpaceId )
                {
                    selectedSpace = space ;
                    break ;
                }
            }
        }



        Map<String, Object> mapModel = getDefaultModel(  );
        mapModel.put( MARK_MEDIA_TYPE, mediaType );
        mapModel.put( MARK_MEDIA_TYPES, LibraryMediaHome.findAllMedia( _plugin ) );
        mapModel.put( MARK_MEDIA_TYPE_ATTRIBUTES, mediaType.getMediaAttributeList(  ) );
        mapModel.put( MARK_ALL_MEDIA_ATTRIBUTES_ASSOCIATIONS, mapAssociationAttributes );
        mapModel.put( MARK_ALL_DOCUMENTS, listDocuments );
        mapModel.put( MARK_SPACES_TREE, _documentSpacesService.getSpacesTreeRoots( _user ) );
        mapModel.put( MARK_DOCUMENT_TYPES, listDocumentTypes );
        mapModel.put( MARK_SELECTED_SPACE, nSpaceId );
        mapModel.put( MARK_PAGINATOR, paginator );
        mapModel.put( MARK_NB_ITEMS_PER_PAGE, Integer.toString( paginator.getItemsPerPage(  ) ) );
        mapModel.put( MARK_SELECTED_MEDIAS, _listSelectedMedia );
        mapModel.put( MARK_IS_DOCUMENT_CREATION_ALLOWED, ( selectedSpace != null ) && selectedSpace.isDocumentCreationAllowed(  ) );
        HtmlTemplate template = AppTemplateService.getTemplate( mediaType.getIsMultipleMedia(  )
                ? TEMPLATE_MEDIA_SELECTOR_MULTIPLE : TEMPLATE_MEDIA_SELECTOR, _user.getLocale(  ), toModel( model, mapModel ) );

        return template.getHtml(  );
    }

    /**
     * Returns the values of the media for the selected documents, a form the page sends at once to the insertion
     * @param model the model of the page
     * @param request the request, carrying the media type, and for a single media its document and its mapping
     * @return the page, or the choice of the media type when the media type, the document or the selection is missing
     */
    @View( value = VIEW_EDIT_SELECTED_MEDIA, securityTokenAction = ACTION_INSERT_MEDIA )
    public String getEditSelectedMedia( Models model, HttpServletRequest request )
    {
        initService( request );

        LibraryMedia mediaType = findMediaType( request );

        if ( mediaType == null )
        {
            return getMediaTypeSelectorPage( model );
        }

        mediaType.setMediaAttributeList( MediaAttributeHome.findAllAttributesForMedia( mediaType.getMediaId(  ), _plugin ) );

        Map<String, Object> mapModel = getDefaultModel(  );
        mapModel.put( MARK_MEDIA_TYPE, mediaType );
        mapModel.put( MARK_MEDIA_TYPE_ATTRIBUTES, mediaType.getMediaAttributeList(  ) );

        if ( mediaType.getIsMultipleMedia(  ) )
        {
            if ( ( _listSelectedMedia == null ) || _listSelectedMedia.isEmpty(  ) )
            {
                return redirect( request, VIEW_SELECT_MEDIA, PARAMETER_MEDIA_TYPE, mediaType.getMediaId(  ) );
            }

            for ( SelectedMedia media : _listSelectedMedia )
            {
                LibraryMapping mapping = LibraryMappingHome.findByPrimaryKey( media.getMappingId(  ), _plugin );
                media.setAttributesFromMapping( getAttributesFromMapping( mapping ) );

                if ( media.getDocument(  ) == null )
                {
                    media.setDocument( DocumentHome.findByPrimaryKeyWithoutBinaries( media.getDocumentId(  ) ) );
                }
            }

            mapModel.put( MARK_MEDIA_DOC_LIST, _listSelectedMedia );
        }
        else
        {
            Document doc = DocumentHome.findByPrimaryKey( NumberUtils.toInt( request.getParameter( PARAMETER_DOCUMENT_ID ), -1 ) );
            LibraryMapping mapping = LibraryMappingHome.findByPrimaryKey( NumberUtils.toInt( request.getParameter( PARAMETER_MAPPING_ID ), -1 ), _plugin );

            if ( ( doc == null ) || ( mapping == null ) )
            {
                return redirect( request, VIEW_SELECT_MEDIA, PARAMETER_MEDIA_TYPE, mediaType.getMediaId(  ) );
            }

            mapModel.put( MARK_MEDIA_DOC, doc );
            mapModel.put( MARK_MEDIA_ATTRIBUTES_ASSOCIATIONS, getAttributesFromMapping( mapping ) );
        }

        mapModel.put( MARK_JSP_INSERT, JSP_CONTROLLER );

        HtmlTemplate template = AppTemplateService.getTemplate( mediaType.getIsMultipleMedia(  )
                ? TEMPLATE_EDIT_MEDIA_MULTIPLE : TEMPLATE_EDIT_MEDIA, _user.getLocale(  ), toModel( model, mapModel ) );

        return template.getHtml(  );
    }

    /**
     * Changes the selection of a multiple media type, the one action every form of the selection page posts (the token of the page is for
     * it): adds every document of the space browsed (add_all), moves a document (move_up), removes one (remove_selected), or adds the
     * documents selected on the screen, in the order of the grid; a document already in the selection is not added twice
     * @param request the request, carrying the documents and their mappings (one id_document and one id_mapping per document) and the media type
     * @return the redirection to the page selecting the documents
     */
    @Action( ACTION_SELECT_DOCUMENT )
    public String doSelectDocument( HttpServletRequest request )
    {
        initService( request );

        LibraryMedia mediaType = findMediaType( request );

        if ( mediaType == null )
        {
            return redirectView( request, VIEW_SELECT_MEDIA );
        }

        if ( Boolean.parseBoolean( request.getParameter( PARAMETER_ADD_ALL ) ) )
        {
            return addAllSpace( request, mediaType );
        }

        if ( request.getParameter( PARAMETER_MOVE_UP ) != null )
        {
            return moveDocument( request, mediaType );
        }

        if ( Boolean.parseBoolean( request.getParameter( PARAMETER_REMOVE_SELECTED ) ) )
        {
            int nIdDocument = NumberUtils.toInt( request.getParameter( PARAMETER_DOCUMENT_ID ), -1 );

            if ( ( _listSelectedMedia != null ) && ( _listSelectedMedia.size(  ) > 0 ) )
            {
                for ( SelectedMedia media : _listSelectedMedia )
                {
                    if ( media.getDocumentId(  ) == nIdDocument )
                    {
                        _listSelectedMedia.remove( media );

                        break;
                    }
                }

                if ( _listSelectedMedia.size(  ) > 0 )
                {
                    Collections.sort( _listSelectedMedia );

                    int nOrder = 1;

                    for ( SelectedMedia media : _listSelectedMedia )
                    {
                        media.setOrder( nOrder++ );
                    }
                }
                else
                {
                    _listSelectedMedia = null;
                }
            }

            return redirect( request, VIEW_SELECT_MEDIA, PARAMETER_MEDIA_TYPE, mediaType.getMediaId(  ) );
        }

        String[] arrayDocumentId = request.getParameterValues( PARAMETER_DOCUMENT_ID );
        String[] arrayMappingId = request.getParameterValues( PARAMETER_MAPPING_ID );

        if ( ( arrayDocumentId == null ) || ( arrayMappingId == null ) || ( arrayDocumentId.length != arrayMappingId.length ) )
        {
            return redirect( request, VIEW_SELECT_MEDIA, PARAMETER_MEDIA_TYPE, mediaType.getMediaId(  ) );
        }

        if ( _listSelectedMedia == null )
        {
            _listSelectedMedia = new ArrayList<SelectedMedia>(  );
        }

        for ( int i = 0; i < arrayDocumentId.length; i++ )
        {
            int nDocumentId = NumberUtils.toInt( arrayDocumentId[i], -1 );
            int nMappingId = NumberUtils.toInt( arrayMappingId[i], -1 );
            Document document = DocumentHome.findByPrimaryKeyWithoutBinaries( nDocumentId );

            if ( ( document == null ) || ( nMappingId < 0 ) ||
                    _listSelectedMedia.stream(  ).anyMatch( selected -> selected.getDocumentId(  ) == nDocumentId ) )
            {
                continue;
            }

            SelectedMedia media = new SelectedMedia( mediaType.getMediaId(  ), nDocumentId, nMappingId );
            media.setDocument( document );
            media.setOrder( _listSelectedMedia.isEmpty(  ) ? 1 : ( _listSelectedMedia.get( _listSelectedMedia.size(  ) - 1 ).getOrder(  ) + 1 ) );
            _listSelectedMedia.add( media );
        }

        return redirect( request, VIEW_SELECT_MEDIA, PARAMETER_MEDIA_TYPE, mediaType.getMediaId(  ) );
    }

    /**
     * Adds to the selection every document of the space browsed whose type has a mapping for the media type, then goes on to the
     * values of the media
     * @param request the request
     * @param mediaType the media type
     * @return the redirection to the values of the media, or to the selection when no space is browsed
     */
    private String addAllSpace( HttpServletRequest request, LibraryMedia mediaType )
    {
        Integer nSpaceIdSession = (Integer) request.getSession(  ).getAttribute( SPACE_ID_SESSION );

        if ( nSpaceIdSession == null )
        {
            return redirect( request, VIEW_SELECT_MEDIA, PARAMETER_MEDIA_TYPE, mediaType.getMediaId(  ) );
        }

        int nMediaTypeId = mediaType.getMediaId(  );
        int nIdSpace = nSpaceIdSession;

        List<Document> listDocuments = DocumentHome.findBySpaceKey( nIdSpace );
        int nCurrentOrder;

        if ( ( _listSelectedMedia == null ) || ( _listSelectedMedia.size(  ) == 0 ) )
        {
            _listSelectedMedia = new ArrayList<SelectedMedia>(  );
            nCurrentOrder = 1;
        }
        else
        {
            nCurrentOrder = _listSelectedMedia.get( _listSelectedMedia.size(  ) - 1 ).getOrder(  ) + 1;
        }

        Collection<LibraryMapping> allMappings = LibraryMappingHome.findAllMappingsByMedia( nMediaTypeId, _plugin );

        for ( Document document : listDocuments )
        {
            document = DocumentHome.findByPrimaryKeyWithoutBinaries( document.getId(  ) );

            LibraryMapping selectedMapping = null;

            for ( LibraryMapping mapping : allMappings )
            {
                if ( Objects.equals( mapping.getCodeDocumentType(  ), document.getCodeDocumentType(  ) ) )
                {
                    selectedMapping = mapping;
                }
            }

            if ( selectedMapping != null )
            {
                SelectedMedia media = new SelectedMedia( nMediaTypeId, document.getId(  ),
                        selectedMapping.getIdMapping(  ) );
                media.setOrder( nCurrentOrder++ );
                media.setDocument( document );
                _listSelectedMedia.add( media );
            }
        }

        return redirect( request, VIEW_EDIT_SELECTED_MEDIA, PARAMETER_MEDIA_ID, nMediaTypeId );
    }

    /**
     * Moves a document of the selection up or down
     * @param request the request, carrying the document and the direction
     * @param mediaType the media type
     * @return the redirection to the page selecting the documents
     */
    private String moveDocument( HttpServletRequest request, LibraryMedia mediaType )
    {
        if ( _listSelectedMedia == null )
        {
            return redirect( request, VIEW_SELECT_MEDIA, PARAMETER_MEDIA_TYPE, mediaType.getMediaId(  ) );
        }

        boolean bMoveUp = Boolean.parseBoolean( request.getParameter( PARAMETER_MOVE_UP ) );

        int nIdDocument = NumberUtils.toInt( request.getParameter( PARAMETER_DOCUMENT_ID ), -1 );

        if ( bMoveUp )
        {
            SelectedMedia previousMedia = null;

            for ( SelectedMedia media : _listSelectedMedia )
            {
                if ( media.getDocumentId(  ) == nIdDocument )
                {
                    if ( previousMedia != null )
                    {
                        int nMediaOrder = media.getOrder(  );
                        media.setOrder( previousMedia.getOrder(  ) );
                        previousMedia.setOrder( nMediaOrder );
                    }

                    break;
                }

                previousMedia = media;
            }
        }
        else
        {
            SelectedMedia currentMedia = null;

            for ( SelectedMedia nextMedia : _listSelectedMedia )
            {
                if ( ( currentMedia != null ) && ( currentMedia.getDocumentId(  ) == nIdDocument ) )
                {
                    int nMediaOrder = nextMedia.getOrder(  );
                    nextMedia.setOrder( currentMedia.getOrder(  ) );
                    currentMedia.setOrder( nMediaOrder );

                    break;
                }

                currentMedia = nextMedia;
            }
        }

        Collections.sort( _listSelectedMedia );

        return redirect( request, VIEW_SELECT_MEDIA, PARAMETER_MEDIA_TYPE, mediaType.getMediaId(  ) );
    }

    /**
     * Renders the selected documents with the template of the media type, and inserts the HTML in the editor
     * @param request the request, carrying the value of each attribute of the media type (suffixed by the index of the document for a multiple media type)
     * @return the page inserting the HTML, rendered in the response to the form rather than reached through a redirect; it writes
     *         nothing, so it takes no token (the values page that posts it is sent by its own script, and by the upload service too)
     */
    @Action( value = ACTION_INSERT_MEDIA, securityTokenDisabled = true )
    public String doInsertMedia( HttpServletRequest request )
    {
        initService( request );

        LibraryMedia mediaType = findMediaType( request );

        if ( mediaType == null )
        {
            return redirectView( request, VIEW_SELECT_MEDIA );
        }

        mediaType.setMediaAttributeList( MediaAttributeHome.findAllAttributesForMedia( mediaType.getMediaId(  ), _plugin ) );

        int nMediaCount = mediaType.getIsMultipleMedia(  ) ? ( ( _listSelectedMedia == null ) ? 0 : _listSelectedMedia.size(  ) ) : 1;
        List<Map<String, String>> listMedia = new ArrayList<>(  );

        for ( int i = 0; i < nMediaCount; i++ )
        {
            String strSuffix = mediaType.getIsMultipleMedia(  ) ? String.valueOf( i ) : StringUtils.EMPTY;
            Map<String, String> mapValues = new HashMap<>(  );

            for ( MediaAttribute attribute : mediaType.getMediaAttributeList(  ) )
            {
                mapValues.put( attribute.getCode(  ), LibraryInsertPage.getMediaValue( request, attribute.getCode(  ) + strSuffix ) );
            }

            listMedia.add( mapValues );
        }

        String strHtml = _mediaTemplateService.render( mediaType, listMedia, _user.getLocale(  ) );

        _nItemsPerPage = 0;
        _strCurrentPageIndex = "";
        _listSelectedMedia = null;

        return LibraryInsertPage.getHtml( _input, strHtml, _user.getLocale(  ) );
    }

    /**
     * Returns the identifiers of the approved documents of a space that have the document type of a mapping
     * @param m the mapping
     * @param nSpaceId the identifier of the space, negative when no space is selected yet
     * @return the identifiers, empty when no space is selected or when the user may not view the space
     */
    private List<Integer> getIdDocumentsFromMapping( LibraryMapping m, int nSpaceId )
    {
            DocumentFilter filter = new DocumentFilter(  );
            filter.setCodeDocumentType( m.getCodeDocumentType(  ) );
            filter.setIdState( APPROVED_DOCUMENT_STATE );

            List<Integer> documents = Collections.emptyList(  );

            if ( nSpaceId >= 0 )
            {
                if ( !_documentSpacesService.isAuthorizedViewByRole( nSpaceId, _user ) ||
                        !_documentSpacesService.isAuthorizedViewByWorkgroup( nSpaceId, _user ) )
                {
                    return Collections.emptyList(  );
                }

                filter.setIdSpace( nSpaceId );
                documents = (List<Integer>) DocumentHome.findPrimaryKeysByFilter(filter,  _user.getLocale(  ) );
            }

           return documents;
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

    /**
     * Sets the user, the plugin and the editor field of the insertion; the field is kept in the session for the screens after the first
     * @param request the request
     */
    private void initService( HttpServletRequest request )
    {
        _user = AdminUserService.getAdminUser( request );
        _plugin = PluginService.getPlugin( PLUGIN_NAME );

        String strInput = request.getParameter( PARAMETER_INPUT );

        if ( StringUtils.isNotBlank( strInput ) )
        {
            _input = strInput;
        }
    }

    /**
     * Returns the media type the request names by its media_type, else by its media
     * @param request the request
     * @return the media type, null when the identifier is missing, not a number or unknown
     */
    private LibraryMedia findMediaType( HttpServletRequest request )
    {
        String strMediaTypeId = StringUtils.defaultIfBlank( request.getParameter( PARAMETER_MEDIA_ID ), request.getParameter( PARAMETER_MEDIA_TYPE ) );
        int nMediaTypeId = NumberUtils.toInt( strMediaTypeId, -1 );

        return ( nMediaTypeId < 0 ) ? null : LibraryMediaHome.findByPrimaryKey( nMediaTypeId, _plugin );
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
     * with the values of the page; the values alone for the first screen, which the core asks for outside the controller
     * @param model the model of the view, null outside the controller
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
     * Build baseurl for action
     *
     * @param request The HTTP request
     * @param selectedSpace The selected space
     * @return baseURL
     */
    private String getBaseUrl( String strMediaType, String selectedSpace )
    {
        UrlItem url = new UrlItem( JSP_CONTROLLER );
        url.addParameter( PARAMETER_INPUT, _input );
        url.addParameter( PARAMETER_PLUGIN_NAME, PLUGIN_NAME );
        url.addParameter( PARAMETER_SPACE_ID, selectedSpace );
        url.addParameter( PARAMETER_MEDIA_TYPE, strMediaType );
        url.addParameter( PARAMETER_NB_ITEMS_PER_PAGE, _nItemsPerPage );

        return url.getUrl(  );
    }

    private LocalizedPaginator<Integer> getPaginator( HttpServletRequest request,
    		List<Integer> listDocumentsId, String baseUrl )
    {
        _nItemsPerPage = AbstractPaginator.getItemsPerPage( request, AbstractPaginator.PARAMETER_ITEMS_PER_PAGE, _nItemsPerPage, _nDefaultItemsPerPage );

        _strCurrentPageIndex = AbstractPaginator.getPageIndex( request, AbstractPaginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );

        if ( StringUtils.isNotEmpty( _strCurrentPageIndex ) && StringUtils.isNumeric( _strCurrentPageIndex ) )
        {
            _strCurrentPageIndex = _strCurrentPageIndex;
        }

        if ( StringUtils.isEmpty( _strCurrentPageIndex ) )
        {
            _strCurrentPageIndex = DEFAULT_PAGE_INDEX;
        }

        LocalizedPaginator<Integer> paginator = new LocalizedPaginator<Integer>( (List<Integer>) listDocumentsId, _nItemsPerPage, baseUrl,
                AbstractPaginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex, _user.getLocale(  ) );
        return paginator;
    }

    public class Pair<X, Y>
    {
        private X first;
        private Y second;

        public Pair( X x, Y y )
        {
            first = x;
            second = y;
        }

        public X getFirst(  )
        {
            return first;
        }

        public Y getSecond(  )
        {
            return second;
        }
    }
}
