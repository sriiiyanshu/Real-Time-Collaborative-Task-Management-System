-- Task Manager Database Schema
-- Last updated: 2025-04-16

-- Users Table
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    reset_token VARCHAR(100),
    reset_token_expiry TIMESTAMP
);

-- Projects Table
CREATE TABLE projects (
    project_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP,
    owner_id INT,
    status VARCHAR(20) DEFAULT 'Active',
    FOREIGN KEY (owner_id) REFERENCES users(user_id)
);

-- Tasks Table
CREATE TABLE tasks (
    task_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP,
    completion_date TIMESTAMP,
    project_id INT,
    assignee_id INT,
    creator_id INT,
    priority VARCHAR(20),
    status VARCHAR(20),
    FOREIGN KEY (project_id) REFERENCES projects(project_id),
    FOREIGN KEY (assignee_id) REFERENCES users(user_id),
    FOREIGN KEY (creator_id) REFERENCES users(user_id)
);

-- Comments Table
CREATE TABLE comments (
    comment_id INT AUTO_INCREMENT PRIMARY KEY,
    task_id INT,
    user_id INT,
    content TEXT NOT NULL,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(task_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Files Table
CREATE TABLE files (
    file_id INT AUTO_INCREMENT PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size BIGINT,
    file_type VARCHAR(100),
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    uploader_id INT,
    task_id INT,
    project_id INT,
    FOREIGN KEY (uploader_id) REFERENCES users(user_id),
    FOREIGN KEY (task_id) REFERENCES tasks(task_id),
    FOREIGN KEY (project_id) REFERENCES projects(project_id)
);

-- Teams Table
CREATE TABLE teams (
    team_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    creator_id INT,
    FOREIGN KEY (creator_id) REFERENCES users(user_id)
);

-- Team Members Table
CREATE TABLE team_members (
    team_id INT,
    user_id INT,
    role VARCHAR(50) NOT NULL,
    joined_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (team_id, user_id),
    FOREIGN KEY (team_id) REFERENCES teams(team_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Team Projects Table
CREATE TABLE team_projects (
    team_id INT,
    project_id INT,
    added_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (team_id, project_id),
    FOREIGN KEY (team_id) REFERENCES teams(team_id),
    FOREIGN KEY (project_id) REFERENCES projects(project_id)
);

-- Project Members Table
CREATE TABLE project_members (
    project_id INT,
    user_id INT,
    role VARCHAR(50),
    PRIMARY KEY (project_id, user_id),
    FOREIGN KEY (project_id) REFERENCES projects(project_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Notifications Table
CREATE TABLE notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    message TEXT NOT NULL,
    type VARCHAR(50),
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    user_id INT,
    link VARCHAR(255),
    related_id INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Task Tags Table
CREATE TABLE task_tags (
    task_id INT,
    tag VARCHAR(50),
    PRIMARY KEY (task_id, tag),
    FOREIGN KEY (task_id) REFERENCES tasks(task_id)
);

-- Task Attachments Table
CREATE TABLE task_attachments (
    task_id INT,
    file_id INT,
    PRIMARY KEY (task_id, file_id),
    FOREIGN KEY (task_id) REFERENCES tasks(task_id),
    FOREIGN KEY (file_id) REFERENCES files(file_id)
);