$ErrorActionPreference = "Stop"

$images = @(
    @{ Name = "api-gateway"; Dockerfile = "api-gateway/Dockerfile" },
    @{ Name = "user-service"; Dockerfile = "user-service/Dockerfile" },
    @{ Name = "product-service"; Dockerfile = "product-service/Dockerfile" },
    @{ Name = "order-service"; Dockerfile = "order-service/Dockerfile" }
)

foreach ($image in $images) {
    $tag = "mercadona/$($image.Name):0.0.1-SNAPSHOT"
    Write-Host "Building $tag"
    minikube image build -t $tag -f $image.Dockerfile .
}

