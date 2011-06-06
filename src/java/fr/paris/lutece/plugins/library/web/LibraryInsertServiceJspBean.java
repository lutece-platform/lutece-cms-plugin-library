/*
 * Copyright (c) 2002-2011, Mairie de Paris
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
import fr.paris.lutece.plugins.document.business.spaces.DocumentSpaceHome;
import fr.paris.lutece.plugins.document.service.spaces.DocumentSpacesService;
import fr.paris.lutece.plugins.library.business.LibraryMapping;
import fr.paris.lutece.plugins.library.business.LibraryMapping.AttributeAssociation;
import fr.paris.lutece.plugins.library.business.LibraryMappingHome;
import fr.paris.lutece.plugins.library.business.LibraryMedia;
import fr.paris.lutece.plugins.library.business.LibraryMediaHome;
import fr.paris.lutece.plugins.library.business.MediaAttribute;
import fr.paris.lutece.plugins.library.business.MediaAttributeHome;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.html.XmlTransformerService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.web.insert.InsertServiceJspBean;
import fr.paris.lutece.portal.web.insert.InsertServiceSelectionBean;
import fr.paris.lutece.util.UniqueIDGenerator;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.html.Paginator;
import fr.paris.lutece.util.url.UrlItem;
import fr.paris.lutece.util.xml.XmlUtil;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


public class LibraryInsertServiceJspBean extends InsertServiceJspBean implements InsertServiceSelectionBean
{
    private static final long serialVersionUID = -7071548876864568789L;
    private static final String PARAMETER_PLUGIN_NAME = "plugin_name";
    private static final String MARK_MEDIA_TYPES = "mediatypes";
    private static final String TEMPLATE_MEDIATYPE_SELECTOR = "admin/plugins/library/mediatype_selector.html";
    private static final String TEMPLATE_MEDIA_SELECTOR = "admin/plugins/library/media_selector.html";
    private static final String PARAMETER_MEDIA_TYPE = "media_type";
    private static final String MARK_MEDIA_TYPE = "mediatype";
    private static final String MARK_MEDIA_TYPE_ATTRIBUTES = "media_attributes";
    private static final String MARK_MEDIA_ATTRIBUTES_ASSOCIATIONS = "attributes_associations";
    private static final String TEMPLATE_EDIT_MEDIA = "admin/plugins/library/mediaedition_selector.html";
    private static final String PARAMETER_MEDIA_ID = "media";
    private static final String PARAMETER_DOCUMENT_ID = "id_document";
    private static final String MARK_MEDIA_DOC = "document";
    private static final String MARK_JSP_INSERT = "jsp_insert";
    
    //JSP
    private static final String JSP_INSERT_MEDIA = "jsp/admin/plugins/library/DoInsertMedia.jsp";
    private static final String JSP_MEDIA_TYPE_SELECTION = "jsp/admin/plugins/library/SelectMedia.jsp";
    
    //private static final String MARK_SPACES = "spaces";
    private static final String MARK_SPACES_BROWSER = "spaces_browser";
    private static final String PARAMETER_SPACE_ID = "space_id";
    private static final String MARK_SELECTED_SPACE = "selected_space";
    private static final String MARK_PREVIEW_TYPE = "preview_type";
    private static final String PARAMETER_INPUT = "input";
    private static final String XML_TAG_MEDIA = "media";
    private static final String MARK_ALL_MEDIA_ATTRIBUTES_ASSOCIATIONS = "all_attributes_associations";
    private static final String MARK_ALL_DOCUMENTS = "all_documents";
    private static final String PARAMETER_MAPPING_ID = "id_mapping";
    private static final int APPROVED_DOCUMENT_STATE = 3;
    private static final String DEFAULT_RESULTS_PER_PAGE = "10";
    private static final String PARAMETER_NB_ITEMS_PER_PAGE = "nb_items";
    private static final String PROPERTY_RESULTS_PER_PAGE = "library.nbdocsperpage";
    private static final String PARAMETER_PAGE_INDEX = "page_index";
    private static final String DEFAULT_PAGE_INDEX = "1";
    private static final String MARK_PAGINATOR = "paginator";
    private static final String SPACE_ID_SESSION = "spaceIdSession";
    private static final String XSL_UNIQUE_PREFIX_ID = UniqueIDGenerator.getNewId(  ) + "library-";
    private AdminUser _user;
    private Plugin _plugin;
    private String _input;
    private int _nNbItemsPerPage;

    public String getInsertServiceSelectorUI( HttpServletRequest request )
    {
        init( request );

        Collection<LibraryMedia> libraryMedia = LibraryMediaHome.findAllMedia( _plugin );
        HashMap<String, Object> model = getDefaultModel(  );
        model.put( MARK_MEDIA_TYPES, libraryMedia );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MEDIATYPE_SELECTOR, _user.getLocale(  ), model );

        return template.getHtml(  );
    }

    /*public String getSpaceBrowser( HttpServletRequest request )
    {
        init( request );
    
        Map<String, Object> model = new HashMap<String, Object>(  );
        String strSpaceId = request.getParameter( DocumentSpacesService.PARAMETER_BROWSER_SELECTED_SPACE_ID );
    
        if ( ( strSpaceId != null ) && !strSpaceId.equals( "" ) )
        {
                return getSelectSlideshow(request);
        }
    
        Locale locale = AdminUserService.getLocale( request );
        // Spaces browser
        model.put( MARK_SPACES_BROWSER, DocumentSpacesService.getInstance(  ).getSpacesBrowser( request, _user, locale, true, true ) );
        String strBaseUrl = AppPathService.getBaseUrl( request );
        model.put( BASE_URL, strBaseUrl );
        model.put( PARAMETER_INPUT, _input );
    
        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_CHOOSE_DOCUMENT, locale, model );
    
        return template.getHtml(  );
    }*/
    public String getSelectMedia( HttpServletRequest request )
    {
        init( request );

        String strMediaType = request.getParameter( PARAMETER_MEDIA_TYPE );

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

        LibraryMedia mediaType = LibraryMediaHome.findByPrimaryKey( Integer.parseInt( strMediaType ), _plugin );
        mediaType.setMediaAttributeList( MediaAttributeHome.findAllAttributesForMedia( mediaType.getMediaId(  ), _plugin ) );

        Collection<LibraryMapping> allMappings = LibraryMappingHome.findAllMappingsByMedia( mediaType.getMediaId(  ),
                _plugin );
        Collection<DocumentSpace> spaces = DocumentSpaceHome.findAll(  );
        spaces = DocumentSpacesService.getInstance(  ).getUserAllowedSpaces( _user );

        List<Pair<String, Document>> listDocuments = new ArrayList<Pair<String, Document>>(  );
        HashMap<String, Map<String, String>> mapAssociationAttributes = new HashMap<String, Map<String, String>>(  );

        for ( LibraryMapping mapping : allMappings )
        {
            listDocuments.addAll( getDocumentsFromMapping( mapping, nSpaceId, spaces ) );
            mapAssociationAttributes.put( String.valueOf( mapping.getIdMapping(  ) ),
                getAttributesFromMapping( mapping ) );
        }

        Paginator paginator = getPaginator( request, listDocuments );
        HashMap<String, Object> model = getDefaultModel(  );
        model.put( MARK_PREVIEW_TYPE, "<img src='%SRC' alt='%ALT' />" );
        model.put( MARK_MEDIA_TYPE, mediaType );
        model.put( MARK_MEDIA_TYPE_ATTRIBUTES, mediaType.getMediaAttributeList(  ) );
        model.put( MARK_ALL_MEDIA_ATTRIBUTES_ASSOCIATIONS, mapAssociationAttributes );
        model.put( MARK_ALL_DOCUMENTS, paginator.getPageItems(  ) );
        //model.put( MARK_SPACES, spaces );
        model.put( MARK_SPACES_BROWSER,
            DocumentSpacesService.getInstance(  ).getSpacesBrowser( request, _user, _user.getLocale(  ), true, true ) );
        model.put( MARK_SELECTED_SPACE, nSpaceId );
        model.put( MARK_PAGINATOR, paginator );
        model.put( PARAMETER_NB_ITEMS_PER_PAGE, _nNbItemsPerPage );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MEDIA_SELECTOR, _user.getLocale(  ), model );

        return template.getHtml(  );
    }

    public String getEditSelectedMedia( HttpServletRequest request )
    {
        init( request );

        String strMediaTypeId = request.getParameter( PARAMETER_MEDIA_ID );
        String strDocumentId = request.getParameter( PARAMETER_DOCUMENT_ID );
        String strMappingId = request.getParameter( PARAMETER_MAPPING_ID );

        LibraryMedia mediaType = LibraryMediaHome.findByPrimaryKey( Integer.parseInt( strMediaTypeId ), _plugin );
        mediaType.setMediaAttributeList( MediaAttributeHome.findAllAttributesForMedia( mediaType.getMediaId(  ), _plugin ) );

        LibraryMapping mapping = LibraryMappingHome.findByPrimaryKey( Integer.parseInt( strMappingId ), _plugin );

        Document doc = DocumentHome.findByPrimaryKey( Integer.parseInt( strDocumentId ) );
        Map<String, Object> model = getDefaultModel(  );
        model.put( MARK_MEDIA_TYPE, mediaType );
        model.put( MARK_MEDIA_TYPE_ATTRIBUTES, mediaType.getMediaAttributeList(  ) );
        model.put( MARK_MEDIA_ATTRIBUTES_ASSOCIATIONS, getAttributesFromMapping( mapping ) );
        model.put( MARK_MEDIA_DOC, doc );
        model.put( MARK_JSP_INSERT, JSP_INSERT_MEDIA );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_EDIT_MEDIA, _user.getLocale(  ), model );

        return template.getHtml(  );
    }

    public String doInsertUrl( HttpServletRequest request )
    {
        init( request );

        String strMediaTypeId = request.getParameter( PARAMETER_MEDIA_ID );
        LibraryMedia mediaType = LibraryMediaHome.findByPrimaryKey( Integer.parseInt( strMediaTypeId ), _plugin );
        mediaType.setMediaAttributeList( MediaAttributeHome.findAllAttributesForMedia( mediaType.getMediaId(  ), _plugin ) );

        Collection allMappings = LibraryMappingHome.findAllMappingsByMedia( mediaType.getMediaId(  ), _plugin );
        LibraryMapping mapping = (LibraryMapping) allMappings.iterator(  ).next(  );

        StringBuffer sbXml = new StringBuffer(  );
        XmlUtil.beginElement( sbXml, XML_TAG_MEDIA );

        for ( MediaAttribute attribute : mediaType.getMediaAttributeList(  ) )
        {
            String strValue = request.getParameter( attribute.getCode(  ) );

            if ( attribute.getTypeId(  ) == MediaAttribute.ATTRIBUTE_TYPE_BINARY )
            {
                strValue = StringEscapeUtils.escapeHtml( strValue );
            }

            XmlUtil.addElement( sbXml, attribute.getCode(  ), strValue );
        }

        XmlUtil.endElement( sbXml, XML_TAG_MEDIA );

        String strHtml = "";

        try
        {
            XmlTransformerService xmlTransformerService = new XmlTransformerService(  );
            String strXslUniqueId = XSL_UNIQUE_PREFIX_ID + strMediaTypeId;
            String strPreHtml = xmlTransformerService.transformBySourceWithXslCache( sbXml.toString(  ),
                    mediaType.getStyleSheet(  ).getSource(  ), strXslUniqueId, null, null );

            strHtml = StringEscapeUtils.escapeJavaScript( strPreHtml );
        }
        catch ( Exception e )
        {
            // TODO Auto-generated catch block
            AppLogService.error( sbXml.toString(  ) );
            AppLogService.error( getClass(  ).getClassLoader(  ).getResource( "testlibrary.xsl" ) );
            AppLogService.error( e.getMessage(  ), e );
        }

        return insertUrl( request, _input, strHtml );
    }

    private List<Pair<String, Document>> getDocumentsFromMapping( LibraryMapping m, int nSpaceId,
        Collection<DocumentSpace> spaces )
    {
        DocumentFilter filter = new DocumentFilter(  );
        filter.setCodeDocumentType( m.getCodeDocumentType(  ) );
        filter.setIdState( APPROVED_DOCUMENT_STATE );

        List<Document> documents = null;
        List<Pair<String, Document>> result = new ArrayList<Pair<String, Document>>(  );

        if ( nSpaceId >= 0 )
        {
            if ( !DocumentSpacesService.getInstance(  ).isAuthorizedViewByRole( nSpaceId, _user ) ||
                    !DocumentSpacesService.getInstance(  ).isAuthorizedViewByWorkgroup( nSpaceId, _user ) )
            {
                return null;
            }

            filter.setIdSpace( nSpaceId );
            documents = DocumentHome.findByFilter( filter, _user.getLocale(  ) );
        }

        /* else
         {
             documents = new ArrayList<Document>(  );
        
             for ( DocumentSpace space : spaces )
             {
                 filter.setIdSpace( space.getId(  ) );
                 documents.addAll( DocumentHome.findByFilter( filter, _user.getLocale(  ) ) );
             }
         }*/
        if ( documents != null )
        {
            for ( Document document : documents )
            {
                DocumentHome.loadAttributes( document );
                result.add( new Pair<String, Document>( String.valueOf( m.getIdMapping(  ) ), document ) );
            }
        }

        return result;
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
        _plugin = PluginService.getPlugin( strPluginName );
        _input = request.getParameter( PARAMETER_INPUT );
    }

    private HashMap<String, Object> getDefaultModel(  )
    {
        HashMap<String, Object> model = new HashMap<String, Object>(  );
        model.put( PARAMETER_PLUGIN_NAME, _plugin.getName(  ) );
        model.put( PARAMETER_INPUT, _input );

        return model;
    }

    private Paginator getPaginator( HttpServletRequest request, List list )
    {
        String strNbItemPerPage = request.getParameter( PARAMETER_NB_ITEMS_PER_PAGE );
        String strDefaultNbItemPerPage = AppPropertiesService.getProperty( PROPERTY_RESULTS_PER_PAGE,
                DEFAULT_RESULTS_PER_PAGE );
        strNbItemPerPage = ( strNbItemPerPage != null ) ? strNbItemPerPage : strDefaultNbItemPerPage;

        _nNbItemsPerPage = Integer.parseInt( strNbItemPerPage );

        String strCurrentPageIndex = request.getParameter( PARAMETER_PAGE_INDEX );
        strCurrentPageIndex = ( strCurrentPageIndex != null ) ? strCurrentPageIndex : DEFAULT_PAGE_INDEX;

        UrlItem url = new UrlItem( JSP_MEDIA_TYPE_SELECTION );
        url.addParameter( PARAMETER_INPUT, _input );
        url.addParameter( PARAMETER_PLUGIN_NAME, _plugin.getName(  ) );

        String strSpaceId = request.getParameter( PARAMETER_SPACE_ID );

        if ( strSpaceId != null )
        {
            url.addParameter( PARAMETER_SPACE_ID, strSpaceId );
        }

        url.addParameter( PARAMETER_MEDIA_TYPE, request.getParameter( PARAMETER_MEDIA_TYPE ) );
        url.addParameter( PARAMETER_NB_ITEMS_PER_PAGE, _nNbItemsPerPage );

        return new Paginator( list, _nNbItemsPerPage, url.getUrl(  ), PARAMETER_PAGE_INDEX, strCurrentPageIndex );
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
