package br.com.socialconnect.api.produtos.service;

import br.com.socialconnect.api.exception.EstoqueNegativoException;
import br.com.socialconnect.api.exception.ProdutoNomeDuplicadoException;
import br.com.socialconnect.api.exception.RecursoNaoEncontradoException;
import br.com.socialconnect.api.produtos.dto.ProdutoRequestDTO;
import br.com.socialconnect.api.produtos.dto.ProdutoResponseDTO;
import br.com.socialconnect.api.produtos.model.CategoriaProduto;
import br.com.socialconnect.api.produtos.model.Produto;
import br.com.socialconnect.api.produtos.repository.ProdutoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoServiceImpl(ProdutoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> listar(String nome, CategoriaProduto categoria, Pageable pageable) {
        Page<Produto> pagina;
        boolean hasNome = nome != null && !nome.isBlank();
        boolean hasCategoria = categoria != null;

        if (hasNome && hasCategoria) {
            pagina = repository.findByNomeContainingIgnoreCaseAndCategoria(nome.trim(), categoria, pageable);
        } else if (hasNome) {
            pagina = repository.findByNomeContainingIgnoreCase(nome.trim(), pageable);
        } else if (hasCategoria) {
            pagina = repository.findByCategoria(categoria, pageable);
        } else {
            pagina = repository.findAll(pageable);
        }

        return pagina.map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoResponseDTO buscarPorId(Long id) {
        return repository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado com o ID: " + id));
    }

    @Override
    @Transactional
    public ProdutoResponseDTO criar(ProdutoRequestDTO dto) {
        validarEstoque(dto);

        if (repository.existsByNome(dto.nome().trim())) {
            throw new ProdutoNomeDuplicadoException(dto.nome().trim());
        }

        Produto produto = Produto.builder()
                .nome(dto.nome().trim())
                .categoria(dto.categoria())
                .estoqueAtual(dto.estoqueAtual())
                .estoqueMinimo(dto.estoqueMinimo())
                .unidadeMedida(dto.unidadeMedida().trim())
                .dataCadastro(LocalDate.now())
                .build();

        Produto salvo = repository.save(produto);
        return toResponseDTO(salvo);
    }

    @Override
    @Transactional
    public ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO dto) {
        Produto produtoExistente = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado com o ID: " + id));

        validarEstoque(dto);

        String nomeTrimmed = dto.nome().trim();
        if (repository.existsByNomeAndIdProdutoNot(nomeTrimmed, id)) {
            throw new ProdutoNomeDuplicadoException(nomeTrimmed);
        }

        produtoExistente.setNome(nomeTrimmed);
        produtoExistente.setCategoria(dto.categoria());
        produtoExistente.setEstoqueAtual(dto.estoqueAtual());
        produtoExistente.setEstoqueMinimo(dto.estoqueMinimo());
        produtoExistente.setUnidadeMedida(dto.unidadeMedida().trim());

        Produto salvo = repository.save(produtoExistente);
        return toResponseDTO(salvo);
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Produto não encontrado com o ID: " + id);
        }
        repository.deleteById(id);
    }

    private void validarEstoque(ProdutoRequestDTO dto) {
        if (dto.estoqueAtual() != null && dto.estoqueAtual() < 0) {
            throw new EstoqueNegativoException("O estoque atual não pode ser negativo");
        }
        if (dto.estoqueMinimo() != null && dto.estoqueMinimo() < 0) {
            throw new EstoqueNegativoException("O estoque mínimo não pode ser negativo");
        }
    }

    private ProdutoResponseDTO toResponseDTO(Produto produto) {
        boolean estoqueBaixo = produto.getEstoqueAtual() != null
                && produto.getEstoqueMinimo() != null
                && produto.getEstoqueAtual() < produto.getEstoqueMinimo();

        return new ProdutoResponseDTO(
                produto.getIdProduto(),
                produto.getNome(),
                produto.getCategoria(),
                produto.getEstoqueAtual(),
                produto.getEstoqueMinimo(),
                produto.getUnidadeMedida(),
                produto.getDataCadastro(),
                estoqueBaixo
        );
    }
}
