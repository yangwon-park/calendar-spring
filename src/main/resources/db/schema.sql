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
	id          BIGINT AUTO_INCREMENT PRIMARY KEY,
	account_id  BIGINT               NOT NULL,
	provider    VARCHAR(20)          NOT NULL,
	provider_user_id VARCHAR(255)         NOT NULL,
	created_at  TIMESTAMP            NOT NULL,
	updated_at  TIMESTAMP            NOT NULL,
	CONSTRAINT UK_account_provider_sns_user_id UNIQUE (provider_user_id),
	CONSTRAINT UK_account_provider_account_id UNIQUE (account_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;