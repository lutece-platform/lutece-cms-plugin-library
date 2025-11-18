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

import java.util.ArrayList;
import java.util.Collection;

@ApplicationScoped
public class LibraryMediaDAO implements ILibraryMediaDAO
{
    private static final String SQL_QUERY_NEW_PK = " SELECT max( id_media ) FROM library_media ";
    private static final String SQL_QUERY_REMOVE_MEDIA = "DELETE FROM library_media WHERE id_media= ?";
    private static final String SQL_QUERY_INSERT_MEDIA = "INSERT INTO library_media (id_media ,name ,description, is_multiple_media, stylesheet) VALUES ( ? , ? , ? , ? , ? )";
    private static final String SQL_QUERY_SELECT_MEDIA = "SELECT id_media, name, description, is_multiple_media, stylesheet FROM library_media WHERE id_media= ?";
    private static final String SQL_QUERY_SELECT_ALL = "SELECT id_media, name, description, is_multiple_media FROM library_media";
    private static final String SQL_QUERY_UPDATE_MEDIA = "UPDATE library_media SET name=?, description=?, is_multiple_media = ?, stylesheet=? WHERE id_media=?";

    public void delete( int nMediaId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_REMOVE_MEDIA, plugin );
        daoUtil.setInt( 1, nMediaId );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    public void insert( LibraryMedia media, Plugin plugin )
    {
        media.setMediaId( newPrimaryKey( plugin ) );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_MEDIA, plugin );

        daoUtil.setInt( 1, media.getMediaId(  ) );
        daoUtil.setString( 2, media.getName(  ) );
        daoUtil.setString( 3, media.getDescription(  ) );
        daoUtil.setBoolean( 4, media.getIsMultipleMedia(  ) );
        daoUtil.setBytes( 5, media.getStyleSheet(  ).getSource(  ) );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    public LibraryMedia load( int nMediaId, Plugin plugin )
    {
        LibraryMedia media = null;
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_MEDIA, plugin );
        daoUtil.setInt( 1, nMediaId );
        daoUtil.executeQuery(  );

        if ( daoUtil.next(  ) )
        {
            media = new LibraryMedia(  );
            media.setMediaId( daoUtil.getInt( 1 ) );
            media.setName( daoUtil.getString( 2 ) );
            media.setDescription( daoUtil.getString( 3 ) );
            media.setIsMultipleMedia( daoUtil.getBoolean( 4 ) );
            media.setStyleSheetBytes( daoUtil.getBytes( 5 ) );
        }

        daoUtil.free(  );

        return media;
    }

    public Collection<LibraryMedia> selectAll( Plugin plugin )
    {
        Collection<LibraryMedia> colMedia = new ArrayList<LibraryMedia>(  );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL, plugin );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            LibraryMedia media = new LibraryMedia(  );
            media.setMediaId( daoUtil.getInt( 1 ) );
            media.setName( daoUtil.getString( 2 ) );
            media.setDescription( daoUtil.getString( 3 ) );
            media.setIsMultipleMedia( daoUtil.getBoolean( 4 ) );
            colMedia.add( media );
        }

        daoUtil.free(  );

        return colMedia;
    }

    public void store( LibraryMedia media, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_MEDIA, plugin );
        daoUtil.setString( 1, media.getName(  ) );
        daoUtil.setString( 2, media.getDescription(  ) );
        daoUtil.setBoolean( 3, media.getIsMultipleMedia(  ) );
        daoUtil.setBytes( 4, media.getStyleSheet(  ).getSource(  ) );
        daoUtil.setInt( 5, media.getMediaId(  ) );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * Generates a new primary key
     * @param plugin The plugin
     * @return The new primary key
     */
    public int newPrimaryKey( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEW_PK, plugin );
        daoUtil.executeQuery(  );

        int nKey;

        if ( !daoUtil.next(  ) )
        {
            // if the table is empty
            nKey = 1;
        }

        nKey = daoUtil.getInt( 1 ) + 1;
        daoUtil.free(  );

        return nKey;
    }
}
