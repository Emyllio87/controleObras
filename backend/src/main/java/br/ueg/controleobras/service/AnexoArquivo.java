package br.ueg.controleobras.service;

import org.springframework.core.io.Resource;

public record AnexoArquivo(String nome, String tipo, Resource recurso) {
}
