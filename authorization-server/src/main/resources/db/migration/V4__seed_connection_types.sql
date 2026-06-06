INSERT INTO connection_types (id, name, description, is_social, required_fields, settings_schema, is_active, created_at)
VALUES
    (
        gen_random_uuid(),
        'Google',
        'Google OAuth2 provider',
        true,
        '["clientId","clientSecret","redirectUri"]',
        '[
            {"key":"clientId","label":"Client ID","type":"text","placeholder":"Google client id","required":true},
            {"key":"clientSecret","label":"Client Secret","type":"password","placeholder":"Google client secret","required":true,"secret":true},
            {"key":"redirectUri","label":"Redirect URI","type":"text","placeholder":"https://yourapp.com/oauth2/callback","required":true}
        ]',
        true,
        now()
    ),
    (
        gen_random_uuid(),
        'GitHub',
        'GitHub OAuth2 provider',
        true,
        '["clientId","clientSecret","redirectUri"]',
        '[
            {"key":"clientId","label":"Client ID","type":"text","placeholder":"GitHub client id","required":true},
            {"key":"clientSecret","label":"Client Secret","type":"password","placeholder":"GitHub client secret","required":true,"secret":true},
            {"key":"redirectUri","label":"Redirect URI","type":"text","placeholder":"https://yourapp.com/oauth2/callback","required":true}
        ]',
        true,
        now()
    ),
    (
        gen_random_uuid(),
        'SAML',
        'SAML 2.0 enterprise SSO',
        false,
        '["metadataUrl","entityId","acsUrl","certificate"]',
        '[
            {"key":"metadataUrl","label":"Metadata URL","type":"text","placeholder":"https://idp.example.com/metadata","required":true},
            {"key":"entityId","label":"Entity ID","type":"text","placeholder":"urn:authora:tenant","required":true},
            {"key":"acsUrl","label":"ACS URL","type":"text","placeholder":"https://yourapp.com/sso/callback","required":true},
            {"key":"certificate","label":"X.509 Certificate","type":"text","placeholder":"-----BEGIN CERTIFICATE-----","required":true}
        ]',
        true,
        now()
    )
ON CONFLICT (name) DO UPDATE
SET description = EXCLUDED.description,
    is_social = EXCLUDED.is_social,
    required_fields = EXCLUDED.required_fields,
    settings_schema = EXCLUDED.settings_schema,
    is_active = EXCLUDED.is_active;

