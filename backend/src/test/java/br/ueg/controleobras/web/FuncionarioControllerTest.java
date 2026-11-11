package br.ueg.controleobras.web;

import br.ueg.controleobras.dominio.TipoChavePix;
import br.ueg.controleobras.dto.FuncionarioResponse;
import br.ueg.controleobras.erro.RecursoNaoEncontradoException;
import br.ueg.controleobras.repositorio.UsuarioRepository;
import br.ueg.controleobras.seguranca.JwtService;
import br.ueg.controleobras.servico.FuncionarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FuncionarioController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(authorities = "TELA_FUNCIONARIOS")
class FuncionarioControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FuncionarioService funcionarios;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UsuarioRepository usuarios;

    @Test
    void listaFuncionarios() throws Exception {
        when(funcionarios.listar(null)).thenReturn(List.of(funcionario()));

        mvc.perform(get("/api/funcionarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("João"))
                .andExpect(jsonPath("$[0].cpf").value("52998224725"));
    }

    @Test
    void criaFuncionario() throws Exception {
        when(funcionarios.criar(any())).thenReturn(funcionario());

        mvc.perform(post("/api/funcionarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "João",
                                  "telefone": "(11) 91234-5678",
                                  "funcao": "Pedreiro",
                                  "valorDiaria": 150.00,
                                  "tipoChavePix": "CPF",
                                  "chavePix": "529.982.247-25",
                                  "cpf": "529.982.247-25"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("João"));
    }

    @Test
    void rejeitaCadastroComCamposObrigatoriosVazios() throws Exception {
        mvc.perform(post("/api/funcionarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Verifique os campos informados"))
                .andExpect(jsonPath("$.campos.nome").value("Nome é obrigatório"))
                .andExpect(jsonPath("$.campos.cpf").value("CPF é obrigatório"))
                .andExpect(jsonPath("$.campos.valorDiaria").value("Valor da diária é obrigatório"));

        verify(funcionarios, never()).criar(any());
    }

    @Test
    void retornaNaoEncontradoAoBuscarFuncionarioInexistente() throws Exception {
        when(funcionarios.buscar(99L))
                .thenThrow(new RecursoNaoEncontradoException("Funcionário não encontrado"));

        mvc.perform(get("/api/funcionarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Funcionário não encontrado"));
    }

    @Test
    void excluiFuncionario() throws Exception {
        mvc.perform(delete("/api/funcionarios/1"))
                .andExpect(status().isNoContent());

        verify(funcionarios).excluir(1L);
    }

    private static FuncionarioResponse funcionario() {
        return new FuncionarioResponse(
                1L,
                "João",
                "11912345678",
                "Pedreiro",
                new BigDecimal("150.00"),
                TipoChavePix.CPF,
                "52998224725",
                "52998224725");
    }
}
