CREATE TABLE IF NOT EXISTS email__email (
    id SERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    --
    sender TEXT NOT NULL,
    addressee TEXT NOT NULL,
    subject TEXT NOT NULL,
    body TEXT NOT NULL,
    last_attempt_at TIMESTAMP WITH TIME ZONE,
    sent_at TIMESTAMP WITH TIME ZONE
);
