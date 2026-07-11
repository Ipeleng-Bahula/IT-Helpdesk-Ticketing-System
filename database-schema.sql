-- ================================================================
--  HelpDesk — Full Schema + Mock Data
--  Run in pgAdmin against: helpdesk_db
--  Schema:  helpdesk
--  Tables:  app_user | ticket | comment
--
--  All user passwords = Password123!  (BCrypt encoded)
-- ================================================================


-- ----------------------------------------------------------------
-- 0. SETUP SCHEMA
-- ----------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS helpdesk AUTHORIZATION helpdesk_user;
SET search_path TO helpdesk;


-- ----------------------------------------------------------------
-- 1. DROP TABLES (safe order — children before parents)
-- ----------------------------------------------------------------
DROP TABLE IF EXISTS helpdesk.comment  CASCADE;
DROP TABLE IF EXISTS helpdesk.ticket   CASCADE;
DROP TABLE IF EXISTS helpdesk.app_user CASCADE;
 
 
-- ----------------------------------------------------------------
-- 2. CREATE TABLES
--    Column names match exactly what Hibernate generates from the
--    Java field names (camelCase → snake_case by Spring convention)
-- ----------------------------------------------------------------
 
-- 2a. app_user
CREATE TABLE helpdesk.app_user (
    id         BIGSERIAL    PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL DEFAULT 'USER'
                            CHECK (role IN ('USER','TECHNICIAN','ADMIN')),
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);
 
