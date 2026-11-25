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

import fr.paris.lutece.portal.service.template.AppTemplateService;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * Page ending an insertion of the library: it hands the HTML of the media to the editor that opened the insert service.
 * It renders the page of the core DoInsertIntoElement.jsp in the response to the form of the insert service, instead of
 * redirecting the browser to it: a redirect answering a form is checked against the form-action directive of the
 * Content-Security-Policy, which blocks it.
 */
public final class LibraryInsertPage
{
    private static final String TEMPLATE_INSERT_INTO_ELEMENT = "admin/insert/insert_into_element.html";
    private static final String MARK_INPUT = "input";
    private static final String MARK_INSERT = "insert";
    private static final String REGEX_LINE_BREAKS = "[\\r\\n]";

    /**
     * Private constructor
     */
    private LibraryInsertPage(  )
    {
    }

    /**
     * Returns the value of a media attribute posted by the insert form, as plain text. The XSS filter of the back office has already encoded
     * it as HTML (resource?id=1&amp;id_attribute=2 becomes resource?id&#61;1&amp;amp;id_attribute&#61;2), and the template of the media type
     * escapes it again (LibraryMediaTemplateService always renders it with the HTML output format): decoded here, the value is escaped once, which the browser reads back as the original url.
     * @param request the request of the insert form
     * @param strName the name of the parameter
     * @return the plain value, or null if the parameter is absent
     */
    public static String getMediaValue( HttpServletRequest request, String strName )
    {
        String strValue = request.getParameter( strName );

        return ( strValue == null ) ? null : StringEscapeUtils.unescapeHtml4( strValue );
    }

    /**
     * Returns the page inserting HTML into the editor
     * @param strInput the editor input that opened the insert service
     * @param strInsert the HTML to insert, escaped for a JavaScript string
     * @param locale the locale
     * @return the HTML of the page
     */
    public static String getHtml( String strInput, String strInsert, Locale locale )
    {
        Map<String, Object> model = new HashMap<>(  );
        model.put( MARK_INPUT, strInput );
        model.put( MARK_INSERT, strInsert.replaceAll( REGEX_LINE_BREAKS, "" ) );

        return AppTemplateService.getTemplate( TEMPLATE_INSERT_INTO_ELEMENT, locale, model ).getHtml(  );
    }
}
