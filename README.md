# Greek University Entry Calculator (Panhellenic Exams 2024)

> **Academic Project:** This application was developed as part of the undergraduate **"Software Engineering"** course at the **Department of Applied Informatics (Computer Science and Technology)**, University of Macedonia.

A **Java desktop application** designed specifically around the **2024** admission data for Greek Higher Education Institutions. This tool empowers Panhellenic exam candidates to accurately calculate their points based on department-specific coefficients, compare their performance against the 2024 admission thresholds, and explore university statistics.

## 🎯 Core Features

* **Exclusive 2024 Data Integration:** All calculations, lists, and thresholds are strictly based on the 2024 Greek University admission bases and Minimum Admission Base (EBE) regulations.
* **Smart Performance Calculator:** Users input their grades for their specific scientific field. The app dynamically calculates the total points for every applicable department and displays the exact mathematical difference (+/-) between the user's score and the 2024 base/EBE.
* **Comprehensive Statistics:** Rich analytical dashboards that break down the 2024 data. Includes:
  * Box plots showing point distributions across all 4 scientific fields.
  * Scatter plots correlating admission points with EBE.
  * Histograms and pie charts displaying the distribution of universities by city and department volume.
* **Advanced Sorting & Favorites:** Users can explore departments by applying filters (e.g. sorting by descending admission points, city, or university) and save their top choices to a personalized "Favorites" list.
* **Secure User Accounts:** Features a login and registration system with built-in password hashing to ensure user data remains secure.

## 🛠️ Technologies Used

* **Programming Language:** Java 
* **User Interface:** Java Swing
* **Database Management:** MySQL (integrated via JDBC)
* **Statistics & Charts:** JFreeChart library
* **Dependency Management:** Maven

## 📄 Academic Documentation & Software Engineering Lifecycle

In addition to the source code, this repository includes the final project report **(in Greek)** which documents the software engineering methodologies applied throughout the development process. Highlights include:

* **Analysis Phase:** Requirement gathering and prioritization using the **MoSCoW method**, project scheduling via a Gantt chart (Waterfall model), and comprehensive Use Case diagrams and specifications.
* **Design Phase:** Structural and behavioral UML modeling, including Class diagrams and Sequence diagrams designed in Visual Paradigm.
* **UI/UX Prototyping:** Creation of interactive wireframes and mockups using Figma prior to the Java Swing implementation.
* **Testing & Quality Assurance:** Documentation of specific test cases (e.g., login validation, error handling) and evaluation of the codebase using Object-Oriented Software Quality Metrics (such as LoC, CBO, and LOCOM1).

## ⚙️ Setup Instructions

To run this application locally, you will need a running MySQL server and a Java IDE with Maven support.

### 1. Database Setup
* Ensure your MySQL server is running (e.g. via XAMPP, WAMP, or MySQL Workbench).
* Locate the SQL script inside the `SQL_dump` folder of this repository.
* Execute the script in your preferred MySQL client. This will automatically create the required database schema and populate it with the 2024 university data.

### 2. Application Setup
* **Clone the repository** to your local machine.
* **Import the project** into your Java IDE (Eclipse, IntelliJ, etc.) as an existing **Maven project**. Allow your IDE a few moments to resolve and download the required dependencies.
* **Configure the connection:** Open the `DBConnection.java` file in the source code. Locate the database credentials (username and password variables) and update them to match your local MySQL configuration. *(Note: If using XAMPP, the default password is usually left empty `""`)*.
* **Launch:** Run the `Main.java` file to start the application.

### Troubleshooting
* **Path Issues:** Ensure the folder path where you cloned the repository does not contain Greek or other non-Latin characters, as this can sometimes cause IDE build errors.
* **Missing Dependencies:** If the project shows errors upon importing, try forcing a Maven update (e.g. right-click project -> `Maven -> Update Project` in Eclipse) to ensure all libraries are correctly downloaded.

## 🔑 Default Test Credentials
You can easily explore the application using the pre-configured test account:
* **Email:** `user@example.com`
* **Password:** `test`

## 🖼️ Application Showcase

Here is a visual walkthrough of the application's interface and features:

### Authentication & Navigation
**Login / Registration Screen:** Secure entry point with password hashing.
<img width="686" height="393" alt="app (1)" src="https://github.com/user-attachments/assets/6ae6568a-8126-4cfc-972a-e5a4233a9baf" />

**Main Dashboard:** Access to the 2024 bases, point calculators for all 4 fields, favorites, and overall statistics.
<img width="686" height="393" alt="app (2)" src="https://github.com/user-attachments/assets/d642a835-9b0b-4aee-bb5c-bde1f23c82e5" />

### 2024 Bases & Filtering
**Bases Directory:** Browsing the 2024 humanities departments.
<img width="986" height="593" alt="app (3)" src="https://github.com/user-attachments/assets/1c2f745d-3ff6-43d8-96ea-8d4948dcb117" />

**Smart Sorting:** Applying custom filters (e.g., descending base points) to easily find target schools.
<img width="986" height="593" alt="app (4)" src="https://github.com/user-attachments/assets/af21e263-91fe-4fad-87b5-1f150e38e5a0" />

### Grade Calculation
**Points Input:** Entering grades (0-20) for specific subjects.
<img width="686" height="393" alt="app (7)" src="https://github.com/user-attachments/assets/210209dc-f082-4532-b4ea-3ad067c03d6d" />

**Results & Comparisons:** Viewing the calculated points and the exact difference from the 2024 Base and EBE for each department.
<img width="1106" height="588" alt="app (8)" src="https://github.com/user-attachments/assets/dfcc4bb8-0463-470a-a2b1-24a74879aa1f" />

**Personalized Favorites:** A dedicated screen to manage saved departments.
<img width="986" height="593" alt="app (6)" src="https://github.com/user-attachments/assets/0a47b21b-8936-4d26-b9d7-20e4d3c75cdb" />

### Statistics Dashboards
**Field-Specific Statistics:** Detailed charts for a single scientific field, including coefficient averages and EBE scatter plots.
<img width="1586" height="943" alt="app (5)" src="https://github.com/user-attachments/assets/8bd24002-208a-4b9e-92fc-9411172b5612" />

**Overall 2024 Statistics:** Macro-level view of all 4 fields using box plots, and the distribution of departments across Greek cities.
<img width="1386" height="893" alt="app (9)" src="https://github.com/user-attachments/assets/05012721-0a0c-4e7c-85b0-59ca0417b26e" />
