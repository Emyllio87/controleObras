package br.ueg.controleobras.model;

import br.ueg.controleobras.model.Tela;

import java.util.Set;

public record LoginResponse(String token, String nome, String login, Set<Tela> telas) {
}
