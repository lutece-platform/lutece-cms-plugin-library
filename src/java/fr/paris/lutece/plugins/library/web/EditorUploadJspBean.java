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
package fr.paris.lutece.plugins.library.web;

import fr.paris.lutece.plugins.library.service.EditorUploadService;
import fr.paris.lutece.plugins.library.service.EditorUploadService.Kind;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.security.SecurityTokenService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.ResponseBody;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.web.insert.InsertServiceSelectorJspBean;
import fr.paris.lutece.portal.web.upload.MultipartHttpServletRequest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Upload of images and PDF files from the rich text editors of the back office (TinyMCE, Autumn Note). It requires the right of the insert
 * services, as the list of the insert services the editors read.
 */
@RequestScoped
@Named
@Controller( controllerJsp = EditorUploadJspBean.CONTROLLER_JSP, controllerPath = EditorUploadJspBean.CONTROLLER_PATH, right = InsertServiceSelectorJspBean.RIGHT_MANAGE_LINK_SERVICE, securityTokenEnabled = true )
public class EditorUploadJspBean extends MVCAdminJspBean
{
    static final String CONTROLLER_JSP = "EditorUpload.jsp";
    static final String CONTROLLER_PATH = "jsp/admin/plugins/library/";

    private static final long serialVersionUID = 1L;

    // Views
    private static final String VIEW_GET_UPLOAD_CONFIG = "getUploadConfig";

    // Actions
    private static final String ACTION_UPLOAD = "upload";

    // Parameters
    private static final String PARAMETER_FILE = "file";
    private static final String PARAMETER_KIND = "kind";

    // JSON keys
    private static final String KEY_IMAGE = "image";
    private static final String KEY_PDF = "pdf";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_TITLE = "title";
    private static final String KEY_ERROR = "error";

    @Inject
    private EditorUploadService _editorUploadService;

    /**
     * Tells the editor which kinds of file it may upload, with a token for the next upload. A view, read with a GET: it changes nothing, and
     * another site may send it but not read the token.
     *
     * @param request
     *            the request
     * @return image and pdf, true when the user may upload them, and token
     */
    @View( VIEW_GET_UPLOAD_CONFIG )
    @ResponseBody
    public Map<String, Object> getUploadConfig( HttpServletRequest request )
    {
        AdminUser user = AdminUserService.getAdminUser( request );
        Map<String, Object> config = new LinkedHashMap<>( );
        config.put( KEY_IMAGE, _editorUploadService.isAvailable( Kind.IMAGE, user ) );
        config.put( KEY_PDF, _editorUploadService.isAvailable( Kind.PDF, user ) );
        config.put( KEY_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_UPLOAD ) );

        return config;
    }

    /**
     * Uploads a file posted by an editor, with the token of getUploadConfig
     *
     * @param request
     *            the multipart request: file, kind (image or pdf), token
     * @return location and title of the file, or error
     */
    @Action( ACTION_UPLOAD )
    @ResponseBody
    public Map<String, Object> doUpload( HttpServletRequest request )
    {
        Map<String, Object> result = new LinkedHashMap<>( );

        if ( !( request instanceof MultipartHttpServletRequest ) )
        {
            result.put( KEY_ERROR, "multipart/form-data expected" );

            return result;
        }

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        EditorUploadService.Result upload = _editorUploadService.upload( request, multipartRequest.getFile( PARAMETER_FILE ),
                Kind.fromString( request.getParameter( PARAMETER_KIND ) ), AdminUserService.getAdminUser( request ), getLocale( ) );

        if ( upload.getError( ) != null )
        {
            result.put( KEY_ERROR, upload.getError( ) );
        }
        else
        {
            result.put( KEY_LOCATION, upload.getLocation( ) );
            result.put( KEY_TITLE, upload.getTitle( ) );
        }

        return result;
    }
}
