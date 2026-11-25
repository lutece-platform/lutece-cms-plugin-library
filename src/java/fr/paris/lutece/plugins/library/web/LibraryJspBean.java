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
import fr.paris.lutece.plugins.library.service.LibraryMediaTemplateService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.template.HtmlMarkup;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.web.cdi.mvc.Models;
import fr.paris.lutece.portal.web.constants.Messages;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;


/**
 * Back office of the library: the media types, their attributes and their mappings to the document types
 */
@RequestScoped
@Named
@Controller( controllerJsp = LibraryJspBean.CONTROLLER_JSP, controllerPath = LibraryJspBean.CONTROLLER_PATH, right = LibraryJspBean.LIBRARY_MANAGEMENT, securityTokenEnabled = true )
public class LibraryJspBean extends MVCAdminJspBean
{
    /**
     * Right to manage libray plugin
     */
    public static final String LIBRARY_MANAGEMENT = "LIBRARY_MANAGEMENT";
    static final String CONTROLLER_JSP = "ManageLibrary.jsp";
    static final String CONTROLLER_PATH = "jsp/admin/plugins/library/";
    private static final String PLUGIN_NAME = "library";
    private static final long serialVersionUID = -4537118194245494293L;

    // Views
    private static final String VIEW_MANAGE_LIBRARY = "manageLibrary";
    private static final String VIEW_CREATE_MEDIA = "createMedia";
    private static final String VIEW_MANAGE_MEDIA = "manageMedia";
    private static final String VIEW_CONFIRM_REMOVE_MEDIA = "confirmRemoveMedia";
    private static final String VIEW_CREATE_ATTRIBUTE = "createAttribute";
    private static final String VIEW_MODIFY_ATTRIBUTE = "modifyAttribute";
    private static final String VIEW_CONFIRM_REMOVE_ATTRIBUTE = "confirmRemoveAttribute";
    private static final String VIEW_MANAGE_MAPPINGS = "manageMappings";
    private static final String VIEW_CREATE_MAPPING = "createMapping";
    private static final String VIEW_MODIFY_MAPPING = "modifyMapping";
    private static final String VIEW_CONFIRM_REMOVE_MAPPING = "confirmRemoveMapping";

    // Actions: a form view and its action share their name, so the token of the view is the one of the action
    private static final String ACTION_CREATE_MEDIA = VIEW_CREATE_MEDIA;
    private static final String ACTION_MODIFY_MEDIA = "modifyMedia";
    private static final String ACTION_REMOVE_MEDIA = "removeMedia";
    private static final String ACTION_CREATE_ATTRIBUTE = VIEW_CREATE_ATTRIBUTE;
    private static final String ACTION_MODIFY_ATTRIBUTE = VIEW_MODIFY_ATTRIBUTE;
    private static final String ACTION_REMOVE_ATTRIBUTE = "removeAttribute";
    private static final String ACTION_CREATE_MAPPING = VIEW_CREATE_MAPPING;
    private static final String ACTION_MODIFY_MAPPING = VIEW_MODIFY_MAPPING;
    private static final String ACTION_REMOVE_MAPPING = "removeMapping";

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
    private static final String PARAMETER_MEDIA_TEMPLATE = "media_template";
    private static final String PARAMETER_MEDIA_MULTIPLE = "media-multiple";

    // Properties
    private static final String PROPERTY_PAGE_TITLE_MANAGE_LIBRARY = "library.adminFeature.library_management.name";
    private static final String PROPERTY_PAGE_TITLE_MAPPING_CREATION = "library.pageTitle.mapping.creation";
    private static final String PROPERTY_PAGE_TITLE_MEDIA_CREATION = "library.pageTitle.media.creation";
    private static final String PROPERTY_PAGE_TITLE_MEDIA_MODIFICATION = "library.pageTitle.media.modification";
    private static final String PROPERTY_PAGE_TITLE_ATTRIBUTE_CREATION = "library.pageTitle.attribute.modification";
    private static final String PROPERTY_PAGE_TITLE_ATTRIBUTE_MODIFICATION = "library.pageTitle.attribute.creation";
    private static final String PROPERTY_PAGE_TITLE_MAPPING_MANAGEMENT = "library.pageTitle.mapping.management";
    private static final String PROPERTY_PAGE_TITLE_MAPPING_MODIFICATION = "library.pageTitle.mapping.modification";
    private static final String PROPERTY_MESSAGE_REMOVE_MEDIA = "library.message.media.remove";
    private static final String PROPERTY_MESSAGE_REMOVE_MAPPING = "library.message.mapping.remove";
    private static final String PROPERTY_MESSAGE_REMOVE_ATTRIBUTE = "library.message.attribute.remove";
    private static final String PROPERTY_MESSAGE_MEDIA_NOT_FOUND = "library.message.media.notFound";
    private static final String PROPERTY_MESSAGE_ATTRIBUTE_NOT_FOUND = "library.message.attribute.notFound";
    private static final String PROPERTY_MESSAGE_MAPPING_NOT_FOUND = "library.message.mapping.notFound";
    private static final String PROPERTY_MESSAGE_DOCUMENT_TYPE_NOT_FOUND = "library.message.documentType.notFound";
    private static final String PROPERTY_MESSAGE_ATTRIBUTE_DUPLICATE_CODE = "library.message.attribute.duplicateCode";
    private static final String PROPERTY_MESSAGE_TEMPLATE_ERROR = "library.message.media.templateError";

