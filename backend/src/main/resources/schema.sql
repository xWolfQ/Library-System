-- === BOOKS ===
CREATE TABLE books (
                       id NUMBER PRIMARY KEY,
                       title VARCHAR2(255) NOT NULL,
                       author VARCHAR2(255) NOT NULL
);

CREATE SEQUENCE books_seq START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE TRIGGER books_bi
BEFORE INSERT ON books
FOR EACH ROW
WHEN (new.id IS NULL)
BEGIN
SELECT books_seq.NEXTVAL INTO :new.id FROM dual;
END;
/

-- === READERS ===
CREATE TABLE readers (
                         id NUMBER PRIMARY KEY,
                         first_name VARCHAR2(100) NOT NULL,
                         last_name VARCHAR2(100) NOT NULL,
                         email VARCHAR2(255)
);

CREATE SEQUENCE readers_seq START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE TRIGGER readers_bi
BEFORE INSERT ON readers
FOR EACH ROW
WHEN (new.id IS NULL)
BEGIN
SELECT readers_seq.NEXTVAL INTO :new.id FROM dual;
END;
/

-- === LOANS ===
CREATE TABLE loans (
                       id NUMBER PRIMARY KEY,
                       book_id NUMBER NOT NULL,
                       reader_id NUMBER NOT NULL,
                       loan_date DATE DEFAULT SYSDATE,
                       return_date DATE,
                       CONSTRAINT fk_loans_book FOREIGN KEY (book_id) REFERENCES books(id),
                       CONSTRAINT fk_loans_reader FOREIGN KEY (reader_id) REFERENCES readers(id)
);

CREATE SEQUENCE loans_seq START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE TRIGGER loans_bi
BEFORE INSERT ON loans
FOR EACH ROW
WHEN (new.id IS NULL)
BEGIN
SELECT loans_seq.NEXTVAL INTO :new.id FROM dual;
END;
/
