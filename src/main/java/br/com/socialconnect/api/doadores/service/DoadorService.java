package br.com.socialconnect.api.doadores.service;

import br.com.socialconnect.api.doadores.dto.DoadorRequestDTO;
import br.com.socialconnect.api.doadores.dto.DoadorResponseDTO;
import br.com.socialconnect.api.doadores.model.Doador;
import br.com.socialconnect.api.doadores.repository.DoadorRepository;
import br.com.socialconnect.api.exception.RecursoNaoEncontradoException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DoadorService {

    private final DoadorRepository repository;

    public DoadorService(DoadorRepository repository) {
        this.repository = repository;
    }

    public Page<DoadorResponseDTO> listar(String nome, Pageable pageable) {
        Page<Doador> page;
        if (nome != null && !nome.isBlank()) {
            page = repository.findByNomeContainingIgnoreCase(nome, pageable);
        } else {
            page = repository.findAll(pageable);
        }
        return page.map(this::toDTO);
    }

    public List<DoadorResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public DoadorResponseDTO buscarPorId(Long idDoador) {
        return repository.findById(idDoador)
                .map(this::toDTO)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Doador não encontrado com o ID: " + idDoador));
    }

    @Transactional
    public DoadorResponseDTO criar(DoadorRequestDTO dto) {
        Doador entity = Doador.builder()
                .nome(dto.nome())
                .tipo(dto.tipo())
                .email(dto.email())
                .telefone(dto.telefone())
                .build();
        return toDTO(repository.save(entity));
    }

    @Transactional
    public DoadorResponseDTO atualizar(Long idDoador, DoadorRequestDTO dto) {
        Doador entity = repository.findById(idDoador)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Doador não encontrado com o ID: " + idDoador));

        entity.setNome(dto.nome());
        entity.setTipo(dto.tipo());
        entity.setEmail(dto.email());
        entity.setTelefone(dto.telefone());

        return toDTO(repository.save(entity));
    }

    @Transactional
    public void deletar(Long idDoador) {
        if (!repository.existsById(idDoador)) {
            throw new RecursoNaoEncontradoException("Doador não encontrado com o ID: " + idDoador);
        }
        repository.deleteById(idDoador);
    }

    private DoadorResponseDTO toDTO(Doador entity) {
        return new DoadorResponseDTO(
                entity.getIdDoador(),
                entity.getNome(),
                entity.getTipo(),
                entity.getEmail(),
                entity.getTelefone()
        );
    }
}