    // Marks
    private static final String MARK_MEDIA_LIST = "media_list";
    private static final String MARK_MEDIA = "media";
    private static final String MARK_MEDIA_ID = "mediaId";
    private static final String MARK_ATTRIBUTE_TYPE_LIST = "attribute_type_list";
    private static final String MARK_ATTRIBUTE = "attribute";
    private static final String MARK_MAPPING_LIST = "mapping_list";
    private static final String MARK_ASSOCIATION_LABELS = "association_labels";
    private static final String MARK_DOCUMENT_TYPE_LIST = "document_type_list";
    private static final String MARK_DOCUMENT_TYPE = "document_type";
    private static final String MARK_DOCUMENT_ATTRIBUTE = "document_attribute";
    private static final String MARK_MAPPING = "mapping";
    private static final String MARK_MEDIA_TEMPLATE = "media_template";
    private static final String MARK_LEGACY_STYLESHEET = "legacy_stylesheet";
    private static final String MARK_LEGACY_MEDIA_IDS = "legacy_media_ids";

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

    @Inject
    private LibraryMediaTemplateService _mediaTemplateService;

    /**
     * {@inheritDoc}
     * The bean is request scoped: the plugin is the library one, whatever the request carries
     */
    @Override
    public Plugin getPlugin( )
    {
        return PluginService.getPlugin( PLUGIN_NAME );
    }

