The University Event Management System is a Java-based desktop application designed for efficient management of university events. The system supports event creation, participant registration, comprehensive reporting, and analytics, all while utilizing persistent storage. It was developed to address common university issues such as manual event scheduling, poor record keeping, event conflicts, and the lack of centralized data storage.

--------------------------------------------------------------------------------
Key Features
The system provides a robust set of features to handle event coordination:
1. Secure Authentication: Features a secure login system for users.
2. Event Management (CRUD): Allows for the creation, updating, and deletion of events.
3. Participant Registration: Enables the registration of participants (Students/Staff) for specific events.
4. Conflict Prevention: Includes logic to check for conflicting events based on date and venue.
5. Data Persistence & Backup: All data is persistently saved, and users can create backups of the database.
6. Reporting & Export: Users can view event summary reports and export event data to CSV format for external use.
7. Analytics Dashboard: Provides quick statistics like total events, total participants, and upcoming event counts.

--------------------------------------------------------------------------------
Technical Architecture
The system follows a layered approach using standard Java components:
Component
Description
Technologies
Frontend
The graphical user interface (GUI) component, built with a Material-style aesthetic.
Java Swing, JFrame, JDialog
Backend Logic
Manages the application state, business rules, and data flow.
DataManager (Singleton pattern)
Persistence Layer
Handles saving and loading application data to local files on disk.
DatabaseManager
Data Storage
Local directory created at runtime to store persistent data.
File-based storage (eventmanagement_data/)
Data Persistence Details
The system utilizes file-based persistence by automatically creating the eventmanagement_data directory in the project root upon initialization. All events and users are saved to local files (events.txt and users.txt). Data is automatically saved whenever an event or user record is modified, ensuring data is not lost when the application closes.

--------------------------------------------------------------------------------
Getting Started
Installation and Running
1. Ensure you have a Java environment set up.
2. Run the main entry point of the application, which is executed via EventManagementSystem.main.
Default Credentials
Upon initial launch (if the user file does not exist), the system loads default user accounts.
Role
Username
Password
Admin
admin
admin123
Coordinator
coordinator
coord123
Note: If the eventmanagement_data/users.txt file exists from a previous run, the credentials stored in that file will be used. If you have manually configured the application to use groupfive as a user, you may also log in with groupfive / BIT1201.# Event-Driven-Programming-Project-
