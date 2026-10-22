@echo off
cd /d "%~dp0"
where java >nul 2>nul
if errorlevel 1 (
 echo Instale JDK 17 y agregue Java al PATH. Consulte LEEME-PRIMERO.md.
 pause
 exit /b 1
)
echo Cuando aparezca Started CafeJuninApplication, abra http://localhost:8080
echo Para detener la demo, presione Ctrl+C en esta ventana.
java -jar "%~dp0dist\cafejunin-backend-0.1.0.jar" --spring.profiles.active=memory
pause
