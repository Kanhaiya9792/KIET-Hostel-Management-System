KIET HOSTEL MANAGEMENT SYSTEM (Java JDBC + MySQL)
=================================================

PROJECT STRUCTURE
-----------------
KIET_Hostel_Management_System/
|-- pom.xml
|-- database.sql
|-- README.txt
`-- src/
    `-- main/
        `-- java/
            `-- com/kiet/hostel/
                |-- DBConnection.java
                `-- HostelManagementSystem.java

REQUIREMENTS
------------
1. JDK 17 or later
2. MySQL Server
3. IntelliJ IDEA (or another Java IDE)
4. Maven support (MySQL JDBC driver is added through pom.xml)

SETUP
-----
1. Open MySQL Workbench or MySQL command line.
2. Run database.sql. It creates hostel_db and HostelStudent.
3. Open DBConnection.java.
4. Change USER and PASSWORD to match your MySQL credentials.
   Example: USER = "root"; PASSWORD = "your actual password";
5. Open the project folder in IntelliJ IDEA as a Maven project.
6. Wait for Maven to download mysql-connector-j.
7. Run HostelManagementSystem.java.

MENU OPERATIONS
---------------
1. Inserts three sample records using PreparedStatement.
   Duplicate primary keys are handled and skipped.
2. Accepts hostel name; displays matching records with fee > 50000,
   sorted by fee descending.
3. Updates fee for the entered student ID.
4. Finds a student, updates room number only if found, and displays
   the updated details.
5. Displays SUM(fee), then deletes the entered student only if fee > 0.
0. Exits the application.

SAMPLE HOSTEL NAMES
-------------------
Tagore Hostel
Raman Hostel

NOTE
----
Running option 1 multiple times will show duplicate-ID messages for
records already inserted. This is expected and prevents duplicate rows.
