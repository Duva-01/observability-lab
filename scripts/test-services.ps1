param(
    [switch]$EnsurePortForwards
)

$ErrorActionPreference = "Stop"

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

if ($EnsurePortForwards) {
    & (Join-Path $scriptRoot "port-forward-services.ps1") start
    Start-Sleep -Seconds 2
}

$checks = @(
    @{ Name = "api-gateway-overview"; Url = "http://127.0.0.1:8080/api/overview"; Type = "json" },
    @{ Name = "user-service-health"; Url = "http://127.0.0.1:8081/actuator/health"; Type = "json" },
    @{ Name = "user-service-prometheus"; Url = "http://127.0.0.1:8081/actuator/prometheus"; Type = "text" },
    @{ Name = "product-service-health"; Url = "http://127.0.0.1:8082/actuator/health"; Type = "json" },
    @{ Name = "product-service-prometheus"; Url = "http://127.0.0.1:8082/actuator/prometheus"; Type = "text" },
    @{ Name = "order-service-health"; Url = "http://127.0.0.1:8083/actuator/health"; Type = "json" },
    @{ Name = "order-service-prometheus"; Url = "http://127.0.0.1:8083/actuator/prometheus"; Type = "text" }
)

$results = @()

foreach ($check in $checks) {
    try {
        $response = Invoke-WebRequest -Uri $check.Url -UseBasicParsing -TimeoutSec 10

        $summary = ""
        if ($check.Type -eq "json") {
            try {
                $json = $response.Content | ConvertFrom-Json
                if ($json.status) {
                    $summary = "status=$($json.status)"
                } else {
                    $summary = "json-ok"
                }
            } catch {
                $summary = "json-unreadable"
            }
        } else {
            if ($response.Content -match "# HELP") {
                $summary = "prometheus-format-ok"
            } else {
                $summary = "text-ok"
            }
        }

        $results += [pscustomobject]@{
            Check      = $check.Name
            Status     = "OK"
            StatusCode = $response.StatusCode
            Summary    = $summary
            Url        = $check.Url
        }
    } catch {
        $results += [pscustomobject]@{
            Check      = $check.Name
            Status     = "FAIL"
            StatusCode = ""
            Summary    = $_.Exception.Message
            Url        = $check.Url
        }
    }
}

$results | Format-Table -AutoSize

$failed = $results | Where-Object { $_.Status -eq "FAIL" }
if ($failed.Count -gt 0) {
    exit 1
}
