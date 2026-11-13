package br.ueg.controleobras.service;

import br.ueg.controleobras.model.AutorizacaoPagamento;
import br.ueg.controleobras.model.Diaria;
import br.ueg.controleobras.model.FechamentoDiaria;
import br.ueg.controleobras.model.Ferramenta;
import br.ueg.controleobras.model.Fornecedor;
import br.ueg.controleobras.model.ItemPedido;
import br.ueg.controleobras.model.PedidoOrcamento;
import br.ueg.controleobras.model.TipoChavePix;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class RelatorioPdfService {

    private static final Locale BR = new Locale("pt", "BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color AZUL = new Color(35, 72, 104);

    private static final Font TITULO = new Font(Font.HELVETICA, 16, Font.BOLD);
    private static final Font SECAO = new Font(Font.HELVETICA, 11, Font.BOLD);
    private static final Font ROTULO = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font TEXTO = new Font(Font.HELVETICA, 10);
    private static final Font TEXTO_TABELA = new Font(Font.HELVETICA, 9);
    private static final Font CABECALHO_TABELA = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);

    public byte[] pedido(PedidoOrcamento pedido) {
        Document documento = new Document(PageSize.A4, 40, 40, 40, 40);
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(documento, saida);
            documento.open();
            titulo(documento, "Pedido de orçamento nº " + numero(pedido.getNumero()));
            campo(documento, "ID", identificador(pedido.getId()));
            campo(documento, "Data de emissão", data(pedido.getDataEmissao()));

            Fornecedor fornecedor = pedido.getFornecedor();
            secao(documento, "Fornecedor");
            campo(documento, "ID", identificador(fornecedor.getId()));
            campo(documento, "Razão social", fornecedor.getRazaoSocial());
            campo(documento, "CNPJ", cnpj(fornecedor.getCnpj()));
            campo(documento, "Telefone", telefone(fornecedor.getTelefone()));
            campo(documento, "Contato", fornecedor.getNomeContato());
            campo(documento, "Chave PIX", chavePix(fornecedor.getTipoChavePix(), fornecedor.getChavePix()));

            secao(documento, "Itens");
            PdfPTable tabela = tabela(new float[]{0.8f, 0.8f, 6f, 1.6f, 1.4f});
            tabela.addCell(cabecalho("ID"));
            tabela.addCell(cabecalho("Nº"));
            tabela.addCell(cabecalho("Descrição"));
            tabela.addCell(cabecalho("Quantidade"));
            tabela.addCell(cabecalho("Unidade"));
            for (ItemPedido item : pedido.getItens()) {
                tabela.addCell(celula(identificador(item.getId()), Element.ALIGN_CENTER));
                tabela.addCell(celula(String.valueOf(item.getNumero()), Element.ALIGN_CENTER));
                tabela.addCell(celula(item.getDescricao(), Element.ALIGN_LEFT));
                tabela.addCell(celula(quantidade(item.getQuantidade()), Element.ALIGN_RIGHT));
                tabela.addCell(celula(item.getUnidade(), Element.ALIGN_CENTER));
            }
            documento.add(tabela);
        } catch (DocumentException e) {
            throw new IllegalStateException("Falha ao gerar o PDF do pedido", e);
        } finally {
            documento.close();
        }
        return saida.toByteArray();
    }

    public byte[] autorizacao(AutorizacaoPagamento autorizacao, byte[] anexo, String tipoAnexo) {
        boolean anexoPdf = anexo != null && "application/pdf".equalsIgnoreCase(tipoAnexo);
        byte[] principal = autorizacaoBase(autorizacao, anexoPdf ? null : anexo);
        if (!anexoPdf) {
            return principal;
        }
        return juntarPdf(principal, anexo);
    }

    private byte[] autorizacaoBase(AutorizacaoPagamento autorizacao, byte[] imagemAnexo) {
        Document documento = new Document(PageSize.A4, 40, 40, 40, 40);
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        PedidoOrcamento pedido = autorizacao.getPedido();
        try {
            PdfWriter.getInstance(documento, saida);
            documento.open();
            titulo(documento, "Autorização de pagamento nº " + numero(autorizacao.getNumero()));
            campo(documento, "ID", identificador(autorizacao.getId()));
            campo(documento, "Data de emissão", data(autorizacao.getDataEmissao()));
            secao(documento, "Pedido");
            campo(documento, "ID do pedido", identificador(pedido.getId()));
            campo(documento, "Número do pedido", numero(pedido.getNumero()));
            campo(documento, "Fornecedor", pedido.getFornecedor().getRazaoSocial());
            campo(documento, "CNPJ", cnpj(pedido.getFornecedor().getCnpj()));

            secao(documento, "Valor");
            Paragraph valor = new Paragraph(moeda(autorizacao.getValor()), new Font(Font.HELVETICA, 13, Font.BOLD));
            valor.setSpacingAfter(8);
            documento.add(valor);

            secao(documento, "Anexo da Solicitação");
            if (autorizacao.getArquivoCaminho() == null) {
                campo(documento, "Documento", "Nenhum anexo");
            }
            if (imagemAnexo != null) {
                anexarImagem(documento, imagemAnexo);
            }
        } catch (DocumentException e) {
            throw new IllegalStateException("Falha ao gerar o PDF da autorização", e);
        } finally {
            documento.close();
        }
        return saida.toByteArray();
    }

    private void anexarImagem(Document documento, byte[] conteudo) throws DocumentException {
        try {
            Image imagem = Image.getInstance(conteudo);
            float largura = documento.getPageSize().getWidth() - documento.leftMargin() - documento.rightMargin() - 2f;
            float altura = documento.getPageSize().getHeight() - documento.topMargin() - documento.bottomMargin() - 60f;
            imagem.scaleToFit(largura, Math.max(altura, 1));
            imagem.setAlignment(Element.ALIGN_CENTER);

            Paragraph espaco = new Paragraph();
            espaco.setSpacingAfter(8);
            documento.add(espaco);
            documento.add(imagem);
        } catch (IOException e) {
            campo(documento, "Imagem", "Não foi possível exibir a imagem anexada");
        }
    }

    private byte[] juntarPdf(byte[] principal, byte[] anexo) {
        Document documento = new Document();
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        PdfReader leitorPrincipal = null;
        PdfReader leitorAnexo = null;
        PdfCopy copia = null;
        try {
            copia = new PdfCopy(documento, saida);
            documento.open();
            leitorPrincipal = new PdfReader(principal);
            copiarPaginas(copia, leitorPrincipal);
            leitorAnexo = new PdfReader(anexo);
            copiarPaginas(copia, leitorAnexo);
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Falha ao anexar o PDF à autorização", e);
        } finally {
            if (leitorPrincipal != null) {
                leitorPrincipal.close();
            }
            if (leitorAnexo != null) {
                leitorAnexo.close();
            }
            if (copia != null) {
                copia.close();
            } else {
                documento.close();
            }
        }
        return saida.toByteArray();
    }

    private void copiarPaginas(PdfCopy copia, PdfReader leitor) throws DocumentException, IOException {
        for (int pagina = 1; pagina <= leitor.getNumberOfPages(); pagina++) {
            copia.addPage(copia.getImportedPage(leitor, pagina));
        }
    }

    public byte[] almoxarifado(List<Ferramenta> ferramentas) {
        Document documento = new Document(PageSize.A4, 40, 40, 40, 40);
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(documento, saida);
            documento.open();
            titulo(documento, "Almoxarifado — relação de ferramentas");
            campo(documento, "Emitido em", data(LocalDate.now()));

            PdfPTable tabela = tabela(new float[]{0.8f, 6f, 2f, 1.6f});
            tabela.addCell(cabecalho("ID"));
            tabela.addCell(cabecalho("Ferramenta"));
            tabela.addCell(cabecalho("Quantidade"));
            tabela.addCell(cabecalho("Unidade"));
            for (Ferramenta ferramenta : ferramentas) {
                tabela.addCell(celula(identificador(ferramenta.getId()), Element.ALIGN_CENTER));
                tabela.addCell(celula(ferramenta.getNome(), Element.ALIGN_LEFT));
                tabela.addCell(celula(quantidade(ferramenta.getQuantidade()), Element.ALIGN_RIGHT));
                tabela.addCell(celula(ferramenta.getUnidade(), Element.ALIGN_CENTER));
            }
            documento.add(tabela);
        } catch (DocumentException e) {
            throw new IllegalStateException("Falha ao gerar o PDF do almoxarifado", e);
        } finally {
            documento.close();
        }
        return saida.toByteArray();
    }

    public byte[] fechamento(FechamentoDiaria fechamento) {
        Document documento = new Document(PageSize.A4, 40, 40, 40, 40);
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(documento, saida);
            documento.open();
            titulo(documento, "Fechamento de diárias");
            campo(documento, "ID", identificador(fechamento.getId()));
            campo(documento, "Funcionário", fechamento.getFuncionario().getNome());
            campo(documento, "ID do funcionário", identificador(fechamento.getFuncionario().getId()));
            campo(documento, "CPF", cpf(fechamento.getFuncionario().getCpf()));
            campo(documento, "Função", fechamento.getFuncionario().getFuncao());
            campo(documento, "Período", data(fechamento.getPeriodoInicio()) + " a " + data(fechamento.getPeriodoFim()));
            campo(documento, "Data do fechamento", data(fechamento.getDataFechamento()));

            secao(documento, "Diárias lançadas");
            PdfPTable tabela = tabela(new float[]{0.8f, 4f, 2f});
            tabela.addCell(cabecalho("ID"));
            tabela.addCell(cabecalho("Data"));
            tabela.addCell(cabecalho("Valor"));
            for (Diaria diaria : fechamento.getDiarias()) {
                tabela.addCell(celula(identificador(diaria.getId()), Element.ALIGN_CENTER));
                tabela.addCell(celula(data(diaria.getData()), Element.ALIGN_LEFT));
                tabela.addCell(celula(moeda(diaria.getValor()), Element.ALIGN_RIGHT));
            }
            documento.add(tabela);

            Paragraph total = new Paragraph();
            total.setAlignment(Element.ALIGN_RIGHT);
            total.add(new Chunk("Total: ", ROTULO));
            total.add(new Chunk(moeda(fechamento.getValorTotal()), new Font(Font.HELVETICA, 12, Font.BOLD)));
            documento.add(total);
        } catch (DocumentException e) {
            throw new IllegalStateException("Falha ao gerar o PDF do fechamento de diárias", e);
        } finally {
            documento.close();
        }
        return saida.toByteArray();
    }

    private void titulo(Document documento, String texto) throws DocumentException {
        Paragraph paragrafo = new Paragraph(texto, TITULO);
        paragrafo.setSpacingAfter(14);
        documento.add(paragrafo);
    }

    private void secao(Document documento, String texto) throws DocumentException {
        Paragraph paragrafo = new Paragraph(texto, SECAO);
        paragrafo.setSpacingBefore(10);
        paragrafo.setSpacingAfter(6);
        documento.add(paragrafo);
    }

    private void campo(Document documento, String rotulo, String valor) throws DocumentException {
        Paragraph paragrafo = new Paragraph();
        paragrafo.add(new Chunk(rotulo + ": ", ROTULO));
        paragrafo.add(new Chunk(valor == null || valor.isBlank() ? "-" : valor, TEXTO));
        paragrafo.setSpacingAfter(3);
        documento.add(paragrafo);
    }

    private PdfPTable tabela(float[] larguras) {
        PdfPTable tabela = new PdfPTable(larguras.length);
        tabela.setWidthPercentage(100);
        tabela.setSpacingBefore(4);
        tabela.setSpacingAfter(8);
        try {
            tabela.setWidths(larguras);
        } catch (DocumentException e) {
            throw new IllegalStateException(e);
        }
        return tabela;
    }

    private PdfPCell cabecalho(String texto) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, CABECALHO_TABELA));
        celula.setBackgroundColor(AZUL);
        celula.setPadding(5);
        celula.setHorizontalAlignment(Element.ALIGN_CENTER);
        return celula;
    }

    private PdfPCell celula(String texto, int alinhamento) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, TEXTO_TABELA));
        celula.setPadding(5);
        celula.setHorizontalAlignment(alinhamento);
        return celula;
    }

    private static String identificador(Long id) {
        return id == null ? "-" : String.valueOf(id);
    }

    private static String numero(Long numero) {
        return numero == null ? "-" : String.format("%06d", numero);
    }

    private static String data(LocalDate data) {
        return data == null ? "-" : DATA.format(data);
    }

    private static String moeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(BR).format(valor == null ? BigDecimal.ZERO : valor);
    }

    private static String quantidade(BigDecimal valor) {
        NumberFormat formato = NumberFormat.getNumberInstance(BR);
        formato.setMinimumFractionDigits(0);
        formato.setMaximumFractionDigits(3);
        return formato.format(valor == null ? BigDecimal.ZERO : valor);
    }

    private static String cpf(String valor) {
        if (valor == null || valor.length() != 11) {
            return valor;
        }
        return valor.substring(0, 3) + "." + valor.substring(3, 6) + "." + valor.substring(6, 9)
                + "-" + valor.substring(9);
    }

    private static String cnpj(String valor) {
        if (valor == null || valor.length() != 14) {
            return valor;
        }
        return valor.substring(0, 2) + "." + valor.substring(2, 5) + "." + valor.substring(5, 8)
                + "/" + valor.substring(8, 12) + "-" + valor.substring(12);
    }

    private static String telefone(String valor) {
        if (valor == null) {
            return null;
        }
        if (valor.length() == 11) {
            return "(" + valor.substring(0, 2) + ") " + valor.substring(2, 7) + "-" + valor.substring(7);
        }
        if (valor.length() == 10) {
            return "(" + valor.substring(0, 2) + ") " + valor.substring(2, 6) + "-" + valor.substring(6);
        }
        return valor;
    }

    private static String chavePix(TipoChavePix tipo, String chave) {
        if (tipo == null || chave == null) {
            return null;
        }
        return switch (tipo) {
            case CPF -> cpf(chave);
            case CNPJ -> cnpj(chave);
            case TELEFONE -> "+" + chave.substring(0, 2) + " " + telefone(chave.substring(2));
            case EMAIL, ALEATORIA -> chave;
        };
    }
}
