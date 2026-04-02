param(
  [string]$Source = "docs/observability-lab-project.md",
  [string]$OutputTex = "docs/observability-lab-project.latex.tex",
  [string]$OutputPdf = "docs/observability-lab-project.latex.pdf",
  [string]$Metadata = "docs/latex/report-metadata.yaml",
  [string]$Template = "docs/latex/academic-template.tex"
)

$ErrorActionPreference = "Stop"

$pandoc = Resolve-Path ".\tools\pandoc\pandoc-3.9.0.2\pandoc.exe"
$tectonic = Resolve-Path ".\tools\tectonic\tectonic.exe"
$sourcePath = Resolve-Path $Source
$metadataPath = Resolve-Path $Metadata
$templatePath = Resolve-Path $Template
$preamblePath = Resolve-Path ".\docs\latex\preamble.tex"
$cleanupFilter = Resolve-Path ".\docs\latex\cleanup-frontmatter.lua"
$outputTexPath = Join-Path (Get-Location) $OutputTex
$outputPdfPath = Join-Path (Get-Location) $OutputPdf
$outputDir = Split-Path $outputTexPath -Parent
$compiledPdfPath = Join-Path $outputDir ([System.IO.Path]::GetFileName($outputTexPath).Replace(".tex", ".pdf"))

& $pandoc `
  $sourcePath `
  --from "markdown+smart+fenced_code_blocks+pipe_tables" `
  --standalone `
  --metadata-file $metadataPath `
  --template $templatePath `
  --include-in-header $preamblePath `
  --lua-filter $cleanupFilter `
  --number-sections `
  --shift-heading-level-by -1 `
  --top-level-division=chapter `
  --toc `
  --syntax-highlighting=none `
  --output $outputTexPath

& $tectonic `
  $outputTexPath `
  --outdir $outputDir

if ([System.IO.Path]::GetFullPath($outputPdfPath) -ne [System.IO.Path]::GetFullPath($compiledPdfPath)) {
  Copy-Item `
    $compiledPdfPath `
    $outputPdfPath `
    -Force
}

Get-Item $outputTexPath, $outputPdfPath | Select-Object FullName, Length, LastWriteTime
