/*
 * Copyright (c) 2002-2026, Mairie de Paris
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
package fr.paris.lutece.plugins.library.service;

import fr.paris.lutece.plugins.document.business.Document;
import fr.paris.lutece.plugins.document.business.DocumentType;
import fr.paris.lutece.plugins.document.business.DocumentTypeHome;
import fr.paris.lutece.plugins.document.business.attributes.DocumentAttribute;
import fr.paris.lutece.plugins.document.business.attributes.DocumentAttributeHome;
import fr.paris.lutece.plugins.document.business.spaces.DocumentSpaceHome;
import fr.paris.lutece.plugins.document.business.workflow.DocumentAction;
import fr.paris.lutece.plugins.document.business.workflow.DocumentActionHome;
import fr.paris.lutece.plugins.document.service.DocumentException;
import fr.paris.lutece.plugins.document.service.DocumentService;
import fr.paris.lutece.plugins.document.service.DocumentTypeResourceIdService;
import fr.paris.lutece.plugins.library.business.LibraryMapping;
import fr.paris.lutece.plugins.library.business.LibraryMapping.AttributeAssociation;
import fr.paris.lutece.plugins.library.business.LibraryMappingHome;
import fr.paris.lutece.plugins.library.business.LibraryMediaHome;
import fr.paris.lutece.plugins.library.business.MediaAttribute;
import fr.paris.lutece.plugins.library.business.MediaAttributeHome;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.insert.InsertResourceIdService;
import fr.paris.lutece.portal.service.insert.InsertService;
import fr.paris.lutece.portal.service.insert.InsertServiceManager;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.upload.MultipartItem;
import fr.paris.lutece.portal.web.upload.MultipartHttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Upload of a file from a rich text editor: the file becomes a document of the media type of the library insert service, in its space, both set
 * in the datastore (the keys used by the image and pdf buttons of the editors). Available only when the document plugin is enabled, both keys are
 * set and the user may create such a document there.
 */
@ApplicationScoped
public class EditorUploadService
{
    /** The insert service of the library that uploads a media */
    public static final String UPLOAD_INSERT_SERVICE_ID = "libraryuploadlinkservice";

    private static final String PLUGIN_DOCUMENT = "document";
    private static final String PLUGIN_LIBRARY = "library";
    private static final String PARAMETER_DOCUMENT_TITLE = "document_title";
    private static final String PARAMETER_DOCUMENT_SUMMARY = "document_summary";
    private static final String URL_DOCUMENT_RESOURCE = "servlet/plugins/document/resource?id=";
    private static final String URL_PARAMETER_ATTRIBUTE = "&id_attribute=";
    private static final String MESSAGE_NOT_AVAILABLE = "library.editorUpload.message.notAvailable";
    private static final String MESSAGE_NO_FILE = "library.editorUpload.message.noFile";
    private static final String MESSAGE_DOCUMENT_ERROR = "library.uploadinsertservice.message.documentError";
    private static final String DEFAULT_TITLE = "media";

    /**
     * The kinds of file an editor uploads, with the datastore keys of their media type and space
     */
    public enum Kind
    {
        IMAGE( "library.insert_service_media_type_image", "library.insert_service_media_type_image_space" ),
        PDF( "library.insert_service_media_type_pdf", "library.insert_service_media_type_pdf_space" );

        private final String _strMediaTypeKey;
        private final String _strSpaceKey;

        Kind( String strMediaTypeKey, String strSpaceKey )
        {
            _strMediaTypeKey = strMediaTypeKey;
            _strSpaceKey = strSpaceKey;
        }

        /**
         * @param strKind
         *            image or pdf, whatever the case
         * @return the kind, null when unknown
         */
        public static Kind fromString( String strKind )
        {
            for ( Kind kind : values( ) )
            {
                if ( kind.name( ).equalsIgnoreCase( strKind ) )
                {
                    return kind;
                }
            }

            return null;
        }
    }

    /**
     * Where a kind of file goes: the document type and space, and the document attribute that receives the file
     */
    private static final class Target
    {
        private final String _strDocumentType;
        private final int _nSpaceId;
        private final DocumentAttribute _fileAttribute;

