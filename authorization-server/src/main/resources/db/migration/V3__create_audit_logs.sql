CREATE TABLE IF NOT EXISTS public.audit_logs
(
    id            uuid        NOT NULL PRIMARY KEY,
    tenant_id     uuid        NOT NULL REFERENCES public.tenants ON DELETE CASCADE,
    actor_user_id uuid        NULL REFERENCES public.users ON DELETE SET NULL,
    action        varchar(100) NOT NULL,
    target_type   varchar(100),
    target_id     varchar(255),
    metadata      jsonb       NOT NULL DEFAULT '{}'::jsonb,
    ip            varchar(45),
    user_agent    varchar(255),
    created_at    timestamp   NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_tenant_created
    ON public.audit_logs (tenant_id, created_at DESC);

