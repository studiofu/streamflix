#!/usr/bin/env pwsh
# Stop all infra containers started by dev-up.ps1.
# mprocs-managed Java procs are stopped when you quit mprocs itself.

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

Write-Host "Stopping infra containers..." -ForegroundColor Cyan
docker compose down
