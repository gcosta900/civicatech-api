package br.com.fiap.civicatech.exception;

import java.time.Instant;
import java.util.List;

public class ErroPadraoDTO {
    private Instant timestamp;
    private int status;
    private String erro;
    private String mensagem;
    private List<String> detalhes;

    public ErroPadraoDTO(int status, String erro, String mensagem, List<String> detalhes) {
        this.timestamp = Instant.now();
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
        this.detalhes = detalhes;
    }

    public Instant getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getErro() { return erro; }
    public String getMensagem() { return mensagem; }
    public List<String> getDetalhes() { return detalhes; }
}
