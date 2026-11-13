/*
 * Copyright (c) 2002-2026, City of Paris
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
package fr.paris.lutece.plugins.library.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.document.business.Document;
import fr.paris.lutece.plugins.document.business.attributes.DocumentAttribute;
import fr.paris.lutece.plugins.document.modules.metadatadublincore.business.DublinCoreMetadata;
import fr.paris.lutece.plugins.library.business.LibraryMapping;
import fr.paris.lutece.plugins.library.business.LibraryMedia;
import fr.paris.lutece.plugins.library.business.MediaAttribute;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.template.HtmlMarkup;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.html.Paginator;

/**
 * Renders, with the HTML auto-escaping of the core (LUT-33153), the library templates that write HTML built in Java: the
 * document creation form, the spaces tree and browser of the document plugin and the labels of the mapping associations
 * are marked with HtmlMarkup where they are put in the model and keep their tags, while the text typed by a user is
 * escaped once. The templates whose links or buttons called JavaScript render too.
 */
public class LibraryTemplatesRenderTest extends LuteceTestCase
{
    private static final String FRAGMENT = "<div class=\"fragment\"><input name=\"x\" value=\"a &lt;b&gt;\"></div>";
    private static final String FRAGMENT_ESCAPED = "&lt;div class=&quot;fragment&quot;&gt;";
    private static final String TEXT = "Dupont <b>\"&'";
    private static final String TEXT_ESCAPED = "Dupont &lt;b&gt;&quot;&amp;&#39;";
    private static final String TEXT_UNESCAPED = "Dupont <b>";
    private static final int ID_DOCUMENT = 43;
    private static final int ID_MEDIA = 2;

    /**
     * A document whose thumbnail is known without reading its type in the database
     */
    public static class TestDocument extends Document
    {
        @Override
        public String getThumbnail( )
        {
            return "document?id=" + ID_DOCUMENT + "&id_attribute=1";
        }
    }

    /**
     * Renders a template with the core configuration.
     *
     * @param strTemplate
     *            the template path
     * @param model
     *            the model
     * @return the HTML
     */
    private static String render( String strTemplate, Map<String, Object> model )
    {
        return AppTemplateService.getTemplate( strTemplate, Locale.FRENCH, model ).getHtml( );
    }

    /**
     * The model shared by the insert service pages
     *
     * @return the model
     */
    private static Map<String, Object> insertModel( )
    {
        Map<String, Object> model = new HashMap<>( );
        model.put( "plugin_name", "library" );
        model.put( "input", "content" );
        return model;
    }

    /**
     * A media type with a text attribute
     *
     * @param nTypeId
     *            the type of the attribute
     * @return the media type
     */
    private static LibraryMedia mediaType( int nTypeId )
    {
        MediaAttribute attribute = new MediaAttribute( );
        attribute.setAttributeId( 5 );
        attribute.setCode( "file" );
        attribute.setDescription( TEXT );
        attribute.setTypeId( nTypeId );
        attribute.setDefaultValue( TEXT );
        List<MediaAttribute> listAttributes = new ArrayList<>( );
        listAttributes.add( attribute );
        LibraryMedia media = new LibraryMedia( );
        media.setMediaId( ID_MEDIA );
        media.setName( TEXT );
        media.setDescription( TEXT );
        media.setMediaAttributeList( listAttributes );
        return media;
    }

    /**
     * A document whose title and summary hold markup characters, with a file attribute
     *
     * @return the document
     */
    private static Document document( )
    {
        Document document = new TestDocument( );
        document.setId( ID_DOCUMENT );
        document.setTitle( TEXT );
        document.setSummary( TEXT );
        document.setType( "image" );
        DocumentAttribute attribute = new DocumentAttribute( );
        attribute.setId( 7 );
        attribute.setCode( "document_file" );
        attribute.setTextValue( TEXT );
        List<DocumentAttribute> listAttributes = new ArrayList<>( );
        listAttributes.add( attribute );
        document.setAttributes( listAttributes );
        return document;
    }

