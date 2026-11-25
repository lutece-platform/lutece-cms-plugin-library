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
import jakarta.enterprise.inject.spi.CDI;

import java.util.Collection;
import java.util.List;


/**
 * Home of the media types
 */
public final class LibraryMediaHome
{
    private static ILibraryMediaDAO _dao = CDI.current( ).select( ILibraryMediaDAO.class ).get( );

    /**
     * Private constructor
     */
    private LibraryMediaHome(  )
    {
    }

    /**
     * Finds all the media types, without their attributes
     * @param plugin the plugin
     * @return the media types
     */
    public static Collection<LibraryMedia> findAllMedia( Plugin plugin )
    {
        return _dao.selectAll( plugin );
    }

    /**
     * Finds a media type, without its attributes
     * @param nMediaId the identifier of the media type
     * @param plugin the plugin
     * @return the media type, null if it does not exist
     */
    public static LibraryMedia findByPrimaryKey( int nMediaId, Plugin plugin )
    {
        return _dao.load( nMediaId, plugin );
    }

    /**
     * Creates a media type
     * @param media the media type
     * @param plugin the plugin
     */
    public static void create( LibraryMedia media, Plugin plugin )
    {
        _dao.insert( media, plugin );
    }

    /**
     * Updates a media type
     * @param media the media type
     * @param plugin the plugin
     */
    public static void update( LibraryMedia media, Plugin plugin )
    {
        _dao.store( media, plugin );
    }

    /**
     * Removes a media type
     * @param nMediaId the identifier of the media type
     * @param plugin the plugin
     */
    public static void remove( int nMediaId, Plugin plugin )
    {
        _dao.delete( nMediaId, plugin );
    }

    /**
     * Finds the legacy XSL stylesheet of a media type. To be called only while legacy stylesheets are pending: the column exists only on a site upgraded from a version with stylesheets
     * @param nMediaId the identifier of the media type
     * @param plugin the plugin
     * @return the source of the stylesheet, null if the media type has none
     */
    public static String findLegacyStylesheet( int nMediaId, Plugin plugin )
    {
        return _dao.selectLegacyStylesheet( nMediaId, plugin );
    }

    /**
     * Finds the media types that still have a legacy XSL stylesheet. To be called only while legacy stylesheets are pending
     * @param plugin the plugin
     * @return the identifiers of the media types
     */
    public static List<Integer> findIdsWithLegacyStylesheet( Plugin plugin )
    {
        return _dao.selectIdsWithLegacyStylesheet( plugin );
    }

    /**
     * Discards the legacy XSL stylesheet of a media type. To be called only while legacy stylesheets are pending
     * @param nMediaId the identifier of the media type
     * @param plugin the plugin
     */
    public static void removeLegacyStylesheet( int nMediaId, Plugin plugin )
    {
        _dao.deleteLegacyStylesheet( nMediaId, plugin );
    }
}
