#!/usr/bin/env pwsh
# Bring up infra containers and launch mprocs for all Spring Boot services.
# Run from anywhere; this script pins its own working directory to the repo root.

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

Write-Host "Starting infra containers (waiting until healthy)..." -ForegroundColor Cyan
docker compose up -d --wait

Write-Host "Launching mprocs..." -ForegroundColor Cyan
mprocs
