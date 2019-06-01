INSERT INTO BOOKSTORE (NAME)
VALUES ('Bloomsbury'),
  ('The Strand'),
  ('City Lights Books');

INSERT INTO ADDRESS (STREET, CITY, COUNTRY, BOOKSTORE_ID)
VALUES ('Charing Cross Road', 'London', 'Great Britain', 1),
  ('828 Broadway', 'New York', 'USA', 2);

UPDATE BOOKSTORE
SET ADDRESS_ID = 1
WHERE ID = 1;

UPDATE BOOKSTORE
SET ADDRESS_ID = 2
WHERE ID = 2;

INSERT INTO BOOK (TITLE, PRICE, GENRE, BOOKSTORE_ID)
VALUES ('Harry Potter', 10, 'FANTASY', 1),
  ('Lord of the rings', 5, 'FANTASY', 1),
  ('Cassandra', 20, 'SCIENCE', 1),
  ('Effective Java', 20, 'SCIENCE', 2);

INSERT INTO AUTHOR (NAME)
VALUES ('J. K. Rowling'), 
('J. R. R. Tolkien'), 
('Carpenter Jeff'), 
('Hewitt Eben'),
('Joshua Bloch');

INSERT INTO BOOK_AUTHOR (BOOKS_ID, AUTHORS_ID)
VALUES (1,1), (2,2), (3,3), (3,4), (4,5);


INSERT INTO Announcement (a_id,title,dividend,createdOn) VALUES (1, 'CashDividend', 7,'2019-06-01T00:00:30+0');
INSERT INTO Announcement (a_id,title,dividend,createdOn) VALUES (2, 'CashDividend', 8,'2019-05-05T00:00:30+0');

INSERT INTO Entitlement (e_id,amount,announcementId) VALUES (22,14,1);
INSERT INTO Entitlement (e_id,amount,announcementId) VALUES (33,16,1);