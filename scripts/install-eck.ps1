$ErrorActionPreference = "Stop"

kubectl apply -k .\deploy\observability\elastic\eck
kubectl get pods -n elastic-system
