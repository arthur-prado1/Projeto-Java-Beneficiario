package br.com.socialconnect.api.doacoes.service;

import br.com.socialconnect.api.doacoes.dto.DoacaoRequestDTO;
import br.com.socialconnect.api.doacoes.dto.DoacaoResponseDTO;
import br.com.socialconnect.api.doacoes.model.Doacao;
import br.com.socialconnect.api.doacoes.model.TipoDoacao;
import br.com.socialconnect.api.doacoes.repository.DoacaoRepository;
import br.com.socialconnect.api.doadores.repository.DoadorRepository;
import br.com.socialconnect.api.exception.RecursoNaoEncontradoException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DoacaoService {

    private final DoacaoRepository repository;
    private final DoadorRepository doadorRepository;

    public DoacaoService(DoacaoRepository repository, DoadorRepository doadorRepository) {
        this.repository = repository;
        this.doadorRepository = doadorRepository;
    }

    @Transactional
    public DoacaoResponseDTO criar(DoacaoRequestDTO dto) {
        doadorRepository.findById(dto.idDoador())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Doador não encontrado com o ID: " + dto.idDoador()));

        Doacao doacao = Doacao.builder()
                .idDoador(dto.idDoador())
                .dataDoacao(dto.dataDoacao() != null ? dto.dataDoacao() : LocalDate.now())
                .valor(dto.valor())
                .tipo(dto.tipo())
                .descricao(dto.descricao())
                .build();
        doacao = repository.save(doacao);
        return toDTO(doacao);
    }

    @Transactional
    public DoacaoResponseDTO salvar(DoacaoRequestDTO dto) {
        return criar(dto);
    }

    public Page<DoacaoResponseDTO> listar(
            LocalDate dataInicio, LocalDate dataFim, TipoDoacao tipo, Pageable pageable) {

        Page<Doacao> page;

        if (dataInicio != null && dataFim != null && tipo != null) {
            page = repository.findByDataDoacaoBetweenAndTipo(dataInicio, dataFim, tipo, pageable);
        } else if (dataInicio != null && dataFim != null) {
            page = repository.findByDataDoacaoBetween(dataInicio, dataFim, pageable);
        } else if (tipo != null) {
            page = repository.findByTipo(tipo, pageable);
        } else {
            page = repository.findAll(pageable);
        }

        return page.map(this::toDTO);
    }

    public DoacaoResponseDTO buscarPorId(Long idDoacao) {
        return repository.findById(idDoacao)
                .map(this::toDTO)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Doação não encontrada com o ID: " + idDoacao));
    }

    @Transactional
    public void deletar(Long idDoacao) {
        if (!repository.existsById(idDoacao)) {
            throw new RecursoNaoEncontradoException("Doação não encontrada com o ID: " + idDoacao);
        }
        repository.deleteById(idDoacao);
    }

    private DoacaoResponseDTO toDTO(Doacao entity) {
        return new DoacaoResponseDTO(
                entity.getIdDoacao(),
                entity.getIdDoador(),
                entity.getDataDoacao(),
                entity.getValor(),
                entity.getTipo(),
                entity.getDescricao()
        );
    }
}
