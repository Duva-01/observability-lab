$ErrorActionPreference = "Stop"

function Wait-DeploymentRollout {
    param(
        [string]$Name,
        [string]$Namespace = "observability",
        [int]$TimeoutSeconds = 180
    )

    $result = kubectl rollout status deployment/$Name -n $Namespace --timeout="$($TimeoutSeconds)s" 2>&1
    if ($LASTEXITCODE -eq 0) {
        $result | Write-Host
        return
    }

    Write-Host $result
    Write-Host "Deployment $Name in namespace $Namespace did not complete in time. Showing diagnostics..."
    kubectl describe deployment $Name -n $Namespace
    kubectl get pods -n $Namespace -l app=$Name -o wide
    kubectl logs deployment/$Name -n $Namespace --tail=120 --all-containers=true
    throw "Rollout failed for deployment $Name in namespace $Namespace"
}

function Wait-DaemonSetRollout {
    param(
        [string]$Name,
        [string]$Namespace = "observability",
        [int]$TimeoutSeconds = 180
    )

    $result = kubectl rollout status daemonset/$Name -n $Namespace --timeout="$($TimeoutSeconds)s" 2>&1
    if ($LASTEXITCODE -eq 0) {
        $result | Write-Host
        return
    }

    Write-Host $result
    Write-Host "DaemonSet $Name in namespace $Namespace did not complete in time. Showing diagnostics..."
    kubectl describe daemonset $Name -n $Namespace
    kubectl get pods -n $Namespace -o wide
    kubectl logs daemonset/$Name -n $Namespace --tail=120 --all-containers=true
    throw "Rollout failed for daemonset $Name in namespace $Namespace"
}

kubectl apply -f .\deploy\observability\prometheus\namespace.yaml

kubectl apply -f .\deploy\observability\jaeger\
kubectl apply -f .\deploy\observability\otel-collector\
kubectl apply -f .\deploy\observability\prometheus\
kubectl apply -f .\deploy\observability\grafana\

helm repo add kube-state-metrics https://kubernetes.github.io/kube-state-metrics | Out-Null
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts | Out-Null
helm repo update | Out-Null

helm upgrade --install kube-state-metrics kube-state-metrics/kube-state-metrics `
    -n observability `
    -f .\deploy\observability\kube-state-metrics\values.yaml

helm upgrade --install node-exporter prometheus-community/prometheus-node-exporter `
    -n observability `
    -f .\deploy\observability\node-exporter\values.yaml

kubectl rollout restart deployment/jaeger -n observability
kubectl rollout restart deployment/otel-collector -n observability
kubectl rollout restart deployment/prometheus -n observability
kubectl rollout restart deployment/grafana -n observability

Wait-DeploymentRollout -Name "jaeger"
Wait-DeploymentRollout -Name "otel-collector"
Wait-DeploymentRollout -Name "prometheus"
Wait-DeploymentRollout -Name "grafana"
Wait-DeploymentRollout -Name "kube-state-metrics"
Wait-DaemonSetRollout -Name "node-exporter"

kubectl get pods -n observability
kubectl get svc -n observability
