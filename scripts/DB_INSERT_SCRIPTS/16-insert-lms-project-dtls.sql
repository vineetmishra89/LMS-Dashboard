
INSERT INTO lms_schema.LMS_PROJECT_DTLS (
    PROJECT_NAME, 
    PM_EMAIL_ID, 
    ADM_EMAIL_ID, 
    OFFSHORE_DD_EMAIL_ID, 
    ONSITE_DD_EMAIL_ID, 
    HRBP_EMAIL_ID, 
    PROJ_ACTIVE_FLAG, 
    CREATED_BY, 
    CREATED_TS, 
    UPDATED_BY, 
    UPDATED_TS
) VALUES
(
    'Digital Banking Platform',
    'pm1@example.com',
    'admin1@example.com',
    'offshore.dd1@example.com',
    'onsite.dd1@example.com',
    'hrbp1@example.com',
    'Y',
    'SYSTEM',
    CURRENT_TIMESTAMP,
    'SYSTEM',
    CURRENT_TIMESTAMP
),
(
    'Customer Portal Modernization',
    'pm1@example.com',
    'admin2@example.com',
    'offshore.dd2@example.com',
    'onsite.dd2@example.com',
    'hrbp1@example.com',
    'Y',
    'SYSTEM',
    CURRENT_TIMESTAMP,
    'SYSTEM',
    CURRENT_TIMESTAMP
),
(
    'Mobile Banking App',
    'pm2@example.com',
    'admin1@example.com',
    'offshore.dd1@example.com',
    'onsite.dd3@example.com',
    'hrbp2@example.com',
    'Y',
    'SYSTEM',
    CURRENT_TIMESTAMP,
    'SYSTEM',
    CURRENT_TIMESTAMP
),
(
    'Data Analytics Platform',
    'pm2@example.com',
    'admin2@example.com',
    'offshore.dd3@example.com',
    'onsite.dd4@example.com',
    'hrbp2@example.com',
    'Y',
    'SYSTEM',
    CURRENT_TIMESTAMP,
    'SYSTEM',
    CURRENT_TIMESTAMP
),
(
    'Legacy System Migration',
    'pm3@example.com',
    'admin1@example.com',
    'offshore.dd2@example.com',
    'onsite.dd5@example.com',
    'hrbp3@example.com',
    'Y',
    'SYSTEM',
    CURRENT_TIMESTAMP,
    'SYSTEM',
    CURRENT_TIMESTAMP
);

COMMIT;
