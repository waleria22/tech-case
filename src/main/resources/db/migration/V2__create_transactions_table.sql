CREATE TABLE transactions (
                              id BIGSERIAL PRIMARY KEY,
                              account_id BIGINT NOT NULL,
                              operation_type_id BIGINT NOT NULL,
                              amount NUMERIC(19, 2) NOT NULL,
                              balance NUMERIC(19, 2) NOT NULL,
                              event_date TIMESTAMP NOT NULL,
                              CONSTRAINT fk_transactions_account
                                  FOREIGN KEY (account_id)
                                      REFERENCES accounts (account_id)
);