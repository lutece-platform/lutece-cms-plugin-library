/*
 * Copyright (c) 2002-2013, Mairie de Paris
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

import fr.paris.lutece.plugins.library.business.LibraryMapping.AttributeAssociation;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.Collection;


public class LibraryMappingDAO implements ILibraryMappingDAO
{
    private static final String SQL_QUERY_NEW_PK = " SELECT max( id_mapping ) FROM library_mapping ";
    private static final String SQL_QUERY_REMOVE_MAPPING = "DELETE FROM library_mapping WHERE id_mapping = ?";
    private static final String SQL_QUERY_REMOVE_MAPPING_ATTRIBUTES = "DELETE FROM library_mapping_attribute WHERE id_mapping = ?";
    private static final String SQL_QUERY_INSERT_MAPPING = "INSERT INTO library_mapping (id_mapping ,id_media ,code_document_type) VALUES ( ? , ? , ? )";
    private static final String SQL_QUERY_INSERT_MAPPING_ATTRIBUTE = "INSERT INTO library_mapping_attribute (id_mapping ,id_media_attribute ,id_document_attribute) VALUES ( ? , ? , ? )";
    private static final String SQL_QUERY_SELECT_MAPPING = "SELECT a.id_mapping ,a.id_media ,a.code_document_type, b.id_media_attribute ,b.id_document_attribute FROM library_mapping a LEFT JOIN library_mapping_attribute b ON a.id_mapping= b.id_mapping WHERE a.id_mapping = ? ";
    private static final String SQL_QUERY_SELECT_MAPPING_BY_MEDIA = "SELECT * FROM library_mapping WHERE id_media = ? ";
    private static final String SQL_QUERY_SELECT_MAPPING_ATTRIBUTES_BY_MAPPING_ID = "SELECT * FROM library_mapping_attribute WHERE id_mapping = ? ";
    private static final String SQL_QUERY_UPDATE_MAPPING = "UPDATE library_mapping SET id_media=?, code_document_type=? WHERE id_mapping=?";
    private static final String SQL_QUERY_REMOVE_MAPPING_ATTRIBUTE_FROM_ID = "DELETE FROM library_mapping_attribute WHERE id_media_attribute = ?";

    public void delete( int nMappingId, Plugin plugin )
    {
        // delete all attribute associations for this mapping
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_REMOVE_MAPPING_ATTRIBUTES, plugin );
        daoUtil.setInt( 1, nMappingId );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );

        // delete the mapping definition
        daoUtil = new DAOUtil( SQL_QUERY_REMOVE_MAPPING, plugin );
        daoUtil.setInt( 1, nMappingId );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    public void insert( LibraryMapping mapping, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_MAPPING, plugin );
        int nMappingId = newPrimaryKey( plugin );
        // create the new mapping definition
        daoUtil.setInt( 1, nMappingId );
        daoUtil.setInt( 2, mapping.getIdMedia(  ) );
        daoUtil.setString( 3, mapping.getCodeDocumentType(  ) );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );

        if ( mapping.getAttributeAssociationList(  ) == null )
        {
            return;
        }

        // create the associations
        for ( AttributeAssociation association : mapping.getAttributeAssociationList(  ) )
        {
            daoUtil = new DAOUtil( SQL_QUERY_INSERT_MAPPING_ATTRIBUTE, plugin );
            daoUtil.setInt( 1, nMappingId );
            daoUtil.setInt( 2, association.getIdMediaAttribute(  ) );
            daoUtil.setInt( 3, association.getIdDocumentAttribute(  ) );
            daoUtil.executeUpdate(  );
            daoUtil.free(  );
        }
    }

    public LibraryMapping load( int nMappingId, Plugin plugin )
    {
        LibraryMapping mapping = null;
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_MAPPING, plugin );
        daoUtil.setInt( 1, nMappingId );
        daoUtil.executeQuery(  );

        if ( daoUtil.next(  ) )
        {
            mapping = new LibraryMapping(  );
            mapping.setIdMapping( daoUtil.getInt( 1 ) );
            mapping.setIdMedia( daoUtil.getInt( 2 ) );
            mapping.setCodeDocumentType( daoUtil.getString( 3 ) );

            // load first attribute definition
            mapping.addAttributeAssociation( daoUtil.getInt( 4 ), daoUtil.getInt( 5 ) );
        }

        // load other associations
        while ( daoUtil.next(  ) )
        {
            mapping.addAttributeAssociation( daoUtil.getInt( 4 ), daoUtil.getInt( 5 ) );
        }

        daoUtil.free(  );

        return mapping;
    }

    public Collection<LibraryMapping> selectByMedia( int nMediaId, Plugin plugin )
    {
        Collection<LibraryMapping> colMapping = new ArrayList<LibraryMapping>(  );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_MAPPING_BY_MEDIA, plugin );
        daoUtil.setInt( 1, nMediaId );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            LibraryMapping mapping = new LibraryMapping(  );
            mapping.setIdMapping( daoUtil.getInt( 1 ) );
            mapping.setIdMedia( daoUtil.getInt( 2 ) );
            mapping.setCodeDocumentType( daoUtil.getString( 3 ) );

            // load attributes for this mapping	
            DAOUtil daoUtilAssociation = new DAOUtil( SQL_QUERY_SELECT_MAPPING_ATTRIBUTES_BY_MAPPING_ID, plugin );
            daoUtilAssociation.setInt( 1, mapping.getIdMapping(  ) );
            daoUtilAssociation.executeQuery(  );

            while ( daoUtilAssociation.next(  ) )
            {
                mapping.addAttributeAssociation( daoUtilAssociation.getInt( 2 ), daoUtilAssociation.getInt( 3 ) );
            }

            daoUtilAssociation.free(  );

            colMapping.add( mapping );
        }

        daoUtil.free(  );

        return colMapping;
    }

    public void store( LibraryMapping mapping, Plugin plugin )
    {
        // delete all attribute associations for this mapping
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_REMOVE_MAPPING_ATTRIBUTES, plugin );
        daoUtil.setInt( 1, mapping.getIdMapping(  ) );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );

        // update mapping information
        daoUtil = new DAOUtil( SQL_QUERY_UPDATE_MAPPING, plugin );
        daoUtil.setInt( 1, mapping.getIdMedia(  ) );
        daoUtil.setString( 2, mapping.getCodeDocumentType(  ) );
        daoUtil.setInt( 3, mapping.getIdMapping(  ) );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );

        if ( mapping.getAttributeAssociationList(  ) == null )
        {
            return;
        }

        // enter new association definitions
        // create the associations
        for ( AttributeAssociation association : mapping.getAttributeAssociationList(  ) )
        {
            daoUtil = new DAOUtil( SQL_QUERY_INSERT_MAPPING_ATTRIBUTE, plugin );
            daoUtil.setInt( 1, mapping.getIdMapping(  ) );
            daoUtil.setInt( 2, association.getIdMediaAttribute(  ) );
            daoUtil.setInt( 3, association.getIdDocumentAttribute(  ) );
            daoUtil.executeUpdate(  );
            daoUtil.free(  );
        }
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

    public void deleteAttributeAssociation( int nAttributeId, Plugin plugin )
    {
        // delete all attribute associations for this media attribute
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_REMOVE_MAPPING_ATTRIBUTE_FROM_ID, plugin );
        daoUtil.setInt( 1, nAttributeId );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }
}
