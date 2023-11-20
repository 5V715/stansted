alter table links
    drop column hits;

create table hits
(
    id         uuid  not null primary key default gen_random_uuid(),
    created_at timestamp with time zone   default now(),
    data       jsonb not null,
    link_id    uuid  not null references links (id)
)