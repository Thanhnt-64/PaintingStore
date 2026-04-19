# Redis Setup Script for Windows
# This script downloads and runs Redis for local development

Write-Host "Setting up Redis for ManageStore application..." -ForegroundColor Green

# Check if running as Administrator
$currentPrincipal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
if (-not $currentPrincipal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Host "This script requires Administrator privileges. Please run PowerShell as Administrator." -ForegroundColor Red
    exit 1
}

# Check if Chocolatey is installed
if (-not (Get-Command choco -ErrorAction SilentlyContinue)) {
    Write-Host "Installing Chocolatey package manager..." -ForegroundColor Yellow
    Set-ExecutionPolicy Bypass -Scope Process -Force
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
    Invoke-Expression ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
    
    # Refresh environment variables
    $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
}

# Install Redis using Chocolatey
Write-Host "Installing Redis..." -ForegroundColor Yellow
choco install redis-64 -y

# Start Redis service
Write-Host "Starting Redis service..." -ForegroundColor Yellow
Start-Service redis

# Test Redis connection
Write-Host "Testing Redis connection..." -ForegroundColor Yellow
try {
    $redisTest = redis-cli ping
    if ($redisTest -eq "PONG") {
        Write-Host "✅ Redis is running successfully!" -ForegroundColor Green
        Write-Host "Redis is available at localhost:6379" -ForegroundColor Green
    } else {
        Write-Host "❌ Redis test failed" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Redis connection test failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n📋 Next steps:" -ForegroundColor Cyan
Write-Host "1. Redis is now running on localhost:6379" -ForegroundColor White
Write-Host "2. Configure email settings in application.properties" -ForegroundColor White
Write-Host "3. Start your Spring Boot application: mvn spring-boot:run" -ForegroundColor White

Write-Host "`n🔧 Redis Management:" -ForegroundColor Cyan
Write-Host "- Start: Start-Service redis" -ForegroundColor White
Write-Host "- Stop: Stop-Service redis" -ForegroundColor White
Write-Host "- Test: redis-cli ping" -ForegroundColor White
Write-Host "- Monitor: redis-cli monitor" -ForegroundColor White