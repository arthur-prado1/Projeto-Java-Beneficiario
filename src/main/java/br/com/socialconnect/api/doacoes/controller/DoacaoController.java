package br.com.socialconnect.api.doacoes.controller;

import br.com.socialconnect.api.doacoes.dto.DoacaoRequestDTO;
import br.com.socialconnect.api.doacoes.dto.DoacaoResponseDTO;
import br.com.socialconnect.api.doacoes.model.TipoDoacao;
import br.com.socialconnect.api.doacoes.service.DoacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/doacoes")
@Tag(name = "Doações", description = "API para registro e consulta de doações recebidas")
public class DoacaoController {

    private final DoacaoService service;

    public DoacaoController(DoacaoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Registra uma nova doação", description = "Cadastra uma nova doação associada a um doador existente")
    @ApiResponse(responseCode = "201", description = "Doação registrada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados da doação inválidos")
    @ApiResponse(responseCode = "404", description = "Doador não encontrado")
    public ResponseEntity<DoacaoResponseDTO> salvar(@Valid @RequestBody DoacaoRequestDTO dto) {
        DoacaoResponseDTO salvo = service.criar(dto);
        URI location = URI.create("/api/v1/doacoes/" + salvo.idDoacao());
        return ResponseEntity.created(location).body(salvo);
    }

    @GetMapping
    @Operation(summary = "Lista doações com filtros", description = "Retorna lista paginada de doações com filtros por período e tipo")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<Page<DoacaoResponseDTO>> listar(
            @Parameter(description = "Data inicial para filtro (AAAA-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data final para filtro (AAAA-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @Parameter(description = "Tipo da doação")
            @RequestParam(required = false) TipoDoacao tipo,
            @PageableDefault(size = 10, sort = "dataDoacao", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(service.listar(dataInicio, dataFim, tipo, pageable));
    }

    @GetMapping("/{idDoacao}")
    @Operation(summary = "Busca doação por ID", description = "Recupera os detalhes de uma doação específica")
    @ApiResponse(responseCode = "200", description = "Doação encontrada com sucesso")
    @ApiResponse(responseCode = "404", description = "Doação não encontrada")
    public ResponseEntity<DoacaoResponseDTO> buscarPorId(
            @Parameter(description = "ID da doação", example = "1")
            @PathVariable Long idDoacao) {
        return ResponseEntity.ok(service.buscarPorId(idDoacao));
    }

    @DeleteMapping("/{idDoacao}")
    @Operation(summary = "Remove uma doação", description = "Exclui o registro de uma doação pelo ID")
    @ApiResponse(responseCode = "204", description = "Doação excluída com sucesso")
    @ApiResponse(responseCode = "404", description = "Doação não encontrada")
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID da doação", example = "1")
            @PathVariable Long idDoacao) {
        service.deletar(idDoacao);
        return ResponseEntity.noContent().build();
    }
}
