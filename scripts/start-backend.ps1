$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$rootDir = Split-Path -Parent $scriptDir
$backendDir = Join-Path $rootDir 'backend'
$localConfigPath = Join-Path $scriptDir 'start-backend.local.ps1'

if (-not (Test-Path (Join-Path $backendDir 'mvnw.cmd'))) {
    Write-Host "[error] Backend directory not found: $backendDir" -ForegroundColor Red
    exit 1
}

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Host '[error] Java was not found in PATH.' -ForegroundColor Red
    exit 1
}

if (Test-Path $localConfigPath) {
    . $localConfigPath
}

if (-not $env:DB_USERNAME) {
    $env:DB_USERNAME = 'root'
}

if (-not $env:JWT_SECRET) {
    $env:JWT_SECRET = 'change-this-local-jwt-secret-to-a-long-random-value'
}

$env:APP_SEED_DEMO_DATA = 'false'

if (-not $env:DEEPSEEK_BASE_URL) {
    $env:DEEPSEEK_BASE_URL = 'https://openrouter.fans'
}

if (-not $env:DEEPSEEK_MODEL) {
    $env:DEEPSEEK_MODEL = 'deepseek-chat'
}

if (-not $env:DEEPSEEK_API_KEY) {
    Write-Host "[error] Missing DEEPSEEK_API_KEY. Create $localConfigPath based on start-backend.local.example.ps1." -ForegroundColor Red
    exit 1
}

if (-not $env:DB_PASSWORD) {
    Write-Host "[error] Missing DB_PASSWORD. Create $localConfigPath based on start-backend.local.example.ps1." -ForegroundColor Red
    exit 1
}

if ($env:GETOFFER_DRY_RUN -eq '1') {
    Write-Host "[dry-run] BACKEND_DIR=$backendDir"
    Write-Host "[dry-run] LOCAL_CONFIG=$localConfigPath"
    Write-Host "[dry-run] DB_USERNAME=$env:DB_USERNAME"
    Write-Host "[dry-run] DB_PASSWORD_SET=$([bool]$env:DB_PASSWORD)"
    Write-Host "[dry-run] JWT_SECRET_SET=$([bool]$env:JWT_SECRET)"
    Write-Host "[dry-run] APP_SEED_DEMO_DATA=$env:APP_SEED_DEMO_DATA"
    Write-Host "[dry-run] DEEPSEEK_BASE_URL=$env:DEEPSEEK_BASE_URL"
    Write-Host "[dry-run] DEEPSEEK_MODEL=$env:DEEPSEEK_MODEL"
    Write-Host "[dry-run] DEEPSEEK_API_KEY_SET=$([bool]$env:DEEPSEEK_API_KEY)"
    Write-Host '[dry-run] .\mvnw.cmd spring-boot:run'
    exit 0
}

Write-Host "[backend] Working directory: $backendDir" -ForegroundColor Cyan
Write-Host '[backend] Starting Spring Boot...' -ForegroundColor Cyan

Push-Location $backendDir
try {
    & .\mvnw.cmd spring-boot:run
    $exitCode = $LASTEXITCODE
}
finally {
    Pop-Location
}

Write-Host ''
Write-Host "[backend] Process exited with code: $exitCode" -ForegroundColor Yellow
exit $exitCode
