package br.ueg.controleobras.web;

import br.ueg.controleobras.config.JacksonConfig;
import br.ueg.controleobras.dto.FornecedorResponse;
import br.ueg.controleobras.dto.ItemResponse;
import br.ueg.controleobras.dto.PedidoResponse;
import br.ueg.controleobras.repositorio.UsuarioRepository;
import br.ueg.controleobras.seguranca.JwtService;
import br.ueg.controleobras.servico.PedidoService;
import br.ueg.controleobras.util.PdfGerado;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PedidoController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(authorities = "TELA_PEDIDOS_ORCAMENTO")
@Import(JacksonConfig.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PedidoService pedidos;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UsuarioRepository usuarios;

    @Test
    void listaPedidos() throws Exception {
        when(pedidos.listar()).thenReturn(List.of(pedido()));

        mvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numero").value(7))
                .andExpect(jsonPath("$[0].dataEmissao").value("10/02/2026"))
                .andExpect(jsonPath("$[0].fornecedor.razaoSocial").value("Construtora Alfa Ltda"))
                .andExpect(jsonPath("$[0].itens[0].id").value(11))
                .andExpect(jsonPath("$[0].itens[0].descricao").value("Cimento"))
                .andExpect(jsonPath("$[0].itens[0].quantidade").value(2.500))
                .andExpect(jsonPath("$[0].itens[0].unidade").value("SACO"));
    }

    @Test
    void criaPedido() throws Exception {
        when(pedidos.criar(any())).thenReturn(pedido());

        mvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fornecedorId": 1,
                                  "itens": [
                                    {"descricao": "Cimento", "quantidade": 2.5, "unidade": "saco"}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value(7));
    }

    @Test
    void rejeitaPedidoSemFornecedorSemItens() throws Exception {
        mvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.fornecedorId").value("Selecione o fornecedor"))
                .andExpect(jsonPath("$.campos.itens").value("Inclua ao menos um item"));

        verify(pedidos, never()).criar(any());
    }

    @Test
    void baixaPdfDoPedido() throws Exception {
        when(pedidos.gerarPdf(1L)).thenReturn(
                new PdfGerado("pedido-000007.pdf", new byte[]{1, 2, 3}));

        mvc.perform(get("/api/pedidos/1/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("pedido-000007.pdf")))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    private static PedidoResponse pedido() {
        FornecedorResponse fornecedor = new FornecedorResponse(
                1L, "Construtora Alfa Ltda", "11222333000181",
                "1133334444", "Maria", null, null);
        return new PedidoResponse(
                1L,
                7L,
                LocalDate.of(2026, 2, 10),
                fornecedor,
                List.of(new ItemResponse(11L, 1, "Cimento", new BigDecimal("2.500"), "SACO")));
    }
}
