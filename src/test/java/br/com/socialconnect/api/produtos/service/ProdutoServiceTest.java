package br.com.socialconnect.api.produtos.service;

import br.com.socialconnect.api.exception.EstoqueNegativoException;
import br.com.socialconnect.api.exception.ProdutoNomeDuplicadoException;
import br.com.socialconnect.api.exception.RecursoNaoEncontradoException;
import br.com.socialconnect.api.produtos.dto.ProdutoRequestDTO;
import br.com.socialconnect.api.produtos.dto.ProdutoResponseDTO;
import br.com.socialconnect.api.produtos.model.CategoriaProduto;
import br.com.socialconnect.api.produtos.model.Produto;
import br.com.socialconnect.api.produtos.repository.ProdutoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository repository;

    @InjectMocks
    private ProdutoServiceImpl service;

    @Test
    @DisplayName("Deve criar produto quando dados válidos")
    void deveCriarProdutoQuandoDadosValidos() {
        // ==========================================
        // ARRANGE: Preparar o cenário
        // ==========================================
        ProdutoRequestDTO dto = new ProdutoRequestDTO(
                "Arroz 5kg",
                CategoriaProduto.ALIMENTO,
                15,
                10,
                "unidade"
        );

        Produto produtoSalvo = Produto.builder()
                .idProduto(1L)
                .nome("Arroz 5kg")
                .categoria(CategoriaProduto.ALIMENTO)
                .estoqueAtual(15)
                .estoqueMinimo(10)
                .unidadeMedida("unidade")
                .dataCadastro(LocalDate.now())
                .build();

        Mockito.when(repository.existsByNome("Arroz 5kg")).thenReturn(false);
        Mockito.when(repository.save(Mockito.any(Produto.class))).thenReturn(produtoSalvo);

        // ==========================================
        // ACT: Executar a ação
        // ==========================================
        ProdutoResponseDTO resultado = service.criar(dto);

        // ==========================================
        // ASSERT: Verificar o resultado
        // ==========================================
        Assertions.assertNotNull(resultado);
        Assertions.assertEquals(1L, resultado.idProduto());
        Assertions.assertEquals("Arroz 5kg", resultado.nome());
        Assertions.assertEquals(CategoriaProduto.ALIMENTO, resultado.categoria());
        Assertions.assertEquals(15, resultado.estoqueAtual());
        Assertions.assertEquals(10, resultado.estoqueMinimo());
        Assertions.assertEquals("unidade", resultado.unidadeMedida());
        Assertions.assertFalse(resultado.estoqueBaixo(), "15 >= 10, então estoqueBaixo deve ser false");
        Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(Produto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando estoque negativo")
    void deveLancarExcecaoQuandoEstoqueNegativo() {
        // ==========================================
        // ARRANGE: Preparar o cenário
        // ==========================================
        ProdutoRequestDTO dto = new ProdutoRequestDTO(
                "Feijão 1kg",
                CategoriaProduto.ALIMENTO,
                -5,
                10,
                "kg"
        );

        // ==========================================
        // ACT & ASSERT: Executar e verificar exceção
        // ==========================================
        Assertions.assertThrows(EstoqueNegativoException.class, () -> {
            service.criar(dto);
        });

        Mockito.verify(repository, Mockito.never()).save(Mockito.any(Produto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome duplicado")
    void deveLancarExcecaoQuandoNomeDuplicado() {
        // ==========================================
        // ARRANGE: Preparar o cenário
        // ==========================================
        ProdutoRequestDTO dto = new ProdutoRequestDTO(
                "Arroz 5kg",
                CategoriaProduto.ALIMENTO,
                20,
                5,
                "unidade"
        );

        Mockito.when(repository.existsByNome("Arroz 5kg")).thenReturn(true);

        // ==========================================
        // ACT & ASSERT: Executar e verificar exceção
        // ==========================================
        Assertions.assertThrows(ProdutoNomeDuplicadoException.class, () -> {
            service.criar(dto);
        });

        Mockito.verify(repository, Mockito.never()).save(Mockito.any(Produto.class));
    }

    @Test
    @DisplayName("Deve calcular estoqueBaixo como true quando estoqueAtual < estoqueMinimo")
    void deveCalcularEstoqueBaixoComoTrueQuandoEstoqueMenorQueMinimo() {
        // ==========================================
        // ARRANGE: Preparar o cenário
        // ==========================================
        ProdutoRequestDTO dto = new ProdutoRequestDTO(
                "Sabonete 90g",
                CategoriaProduto.HIGIENE,
                3,
                10,
                "unidade"
        );

        Produto produtoSalvo = Produto.builder()
                .idProduto(2L)
                .nome("Sabonete 90g")
                .categoria(CategoriaProduto.HIGIENE)
                .estoqueAtual(3)
                .estoqueMinimo(10)
                .unidadeMedida("unidade")
                .dataCadastro(LocalDate.now())
                .build();

        Mockito.when(repository.existsByNome("Sabonete 90g")).thenReturn(false);
        Mockito.when(repository.save(Mockito.any(Produto.class))).thenReturn(produtoSalvo);

        // ==========================================
        // ACT: Executar a ação
        // ==========================================
        ProdutoResponseDTO resultado = service.criar(dto);

        // ==========================================
        // ASSERT: Verificar o resultado
        // ==========================================
        Assertions.assertNotNull(resultado);
        Assertions.assertTrue(resultado.estoqueBaixo(), "3 < 10, então estoqueBaixo deve ser true");
    }

    @Test
    @DisplayName("Deve buscar produto por ID com sucesso")
    void deveBuscarProdutoPorIdQuandoExistir() {
        // ==========================================
        // ARRANGE: Preparar o cenário
        // ==========================================
        Produto produto = Produto.builder()
                .idProduto(1L)
                .nome("Camiseta Básica M")
                .categoria(CategoriaProduto.ROUPA)
                .estoqueAtual(50)
                .estoqueMinimo(20)
                .unidadeMedida("unidade")
                .dataCadastro(LocalDate.of(2026, 9, 18))
                .build();

        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(produto));

        // ==========================================
        // ACT: Executar a ação
        // ==========================================
        ProdutoResponseDTO resultado = service.buscarPorId(1L);

        // ==========================================
        // ASSERT: Verificar o resultado
        // ==========================================
        Assertions.assertNotNull(resultado);
        Assertions.assertEquals(1L, resultado.idProduto());
        Assertions.assertEquals("Camiseta Básica M", resultado.nome());
        Assertions.assertEquals(CategoriaProduto.ROUPA, resultado.categoria());
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao buscar ID inexistente")
    void deveLancarRecursoNaoEncontradoQuandoProdutoNaoExistir() {
        // ==========================================
        // ARRANGE: Preparar o cenário
        // ==========================================
        Mockito.when(repository.findById(999L)).thenReturn(Optional.empty());

        // ==========================================
        // ACT & ASSERT: Executar e verificar exceção
        // ==========================================
        Assertions.assertThrows(RecursoNaoEncontradoException.class, () -> {
            service.buscarPorId(999L);
        });
    }

    @Test
    @DisplayName("Deve atualizar produto com sucesso quando dados válidos")
    void deveAtualizarProdutoQuandoDadosValidos() {
        // ==========================================
        // ARRANGE: Preparar o cenário
        // ==========================================
        Produto produtoExistente = Produto.builder()
                .idProduto(1L)
                .nome("Arroz 1kg")
                .categoria(CategoriaProduto.ALIMENTO)
                .estoqueAtual(10)
                .estoqueMinimo(5)
                .unidadeMedida("unidade")
                .dataCadastro(LocalDate.of(2026, 9, 1))
                .build();

        ProdutoRequestDTO dtoAtualizacao = new ProdutoRequestDTO(
                "Arroz 5kg",
                CategoriaProduto.ALIMENTO,
                25,
                10,
                "pacote"
        );

        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(produtoExistente));
        Mockito.when(repository.existsByNomeAndIdProdutoNot("Arroz 5kg", 1L)).thenReturn(false);
        Mockito.when(repository.save(Mockito.any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ==========================================
        // ACT: Executar a ação
        // ==========================================
        ProdutoResponseDTO resultado = service.atualizar(1L, dtoAtualizacao);

        // ==========================================
        // ASSERT: Verificar o resultado
        // ==========================================
        Assertions.assertNotNull(resultado);
        Assertions.assertEquals("Arroz 5kg", resultado.nome());
        Assertions.assertEquals(25, resultado.estoqueAtual());
        Assertions.assertEquals("pacote", resultado.unidadeMedida());
        Assertions.assertFalse(resultado.estoqueBaixo());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar se nome já pertencer a outro produto")
    void deveLancarExcecaoAoAtualizarQuandoNomeDuplicado() {
        // ==========================================
        // ARRANGE: Preparar o cenário
        // ==========================================
        Produto produtoExistente = Produto.builder()
                .idProduto(1L)
                .nome("Arroz 1kg")
                .categoria(CategoriaProduto.ALIMENTO)
                .estoqueAtual(10)
                .estoqueMinimo(5)
                .unidadeMedida("unidade")
                .dataCadastro(LocalDate.now())
                .build();

        ProdutoRequestDTO dtoAtualizacao = new ProdutoRequestDTO(
                "Feijão Preto",
                CategoriaProduto.ALIMENTO,
                25,
                10,
                "kg"
        );

        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(produtoExistente));
        Mockito.when(repository.existsByNomeAndIdProdutoNot("Feijão Preto", 1L)).thenReturn(true);

        // ==========================================
        // ACT & ASSERT: Executar e verificar exceção
        // ==========================================
        Assertions.assertThrows(ProdutoNomeDuplicadoException.class, () -> {
            service.atualizar(1L, dtoAtualizacao);
        });

        Mockito.verify(repository, Mockito.never()).save(Mockito.any(Produto.class));
    }

    @Test
    @DisplayName("Deve remover produto quando ID existir")
    void deveDeletarProdutoQuandoExistir() {
        // ==========================================
        // ARRANGE: Preparar o cenário
        // ==========================================
        Mockito.when(repository.existsById(1L)).thenReturn(true);

        // ==========================================
        // ACT: Executar a ação
        // ==========================================
        service.deletar(1L);

        // ==========================================
        // ASSERT: Verificar o resultado
        // ==========================================
        Mockito.verify(repository, Mockito.times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao deletar ID inexistente")
    void deveLancarRecursoNaoEncontradoAoDeletarInexistente() {
        // ==========================================
        // ARRANGE: Preparar o cenário
        // ==========================================
        Mockito.when(repository.existsById(999L)).thenReturn(false);

        // ==========================================
        // ACT & ASSERT: Executar e verificar exceção
        // ==========================================
        Assertions.assertThrows(RecursoNaoEncontradoException.class, () -> {
            service.deletar(999L);
        });

        Mockito.verify(repository, Mockito.never()).deleteById(Mockito.anyLong());
    }

    @Test
    @DisplayName("Deve listar produtos com paginação e filtro por nome e categoria")
    void deveListarProdutosComPaginacaoEFiltros() {
        // ==========================================
        // ARRANGE: Preparar o cenário
        // ==========================================
        Pageable pageable = PageRequest.of(0, 10);
        Produto p1 = Produto.builder()
                .idProduto(1L)
                .nome("Arroz Branco")
                .categoria(CategoriaProduto.ALIMENTO)
                .estoqueAtual(10)
                .estoqueMinimo(5)
                .unidadeMedida("kg")
                .dataCadastro(LocalDate.now())
                .build();

        Page<Produto> pagina = new PageImpl<>(List.of(p1), pageable, 1);
        Mockito.when(repository.findByNomeContainingIgnoreCaseAndCategoria("Arroz", CategoriaProduto.ALIMENTO, pageable))
                .thenReturn(pagina);

        // ==========================================
        // ACT: Executar a ação
        // ==========================================
        Page<ProdutoResponseDTO> resultado = service.listar("Arroz", CategoriaProduto.ALIMENTO, pageable);

        // ==========================================
        // ASSERT: Verificar o resultado
        // ==========================================
        Assertions.assertNotNull(resultado);
        Assertions.assertEquals(1, resultado.getTotalElements());
        Assertions.assertEquals("Arroz Branco", resultado.getContent().get(0).nome());
    }
}
