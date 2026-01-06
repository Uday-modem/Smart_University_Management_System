# Database Schema Fix Instructions

## Issues Fixed:

### 1. **Foreign Key Type Incompatibility**
   - **Problem**: Database tables had mismatched column types (INT vs BIGINT)
   - **Errors**:
     - `timetable.staff_id` incompatible with `staff.id`
     - `staff_expertise.staff_id` incompatible with `staff.id`
     - `attendance.staff_id` incompatible with `staff.id`

### 2. **Port 8080 Already in Use**
   - **Problem**: Another process is using port 8080
   - **Solution**: Stop the existing process

## Fixes Applied:

1. ✅ Added explicit `columnDefinition = "BIGINT"` to ensure consistent types
2. ✅ Changed `ddl-auto` from `update` to `create-drop` temporarily to recreate schema

## Steps to Fix:

### Step 1: Stop Process on Port 8080

**On macOS/Linux:**
```bash
# Find the process using port 8080
lsof -ti:8080

# Kill the process
kill -9 $(lsof -ti:8080)

# Or manually:
# 1. Find PID: lsof -ti:8080
# 2. Kill it: kill -9 <PID>
```

**On Windows:**
```cmd
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Step 2: Run the Application

```bash
cd rfid-backend
mvn spring-boot:run
```

**⚠️ IMPORTANT**: The first run will **DELETE ALL DATA** and recreate tables with correct schema.

### Step 3: After First Successful Run

Change `application.properties` back to preserve data:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Then restart the application.

## What Changed:

1. **Timetable.java**: Added `columnDefinition = "BIGINT"` to `staff_id`
2. **Attendance.java**: Added `columnDefinition = "BIGINT"` to `student_id` and `staff_id`
3. **application.properties**: Temporarily set `ddl-auto=create-drop`

## Why This Happened:

- Database tables were created with inconsistent types (some INT, some BIGINT)
- Hibernate tried to create foreign keys but types didn't match
- MySQL requires exact type matching for foreign keys

## After Fix:

- All foreign key columns will be BIGINT (matching `Staff.id`)
- Schema will be consistent
- Application will start successfully

