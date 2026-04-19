-- Initial schema for PaintingStore
-- NOTE: If you run this via Flyway, Flyway executes against the configured database; you can remove the CREATE DATABASE / USE lines if not desired.

-- IMPORTANT: Flyway runs against the database configured in your datasource.
-- Remove CREATE DATABASE / USE from migrations when using Flyway-managed database.

-- Roles
CREATE TABLE IF NOT EXISTS roles (
  role_id INT AUTO_INCREMENT PRIMARY KEY,
  role_name VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Admins
CREATE TABLE IF NOT EXISTS admins (
  admin_id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role_id INT DEFAULT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_admin_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Artists
CREATE TABLE IF NOT EXISTS artists (
  artist_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  slug VARCHAR(255) NOT NULL UNIQUE,
  birth_year INT DEFAULT NULL,
  death_year INT DEFAULT NULL,
  biography TEXT,
  nationality VARCHAR(100),
  image_url VARCHAR(500),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Categories (self-referencing parent_id)
CREATE TABLE IF NOT EXISTS categories (
  category_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  slug VARCHAR(255) NOT NULL UNIQUE,
  parent_id INT DEFAULT NULL,
  CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES categories(category_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Artworks
CREATE TABLE IF NOT EXISTS artworks (
  artwork_id INT AUTO_INCREMENT PRIMARY KEY,
  type VARCHAR(100),
  artist_id INT DEFAULT NULL,
  created_by INT DEFAULT NULL,
  title VARCHAR(255) NOT NULL,
  slug VARCHAR(255) NOT NULL UNIQUE,
  year_created INT DEFAULT NULL,
  description TEXT,
  price DECIMAL(12,2) DEFAULT 0.00,
  is_published TINYINT(1) DEFAULT 0,
  is_featured TINYINT(1) DEFAULT 0,
  view_count INT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_artwork_artist FOREIGN KEY (artist_id) REFERENCES artists(artist_id) ON DELETE SET NULL,
  CONSTRAINT fk_artwork_admin FOREIGN KEY (created_by) REFERENCES admins(admin_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Artwork images
CREATE TABLE IF NOT EXISTS artwork_images (
  image_id INT AUTO_INCREMENT PRIMARY KEY,
  artwork_id INT NOT NULL,
  image_url VARCHAR(500),
  is_primary TINYINT(1) DEFAULT 0,
  CONSTRAINT fk_image_artwork FOREIGN KEY (artwork_id) REFERENCES artworks(artwork_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Artwork <-> Category (many-to-many)
CREATE TABLE IF NOT EXISTS artwork_categories (
  artwork_id INT NOT NULL,
  category_id INT NOT NULL,
  PRIMARY KEY (artwork_id, category_id),
  CONSTRAINT fk_ac_artwork FOREIGN KEY (artwork_id) REFERENCES artworks(artwork_id) ON DELETE CASCADE,
  CONSTRAINT fk_ac_category FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Events
CREATE TABLE IF NOT EXISTS events (
  event_id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  start_date DATETIME DEFAULT NULL,
  end_date DATETIME DEFAULT NULL,
  image_url VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Audit logs
CREATE TABLE IF NOT EXISTS audit_logs (
  log_id INT AUTO_INCREMENT PRIMARY KEY,
  admin_id INT DEFAULT NULL,
  action VARCHAR(255),
  target_table VARCHAR(255),
  target_id INT DEFAULT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_audit_admin FOREIGN KEY (admin_id) REFERENCES admins(admin_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Optional indexes
CREATE INDEX idx_artists_slug ON artists(slug);
CREATE INDEX idx_categories_slug ON categories(slug);
CREATE INDEX idx_artworks_slug ON artworks(slug);
CREATE INDEX idx_artworks_artist ON artworks(artist_id);
CREATE INDEX idx_artwork_images_artwork ON artwork_images(artwork_id);

-- Seed a default role (optional)
INSERT INTO roles (role_name)
SELECT 'ADMIN' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'ADMIN');

