package br.ueg.controleobras.dto;

import br.ueg.controleobras.dominio.Tela;

import java.util.Set;

public record SessaoResponse(String nome, String login, Set<Tela> telas) {
}
