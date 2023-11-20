CREATE OR REPLACE FUNCTION notify_links_event() RETURNS TRIGGER AS
$$
BEGIN
    IF (TG_OP = 'DELETE') then
        perform pg_notify('link_event_notification', (row_to_json(old)::jsonb || '{"type":"DELETE"}'::jsonb)::text);
    ELSIF (TG_OP = 'UPDATE') THEN
        perform pg_notify('link_event_notification', (row_to_json(new)::jsonb || '{"type":"UPDATE"}'::jsonb)::text);
    ELSIF (TG_OP = 'INSERT') THEN
        perform pg_notify('link_event_notification', (row_to_json(new)::jsonb || '{"type":"INSERT"}'::jsonb)::text);
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;;

DROP TRIGGER IF EXISTS notify_link_event ON links;

CREATE TRIGGER notify_link_event
    AFTER INSERT OR UPDATE OR DELETE
    ON links
    FOR EACH ROW
EXECUTE PROCEDURE notify_links_event();