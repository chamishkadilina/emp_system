# EM System – Employee Management

A Java Swing desktop app for managing employees, attendance and leave requests, backed by a MySQL database.

## Project layout

```
project suchira/
├── database/
│   └── empmanage.sql           Database schema and sample data (MySQL 8 and XAMPP/MariaDB)
├── design/                     Source artwork (PNG/PSD), not used by the app at runtime
├── installer/
│   ├── launch4j.xml            Wraps the jar into "EM System.exe" (Launch4j)
│   └── setup.iss               Windows installer script (Inno Setup)
└── EmpSystem/                  The application (also a NetBeans project)
    ├── build.sh / build.bat    Build without NetBeans → dist/EmpSystem.jar
    ├── lib/                    Third-party jars
    ├── nbproject/, build.xml   NetBeans project files
    └── src/empsystem/
        ├── Main.java           Entry point
        ├── db/                 Database connection and server address setting
        ├── dao/                All SQL, one class per area (admins, employees, attendance, leave)
        ├── model/              Plain data records (Employee, EmployeeLogin)
        ├── report/             PDF export
        ├── ui/                 Screens (Swing JFrames + NetBeans .form files)
        ├── util/               Validation, dialogs, image helpers
        └── resources/          Icons and images bundled into the jar
```

The screens in `ui/` were built with the NetBeans GUI Builder. Edit their layout in NetBeans:
the code between `GEN-BEGIN` and `GEN-END` is regenerated from the `.form` file.

## Requirements

- JDK 17 or newer
- MySQL 8 (or XAMPP's MariaDB) on port 3306, user `root` with an empty password

## Set up the database

```sh
mysql -u root -e "CREATE DATABASE empmanage;"
mysql -u root empmanage < database/empmanage.sql
```

With XAMPP you can instead create the `empmanage` database in phpMyAdmin and import `database/empmanage.sql`.

## Build and run

macOS / Linux:

```sh
cd EmpSystem
./build.sh
java -jar dist/EmpSystem.jar
```

Windows (Command Prompt):

```bat
cd EmpSystem
build.bat
java -jar dist\EmpSystem.jar
```

Or open the `EmpSystem` folder in NetBeans and press Run.

Default login: `krishan` / `123`. The admin-users screen (gear icon on the login screen) asks for the password `123`.

## Database server address

The app connects to `localhost` by default. To use MySQL on another computer, click the link icon on the
login screen, enter the host address, close the window and start the app again.

## Windows installer

1. Build the jar with `EmpSystem\build.bat`.
2. Open `installer\launch4j.xml` in [Launch4j](https://launch4j.sourceforge.net/) and build → `EmpSystem\dist\EM System.exe`.
3. Compile `installer\setup.iss` with [Inno Setup](https://jrsoftware.org/isinfo.php) → `installer\output\EMS Setup.exe`.

The installed app still needs Java 17+ and the MySQL database on the PC.
