package br.ueg.controleobras.dto;

import br.ueg.controleobras.dominio.StatusUsuario;
import br.ueg.controleobras.dominio.Tela;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UsuarioRequest(
        @NotBlank(message = "Nome é obrigatório") @Size(max = 120) String nome,
        @NotBlank(message = "Login é obrigatório") @Size(max = 60) String login,
        @Size(max = 60) String senha,
        @NotNull(message = "Status é obrigatório") StatusUsuario status,
        @NotEmpty(message = "Selecione ao menos uma tela") Set<Tela> telas) {
}
