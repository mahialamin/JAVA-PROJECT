-- Create Database (if not exists)
CREATE DATABASE IF NOT EXISTS cuconnect;
USE cuconnect;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('STUDENT', 'FACULTY') NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    -- Faculty-specific details are stored here directly to keep schema simple
    department VARCHAR(100) DEFAULT NULL,
    designation VARCHAR(100) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
);

-- 2. Sections Table
CREATE TABLE IF NOT EXISTS sections (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    department VARCHAR(100) NOT NULL
);

-- 3. Student Sections Table (Student-specific metadata & section assignment)
CREATE TABLE IF NOT EXISTS student_sections (
    user_id INT PRIMARY KEY,
    student_id VARCHAR(50) UNIQUE NOT NULL,
    department VARCHAR(100) NOT NULL,
    section_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE SET NULL
);

-- 4. Notices Table (with Pinned and Expiry options)
CREATE TABLE IF NOT EXISTS notices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    created_by INT NOT NULL,
    section_id INT DEFAULT NULL, -- NULL means general notice for all sections
    is_pinned BOOLEAN DEFAULT FALSE,
    expiry_date DATE DEFAULT NULL, -- NULL means notice never expires
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE CASCADE,
    INDEX idx_created_at (created_at),
    INDEX idx_expiry (expiry_date)
);

-- 5. Notice Reads Table (tracking read status per student)
CREATE TABLE IF NOT EXISTS notice_reads (
    user_id INT NOT NULL,
    notice_id INT NOT NULL,
    read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, notice_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (notice_id) REFERENCES notices(id) ON DELETE CASCADE
);

-- 6. Messages Table (group chat messages per section)
CREATE TABLE IF NOT EXISTS messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    section_id INT NOT NULL,
    content TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE CASCADE,
    INDEX idx_section_sent (section_id, sent_at)
);
