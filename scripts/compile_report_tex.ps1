param(
  [string]$SourceTex = "docs/observability-lab-project.latex.tex"
)

$ErrorActionPreference = "Stop"

$sourceTexPath = Resolve-Path $SourceTex
$outputDir = Split-Path $sourceTexPath -Parent
$pdfPath = Join-Path $outputDir ([System.IO.Path]::GetFileName($sourceTexPath).Replace(".tex", ".pdf"))
$miktexBin = Join-Path (Get-Location) "tools\miktex-portable\texmfs\install\miktex\bin\x64"
$pdflatex = Join-Path $miktexBin "pdflatex.exe"

if (-not (Test-Path $pdflatex)) {
  throw "No se ha encontrado pdflatex en tools\\miktex-portable\\texmfs\\install\\miktex\\bin\\x64."
}

$env:PATH = $miktexBin + ";" + $env:PATH

Push-Location $outputDir
try {
  $texFile = [System.IO.Path]::GetFileName($sourceTexPath)
  $cmd = '"' + $pdflatex + '" -interaction=nonstopmode -halt-on-error "' + $texFile + '"'

  cmd /c $cmd
  if ($LASTEXITCODE -ne 0) {
    throw "La primera pasada de pdflatex ha fallado."
  }

  cmd /c $cmd
  if ($LASTEXITCODE -ne 0) {
    throw "La segunda pasada de pdflatex ha fallado."
  }
}
finally {
  Pop-Location
}

Get-Item `
  $sourceTexPath, `
  $pdfPath |
  Select-Object FullName, Length, LastWriteTime
