# Real-Time Collaborative Task Management System

A modern, real-time collaborative task management system built with Java Servlets, JSP, JDBC, WebSockets, PHP, and MySQL.

## Features

- **Real-time Collaboration**: WebSocket-based live updates for task changes
- **User Authentication**: Secure login/register with session management
- **Password Reset**: PHP-based password reset with email tokens
- **Project Management**: Create and manage multiple projects
- **Task Management**: Kanban-style task board with drag-and-drop
- **Analytics Dashboard**: Real-time project statistics and insights
- **Modern UI**: Responsive, dark-themed interface

## Tech Stack

- **Backend**: Java Servlets, JSP, JDBC
- **Database**: MySQL 8.0+
- **Real-time**: WebSockets (Jakarta WebSocket API)
- **Password Reset**: PHP 7.4+
- **Frontend**: HTML5, CSS3, JavaScript (ES6+)
- **Build Tool**: Maven 3.6+
- **Connection Pool**: HikariCP
- **Security**: BCrypt password hashing

## Prerequisites

- Java 17+
- Maven 3.6+
- MySQL 8.0+
- PHP 7.4+ (for password reset)
- Tomcat 10+ or compatible servlet container

## Setup Instructions

### 1. Database Setup

```bash
# Create database and user
mysql -u root -p
CREATE DATABASE collabtask CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER 'collabtask'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON collabtask.* TO 'collabtask'@'localhost';
FLUSH PRIVILEGES;

# Import schema
mysql -u collabtask -p collabtask < db/schema.sql
```

### 2. Environment Configuration

Set the following environment variables:

```bash
export DB_URL="jdbc:mysql://localhost:3306/collabtask?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export DB_USER="collabtask"
export DB_PASS="your_password"

# For PHP password reset
export PHP_DB_DSN="mysql:host=127.0.0.1;dbname=collabtask;charset=utf8mb4"
export PHP_DB_USER="collabtask"
export PHP_DB_PASS="your_password"
```

### 3. Build and Deploy

```bash
# Build the project
mvn clean package

# Deploy to Tomcat
cp target/collabtask.war $TOMCAT_HOME/webapps/

# Start Tomcat
$TOMCAT_HOME/bin/startup.sh
```

### 4. PHP Password Reset Setup

```bash
# Make PHP scripts executable and set up web server
# Point your web server to the php/reset/ directory
# Example with Apache:
# DocumentRoot /path/to/Real-Time\ Collaborative\ Task\ Management\ System/php/reset/
```

## Project Structure

```
src/main/
├── java/com/collabtask/
│   ├── app/                 # Main application classes
│   │   ├── AppContextListener.java
│   │   ├── AuthServlet.java
│   │   ├── ApiServlet.java
│   │   └── DataSourceProvider.java
│   ├── dao/                 # Data Access Objects
│   │   ├── UserDao.java
│   │   ├── ProjectDao.java
│   │   └── TaskDao.java
│   ├── model/               # Data models
│   │   └── Models.java
│   └── websocket/           # WebSocket endpoints
│       └── TaskSocket.java
└── webapp/
    ├── WEB-INF/
    │   └── web.xml
    ├── assets/
    │   ├── css/style.css
    │   └── js/app.js
    ├── index.jsp            # Login/Register page
    ├── dashboard.jsp        # Main dashboard
    └── error.jsp           # Error page
db/
└── schema.sql              # Database schema
php/reset/                  # Password reset scripts
├── request_reset.php
└── reset_form.php
```

## API Endpoints

### Authentication
- `POST /login` - User login
- `POST /register` - User registration
- `GET /logout` - User logout

### Projects
- `GET /api/projects` - List user's projects
- `POST /api/projects` - Create new project

### Tasks
- `GET /api/tasks?projectId={id}` - List project tasks
- `POST /api/tasks?action=create` - Create new task
- `POST /api/tasks?action=update_status` - Update task status

### Analytics
- `GET /api/analytics?projectId={id}` - Get project statistics

### WebSocket
- `WS /ws/tasks/{projectId}` - Real-time task updates

## Usage

1. **Register/Login**: Create an account or sign in
2. **Create Project**: Start a new project from the dashboard
3. **Add Tasks**: Create tasks and assign them to team members
4. **Real-time Updates**: See changes instantly across all connected clients
5. **Analytics**: Monitor project progress and team performance

## Security Features

- BCrypt password hashing (cost factor 12)
- Session-based authentication
- SQL injection prevention with prepared statements
- CSRF protection via session validation
- Secure password reset with time-limited tokens

## Development

### Running in Development

```bash
# Start MySQL
brew services start mysql

# Set environment variables
export DB_URL="jdbc:mysql://localhost:3306/collabtask?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export DB_USER="root"
export DB_PASS=""

# Run with Maven Tomcat plugin
mvn tomcat7:run
```

### Database Migrations

The schema is provided in `db/schema.sql`. For production, consider using a proper migration tool.

## Troubleshooting

### Common Issues

1. **Database Connection**: Ensure MySQL is running and credentials are correct
2. **WebSocket Issues**: Check that your servlet container supports WebSockets
3. **PHP Reset**: Ensure PHP can connect to the same MySQL database
4. **Session Issues**: Check that cookies are enabled and sessions are working

### Logs

Check Tomcat logs in `$TOMCAT_HOME/logs/` for application errors.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License.
