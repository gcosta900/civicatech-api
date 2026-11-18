package br.com.fiap.civicatech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AtualizacaoRequestDTO {

    @NotBlank(message = "A descricao da atualizacao e obrigatoria")
    @Size(min = 5, max = 400, message = "A descricao deve ter entre 5 e 400 caracteres")
    private String descricao;

    @NotBlank(message = "O novo status e obrigatorio")
    @Pattern(regexp = "ABERTA|EM_ANALISE|RESOLVIDA", message = "Status deve ser ABERTA, EM_ANALISE ou RESOLVIDA")
    private String statusNovo;

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getStatusNovo() { return statusNovo; }
    public void setStatusNovo(String statusNovo) { this.statusNovo = statusNovo; }
}