        private Target( String strDocumentType, int nSpaceId, DocumentAttribute fileAttribute )
        {
            _strDocumentType = strDocumentType;
            _nSpaceId = nSpaceId;
            _fileAttribute = fileAttribute;
        }
    }

    /**
     * The result of an upload: the URL of the file and its title, or the error message
     */
    public static final class Result
    {
        private final String _strLocation;
        private final String _strTitle;
        private final String _strError;

        private Result( String strLocation, String strTitle, String strError )
        {
            _strLocation = strLocation;
            _strTitle = strTitle;
            _strError = strError;
        }

        /**
         * @return the URL of the uploaded file, null on error
         */
        public String getLocation( )
        {
            return _strLocation;
        }

        /**
         * @return the title of the document, null on error
         */
        public String getTitle( )
        {
            return _strTitle;
        }

        /**
         * @return the error message, null on success
         */
        public String getError( )
        {
            return _strError;
        }
    }

    @Inject
    private DocumentService _documentService;

    /**
     * Tells whether the user can upload this kind of file from an editor
     *
     * @param kind
     *            the kind of file
     * @param user
     *            the user
     * @return true if the upload is available
     */
    public boolean isAvailable( Kind kind, AdminUser user )
    {
        return getTarget( kind, user ) != null;
    }

    /**
     * Creates a document from an uploaded file, then validates it, as the quick image insertion of the upload insert service does
     *
     * @param request
     *            the multipart request of the upload
     * @param file
     *            the uploaded file
     * @param kind
     *            the kind of file
     * @param user
     *            the user
     * @param locale
     *            the locale of the messages
     * @return the URL of the file, or the error message
     */
    public Result upload( HttpServletRequest request, MultipartItem file, Kind kind, AdminUser user, Locale locale )
    {
        Target target = getTarget( kind, user );

        if ( target == null )
        {
            return error( MESSAGE_NOT_AVAILABLE, locale );
        }

        if ( ( file == null ) || ( file.getSize( ) == 0 ) )
        {
            return error( MESSAGE_NO_FILE, locale );
        }

        String strTitle = getTitle( file.getName( ) );
        DocumentType documentType = DocumentTypeHome.findByPrimaryKey( target._strDocumentType );

        // The request of the document form: title, summary, the file in its attribute, the file name in the required text attributes
        Map<String, String [ ]> mapParameters = new HashMap<>( );
        mapParameters.put( PARAMETER_DOCUMENT_TITLE, new String [ ] { strTitle } );
        mapParameters.put( PARAMETER_DOCUMENT_SUMMARY, new String [ ] { strTitle } );

        for ( DocumentAttribute attribute : documentType.getAttributes( ) )
        {
            if ( attribute.isRequired( ) && !attribute.getCode( ).equals( target._fileAttribute.getCode( ) ) )
            {
                mapParameters.put( attribute.getCode( ), new String [ ] { strTitle } );
            }
        }

        Map<String, List<MultipartItem>> mapFiles = new HashMap<>( );
        mapFiles.put( target._fileAttribute.getCode( ), List.of( file ) );

        MultipartHttpServletRequest documentRequest = new MultipartHttpServletRequest( request, mapFiles, mapParameters );
        Document document = new Document( );
        document.setCodeDocumentType( target._strDocumentType );

        if ( _documentService.getDocumentData( documentRequest, document, locale ) != null )
        {
            // getDocumentData stored its error as the admin message of the request
            AdminMessage message = AdminMessageService.getMessage( request );

            return new Result( null, null, ( message != null ) ? message.getText( locale ) : I18nService.getLocalizedString( MESSAGE_DOCUMENT_ERROR, locale ) );
        }

        document.setSpaceId( target._nSpaceId );
        document.setStateId( 1 );
        document.setCreatorId( user.getUserId( ) );

        try
        {
            _documentService.createDocument( document, user );

            DocumentAction action = DocumentActionHome.findByPrimaryKey( DocumentAction.ACTION_VALIDATE );
            _documentService.validateDocument( document, user, action.getFinishDocumentState( ).getId( ) );
        }
        catch( DocumentException e )
        {
            return new Result( null, null, I18nService.getLocalizedString( MESSAGE_DOCUMENT_ERROR, locale ) + " "
                    + I18nService.getLocalizedString( e.getI18nMessage( ), locale ) );
        }

        DocumentAttribute fileAttribute = document.getAttribute( target._fileAttribute.getCode( ) );

        return new Result( URL_DOCUMENT_RESOURCE + document.getId( ) + URL_PARAMETER_ATTRIBUTE + fileAttribute.getId( ), strTitle, null );
    }

