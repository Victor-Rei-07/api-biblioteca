
CREATE TABLE authors (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    bio VARCHAR(256),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE books (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author_id  INT REFERENCES authors(id),
    isbn VARCHAR(50) UNIQUE,
    available BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW()
);



CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT NOW()
);



CREATE TABLE refresh_tokens (
    id SERIAL PRIMARY KEY,
    token VARCHAR(256) UNIQUE,
    user_id INT REFERENCES users(id),
    expiry_date TIMESTAMP 
);

CREATE TABLE loans (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    book_id INT REFERENCES books(id),
    loan_date TIMESTAMP DEFAULT NOW(),
    return_date TIMESTAMP NOT NULL,
    returned BOOLEAN DEFAULT false
);


CREATE INDEX idx_loans_pendentes ON loans(return_date) WHERE returned = false;

CREATE INDEX idx_books_author_id ON books(author_id);
CREATE INDEX idx_loans_user_id ON loans(user_id);

