$ErrorActionPreference = "Stop"

$database = @{ Release = "postgres"; Chart = ".\\deploy\\helm\\postgres"; Values = ".\\deploy\\environments\\minikube\\postgres-values.yaml" }

$services = @(
    @{ Release = "user-service"; Chart = ".\\deploy\\helm\\user-service"; Values = ".\\deploy\\environments\\minikube\\user-service-values.yaml" },
    @{ Release = "product-service"; Chart = ".\\deploy\\helm\\product-service"; Values = ".\\deploy\\environments\\minikube\\product-service-values.yaml" },
    @{ Release = "order-service"; Chart = ".\\deploy\\helm\\order-service"; Values = ".\\deploy\\environments\\minikube\\order-service-values.yaml" },
    @{ Release = "api-gateway"; Chart = ".\\deploy\\helm\\api-gateway"; Values = ".\\deploy\\environments\\minikube\\api-gateway-values.yaml" }
)

function Wait-DeploymentRollout {
    param(
        [string]$Name,
        [int]$TimeoutSeconds = 180
    )

    $result = kubectl rollout status deployment/$Name --timeout="$($TimeoutSeconds)s" 2>&1
    if ($LASTEXITCODE -eq 0) {
        $result | Write-Host
        return
    }

    Write-Host $result
    Write-Host "Deployment $Name did not complete in time. Showing diagnostics..."
    kubectl describe deployment $Name
    kubectl get pods -l app=$Name -o wide
    kubectl logs deployment/$Name --tail=120 --all-containers=true
    throw "Rollout failed for deployment $Name"
}

helm upgrade --install $database.Release $database.Chart -f $database.Values
Wait-DeploymentRollout -Name "postgres"

foreach ($service in $services) {
    helm upgrade --install $service.Release $service.Chart -f $service.Values
}

foreach ($service in $services) {
    kubectl rollout restart deployment/$($service.Release)
}

Wait-DeploymentRollout -Name "user-service"
Wait-DeploymentRollout -Name "product-service"
Wait-DeploymentRollout -Name "order-service"
Wait-DeploymentRollout -Name "api-gateway"
kubectl get pods
kubectl get svc
