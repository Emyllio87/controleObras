package br.ueg.controleobras.model;

import br.ueg.controleobras.model.StatusUsuario;
import br.ueg.controleobras.model.Tela;
import br.ueg.controleobras.model.Usuario;

import java.util.Set;

public record UsuarioResponse(Long id, String nome, String login, StatusUsuario status, Set<Tela> telas) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getLogin(),
                usuario.getStatus(), usuario.getTelas());
    }
}
