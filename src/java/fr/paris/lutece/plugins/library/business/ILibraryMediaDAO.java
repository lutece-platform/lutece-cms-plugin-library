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
package fr.paris.lutece.plugins.library.business;

import fr.paris.lutece.portal.service.plugin.Plugin;

import java.util.Collection;
import java.util.List;


/**
 * DAO interface of the media types
 */
public interface ILibraryMediaDAO
{
    /**
     * Loads all the media types, without their attributes
     * @param plugin the plugin
     * @return the media types
     */
    Collection<LibraryMedia> selectAll( Plugin plugin );

    /**
     * Loads a media type, without its attributes
     * @param nMediaId the identifier of the media type
     * @param plugin the plugin
     * @return the media type, null if it does not exist
     */
    LibraryMedia load( int nMediaId, Plugin plugin );

    /**
     * Creates a media type and sets its new identifier
     * @param media the media type
     * @param plugin the plugin
     */
    void insert( LibraryMedia media, Plugin plugin );

    /**
     * Updates a media type
     * @param media the media type
     * @param plugin the plugin
     */
    void store( LibraryMedia media, Plugin plugin );

    /**
     * Removes a media type
     * @param nMediaId the identifier of the media type
     * @param plugin the plugin
     */
    void delete( int nMediaId, Plugin plugin );

    /**
     * Loads the XSL stylesheet a media type had before the FreeMarker templates. The stylesheet column exists only until every legacy
     * stylesheet has been rewritten
     * @param nMediaId the identifier of the media type
     * @param plugin the plugin
     * @return the source of the stylesheet, null if the media type has none
     */
    String selectLegacyStylesheet( int nMediaId, Plugin plugin );

    /**
     * Lists the media types that still have a legacy XSL stylesheet
     * @param plugin the plugin
     * @return the identifiers of the media types
     */
    List<Integer> selectIdsWithLegacyStylesheet( Plugin plugin );

    /**
     * Discards the legacy XSL stylesheet of a media type
     * @param nMediaId the identifier of the media type
     * @param plugin the plugin
     */
    void deleteLegacyStylesheet( int nMediaId, Plugin plugin );
}
