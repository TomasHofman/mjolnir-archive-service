ALTER TABLE REPOSITORY_FORKS ADD DELETED TIMESTAMP AFTER CREATED;
ALTER TABLE REPOSITORY_FORKS ADD STATUS VARCHAR(255);
