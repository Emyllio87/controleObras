package br.ueg.controleobras.arquivo;

import org.springframework.core.io.Resource;

public record AnexoArquivo(String nome, String tipo, Resource recurso) {
}
