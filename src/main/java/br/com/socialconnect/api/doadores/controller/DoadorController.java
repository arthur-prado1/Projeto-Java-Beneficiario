package br.com.socialconnect.api.doadores.controller;

import br.com.socialconnect.api.doadores.dto.DoadorRequestDTO;
import br.com.socialconnect.api.doadores.dto.DoadorResponseDTO;
import br.com.socialconnect.api.doadores.service.DoadorService;
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
@RequestMapping("/api/v1/doadores")
@Tag(name = "Doadores", description = "API para gestão de doadores (pessoas físicas e jurídicas)")
public class DoadorController {

    private final DoadorService service;

    public DoadorController(DoadorService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista todos os doadores", description = "Retorna uma lista paginada de doadores com filtro opcional por nome")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<Page<DoadorResponseDTO>> listar(
            @Parameter(description = "Nome para filtrar (parcial)")
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(service.listar(nome, pageable));
    }

    @GetMapping("/{idDoador}")
    @Operation(summary = "Busca doador por ID", description = "Recupera os detalhes de um doador específico")
    @ApiResponse(responseCode = "200", description = "Doador encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Doador não encontrado")
    public ResponseEntity<DoadorResponseDTO> buscarPorId(
            @Parameter(description = "ID do doador", example = "1")
            @PathVariable Long idDoador) {
        return ResponseEntity.ok(service.buscarPorId(idDoador));
    }

    @PostMapping
    @Operation(summary = "Cria um novo doador", description = "Cadastra um novo doador no sistema")
    @ApiResponse(responseCode = "201", description = "Doador cadastrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    public ResponseEntity<DoadorResponseDTO> criar(
            @Valid @RequestBody DoadorRequestDTO dto) {
        DoadorResponseDTO salvo = service.criar(dto);
        URI location = URI.create("/api/v1/doadores/" + salvo.idDoador());
        return ResponseEntity.created(location).body(salvo);
    }

    @PutMapping("/{idDoador}")
    @Operation(summary = "Atualiza um doador", description = "Atualiza todos os dados de um doador cadastrado")
    @ApiResponse(responseCode = "200", description = "Doador atualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    @ApiResponse(responseCode = "404", description = "Doador não encontrado")
    public ResponseEntity<DoadorResponseDTO> atualizar(
            @Parameter(description = "ID do doador", example = "1")
            @PathVariable Long idDoador,
            @Valid @RequestBody DoadorRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(idDoador, dto));
    }

    @DeleteMapping("/{idDoador}")
    @Operation(summary = "Remove um doador", description = "Exclui o cadastro de um doador pelo ID")
    @ApiResponse(responseCode = "204", description = "Doador excluído com sucesso")
    @ApiResponse(responseCode = "404", description = "Doador não encontrado")
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do doador", example = "1")
            @PathVariable Long idDoador) {
        service.deletar(idDoador);
        return ResponseEntity.noContent().build();
    }
}
