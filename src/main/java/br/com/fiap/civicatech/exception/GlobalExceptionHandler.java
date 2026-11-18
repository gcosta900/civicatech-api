package br.com.fiap.civicatech.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroPadraoDTO> handleNotFound(RecursoNaoEncontradoException ex) {
        ErroPadraoDTO erro = new ErroPadraoDTO(
                HttpStatus.NOT_FOUND.value(),
                "Recurso nao encontrado",
                ex.getMessage(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroPadraoDTO> handleConflict(RegraNegocioException ex) {
        ErroPadraoDTO erro = new ErroPadraoDTO(
                HttpStatus.CONFLICT.value(),
                "Violacao de regra de negocio",
                ex.getMessage(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroPadraoDTO> handleValidation(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.toList());

        ErroPadraoDTO erro = new ErroPadraoDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Dados de entrada invalidos",
                "Verifique os campos enviados na requisicao",
                detalhes
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroPadraoDTO> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        ErroPadraoDTO erro = new ErroPadraoDTO(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Metodo HTTP nao suportado",
                "O metodo utilizado nao e permitido para esta rota",
                List.of()
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroPadraoDTO> handleGeneric(Exception ex) {
        ErroPadraoDTO erro = new ErroPadraoDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno no servidor",
                "Ocorreu uma falha inesperada ao processar a solicitacao",
                List.of()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }
}
