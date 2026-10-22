<#
.SYNOPSIS
    Convert mot file .docx sang .pdf bang Word COM Automation.
    Yeu cau: may chay script phai co cai Microsoft Word.

.PARAMETER DocxPath
    Duong dan tuyet doi toi file .docx nguon.

.PARAMETER OutDir
    Thu muc dich de luu file .pdf (cung ten, doi duoi .pdf).
#>
param(
    [Parameter(Mandatory = $true)][string]$DocxPath,
    [Parameter(Mandatory = $true)][string]$OutDir
)

$ErrorActionPreference = "Stop"

$DocxPath = (Resolve-Path -LiteralPath $DocxPath).Path
if (-not (Test-Path -LiteralPath $OutDir)) {
    New-Item -ItemType Directory -Path $OutDir -Force | Out-Null
}
$OutDir = (Resolve-Path -LiteralPath $OutDir).Path

$baseName = [System.IO.Path]::GetFileNameWithoutExtension($DocxPath)
$pdfPath = Join-Path $OutDir ($baseName + ".pdf")

# wdFormatPDF = 17
$wdFormatPDF = 17

$word = New-Object -ComObject Word.Application
$word.Visible = $false
try {
    $doc = $word.Documents.Open($DocxPath, $false, $true)
    try {
        $doc.SaveAs([ref]$pdfPath, [ref]$wdFormatPDF)
    }
    finally {
        $doc.Close([ref]$false)
    }
}
finally {
    $word.Quit()
    [System.Runtime.Interopservices.Marshal]::ReleaseComObject($word) | Out-Null
}

Write-Output $pdfPath
