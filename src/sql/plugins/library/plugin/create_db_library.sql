-- liquibase formatted sql
-- changeset library:create_db_library.sql
-- preconditions onFail:MARK_RAN onError:WARN
--
-- Structure for table library_mapping
--
DROP TABLE IF EXISTS library_mapping;
CREATE TABLE library_mapping (
  id_mapping int DEFAULT '0' NOT NULL,
  id_media int DEFAULT '0' NOT NULL,
  code_document_type varchar(30) DEFAULT '' NOT NULL,
  PRIMARY KEY  (id_mapping)
);



--
-- Structure for table library_mapping_attribute
--
DROP TABLE IF EXISTS library_mapping_attribute;
CREATE TABLE library_mapping_attribute (
  id_mapping int DEFAULT '0' NOT NULL,
  id_media_attribute int DEFAULT '0' NOT NULL,
  id_document_attribute int DEFAULT '0' NOT NULL,
  PRIMARY KEY  (id_mapping,id_media_attribute)
);


--
-- Structure for table library_media
--
DROP TABLE IF EXISTS library_media;
CREATE TABLE library_media (
  id_media int DEFAULT '0' NOT NULL,
  name varchar(45) DEFAULT '' NOT NULL,
  description varchar(100) DEFAULT '' NOT NULL,
  stylesheet long varbinary,
  is_multiple_media SMALLINT DEFAULT 0,
  PRIMARY KEY  (id_media)
);


--
-- Structure for table library_media_attribute
--
DROP TABLE IF EXISTS library_media_attribute;
CREATE TABLE library_media_attribute (
  id_attribute int DEFAULT '0' NOT NULL,
  id_media int DEFAULT '0' NOT NULL,
  code varchar(45) DEFAULT '' NOT NULL,
  description varchar(100) DEFAULT '' NOT NULL,
  type smallint DEFAULT '0' NOT NULL,
  DEFAULT_value varchar(100) DEFAULT '' NOT NULL,
  PRIMARY KEY  (id_attribute)
);