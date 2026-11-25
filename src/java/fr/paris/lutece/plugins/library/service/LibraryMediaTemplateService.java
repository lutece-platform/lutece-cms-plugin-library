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

import fr.paris.lutece.plugins.library.business.LibraryMedia;
import fr.paris.lutece.plugins.library.business.LibraryMediaHome;
import fr.paris.lutece.plugins.library.business.MediaAttribute;
import fr.paris.lutece.plugins.resource.loader.ResourceNotFoundException;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.util.html.exception.LuteceFreemarkerException;

import jakarta.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Renders the HTML of a media type with its FreeMarker template, and keeps the XSL stylesheets of the media types readable until they are
 * rewritten as templates.
 *
 * Model of a media template:
 * <ul>
 * <li>media: the values of the first selected document, keyed by the codes of the attributes of the media type</li>
 * <li>media_list: the values of every selected document, in their order, for a multiple media type</li>
 * <li>mediatype: the media type</li>
 * </ul>
 */
@ApplicationScoped
public class LibraryMediaTemplateService
{
    /** Datastore key telling that some media types still have a legacy XSL stylesheet */
    public static final String DATASTORE_KEY_LEGACY_PENDING = "library.legacyStylesheets.pending";

    /** Template used by a media type without specific template */
    public static final String TEMPLATE_DEFAULT = "admin/plugins/library/media_default.html";

    /** Values of the first document */
    public static final String MARK_MEDIA = "media";

    /** Values of every document */
    public static final String MARK_MEDIA_LIST = "media_list";

    /** The media type */
    public static final String MARK_MEDIA_TYPE = "mediatype";

    private static final String MESSAGE_OUTPUT_FORMAT = "library.message.media.templateOutputFormat";
    private static final String OUTPUT_FORMAT_HTML = "HTML";
    private static final String FTL_HEADER_HTML = "<#ftl output_format=\"HTML\">";
    private static final String FTL_PARAMETER_OUTPUT_FORMAT_HTML = " output_format=\"HTML\"";
    private static final Pattern PATTERN_FTL_HEADER = Pattern.compile( "^\\s*<#ftl\\b([^>]*)>" );
    private static final Pattern PATTERN_OUTPUT_FORMAT = Pattern.compile( "\\boutput_format\\s*=\\s*([\"'])(.*?)\\1" );
    private static final Pattern PATTERN_AUTO_ESC_OFF = Pattern.compile( "\\bauto_esc\\s*=\\s*false\\b" );
    private static final String PATH_TEMPLATES = "/WEB-INF/templates/";
    private static final String SAMPLE_TEXT = "Lorem ipsum";
    private static final String SAMPLE_URL = "servlet/plugins/document/resource?id=0&id_attribute=0";
    private static final int SAMPLE_MEDIA_COUNT = 2;

    /**
     * Renders the HTML of a media type for the selected documents. A specific template that fails is logged and the default template is used
     * @param mediaType the media type
     * @param listMedia the values of each selected document, keyed by attribute code
     * @param locale the locale
     * @return the HTML
     */
    public String render( LibraryMedia mediaType, List<Map<String, String>> listMedia, Locale locale )
    {
        Map<String, Object> model = buildModel( mediaType, listMedia );

        if ( StringUtils.isNotBlank( mediaType.getTemplate(  ) ) )
        {
            String strTemplate = toHtmlOutputFormat( mediaType.getTemplate(  ) );

            if ( strTemplate == null )
            {
                AppLogService.error( "The template of the media type {} does not escape its values as HTML", mediaType.getMediaId(  ) );
            }
            else
            {
                try
                {
                    return AppTemplateService.getTemplateFromStringFtl( strTemplate, locale, model ).getHtml(  );
                }
                catch ( LuteceFreemarkerException e )
                {
                    AppLogService.error( "Error rendering the template of the media type {}", mediaType.getMediaId(  ), e );
                }
            }
        }

        return AppTemplateService.getTemplate( TEMPLATE_DEFAULT, locale, model ).getHtml(  );
    }

    /**
     * Checks a template by rendering it with sample values for each attribute of the media type
     * @param mediaType the media type, with its attributes
     * @param strTemplate the template to check
     * @param locale the locale
     * @return null if the template is valid, otherwise the error message
     */
    public String validateTemplate( LibraryMedia mediaType, String strTemplate, Locale locale )
    {
        if ( StringUtils.isBlank( strTemplate ) )
        {
            return null;
        }

        String strHtmlTemplate = toHtmlOutputFormat( strTemplate );

        if ( strHtmlTemplate == null )
        {
            return I18nService.getLocalizedString( MESSAGE_OUTPUT_FORMAT, locale );
        }

        List<Map<String, String>> listMedia = new ArrayList<>(  );

        for ( int i = 0; i < SAMPLE_MEDIA_COUNT; i++ )
        {
            Map<String, String> mapValues = new HashMap<>(  );

            if ( mediaType.getMediaAttributeList(  ) != null )
            {
                for ( MediaAttribute attribute : mediaType.getMediaAttributeList(  ) )
                {
                    mapValues.put( attribute.getCode(  ), getSampleValue( attribute ) );
                }
            }

            listMedia.add( mapValues );
        }

        try
        {
            AppTemplateService.getTemplateFromStringFtl( strHtmlTemplate, locale, buildModel( mediaType, listMedia ) );

            return null;
        }
        catch ( LuteceFreemarkerException e )
        {
            return e.getMessage(  );
        }
    }

