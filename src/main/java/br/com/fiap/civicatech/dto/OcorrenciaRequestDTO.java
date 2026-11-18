package br.com.fiap.civicatech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class OcorrenciaRequestDTO {

    @NotBlank(message = "O titulo e obrigatorio")
    @Size(min = 5, max = 150, message = "O titulo deve ter entre 5 e 150 caracteres")
    private String titulo;

    @NotBlank(message = "A descricao e obrigatoria")
    @Size(min = 10, max = 500, message = "A descricao deve ter entre 10 e 500 caracteres")
    private String descricao;

    @NotBlank(message = "A categoria e obrigatoria")
    @Pattern(regexp = "ILUMINACAO|BURACO_VIA|LIXO_ENTULHO|PODAS_ARVORE|ALAGAMENTO",
             message = "Categoria invalida")
    private String categoria;

    @NotBlank(message = "O bairro e obrigatorio")
    @Size(min = 2, max = 100, message = "O bairro deve ter entre 2 e 100 caracteres")
    private String bairro;

    @NotBlank(message = "O endereco e obrigatorio")
    @Size(min = 5, max = 200, message = "O endereco deve ter entre 5 e 200 caracteres")
    private String endereco;

    @NotBlank(message = "A prioridade e obrigatoria")
    @Pattern(regexp = "BAIXA|MEDIA|ALTA", message = "Prioridade deve ser BAIXA, MEDIA ou ALTA")
    private String prioridade;

    @Pattern(regexp = "ABERTA|EM_ANALISE|RESOLVIDA", message = "Status invalido")
    private String status;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getPrioridade() { return prioridade; }
    public void setPrioridade(String prioridade) { this.prioridade = prioridade; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