    /**
     * The document type, space and file attribute of a kind of file, when the upload is available to the user
     *
     * @param kind
     *            the kind of file
     * @param user
     *            the user
     * @return the target, null when the upload is not available
     */
    private Target getTarget( Kind kind, AdminUser user )
    {
        if ( ( kind == null ) || ( user == null ) || !PluginService.isPluginEnable( PLUGIN_DOCUMENT ) || !PluginService.isPluginEnable( PLUGIN_LIBRARY ) )
        {
            return null;
        }

        int nMediaTypeId = getId( kind._strMediaTypeKey );
        int nSpaceId = getId( kind._strSpaceKey );
        Plugin plugin = PluginService.getPlugin( PLUGIN_LIBRARY );

        if ( ( nMediaTypeId <= 0 ) || ( nSpaceId <= 0 ) || ( LibraryMediaHome.findByPrimaryKey( nMediaTypeId, plugin ) == null )
                || ( DocumentSpaceHome.findByPrimaryKey( nSpaceId ) == null ) )
        {
            return null;
        }

        InsertService insertService = InsertServiceManager.getInsertService( UPLOAD_INSERT_SERVICE_ID );

        if ( ( insertService == null ) || !RBACService.isAuthorized( insertService, InsertResourceIdService.PERMISSION_USE, user ) )
        {
            return null;
        }

        for ( LibraryMapping mapping : LibraryMappingHome.findAllMappingsByMedia( nMediaTypeId, plugin ) )
        {
            DocumentAttribute fileAttribute = getFileAttribute( mapping, plugin );

            if ( ( fileAttribute != null ) && _documentService.isAuthorizedAdminDocument( nSpaceId, mapping.getCodeDocumentType( ),
                    DocumentTypeResourceIdService.PERMISSION_CREATE, user ) )
            {
                return new Target( mapping.getCodeDocumentType( ), nSpaceId, fileAttribute );
            }
        }

        return null;
    }

    /**
     * The document attribute that a binary media attribute of the mapping is associated with
     *
     * @param mapping
     *            the mapping of the media type to a document type
     * @param plugin
     *            the library plugin
     * @return the document attribute, null when the mapping has no binary attribute
     */
    private static DocumentAttribute getFileAttribute( LibraryMapping mapping, Plugin plugin )
    {
        for ( AttributeAssociation association : mapping.getAttributeAssociationList( ) )
        {
            MediaAttribute mediaAttribute = MediaAttributeHome.findByPrimaryKey( association.getIdMediaAttribute( ), plugin );

            if ( ( mediaAttribute != null ) && ( mediaAttribute.getTypeId( ) == MediaAttribute.ATTRIBUTE_TYPE_BINARY ) )
            {
                return DocumentAttributeHome.findByPrimaryKey( association.getIdDocumentAttribute( ) );
            }
        }

        return null;
    }

    /**
     * @param strKey
     *            a datastore key
     * @return its value as an id, 0 when it is not set
     */
    private static int getId( String strKey )
    {
        String strValue = DatastoreService.getDataValue( strKey, StringUtils.EMPTY ).trim( );

        return StringUtils.isNumeric( strValue ) && !strValue.isEmpty( ) ? Integer.parseInt( strValue ) : 0;
    }

    /**
     * The title of the document: the file name without its extension, without the characters a document title may not hold
     *
     * @param strFileName
     *            the name of the uploaded file
     * @return the title
     */
    private static String getTitle( String strFileName )
    {
        String strTitle = StringUtils.trimToEmpty( FilenameUtils.getBaseName( StringUtils.defaultString( strFileName ) ) ).replaceAll( "[<>&\"']", " " ).trim( );

        return strTitle.isEmpty( ) ? DEFAULT_TITLE : strTitle;
    }

    private static Result error( String strMessageKey, Locale locale )
    {
        return new Result( null, null, I18nService.getLocalizedString( strMessageKey, locale ) );
    }
}
