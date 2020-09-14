CREATE TABLE IF NOT EXISTS "table.tableName"
(
    id          INTEGER AUTO_INCREMENT PRIMARY KEY,
    description varchar(128),
    date        timestamp,
    uuid        varchar(36),
    owner       varchar(20),
    world       varchar(30),
    x           double(30, 20),
    y           double(30, 20),
    z           double(30, 20),
    p           double(30, 20),
    f           double(30, 20),
    adminreply  varchar(128),
    userreply   varchar(128),
    status      varchar(16),
    admin       varchar(30) collate latin1_swedish_ci,
    expiration  timestamp NULL DEFAULT NULL,
    server      varchar(30)
);