    /**
     * The labels of the associations are written as markup, a String would print as text
     */
    @Test
    public void testManageMappingsKeepsTheAssociationLabels( )
    {
        LibraryMapping mapping = new LibraryMapping( );
        mapping.setIdMapping( 3 );
        mapping.setIdMedia( ID_MEDIA );
        mapping.setCodeDocumentType( "image" );
        mapping.addAttributeAssociation( 5, 7 );
        List<LibraryMapping> listMappings = new ArrayList<>( );
        listMappings.add( mapping );
        Map<String, Object> labels = new HashMap<>( );
        labels.put( "3-5", HtmlMarkup.of( FRAGMENT ) );

        Map<String, Object> model = new HashMap<>( );
        model.put( "media", mediaType( MediaAttribute.ATTRIBUTE_TYPE_TEXT_DOCUMENT ) );
        model.put( "mapping_list", listMappings );
        model.put( "association_labels", labels );
        model.put( "document_type_list", new ReferenceList( ) );
        String strHtml = render( "admin/plugins/library/manage_mappings.html", model );
        assertTrue( strHtml, strHtml.contains( FRAGMENT ) );

        labels.put( "3-5", FRAGMENT );
        strHtml = render( "admin/plugins/library/manage_mappings.html", model );
        assertTrue( strHtml, strHtml.contains( FRAGMENT_ESCAPED ) );
    }

    /**
     * The label of an association escapes the codes and names it is built from
     */
    @Test
    public void testAssociationLabelEscapesTheAttributes( )
    {
        DocumentAttribute documentAttribute = new DocumentAttribute( );
        documentAttribute.setCodeDocumentType( "image" );
        documentAttribute.setName( TEXT );
        Map<String, Object> model = new HashMap<>( );
        model.put( "attribute", mediaType( MediaAttribute.ATTRIBUTE_TYPE_TEXT_DOCUMENT ).getMediaAttributeList( ).iterator( ).next( ) );
        model.put( "document_attribute", documentAttribute );
        String strHtml = render( "admin/plugins/library/generate_mapping_association_label.html", model );
        assertTrue( strHtml, strHtml.contains( TEXT_ESCAPED ) );
        assertFalse( strHtml, strHtml.contains( TEXT_UNESCAPED ) );
        assertTrue( strHtml, strHtml.contains( "height=\"10\"" ) );
    }

    /**
     * The fields of the document type are written as markup and the cancel button goes back without inline JavaScript
     */
    @Test
    public void testMediaCreatorKeepsTheCreateForm( )
    {
        Map<String, Object> model = insertModel( );
        model.put( "document_type", "image" );
        model.put( "fields", HtmlMarkup.of( FRAGMENT ) );
        model.put( "metadata", new DublinCoreMetadata( ) );
        model.put( "media", ID_MEDIA );
        model.put( "space_id", 1234 );
        model.put( "is_image", true );
        String strHtml = render( "admin/plugins/library/media_creator.html", model );
        assertTrue( strHtml, strHtml.contains( FRAGMENT ) );
        assertTrue( strHtml, strHtml.contains( "data-lutece-action=\"back\"" ) );
        assertTrue( strHtml, strHtml.contains( "lutece-actions.js" ) );
        assertTrue( strHtml, strHtml.contains( "value=\"1234\"" ) );
        assertTrue( strHtml, strHtml.contains( "aria-controls=\"dublin_core_meta\"" ) );
        assertFalse( strHtml, strHtml.contains( "javascript:" ) );
    }

    /**
     * The spaces tree, output of a stylesheet, is written as markup
     */
    @Test
    public void testUploadSpaceSelectorKeepsTheSpacesTree( )
    {
        Map<String, Object> model = insertModel( );
        model.put( "mediatype", "2" );
        model.put( "spaces_tree", HtmlMarkup.of( FRAGMENT ) );
        String strHtml = render( "admin/plugins/library/upload_space_selector.html", model );
        assertTrue( strHtml, strHtml.contains( FRAGMENT ) );
    }

