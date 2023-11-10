create table links(
    id uuid not null primary key default gen_random_uuid(),
    short_url varchar(20) not null,
    full_url varchar(2048) not null,
    hits integer default 0
)