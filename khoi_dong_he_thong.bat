@echo off
title Than Nong AI - He Thong Ket Noi Cap
echo --- DANG KHOI DONG KET NOI VA SQL BACKEND (PORT 8001) ---
echo.

echo [1/3] Dang lay dia chi IP may tinh...
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr "IPv4"') do (
    set "ip=%%a"
    goto :show_ip
)

:show_ip
echo IP CUA BAN LA:%ip%
echo.

echo [2/3] Dang thiet lap ket noi cap USB cho ca 2 cong...
echo (Port 8000 cho AI va Port 8001 cho SQL)
adb reverse tcp:8000 tcp:8000
adb reverse tcp:8001 tcp:8001

echo [3/3] Dang khoi dong SQL Backend...
cd /d D:\Androi_DATN
python backend_sql.py

pause
