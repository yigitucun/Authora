create table public.oauth2_registered_client
(
    id                            varchar(100)                                       not null
        primary key,
    client_id                     varchar(100)                                       not null
        unique,
    client_id_issued_at           timestamp with time zone default CURRENT_TIMESTAMP not null,
    client_secret                 varchar(200)             default NULL::character varying,
    client_secret_expires_at      timestamp with time zone,
    client_name                   varchar(200)                                       not null,
    client_authentication_methods varchar(1000)                                      not null,
    authorization_grant_types     varchar(1000)                                      not null,
    redirect_uris                 varchar(1000)            default NULL::character varying,
    post_logout_redirect_uris     varchar(1000)            default NULL::character varying,
    scopes                        varchar(1000)                                      not null,
    client_settings               varchar(2000)                                      not null,
    token_settings                varchar(2000)                                      not null,
    tenant_id                     uuid
);

alter table public.oauth2_registered_client
    owner to postgres;

create table public.oauth2_authorization
(
    id                            varchar(100) not null
        primary key,
    registered_client_id          varchar(100) not null,
    principal_name                varchar(200) not null,
    authorization_grant_type      varchar(100) not null,
    authorized_scopes             varchar(1000) default NULL::character varying,
    attributes                    text,
    state                         varchar(500)  default NULL::character varying,
    authorization_code_value      text,
    authorization_code_issued_at  timestamp with time zone,
    authorization_code_expires_at timestamp with time zone,
    authorization_code_metadata   text,
    access_token_value            text,
    access_token_issued_at        timestamp with time zone,
    access_token_expires_at       timestamp with time zone,
    access_token_metadata         text,
    access_token_type             varchar(100)  default NULL::character varying,
    access_token_scopes           varchar(1000) default NULL::character varying,
    oidc_id_token_value           text,
    oidc_id_token_issued_at       timestamp with time zone,
    oidc_id_token_expires_at      timestamp with time zone,
    oidc_id_token_metadata        text,
    refresh_token_value           text,
    refresh_token_issued_at       timestamp with time zone,
    refresh_token_expires_at      timestamp with time zone,
    refresh_token_metadata        text,
    user_code_value               text,
    user_code_issued_at           timestamp with time zone,
    user_code_expires_at          timestamp with time zone,
    user_code_metadata            text,
    device_code_value             text,
    device_code_issued_at         timestamp with time zone,
    device_code_expires_at        timestamp with time zone,
    device_code_metadata          text
);

alter table public.oauth2_authorization
    owner to postgres;

create table public.oauth2_authorization_consent
(
    registered_client_id varchar(100)  not null,
    principal_name       varchar(200)  not null,
    authorities          varchar(1000) not null,
    primary key (registered_client_id, principal_name)
);

alter table public.oauth2_authorization_consent
    owner to postgres;

create table public.connection_types
(
    id              uuid      default gen_random_uuid() not null
        primary key,
    name            varchar(50)                         not null
        unique,
    description     varchar(255),
    is_social       boolean   default false             not null,
    required_fields jsonb     default '[]'::jsonb       not null,
    settings_schema jsonb     default '[]'::jsonb       not null,
    is_active       boolean   default true              not null,
    created_at      timestamp default now()             not null
);

alter table public.connection_types
    owner to postgres;

create table public.app_connections
(
    id                 uuid      default gen_random_uuid() not null
        primary key,
    client_id          varchar(100)                        not null
        references public.oauth2_registered_client (client_id)
            on delete cascade,
    connection_type_id uuid                                not null
        references public.connection_types,
    is_enabled         boolean   default true              not null,
    settings           jsonb,
    form_config        jsonb,
    created_at         timestamp default now()             not null,
    updated_at         timestamp default now()             not null,
    constraint uq_app_connections
        unique (client_id, connection_type_id)
);

alter table public.app_connections
    owner to postgres;

create index idx_app_conn_client_id
    on public.app_connections (client_id);

create index idx_app_conn_type_id
    on public.app_connections (connection_type_id);

create table public.tenants
(
    id                   uuid      default gen_random_uuid() not null
        primary key,
    name                 varchar(100)                        not null,
    is_active            boolean   default true              not null,
    created_at           timestamp default now()             not null,
    updated_at           timestamp default now()             not null,
    company_name         varchar(100),
    usage_type           varchar(20),
    company_size         varchar(20),
    onboarding_completed boolean   default false             not null
);

alter table public.tenants
    owner to postgres;

create table public.users
(
    id              uuid      default gen_random_uuid() not null
        primary key,
    tenant_id       uuid                                not null
        references public.tenants
            on delete cascade,
    email           varchar(255)                        not null,
    password        varchar(255),
    is_verified     boolean   default false             not null,
    created_at      timestamp default now()             not null,
    updated_at      timestamp default now()             not null,
    is_tenant_admin boolean   default false             not null,
    constraint uq_users_tenant_email
        unique (tenant_id, email)
);

alter table public.users
    owner to postgres;

create table public.audit_logs
(
    id            uuid                          not null
        primary key,
    tenant_id     uuid                          not null
        references public.tenants
            on delete cascade,
    actor_user_id uuid
                                                references public.users
                                                    on delete set null,
    action        varchar(100)                  not null,
    target_type   varchar(100),
    target_id     varchar(255),
    metadata      jsonb     default '{}'::jsonb not null,
    ip            varchar(45),
    user_agent    varchar(255),
    created_at    timestamp default now()       not null
);

alter table public.audit_logs
    owner to postgres;

create index idx_audit_logs_tenant_created
    on public.audit_logs (tenant_id asc, created_at desc);

