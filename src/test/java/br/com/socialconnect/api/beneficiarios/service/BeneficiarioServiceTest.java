package br.com.socialconnect.api.beneficiarios.service;

import br.com.socialconnect.api.beneficiarios.dto.BeneficiarioRequestDTO;
import br.com.socialconnect.api.beneficiarios.dto.BeneficiarioResponseDTO;
import br.com.socialconnect.api.beneficiarios.model.Beneficiario;
import br.com.socialconnect.api.beneficiarios.repository.BeneficiarioRepository;
import br.com.socialconnect.api.exception.CpfDuplicadoException;
import br.com.socialconnect.api.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class BeneficiarioServiceTest {

    @Mock
    private BeneficiarioRepository repository;

    @InjectMocks
    private BeneficiarioService service;

    @Test
    @DisplayName("Deve criar beneficiário quando dados válidos")
    void deveCriarBeneficiarioQuandoDadosValidos() {
        // ==========================================
        // ARRANGE: Preparar o cenário
        // ==========================================
        BeneficiarioRequestDTO dto = new BeneficiarioRequestDTO(
                "Maria da Silva", "12345678900", "11999999999",
                "Rua das Flores, 123", "Renda familiar baixa"
        );
        Beneficiario beneficiarioSalvo = Beneficiario.builder()
                .idBeneficiario(1L)
                .nome("Maria da Silva")
                .cpf("12345678900")
                .telefone("11999999999")
                .endereco("Rua das Flores, 123")
                .situacaoVulnerabilidade("Renda familiar baixa")
                .dataCadastro(LocalDate.now())
                .build();

        Mockito.when(repository.existsByCpf("12345678900")).thenReturn(false);
        Mockito.when(repository.save(Mockito.any(Beneficiario.class))).thenReturn(beneficiarioSalvo);

        // ==========================================
        // ACT: Executar a ação
        // ==========================================
        BeneficiarioResponseDTO resultado = service.criar(dto);

        // ==========================================
        // ASSERT: Verificar o resultado
        // ==========================================
        Assertions.assertNotNull(resultado.idBeneficiario());
        Assertions.assertEquals("Maria da Silva", resultado.nome());
        Assertions.assertEquals("12345678900", resultado.cpf());
        Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(Beneficiario.class));
    }

    @Test
    @DisplayName("Deve lançar CpfDuplicadoException quando CPF já cadastrado")
    void deveLancarExcecaoQuandoCpfJaCadastrado() {
        // ==========================================
        // ARRANGE: Preparar o cenário
        // ==========================================
        BeneficiarioRequestDTO dto = new BeneficiarioRequestDTO(
                "Maria da Silva", "12345678900", "11999999999",
                "Rua das Flores, 123", "Renda familiar baixa"
        );
        Mockito.when(repository.existsByCpf("12345678900")).thenReturn(true);

        // ==========================================
        // ACT & ASSERT: Executar e verificar exceção
        // ==========================================
        Assertions.assertThrows(CpfDuplicadoException.class, () -> {
            service.criar(dto);
        });

        Mockito.verify(repository, Mockito.never()).save(Mockito.any(Beneficiario.class));
    }

    @Test
    @DisplayName("Deve buscar beneficiário por ID quando existir")
    void deveBuscarBeneficiarioPorIdQuandoExistir() {
        // ==========================================
        // ARRANGE: Preparar o cenário
        // ==========================================
        Beneficiario beneficiario = Beneficiario.builder()
                .idBeneficiario(1L)
                .nome("Maria da Silva")
                .cpf("12345678900")
                .dataCadastro(LocalDate.now())
                .build();
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(beneficiario));

        // ==========================================
        // ACT: Executar a ação
        // ==========================================
        BeneficiarioResponseDTO resultado = service.buscarPorId(1L);

        // ==========================================
        // ASSERT: Verificar o resultado
        // ==========================================
        Assertions.assertNotNull(resultado);
        Assertions.assertEquals(1L, resultado.idBeneficiario());
        Assertions.assertEquals("Maria da Silva", resultado.nome());
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException quando ID não existir")
    void deveLancarExcecaoQuandoIdNaoExistir() {
        // ==========================================
        // ARRANGE: Preparar o cenário
        // ==========================================
        Mockito.when(repository.findById(99L)).thenReturn(Optional.empty());

        // ==========================================
        // ACT & ASSERT: Executar e verificar exceção
        // ==========================================
        Assertions.assertThrows(RecursoNaoEncontradoException.class, () -> {
            service.buscarPorId(99L);
        });
    }
}
