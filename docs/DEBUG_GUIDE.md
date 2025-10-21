# Docker Debug Mode Guide
## Running Payroll Management System in Debug Mode

---

## 🐛 **Quick Start Debug Mode**

### **Windows**
```cmd
# Navigate to project directory
cd "d:\SP\job\PayrollManagementSystem\payroll_service"

# Start in debug mode
start-debug.bat
```

### **Linux/Mac**
```bash
# Navigate to project directory
cd /path/to/PayrollManagementSystem/payroll_service

# Make script executable
chmod +x start-debug.sh

# Start in debug mode
./start-debug.sh
```

### **Manual Docker Command**
```bash
docker-compose -f docker-compose.yml -f docker-compose.debug.yml up --build
```

---

## 🔧 **Debug Configuration**

### **Application Ports**
- **Application**: `http://localhost:20001`
- **Remote Debug**: `localhost:5005`
- **PostgreSQL**: `localhost:5432`
- **PgAdmin**: `http://localhost:5050`

### **Debug Features Enabled**
- ✅ **Remote Debugging** on port 5005
- ✅ **SQL Logging** with formatted output
- ✅ **Transaction Debugging** 
- ✅ **Security Debug** logging
- ✅ **Hibernate Statistics**
- ✅ **All Actuator Endpoints** exposed
- ✅ **Enhanced Error Details**
- ✅ **Log Files** mounted to `./logs/`

---

## 🎯 **IDE Remote Debug Setup**

### **IntelliJ IDEA**
1. Go to **Run → Edit Configurations**
2. Click **+** → **Remote JVM Debug**
3. Set **Host**: `localhost`
4. Set **Port**: `5005`
5. Set **Debugger mode**: `Attach to remote JVM`
6. Click **OK** and **Debug**

### **VS Code**
Create `.vscode/launch.json`:
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Remote Debug Payroll",
      "request": "attach",
      "hostName": "localhost",
      "port": 5005
    }
  ]
}
```

### **Eclipse**
1. **Run → Debug Configurations**
2. **Remote Java Application → New**
3. Set **Host**: `localhost`
4. Set **Port**: `5005`
5. Click **Debug**

---

## 📊 **Debug Monitoring**

### **Application Health**
```bash
curl http://localhost:20001/pms/api/v1/actuator/health
```

### **Database Connection**
```bash
curl http://localhost:20001/pms/api/v1/actuator/health/db
```

### **Configuration Properties**
```bash
curl http://localhost:20001/pms/api/v1/actuator/configprops
```

### **Environment Info**
```bash
curl http://localhost:20001/pms/api/v1/actuator/env
```

### **Metrics**
```bash
curl http://localhost:20001/pms/api/v1/actuator/metrics
```

---

## 📁 **Debug Files & Logs**

### **Log Files Location**
```
payroll_service/
├── logs/
│   └── payroll-debug.log    # Application logs
├── debug/
│   └── heapdump.hprof      # Heap dumps (if OOM occurs)
```

### **Real-time Log Monitoring**
```bash
# Windows
Get-Content -Path "logs\payroll-debug.log" -Wait

# Linux/Mac  
tail -f logs/payroll-debug.log
```

---

## 🔍 **Common Debug Scenarios**

### **1. Authentication Issues**
**Debug Steps:**
```bash
# Check user table
docker-compose exec postgres psql -U payroll_user -d payroll_db -c "SELECT username, role FROM users;"

# Check security logs
grep "authentication" logs/payroll-debug.log

# Test login endpoint
curl -X POST http://localhost:20001/pms/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### **2. Database Connection Issues**
**Debug Steps:**
```bash
# Check PostgreSQL connection
docker-compose exec postgres pg_isready -U payroll_user -d payroll_db

# Check Liquibase migration status
curl http://localhost:20001/pms/api/v1/actuator/liquibase

# View database logs
docker-compose logs postgres
```

### **3. API Endpoint Issues**
**Debug Steps:**
```bash
# Check all available endpoints
curl http://localhost:20001/pms/api/v1/actuator/mappings

# Test specific endpoint with debug
curl -v http://localhost:20001/pms/api/v1/employees

# Check application logs for errors
grep "ERROR" logs/payroll-debug.log
```

---

## 🚨 **Troubleshooting**

### **Debug Port Already in Use**
```bash
# Find process using port 5005
netstat -ano | findstr :5005

# Kill process (Windows)
taskkill /PID <process_id> /F

# Kill process (Linux/Mac)
kill -9 $(lsof -t -i:5005)
```

### **Container Won't Start**
```bash
# Check container logs
docker-compose logs payroll-service

# Rebuild from scratch
docker-compose down --volumes
docker-compose build --no-cache
docker-compose -f docker-compose.yml -f docker-compose.debug.yml up
```

### **Memory Issues**
```bash
# Check container memory usage
docker stats payroll-backend

# Increase memory in docker-compose.debug.yml
# Modify JAVA_TOOL_OPTIONS: -Xmx2048m
```

---

## 📝 **Debug Environment Variables**

```yaml
# Key debug environment variables
SPRING_PROFILES_ACTIVE: docker,debug
JAVA_TOOL_OPTIONS: "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
LOGGING_LEVEL_ORG_SP_PAYROLL_SERVICE: DEBUG
SPRING_JPA_SHOW_SQL: "true"
```

---

## 🎯 **Debug Best Practices**

1. **Set Breakpoints** in authentication flow first
2. **Monitor SQL queries** during API calls
3. **Check security logs** for authorization issues
4. **Use Actuator endpoints** for runtime information
5. **Test with Postman/cURL** for API validation
6. **Monitor container logs** for startup issues

---

**Debug Mode Ready!** 🚀  
Start debugging your authentication issues by connecting your IDE to port 5005 and setting breakpoints in the AuthController and SecurityConfig!