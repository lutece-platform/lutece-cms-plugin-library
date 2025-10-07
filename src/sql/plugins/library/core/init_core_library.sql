-- liquibase formatted sql
-- changeset library:init_core_library.sql
-- preconditions onFail:MARK_RAN onError:WARN
--
-- Dumping data for table core_admin_right
--
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url) VALUES 
('LIBRARY_MANAGEMENT','library.adminFeature.library_management.name',0,'jsp/admin/plugins/library/ManageLibrary.jsp','library.adminFeature.library_management.description',0,'library','CONTENT','images/admin/skin/plugins/library/library.png', NULL);

--
-- Dumping data for table core_user_right
--
INSERT INTO core_user_right (id_right,id_user) VALUES ('LIBRARY_MANAGEMENT',1); 
DELETE FROM core_datastore WHERE entity_key='library.insert_service_media_type_image';
INSERT INTO core_datastore VALUES ('library.insert_service_media_type_image', '1');
DELETE FROM core_datastore WHERE entity_key='library.insert_service_media_type_image_space';
INSERT INTO core_datastore VALUES ('library.insert_service_media_type_image_space', '6');
DELETE FROM core_datastore WHERE entity_key='library.insert_service_media_type_pdf';
INSERT INTO core_datastore VALUES ('library.insert_service_media_type_pdf', '3');
DELETE FROM core_datastore WHERE entity_key='library.insert_service_media_type_pdf_space';
INSERT INTO core_datastore VALUES ('library.insert_service_media_type_pdf_space', '8');
