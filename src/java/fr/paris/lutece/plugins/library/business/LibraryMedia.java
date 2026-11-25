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

import java.util.Collection;


/**
 * A media type of the library: its attributes, and the FreeMarker template that renders the HTML inserted in the editor
 */
public class LibraryMedia
{
    private int _nMediaId;
    private String _strName;
    private String _strDescription;
    private Collection<MediaAttribute> _colMediaAttribute;
    private String _strTemplate;
    private boolean _bIsMultipleMedia;

    /**
     * Returns the attributes of the media type
     * @return the attributes
     */
    public Collection<MediaAttribute> getMediaAttributeList(  )
    {
        return _colMediaAttribute;
    }

    /**
     * Sets the attributes of the media type
     * @param colMediaAttribute the attributes
     */
    public void setMediaAttributeList( Collection<MediaAttribute> colMediaAttribute )
    {
        _colMediaAttribute = colMediaAttribute;
    }

    /**
     * Returns the identifier of the media type
     * @return the identifier
     */
    public int getMediaId(  )
    {
        return _nMediaId;
    }

    /**
     * Sets the identifier of the media type
     * @param nMediaId the identifier
     */
    public void setMediaId( int nMediaId )
    {
        _nMediaId = nMediaId;
    }

    /**
     * Returns the description of the media type
     * @return the description
     */
    public String getDescription(  )
    {
        return _strDescription;
    }

    /**
     * Sets the description of the media type
     * @param strDescription the description
     */
    public void setDescription( String strDescription )
    {
        _strDescription = strDescription;
    }

    /**
     * Returns the name of the media type
     * @return the name
     */
    public String getName(  )
    {
        return _strName;
    }

    /**
     * Sets the name of the media type
     * @param strName the name
     */
    public void setName( String strName )
    {
        _strName = strName;
    }

    /**
     * Returns the FreeMarker template of the media type, null when the default template of the plugin is used
     * @return the template source
     */
    public String getTemplate(  )
    {
        return _strTemplate;
    }

    /**
     * Sets the FreeMarker template of the media type
     * @param strTemplate the template source, null for the default template of the plugin
     */
    public void setTemplate( String strTemplate )
    {
        _strTemplate = strTemplate;
    }

    /**
     * Tells whether several documents are inserted at once
     * @return true for a multiple media type
     */
    public boolean getIsMultipleMedia(  )
    {
        return _bIsMultipleMedia;
    }

    /**
     * Sets whether several documents are inserted at once
     * @param bIsMultipleMedia true for a multiple media type
     */
    public void setIsMultipleMedia( boolean bIsMultipleMedia )
    {
        this._bIsMultipleMedia = bIsMultipleMedia;
    }
}