    /**
     * Returns the list of the media types
     * @param model the model of the page
     * @param request The request
     * @return The HTML content
     */
    @View( value = VIEW_MANAGE_LIBRARY, defaultView = true )
    public String getManageLibrary( Models model, HttpServletRequest request )
    {
        model.put( MARK_MEDIA_LIST, LibraryMediaHome.findAllMedia( getPlugin( ) ) );
        model.put( MARK_LEGACY_MEDIA_IDS, _mediaTemplateService.getMediaIdsWithLegacyStylesheet( getPlugin( ) ) );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_LIBRARY, TEMPLATE_MANAGE_LIBRARY );
    }

    /**
     * Returns the form creating a media type
     * @param model the model of the page
     * @param request The request
     * @return The HTML content
     */
    @View( VIEW_CREATE_MEDIA )
    public String getCreateMedia( Models model, HttpServletRequest request )
    {
        model.put( MARK_MEDIA_TEMPLATE, _mediaTemplateService.getDefaultTemplateSource( ) );

        return getPage( PROPERTY_PAGE_TITLE_MEDIA_CREATION, TEMPLATE_CREATE_MEDIA );
    }

    /**
     * Creates a media type, its template validated as at its modification
     * @param request The request
     * @return The redirection to the list of the media types, or to the error message
     */
    @Action( ACTION_CREATE_MEDIA )
    public String doCreateMedia( HttpServletRequest request )
    {
        String strName = request.getParameter( PARAMETER_MEDIA_NAME );
        String strDescription = request.getParameter( PARAMETER_MEDIA_DESCRIPTION );
        String strTemplate = request.getParameter( PARAMETER_MEDIA_TEMPLATE );

        if ( StringUtils.isAnyBlank( strName, strDescription, strTemplate ) )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP ) );
        }

        LibraryMedia media = new LibraryMedia( );
        media.setName( strName );
        media.setDescription( strDescription );
        media.setTemplate( strTemplate );
        media.setIsMultipleMedia( Boolean.parseBoolean( request.getParameter( PARAMETER_MEDIA_MULTIPLE ) ) );

        String strError = _mediaTemplateService.validateTemplate( media, strTemplate, getLocale( ) );

        if ( strError != null )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_TEMPLATE_ERROR, new Object [ ] {
                    strError
            }, AdminMessage.TYPE_STOP ) );
        }

        LibraryMediaHome.create( media, getPlugin( ) );

        return redirectView( request, VIEW_MANAGE_LIBRARY );
    }

    /**
     * Asks the confirmation of the removal of a media type
     * @param request The request
     * @return The redirection to the confirmation message, or to the error message for an unknown media type
     */
    @View( value = VIEW_CONFIRM_REMOVE_MEDIA, securityTokenAction = ACTION_REMOVE_MEDIA )
    public String getConfirmRemoveMedia( HttpServletRequest request )
    {
        LibraryMedia media = findMedia( request );

        if ( media == null )
        {
            return redirectMediaNotFound( request );
        }

        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_MEDIA ) );
        url.addParameter( PARAMETER_MEDIA_ID, media.getMediaId( ) );

        return redirect( request, AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_REMOVE_MEDIA, url.getUrl( ), AdminMessage.TYPE_CONFIRMATION ) );
    }

    /**
     * Removes a media type, its attributes and its mappings
     * @param request The request
     * @return The redirection to the list of the media types
     */
    @Action( ACTION_REMOVE_MEDIA )
    public String doRemoveMedia( HttpServletRequest request )
    {
        LibraryMedia media = findMedia( request );

        if ( media == null )
        {
            return redirectMediaNotFound( request );
        }

        int nMediaId = media.getMediaId( );
        LibraryMediaHome.remove( nMediaId, getPlugin( ) );
        MediaAttributeHome.removeAllForMedia( nMediaId, getPlugin( ) );

        for ( LibraryMapping mapping : LibraryMappingHome.findAllMappingsByMedia( nMediaId, getPlugin( ) ) )
        {
            LibraryMappingHome.remove( mapping.getIdMapping( ), getPlugin( ) );
        }

        return redirectView( request, VIEW_MANAGE_LIBRARY );
    }

    /**
     * Returns the page of a media type: its form and its attributes
     * @param model the model of the page
     * @param request The request
     * @return The HTML content, or the redirection to the error message for an unknown media type
     */
    @View( value = VIEW_MANAGE_MEDIA, securityTokenAction = ACTION_MODIFY_MEDIA )
    public String getManageMedia( Models model, HttpServletRequest request )
    {
        LibraryMedia media = findMedia( request );

        if ( media == null )
        {
            return redirectMediaNotFound( request );
        }

        int nMediaId = media.getMediaId( );
        Collection<MediaAttribute> attributeList = MediaAttributeHome.findAllAttributesForMedia( nMediaId, getPlugin( ) );
        media.setMediaAttributeList( I18nService.localizeCollection( attributeList, getLocale( ) ) );

        String strLegacyStylesheet = _mediaTemplateService.getLegacyStylesheet( nMediaId, getPlugin( ) );
        String strTemplate = media.getTemplate( );

        if ( StringUtils.isBlank( strTemplate ) )
        {
            strTemplate = ( strLegacyStylesheet == null ) ? _mediaTemplateService.getDefaultTemplateSource( ) : StringUtils.EMPTY;
        }

        model.put( MARK_MEDIA, media );
        model.put( MARK_MEDIA_TEMPLATE, strTemplate );
        model.put( MARK_LEGACY_STYLESHEET, strLegacyStylesheet );

        return getPage( PROPERTY_PAGE_TITLE_MEDIA_MODIFICATION, TEMPLATE_MANAGE_MEDIA );
    }

    /**
     * Modifies a media type
     * @param request The request
     * @return The redirection to the list of the media types, or to the error message
     */
    @Action( ACTION_MODIFY_MEDIA )
    public String doModifyMedia( HttpServletRequest request )
    {
        LibraryMedia media = findMedia( request );

        if ( media == null )
        {
            return redirectMediaNotFound( request );
        }

        String strName = request.getParameter( PARAMETER_MEDIA_NAME );
        String strDescription = request.getParameter( PARAMETER_MEDIA_DESCRIPTION );
        String strTemplate = request.getParameter( PARAMETER_MEDIA_TEMPLATE );

        if ( StringUtils.isAnyBlank( strName, strDescription ) )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP ) );
        }

        int nMediaId = media.getMediaId( );
        boolean bKeepLegacyStylesheet = StringUtils.isBlank( strTemplate );

        if ( bKeepLegacyStylesheet && ( _mediaTemplateService.getLegacyStylesheet( nMediaId, getPlugin( ) ) == null ) )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP ) );
        }

        media.setMediaAttributeList( MediaAttributeHome.findAllAttributesForMedia( nMediaId, getPlugin( ) ) );

        String strError = _mediaTemplateService.validateTemplate( media, strTemplate, getLocale( ) );

        if ( strError != null )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_TEMPLATE_ERROR, new Object [ ] {
                    strError
            }, AdminMessage.TYPE_STOP ) );
        }

        media.setName( strName );
        media.setDescription( strDescription );
        media.setTemplate( bKeepLegacyStylesheet ? null : strTemplate );
        media.setIsMultipleMedia( Boolean.parseBoolean( request.getParameter( PARAMETER_MEDIA_MULTIPLE ) ) );

        LibraryMediaHome.update( media, getPlugin( ) );

        if ( !bKeepLegacyStylesheet )
        {
            _mediaTemplateService.discardLegacyStylesheet( nMediaId, getPlugin( ) );
        }

        return redirectView( request, VIEW_MANAGE_LIBRARY );
    }

    /**
     * Returns the form creating an attribute of a media type
     * @param model the model of the page
     * @param request the HTTP request
     * @return the page, or the redirection to the error message for an unknown media type
     */
    @View( VIEW_CREATE_ATTRIBUTE )
    public String getCreateAttribute( Models model, HttpServletRequest request )
    {
        LibraryMedia media = findMedia( request );

        if ( media == null )
        {
            return redirectMediaNotFound( request );
        }

        model.put( MARK_MEDIA, media );
        model.put( MARK_MEDIA_ID, String.valueOf( media.getMediaId( ) ) );
        model.put( MARK_ATTRIBUTE_TYPE_LIST, getAttributeTypeReferenceList( getLocale( ) ) );

        return getPage( PROPERTY_PAGE_TITLE_ATTRIBUTE_CREATION, TEMPLATE_CREATE_ATTRIBUTE );
    }

    /**
     * Creates an attribute of a media type, its code unique in the media type
     * @param request the HTTP request
     * @return the redirection to the page of the media type, or to the error message
     */
    @Action( ACTION_CREATE_ATTRIBUTE )
    public String doCreateAttribute( HttpServletRequest request )
    {
        LibraryMedia media = findMedia( request );

        if ( media == null )
        {
            return redirectMediaNotFound( request );
        }

        MediaAttribute attribute = new MediaAttribute( );
        attribute.setMediaId( media.getMediaId( ) );

        String strErrorUrl = populateAttribute( request, attribute );

        if ( strErrorUrl != null )
        {
            return redirect( request, strErrorUrl );
        }

        MediaAttributeHome.create( attribute, getPlugin( ) );

        return redirect( request, VIEW_MANAGE_MEDIA, PARAMETER_MEDIA_ID, media.getMediaId( ) );
    }

    /**
     * Returns the form modifying an attribute of a media type
     * @param model the model of the page
     * @param request the HTTP request
     * @return the page, or the redirection to the error message for an unknown attribute
     */
    @View( VIEW_MODIFY_ATTRIBUTE )
    public String getModifyAttribute( Models model, HttpServletRequest request )
    {
        MediaAttribute attribute = findAttribute( request );

        if ( attribute == null )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_ATTRIBUTE_NOT_FOUND, AdminMessage.TYPE_STOP ) );
        }

        model.put( MARK_ATTRIBUTE, attribute );
        model.put( MARK_MEDIA, LibraryMediaHome.findByPrimaryKey( attribute.getMediaId( ), getPlugin( ) ) );
        model.put( MARK_ATTRIBUTE_TYPE_LIST, getAttributeTypeReferenceList( getLocale( ) ) );

        return getPage( PROPERTY_PAGE_TITLE_ATTRIBUTE_MODIFICATION, TEMPLATE_MODIFY_ATTRIBUTE );
    }

    /**
     * Modifies an attribute of a media type, its code unique in the media type
     * @param request the HTTP request
     * @return the redirection to the page of the media type, or to the error message
     */
    @Action( ACTION_MODIFY_ATTRIBUTE )
    public String doModifyAttribute( HttpServletRequest request )
    {
        MediaAttribute attribute = findAttribute( request );

        if ( attribute == null )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_ATTRIBUTE_NOT_FOUND, AdminMessage.TYPE_STOP ) );
        }

        String strErrorUrl = populateAttribute( request, attribute );

        if ( strErrorUrl != null )
        {
            return redirect( request, strErrorUrl );
        }

        MediaAttributeHome.update( attribute, getPlugin( ) );

        return redirect( request, VIEW_MANAGE_MEDIA, PARAMETER_MEDIA_ID, attribute.getMediaId( ) );
    }

    /**
     * Asks the confirmation of the removal of an attribute
     * @param request the HTTP request
     * @return the redirection to the confirmation message, or to the error message for an unknown attribute
     */
    @View( value = VIEW_CONFIRM_REMOVE_ATTRIBUTE, securityTokenAction = ACTION_REMOVE_ATTRIBUTE )
    public String getConfirmRemoveAttribute( HttpServletRequest request )
    {
        MediaAttribute attribute = findAttribute( request );

        if ( attribute == null )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_ATTRIBUTE_NOT_FOUND, AdminMessage.TYPE_STOP ) );
        }

        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_ATTRIBUTE ) );
        url.addParameter( PARAMETER_ATTRIBUTE_ID, attribute.getAttributeId( ) );

        return redirect( request,
                AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_REMOVE_ATTRIBUTE, url.getUrl( ), AdminMessage.TYPE_CONFIRMATION ) );
    }

    /**
     * Removes an attribute and its associations in the mappings
     * @param request the HTTP request
     * @return the redirection to the page of the media type
     */
    @Action( ACTION_REMOVE_ATTRIBUTE )
    public String doRemoveAttribute( HttpServletRequest request )
    {
        MediaAttribute attribute = findAttribute( request );

        if ( attribute == null )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_ATTRIBUTE_NOT_FOUND, AdminMessage.TYPE_STOP ) );
        }

        MediaAttributeHome.remove( attribute.getAttributeId( ), getPlugin( ) );
        LibraryMappingHome.removeMappingAttributeAssociation( attribute.getAttributeId( ), getPlugin( ) );

        return redirect( request, VIEW_MANAGE_MEDIA, PARAMETER_MEDIA_ID, attribute.getMediaId( ) );
    }

    /**
     * Returns the list of the mappings of a media type
     * @param model the model of the page
     * @param request the HTTP request
     * @return the page, or the redirection to the error message for an unknown media type
     */
    @View( VIEW_MANAGE_MAPPINGS )
    public String getManageMappings( Models model, HttpServletRequest request )
    {
        LibraryMedia media = findMedia( request );

        if ( media == null )
        {
            return redirectMediaNotFound( request );
        }

        int nMediaId = media.getMediaId( );
        Collection<MediaAttribute> attributeList = MediaAttributeHome.findAllAttributesForMedia( nMediaId, getPlugin( ) );
        media.setMediaAttributeList( I18nService.localizeCollection( attributeList, getLocale( ) ) );

        Collection<LibraryMapping> colMappings = LibraryMappingHome.findAllMappingsByMedia( nMediaId, getPlugin( ) );

        model.put( MARK_MEDIA, media );
        model.put( MARK_MAPPING_LIST, colMappings );
        model.put( MARK_ASSOCIATION_LABELS, getAssociationLabels( colMappings ) );
        model.put( MARK_DOCUMENT_TYPE_LIST, DocumentTypeHome.getDocumentTypesList( ) );

        return getPage( PROPERTY_PAGE_TITLE_MAPPING_MANAGEMENT, TEMPLATE_MANAGE_MAPPINGS );
    }

    /**
     * Returns the form creating the mapping of a media type to a document type
     * @param model the model of the page
     * @param request the HTTP request
     * @return the page, or the redirection to the error message for an unknown media type or document type
     */
    @View( VIEW_CREATE_MAPPING )
    public String getCreateMapping( Models model, HttpServletRequest request )
    {
        LibraryMedia media = findMedia( request );

        if ( media == null )
        {
            return redirectMediaNotFound( request );
        }

        DocumentType documentType = findDocumentType( request );

        if ( documentType == null )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_DOCUMENT_TYPE_NOT_FOUND, AdminMessage.TYPE_STOP ) );
        }

        Collection<MediaAttribute> attributeList = MediaAttributeHome.findAllAttributesForMedia( media.getMediaId( ), getPlugin( ) );
        media.setMediaAttributeList( I18nService.localizeCollection( attributeList, getLocale( ) ) );

        model.put( MARK_MEDIA, media );
        model.put( MARK_DOCUMENT_TYPE, documentType );

        return getPage( PROPERTY_PAGE_TITLE_MAPPING_CREATION, TEMPLATE_CREATE_MAPPING );
    }

    /**
     * Creates the mapping of a media type to a document type
     * @param request the HTTP request
     * @return the redirection to the mappings of the media type, or to the error message
     */
    @Action( ACTION_CREATE_MAPPING )
    public String doCreateMapping( HttpServletRequest request )
    {
        LibraryMedia media = findMedia( request );

        if ( media == null )
        {
            return redirectMediaNotFound( request );
        }

        DocumentType documentType = findDocumentType( request );

        if ( documentType == null )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_DOCUMENT_TYPE_NOT_FOUND, AdminMessage.TYPE_STOP ) );
        }

        LibraryMapping mapping = new LibraryMapping( );
        mapping.setCodeDocumentType( documentType.getCode( ) );
        mapping.setIdMedia( media.getMediaId( ) );
        mapping.setAttributeAssociationList( new ArrayList<AttributeAssociation>( ) );
        populateAssociations( request, media, mapping );

        LibraryMappingHome.create( mapping, getPlugin( ) );

        return redirect( request, VIEW_MANAGE_MAPPINGS, PARAMETER_MEDIA_ID, media.getMediaId( ) );
    }

    /**
     * Returns the form modifying the associations of a mapping
     * @param model the model of the page
     * @param request the HTTP request
     * @return the page, or the redirection to the error message for an unknown mapping
     */
    @View( VIEW_MODIFY_MAPPING )
    public String getModifyMapping( Models model, HttpServletRequest request )
    {
        LibraryMapping mapping = findMapping( request );
        LibraryMedia media = ( mapping == null ) ? null : LibraryMediaHome.findByPrimaryKey( mapping.getIdMedia( ), getPlugin( ) );

        if ( media == null )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_MAPPING_NOT_FOUND, AdminMessage.TYPE_STOP ) );
        }

        Collection<MediaAttribute> attributeList = MediaAttributeHome.findAllAttributesForMedia( mapping.getIdMedia( ), getPlugin( ) );
        media.setMediaAttributeList( I18nService.localizeCollection( attributeList, getLocale( ) ) );

        model.put( MARK_MAPPING, mapping );
        model.put( MARK_MEDIA, media );
        model.put( MARK_DOCUMENT_TYPE, DocumentTypeHome.findByPrimaryKey( mapping.getCodeDocumentType( ) ) );
        model.put( MARK_DOCUMENT_TYPE_LIST, DocumentTypeHome.getDocumentTypesList( ) );

        return getPage( PROPERTY_PAGE_TITLE_MAPPING_MODIFICATION, TEMPLATE_MODIFY_MAPPING );
    }

    /**
     * Modifies the associations of a mapping; its media type and its document type stay those it was created with
     * @param request the HTTP request
     * @return the redirection to the mappings of the media type, or to the error message
     */
    @Action( ACTION_MODIFY_MAPPING )
    public String doModifyMappingAssociations( HttpServletRequest request )
    {
        LibraryMapping stored = findMapping( request );
        LibraryMedia media = ( stored == null ) ? null : LibraryMediaHome.findByPrimaryKey( stored.getIdMedia( ), getPlugin( ) );

        if ( media == null )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_MAPPING_NOT_FOUND, AdminMessage.TYPE_STOP ) );
        }

        LibraryMapping mapping = new LibraryMapping( );
        mapping.setIdMapping( stored.getIdMapping( ) );
        mapping.setIdMedia( stored.getIdMedia( ) );
        mapping.setCodeDocumentType( stored.getCodeDocumentType( ) );
        populateAssociations( request, media, mapping );

        LibraryMappingHome.update( mapping, getPlugin( ) );

        return redirect( request, VIEW_MANAGE_MAPPINGS, PARAMETER_MEDIA_ID, media.getMediaId( ) );
    }

    /**
     * Asks the confirmation of the removal of a mapping
     * @param request the HTTP request
     * @return the redirection to the confirmation message, or to the error message for an unknown mapping
     */
    @View( value = VIEW_CONFIRM_REMOVE_MAPPING, securityTokenAction = ACTION_REMOVE_MAPPING )
    public String getConfirmRemoveMapping( HttpServletRequest request )
    {
        LibraryMapping mapping = findMapping( request );

        if ( mapping == null )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_MAPPING_NOT_FOUND, AdminMessage.TYPE_STOP ) );
        }

        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_MAPPING ) );
        url.addParameter( PARAMETER_MAPPING_ID, mapping.getIdMapping( ) );

        return redirect( request,
                AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_REMOVE_MAPPING, url.getUrl( ), AdminMessage.TYPE_CONFIRMATION ) );
    }

    /**
     * Removes a mapping
     * @param request The request
     * @return The redirection to the mappings of the media type
     */
    @Action( ACTION_REMOVE_MAPPING )
    public String doRemoveMapping( HttpServletRequest request )
    {
        LibraryMapping mapping = findMapping( request );

        if ( mapping == null )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_MAPPING_NOT_FOUND, AdminMessage.TYPE_STOP ) );
        }

        LibraryMappingHome.remove( mapping.getIdMapping( ), getPlugin( ) );

        return redirect( request, VIEW_MANAGE_MAPPINGS, PARAMETER_MEDIA_ID, mapping.getIdMedia( ) );
    }

    /**
     * Returns the media type the request names by its media_id
     * @param request the HTTP request
     * @return the media type, null when the identifier is missing, not a number or unknown
     */
    private LibraryMedia findMedia( HttpServletRequest request )
    {
        int nMediaId = NumberUtils.toInt( request.getParameter( PARAMETER_MEDIA_ID ), -1 );

        return ( nMediaId < 0 ) ? null : LibraryMediaHome.findByPrimaryKey( nMediaId, getPlugin( ) );
    }

    /**
     * Returns the attribute the request names by its attribute_id
     * @param request the HTTP request
     * @return the attribute, null when the identifier is missing, not a number or unknown
     */
    private MediaAttribute findAttribute( HttpServletRequest request )
    {
        int nAttributeId = NumberUtils.toInt( request.getParameter( PARAMETER_ATTRIBUTE_ID ), -1 );

        return ( nAttributeId < 0 ) ? null : MediaAttributeHome.findByPrimaryKey( nAttributeId, getPlugin( ) );
    }

    /**
     * Returns the mapping the request names by its mapping_id
     * @param request the HTTP request
     * @return the mapping, null when the identifier is missing, not a number or unknown
     */
    private LibraryMapping findMapping( HttpServletRequest request )
    {
        int nMappingId = NumberUtils.toInt( request.getParameter( PARAMETER_MAPPING_ID ), -1 );

        return ( nMappingId < 0 ) ? null : LibraryMappingHome.findByPrimaryKey( nMappingId, getPlugin( ) );
    }

    /**
     * Returns the document type the request names by its document_type code
     * @param request the HTTP request
     * @return the document type, null when the code is missing or unknown
     */
    private DocumentType findDocumentType( HttpServletRequest request )
    {
        String strDocumentTypeCode = request.getParameter( PARAMETER_DOCUMENT_TYPE );

        return StringUtils.isBlank( strDocumentTypeCode ) ? null : DocumentTypeHome.findByPrimaryKey( strDocumentTypeCode );
    }

    /**
     * Redirects to the message of an unknown media type
     * @param request the HTTP request
     * @return the redirection
     */
    private String redirectMediaNotFound( HttpServletRequest request )
    {
        return redirect( request, AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_MEDIA_NOT_FOUND, AdminMessage.TYPE_STOP ) );
    }

    /**
     * Fills an attribute from the form, refusing a missing field and a code another attribute of the media type already has
     * @param request the HTTP request
     * @param attribute the attribute, its media type and, when it exists, its identifier set
     * @return the URL of the error message, null when the attribute is filled
     */
    private String populateAttribute( HttpServletRequest request, MediaAttribute attribute )
    {
        String strCode = request.getParameter( PARAMETER_ATTRIBUTE_CODE );
        String strDescription = request.getParameter( PARAMETER_ATTRIBUTE_DESCRIPTION );
        int nType = NumberUtils.toInt( request.getParameter( PARAMETER_ATTRIBUTE_TYPE ), -1 );
        String strDefaultValue = request.getParameter( PARAMETER_ATTRIBUTE_DEFAULT_VALUE );

        if ( StringUtils.isAnyBlank( strCode, strDescription ) || ( nType < 0 ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
        }

        boolean bDuplicate = MediaAttributeHome.findAllAttributesForMedia( attribute.getMediaId( ), getPlugin( ) ).stream( )
                .anyMatch( other -> ( other.getAttributeId( ) != attribute.getAttributeId( ) ) && strCode.equals( other.getCode( ) ) );

        if ( bDuplicate )
        {
            return AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_ATTRIBUTE_DUPLICATE_CODE, new Object [ ] {
                    strCode
            }, AdminMessage.TYPE_STOP );
        }

        attribute.setCode( strCode );
        attribute.setDescription( strDescription );
        attribute.setTypeId( nType );
        attribute.setDefaultValue( ( ( strDefaultValue != null ) && ( nType == MediaAttribute.ATTRIBUTE_TYPE_TEXT_USER ) ) ? strDefaultValue : "" );

        return null;
    }

    /**
     * Sets the associations of a mapping from the form: one document attribute for each media attribute that can be associated
     * @param request the HTTP request
     * @param media the media type of the mapping
     * @param mapping the mapping
     */
    private void populateAssociations( HttpServletRequest request, LibraryMedia media, LibraryMapping mapping )
    {
        for ( MediaAttribute attribute : MediaAttributeHome.findAllAttributesForMedia( media.getMediaId( ), getPlugin( ) ) )
        {
            int nDocumentAttributeId = NumberUtils
                    .toInt( request.getParameter( PARAMETER_PREFIX_DOCUMENT_ATTRIBUTE_ID + attribute.getAttributeId( ) ), -1 );

            if ( attribute.isAssociableWithDocument( ) && ( nDocumentAttributeId >= 0 ) )
            {
                mapping.addAttributeAssociation( attribute.getAttributeId( ), nDocumentAttributeId );
            }
        }
    }

    /**
     * Renders the labels of the associations of the mappings
     * @param colMappings the mappings
     * @return the labels, HTML rendered by a template, keyed by "idMapping-idMediaAttribute"
     */
    private Map<String, Object> getAssociationLabels( Collection<LibraryMapping> colMappings )
    {
        Map<String, Object> mapAssociationLabels = new HashMap<>( );

        for ( LibraryMapping mapping : colMappings )
        {
            if ( mapping.getAttributeAssociationList( ) == null )
            {
                continue;
            }

            for ( AttributeAssociation association : mapping.getAttributeAssociationList( ) )
            {
                MediaAttribute mediaAttribute = MediaAttributeHome.findByPrimaryKey( association.getIdMediaAttribute( ), getPlugin( ) );
                DocumentAttribute documentAttribute = DocumentAttributeHome.findByPrimaryKey( association.getIdDocumentAttribute( ) );

                Map<String, Object> modelLabel = new HashMap<>( );
                modelLabel.put( MARK_ATTRIBUTE, mediaAttribute );
                modelLabel.put( MARK_DOCUMENT_ATTRIBUTE, documentAttribute );

                HtmlTemplate templateLabel = AppTemplateService.getTemplate( TEMPLATE_GENERATE_ASSOCIATION_LABEL, getLocale( ), modelLabel );
                mapAssociationLabels.put( mapping.getIdMapping( ) + "-" + association.getIdMediaAttribute( ), HtmlMarkup.of( templateLabel.getHtml( ) ) );
            }
        }

        return mapAssociationLabels;
    }

    /**
     * Returns the types of attribute, localized
     * @param locale the locale
     * @return the types
     */
    private ReferenceList getAttributeTypeReferenceList( Locale locale )
    {
        ReferenceList refList = new ReferenceList( );
        addAttributeType( refList, MediaAttribute.ATTRIBUTE_TYPE_TEXT_USER, MediaAttribute.PROPERTY_ATTRIBUTE_TYPE_LABEL_TEXT_USER, locale );
        addAttributeType( refList, MediaAttribute.ATTRIBUTE_TYPE_TITLE_DOCUMENT, MediaAttribute.PROPERTY_ATTRIBUTE_TYPE_LABEL_TITLE_DOCUMENT, locale );
        addAttributeType( refList, MediaAttribute.ATTRIBUTE_TYPE_SUMMARY_DOCUMENT, MediaAttribute.PROPERTY_ATTRIBUTE_TYPE_LABEL_SUMMARY_DOCUMENT, locale );
        addAttributeType( refList, MediaAttribute.ATTRIBUTE_TYPE_TEXT_DOCUMENT, MediaAttribute.PROPERTY_ATTRIBUTE_TYPE_LABEL_TEXT_DOCUMENT, locale );
        addAttributeType( refList, MediaAttribute.ATTRIBUTE_TYPE_BINARY, MediaAttribute.PROPERTY_ATTRIBUTE_TYPE_LABEL_BINARY, locale );

        return refList;
    }

    /**
     * Adds a type of attribute to a list
     * @param refList the list
     * @param nType the type
     * @param strLabelKey the key of its label
     * @param locale the locale
     */
    private static void addAttributeType( ReferenceList refList, int nType, String strLabelKey, Locale locale )
    {
        ReferenceItem item = new ReferenceItem( );
        item.setCode( String.valueOf( nType ) );
        item.setName( I18nService.getLocalizedString( strLabelKey, locale ) );
        refList.add( item );
    }
}
