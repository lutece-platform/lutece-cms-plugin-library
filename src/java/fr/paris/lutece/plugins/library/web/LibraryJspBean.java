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

import fr.paris.lutece.plugins.document.business.DocumentType;
import fr.paris.lutece.plugins.document.business.DocumentTypeHome;
import fr.paris.lutece.plugins.document.business.attributes.DocumentAttribute;
import fr.paris.lutece.plugins.document.business.attributes.DocumentAttributeHome;
import fr.paris.lutece.plugins.library.business.LibraryMapping;
import fr.paris.lutece.plugins.library.business.LibraryMapping.AttributeAssociation;
import fr.paris.lutece.plugins.library.business.LibraryMappingHome;
import fr.paris.lutece.plugins.library.business.LibraryMedia;
import fr.paris.lutece.plugins.library.business.LibraryMediaHome;
import fr.paris.lutece.plugins.library.business.MediaAttribute;
import fr.paris.lutece.plugins.library.business.MediaAttributeHome;
import fr.paris.lutece.portal.service.html.XmlTransformerService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.web.admin.PluginAdminPageJspBean;
import fr.paris.lutece.portal.web.constants.Messages;
import fr.paris.lutece.portal.web.upload.MultipartHttpServletRequest;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.commons.fileupload.FileItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


/**
 * Library management JSP Bean
 */
public class LibraryJspBean extends PluginAdminPageJspBean
{
    /**
     * Right to manage libray plugin
     */
    public static final String LIBRARY_MANAGEMENT = "LIBRARY_MANAGEMENT";

    // Jsp
    private static final String JSP_URL_MEDIA = "ManageMedia.jsp";
    private static final String JSP_URL_MAPPINGS = "ManageMappings.jsp";
    private static final String JSP_DO_REMOVE_MAPPING = "jsp/admin/plugins/library/DoRemoveMapping.jsp";
    private static final String JSP_DO_REMOVE_ATTRIBUTE = "jsp/admin/plugins/library/DoRemoveAttribute.jsp";
    private static final String JSP_DO_REMOVE_MEDIA = "jsp/admin/plugins/library/DoRemoveMedia.jsp";

    // Parameters
    private static final String PARAMETER_MEDIA_ID = "media_id";
    private static final String PARAMETER_ATTRIBUTE_CODE = "attribute_code";
    private static final String PARAMETER_ATTRIBUTE_DESCRIPTION = "attribute_description";
    private static final String PARAMETER_ATTRIBUTE_TYPE = "attribute_type";
    private static final String PARAMETER_ATTRIBUTE_DEFAULT_VALUE = "attribute_default_value";
    private static final String PARAMETER_ATTRIBUTE_ID = "attribute_id";
    private static final String PARAMETER_DOCUMENT_TYPE = "document_type";
    private static final String PARAMETER_MEDIA_NAME = "media_name";
    private static final String PARAMETER_MEDIA_DESCRIPTION = "media_description";
    private static final String PARAMETER_PREFIX_DOCUMENT_ATTRIBUTE_ID = "document_attribute_id_";
    private static final String PARAMETER_MAPPING_ID = "mapping_id";
    private static final String PARAMETER_MEDIA_STYLESHEET = "media_stylesheet";

    // Properties
    private static final String PROPERTY_PAGE_TITLE_MAPPING_CREATION = "library.pageTitle.mapping.creation";
    private static final String PROPERTY_PAGE_TITLE_MEDIA_CREATION = "library.pageTitle.media.creation";
    private static final String PROPERTY_PAGE_TITLE_MEDIA_MODIFICATION = "library.pageTitle.media.modification";
    private static final String PROPERTY_PAGE_TITLE_ATTRIBUTE_CREATION = "library.pageTitle.attribute.modification";
    private static final String PROPERTY_PAGE_TITLE_ATTRIBUTE_MODIFICATION = "library.pageTitle.attribute.creation";
    private static final String PROPERTY_PAGE_TITLE_MAPPING_MANAGEMENT = "library.pageTitle.mapping.management";
    private static final String PROPERTY_MESSAGE_REMOVE_MEDIA = "library.message.media.remove";
    private static final String PROPERTY_MESSAGE_REMOVE_MAPPING = "library.message.mapping.remove";
    private static final String PROPERTY_PAGE_TITLE_MAPPING_MODIFICATION = "library.pageTitle.mapping.modification";
    private static final String PROPERTY_MESSAGE_REMOVE_ATTRIBUTE = "library.message.attribute.remove";

