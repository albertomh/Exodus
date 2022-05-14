CREATE TABLE IF NOT EXISTS auth__user (
    id SERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    created_by_id uuid NOT NULL REFERENCES auth__user(id),
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    deleted_by_id uuid REFERENCES auth__user(id),
    --
    email TEXT NOT NULL,
    password TEXT NOT NULL,
    activated_at TIMESTAMP WITH TIME ZONE,
    deactivated_at TIMESTAMP WITH TIME ZONE
);
