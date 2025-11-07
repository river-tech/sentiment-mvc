-- PostgreSQL schema for sentiment-mvc

CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY,
  full_name VARCHAR(100) NOT NULL,
  password_hash TEXT NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Future tables
-- CREATE TABLE jobs (...);
-- CREATE TABLE job_articles (...);


