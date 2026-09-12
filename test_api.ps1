$file = "api_test_results.md"
Out-File -FilePath $file -Encoding utf8 -InputObject "# Testes da API SocialConnect`n"

function Test-Endpoint {
    param (
        [string]$Method,
        [string]$Url,
        [string]$Body = $null
    )
    $output = "## $Method $Url`n"
    if ($Body) {
        $output += "**Request Payload:**`n````json`n$Body`n`````n"
    }

    $params = @{
        Uri = $Url
        Method = $Method
        ErrorAction = "Stop"
        SkipHttpErrorCheck = $true
    }

    if ($Body) {
        $params.Body = $Body
        $params.ContentType = "application/json"
    }

    try {
        $response = Invoke-RestMethod @params -ResponseHeadersVariable headers -StatusCodeVariable status
        $responseBody = $response | ConvertTo-Json -Depth 10
        $output += "**Response Status:** $status`n"
        $output += "**Response Payload:**`n````json`n$responseBody`n`````n"
    } catch {
        $output += "**Error Status:** $($_.Exception.Response.StatusCode.value__)`n"
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $errBody = $reader.ReadToEnd()
        $output += "**Error Response Payload:**`n````json`n$errBody`n`````n"
    }
    
    Add-Content -Path $file -Value $output -Encoding utf8
}

Write-Host "Aguardando API..."
while (!(Test-NetConnection localhost -Port 8080 -WarningAction SilentlyContinue).TcpTestSucceeded) {
    Start-Sleep -Seconds 2
}
Write-Host "API online, rodando testes..."

$beneficiarioBody = @{
    nome = "João da Silva"
    cpf = "123.456.789-00"
    telefone = "(11) 99999-9999"
    endereco = "Rua das Flores, 123"
    situacaoVulnerabilidade = "Desempregado"
    dataCadastro = "2026-09-04"
} | ConvertTo-Json

# 1. POST Beneficiario
Test-Endpoint -Method "POST" -Url "http://localhost:8080/api/v1/beneficiarios" -Body $beneficiarioBody

# 2. GET Todos Beneficiarios
Test-Endpoint -Method "GET" -Url "http://localhost:8080/api/v1/beneficiarios"

# 3. GET Beneficiario por ID
Test-Endpoint -Method "GET" -Url "http://localhost:8080/api/v1/beneficiarios/1"

# 4. GET Doacoes (vazio, mas testa endpoint)
Test-Endpoint -Method "GET" -Url "http://localhost:8080/api/v1/doacoes?tipo=ALIMENTO&dataInicio=2026-01-01"

# 5. DELETE Beneficiario por ID
Test-Endpoint -Method "DELETE" -Url "http://localhost:8080/api/v1/beneficiarios/1"

Write-Host "Testes concluídos! Resultados salvos em $file"
