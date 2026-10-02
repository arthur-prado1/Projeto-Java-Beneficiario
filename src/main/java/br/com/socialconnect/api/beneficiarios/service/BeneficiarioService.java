package br.com.socialconnect.api.beneficiarios.service;

import br.com.socialconnect.api.beneficiarios.dto.BeneficiarioPatchDTO;
import br.com.socialconnect.api.beneficiarios.dto.BeneficiarioRequestDTO;
import br.com.socialconnect.api.beneficiarios.dto.BeneficiarioResponseDTO;
import br.com.socialconnect.api.beneficiarios.model.Beneficiario;
import br.com.socialconnect.api.beneficiarios.repository.BeneficiarioRepository;
import br.com.socialconnect.api.exception.CpfDuplicadoException;
import br.com.socialconnect.api.exception.RecursoNaoEncontradoException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BeneficiarioService {

    private final BeneficiarioRepository repository;

    public BeneficiarioService(BeneficiarioRepository repository) {
        this.repository = repository;
    }

    public Page<BeneficiarioResponseDTO> listar(String nome, String cpf, Pageable pageable) {
        Page<Beneficiario> page;
        if (cpf != null && !cpf.isBlank()) {
            page = repository.findByCpf(cpf, pageable);
        } else if (nome != null && !nome.isBlank()) {
            page = repository.findByNomeContainingIgnoreCase(nome, pageable);
        } else {
            page = repository.findAll(pageable);
        }
        return page.map(this::toResponseDTO);
    }

    public List<BeneficiarioResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public BeneficiarioResponseDTO buscarPorId(Long idBeneficiario) {
        return repository.findById(idBeneficiario)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Beneficiário não encontrado com o ID: " + idBeneficiario));
    }

    @Transactional
    public BeneficiarioResponseDTO criar(BeneficiarioRequestDTO dto) {
        if (repository.existsByCpf(dto.cpf())) {
            throw new CpfDuplicadoException(dto.cpf());
        }
        Beneficiario entity = Beneficiario.builder()
                .nome(dto.nome())
                .cpf(dto.cpf())
                .telefone(dto.telefone())
                .endereco(dto.endereco())
                .situacaoVulnerabilidade(dto.situacaoVulnerabilidade())
                .dataCadastro(LocalDate.now())
                .build();
        return toResponseDTO(repository.save(entity));
    }

    @Transactional
    public BeneficiarioResponseDTO salvar(BeneficiarioRequestDTO dto) {
        return criar(dto);
    }

    @Transactional
    public BeneficiarioResponseDTO atualizar(Long idBeneficiario, BeneficiarioRequestDTO dto) {
        Beneficiario entity = repository.findById(idBeneficiario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Beneficiário não encontrado com o ID: " + idBeneficiario));

        if (!entity.getCpf().equals(dto.cpf()) && repository.existsByCpf(dto.cpf())) {
            throw new CpfDuplicadoException(dto.cpf());
        }

        entity.setNome(dto.nome());
        entity.setCpf(dto.cpf());
        entity.setTelefone(dto.telefone());
        entity.setEndereco(dto.endereco());
        entity.setSituacaoVulnerabilidade(dto.situacaoVulnerabilidade());

        return toResponseDTO(repository.save(entity));
    }

    @Transactional
    public BeneficiarioResponseDTO atualizarParcial(Long idBeneficiario, BeneficiarioPatchDTO dto) {
        Beneficiario entity = repository.findById(idBeneficiario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Beneficiário não encontrado com o ID: " + idBeneficiario));

        if (dto.nome() != null && !dto.nome().isBlank()) {
            entity.setNome(dto.nome());
        }
        if (dto.telefone() != null && !dto.telefone().isBlank()) {
            entity.setTelefone(dto.telefone());
        }
        if (dto.endereco() != null && !dto.endereco().isBlank()) {
            entity.setEndereco(dto.endereco());
        }
        if (dto.situacaoVulnerabilidade() != null && !dto.situacaoVulnerabilidade().isBlank()) {
            entity.setSituacaoVulnerabilidade(dto.situacaoVulnerabilidade());
        }

        return toResponseDTO(repository.save(entity));
    }

    @Transactional
    public BeneficiarioResponseDTO atualizarContato(Long idBeneficiario, BeneficiarioPatchDTO dto) {
        return atualizarParcial(idBeneficiario, dto);
    }

    @Transactional
    public void deletar(Long idBeneficiario) {
        if (!repository.existsById(idBeneficiario)) {
            throw new RecursoNaoEncontradoException("Beneficiário não encontrado com o ID: " + idBeneficiario);
        }
        repository.deleteById(idBeneficiario);
    }

    private BeneficiarioResponseDTO toResponseDTO(Beneficiario e) {
        return new BeneficiarioResponseDTO(
                e.getIdBeneficiario(),
                e.getNome(),
                e.getCpf(),
                e.getTelefone(),
                e.getEndereco(),
                e.getSituacaoVulnerabilidade(),
                e.getDataCadastro()
        );
    }
}