package br.ueg.controleobras.pdf;

import br.ueg.controleobras.dominio.AutorizacaoPagamento;
import br.ueg.controleobras.dominio.Fornecedor;
import br.ueg.controleobras.dominio.PedidoOrcamento;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RelatorioPdfServiceTest {

    private final RelatorioPdfService service = new RelatorioPdfService();

    @Test
    void semAnexoGeraUmaPagina() throws IOException {
        AutorizacaoPagamento autorizacao = autorizacao();
        autorizacao.setArquivoNome(null);
        autorizacao.setArquivoCaminho(null);
        autorizacao.setArquivoTipo(null);
        autorizacao.setArquivoTamanho(null);

        byte[] pdf = service.autorizacao(autorizacao, null, null);

        PdfReader leitor = new PdfReader(pdf);
        try {
            assertThat(leitor.getNumberOfPages()).isEqualTo(1);
            PdfDictionary recursos = leitor.getPageN(1).getAsDict(PdfName.RESOURCES);
            assertThat(recursos.getAsDict(PdfName.XOBJECT)).isNull();
        } finally {
            leitor.close();
        }
    }

    @Test
    void exibeImagemAnexadaNoPdf() throws IOException {
        byte[] pdf = service.autorizacao(autorizacao(), imagemPng(), "image/png");

        PdfReader leitor = new PdfReader(pdf);
        try {
            assertThat(leitor.getNumberOfPages()).isEqualTo(1);
            PdfDictionary recursos = leitor.getPageN(1).getAsDict(PdfName.RESOURCES);
            assertThat(recursos).isNotNull();
            assertThat(recursos.getAsDict(PdfName.XOBJECT)).isNotNull();
        } finally {
            leitor.close();
        }
    }

    @Test
    void acrescentaPaginasDoPdfAnexado() throws DocumentException, IOException {
        byte[] pdf = service.autorizacao(autorizacao(), pdfComPaginas(2), "application/pdf");

        PdfReader leitor = new PdfReader(pdf);
        try {
            assertThat(leitor.getNumberOfPages()).isEqualTo(3);
        } finally {
            leitor.close();
        }
    }

    private static byte[] imagemPng() throws IOException {
        BufferedImage imagem = new BufferedImage(1200, 800, BufferedImage.TYPE_INT_RGB);
        Graphics2D grafico = imagem.createGraphics();
        grafico.setColor(Color.BLUE);
        grafico.fillRect(0, 0, 1200, 800);
        grafico.dispose();

        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        ImageIO.write(imagem, "png", saida);
        return saida.toByteArray();
    }

    private static byte[] pdfComPaginas(int paginas) throws DocumentException {
        Document documento = new Document();
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        PdfWriter.getInstance(documento, saida);
        documento.open();
        for (int pagina = 1; pagina <= paginas; pagina++) {
            documento.add(new Paragraph("Anexo - página " + pagina));
            if (pagina < paginas) {
                documento.newPage();
            }
        }
        documento.close();
        return saida.toByteArray();
    }

    private static AutorizacaoPagamento autorizacao() {
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setRazaoSocial("Construtora Alfa Ltda");
        fornecedor.setCnpj("11222333000181");

        PedidoOrcamento pedido = new PedidoOrcamento();
        pedido.setNumero(7L);
        pedido.setDataEmissao(LocalDate.of(2026, 3, 1));
        pedido.setFornecedor(fornecedor);

        AutorizacaoPagamento autorizacao = new AutorizacaoPagamento();
        autorizacao.setNumero(10L);
        autorizacao.setPedido(pedido);
        autorizacao.setValor(new BigDecimal("100.00"));
        autorizacao.setDataEmissao(LocalDate.of(2026, 3, 4));
        autorizacao.setArquivoNome("anexo.png");
        autorizacao.setArquivoCaminho("anexo.png");
        autorizacao.setArquivoTipo("image/png");
        autorizacao.setArquivoTamanho(100L);
        return autorizacao;
    }
}
