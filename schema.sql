CREATE TABLE loan (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_name VARCHAR(128) NOT NULL,
  principal DECIMAL(19,4) NOT NULL,
  annual_interest_rate DECIMAL(9,6) NOT NULL,
  tenor_months INT NOT NULL,
  monthly_emi DECIMAL(19,4) NOT NULL,
  start_date DATE NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

CREATE TABLE schedule_entry (
  id BIGINT NOT NULL AUTO_INCREMENT,
  loan_id BIGINT NOT NULL,
  installment_number INT NOT NULL,
  due_date DATE NOT NULL,
  opening_balance DECIMAL(19,4) NOT NULL,
  emi_amount DECIMAL(19,4) NOT NULL,
  principal_component DECIMAL(19,4) NOT NULL,
  interest_component DECIMAL(19,4) NOT NULL,
  closing_balance DECIMAL(19,4) NOT NULL,
  status VARCHAR(32) NOT NULL,
  paid_date DATE,
  PRIMARY KEY (id),
  CONSTRAINT fk_schedule_loan FOREIGN KEY (loan_id) REFERENCES loan(id)
);

CREATE TABLE loan_transaction (
  id BIGINT NOT NULL AUTO_INCREMENT,
  loan_id BIGINT NOT NULL,
  transaction_type VARCHAR(32) NOT NULL,
  strategy VARCHAR(32) NOT NULL,
  amount DECIMAL(19,4) NOT NULL,
  installment_number INT,
  transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  description VARCHAR(512),
  PRIMARY KEY (id),
  CONSTRAINT fk_transaction_loan FOREIGN KEY (loan_id) REFERENCES loan(id)
);
