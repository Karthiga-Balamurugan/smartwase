SMARTWASTE - UPDATED COLLEGE MINI PROJECT
========================================

Technology:
- Java Swing UI
- Java OOP and Data Structures (HashMap, ArrayList, PriorityQueue, Graph, Dijkstra)
- SQLite database

Run:
1. Keep the lib folder and sqlite-jdbc-3.53.4.0.jar inside this project folder.
2. Double-click run.bat, or open Command Prompt in this folder and run run.bat.
3. First run automatically creates smartwaste.db and required tables.

Default login:
Admin:  username admin  | password admin123
Worker: username worker | password worker123

Demo Dustbin IDs:
D001 - Central Market
D002 - Bus Stand
D003 - College Road
D004 - Hospital
D005 - Residential Area
D006 - Public Park

Demo vehicles are created automatically (V001, V002, V003).

Implemented flow:
1. Worker enters only Dustbin ID and Fill Level.
2. The system validates the exact ID and fetches its saved location. Unknown ID is rejected.
3. Duplicate active reports for the same dustbin are blocked.
4. Admin reviews pending reports. Priority score and distance are displayed; a PriorityQueue orders reports using a combined score.
5. Admin clicks Assign. The system automatically selects a worker with no active assignment and an available vehicle, and calculates the shortest route from Central Market using Dijkstra.
6. Worker opens My Collection Tasks, then uses Update Status: Mark as In Progress, and after collecting the waste, Mark as Collected.
7. On completion, report and assignment status are saved as COMPLETED, the dustbin fill level is reset to 0, the vehicle becomes available, and history is recorded.

Important limitations:
- This is a Java desktop college prototype. Updates are saved immediately in the local database. True multi-device cloud real-time access needs a shared server/database and network deployment.
- Without sensors or camera input, the software cannot automatically measure the real physical fill level. In this prototype the worker enters the observed fill level.
- Road distances are sample values in the Java graph. Dijkstra finds shortest distance, not traffic-aware fastest travel time.
- Vehicle selection currently chooses an available vehicle with the largest listed capacity because the prototype does not estimate actual waste weight.

Before replacing your current project, keep a backup of your old smartwaste.db if you want to preserve old accounts and records. This package intentionally does not include the database, so it can create a clean database on first run.

NEW UPDATES (30-10-2026)
1. Dustbin master list now seeds 20 bins: D001 to D020. The 20 bins use the six supported road-network locations so shortest-route calculation continues to work.
2. Admin Dashboard now displays total dustbins, pending reports, active assignments, completed collections, and overflow alerts.
3. Added Waste Collection Analytics page with fill-level category counts and Refresh option.
4. Added Assigned Collections page. Admin can view assignment ID, dustbin, worker name, worker username, vehicle, route, distance, status and assigned time.
5. After automatic assignment, a confirmation popup displays the selected worker name, username, vehicle ID, route and distance.

NOTE: New dustbins are inserted using INSERT OR IGNORE when the app starts. If an old database already exists, D007-D020 are added without deleting existing users, reports, assignments or history. The app remains Java Swing + SQLite and is a local desktop application; online/cloud report storage is a separate future integration.

LATEST UPDATE - UNIQUE DUSTBIN LOCATIONS
----------------------------------------
- D001 to D020 now have 20 different physical locations.
- Existing dustbin IDs are retained. The location names are corrected during startup.
- SQLite creates a unique index on LOWER(TRIM(location)); duplicate locations are blocked even if case or extra spaces differ.
- Road network entries are added for all 20 locations so route calculation can work for each dustbin.
- Road distances are sample/demo values for the college project, not live map distances.
