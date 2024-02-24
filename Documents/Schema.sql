CREATE DATABASE EmployeeDB;
GO

USE EmployeeDB
GO

DROP TABLE Address;
DROP TABLE Employee;

CREATE TABLE Employee (
    Id              INT PRIMARY KEY IDENTITY,
    FirstName       VARCHAR(50),
    LastName        VARCHAR(50),
    Age             INT,
    DateOfBirth     DATETIME,
    JsonString      VARCHAR(4000)
);

CREATE TABLE Address (
    Id              INT PRIMARY KEY IDENTITY,
    TypeCode VARCHAR(50),
    LineOne         VARCHAR(50),
    LineTwo         VARCHAR(50),
    Zip             INT,
    EmployeeId      INT FOREIGN KEY REFERENCES Employee(Id)
);

INSERT INTO Employee (FirstName, LastName, Age, DateOfBirth) VALUES 
('John', 'Doe', 50, '1/13/1974'),
('Jane', 'Doe', 49, '1/14/1975'),
('Smith', 'Doe', 48, '1/15/1976');

INSERT INTO Address (TypeCode, LineOne, LineTwo, Zip, EmployeeId) VALUES
    ('CURRENT', 'One AD 1 Line One', 'TG', 12340, 1),
    ('PERMANENT','One AD 2 Line One', 'TG', 12340, 1),
    ('CURRENT','Second AD 1 Line One', 'TG', 123325, 2),
    ('PERMANENT','Third AD 2 Line One', 'TG', 132236, 3),
    ('CURRENT','Third AD 2 Line One', 'TG', 12323236, 3);;

GO

CREATE OR ALTER VIEW VW_EmployeeRecord 
AS 
SELECT 
    Employee.Id, FirstName firstName, LastName lastName, Age age, DateOfBirth dateOfBirth, 
    Cur.TypeCode curTypeCode, Cur.LineOne curLineOne, Cur.LineTwo curLineTwo, Cur.Zip curZip,  
    Per.TypeCode perTypeCode, Per.LineOne perLineOne, Per.LineTwo perLineTwo, Per.Zip perZip
FROM Employee
    LEFT JOIN Address AS Cur ON Employee.Id = Cur.EmployeeId AND Cur.TypeCode = 'CURRENT'
    LEFT JOIN Address AS Per ON Employee.Id = Per.EmployeeId AND Per.TypeCode = 'PERMANENT';

GO

SELECT COUNT(*) FROM dbo.VW_EmployeeRecord
SELECT * FROM dbo.VW_EmployeeRecord
SELECT * FROM VW_EmployeeRecord

-- ALTER TABLE Employee ADD JsonString VARCHAR(4000);

SELECT JOB_INSTANCE_ID, JOB_NAME from BATCH_JOB_INSTANCE
SELECT * FROM Employee ORDER BY id DESC -- 42326
SELECT * FROM Address WHERE EmployeeId = 45325