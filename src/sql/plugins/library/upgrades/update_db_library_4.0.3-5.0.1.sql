-- liquibase formatted sql
-- changeset library:update_db_library_4.0.3-5.0.1.sql
-- preconditions onFail:MARK_RAN onError:WARN

DELETE FROM core_datastore WHERE entity_key='library.insert_service_media_type_image';
INSERT INTO core_datastore VALUES ('library.insert_service_media_type_image', '1');
DELETE FROM core_datastore WHERE entity_key='library.insert_service_media_type_image_space';
INSERT INTO core_datastore VALUES ('library.insert_service_media_type_image_space', '6');
DELETE FROM core_datastore WHERE entity_key='library.insert_service_media_type_pdf';
INSERT INTO core_datastore VALUES ('library.insert_service_media_type_pdf', '3');
DELETE FROM core_datastore WHERE entity_key='library.insert_service_media_type_pdf_space';
INSERT INTO core_datastore VALUES ('library.insert_service_media_type_pdf_space', '8');
