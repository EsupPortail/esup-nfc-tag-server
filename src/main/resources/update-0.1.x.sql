CREATE FUNCTION textsearchable_taglog_trigger() RETURNS trigger AS $$ begin new.textsearchable_index_col = setweight(to_tsvector('simple', coalesce(new.csn,'')), 'A')||setweight(to_tsvector('simple', coalesce(new.numero_id,'')), 'B')||setweight(to_tsvector('simple', coalesce(new.eppn,'')), 'A')||setweight(to_tsvector('simple', coalesce(new.eppn_init,'')), 'B')||setweight(to_tsvector('simple', coalesce(new.application_name,'')), 'A')||setweight(to_tsvector('simple', coalesce(new.location,'')), 'A')||setweight(to_tsvector('simple', coalesce(new.application_name,'')), 'A')||setweight(to_tsvector('simple', coalesce(new.status,'')), 'A')||setweight(to_tsvector('simple', coalesce(new.firstname,'')), 'A')||setweight(to_tsvector('simple', coalesce(new.lastname,'')), 'A'); return new; end $$ LANGUAGE plpgsql;
CREATE TRIGGER tsvectorupdate BEFORE INSERT OR UPDATE ON tag_log FOR EACH ROW EXECUTE PROCEDURE textsearchable_taglog_trigger();
CREATE FUNCTION textsearchable_device_trigger() RETURNS trigger AS $$ begin new.textsearchable_index_col = setweight(to_tsvector('simple', coalesce(new.eppn_init,'')), 'A')||setweight(to_tsvector('simple', coalesce(new.numero_id,'')), 'A')||setweight(to_tsvector('simple', coalesce(new.imei,'')), 'A')||setweight(to_tsvector('simple', coalesce(new.mac_address,'')), 'A')||setweight(to_tsvector('simple', coalesce(new.location,'')), 'A')||setweight(to_tsvector('simple', coalesce(application.name,'')), 'A') FROM application where new.application=application.id; return new; end $$ LANGUAGE plpgsql;
CREATE TRIGGER tsvectorupdate BEFORE INSERT OR UPDATE ON device FOR EACH ROW EXECUTE PROCEDURE textsearchable_device_trigger();
