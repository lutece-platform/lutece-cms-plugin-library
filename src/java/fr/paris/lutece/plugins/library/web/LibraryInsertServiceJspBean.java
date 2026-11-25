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
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.web.insert.InsertServiceJspBean;
import fr.paris.lutece.portal.web.insert.InsertServiceSelectionBean;
import fr.paris.lutece.portal.web.util.LocalizedPaginator;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.html.AbstractPaginator;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;


/**
 * Library insert service JSP Bean
 */
@SessionScoped
@Named
public class LibraryInsertServiceJspBean extends InsertServiceJspBean implements InsertServiceSelectionBean
{
    private static final long serialVersionUID = -7071548876864568789L;

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
    private static final String PARAMETER_BROWSER_SPACE_ID = "browser_id_space";


    // JSP
    private static final String JSP_INSERT_MEDIA = "jsp/admin/plugins/library/DoInsertMedia.jsp";
    private static final String JSP_MEDIA_TYPE_SELECTION = "jsp/admin/plugins/library/SelectMedia.jsp";
    private static final String JSP_MEDIA_EDIT_SELECTED = "jsp/admin/plugins/library/EditSelectedMedia.jsp";

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
    private static final String MARK_PREVIEW_TYPE = "preview_type";
    private static final String MARK_PAGINATOR = "paginator";
    private static final String MARK_NB_ITEMS_PER_PAGE = "nb_items_per_page";
    private static final String MARK_SELECTED_MEDIAS = "selected_medias";
    private static final String MARK_ALL_MEDIA_ATTRIBUTES_ASSOCIATIONS = "all_attributes_associations";
    private static final String MARK_ALL_DOCUMENTS = "all_documents";
    private static final String MARK_MEDIA_DOC_LIST = "document_list";
    private static final String MARK_IS_DOCUMENT_CREATION_ALLOWED = "is_document_creation_allowed" ;

    // Constants
    private static final String DEFAULT_RESULTS_PER_PAGE = "10";
    private static final String PROPERTY_RESULTS_PER_PAGE = "library.nbdocsperpage";
    private static final String SPACE_ID_SESSION = "spaceIdSession";
    private static final String DEFAULT_PAGE_INDEX = "1";
    private static final int APPROVED_DOCUMENT_STATE = 3;

    @Inject
    private DocumentSpacesService _documentSpacesService;
    @Inject
    private LibraryMediaTemplateService _mediaTemplateService;

    // Local session variables
    private AdminUser _user;
    private Plugin _plugin;
    private String _input;
    private String _strCurrentPageIndex;
    private List<SelectedMedia> _listSelectedMedia;
    private int _nItemsPerPage;
    private int _nDefaultItemsPerPage;

    public String getInsertServiceSelectorUI( HttpServletRequest request )
    {
        init( request );
        _listSelectedMedia = null;

        return getMediaTypeSelectorPage(  );
    }

    /**
     * Returns the page choosing the media type, keeping the current selection
     * @return the page
     */
    private String getMediaTypeSelectorPage(  )
    {
        Collection<LibraryMedia> libraryMedia = LibraryMediaHome.findAllMedia( _plugin );
        HashMap<String, Object> model = getDefaultModel(  );
        model.put( MARK_MEDIA_TYPES, libraryMedia );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MEDIATYPE_SELECTOR, _user.getLocale(  ), model );

