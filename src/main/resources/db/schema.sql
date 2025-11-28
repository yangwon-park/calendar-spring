CREATE TABLE IF NOT EXISTS account
(
	id           BIGINT AUTO_INCREMENT PRIMARY KEY,
	email        VARCHAR(255)         NOT NULL,
	password     VARCHAR(255)         NULL,
	name         VARCHAR(255)         NOT NULL,
	role         VARCHAR(50)          NOT NULL,
	provider     VARCHAR(50)          NOT NULL,
	is_banned    TINYINT(1) DEFAULT 0 NOT NULL,
	is_withdraw  TINYINT(1) DEFAULT 0 NOT NULL,
	is_deleted   TINYINT(1) DEFAULT 0 NOT NULL,
	banned_at    TIMESTAMP            NULL,
	withdrawn_at TIMESTAMP            NULL,
	created_at   TIMESTAMP            NOT NULL,
	updated_at   TIMESTAMP            NOT NULL,
	CONSTRAINT UK_account_email UNIQUE (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS admin
(
	id         BIGINT AUTO_INCREMENT PRIMARY KEY,
	account_id BIGINT      NOT NULL,
	role       VARCHAR(20) NOT NULL,
	created_at TIMESTAMP   NOT NULL,
	updated_at TIMESTAMP   NOT NULL,
	CONSTRAINT UK_admin_account_id UNIQUE (account_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS account_provider
(
	id               BIGINT AUTO_INCREMENT PRIMARY KEY,
	account_id       BIGINT       NOT NULL,
	provider         VARCHAR(20)  NOT NULL,
	provider_user_id VARCHAR(255) NOT NULL,
	created_at       TIMESTAMP    NOT NULL,
	updated_at       TIMESTAMP    NOT NULL,
	CONSTRAINT UK_account_provider_provider_user_id UNIQUE (provider_user_id),
	CONSTRAINT UK_account_provider_account_id UNIQUE (account_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS couple
(
	id          BIGINT AUTO_INCREMENT PRIMARY KEY,
	account1_id BIGINT    NOT NULL,
	account2_id BIGINT    NOT NULL,
	start_date  DATE      NOT NULL,
	created_at  TIMESTAMP NOT NULL,
	updated_at  TIMESTAMP NOT NULL,
	CONSTRAINT UK_couple_account1_id_account2_id UNIQUE (account1_id, account2_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS calendar
(
	id          BIGINT PRIMARY KEY AUTO_INCREMENT,
	owner_id    BIGINT       NOT NULL,
	name        VARCHAR(100) NOT NULL,
	type        VARCHAR(20)  NOT NULL, -- PERSONAL, COUPLE, GROUP
	color       VARCHAR(7)   NOT NULL,
	description TEXT,
	created_at  TIMESTAMP     NOT NULL,
	updated_at  TIMESTAMP     NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS calendar_member
(
	id          BIGINT PRIMARY KEY AUTO_INCREMENT,
	calendar_id BIGINT      NOT NULL,
	account_id  BIGINT      NOT NULL,
	role        VARCHAR(20) NOT NULL, -- OWNER, ADMIN, MEMBER, VIEWER 등
	status      VARCHAR(20) NOT NULL, -- ACCEPTED, PENDING (초대 수락 여부 등)
	created_at  TIMESTAMP   NOT NULL,

	CONSTRAINT UK_calendar_member UNIQUE (calendar_id, account_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS event
(
	id          BIGINT PRIMARY KEY AUTO_INCREMENT,
	calendar_id BIGINT       NOT NULL,
	account_id  BIGINT       NOT NULL,
	category_id BIGINT       NOT NULL,
	title       VARCHAR(200) NOT NULL,
	description TEXT,
	event_date  DATE         NOT NULL,
	created_at  DATETIME     NOT NULL,
	updated_at  DATETIME     NOT NULL,
	INDEX IDX_calendar_date (calendar_id, event_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;