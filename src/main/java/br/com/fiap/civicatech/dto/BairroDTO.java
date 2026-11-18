package br.com.fiap.civicatech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BairroDTO {
    private Long id;

    @NotBlank(message = "O nome do bairro e obrigatorio")
    @Size(min = 2, max = 100, message = "Nome do bairro deve ter entre 2 e 100 caracteres")
    private String nome;

    @NotBlank(message = "A regiao e obrigatoria")
    @Size(min = 3, max = 60, message = "Regiao deve ter entre 3 e 60 caracteres")
    private String regiao;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getRegiao() { return regiao; }
    public void setRegiao(String regiao) { this.regiao = regiao; }
}
