@echo off
title Huellitas - Iniciando aplicacion...
color 0A

echo.
echo  ==========================================
echo   HUELLITAS - Sistema Veterinario
echo  ==========================================
echo.

REM Verificar que existe el archivo .env
if not exist ".env" (
    echo  [ERROR] No se encontro el archivo .env
    echo.
    echo  Sigue estos pasos:
    echo  1. Copia el archivo .env.example
    echo  2. Renombralo a .env
    echo  3. Reemplaza DB_PASS con la contrasena del equipo
    echo.
    pause
    exit /b 1
)

echo  [OK] Archivo .env encontrado
echo  [..] Cargando variables de entorno...

REM Cargar variables del archivo .env
for /f "usebackq tokens=1,2 delims==" %%A in (".env") do (
    if not "%%A"=="" if not "%%A:~0,1%"=="#" (
        set "%%A=%%B"
    )
)

echo  [OK] Variables cargadas
echo  [..] Iniciando Spring Boot...
echo.
echo  Una vez iniciado, abre tu navegador en:
echo  --^> http://localhost:8080
echo.

call mvnw.cmd spring-boot:run

pause
