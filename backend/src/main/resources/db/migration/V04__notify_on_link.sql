CREATE OR REPLACE FUNCTION notify_links_event() RETURNS TRIGGER AS
$$
DECLARE
    payload JSON;
BEGIN
    payload = row_to_json(NEW);
    PERFORM pg_notify('link_event_notification', payload::text);
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;;

DROP TRIGGER IF EXISTS notify_link_event ON links;

CREATE TRIGGER notify_link_event
    AFTER INSERT OR UPDATE OR DELETE
    ON links
    FOR EACH ROW
EXECUTE PROCEDURE notify_links_event();