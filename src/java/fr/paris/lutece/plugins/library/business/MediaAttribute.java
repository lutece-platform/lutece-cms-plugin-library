/*
 * Copyright (c) 2002-2012, Mairie de Paris
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

import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.i18n.Localizable;

import java.util.Locale;


public class MediaAttribute implements Localizable
{
    public static final int ATTRIBUTE_TYPE_TEXT_USER = 0;
    public static final int ATTRIBUTE_TYPE_TEXT_DOCUMENT = 1;
    public static final int ATTRIBUTE_TYPE_BINARY = 2;
    public static final int ATTRIBUTE_TYPE_TITLE_DOCUMENT = 3;
    public static final int ATTRIBUTE_TYPE_SUMMARY_DOCUMENT = 4;
    public static final String PROPERTY_ATTRIBUTE_TYPE_LABEL_TEXT_USER = "library.attribute.type.label.textUser";
    public static final String PROPERTY_ATTRIBUTE_TYPE_LABEL_TEXT_DOCUMENT = "library.attribute.type.label.textDocument";
    public static final String PROPERTY_ATTRIBUTE_TYPE_LABEL_BINARY = "library.attribute.type.label.binary";
    public static final String PROPERTY_ATTRIBUTE_TYPE_LABEL_TITLE_DOCUMENT = "library.attribute.type.label.titleDocument";
    public static final String PROPERTY_ATTRIBUTE_TYPE_LABEL_SUMMARY_DOCUMENT = "library.attribute.type.label.summaryDocument";
    private int _nAttributeId;
    private int _nMediaId;
    private String _strCode;
    private String _strDescription;
    private int _nTypeId;
    private String _strDefaultValue;
    private Locale _locale;

    public int getAttributeId(  )
    {
        return _nAttributeId;
    }

    public void setAttributeId( int nId )
    {
        _nAttributeId = nId;
    }

    public String getCode(  )
    {
        return _strCode;
    }

    public void setCode( String strCode )
    {
        _strCode = strCode;
    }

    public String getDescription(  )
    {
        return _strDescription;
    }

    public void setDescription( String strDescription )
    {
        _strDescription = strDescription;
    }

    public int getMediaId(  )
    {
        return _nMediaId;
    }

    public void setMediaId( int nMediaId )
    {
        _nMediaId = nMediaId;
    }

    public boolean isAssociableWithDocument(  )
    {
        return ( ( _nTypeId != ATTRIBUTE_TYPE_TEXT_USER ) && ( _nTypeId != ATTRIBUTE_TYPE_TITLE_DOCUMENT ) &&
        ( _nTypeId != ATTRIBUTE_TYPE_SUMMARY_DOCUMENT ) );
    }

    public int getTypeId(  )
    {
        return _nTypeId;
    }

    public void setTypeId( int nTypeId )
    {
        _nTypeId = nTypeId;
    }

    public String getTypeLabel(  )
    {
        String strTypeLabel;

        switch ( getTypeId(  ) )
        {
            case ATTRIBUTE_TYPE_BINARY:
                strTypeLabel = I18nService.getLocalizedString( PROPERTY_ATTRIBUTE_TYPE_LABEL_BINARY, _locale );

                break;

            case ATTRIBUTE_TYPE_TEXT_DOCUMENT:
                strTypeLabel = I18nService.getLocalizedString( PROPERTY_ATTRIBUTE_TYPE_LABEL_TEXT_DOCUMENT, _locale );

                break;

            case ATTRIBUTE_TYPE_TITLE_DOCUMENT:
                strTypeLabel = I18nService.getLocalizedString( PROPERTY_ATTRIBUTE_TYPE_LABEL_TITLE_DOCUMENT, _locale );

                break;

            case ATTRIBUTE_TYPE_SUMMARY_DOCUMENT:
                strTypeLabel = I18nService.getLocalizedString( PROPERTY_ATTRIBUTE_TYPE_LABEL_SUMMARY_DOCUMENT, _locale );

                break;

            default:
                strTypeLabel = I18nService.getLocalizedString( PROPERTY_ATTRIBUTE_TYPE_LABEL_TEXT_USER, _locale );

                break;
        }

        return strTypeLabel;
    }

    public void setLocale( Locale locale )
    {
        _locale = locale;
    }

    /**
     * Getter for the default value
     *
     * @return the MediaAttribute default value
     */
    public String getDefaultValue(  )
    {
        return _strDefaultValue;
    }

    /**
     * Setter for the default value
     *
     * @param strDefaultValue the default value to set for the MediaAttribute
     */
    public void setDefaultValue( String strDefaultValue )
    {
        _strDefaultValue = strDefaultValue;
    }
}
