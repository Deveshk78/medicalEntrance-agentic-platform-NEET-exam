# MedEnt — Start all portals and open in separate browser windows
$ProjectRoot = "C:\Users\DeveshKumar(HtH)\Projects\medent-agent-platform"
Set-Location $ProjectRoot

$AdminUrl = "http://localhost:3001"
$StudentUrl = "http://localhost:3002"
$ObsUrl = "http://localhost:3003"

function Test-Port($Port) {
    try {
        $tcp = New-Object System.Net.Sockets.TcpClient
        $tcp.Connect("127.0.0.1", $Port)
        $tcp.Close()
        return $true
    } catch { return $false }
}

# Try Docker first
$dockerOk = $false
if (Get-Command docker -ErrorAction SilentlyContinue) {
    Write-Host "Starting portals via Docker Compose..."
    docker compose up -d admin-portal student-portal observability-portal 2>$null
    Start-Sleep -Seconds 8
    if ((Test-Port 3001) -and (Test-Port 3002) -and (Test-Port 3003)) {
        $dockerOk = $true
        Write-Host "Docker portals are up."
    }
}

# Fallback: npm dev in separate PowerShell windows
if (-not $dockerOk) {
    Write-Host "Starting npm dev servers in separate windows..."
    $portals = @(
        @{ Name = "Admin"; Path = "portals\admin"; Port = 3001 },
        @{ Name = "Student"; Path = "portals\student"; Port = 3002 },
        @{ Name = "Observability"; Path = "portals\observability"; Port = 3003 }
    )
    foreach ($p in $portals) {
        $dir = Join-Path $ProjectRoot $p.Path
        if (-not (Test-Path (Join-Path $dir "node_modules"))) {
            Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$dir'; npm install; npm run dev"
        } else {
            Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$dir'; npm run dev"
        }
    }
    Write-Host "Waiting for dev servers (up to 45s)..."
    $deadline = (Get-Date).AddSeconds(45)
    while ((Get-Date) -lt $deadline) {
        if ((Test-Port 3001) -and (Test-Port 3002) -and (Test-Port 3003)) { break }
        Start-Sleep -Seconds 2
    }
}

# Open each portal in a separate browser window
Write-Host "Opening portals in separate browser windows..."
Start-Process $AdminUrl
Start-Sleep -Milliseconds 500
Start-Process $StudentUrl
Start-Sleep -Milliseconds 500
Start-Process $ObsUrl

Write-Host ""
Write-Host "============================================"
Write-Host " Admin Portal:         $AdminUrl"
Write-Host " Student Portal:       $StudentUrl"
Write-Host " Observability Portal: $ObsUrl"
Write-Host "============================================"