    // Marks
    private static final String MARK_MEDIA_LIST = "media_list";
    private static final String MARK_MEDIA = "media";
    private static final String MARK_MEDIA_ID = "mediaId";
    private static final String MARK_ATTRIBUTE_TYPE_LIST = "attribute_type_list";
    private static final String MARK_ATTRIBUTE = "attribute";
    private static final String MARK_MAPPING_LIST = "mapping_list";
    private static final String MARK_DOCUMENT_TYPE_LIST = "document_type_list";
    private static final String MARK_DOCUMENT_TYPE = "document_type";
    private static final String MARK_DOCUMENT_ATTRIBUTE = "document_attribute";
    private static final String MARK_MAPPING = "mapping";
    private static final String MARK_MEDIA_MULTIPLE = "media-multiple";

    // Templates
    private static final String TEMPLATE_MANAGE_MEDIA = "admin/plugins/library/manage_media.html";
    private static final String TEMPLATE_CREATE_MEDIA = "admin/plugins/library/create_media.html";
    private static final String TEMPLATE_CREATE_ATTRIBUTE = "admin/plugins/library/create_attribute.html";
    private static final String TEMPLATE_MODIFY_ATTRIBUTE = "admin/plugins/library/modify_attribute.html";
    private static final String TEMPLATE_MANAGE_LIBRARY = "admin/plugins/library/manage_library.html";
    private static final String TEMPLATE_MANAGE_MAPPINGS = "admin/plugins/library/manage_mappings.html";
    private static final String TEMPLATE_CREATE_MAPPING = "admin/plugins/library/create_mapping.html";
    private static final String TEMPLATE_GENERATE_ASSOCIATION_LABEL = "admin/plugins/library/generate_mapping_association_label.html";
    private static final String TEMPLATE_MODIFY_MAPPING = "admin/plugins/library/modify_mapping.html";

