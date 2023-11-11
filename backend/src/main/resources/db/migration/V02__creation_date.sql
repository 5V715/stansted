alter table links
    add column created_at timestamp with time zone default now();