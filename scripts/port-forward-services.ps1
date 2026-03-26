param(
    [ValidateSet("start", "stop", "status")]
    [string]$Action = "start",
    [ValidateSet("all", "apps", "observability")]
    [string]$Scope = "all"
)

$ErrorActionPreference = "Stop"

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$stateDir = Join-Path $scriptRoot ".port-forward-state"
$stateFile = Join-Path $stateDir "port-forwards.json"

function Get-ForwardsByScope {
    param(
        [string]$SelectedScope
    )

    $apps = @(
        @{ Name = "api-gateway"; Namespace = "default"; LocalPort = 8080; RemotePort = 8080; Url = "http://127.0.0.1:8080/api/overview" },
        @{ Name = "user-service"; Namespace = "default"; LocalPort = 8081; RemotePort = 8080; Url = "http://127.0.0.1:8081/actuator/prometheus" },
        @{ Name = "product-service"; Namespace = "default"; LocalPort = 8082; RemotePort = 8080; Url = "http://127.0.0.1:8082/actuator/prometheus" },
        @{ Name = "order-service"; Namespace = "default"; LocalPort = 8083; RemotePort = 8080; Url = "http://127.0.0.1:8083/actuator/prometheus" }
    )

    $observability = @(
        @{ Name = "grafana"; Namespace = "observability"; LocalPort = 3000; RemotePort = 3000; Url = "http://127.0.0.1:3000" },
        @{ Name = "prometheus"; Namespace = "observability"; LocalPort = 9090; RemotePort = 9090; Url = "http://127.0.0.1:9090" },
        @{ Name = "jaeger"; Namespace = "observability"; LocalPort = 16686; RemotePort = 16686; Url = "http://127.0.0.1:16686" },
        @{ Name = "mercadona-kibana-kb-http"; Namespace = "observability"; LocalPort = 5601; RemotePort = 5601; Url = "https://127.0.0.1:5601" },
        @{ Name = "mercadona-logs-es-http"; Namespace = "observability"; LocalPort = 9200; RemotePort = 9200; Url = "https://127.0.0.1:9200" },
        @{ Name = "otel-collector"; Namespace = "observability"; LocalPort = 4317; RemotePort = 4317; Url = "grpc://127.0.0.1:4317" },
        @{ Name = "otel-collector-http"; Namespace = "observability"; ServiceName = "otel-collector"; LocalPort = 4318; RemotePort = 4318; Url = "http://127.0.0.1:4318" },
        @{ Name = "gitlab-ingress-http"; Namespace = "ingress-nginx"; ServiceName = "ingress-nginx-controller"; LocalPort = 8088; RemotePort = 80; Url = "http://gitlab.192.168.49.2.nip.io:8088" },
        @{ Name = "gitlab-ingress-https"; Namespace = "ingress-nginx"; ServiceName = "ingress-nginx-controller"; LocalPort = 8443; RemotePort = 443; Url = "https://gitlab.192.168.49.2.nip.io:8443" }
    )

    switch ($SelectedScope) {
        "apps" { return $apps }
        "observability" { return $observability }
        default { return @($apps + $observability) }
    }
}

$forwards = Get-ForwardsByScope -SelectedScope $Scope

if (-not $forwards.Count) {
    throw "No hay servicios configurados para el scope '$Scope'."
}

function Get-ServiceReference {
    param(
        [hashtable]$Forward
    )

    if ($Forward.ContainsKey("ServiceName")) {
        return $Forward.ServiceName
    }

    return $Forward.Name
}

function Get-StateKey {
    param(
        [hashtable]$Forward
    )

    return "$($Forward.Namespace)/$($Forward.Name):$($Forward.LocalPort)"
}

function Get-StateKeyFromItem {
    param(
        $Item
    )

    if ($Item.PSObject.Properties.Name -contains "Key") {
        return $Item.Key
    }

    if ($Item.PSObject.Properties.Name -contains "Namespace") {
        return "$($Item.Namespace)/$($Item.Name)"
    }

    return $Item.Name
}

function Get-DisplayName {
    param(
        [hashtable]$Forward
    )

    return "$($Forward.Namespace)/$($Forward.Name)"
}

function Get-DisplayNameFromItem {
    param(
        $Item
    )

    if ($Item.PSObject.Properties.Name -contains "DisplayName") {
        return $Item.DisplayName
    }

    if ($Item.PSObject.Properties.Name -contains "Namespace") {
        return "$($Item.Namespace)/$($Item.Name)"
    }

    return $Item.Name
}

