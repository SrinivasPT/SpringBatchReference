import pyodbc
from faker import Faker
import random

fake = Faker()

# Connection parameters
server = 'localhost'
database = 'EmployeeDB'
username = 'sa'
password = 'OkatiRendu@12'
driver = '{ODBC Driver 17 for SQL Server}'

cnxn = pyodbc.connect(f'DRIVER={driver};SERVER={server};DATABASE={database};UID={username};PWD={password}', autocommit=False)
cursor = cnxn.cursor()

# Determine the next starting ID
cursor.execute("SELECT MAX(Id) FROM Employee")
max_id_result = cursor.fetchone()
starting_id = max_id_result[0] + 1 if max_id_result[0] is not None else 1

num_employees = 100000  # Number of employees to insert
batch_size = 100  # Optimal batch size for bulk insert

# SQL insert statements
employee_insert_sql = "INSERT INTO Employee (Id, FirstName, LastName, Age, DateOfBirth) VALUES (?, ?, ?, ?, ?)"
address_insert_sql = "INSERT INTO Address (TypeCode, LineOne, LineTwo, Zip, EmployeeId) VALUES (?, ?, ?, ?, ?)"

# Prepare data for bulk insert
employee_records = []
address_records = []

cursor.execute("SET IDENTITY_INSERT Employee ON")

for i in range(num_employees):
    id = starting_id + i
    first_name = fake.first_name()
    last_name = fake.last_name()
    age = random.randint(18, 70)
    dob = fake.date_of_birth(minimum_age=age, maximum_age=age).strftime("%Y-%m-%d")

    # Prepare Employee record
    employee_records.append((id, first_name, last_name, age, dob))

    # Every employee has both a current and permanent address
    # Current Address
    address_records.append(("CURRENT", fake.street_address(), fake.secondary_address(), fake.zipcode(), id))
    # Permanent Address
    address_records.append(("PERMANENT", fake.street_address(), fake.secondary_address(), fake.zipcode(), id))

    # Execute batch insert when batch size is reached
    if (i + 1) % batch_size == 0 or i == num_employees - 1:
        cursor.fast_executemany = True
        cursor.executemany(employee_insert_sql, employee_records)
        cursor.executemany(address_insert_sql, address_records)
        cnxn.commit()  # Commit after each batch

        # Clear the lists for the next batch
        employee_records = []
        address_records = []

cursor.execute("SET IDENTITY_INSERT Employee OFF")

cursor.close()
cnxn.close()

print(f"Inserted {num_employees} employees and their addresses.")
