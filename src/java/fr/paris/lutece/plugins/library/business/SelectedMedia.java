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

import fr.paris.lutece.plugins.document.business.Document;

import java.util.Map;


public class SelectedMedia implements Comparable<SelectedMedia>
{
    private int _nMediaTypeId;
    private int _nDocumentId;
    private int _nMappingId;
    private int _nOrder;
    private Document _document;
    private Map<String, String> _attributesFromMapping;

    /**
     * Default constructor
     */
    public SelectedMedia(  )
    {
        // Default constructor
    }

    /**
     * Constructor with every parameters initialized
     * @param nMediaTypeId The id of the media type
     * @param nDocumentId The document id
     * @param nMappingId The mapping id
     */
    public SelectedMedia( int nMediaTypeId, int nDocumentId, int nMappingId )
    {
        this._nMediaTypeId = nMediaTypeId;
        this._nDocumentId = nDocumentId;
        this._nMappingId = nMappingId;
    }

    /**
     * Get the id of the media type
     * @return The id of the media type
     */
    public int getMediaTypeId(  )
    {
        return _nMediaTypeId;
    }

    /**
     * Set the id of the media type
     * @param nMediaTypeId The id of the media type
     */
    public void setMediaTypeId( int nMediaTypeId )
    {
        this._nMediaTypeId = nMediaTypeId;
    }

    /**
     * Get the document id
     * @return The document id
     */
    public int getDocumentId(  )
    {
        return _nDocumentId;
    }

    /**
     * Set the document id
     * @param nDocumentId The document id
     */
    public void setDocumentId( int nDocumentId )
    {
        this._nDocumentId = nDocumentId;
    }

    /**
     * Get the mapping id
     * @return The mapping id
     */
    public int getMappingId(  )
    {
        return _nMappingId;
    }

    /**
     * Set the mapping id
     * @param nMappingId The mapping id
     */
    public void setMappingId( int nMappingId )
    {
        this._nMappingId = nMappingId;
    }

    /**
     * Get the order of the item
     * @return The order of the item
     */
    public int getOrder(  )
    {
        return _nOrder;
    }

    /**
     * Set the order of the item
     * @param nOrder The order of the item
     */
    public void setOrder( int nOrder )
    {
        this._nOrder = nOrder;
    }

    /**
     * Get the document associated with this media
     * @return The document
     */
    public Document getDocument(  )
    {
        return _document;
    }

    /**
     * Set the document
     * @param document The document
     */
    public void setDocument( Document document )
    {
        this._document = document;
    }

    public Map<String, String> getAttributesFromMapping(  )
    {
        return _attributesFromMapping;
    }

    public void setAttributesFromMapping( Map<String, String> attributesFromMapping )
    {
        this._attributesFromMapping = attributesFromMapping;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo( SelectedMedia o )
    {
        if ( o == null )
        {
            return -1;
        }

        return getOrder(  ) - o.getOrder(  );
    }
}