    /**
     * Get the manage library page
     * @param request The request
     * @return The HTML content
     */
    public String getManageLibrary( HttpServletRequest request )
    {
        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( MARK_MEDIA_LIST, LibraryMediaHome.findAllMedia( getPlugin(  ) ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MANAGE_LIBRARY, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Get the page to create a media
     * @param request The request
     * @return The HTML content
     */
    public String getCreateMedia( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_MEDIA_CREATION );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_CREATE_MEDIA, getLocale(  ), null );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Do create a media
     * @param request The request
     * @return The next URL to redirect to
     */
    public String doCreateMedia( HttpServletRequest request )
    {
        String strName = request.getParameter( PARAMETER_MEDIA_NAME );
        String strDescription = request.getParameter( PARAMETER_MEDIA_DESCRIPTION );

        MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
        FileItem item = mRequest.getFile( PARAMETER_MEDIA_STYLESHEET );

        if ( ( strName.length(  ) == 0 ) || ( strDescription.length(  ) == 0 ) || ( item.getSize(  ) == 0 ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
        }

        LibraryMedia media = new LibraryMedia(  );
        media.setName( strName );
        media.setDescription( strDescription );
        media.setStyleSheetBytes( item.get(  ) );
        media.setIsMultipleMedia( Boolean.parseBoolean( request.getParameter( MARK_MEDIA_MULTIPLE ) ) );

        LibraryMediaHome.create( media, getPlugin(  ) );

        return getHomeUrl( request );
    }

    /**
     * Get the confirmation page before removing a media
     * @param request The request
     * @return The next URL to redirect to
     */
    public String getRemoveMedia( HttpServletRequest request )
    {
        UrlItem url = new UrlItem( JSP_DO_REMOVE_MEDIA );
        String strMediaId = request.getParameter( PARAMETER_MEDIA_ID );
        int nMediaId = Integer.parseInt( strMediaId );
        url.addParameter( PARAMETER_MEDIA_ID, nMediaId );

        return AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_REMOVE_MEDIA, url.getUrl(  ),
            AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Do remove a media
     * @param request The request
     * @return The next URL to redirect to
     */
    public String doRemoveMedia( HttpServletRequest request )
    {
        String strMediaId = request.getParameter( PARAMETER_MEDIA_ID );
        int nMediaId = Integer.parseInt( strMediaId );

        XmlTransformerService.clearXslCache(  );

        // remove media definition
        LibraryMediaHome.remove( nMediaId, getPlugin(  ) );

        // remove all attributes associated with this media
        MediaAttributeHome.removeAllForMedia( nMediaId, getPlugin(  ) );

        // delete all the mapping definitions that exist for this media
        Collection<LibraryMapping> colMappings = LibraryMappingHome.findAllMappingsByMedia( nMediaId, getPlugin(  ) );

        for ( LibraryMapping mapping : colMappings )
        {
            LibraryMappingHome.remove( mapping.getIdMapping(  ), getPlugin(  ) );
        }

        return getHomeUrl( request );
    }

    /**
     *
     * @param request
     * @return
     */
    public String getManageMedia( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_MEDIA_MODIFICATION );

        String strMediaId = request.getParameter( PARAMETER_MEDIA_ID );
        int nMediaId = Integer.parseInt( strMediaId );

        LibraryMedia media = LibraryMediaHome.findByPrimaryKey( nMediaId, getPlugin(  ) );
        Collection<MediaAttribute> attributeList = MediaAttributeHome.findAllAttributesForMedia( nMediaId, getPlugin(  ) );
        media.setMediaAttributeList( I18nService.localizeCollection( attributeList, getLocale(  ) ) );

        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( MARK_MEDIA, media );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MANAGE_MEDIA, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     *
     * @param request
     * @return
     */
    public String doModifyMedia( HttpServletRequest request )
    {
        String strMediaId = request.getParameter( PARAMETER_MEDIA_ID );
        String strName = request.getParameter( PARAMETER_MEDIA_NAME );
        String strDescription = request.getParameter( PARAMETER_MEDIA_DESCRIPTION );

        MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
        FileItem item = mRequest.getFile( PARAMETER_MEDIA_STYLESHEET );

        if ( ( strName.length(  ) == 0 ) || ( strDescription.length(  ) == 0 ) || ( strMediaId.length(  ) == 0 ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
        }

        int nMediaId = Integer.parseInt( strMediaId );

        LibraryMedia media = LibraryMediaHome.findByPrimaryKey( nMediaId, getPlugin(  ) );
        media.setMediaId( nMediaId );
        media.setName( strName );
        media.setDescription( strDescription );

        if ( ( item != null ) && ( item.getSize(  ) > 0 ) )
        {
            XmlTransformerService.clearXslCache(  );
            media.setStyleSheetBytes( item.get(  ) );
        }

        media.setIsMultipleMedia( Boolean.parseBoolean( request.getParameter( MARK_MEDIA_MULTIPLE ) ) );

        LibraryMediaHome.update( media, getPlugin(  ) );

        return getHomeUrl( request );
    }

    /**
     *
     * @param request
     * @return
     */
    public String getCreateAttribute( HttpServletRequest request )
    {
        String strMediaId = request.getParameter( PARAMETER_MEDIA_ID );
        setPageTitleProperty( PROPERTY_PAGE_TITLE_ATTRIBUTE_CREATION );

        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( MARK_MEDIA_ID, strMediaId );
        model.put( MARK_ATTRIBUTE_TYPE_LIST, getAttributeTypeReferenceList( getLocale(  ) ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_CREATE_ATTRIBUTE, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     *
     * @param request
     * @return
     */
    public String doCreateAttribute( HttpServletRequest request )
    {
        String strMediaId = request.getParameter( PARAMETER_MEDIA_ID );
        String strCode = request.getParameter( PARAMETER_ATTRIBUTE_CODE );
        String strDescription = request.getParameter( PARAMETER_ATTRIBUTE_DESCRIPTION );
        String strType = request.getParameter( PARAMETER_ATTRIBUTE_TYPE );
        String strDefaultValue = request.getParameter( PARAMETER_ATTRIBUTE_DEFAULT_VALUE );

        if ( ( strMediaId.length(  ) == 0 ) || ( strCode.length(  ) == 0 ) || ( strDescription.length(  ) == 0 ) ||
                ( strType.length(  ) == 0 ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
        }

        int nMediaId = Integer.parseInt( strMediaId );
        int nType = Integer.parseInt( strType );

        MediaAttribute attribute = new MediaAttribute(  );
        attribute.setMediaId( nMediaId );
        attribute.setCode( strCode );
        attribute.setDescription( strDescription );
        attribute.setTypeId( nType );
        attribute.setDefaultValue( ( ( strDefaultValue != null ) && ( nType == MediaAttribute.ATTRIBUTE_TYPE_TEXT_USER ) )
            ? strDefaultValue : "" );

        MediaAttributeHome.create( attribute, getPlugin(  ) );

        return JSP_URL_MEDIA + "?" + PARAMETER_MEDIA_ID + "=" + nMediaId;
    }

    /**
     *
     * @param request
     * @return
     */
    public String getModifyAttribute( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_ATTRIBUTE_MODIFICATION );

        String strAttributeId = request.getParameter( PARAMETER_ATTRIBUTE_ID );
        int nAttributeId = Integer.parseInt( strAttributeId );

        MediaAttribute attribute = MediaAttributeHome.findByPrimaryKey( nAttributeId, getPlugin(  ) );
        HashMap<String, Object> model = new HashMap<String, Object>(  );

        model.put( MARK_ATTRIBUTE, attribute );
        model.put( MARK_ATTRIBUTE_TYPE_LIST, getAttributeTypeReferenceList( getLocale(  ) ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MODIFY_ATTRIBUTE, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     *
     * @param request
     * @return
     */
    public String doModifyAttribute( HttpServletRequest request )
    {
        String strMediaId = request.getParameter( PARAMETER_MEDIA_ID );
        String strAttributeId = request.getParameter( PARAMETER_ATTRIBUTE_ID );
        String strCode = request.getParameter( PARAMETER_ATTRIBUTE_CODE );
        String strDescription = request.getParameter( PARAMETER_ATTRIBUTE_DESCRIPTION );
        String strType = request.getParameter( PARAMETER_ATTRIBUTE_TYPE );
        String strDefaultValue = request.getParameter( PARAMETER_ATTRIBUTE_DEFAULT_VALUE );

        if ( ( strMediaId.length(  ) == 0 ) || ( strAttributeId.length(  ) == 0 ) || ( strCode.length(  ) == 0 ) ||
                ( strDescription.length(  ) == 0 ) || ( strType.length(  ) == 0 ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
        }

        int nMediaId = Integer.parseInt( strMediaId );
        int nAttributeId = Integer.parseInt( strAttributeId );
        int nType = Integer.parseInt( strType );

        MediaAttribute attribute = new MediaAttribute(  );
        attribute.setMediaId( nMediaId );
        attribute.setAttributeId( nAttributeId );
        attribute.setCode( strCode );
        attribute.setDescription( strDescription );
        attribute.setTypeId( nType );
        attribute.setDefaultValue( ( ( strDefaultValue != null ) && ( nType == MediaAttribute.ATTRIBUTE_TYPE_TEXT_USER ) )
            ? strDefaultValue : "" );

        MediaAttributeHome.update( attribute, getPlugin(  ) );

        return JSP_URL_MEDIA + "?" + PARAMETER_MEDIA_ID + "=" + nMediaId;
    }

    /**
     *
     * @param request
     * @return
     */
    public String getRemoveAttribute( HttpServletRequest request )
    {
        UrlItem url = new UrlItem( JSP_DO_REMOVE_ATTRIBUTE );
        String strAttributeId = request.getParameter( PARAMETER_ATTRIBUTE_ID );
        String strMediaId = request.getParameter( PARAMETER_MEDIA_ID );
        int nMediaId = Integer.parseInt( strMediaId );
        int nAttributeId = Integer.parseInt( strAttributeId );
        url.addParameter( PARAMETER_ATTRIBUTE_ID, nAttributeId );
        url.addParameter( PARAMETER_MEDIA_ID, nMediaId );

        return AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_REMOVE_ATTRIBUTE, url.getUrl(  ),
            AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     *
     * @param request
     * @return
     */
    public String doRemoveAttribute( HttpServletRequest request )
    {
        String strAttributeId = request.getParameter( PARAMETER_ATTRIBUTE_ID );
        int nAttributeId = Integer.parseInt( strAttributeId );
        String strMediaId = request.getParameter( PARAMETER_MEDIA_ID );

        // remove attribute
        MediaAttributeHome.remove( nAttributeId, getPlugin(  ) );

        //remove existing mapping associations found for this attribute
        LibraryMappingHome.removeMappingAttributeAssociation( nAttributeId, getPlugin(  ) );

        return JSP_URL_MEDIA + "?" + PARAMETER_MEDIA_ID + "=" + strMediaId;
    }

    /**
     *
     * @param locale
     * @return
     */
    private ReferenceList getAttributeTypeReferenceList( Locale locale )
    {
        ReferenceList refList = new ReferenceList(  );
        ReferenceItem item = new ReferenceItem(  );
        item.setCode( String.valueOf( MediaAttribute.ATTRIBUTE_TYPE_TEXT_USER ) );
        item.setName( I18nService.getLocalizedString( MediaAttribute.PROPERTY_ATTRIBUTE_TYPE_LABEL_TEXT_USER, locale ) );
        refList.add( item );
        item = new ReferenceItem(  );
        item.setCode( String.valueOf( MediaAttribute.ATTRIBUTE_TYPE_TITLE_DOCUMENT ) );
        item.setName( I18nService.getLocalizedString( MediaAttribute.PROPERTY_ATTRIBUTE_TYPE_LABEL_TITLE_DOCUMENT,
                locale ) );
        refList.add( item );
        item = new ReferenceItem(  );
        item.setCode( String.valueOf( MediaAttribute.ATTRIBUTE_TYPE_SUMMARY_DOCUMENT ) );
        item.setName( I18nService.getLocalizedString( MediaAttribute.PROPERTY_ATTRIBUTE_TYPE_LABEL_SUMMARY_DOCUMENT,
                locale ) );
        refList.add( item );
        item = new ReferenceItem(  );
        item.setCode( String.valueOf( MediaAttribute.ATTRIBUTE_TYPE_TEXT_DOCUMENT ) );
        item.setName( I18nService.getLocalizedString( MediaAttribute.PROPERTY_ATTRIBUTE_TYPE_LABEL_TEXT_DOCUMENT, locale ) );
        refList.add( item );
        item = new ReferenceItem(  );
        item.setCode( String.valueOf( MediaAttribute.ATTRIBUTE_TYPE_BINARY ) );
        item.setName( I18nService.getLocalizedString( MediaAttribute.PROPERTY_ATTRIBUTE_TYPE_LABEL_BINARY, locale ) );
        refList.add( item );

        return refList;
    }

    /**
     *
     * @param request
     * @return
     */
    public String getManageMappings( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_MAPPING_MANAGEMENT );

        String strMediaId = request.getParameter( PARAMETER_MEDIA_ID );
        int nMediaId = Integer.parseInt( strMediaId );

        LibraryMedia media = LibraryMediaHome.findByPrimaryKey( nMediaId, getPlugin(  ) );
        Collection<MediaAttribute> attributeList = MediaAttributeHome.findAllAttributesForMedia( nMediaId, getPlugin(  ) );
        media.setMediaAttributeList( I18nService.localizeCollection( attributeList, getLocale(  ) ) );

        Collection<LibraryMapping> colMappings = LibraryMappingHome.findAllMappingsByMedia( nMediaId, getPlugin(  ) );

        for ( LibraryMapping mapping : colMappings )
        {
            if ( mapping.getAttributeAssociationList(  ) == null )
            {
                continue;
            }

            for ( AttributeAssociation association : mapping.getAttributeAssociationList(  ) )
            {
                MediaAttribute mediaAttribute = MediaAttributeHome.findByPrimaryKey( association.getIdMediaAttribute(  ),
                        getPlugin(  ) );
                DocumentAttribute documentAttribute = DocumentAttributeHome.findByPrimaryKey( association.getIdDocumentAttribute(  ) );

                Map<String, Object> modelLabel = new HashMap<String, Object>(  );
                modelLabel.put( MARK_ATTRIBUTE, mediaAttribute );
                modelLabel.put( MARK_DOCUMENT_ATTRIBUTE, documentAttribute );

                HtmlTemplate templateLabel = AppTemplateService.getTemplate( TEMPLATE_GENERATE_ASSOCIATION_LABEL,
                        getLocale(  ), modelLabel );
                association.setAssociationLabel( templateLabel.getHtml(  ) );
            }
        }

        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( MARK_MEDIA, media );
        model.put( MARK_MAPPING_LIST, colMappings );
        model.put( MARK_DOCUMENT_TYPE_LIST, DocumentTypeHome.getDocumentTypesList(  ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MANAGE_MAPPINGS, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     *
     * @param request
     * @return
     */
    public String getCreateMapping( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_MAPPING_CREATION );

        String strMediaId = request.getParameter( PARAMETER_MEDIA_ID );
        int nMediaId = Integer.parseInt( strMediaId );
        String strDocumentTypeCode = request.getParameter( PARAMETER_DOCUMENT_TYPE );

        DocumentType documentType = DocumentTypeHome.findByPrimaryKey( strDocumentTypeCode );

        LibraryMedia media = LibraryMediaHome.findByPrimaryKey( nMediaId, getPlugin(  ) );
        Collection<MediaAttribute> attributeList = MediaAttributeHome.findAllAttributesForMedia( nMediaId, getPlugin(  ) );
        media.setMediaAttributeList( I18nService.localizeCollection( attributeList, getLocale(  ) ) );

        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( MARK_MEDIA, media );
        model.put( MARK_DOCUMENT_TYPE, documentType );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_CREATE_MAPPING, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     *
     * @param request
     * @return
     */
    public String doCreateMapping( HttpServletRequest request )
    {
        String strMediaId = request.getParameter( PARAMETER_MEDIA_ID );
        int nMediaId = Integer.parseInt( strMediaId );

        String strDocumentTypeCode = request.getParameter( PARAMETER_DOCUMENT_TYPE );

        LibraryMapping mapping = new LibraryMapping(  );
        mapping.setCodeDocumentType( strDocumentTypeCode );
        mapping.setIdMedia( nMediaId );

        LibraryMedia media = LibraryMediaHome.findByPrimaryKey( nMediaId, getPlugin(  ) );
        Collection<MediaAttribute> colMediaAttribute = MediaAttributeHome.findAllAttributesForMedia( nMediaId,
                getPlugin(  ) );
        media.setMediaAttributeList( colMediaAttribute );
        mapping.setAttributeAssociationList( new ArrayList<AttributeAssociation>(  ) );

        for ( MediaAttribute attribute : media.getMediaAttributeList(  ) )
        {
            if ( attribute.isAssociableWithDocument(  ) )
            {
                String strParameterDocumentAttributeId = PARAMETER_PREFIX_DOCUMENT_ATTRIBUTE_ID +
                    attribute.getAttributeId(  );
                String strDocumentAttributeId = request.getParameter( strParameterDocumentAttributeId );

                if ( strDocumentAttributeId != null )
                {
                    int nDocumentAttributeId = Integer.parseInt( strDocumentAttributeId );
                    mapping.addAttributeAssociation( attribute.getAttributeId(  ), nDocumentAttributeId );
                }
            }
        }

        LibraryMappingHome.create( mapping, getPlugin(  ) );

        return JSP_URL_MAPPINGS + "?" + PARAMETER_MEDIA_ID + "=" + nMediaId;
    }

    /**
     *
     * @param request
     * @return
     */
    public String getModifyMapping( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_MAPPING_MODIFICATION );

        String strMappingId = request.getParameter( PARAMETER_MAPPING_ID );
        int nMappingId = Integer.parseInt( strMappingId );

        LibraryMapping mapping = LibraryMappingHome.findByPrimaryKey( nMappingId, getPlugin(  ) );

        DocumentType documentType = DocumentTypeHome.findByPrimaryKey( mapping.getCodeDocumentType(  ) );

        LibraryMedia media = LibraryMediaHome.findByPrimaryKey( mapping.getIdMedia(  ), getPlugin(  ) );
        Collection<MediaAttribute> attributeList = MediaAttributeHome.findAllAttributesForMedia( mapping.getIdMedia(  ),
                getPlugin(  ) );
        media.setMediaAttributeList( I18nService.localizeCollection( attributeList, getLocale(  ) ) );

        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( MARK_MAPPING, mapping );
        model.put( MARK_MEDIA, media );
        model.put( MARK_DOCUMENT_TYPE, documentType );
        model.put( MARK_DOCUMENT_TYPE_LIST, DocumentTypeHome.getDocumentTypesList(  ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MODIFY_MAPPING, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     *
     * @param request
     * @return
     */
    public String doModifyMappingAssociations( HttpServletRequest request )
    {
        String strMappingId = request.getParameter( PARAMETER_MAPPING_ID );
        int nMappingId = Integer.parseInt( strMappingId );

        String strMediaId = request.getParameter( PARAMETER_MEDIA_ID );
        int nMediaId = Integer.parseInt( strMediaId );

        String strDocumentTypeCode = request.getParameter( PARAMETER_DOCUMENT_TYPE );

        LibraryMapping mapping = new LibraryMapping(  );
        mapping.setCodeDocumentType( strDocumentTypeCode );
        mapping.setIdMedia( nMediaId );
        mapping.setIdMapping( nMappingId );

        LibraryMedia media = LibraryMediaHome.findByPrimaryKey( nMediaId, getPlugin(  ) );
        Collection<MediaAttribute> colMediaAttribute = MediaAttributeHome.findAllAttributesForMedia( nMediaId,
                getPlugin(  ) );
        media.setMediaAttributeList( colMediaAttribute );

        for ( MediaAttribute attribute : media.getMediaAttributeList(  ) )
        {
            if ( attribute.isAssociableWithDocument(  ) )
            {
                String strParameterDocumentAttributeId = PARAMETER_PREFIX_DOCUMENT_ATTRIBUTE_ID +
                    attribute.getAttributeId(  );
                String strDocumentAttributeId = request.getParameter( strParameterDocumentAttributeId );

                if ( strDocumentAttributeId != null )
                {
                    int nDocumentAttributeId = Integer.parseInt( strDocumentAttributeId );
                    mapping.addAttributeAssociation( attribute.getAttributeId(  ), nDocumentAttributeId );
                }
            }
        }

        LibraryMappingHome.update( mapping, getPlugin(  ) );

        return JSP_URL_MAPPINGS + "?" + PARAMETER_MEDIA_ID + "=" + nMediaId;
    }

    /**
     *
     * @param request
     * @return
     */
    public String getRemoveMapping( HttpServletRequest request )
    {
        UrlItem url = new UrlItem( JSP_DO_REMOVE_MAPPING );
        String strMappingId = request.getParameter( PARAMETER_MAPPING_ID );
        int nMappingId = Integer.parseInt( strMappingId );
        url.addParameter( PARAMETER_MAPPING_ID, nMappingId );

        return AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_REMOVE_MAPPING, url.getUrl(  ),
            AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Do remove a mapping
     * @param request The request
     * @return The next URL to redirect to
     */
    public String doRemoveMapping( HttpServletRequest request )
    {
        String strMappingId = request.getParameter( PARAMETER_MAPPING_ID );
        int nMappingId = Integer.parseInt( strMappingId );
        LibraryMapping mapping = LibraryMappingHome.findByPrimaryKey( nMappingId, getPlugin(  ) );

        LibraryMappingHome.remove( nMappingId, getPlugin(  ) );

        return JSP_URL_MAPPINGS + "?" + PARAMETER_MEDIA_ID + "=" + mapping.getIdMedia(  );
    }
}
