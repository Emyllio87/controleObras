package br.ueg.controleobras.service;

import br.ueg.controleobras.exception.RegraNegocioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ArquivoService {

    private static final Set<String> EXTENSOES_PERMITIDAS = Set.of("jpg", "jpeg", "png", "pdf");
    private static final Map<String, String> TIPOS = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "pdf", "application/pdf");

    private final Path diretorio;
    private final long tamanhoMaximo;

    public ArquivoService(@Value("${app.arquivos.diretorio}") String diretorio,
                          @Value("${app.arquivos.tamanho-maximo}") long tamanhoMaximo) {
        this.diretorio = Path.of(diretorio).toAbsolutePath().normalize();
        this.tamanhoMaximo = tamanhoMaximo;
        try {
            Files.createDirectories(this.diretorio);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível criar o diretório de anexos", e);
        }
    }

    public ArquivoArmazenado salvar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            return null;
        }
        if (arquivo.getSize() > tamanhoMaximo) {
            throw new RegraNegocioException("Arquivo excede o limite de " + tamanhoMaximo / (1024 * 1024) + " MB");
        }
        String nomeOriginal = StringUtils.cleanPath(
                arquivo.getOriginalFilename() == null ? "arquivo" : arquivo.getOriginalFilename());
        String extensao = extensao(nomeOriginal);
        if (!EXTENSOES_PERMITIDAS.contains(extensao)) {
            throw new RegraNegocioException("Envie um arquivo JPG, PNG ou PDF");
        }
        String nomeArmazenado = UUID.randomUUID() + "." + extensao;
        try {
            arquivo.transferTo(diretorio.resolve(nomeArmazenado));
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao gravar o anexo", e);
        }
        return new ArquivoArmazenado(nomeOriginal, nomeArmazenado, TIPOS.get(extensao), arquivo.getSize());
    }

    public void excluir(String nomeArmazenado) {
        if (nomeArmazenado == null || nomeArmazenado.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolver(nomeArmazenado));
        } catch (IOException ignored) {
            // o registro pode ser removido mesmo que o arquivo já não exista mais
        }
    }

    public Resource carregar(String nomeArmazenado) {
        try {
            return new UrlResource(resolver(nomeArmazenado).toUri());
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Anexo indisponível", e);
        }
    }

    private Path resolver(String nomeArmazenado) {
        Path caminho = diretorio.resolve(nomeArmazenado).normalize();
        if (!caminho.startsWith(diretorio)) {
            throw new RegraNegocioException("Anexo inválido");
        }
        return caminho;
    }

    private String extensao(String nome) {
        int ponto = nome.lastIndexOf('.');
        return ponto < 0 ? "" : nome.substring(ponto + 1).toLowerCase(Locale.ROOT);
    }
}