    /**
     * Returns the source of the default template, a starting point to write the template of a media type
     * @return the source of the template, an empty string if it can not be read
     */
    public String getDefaultTemplateSource(  )
    {
        try ( InputStream stream = AppPathService.getResourceStream( PATH_TEMPLATES, TEMPLATE_DEFAULT ) )
        {
            return new String( stream.readAllBytes(  ), StandardCharsets.UTF_8 );
        }
        catch ( ResourceNotFoundException | IOException e )
        {
            AppLogService.error( "Unable to read the default media template {}", TEMPLATE_DEFAULT, e );

            return StringUtils.EMPTY;
        }
    }

    /**
     * Tells if some media types still have a legacy XSL stylesheet
     * @return true if legacy stylesheets remain
     */
    public boolean isLegacyPending(  )
    {
        return DatastoreService.VALUE_TRUE.equals( DatastoreService.getInstanceDataValue( DATASTORE_KEY_LEGACY_PENDING,
                DatastoreService.VALUE_FALSE ) );
    }

    /**
     * Returns the identifiers of the media types that still have a legacy XSL stylesheet
     * @param plugin the plugin
     * @return the identifiers, empty if there are none
     */
    public List<Integer> getMediaIdsWithLegacyStylesheet( Plugin plugin )
    {
        return isLegacyPending(  ) ? LibraryMediaHome.findIdsWithLegacyStylesheet( plugin ) : Collections.emptyList(  );
    }

    /**
     * Returns the legacy XSL stylesheet of a media type, to rewrite it as a template
     * @param nMediaId the identifier of the media type
     * @param plugin the plugin
     * @return the source of the stylesheet, null if the media type has none
     */
    public String getLegacyStylesheet( int nMediaId, Plugin plugin )
    {
        return isLegacyPending(  ) ? LibraryMediaHome.findLegacyStylesheet( nMediaId, plugin ) : null;
    }

    /**
     * Discards the legacy XSL stylesheet of a media type, once rewritten as a template. When no media type has a legacy stylesheet any more,
     * the stylesheets are no longer read: their column is left in place, a later upgrade script may drop it
     * @param nMediaId the identifier of the media type
     * @param plugin the plugin
     */
    public void discardLegacyStylesheet( int nMediaId, Plugin plugin )
    {
        if ( !isLegacyPending(  ) )
        {
            return;
        }

        LibraryMediaHome.removeLegacyStylesheet( nMediaId, plugin );

        if ( LibraryMediaHome.findIdsWithLegacyStylesheet( plugin ).isEmpty(  ) )
        {
            DatastoreService.setInstanceDataValue( DATASTORE_KEY_LEGACY_PENDING, DatastoreService.VALUE_FALSE );
        }
    }

    /**
     * Returns a template rendered with the HTML output format, so that the values of the documents are always HTML-escaped: the values posted
     * by the insert form are decoded before rendering (LibraryInsertPage.getMediaValue), a template that did not escape them would insert
     * them as markup. A template without ftl header gets one, and a header without output format gets the HTML one; the header is kept on
     * the first line so that the line numbers of the errors stay those of the template.
     * @param strTemplate the template of a media type
     * @return the template with the HTML output format, or null if its ftl header asks for another output format or disables auto-escaping
     */
    private static String toHtmlOutputFormat( String strTemplate )
    {
        Matcher header = PATTERN_FTL_HEADER.matcher( strTemplate );

        if ( !header.find(  ) )
        {
            return FTL_HEADER_HTML + strTemplate;
        }

        String strParameters = header.group( 1 );

        if ( PATTERN_AUTO_ESC_OFF.matcher( strParameters ).find(  ) )
        {
            return null;
        }

        Matcher outputFormat = PATTERN_OUTPUT_FORMAT.matcher( strParameters );

        if ( outputFormat.find(  ) )
        {
            return OUTPUT_FORMAT_HTML.equals( outputFormat.group( 2 ) ) ? strTemplate : null;
        }

        return strTemplate.substring( 0, header.end( 1 ) ) + FTL_PARAMETER_OUTPUT_FORMAT_HTML + strTemplate.substring( header.end( 1 ) );
    }

    /**
     * Builds the model of a media template
     * @param mediaType the media type
     * @param listMedia the values of each selected document
     * @return the model
     */
    private Map<String, Object> buildModel( LibraryMedia mediaType, List<Map<String, String>> listMedia )
    {
        Map<String, Object> model = new LinkedHashMap<>(  );
        model.put( MARK_MEDIA_TYPE, mediaType );
        model.put( MARK_MEDIA_LIST, listMedia );
        model.put( MARK_MEDIA, listMedia.isEmpty(  ) ? Collections.emptyMap(  ) : listMedia.get( 0 ) );

        return model;
    }

    /**
     * Returns a sample value for an attribute, used to check a template
     * @param attribute the attribute
     * @return the default value of the attribute, or a sample URL for a file, or a sample text
     */
    private String getSampleValue( MediaAttribute attribute )
    {
        if ( StringUtils.isNotBlank( attribute.getDefaultValue(  ) ) )
        {
            return attribute.getDefaultValue(  );
        }

        return ( attribute.getTypeId(  ) == MediaAttribute.ATTRIBUTE_TYPE_BINARY ) ? SAMPLE_URL : SAMPLE_TEXT;
    }
}
