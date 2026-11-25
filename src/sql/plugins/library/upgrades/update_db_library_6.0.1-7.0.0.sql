-- liquibase formatted sql
-- changeset library:update_db_library_6.0.1-7.0.0.sql
-- preconditions onFail:MARK_RAN onError:WARN
--
-- FreeMarker template of the media types (replaces the XSL stylesheet).
-- A NULL template means that the default template of the plugin is used.
-- The stylesheet column is kept so that the specific stylesheets can be rewritten as templates.
-- It is not dropped by this script: a stylesheet that is not rewritten yet must stay readable, and this script may be
-- run by hand, without the Liquibase preconditions. The media types that were shipped and are no longer (banners,
-- video, sound, Flash) are not removed either: unmodified, they get the template of their stylesheet below; modified,
-- they are flagged for rewriting. An administrator removes the ones the site does not use in the back office.
--
ALTER TABLE library_media ADD COLUMN template long varchar;

-- changeset library:update_db_library_6.0.1-7.0.0.sql-rev2.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- comment The media types whose XSL stylesheet was never modified (same checksum as a shipped stylesheet) get the shipped FreeMarker template, and their stylesheet is discarded
UPDATE library_media SET template = '<#ftl output_format="HTML">
<img src="${media.src!}" alt="${media.alt!}" height="${media.height!}" width="${media.width!}"/>', stylesheet = NULL
	WHERE template IS NULL AND MD5(stylesheet) IN ('e01c38d9211fa78b5297cad1d9070aa8', '104cf6c995a9ae3d0222af8079d1db3d');
UPDATE library_media SET template = '<#ftl output_format="HTML">
<img src="${media.src!}" alt="${media.alt!}" title="${media.title!}" height="${media.height!}" width="${media.width!}"/>', stylesheet = NULL
	WHERE template IS NULL AND MD5(stylesheet) IN ('792d7f60bd6732621adf945c2d122bc6', '8e9a4bf7a4b6dbbb3ae881070f403065');
UPDATE library_media SET template = '<#ftl output_format="HTML">
<a href="${media.src!}">${media.title!}</a>', stylesheet = NULL
	WHERE template IS NULL AND MD5(stylesheet) IN ('775189de9ebe49d1b15feb152c7bb5b0', '9074f0956994e74cc7a1b77860a29b59');
UPDATE library_media SET template = '<#ftl output_format="HTML">
<object data="${media.src!}" type="application/x-shockwave-flash" width="${media.width!}" height="${media.height!}" align="${media.align!}" classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0">
	<param name="Autostart" value="${media.autostart!}" />
	<param name="Quality" value="${media.quality!}" />
	<param name="Loop" value="${media.loop!}" />
	<param name="menu" value="${media.menu!}" />
	<param name="allowScriptAccess" value="samedomain" />
	<param name="movie" value="${media.src!}" />
	<embed src="${media.src!}" type="application/x-shockwave-flash" width="${media.width!}" height="${media.height!}" align="${media.align!}" quality="${media.quality!}" loop="${media.loop!}" menu="${media.menu!}" allowScriptAccess="samedomain" pluginspace="http://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash"></embed>
</object>', stylesheet = NULL
	WHERE template IS NULL AND MD5(stylesheet) IN ('629c5b0cc4687784fc77c47ed552a312', 'e56816056d29075e9e60b893e35c912f');
UPDATE library_media SET template = '<#ftl output_format="HTML">
<div id="myCarousel" class="carousel slide" data-ride="carousel">
	<div class="carousel-inner">
<#list media_list as item>
		<div class="<#if item?is_first>active </#if>item">
			<img src="${item.src!}" alt="${item.alt!}" height="${item.height!}" width="${item.width!}"/>
			<div class="carousel-caption">
				<p>${item.alt!}</p>
			</div>
		</div>
</#list>
	</div>
	<a class="left carousel-control" href="#myCarousel" data-slide="prev">
		<span class="glyphicon glyphicon-chevron-left"></span>
	</a>
	<a class="right carousel-control" href="#myCarousel" data-slide="next">
		<span class="glyphicon glyphicon-chevron-right"></span>
	</a>
</div>', stylesheet = NULL
	WHERE template IS NULL AND MD5(stylesheet) IN ('eb6b445e055fa68e8278f00c8cc11cab', 'ecedcfd8e0f7326194df0553e6bada10');

-- changeset library:update_db_library_6.0.1-7.0.0.sql-rev3.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- comment The media types with specific XSL stylesheets are flagged in the back office until the stylesheets are rewritten as templates
-- precondition-sql-check expectedResult:1 SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM library_media WHERE stylesheet IS NOT NULL
DELETE FROM core_datastore WHERE entity_key = 'library.legacyStylesheets.pending';
INSERT INTO core_datastore (entity_key, entity_value) VALUES ('library.legacyStylesheets.pending', 'true');

