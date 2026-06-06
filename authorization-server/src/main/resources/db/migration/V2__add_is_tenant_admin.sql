ALTER TABLE public.users
    ADD COLUMN IF NOT EXISTS is_tenant_admin boolean DEFAULT false NOT NULL;