        return template.getHtml(  );
    }

    /**
     * Returns the page selecting the documents of a media type
     * @param request the request, carrying the media type
     * @return the page
     */
    public String getSelectMedia( HttpServletRequest request )
    {
        init( request );

        return getSelectMediaPage( request, request.getParameter( PARAMETER_MEDIA_TYPE ) );
    }

    /**
     * Returns the page selecting the documents of a media type
     * @param request the request
     * @param strMediaType the identifier of the media type
     * @return the page
     */
    private String getSelectMediaPage( HttpServletRequest request, String strMediaType )
    {
        // A missing or unknown media type (an editor whose insert service is not configured) leads back to the choice of the media type
        LibraryMedia mediaType = StringUtils.isNumeric( strMediaType )
            ? LibraryMediaHome.findByPrimaryKey( Integer.parseInt( strMediaType ), _plugin ) : null;

        if ( mediaType == null )
        {
            return getMediaTypeSelectorPage(  );
        }

        // The space chosen in the tree, else the space carried by the forms and the links of the page
        String strSpaceIdRequest = request.getParameter( DocumentSpacesService.PARAMETER_BROWSER_SELECTED_SPACE_ID );

        if ( StringUtils.isBlank( strSpaceIdRequest ) )
        {
            strSpaceIdRequest = request.getParameter( DocumentSpacesService.PARAMETER_BROWSER_SPACE_ID );
        }

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

        // get the selected space
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



        HashMap<String, Object> model = getDefaultModel(  );
        model.put( MARK_PREVIEW_TYPE, "<img src='%SRC' alt='%ALT' />" );
        model.put( MARK_MEDIA_TYPE, mediaType );
        // the media types, as tabs of the page
        model.put( MARK_MEDIA_TYPES, LibraryMediaHome.findAllMedia( _plugin ) );
        model.put( MARK_MEDIA_TYPE_ATTRIBUTES, mediaType.getMediaAttributeList(  ) );
        model.put( MARK_ALL_MEDIA_ATTRIBUTES_ASSOCIATIONS, mapAssociationAttributes );
        model.put( MARK_ALL_DOCUMENTS, listDocuments );
        model.put( MARK_SPACES_TREE, _documentSpacesService.getSpacesTreeRoots( _user ) );
        model.put( MARK_DOCUMENT_TYPES, listDocumentTypes );
        model.put( MARK_SELECTED_SPACE, nSpaceId );
        model.put( MARK_PAGINATOR, paginator );
        model.put( MARK_NB_ITEMS_PER_PAGE, Integer.toString( paginator.getItemsPerPage(  ) ) );
        model.put( MARK_SELECTED_MEDIAS, _listSelectedMedia );
        model.put( MARK_IS_DOCUMENT_CREATION_ALLOWED ,
                ( selectedSpace!=null?selectedSpace.isDocumentCreationAllowed():false ) ) ;
        HtmlTemplate template = AppTemplateService.getTemplate( mediaType.getIsMultipleMedia(  )
                ? TEMPLATE_MEDIA_SELECTOR_MULTIPLE : TEMPLATE_MEDIA_SELECTOR, _user.getLocale(  ), model );

        return template.getHtml(  );
    }

    public String getEditSelectedMedia( HttpServletRequest request )
    {
        init( request );

        String strMediaTypeId = request.getParameter( PARAMETER_MEDIA_ID );

        LibraryMedia mediaType = LibraryMediaHome.findByPrimaryKey( Integer.parseInt( strMediaTypeId ), _plugin );
        mediaType.setMediaAttributeList( MediaAttributeHome.findAllAttributesForMedia( mediaType.getMediaId(  ), _plugin ) );

        Map<String, Object> model = getDefaultModel(  );
        model.put( MARK_MEDIA_TYPE, mediaType );
        model.put( MARK_MEDIA_TYPE_ATTRIBUTES, mediaType.getMediaAttributeList(  ) );

        if ( mediaType.getIsMultipleMedia(  ) )
        {
            for ( SelectedMedia media : _listSelectedMedia )
            {
                LibraryMapping mapping = LibraryMappingHome.findByPrimaryKey( media.getMappingId(  ), _plugin );
                media.setAttributesFromMapping( getAttributesFromMapping( mapping ) );

                if ( media.getDocument(  ) == null )
                {
                    media.setDocument( DocumentHome.findByPrimaryKeyWithoutBinaries( media.getDocumentId(  ) ) );
                }
            }

            model.put( MARK_MEDIA_DOC_LIST, _listSelectedMedia );
        }
        else
        {
            String strDocumentId = request.getParameter( PARAMETER_DOCUMENT_ID );
            Document doc = DocumentHome.findByPrimaryKey( Integer.parseInt( strDocumentId ) );
            model.put( MARK_MEDIA_DOC, doc );

            String strMappingId = request.getParameter( PARAMETER_MAPPING_ID );
            LibraryMapping mapping = LibraryMappingHome.findByPrimaryKey( Integer.parseInt( strMappingId ), _plugin );
            model.put( MARK_MEDIA_ATTRIBUTES_ASSOCIATIONS, getAttributesFromMapping( mapping ) );
        }

        model.put( MARK_JSP_INSERT, JSP_INSERT_MEDIA );

        HtmlTemplate template = AppTemplateService.getTemplate( mediaType.getIsMultipleMedia(  )
                ? TEMPLATE_EDIT_MEDIA_MULTIPLE : TEMPLATE_EDIT_MEDIA, _user.getLocale(  ), model );

        return template.getHtml(  );
    }

    /**
     * Adds the documents selected on the screen to the selection of a multiple media type, or removes one from the selection
     * @param request the request, carrying the documents and their mappings (one id_document and one id_mapping per document) and the media type
     * @return the page selecting the documents, rendered in the response to the form rather than reached through a redirect
     */
    public String doSelectMedia( HttpServletRequest request )
    {
        init( request );

        String strDocumentId = request.getParameter( PARAMETER_DOCUMENT_ID );
        String strMediaTypeId = request.getParameter( PARAMETER_MEDIA_ID );

        if ( Boolean.parseBoolean( request.getParameter( PARAMETER_REMOVE_SELECTED ) ) )
        {
            int nIdDocument = Integer.parseInt( strDocumentId );

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

            return getSelectMediaPage( request, strMediaTypeId );
        }

        // The documents selected together come as many id_document and id_mapping, in the order of the grid
        String[] arrayDocumentId = request.getParameterValues( PARAMETER_DOCUMENT_ID );
        String[] arrayMappingId = request.getParameterValues( PARAMETER_MAPPING_ID );

        if ( ( arrayDocumentId == null ) || ( arrayMappingId == null ) || ( arrayDocumentId.length != arrayMappingId.length ) )
        {
            return getSelectMediaPage( request, strMediaTypeId );
        }

        if ( _listSelectedMedia == null )
        {
            _listSelectedMedia = new ArrayList<SelectedMedia>(  );
        }

        for ( int i = 0; i < arrayDocumentId.length; i++ )
        {
            // A document already in the selection is not added twice: the screen shows it as selected and lets it be chosen only once removed
            int nDocumentId = Integer.parseInt( arrayDocumentId[i] );

            if ( _listSelectedMedia.stream(  ).anyMatch( selected -> selected.getDocumentId(  ) == nDocumentId ) )
            {
                continue;
            }

            SelectedMedia media = new SelectedMedia( Integer.parseInt( strMediaTypeId ), nDocumentId,
                    Integer.parseInt( arrayMappingId[i] ) );
            media.setDocument( DocumentHome.findByPrimaryKeyWithoutBinaries( media.getDocumentId(  ) ) );
            media.setOrder( _listSelectedMedia.isEmpty(  ) ? 1 : ( _listSelectedMedia.get( _listSelectedMedia.size(  ) - 1 ).getOrder(  ) + 1 ) );
            _listSelectedMedia.add( media );
        }

        return getSelectMediaPage( request, strMediaTypeId );
    }

    public String addAllSpace( HttpServletRequest request )
    {
        init( request );

        String strMediaTypeId = request.getParameter( PARAMETER_MEDIA_ID );

        int nMediaTypeId = Integer.parseInt( strMediaTypeId );

        int nIdSpace = (Integer) request.getSession(  ).getAttribute( SPACE_ID_SESSION );

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

        LibraryMedia mediaType = LibraryMediaHome.findByPrimaryKey( nMediaTypeId, _plugin );
        Collection<LibraryMapping> allMappings = LibraryMappingHome.findAllMappingsByMedia( mediaType.getMediaId(  ),
                _plugin );

        for ( Document document : listDocuments )
        {
            document = DocumentHome.findByPrimaryKeyWithoutBinaries( document.getId(  ) );

            LibraryMapping selectedMapping = null;

            for ( LibraryMapping mapping : allMappings )
            {
                if ( StringUtils.equals( mapping.getCodeDocumentType(  ), document.getCodeDocumentType(  ) ) )
                {
                    selectedMapping = mapping;
                }
            }

            if ( selectedMapping != null )
            {
                SelectedMedia media = new SelectedMedia( nMediaTypeId, document.getId(  ),
                        selectedMapping.getIdMapping(  ) );
                media.setOrder( nCurrentOrder );
                media.setDocument( document );
                _listSelectedMedia.add( media );
            }
        }

        UrlItem urlItem = new UrlItem( AppPathService.getBaseUrl( request ) + JSP_MEDIA_EDIT_SELECTED );
        urlItem.addParameter( PARAMETER_MEDIA_ID, strMediaTypeId );

        return urlItem.getUrl(  );
    }

    public String doMoveMedia( HttpServletRequest request )
    {
        init( request );

        String strDocumentId = request.getParameter( PARAMETER_DOCUMENT_ID );
        String strMediaTypeId = request.getParameter( PARAMETER_MEDIA_ID );
        String strIdSpaceFilter = request.getParameter( DocumentSpacesService.PARAMETER_BROWSER_SELECTED_SPACE_ID );
        String strIdSpace = request.getParameter( PARAMETER_BROWSER_SPACE_ID );


        UrlItem urlItem = new UrlItem( AppPathService.getBaseUrl( request ) + JSP_MEDIA_TYPE_SELECTION );
        urlItem.addParameter( PARAMETER_MEDIA_TYPE, strMediaTypeId );
        urlItem.addParameter( DocumentSpacesService.PARAMETER_BROWSER_SELECTED_SPACE_ID, strIdSpaceFilter );
        urlItem.addParameter( PARAMETER_BROWSER_SPACE_ID, strIdSpace );


        boolean bMoveUp = Boolean.parseBoolean( request.getParameter( PARAMETER_MOVE_UP ) );

        int nIdDocument = Integer.parseInt( strDocumentId );

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

        return urlItem.getUrl(  );
    }

    /**
     * Renders the selected documents with the template of the media type, and inserts the HTML in the editor
     * @param request the request, carrying the value of each attribute of the media type (suffixed by the index of the document for a multiple media type)
     * @return the page inserting the HTML, rendered in the response to the form rather than reached through a redirect
     */
    public String doInsertMedia( HttpServletRequest request )
    {
        init( request );

        String strMediaTypeId = request.getParameter( PARAMETER_MEDIA_ID );
        LibraryMedia mediaType = LibraryMediaHome.findByPrimaryKey( Integer.parseInt( strMediaTypeId ), _plugin );
        mediaType.setMediaAttributeList( MediaAttributeHome.findAllAttributesForMedia( mediaType.getMediaId(  ), _plugin ) );

        int nMediaCount = mediaType.getIsMultipleMedia(  ) ? _listSelectedMedia.size(  ) : 1;
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

        // We reset session parameters
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

    private void init( HttpServletRequest request )
    {
        String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
        _user = AdminUserService.getAdminUser( request );

        if ( StringUtils.isNotBlank( strPluginName ) )
        {
            _plugin = PluginService.getPlugin( strPluginName );
        }

        String strInput = request.getParameter( PARAMETER_INPUT );

        if ( StringUtils.isNotBlank( strInput ) )
        {
            _input = strInput;
        }
    }

    private HashMap<String, Object> getDefaultModel(  )
    {
        HashMap<String, Object> model = new HashMap<String, Object>(  );
        model.put( PARAMETER_PLUGIN_NAME, _plugin.getName(  ) );
        model.put( PARAMETER_INPUT, _input );

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
        UrlItem url = new UrlItem( JSP_MEDIA_TYPE_SELECTION );
        url.addParameter( PARAMETER_INPUT, _input );
        url.addParameter( PARAMETER_PLUGIN_NAME, _plugin.getName(  ) );
        url.addParameter( PARAMETER_SPACE_ID, selectedSpace );
        url.addParameter( PARAMETER_MEDIA_TYPE, strMediaType );
        url.addParameter( PARAMETER_NB_ITEMS_PER_PAGE, _nItemsPerPage );

        return url.getUrl(  );
    }

    private LocalizedPaginator<Integer> getPaginator( HttpServletRequest request,
    		List<Integer> listDocumentsId, String baseUrl )
    {
        _nDefaultItemsPerPage = AppPropertiesService.getPropertyInt( PROPERTY_RESULTS_PER_PAGE, 10 );
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
