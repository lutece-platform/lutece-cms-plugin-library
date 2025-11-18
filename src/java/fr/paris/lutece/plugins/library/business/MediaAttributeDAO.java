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
public class MediaAttributeDAO implements IMediaAttributeDAO
{
    private static final String SQL_QUERY_NEW_PK = " SELECT max( id_attribute ) FROM library_media_attribute ";
    private static final String SQL_QUERY_REMOVE_ATTRIBUTE = "DELETE FROM library_media_attribute WHERE id_attribute= ?";
    private static final String SQL_QUERY_INSERT_ATTRIBUTE = "INSERT INTO library_media_attribute (id_attribute, id_media ,code ,description, type, default_value) VALUES ( ?, ? , ? , ?, ?, ? )";
    private static final String SQL_QUERY_SELECT_ATTRIBUTE = "SELECT * FROM library_media_attribute WHERE id_attribute= ?";
    private static final String SQL_QUERY_SELECT_ALL_FOR_MEDIA = "SELECT * FROM library_media_attribute WHERE id_media = ?";
    private static final String SQL_QUERY_UPDATE_ATTRIBUTE = "UPDATE library_media_attribute SET id_media=?, code=?, description=?, type=?, default_value=? WHERE id_attribute=?";
    private static final String SQL_QUERY_REMOVE_ALL_ATTRIBUTE_FOR_MEDIA = "DELETE FROM library_media_attribute WHERE id_media = ?";

    public void delete( int nAttributeId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_REMOVE_ATTRIBUTE, plugin );
        daoUtil.setInt( 1, nAttributeId );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    public void insert( MediaAttribute attribute, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_ATTRIBUTE, plugin );

        daoUtil.setInt( 1, newPrimaryKey( plugin ) );
        daoUtil.setInt( 2, attribute.getMediaId(  ) );
        daoUtil.setString( 3, attribute.getCode(  ) );
        daoUtil.setString( 4, attribute.getDescription(  ) );
        daoUtil.setInt( 5, attribute.getTypeId(  ) );
        daoUtil.setString( 6, attribute.getDefaultValue(  ) );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    public MediaAttribute load( int nAttributeId, Plugin plugin )
    {
        MediaAttribute attribute = null;
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ATTRIBUTE, plugin );
        daoUtil.setInt( 1, nAttributeId );
        daoUtil.executeQuery(  );

        if ( daoUtil.next(  ) )
        {
            attribute = new MediaAttribute(  );
            attribute.setAttributeId( daoUtil.getInt( 1 ) );
            attribute.setMediaId( daoUtil.getInt( 2 ) );
            attribute.setCode( daoUtil.getString( 3 ) );
            attribute.setDescription( daoUtil.getString( 4 ) );
            attribute.setTypeId( daoUtil.getInt( 5 ) );
            attribute.setDefaultValue( daoUtil.getString( 6 ) );
        }

        daoUtil.free(  );

        return attribute;
    }

    public Collection<MediaAttribute> selectAllAttributesForMedia( int nIdMedia, Plugin plugin )
    {
        Collection<MediaAttribute> colAttributes = new ArrayList<MediaAttribute>(  );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL_FOR_MEDIA, plugin );
        daoUtil.setInt( 1, nIdMedia );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            MediaAttribute attribute = new MediaAttribute(  );
            attribute.setAttributeId( daoUtil.getInt( 1 ) );
            attribute.setMediaId( daoUtil.getInt( 2 ) );
            attribute.setCode( daoUtil.getString( 3 ) );
            attribute.setDescription( daoUtil.getString( 4 ) );
            attribute.setTypeId( daoUtil.getInt( 5 ) );
            attribute.setDefaultValue( daoUtil.getString( 6 ) );
            colAttributes.add( attribute );
        }

        daoUtil.free(  );

        return colAttributes;
    }

    public void store( MediaAttribute attribute, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_ATTRIBUTE, plugin );
        daoUtil.setInt( 1, attribute.getMediaId(  ) );
        daoUtil.setString( 2, attribute.getCode(  ) );
        daoUtil.setString( 3, attribute.getDescription(  ) );
        daoUtil.setInt( 4, attribute.getTypeId(  ) );
        daoUtil.setString( 5, attribute.getDefaultValue(  ) );

        daoUtil.setInt( 6, attribute.getAttributeId(  ) );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * Generates a new primary key
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

    public void deleteAllForMedia( int nMediaId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_REMOVE_ALL_ATTRIBUTE_FOR_MEDIA, plugin );
        daoUtil.setInt( 1, nMediaId );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }
}
