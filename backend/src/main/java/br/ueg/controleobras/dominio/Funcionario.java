package br.ueg.controleobras.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "funcionarios")
@Getter
@Setter
@NoArgsConstructor
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 11)
    private String telefone;

    @Column(length = 60)
    private String funcao;

    @Column(name = "valor_diaria", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorDiaria;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_chave_pix", length = 20)
    private TipoChavePix tipoChavePix;

    @Column(name = "chave_pix", length = 140)
    private String chavePix;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;
}
