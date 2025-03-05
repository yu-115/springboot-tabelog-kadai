CREATE TABLE IF NOT EXISTS categories (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS shops (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	name VARCHAR(50) NOT NULL,
	image_name VARCHAR(255),
	description VARCHAR(255) NOT NULL,
	price INT NOT NULL,
	capacity INT NOT NULL,
	address VARCHAR(255) NOT NULL,
	phone_number VARCHAR(50) NOT NULL,
	created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS shop_categories (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	shop_id INT NOT NULL,
	category_id INT NOT NULL,
	FOREIGN KEY (shop_id) REFERENCES shops (id),
	FOREIGN KEY (category_id) REFERENCES categories (id)
);

CREATE TABLE IF NOT EXISTS roles (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	role_id INT NOT NULL,
	name VARCHAR(50) NOT NULL,
	furigana VARCHAR(50) NOT NULL,
	phone_number VARCHAR(50) NOT NULL,
	email VARCHAR(255) NOT NULL UNIQUE,
	password VARCHAR(255) NOT NULL,
	enabled BOOLEAN NOT NULL,
	created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE IF NOT EXISTS verification_tokens (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	user_id INT NOT NULL UNIQUE,
	token VARCHAR(255) NOT NULL,
	created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS reservations (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	shop_id INT NOT NULL,
	user_id INT NOT NULL,
	check_in DATETIME NOT NULL,
	number_of_people INT NOT NULL,
	created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	FOREIGN KEY (shop_id) REFERENCES shops (id),
	FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS reviews (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	shop_id INT NOT NULL,
	user_id INT NOT NULL,
	user_name VARCHAR(50) NOT NULL,
	review_comment VARCHAR(255) NOT NULL,
	score VARCHAR(20) NOT NULL,
	enabled BOOLEAN NOT NULL,
	created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	FOREIGN KEY (shop_id) REFERENCES shops (id),
	FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS favorites (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	shop_id INT NOT NULL,
	user_id INT NOT NULL,
	FOREIGN KEY (shop_id) REFERENCES shops (id),
	FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS users_paid (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	user_id INT NOT NULL UNIQUE,
	payment_id VARCHAR(255) NOT NULL,
	customer_id VARCHAR(255) NOT NULL,
	FOREIGN KEY (user_id) REFERENCES users (id)
);