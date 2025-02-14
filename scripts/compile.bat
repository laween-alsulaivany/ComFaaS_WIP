@echo off
REM compile.bat - Compile Java source using Maven and generate ComFaaS.jar in the root directory.

SET SCRIPT_DIR=%~dp0
SET ROOT_DIR=%SCRIPT_DIR%..
SET BACKEND_DIR=%ROOT_DIR%\backend

echo [INFO] Compiling and packaging using Maven...
cd /d "%BACKEND_DIR%" || (echo [ERROR] Backend directory not found. & exit /b 1)

REM Ensure env.bat exists and load it if available
SET ENV_FILE=%BACKEND_DIR%\configs\env.bat
IF EXIST "%ENV_FILE%" (
    CALL "%ENV_FILE%"
    echo [INFO] Environment variables loaded from env.bat.
) ELSE (
    echo [WARNING] env.bat not found. Proceeding without it.
)

REM Manually remove the target directory to prevent file lock issues
IF EXIST "%BACKEND_DIR%\target" (
    echo [INFO] Removing existing target directory...
    rmdir /s /q "%BACKEND_DIR%\target"
)

REM Compile and package using Maven
mvn clean package || (echo [ERROR] Maven build failed. & exit /b 1)

REM Move the JAR to the root directory and rename it
echo [INFO] Moving JAR to root directory as ComFaaS.jar...
move /Y "%BACKEND_DIR%\target\backend-1.0-SNAPSHOT.jar" "%ROOT_DIR%\ComFaaS.jar" || (echo [ERROR] Failed to move JAR file. & exit /b 1)

echo [SUCCESS] Compilation and JAR creation completed.
