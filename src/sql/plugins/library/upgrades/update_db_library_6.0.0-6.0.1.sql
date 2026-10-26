-- liquibase formatted sql
-- changeset library:update_db_library_6.0.0-6.0.1.sql
-- preconditions onFail:MARK_RAN onError:WARN
UPDATE core_admin_right SET icon_url = 'ti ti-books' WHERE id_right = 'LIBRARY_MANAGEMENT' AND icon_url = 'images/admin/skin/plugins/library/library.png';
