package br.com.socialconnect.api.doacoes.service;

import br.com.socialconnect.api.doacoes.dto.DoacaoRequestDTO;
import br.com.socialconnect.api.doacoes.dto.DoacaoResponseDTO;
import br.com.socialconnect.api.doacoes.model.Doacao;
import br.com.socialconnect.api.doacoes.model.TipoDoacao;
import br.com.socialconnect.api.doacoes.repository.DoacaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DoacaoService {

    private final DoacaoRepository repository;

    public DoacaoService(DoacaoRepository repository) {
        this.repository = repository;
    }

    public DoacaoResponseDTO salvar(DoacaoRequestDTO dto) {
        Doacao doacao = Doacao.builder()
                .idDoador(dto.idDoador())
                .dataDoacao(dto.dataDoacao() != null ? dto.dataDoacao() : LocalDate.now())
                .valor(dto.valor())
                .tipo(dto.tipo())
                .build();
        doacao = repository.save(doacao);
        return toDTO(doacao);
    }

    public Page<DoacaoResponseDTO> listar(
            LocalDate dataInicio, LocalDate dataFim, TipoDoacao tipo, Pageable pageable) {
        
        Page<Doacao> page;
        
        if (dataInicio != null && dataFim != null && tipo != null) {
            page = repository.findByDataDoacaoBetweenAndTipo(dataInicio, dataFim, tipo, pageable);
        } else {
            page = repository.findAll(pageable);
        }
        
        return page.map(this::toDTO);
    }
    
    private DoacaoResponseDTO toDTO(Doacao entity) {
        return new DoacaoResponseDTO(
            entity.getIdDoacao(), entity.getIdDoador(), entity.getDataDoacao(),
            entity.getValor(), entity.getTipo()
        );
    }
}
