CREATE TABLE bot_settings
(
    id                    BIGINT PRIMARY KEY,
    admin_group_chat_id   BIGINT,
    admin_ids             TEXT,
    google_spreadsheet_id VARCHAR(255),
    google_calendar_id    VARCHAR(255)
);

INSERT INTO bot_settings (id, admin_group_chat_id, admin_ids, google_spreadsheet_id, google_calendar_id)
VALUES (1, '-4992543013', '456926335', '1P6QsLZVtYUYIsWns-fiwNyK9XvyT2VAp3VQMR3btWnc', '34707f34cf080c98ac26ea0bb1b05da48d26dfcf4960ce78b05a2b640a5ad238@group.calendar.google.com')
ON CONFLICT (id) DO NOTHING;
