package br.ueg.controleobras.web;

import br.ueg.controleobras.dominio.StatusPagamento;
import br.ueg.controleobras.dominio.TipoChavePix;
import br.ueg.controleobras.dto.PagamentoResponse;
import br.ueg.controleobras.repositorio.UsuarioRepository;
import br.ueg.controleobras.seguranca.JwtService;
import br.ueg.controleobras.servico.PagamentoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PagamentoController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(authorities = "TELA_PAGAMENTOS")
class PagamentoControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PagamentoService pagamentos;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UsuarioRepository usuarios;

    @Test
    void listaPagamentos() throws Exception {
        when(pagamentos.listar(null))
                .thenReturn(List.of(pagamento(StatusPagamento.PENDENTE)));

        mvc.perform(get("/api/pagamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].funcionario").value("Ana"))
                .andExpect(jsonPath("$[0].valorTotal").value(250.00))
                .andExpect(jsonPath("$[0].status").value("PENDENTE"));
    }

    @Test
    void filtraPagamentosPorStatus() throws Exception {
        when(pagamentos.listar(StatusPagamento.PAGO))
                .thenReturn(List.of(pagamento(StatusPagamento.PAGO)));

        mvc.perform(get("/api/pagamentos").param("status", "PAGO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PAGO"));

        verify(pagamentos).listar(StatusPagamento.PAGO);
    }

    @Test
    void alteraStatusDoPagamento() throws Exception {
        when(pagamentos.alterarStatus(1L, StatusPagamento.PAGO))
                .thenReturn(pagamento(StatusPagamento.PAGO));

        mvc.perform(patch("/api/pagamentos/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PAGO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAGO"));

        verify(pagamentos).alterarStatus(1L, StatusPagamento.PAGO);
    }

    @Test
    void rejeitaAlteracaoSemStatus() throws Exception {
        mvc.perform(patch("/api/pagamentos/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.status").value("Informe o status"));

        verify(pagamentos, never()).alterarStatus(anyLong(), any());
    }

    private static PagamentoResponse pagamento(StatusPagamento status) {
        return new PagamentoResponse(
                1L,
                1L,
                "Ana",
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 7),
                new BigDecimal("250.00"),
                TipoChavePix.EMAIL,
                "ana@obra.com",
                status);
    }
}
