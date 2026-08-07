@echo off
title Than Nong AI - Khoi Dong Nhanh
echo --- DANG KHOI DONG HE THONG THAN NONG AI (PORT 8000) ---
echo.

echo [1/3] Dang lay dia chi IP may tinh...
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr "IPv4"') do (
    set "ip=%%a"
    goto :show_ip
)

:show_ip
echo IP CUA BAN LA:%ip%
echo.

echo [2/3] Dang thiet lap ket noi cap USB (ADB Reverse)...
adb reverse tcp:8000 tcp:8000

echo [3/3] Dang khoi dong Backend Python...
python backend_v13.py

pause
