-- Add users table to match User entity
-- This table will be used for admin accounts registration

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  enabled BIT NOT NULL DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User roles junction table
CREATE TABLE IF NOT EXISTS user_roles (
  user_id INT NOT NULL,
  roles VARCHAR(50) NOT NULL,
  PRIMARY KEY (user_id, roles),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create first admin user (password: admin123)  
-- Note: Password will be encoded by Spring when first admin logs in
-- For now, we'll create without INSERT to avoid hash issues

-- Add indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_enabled ON users(enabled);