function Test-ForwardProcess {
    param(
        [int]$ProcessId
    )

    try {
        $null = Get-Process -Id $ProcessId -ErrorAction Stop
        return $true
    } catch {
        return $false
    }
}

function Load-State {
    if (-not (Test-Path $stateFile)) {
        return @()
    }

    $raw = Get-Content $stateFile -Raw
    if ([string]::IsNullOrWhiteSpace($raw)) {
        return @()
    }

    $state = $raw | ConvertFrom-Json
    if ($state -is [System.Array]) {
        return $state
    }

    return @($state)
}

function Save-State {
    param(
        [array]$State
    )

    if (-not (Test-Path $stateDir)) {
        New-Item -ItemType Directory -Path $stateDir | Out-Null
    }

    $State | ConvertTo-Json | Set-Content $stateFile
}

switch ($Action) {
    "start" {
        if (-not (Test-Path $stateDir)) {
            New-Item -ItemType Directory -Path $stateDir | Out-Null
        }

        $existingState = Load-State
        $newState = @()

        foreach ($forward in $forwards) {
            $stateKey = Get-StateKey -Forward $forward
            $displayName = Get-DisplayName -Forward $forward
            $serviceRef = Get-ServiceReference -Forward $forward

            $current = $existingState | Where-Object { (Get-StateKeyFromItem -Item $_) -eq $stateKey } | Select-Object -First 1
            if ($current -and (Test-ForwardProcess -ProcessId ([int]$current.ProcessId))) {
                Write-Host "${displayName}: ya esta corriendo en localhost:$($forward.LocalPort)"
                $newState += $current
                continue
            }

            $safeName = $displayName.Replace("/", "__")
            $stdout = Join-Path $stateDir "$safeName.out.log"
            $stderr = Join-Path $stateDir "$safeName.err.log"

            try {
                $process = Start-Process -FilePath "kubectl" `
                    -ArgumentList @("-n", $forward.Namespace, "port-forward", "svc/$serviceRef", "$($forward.LocalPort):$($forward.RemotePort)") `
                    -RedirectStandardOutput $stdout `
                    -RedirectStandardError $stderr `
                    -PassThru
            } catch {
                Write-Host "${displayName}: no se pudo arrancar el port-forward."
                Write-Host "Revisa: $stderr"
                continue
            }

            Start-Sleep -Seconds 1

            if (-not (Test-ForwardProcess -ProcessId $process.Id)) {
                Write-Host "${displayName}: fallo al arrancar el port-forward. Revisa $stderr"
                continue
            }

            $newState += [pscustomobject]@{
                Key        = $stateKey
                Name       = $serviceRef
                Namespace  = $forward.Namespace
                DisplayName = $displayName
                LocalPort  = $forward.LocalPort
                RemotePort = $forward.RemotePort
                ProcessId  = $process.Id
                Url        = $forward.Url
            }

            Write-Host "${displayName}: localhost:$($forward.LocalPort) -> svc/${serviceRef}:$($forward.RemotePort)"
        }

        Save-State -State $newState

        Write-Host ""
        Write-Host "URLs utiles:"
        foreach ($item in $newState) {
            Write-Host "- $(Get-DisplayNameFromItem -Item $item): $($item.Url)"
        }
    }

    "stop" {
        $state = Load-State
        if (-not $state.Count) {
            Write-Host "No hay port-forwards guardados."
            break
        }

        foreach ($item in $state) {
            if (Test-ForwardProcess -ProcessId ([int]$item.ProcessId)) {
                Stop-Process -Id ([int]$item.ProcessId) -Force
                Write-Host "$(Get-DisplayNameFromItem -Item $item): detenido"
            } else {
                Write-Host "$(Get-DisplayNameFromItem -Item $item): ya no estaba corriendo"
            }
        }

        if (Test-Path $stateFile) {
            Remove-Item $stateFile -Force
        }
    }

    "status" {
        $state = Load-State
        if (-not $state.Count) {
            Write-Host "No hay port-forwards guardados."
            break
        }

        foreach ($item in $state) {
            $running = Test-ForwardProcess -ProcessId ([int]$item.ProcessId)
            $status = if ($running) { "RUNNING" } else { "STOPPED" }
            Write-Host "$(Get-DisplayNameFromItem -Item $item): $status | localhost:$($item.LocalPort) | PID=$($item.ProcessId)"
        }
    }
}
