DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS people;

CREATE TABLE people  (
  firstName VARCHAR(11) NOT NULL ,
  lastName VARCHAR(10) NOT NULL
);

CREATE TABLE customer  (
  id BIGINT  NOT NULL PRIMARY KEY ,
  firstName VARCHAR(50) NOT NULL ,
  middleInitial VARCHAR(1),
  lastName VARCHAR(50) NOT NULL,
  address VARCHAR(45) NOT NULL,
  city VARCHAR(16) NOT NULL,
  state CHAR(2) NOT NULL,
  zipCode CHAR(5)
);
