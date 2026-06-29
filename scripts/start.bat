@echo off
echo ============================================
echo  MedEnt Agent Platform - Starting Stack
echo ============================================
cd /d "%~dp0.."
if not exist .env copy .env.example .env
echo Building and starting all services...
docker compose up -d --build
echo.
echo Waiting for services to be healthy...
timeout /t 30 /nobreak >nul
echo.
echo ============================================
echo  Services:
echo  Admin Portal:         http://localhost:3001
echo  Student Portal:       http://localhost:3002
echo  Observability:        http://localhost:3003
echo  Agent API:            http://localhost:8080
echo  Analytics API:        http://localhost:8000
echo  Load Balancer:        http://localhost:80
echo  RabbitMQ Management:  http://localhost:15672
echo ============================================
docker compose ps
