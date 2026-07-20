CREATE TABLE friendships (
    id BINARY(16) NOT NULL,
    requester_id BINARY(16) NOT NULL,
    receiver_id BINARY(16) NOT NULL,
    status VARCHAR(20) NOT NULL,
    blocked_by BINARY(16) NULL,
    created_at DATETIME(6) NOT NULL,
    accepted_at DATETIME(6) NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_friendship_pair UNIQUE (requester_id, receiver_id)
);

CREATE INDEX idx_friendship_receiver ON friendships (receiver_id);
CREATE INDEX idx_friendship_status ON friendships (status);
