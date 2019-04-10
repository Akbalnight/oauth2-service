DROP table IF EXISTS oauth_refresh_token cascade;
DROP table IF EXISTS oauth_access_token cascade;

CREATE TABLE oauth_access_token
(
    authentication_id character varying(256) NOT NULL,
    token_id character varying(256),
    token bytea,
    user_name character varying(256),
    client_id character varying(256),
    authentication bytea,
    refresh_token character varying(256),
    CONSTRAINT pk_oauth_access_token PRIMARY KEY (authentication_id)
);

CREATE TABLE oauth_refresh_token
(
    token_id character varying(256),
    token bytea,
    authentication bytea
)