    /**
     * Each media type is a submit button of the form, the description is escaped once
     */
    @Test
    public void testUploadMediaTypeSelectorSubmitsTheType( )
    {
        LibraryMedia media = mediaType( MediaAttribute.ATTRIBUTE_TYPE_TEXT_USER );
        List<LibraryMedia> listMedia = new ArrayList<>( );
        listMedia.add( media );
        Map<String, Object> model = insertModel( );
        model.put( "mediatypes", listMedia );
        String strHtml = render( "admin/plugins/library/upload_mediatype_selector.html", model );
        assertTrue( strHtml, strHtml.contains( "name=\"media_type\" value=\"" + ID_MEDIA + "\"" ) );
        assertTrue( strHtml, strHtml.contains( TEXT_ESCAPED ) );
        assertFalse( strHtml, strHtml.contains( TEXT_UNESCAPED ) );
        assertFalse( strHtml, strHtml.contains( "javascript:" ) );
    }

    /**
     * The spaces browser is written as markup; a document is selected by a listener reading data attributes
     */
    @Test
    public void testMediaSelectorsKeepTheSpacesBrowser( )
    {
        List<Object> listDocuments = new ArrayList<>( );
        listDocuments.add( new LibraryInsertServiceJspBean( ).new Pair<String, Document>( "3", document( ) ) );
        Map<String, Object> model = insertModel( );
        model.put( "mediatype", mediaType( MediaAttribute.ATTRIBUTE_TYPE_TEXT_USER ) );
        model.put( "all_documents", listDocuments );
        model.put( "spaces_browser", HtmlMarkup.of( FRAGMENT ) );
        model.put( "selected_space", 1 );
        model.put( "paginator", new Paginator<>( new ArrayList<Integer>( ), 10, "jsp/admin/plugins/library/SelectMedia.jsp",
                Paginator.PARAMETER_PAGE_INDEX, "1" ) );
        model.put( "nb_items_per_page", "10" );

        String strHtml = render( "admin/plugins/library/media_selector.html", model );
        assertTrue( strHtml, strHtml.contains( FRAGMENT ) );
        assertTrue( strHtml, strHtml.contains( TEXT_ESCAPED ) );
        assertFalse( strHtml, strHtml.contains( TEXT_UNESCAPED ) );

        strHtml = render( "admin/plugins/library/media_selector_multiple.html", model );
        assertTrue( strHtml, strHtml.contains( FRAGMENT ) );
        assertTrue( strHtml, strHtml.contains( "data-mapping=\"3\"" ) );
        assertTrue( strHtml, strHtml.contains( "data-document=\"" + ID_DOCUMENT + "\"" ) );
        assertTrue( strHtml, strHtml.contains( "name=\"id_document\"" ) );
        assertFalse( strHtml, strHtml.contains( TEXT_UNESCAPED ) );
        assertFalse( strHtml, strHtml.contains( "javascript:" ) );
    }

    /**
     * The edition page writes the URL of a file once escaped, under the name of the attribute, and the text of a document
     * attribute escaped once
     */
    @Test
    public void testMediaEditionSelectorKeepsTheFieldNames( )
    {
        Map<String, String> associations = new HashMap<>( );
        associations.put( "file", "document_file" );
        Map<String, Object> model = insertModel( );
        model.put( "jsp_insert", "jsp/admin/plugins/library/DoInsertMedia.jsp" );
        model.put( "document", document( ) );
        model.put( "attributes_associations", associations );

        LibraryMedia media = mediaType( MediaAttribute.ATTRIBUTE_TYPE_BINARY );
        model.put( "mediatype", media );
        model.put( "media_attributes", media.getMediaAttributeList( ) );
        String strHtml = render( "admin/plugins/library/mediaedition_selector.html", model );
        assertTrue( strHtml, strHtml.contains( "name=\"file\"" ) );
        assertTrue( strHtml, strHtml.contains( "value=\"document?id=" + ID_DOCUMENT + "&amp;id_attribute=7\"" ) );
        assertTrue( strHtml, strHtml.contains( "data-lutece-action=\"back\"" ) );
        assertFalse( strHtml, strHtml.contains( "&amp;amp;" ) );

        media = mediaType( MediaAttribute.ATTRIBUTE_TYPE_TEXT_DOCUMENT );
        model.put( "mediatype", media );
        model.put( "media_attributes", media.getMediaAttributeList( ) );
        strHtml = render( "admin/plugins/library/mediaedition_selector.html", model );
        assertTrue( strHtml, strHtml.contains( TEXT_ESCAPED ) );
        assertFalse( strHtml, strHtml.contains( TEXT_UNESCAPED ) );
    }
}
