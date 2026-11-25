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
import fr.paris.lutece.util.sql.DAOUtil;
import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * DAO of the media types
 */
@ApplicationScoped
public class LibraryMediaDAO implements ILibraryMediaDAO
{
    private static final String SQL_QUERY_NEW_PK = " SELECT max( id_media ) FROM library_media ";
    private static final String SQL_QUERY_REMOVE_MEDIA = "DELETE FROM library_media WHERE id_media= ?";
    private static final String SQL_QUERY_INSERT_MEDIA = "INSERT INTO library_media (id_media ,name ,description, is_multiple_media, template) VALUES ( ? , ? , ? , ? , ? )";
    private static final String SQL_QUERY_SELECT_MEDIA = "SELECT id_media, name, description, is_multiple_media, template FROM library_media WHERE id_media= ?";
    private static final String SQL_QUERY_SELECT_ALL = "SELECT id_media, name, description, is_multiple_media, template FROM library_media";
    private static final String SQL_QUERY_UPDATE_MEDIA = "UPDATE library_media SET name=?, description=?, is_multiple_media = ?, template=? WHERE id_media=?";
    private static final String SQL_QUERY_SELECT_LEGACY_STYLESHEET = "SELECT stylesheet FROM library_media WHERE id_media = ?";
    private static final String SQL_QUERY_SELECT_IDS_WITH_LEGACY_STYLESHEET = "SELECT id_media FROM library_media WHERE stylesheet IS NOT NULL";
    private static final String SQL_QUERY_REMOVE_LEGACY_STYLESHEET = "UPDATE library_media SET stylesheet = NULL WHERE id_media = ?";

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete( int nMediaId, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_REMOVE_MEDIA, plugin ) )
        {
            daoUtil.setInt( 1, nMediaId );
            daoUtil.executeUpdate(  );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert( LibraryMedia media, Plugin plugin )
    {
        media.setMediaId( newPrimaryKey( plugin ) );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_MEDIA, plugin ) )
        {
            daoUtil.setInt( 1, media.getMediaId(  ) );
            daoUtil.setString( 2, media.getName(  ) );
            daoUtil.setString( 3, media.getDescription(  ) );
            daoUtil.setBoolean( 4, media.getIsMultipleMedia(  ) );
            daoUtil.setString( 5, media.getTemplate(  ) );
            daoUtil.executeUpdate(  );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LibraryMedia load( int nMediaId, Plugin plugin )
    {
        LibraryMedia media = null;

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_MEDIA, plugin ) )
        {
            daoUtil.setInt( 1, nMediaId );
            daoUtil.executeQuery(  );

            if ( daoUtil.next(  ) )
            {
                media = dataToMedia( daoUtil );
            }
        }

        return media;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<LibraryMedia> selectAll( Plugin plugin )
    {
        Collection<LibraryMedia> colMedia = new ArrayList<>(  );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL, plugin ) )
        {
            daoUtil.executeQuery(  );

            while ( daoUtil.next(  ) )
            {
                colMedia.add( dataToMedia( daoUtil ) );
            }
        }

        return colMedia;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store( LibraryMedia media, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_MEDIA, plugin ) )
        {
            daoUtil.setString( 1, media.getName(  ) );
            daoUtil.setString( 2, media.getDescription(  ) );
            daoUtil.setBoolean( 3, media.getIsMultipleMedia(  ) );
            daoUtil.setString( 4, media.getTemplate(  ) );
            daoUtil.setInt( 5, media.getMediaId(  ) );
            daoUtil.executeUpdate(  );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String selectLegacyStylesheet( int nMediaId, Plugin plugin )
    {
        String strStylesheet = null;

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_LEGACY_STYLESHEET, plugin ) )
        {
            daoUtil.setInt( 1, nMediaId );
            daoUtil.executeQuery(  );

            if ( daoUtil.next(  ) )
            {
                byte[] stylesheet = daoUtil.getBytes( 1 );

                if ( stylesheet != null )
                {
                    strStylesheet = new String( stylesheet, StandardCharsets.UTF_8 );
                }
            }
        }

        return strStylesheet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> selectIdsWithLegacyStylesheet( Plugin plugin )
    {
        List<Integer> listIds = new ArrayList<>(  );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_IDS_WITH_LEGACY_STYLESHEET, plugin ) )
        {
            daoUtil.executeQuery(  );

            while ( daoUtil.next(  ) )
            {
                listIds.add( daoUtil.getInt( 1 ) );
            }
        }

        return listIds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteLegacyStylesheet( int nMediaId, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_REMOVE_LEGACY_STYLESHEET, plugin ) )
        {
            daoUtil.setInt( 1, nMediaId );
            daoUtil.executeUpdate(  );
        }
    }

    /**
     * Builds a media type from the current row of a query selecting id_media, name, description, is_multiple_media and template
     * @param daoUtil the DAOUtil positioned on the row
     * @return the media type
     */
    private LibraryMedia dataToMedia( DAOUtil daoUtil )
    {
        LibraryMedia media = new LibraryMedia(  );
        media.setMediaId( daoUtil.getInt( 1 ) );
        media.setName( daoUtil.getString( 2 ) );
        media.setDescription( daoUtil.getString( 3 ) );
        media.setIsMultipleMedia( daoUtil.getBoolean( 4 ) );
        media.setTemplate( daoUtil.getString( 5 ) );

        return media;
    }

    /**
     * Generates a new primary key
     * @param plugin The plugin
     * @return The new primary key
     */
    public int newPrimaryKey( Plugin plugin )
    {
        int nKey = 1;

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEW_PK, plugin ) )
        {
            daoUtil.executeQuery(  );

            if ( daoUtil.next(  ) )
            {
                nKey = daoUtil.getInt( 1 ) + 1;
            }
        }

        return nKey;
    }
}
