-- Seed data for Artists, Artworks, Events (for Landing Page API testing)

-- Insert sample artists
INSERT INTO artists (name, slug, birth_year, death_year, biography, nationality, image_url, created_at) VALUES
('Vincent Van Gogh', 'vincent-van-gogh', 1853, 1890, 'Dutch Post-Impressionist painter', 'Dutch', 'https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/Van_Gogh_Self_Portrait_%282%29.jpg', NOW()),
('Pablo Picasso', 'pablo-picasso', 1881, 1973, 'Spanish painter and sculptor', 'Spanish', 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/98/Pablo_picasso_1923.jpg', NOW()),
('Claude Monet', 'claude-monet', 1840, 1926, 'French Impressionist painter', 'French', 'https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Claude_Monet_1899_Nadar_crop.jpg', NOW());

-- Insert sample artworks (featured)
INSERT INTO artworks (type, artist_id, title, slug, year_created, description, price, is_published, is_featured, view_count, created_at, updated_at) VALUES
('painting', 1, 'Starry Night', 'starry-night', 1889, 'A swirling night sky over a village', 5000000, 1, 1, 1250, NOW(), NOW()),
('painting', 1, 'Sunflowers', 'sunflowers', 1888, 'A vibrant arrangement of sunflowers in a vase', 3500000, 1, 1, 980, NOW(), NOW()),
('painting', 2, 'Les Demoiselles d\'Avignon', 'les-demoiselles-davignon', 1907, 'A revolutionary painting that launched Cubism', 8000000, 1, 1, 2100, NOW(), NOW()),
('painting', 3, 'Water Lilies', 'water-lilies', 1906, 'Monet\'s iconic series of water lilies', 6500000, 1, 1, 1850, NOW(), NOW()),
('painting', 2, 'Guernica', 'guernica', 1937, 'Anti-war masterpiece', 9500000, 1, 1, 3200, NOW(), NOW());

-- Insert artwork images (primary images for featured artworks)
INSERT INTO artwork_images (artwork_id, image_url, is_primary) VALUES
(1, 'https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/Van_Gogh_-_Starry_Night.jpg', 1),
(2, 'https://upload.wikimedia.org/wikipedia/commons/thumb/e/ef/Van_Gogh_-_Sunflowers.jpg', 1),
(3, 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/4c/Les_Demoiselles_d%27Avignon.jpg', 1),
(4, 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/8e/Monet_Water_Lilies_1906.jpg', 1),
(5, 'https://upload.wikimedia.org/wikipedia/commons/thumb/7/74/Picasso_Guernica.jpg', 1);

-- Insert sample events (upcoming)
INSERT INTO events (title, description, start_date, end_date, image_url) VALUES
('Triển lãm Impressionism', 'Khám phá thế giới của các Impressionist vĩ đại', '2026-05-01 10:00:00', '2026-05-31 18:00:00', 'https://example.com/impressionism.jpg'),
('Van Gogh Exhibition 2026', 'Cuộc triển lãm đặc biệt về cuộc đời và tác phẩm của Van Gogh', '2026-06-15 09:00:00', '2026-07-30 17:00:00', 'https://example.com/van-gogh-2026.jpg'),
('Picasso Modern Art Showcase', 'Những tác phẩm hiện đại của Picasso', '2026-08-01 10:00:00', '2026-08-31 20:00:00', 'https://example.com/picasso-modern.jpg'),
('Watercolor Art Workshop', 'Workshop về kỹ thuật vẽ màu nước', '2026-07-10 14:00:00', '2026-07-10 17:00:00', 'https://example.com/watercolor-workshop.jpg');