-- changeset library:update_db_library_6.0.1-7.0.0.sql-rev5.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- comment The icon of the admin feature is the SVG shipped by the plugin: the PNG it pointed to is no longer shipped
UPDATE core_admin_right SET icon_url = 'themes/shared/plugins/library/images/library.svg' WHERE id_right = 'LIBRARY_MANAGEMENT';

-- changeset library:update_db_library_6.0.1-7.0.0.sql-rev6.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- comment The shipped carousel template was Bootstrap 3 markup, which the Bootstrap 5 themes of Lutece 8 render as a plain list of images: an unmodified one gets the Bootstrap 5 carousel
UPDATE library_media SET template = '<#ftl output_format="HTML">
<#assign carouselId = "libraryCarousel-" + .now?long?c>
<div id="${carouselId}" class="carousel slide" aria-roledescription="carousel">
<#if media_list?size gt 1>
	<div class="carousel-indicators">
<#list media_list as item>
		<button type="button" data-bs-target="#${carouselId}" data-bs-slide-to="${item?index}"<#if item?is_first> class="active" aria-current="true"</#if> aria-label="${item?counter} / ${media_list?size}"></button>
</#list>
	</div>
</#if>
	<div class="carousel-inner">
<#list media_list as item>
		<div class="carousel-item<#if item?is_first> active</#if>" role="group" aria-roledescription="slide" aria-label="${item?counter} / ${media_list?size}">
			<img src="${item.src!}" alt="${item.alt!}" height="${item.height!}" width="${item.width!}" class="d-block w-100 h-auto"/>
<#if item.alt?has_content>
			<div class="carousel-caption">
				<p>${item.alt}</p>
			</div>
</#if>
		</div>
</#list>
	</div>
<#if media_list?size gt 1>
	<button class="carousel-control-prev" type="button" data-bs-target="#${carouselId}" data-bs-slide="prev">
		<span class="carousel-control-prev-icon" aria-hidden="true"></span>
		<span class="visually-hidden">${"#"}i18n{portal.util.labelPrevious}</span>
	</button>
	<button class="carousel-control-next" type="button" data-bs-target="#${carouselId}" data-bs-slide="next">
		<span class="carousel-control-next-icon" aria-hidden="true"></span>
		<span class="visually-hidden">${"#"}i18n{portal.util.labelNext}</span>
	</button>
</#if>
</div>'
	WHERE MD5(template) IN ('82a90b3bf56163c361fce2902bf0bb6c', '7cf422d12a52031ba337f659d7d150eb');

-- changeset library:update_db_library_6.0.1-7.0.0.sql-rev7.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM library_media WHERE name = 'Galerie LightBox'
-- comment New media type: a gallery of images that open in a lightbox (fslightbox, shipped by the plugin), mapped to the file of the image documents
INSERT INTO library_media (id_media,name,description,template,is_multiple_media)
	SELECT m.max_id + 1, 'Galerie LightBox', 'Insertion d''une galerie d''images en lightbox', '<#ftl output_format="HTML">
<#assign galleryId = "libraryGallery-" + .now?long?c>
<ul class="list-unstyled row g-2 library-lightbox-gallery">
<#list media_list as item>
	<li class="col-6 col-md-4 col-lg-3">
		<a href="${item.src!}" class="d-block" data-fslightbox="${galleryId}" data-type="image"<#if item.alt?has_content> data-alt="${item.alt}"</#if>>
			<img src="${item.src!}" alt="${item.alt!}"<#if item.width?has_content> width="${item.width}"</#if><#if item.height?has_content> height="${item.height}"</#if> class="img-fluid" loading="lazy"/>
			<span class="visually-hidden">${"#"}i18n{library.media.lightbox.labelOpen} (${item?counter} / ${media_list?size})</span>
		</a>
	</li>
</#list>
</ul>', 1
	FROM ( SELECT COALESCE(MAX(id_media), 0) AS max_id FROM library_media ) m;
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value)
	SELECT a.max_id + 1, m.id_media, 'src', 'Source', 2, ''
	FROM library_media m, ( SELECT COALESCE(MAX(id_attribute), 0) AS max_id FROM library_media_attribute ) a WHERE m.name = 'Galerie LightBox';
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value)
	SELECT a.max_id + 1, m.id_media, 'alt', 'Titre de l''image', 3, ''
	FROM library_media m, ( SELECT COALESCE(MAX(id_attribute), 0) AS max_id FROM library_media_attribute ) a WHERE m.name = 'Galerie LightBox';
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value)
	SELECT a.max_id + 1, m.id_media, 'width', 'Largeur de la vignette', 0, '300'
	FROM library_media m, ( SELECT COALESCE(MAX(id_attribute), 0) AS max_id FROM library_media_attribute ) a WHERE m.name = 'Galerie LightBox';
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value)
	SELECT a.max_id + 1, m.id_media, 'height', 'Hauteur de la vignette', 0, ''
	FROM library_media m, ( SELECT COALESCE(MAX(id_attribute), 0) AS max_id FROM library_media_attribute ) a WHERE m.name = 'Galerie LightBox';