-- 2b. ticket
CREATE TABLE helpdesk.ticket (
    id           BIGSERIAL    PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    description  TEXT         NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'OPEN'
                              CHECK (status IN ('OPEN','IN_PROGRESS','RESOLVED','CLOSED')),
    priority     VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM'
                              CHECK (priority IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    created_by   BIGINT       NOT NULL REFERENCES helpdesk.app_user(id),
    assigned_to  BIGINT                REFERENCES helpdesk.app_user(id),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);
 
-- 2c. comment
CREATE TABLE helpdesk.comment (
    id         BIGSERIAL PRIMARY KEY,
    content    TEXT      NOT NULL,
    ticket_id  BIGINT    NOT NULL REFERENCES helpdesk.ticket(id)   ON DELETE CASCADE,
    author_id  BIGINT    NOT NULL REFERENCES helpdesk.app_user(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
 
 
-- ----------------------------------------------------------------
-- 3. INDEXES
-- ----------------------------------------------------------------
CREATE INDEX idx_ticket_created_by  ON helpdesk.ticket(created_by);
CREATE INDEX idx_ticket_assigned_to ON helpdesk.ticket(assigned_to);
CREATE INDEX idx_ticket_status      ON helpdesk.ticket(status);
CREATE INDEX idx_comment_ticket_id  ON helpdesk.comment(ticket_id);
CREATE INDEX idx_comment_author_id  ON helpdesk.comment(author_id);
 
 
-- ----------------------------------------------------------------
-- 4. GRANT PERMISSIONS
-- ----------------------------------------------------------------
GRANT ALL PRIVILEGES ON ALL TABLES    IN SCHEMA helpdesk TO helpdesk_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA helpdesk TO helpdesk_user;
 
 
-- ================================================================
--  MOCK DATA
-- ================================================================
 
 
-- ----------------------------------------------------------------
-- 5. app_user
--    All passwords = Password123!  (real BCrypt hash below)
-- ----------------------------------------------------------------
INSERT INTO helpdesk.app_user
    (username,      email,                        password,                                                       role,         created_at)
VALUES
('admin',          'admin@helpdesk.co.za',        '$2b$10$yeYrcj51EsBIV56uxrZY3OfhI2wCmOeEo6xoA.uu6q8qK2SV2ihbe', 'ADMIN',       NOW() - INTERVAL '90 days'),
('tech_sipho',     'sipho@helpdesk.co.za',        '$2b$10$yeYrcj51EsBIV56uxrZY3OfhI2wCmOeEo6xoA.uu6q8qK2SV2ihbe', 'TECHNICIAN',  NOW() - INTERVAL '60 days'),
('tech_naledi',    'naledi@helpdesk.co.za',       '$2b$10$yeYrcj51EsBIV56uxrZY3OfhI2wCmOeEo6xoA.uu6q8qK2SV2ihbe', 'TECHNICIAN',  NOW() - INTERVAL '55 days'),
('lulu',           'lulu@gmail.com',              '$2b$10$yeYrcj51EsBIV56uxrZY3OfhI2wCmOeEo6xoA.uu6q8qK2SV2ihbe', 'USER',        NOW() - INTERVAL '30 days'),
('john_doe',       'john.doe@company.co.za',      '$2b$10$yeYrcj51EsBIV56uxrZY3OfhI2wCmOeEo6xoA.uu6q8qK2SV2ihbe', 'USER',        NOW() - INTERVAL '45 days'),
('sarah_k',        'sarah.k@company.co.za',       '$2b$10$yeYrcj51EsBIV56uxrZY3OfhI2wCmOeEo6xoA.uu6q8qK2SV2ihbe', 'USER',        NOW() - INTERVAL '40 days'),
('thabo_m',        'thabo.m@company.co.za',       '$2b$10$yeYrcj51EsBIV56uxrZY3OfhI2wCmOeEo6xoA.uu6q8qK2SV2ihbe', 'USER',        NOW() - INTERVAL '20 days'),
('priya_n',        'priya.n@company.co.za',       '$2b$10$yeYrcj51EsBIV56uxrZY3OfhI2wCmOeEo6xoA.uu6q8qK2SV2ihbe', 'USER',        NOW() - INTERVAL '15 days');
 
 
-- ----------------------------------------------------------------
-- 6. ticket
--    User ID map: 1=admin 2=tech_sipho 3=tech_naledi 4=lulu
--                 5=john_doe 6=sarah_k 7=thabo_m 8=priya_n
-- ----------------------------------------------------------------
INSERT INTO helpdesk.ticket
    (title, description, status, priority, created_by, assigned_to, created_at, updated_at)
VALUES
 
('Cannot connect to company VPN',
 'Since this morning I am unable to connect to the VPN from home. Error: "Authentication failed." I have not changed my password and it was working fine yesterday. Tried restarting laptop and VPN client.',
 'OPEN', 'HIGH', 5, NULL,
 NOW() - INTERVAL '2 hours',  NOW() - INTERVAL '2 hours'),
 
('Printer on 3rd floor not responding',
 'The HP LaserJet printer near the 3rd floor kitchen shows offline in Windows. Multiple staff affected. Turning it off and on again did not help.',
 'OPEN', 'MEDIUM', 6, NULL,
 NOW() - INTERVAL '5 hours',  NOW() - INTERVAL '5 hours'),
 
('Request for Adobe Acrobat Pro license',
 'I need Adobe Acrobat Pro to edit and sign PDF contracts. I only have the free Reader version. Manager has approved this request.',
 'OPEN', 'LOW', 7, NULL,
 NOW() - INTERVAL '1 day',    NOW() - INTERVAL '1 day'),
 
('Laptop screen flickering intermittently',
 'My Dell Latitude 5520 (Asset: DL-00234) screen flickers 4-5 times per hour for a few seconds. Very disruptive during video calls. Started 3 days ago.',
 'OPEN', 'MEDIUM', 8, NULL,
 NOW() - INTERVAL '3 days',   NOW() - INTERVAL '3 days'),
 
('CRITICAL: Production database returning 500 errors',
 'Main application returning HTTP 500 for all users since 14:30. Logs show: "Connection pool exhausted". All 200+ users affected. Business operations stopped.',
 'OPEN', 'CRITICAL', 4, NULL,
 NOW() - INTERVAL '30 minutes', NOW() - INTERVAL '30 minutes'),
 
('Outlook not syncing emails',
 'Outlook inbox has not synced since yesterday afternoon. Can send but nothing comes in. Tried removing and re-adding account. Running Outlook 365 on Windows 11.',
 'IN_PROGRESS', 'HIGH', 5, 2,
 NOW() - INTERVAL '2 days',   NOW() - INTERVAL '4 hours'),
 
('New employee laptop setup required',
 'New starter joining Monday: Keanu Mokoena (Software Developer). Needs Windows 11, VS Code, Git, Java 17, PostgreSQL, IntelliJ IDEA. Add to Dev network group. Contact HR for asset assignment.',
 'IN_PROGRESS', 'MEDIUM', 6, 3,
 NOW() - INTERVAL '3 days',   NOW() - INTERVAL '1 day'),
 
('Slow internet on 2nd floor',
 'Everyone on 2nd floor is getting 2Mbps download instead of the usual 100Mbps. Started after the maintenance window on Saturday. About 30 staff affected.',
 'IN_PROGRESS', 'HIGH', 7, 2,
 NOW() - INTERVAL '4 days',   NOW() - INTERVAL '6 hours'),
 
('Teams video calls dropping after 10 minutes',
 'Microsoft Teams meetings drop around the 10-minute mark. Audio continues but video freezes and I get disconnected. Happened in 5 consecutive meetings today.',
 'IN_PROGRESS', 'MEDIUM', 8, 3,
 NOW() - INTERVAL '1 day',    NOW() - INTERVAL '3 hours'),
 
('Cannot log into HR portal',
 'Getting "Invalid credentials" on hr.company.co.za. My Windows login works fine but the HR portal keeps rejecting my password.',
 'RESOLVED', 'MEDIUM', 4, 2,
 NOW() - INTERVAL '7 days',   NOW() - INTERVAL '5 days'),
 
('Request for dual monitor installation',
 'Requesting a second monitor for my workstation. I do a lot of spreadsheet comparison work and my desk has space for a second screen.',
 'RESOLVED', 'LOW', 5, 3,
 NOW() - INTERVAL '10 days',  NOW() - INTERVAL '7 days'),
 
('Antivirus flagging internal reporting tool',
 'CrowdStrike is quarantining our internal ReportGen v2.1.3 tool as malware. It is developed internally and is safe. Please whitelist SHA256: a3f8c2d1e9b047f6c3a2d8e1f9b04c7d.',
 'RESOLVED', 'HIGH', 6, 2,
 NOW() - INTERVAL '14 days',  NOW() - INTERVAL '12 days'),
 
('Password reset — locked out of workstation',
 'Forgot my Windows login password and am locked out. I am at the office and need urgent in-person assistance.',
 'CLOSED', 'HIGH', 7, 3,
 NOW() - INTERVAL '20 days',  NOW() - INTERVAL '19 days'),
 
('Office 365 activation error on new laptop',
 'After receiving my new laptop, Office 365 shows "Product Activation Failed". Signed in with company account but it still shows unlicensed.',
 'CLOSED', 'MEDIUM', 8, 2,
 NOW() - INTERVAL '25 days',  NOW() - INTERVAL '23 days'),
 
('Wi-Fi dropping in boardroom B1',
 'Wi-Fi drops every 15-20 minutes in the main boardroom (Room B1). Very disruptive during client presentations. Please investigate access point AP-B1-04.',
 'CLOSED', 'HIGH', 4, 3,
 NOW() - INTERVAL '30 days',  NOW() - INTERVAL '28 days');
 
 
-- ----------------------------------------------------------------
-- 7. comment
--    Ticket ID map (insertion order above):
--      1 VPN  2 Printer  3 Adobe  4 Laptop flicker  5 CRITICAL DB
--      6 Outlook  7 New employee  8 Slow internet  9 Teams calls
--      10 HR portal  11 Dual monitor  12 Antivirus  13 Password reset
--      14 Office 365  15 Wi-Fi boardroom
-- ----------------------------------------------------------------
INSERT INTO helpdesk.comment
    (content, ticket_id, author_id, created_at)
VALUES
 
('Escalating immediately to the senior infrastructure team. Investigating connection pool settings on the database server now.',
 5, 1, NOW() - INTERVAL '25 minutes'),
 
('Hi John, I have picked up your ticket. Can you go to File → Account Settings → Account Settings, click your account and hit "Test Account Settings"? Let me know what result you get.',
 6, 2, NOW() - INTERVAL '2 days'),
 
('Hi Sipho, I ran the test and got: "Log onto incoming mail server (IMAP): Failed". The error says connection timed out.',
 6, 5, NOW() - INTERVAL '1 day' - INTERVAL '8 hours'),
 
('Thanks for that. IMAP port 993 is being blocked by the firewall after our security update yesterday. I have logged a firewall rule change request — should be resolved within 2 hours.',
 6, 2, NOW() - INTERVAL '4 hours'),
 
('Collected the laptop from the asset room (Asset: DL-00289). Starting setup now — installing Windows 11 and running all updates first. Should be done by end of today.',
 7, 3, NOW() - INTERVAL '2 days'),
 
('Windows 11 setup complete. Installing dev tools now. Quick question — does Keanu need IntelliJ IDEA in addition to VS Code?',
 7, 3, NOW() - INTERVAL '1 day' - INTERVAL '4 hours'),
 
('Yes, please add IntelliJ IDEA Community Edition as well. Thank you for the fast turnaround!',
 7, 6, NOW() - INTERVAL '1 day' - INTERVAL '2 hours'),
 
('I have checked the core switch and can see packet loss on the uplink port for the 2nd floor. Running diagnostics now and will update shortly.',
 8, 2, NOW() - INTERVAL '3 days'),
 
('Traced the issue to a misconfigured VLAN after Saturday maintenance. Rolling back the change now. ETA 30 minutes.',
 8, 2, NOW() - INTERVAL '2 days' - INTERVAL '6 hours'),
 
('Hi Priya, I have checked your Teams diagnostics and can see the video stream is maxing out your upload bandwidth at 10 minutes. Are you on Wi-Fi or wired?',
 9, 3, NOW() - INTERVAL '23 hours'),
 
('I am on Wi-Fi. Should I switch to wired?',
 9, 8, NOW() - INTERVAL '22 hours'),
 
('Yes please try wired and let me know if the issue persists. I will also look at the Wi-Fi channel congestion near your desk.',
 9, 3, NOW() - INTERVAL '21 hours'),
 
('Hi Lulu, your HR portal account was locked after 5 failed login attempts. I have unlocked it and reset the portal-specific password. Please check your email for the temporary password.',
 10, 2, NOW() - INTERVAL '6 days'),
 
('That worked perfectly, thank you Sipho! I can now access the HR portal. You can close this ticket.',
 10, 4, NOW() - INTERVAL '5 days'),
 
('Great! Closing the ticket. Note: the HR portal uses a separate password from your Windows account. You can sync them at hr.company.co.za/settings → Security → Link AD Account.',
 10, 2, NOW() - INTERVAL '5 days' + INTERVAL '30 minutes'),
 
('Hi John, I have reserved a 24" Dell monitor from the asset store. Will come to your desk tomorrow morning for installation. Do you have a preference for left or right side?',
 11, 3, NOW() - INTERVAL '9 days'),
 
('Right side please. Thank you Naledi!',
 11, 5, NOW() - INTERVAL '9 days' + INTERVAL '1 hour'),
 
('Monitor installed and configured. Display settings set to "Extend" mode. Let me know if you need any adjustments.',
 11, 3, NOW() - INTERVAL '7 days'),
 
('Can you please confirm the exact file path and version of ReportGen being flagged? I need this for the whitelist submission to the security team.',
 12, 2, NOW() - INTERVAL '13 days'),
 
('Path: C:\\Tools\\ReportGen\\reportgen.exe — Version: 2.1.3. Installer also on \\\\devshare\\tools\\reportgen.',
 12, 6, NOW() - INTERVAL '13 days' + INTERVAL '2 hours'),
 
('Whitelist approved by the security team and pushed to all endpoints. CrowdStrike policy propagation takes about 30 minutes. Please confirm once resolved.',
 12, 2, NOW() - INTERVAL '12 days'),
 
('Confirmed! ReportGen is running without issues now. Thank you for the quick resolution.',
 12, 6, NOW() - INTERVAL '12 days' + INTERVAL '1 hour'),
 
('Hi Thabo, I am on my way to your desk now. Please have your staff ID card ready for identity verification.',
 13, 3, NOW() - INTERVAL '19 days' + INTERVAL '20 minutes'),
 
('Password reset done in person. Thabo is back at his workstation. Closing ticket.',
 13, 3, NOW() - INTERVAL '19 days' + INTERVAL '45 minutes'),
 
('Hi Priya, I can see your device is not registered in our Microsoft tenant. I am running the activation script remotely now. Please keep your laptop on.',
 14, 2, NOW() - INTERVAL '24 days'),
 
('Script completed. Office 365 should be activated now. Please open Word and check if the "Product Activation Failed" banner is gone.',
 14, 2, NOW() - INTERVAL '24 days' + INTERVAL '15 minutes'),
 
('It is activated! All Office apps are working correctly. Thank you.',
 14, 8, NOW() - INTERVAL '23 days'),
 
('I have checked access point AP-B1-04. Logs show it has been rebooting every 20 minutes due to a firmware bug. Updating the firmware now.',
 15, 3, NOW() - INTERVAL '29 days'),
 
('Firmware updated from v2.1.0 to v2.3.4. The auto-reboot bug is fixed in this version. Monitored for 2 hours — connection is stable. Please test and confirm.',
 15, 3, NOW() - INTERVAL '28 days' + INTERVAL '2 hours'),
 
('We had 3 back-to-back meetings in the boardroom today with zero drops. Fully resolved — thank you!',
 15, 4, NOW() - INTERVAL '28 days' + INTERVAL '6 hours');
 
 
-- ================================================================
-- 8. VERIFY
-- ================================================================
SELECT 'app_user' AS table_name, COUNT(*) AS total_rows FROM helpdesk.app_user
UNION ALL
SELECT 'ticket',                  COUNT(*)               FROM helpdesk.ticket
UNION ALL
SELECT 'comment',                 COUNT(*)               FROM helpdesk.comment;
 
SELECT status, COUNT(*) AS count
FROM helpdesk.ticket
GROUP BY status
ORDER BY status;