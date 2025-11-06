# Installation Guide

## Prerequisites

- Java 17 or higher
- Apache Maven 3.6 or higher
- Apache Tomcat 10
- MySQL 8.0 or compatible database

## Step 1: Database Setup

1. Create MySQL database:
```sql
CREATE DATABASE sentiment_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Create tables (TODO: Add SQL schema scripts)

## Step 2: Configure Application

1. Edit `src/main/resources/application.properties`
2. Update database connection settings:
   - `db.url`: Database connection URL
   - `db.username`: Database username
   - `db.password`: Database password

## Step 3: Build Project

```bash
cd sentiment-mvc
mvn clean package
```

This will create `target/sentiment-mvc.war`

## Step 4: Deploy to Tomcat

1. Copy WAR file to Tomcat webapps directory:
```bash
cp target/sentiment-mvc.war $CATALINA_HOME/webapps/
```

2. Start Tomcat:
```bash
$CATALINA_HOME/bin/startup.sh
```

3. Access application:
```
http://localhost:8080/sentiment-mvc/
```

## Troubleshooting

- Check Tomcat logs: `$CATALINA_HOME/logs/catalina.out`
- Verify database connection settings
- Ensure Java 17 is installed and configured
- Check port 8080 is not in use

## TODO

- Add database schema SQL scripts
- Add migration scripts
- Add environment-specific configuration

