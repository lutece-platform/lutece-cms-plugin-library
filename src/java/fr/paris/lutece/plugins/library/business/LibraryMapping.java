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

import java.util.ArrayList;
import java.util.Collection;


public class LibraryMapping
{
    private int _nIdMapping;
    private int _nIdMedia;
    private String _strCodeDocumentType;
    private Collection<AttributeAssociation> _colAttributeAssociation;

    public int getIdMapping(  )
    {
        return _nIdMapping;
    }

    public void setIdMapping( int nIdMapping )
    {
        _nIdMapping = nIdMapping;
    }

    public int getIdMedia(  )
    {
        return _nIdMedia;
    }

    public void setIdMedia( int nIdMedia )
    {
        _nIdMedia = nIdMedia;
    }

    public String getCodeDocumentType(  )
    {
        return _strCodeDocumentType;
    }

    public void setCodeDocumentType( String strCodeDocumentType )
    {
        _strCodeDocumentType = strCodeDocumentType;
    }

    public Collection<AttributeAssociation> getAttributeAssociationList(  )
    {
        return _colAttributeAssociation;
    }

    public void setAttributeAssociationList( Collection<AttributeAssociation> colAttributeAssociation )
    {
        _colAttributeAssociation = colAttributeAssociation;
    }

    public void addAttributeAssociation( int nMediaAttributeId, int nDocumentAttributeId )
    {
        AttributeAssociation association = new AttributeAssociation(  );
        association.setIdMediaAttribute( nMediaAttributeId );
        association.setIdDocumentAttribute( nDocumentAttributeId );

        if ( _colAttributeAssociation == null )
        {
            _colAttributeAssociation = new ArrayList<AttributeAssociation>(  );
        }

        _colAttributeAssociation.add( association );
    }

    public class AttributeAssociation
    {
        private int _nIdMediaAttribute;
        private int _nIdDocumentAttribute;
        private String _strAssociationLabel;

        public int getIdDocumentAttribute(  )
        {
            return _nIdDocumentAttribute;
        }

        public void setIdDocumentAttribute( int nIdDocumentAttribute )
        {
            _nIdDocumentAttribute = nIdDocumentAttribute;
        }

        public int getIdMediaAttribute(  )
        {
            return _nIdMediaAttribute;
        }

        public void setIdMediaAttribute( int nIdMediaAttribute )
        {
            _nIdMediaAttribute = nIdMediaAttribute;
        }

        public void setAssociationLabel( String strAssociationLabel )
        {
            _strAssociationLabel = strAssociationLabel;
        }

        public String getAssociationLabel(  )
        {
            return _strAssociationLabel;
        }
    }
}