INSERT INTO library_mapping (id_mapping,id_media,code_document_type)
	SELECT p.max_id + 1, m.id_media, 'image'
	FROM library_media m, ( SELECT COALESCE(MAX(id_mapping), 0) AS max_id FROM library_mapping ) p
	WHERE m.name = 'Galerie LightBox' AND EXISTS ( SELECT 1 FROM document_type_attr WHERE code_document_type = 'image' AND code = 'file' );
INSERT INTO library_mapping_attribute (id_mapping,id_media_attribute,id_document_attribute)
	SELECT p.id_mapping, a.id_attribute, d.id_document_attr
	FROM library_media m, library_mapping p, library_media_attribute a, document_type_attr d
	WHERE m.name = 'Galerie LightBox' AND p.id_media = m.id_media AND a.id_media = m.id_media AND a.code = 'src'
	AND d.code_document_type = 'image' AND d.code = 'file';

-- changeset library:update_db_library_6.0.1-7.0.0.sql-rev8.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM library_media WHERE name = 'Image lightbox'
-- comment New media type: one image that opens in a lightbox (fslightbox, shipped by the plugin), mapped to the file of the image documents
INSERT INTO library_media (id_media,name,description,template,is_multiple_media)
	SELECT m.max_id + 1, 'Image lightbox', 'Insertion d''une image qui s''agrandit en lightbox', '<#ftl output_format="HTML">
<a href="${media.src!}" class="d-inline-block library-lightbox" data-fslightbox="libraryLightbox-${.now?long?c}" data-type="image"<#if media.alt?has_content> data-alt="${media.alt}"</#if>>
	<img src="${media.src!}" alt="${media.alt!}"<#if media.width?has_content> width="${media.width}"</#if><#if media.height?has_content> height="${media.height}"</#if> class="img-fluid"/>
	<span class="visually-hidden">${"#"}i18n{library.media.lightbox.labelOpen}</span>
</a>', 0
	FROM ( SELECT COALESCE(MAX(id_media), 0) AS max_id FROM library_media ) m;
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value)
	SELECT a.max_id + 1, m.id_media, 'src', 'Source', 2, ''
	FROM library_media m, ( SELECT COALESCE(MAX(id_attribute), 0) AS max_id FROM library_media_attribute ) a WHERE m.name = 'Image lightbox';
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value)
	SELECT a.max_id + 1, m.id_media, 'alt', 'Titre de l''image', 3, ''
	FROM library_media m, ( SELECT COALESCE(MAX(id_attribute), 0) AS max_id FROM library_media_attribute ) a WHERE m.name = 'Image lightbox';
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value)
	SELECT a.max_id + 1, m.id_media, 'width', 'Largeur de l''image', 0, '600'
	FROM library_media m, ( SELECT COALESCE(MAX(id_attribute), 0) AS max_id FROM library_media_attribute ) a WHERE m.name = 'Image lightbox';
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value)
	SELECT a.max_id + 1, m.id_media, 'height', 'Hauteur de l''image', 0, ''
	FROM library_media m, ( SELECT COALESCE(MAX(id_attribute), 0) AS max_id FROM library_media_attribute ) a WHERE m.name = 'Image lightbox';
INSERT INTO library_mapping (id_mapping,id_media,code_document_type)
	SELECT p.max_id + 1, m.id_media, 'image'
	FROM library_media m, ( SELECT COALESCE(MAX(id_mapping), 0) AS max_id FROM library_mapping ) p
	WHERE m.name = 'Image lightbox' AND EXISTS ( SELECT 1 FROM document_type_attr WHERE code_document_type = 'image' AND code = 'file' );
INSERT INTO library_mapping_attribute (id_mapping,id_media_attribute,id_document_attribute)
	SELECT p.id_mapping, a.id_attribute, d.id_document_attr
	FROM library_media m, library_mapping p, library_media_attribute a, document_type_attr d
	WHERE m.name = 'Image lightbox' AND p.id_media = m.id_media AND a.id_media = m.id_media AND a.code = 'src'
	AND d.code_document_type = 'image' AND d.code = 'file';

-- changeset library:update_db_library_6.0.1-7.0.0.sql-rev9.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- comment The icon of the admin feature is the Tabler books icon of the descriptor and of the install script (LUT-34354)
UPDATE core_admin_right SET icon_url = 'ti ti-books' WHERE id_right = 'LIBRARY_MANAGEMENT';
