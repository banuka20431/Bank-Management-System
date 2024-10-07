CREATE DATABASE ABC_BANK;

USE ABC_BANK;

-- create department table

CREATE TABLE Department(
	depId VARCHAR(4),
	depName VARCHAR(20) UNIQUE,
	CONSTRAINT PK_Department PRIMARY KEY (depId)
);

-- create branch table 

CREATE TABLE Branch(
	branchId VARCHAR(10) NOT NULL,
	branchName VARCHAR(30) NOT NULL UNIQUE,
	CONSTRAINT PK_Branch PRIMARY KEY (branchId)
);

-- creating employee table

CREATE TABLE Employee(
	empId CHAR(15) NOT NULL,
	empTitle CHAR(4) NOT NULL,
	empName VARCHAR(30) NOT NULL,
	empUsername VARCHAR(10) NOT NULL UNIQUE,
	empPasswordHash VARCHAR(100) NOT NULL,
	depId VARCHAR(4) NOT NULL,
	branchId VARCHAR(10) NOT NULL,
	CONSTRAINT PK_Employee PRIMARY KEY (empId),
	CONSTRAINT FK_EmployeeDepartment FOREIGN KEY (depId) REFERENCES Department(depId)
		ON DELETE RESTRICT
		ON UPDATE  CASCADE,
	CONSTRAINT FK_EmployeeBranch FOREIGN KEY (branchId) REFERENCES Branch(branchId)
		ON DELETE RESTRICT
		ON UPDATE  CASCADE
);

-- create account type table

CREATE TABLE AccountType(
	accTypeId VARCHAR(15) NOT NULL,
	accTypeName VARCHAR(30) NOT NULL,
	accTypeInterestRate DECIMAL(2, 1) NOT NULL,
	accTypeMonthlyTransactionLimit INT(3) NOT NULL,
	CONSTRAINT PK_AccountType PRIMARY KEY (accTypeId)
);

-- adding data into account type table

INSERT INTO AccountType(accTypeId, accTypeName, accTypeInterestRate, accTypeMonthlyTransactionLimit)
VALUES("CHK_ACC", "Checking Account", 0.1, 99999);

INSERT INTO AccountType(accTypeId, accTypeName, accTypeInterestRate, accTypeMonthlyTransactionLimit)
VALUES("SVG_ACC", "Saving Account", 0.5, 100);

INSERT INTO AccountType(accTypeId, accTypeName, accTypeInterestRate, accTypeMonthlyTransactionLimit)
VALUES("IVM_ACC", "Investment Account", 1.5, 20);

INSERT INTO AccountType(accTypeId, accTypeName, accTypeInterestRate, accTypeMonthlyTransactionLimit)
VALUES("CD_ACC", "Certificated Deposit Account", 2.5, 6);


-- create account holder table

CREATE TABLE Holder(
	holderAccNumber VARCHAR(20) NOT NULL,
	holderAccTypeId VARCHAR(20) NOT NULL,
	holderAccBalance DECIMAL(30, 2) NOT NULL DEFAULT 0.00,
	holderPinCodeHash VARCHAR(100) NOT NULL,
	holderFullName VARCHAR(30) NOT NULL,
	holderName VARCHAR(20) NOT NULL,
	holderTitle VARCHAR(5) NOT NULL,
	holderProfession VARCHAR(15) NOT NULL,
	holderMobileNumber CHAR(10) NOT NULL,
	holderTelephoneNumber CHAR(10) NOT NULL,
	holderAddress VARCHAR(50) NOT NULL,
	holderPostalCode VARCHAR(11) NOT NULL,
	holderEmail VARCHAR(30) NOT NULL,
	holderNIC CHAR(12) NOT NULL UNIQUE,
	holderDOB DATE NOT NULL,
	onlineBanking BOOLEAN NOT NULL DEFAULT FALSE,
	debitCard BOOL NOT NULL DEFAULT TRUE,
	smsAlerts BOOL NOT NULL DEFAULT TRUE,
	monthlyTransactionCount INT(10) NOT NULL DEFAULT 0,
	status BOOL NULL DEFAULT TRUE,
	CONSTRAINT PK_Holder PRIMARY KEY (holderAccNumber),
	CONSTRAINT FK_Holder FOREIGN KEY (holderAccTypeId) REFERENCES AccountType(accTypeId)
		ON DELETE RESTRICT
		ON UPDATE CASCADE
);


-- creating loan type table

CREATE TABLE LoanType(
    loanTypeDefId VARCHAR(10) NOT NULL,
    loanTypeName VARCHAR(50) NOT NULL,
    loanTypeDefInterestRate INT(3) NOT NULL,
    loanTypeDefAllowedTimePeriod INT(10) NOT NULL,
    loanTypeMinimumAmount DECIMAL(10, 2) NOT NULL,
    CONSTRAINT PK_LoanType PRIMARY KEY(loanTypeDefId)
);

-- adding data into loan type table

INSERT INTO LoanType(loanTypeDefId, loanTypeName, loanTypeDefInterestRate, loanTypeDefAllowedTimePeriod, loanTypeMinimumAmount)
VALUES("DEF-STU-LN", "student loan", 3, 240, 50000.00);

INSERT INTO LoanType(loanTypeDefId, loanTypeName, loanTypeDefInterestRate, loanTypeDefAllowedTimePeriod, loanTypeMinimumAmount)
VALUES("DEF-IVM-LN", "student loan", 12, 60, 500000.00);

INSERT INTO LoanType(loanTypeDefId, loanTypeName, loanTypeDefInterestRate, loanTypeDefAllowedTimePeriod, loanTypeMinimumAmount)
VALUES("DEF-HS-LN", "student loan", 15, 120, 1500000.00);

INSERT INTO LoanType(loanTypeDefId, loanTypeName, loanTypeDefInterestRate, loanTypeDefAllowedTimePeriod, loanTypeMinimumAmount)
VALUES("DEF-VH-LN", "student loan", 16, 120, 2000000.00);

-- creating loan table

CREATE TABLE Loan(
    loanId VARCHAR(10) NOT NULL,
    loanTypeId VARCHAR(50) NOT NULL,
    loanAmount DECIMAL(12,2) NOT NULL,
    loanInterestRate INT(3) NOT NULL,
    loanMonthlyInstalmentAmount DECIMAL(12,2) NOT NULL,
	loanMonthlyInterestAmount DECIMAL(12,2) NOT NULL,
    loanAllowedTimePeriod INT(10) NOT NULL,
    loanDebtorAccNumber VARCHAR(20) NOT NULL,
    loanGainedInterestAmount DECIMAL(12,2) NOT NULL,
    loanClaimedDate DATE NOT NULL,
    loanOverDraft DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT PK_Loan PRIMARY KEY (loanId),
    CONSTRAINT FK_Loan FOREIGN KEY (loanTypeId) REFERENCES LoanType(loanTypeDefId),
        ON DELETE RESTRICT
    	ON UPDATE CASCADE
);