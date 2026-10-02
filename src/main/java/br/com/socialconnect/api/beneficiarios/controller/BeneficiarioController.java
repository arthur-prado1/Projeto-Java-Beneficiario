package br.com.socialconnect.api.beneficiarios.controller;

import br.com.socialconnect.api.beneficiarios.dto.BeneficiarioPatchDTO;
import br.com.socialconnect.api.beneficiarios.dto.BeneficiarioRequestDTO;
import br.com.socialconnect.api.beneficiarios.dto.BeneficiarioResponseDTO;
import br.com.socialconnect.api.beneficiarios.service.BeneficiarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/beneficiarios")
@Tag(name = "Beneficiários", description = "API para gestão de beneficiários atendidos pela instituição")
public class BeneficiarioController {

    private final BeneficiarioService service;

    public BeneficiarioController(BeneficiarioService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            summary = "Lista todos os beneficiários",
            description = "Retorna uma lista paginada de beneficiários com filtros opcionais por nome e CPF"
    )
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    public ResponseEntity<Page<BeneficiarioResponseDTO>> listar(
            @Parameter(description = "Nome para filtrar (parcial, case-insensitive)")
            @RequestParam(required = false) String nome,
            @Parameter(description = "CPF para filtrar (exato)")
            @RequestParam(required = false) String cpf,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(service.listar(nome, cpf, pageable));
    }

    @GetMapping("/{idBeneficiario}")
    @Operation(summary = "Busca beneficiário por ID", description = "Recupera os detalhes de um beneficiário específico")
    @ApiResponse(responseCode = "200", description = "Beneficiário encontrado")
    @ApiResponse(responseCode = "404", description = "Beneficiário não encontrado")
    public ResponseEntity<BeneficiarioResponseDTO> buscarPorId(
            @Parameter(description = "ID do beneficiário", example = "1")
            @PathVariable Long idBeneficiario) {
        return ResponseEntity.ok(service.buscarPorId(idBeneficiario));
    }

    @PostMapping
    @Operation(summary = "Cria um novo beneficiário", description = "Cadastra um novo beneficiário no sistema com validação de dados e unicidade de CPF")
    @ApiResponse(responseCode = "201", description = "Beneficiário criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    @ApiResponse(responseCode = "409", description = "CPF já cadastrado no sistema")
    public ResponseEntity<BeneficiarioResponseDTO> criar(
            @Valid @RequestBody BeneficiarioRequestDTO dto) {
        BeneficiarioResponseDTO salvo = service.criar(dto);
        URI location = URI.create("/api/v1/beneficiarios/" + salvo.idBeneficiario());
        return ResponseEntity.created(location).body(salvo);
    }

    @PutMapping("/{idBeneficiario}")
    @Operation(summary = "Atualização total do beneficiário", description = "Substitui completamente os dados do beneficiário existente (PUT)")
    @ApiResponse(responseCode = "200", description = "Beneficiário atualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    @ApiResponse(responseCode = "404", description = "Beneficiário não encontrado")
    @ApiResponse(responseCode = "409", description = "CPF informado já pertence a outro beneficiário")
    public ResponseEntity<BeneficiarioResponseDTO> atualizar(
            @Parameter(description = "ID do beneficiário", example = "1")
            @PathVariable Long idBeneficiario,
            @Valid @RequestBody BeneficiarioRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(idBeneficiario, dto));
    }

    @PatchMapping("/{idBeneficiario}")
    @Operation(summary = "Atualização parcial do beneficiário", description = "Atualiza apenas os campos enviados no corpo da requisição (PATCH)")
    @ApiResponse(responseCode = "200", description = "Beneficiário atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Beneficiário não encontrado")
    public ResponseEntity<BeneficiarioResponseDTO> atualizarParcial(
            @Parameter(description = "ID do beneficiário", example = "1")
            @PathVariable Long idBeneficiario,
            @RequestBody BeneficiarioPatchDTO dto) {
        return ResponseEntity.ok(service.atualizarParcial(idBeneficiario, dto));
    }

    @DeleteMapping("/{idBeneficiario}")
    @Operation(summary = "Remove um beneficiário", description = "Exclui o cadastro de um beneficiário pelo ID")
    @ApiResponse(responseCode = "204", description = "Beneficiário excluído com sucesso")
    @ApiResponse(responseCode = "404", description = "Beneficiário não encontrado")
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do beneficiário", example = "1")
            @PathVariable Long idBeneficiario) {
        service.deletar(idBeneficiario);
        return ResponseEntity.noContent().build();
    }
}