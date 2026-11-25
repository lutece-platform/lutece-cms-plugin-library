-- liquibase formatted sql
-- changeset library:init_db_library.sql
-- preconditions onFail:MARK_RAN onError:WARN
--
-- Dumping data for table library_media
--
INSERT INTO library_media (id_media,name,description,template,is_multiple_media) VALUES  (1,'image','Insertion de documents Image','<#ftl output_format="HTML">
<img src="${media.src!}" alt="${media.alt!}" height="${media.height!}" width="${media.width!}"/>',0);
INSERT INTO library_media (id_media,name,description,template,is_multiple_media) VALUES  (3,'PDF','Insertion de lien vers un document PDF','<#ftl output_format="HTML">
<a href="${media.src!}">${media.title!}</a>',0);
INSERT INTO library_media (id_media,name,description,template,is_multiple_media) VALUES  (7,'carousel','Insertion d\'un carousel d\'images','<#ftl output_format="HTML">
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
</div>',1);
INSERT INTO library_media (id_media,name,description,template,is_multiple_media) VALUES  (8,'Galerie LightBox','Insertion d''une galerie d''images en lightbox','<#ftl output_format="HTML">
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
</ul>',1);
INSERT INTO library_media (id_media,name,description,template,is_multiple_media) VALUES  (9,'Image lightbox','Insertion d''une image qui s''agrandit en lightbox','<#ftl output_format="HTML">
<a href="${media.src!}" class="d-inline-block library-lightbox" data-fslightbox="libraryLightbox-${.now?long?c}" data-type="image"<#if media.alt?has_content> data-alt="${media.alt}"</#if>>
	<img src="${media.src!}" alt="${media.alt!}"<#if media.width?has_content> width="${media.width}"</#if><#if media.height?has_content> height="${media.height}"</#if> class="img-fluid"/>
	<span class="visually-hidden">${"#"}i18n{library.media.lightbox.labelOpen}</span>
</a>',0);

--
-- Dumping data for table library_mapping
--
INSERT INTO library_mapping (id_mapping,id_media,code_document_type) VALUES (1,1,'image');
INSERT INTO library_mapping (id_mapping,id_media,code_document_type) VALUES (3,3,'pdf');
INSERT INTO library_mapping (id_mapping,id_media,code_document_type) VALUES (7,7,'image');
INSERT INTO library_mapping (id_mapping,id_media,code_document_type) VALUES (8,8,'image');
INSERT INTO library_mapping (id_mapping,id_media,code_document_type) VALUES (9,9,'image');

--
-- Dumping data for table library_mapping_attribute
--
INSERT INTO library_mapping_attribute (id_mapping,id_media_attribute,id_document_attribute) VALUES (1,2,44);
INSERT INTO library_mapping_attribute (id_mapping,id_media_attribute,id_document_attribute) VALUES (1,1,43);
INSERT INTO library_mapping_attribute (id_mapping,id_media_attribute,id_document_attribute) VALUES (3,10,48);
INSERT INTO library_mapping_attribute (id_mapping,id_media_attribute,id_document_attribute) VALUES (7,24,43);
INSERT INTO library_mapping_attribute (id_mapping,id_media_attribute,id_document_attribute) VALUES (8,28,43);
INSERT INTO library_mapping_attribute (id_mapping,id_media_attribute,id_document_attribute) VALUES (9,32,43);

--
-- Dumping data for table library_media_attribute
--
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value) VALUES 
 (1,1,'src','Source de l''image binaire',2,'');
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value) VALUES
 (2,1,'alt','Element alt',1,'');
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value) VALUES
 (3,1,'height','Valeur de la hauteur de l''image',0,'50');
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value) VALUES
 (4,1,'width','Valeur de la largeur de l''image',0,'50');
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value) VALUES
 (10,3,'src','Source du document PDF',2,'');
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value) VALUES
 (11,3,'title','Titre du lien',3,'');
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value) VALUES
 (24,7,'src','Source',2,'');
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value) VALUES
 (25,7,'alt','Titre de l\'image',3,'');
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value) VALUES
 (26,7,'width','Largeur de l\'image',0,'750');
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value) VALUES
 (27,7,'height','Hauteur de l\'image',0,'');
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value) VALUES
 (28,8,'src','Source',2,'');
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value) VALUES
 (29,8,'alt','Titre de l''image',3,'');
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value) VALUES
 (30,8,'width','Largeur de la vignette',0,'300');
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value) VALUES
 (31,8,'height','Hauteur de la vignette',0,'');
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value) VALUES
 (32,9,'src','Source',2,'');
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value) VALUES
 (33,9,'alt','Titre de l''image',3,'');
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value) VALUES
 (34,9,'width','Largeur de l''image',0,'600');
INSERT INTO library_media_attribute (id_attribute,id_media,code,description,type,default_value) VALUES
 (35,9,'height','Hauteur de l''image',0,'');
