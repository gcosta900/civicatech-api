package br.com.fiap.civicatech.controller;

import br.com.fiap.civicatech.dao.AtualizacaoDAO;
import br.com.fiap.civicatech.dao.OcorrenciaDAO;
import br.com.fiap.civicatech.dto.AtualizacaoRequestDTO;
import br.com.fiap.civicatech.dto.AtualizacaoResponseDTO;
import br.com.fiap.civicatech.dto.OcorrenciaRequestDTO;
import br.com.fiap.civicatech.dto.OcorrenciaResponseDTO;
import br.com.fiap.civicatech.exception.RecursoNaoEncontradoException;
import br.com.fiap.civicatech.model.Atualizacao;
import br.com.fiap.civicatech.model.Ocorrencia;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ocorrencias")
public class OcorrenciaController {

    private final OcorrenciaDAO ocorrenciaDAO;
    private final AtualizacaoDAO atualizacaoDAO;
    private final ModelMapper modelMapper;

    public OcorrenciaController(OcorrenciaDAO ocorrenciaDAO, AtualizacaoDAO atualizacaoDAO, ModelMapper modelMapper) {
        this.ocorrenciaDAO = ocorrenciaDAO;
        this.atualizacaoDAO = atualizacaoDAO;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<List<OcorrenciaResponseDTO>> listar() {
        List<OcorrenciaResponseDTO> lista = ocorrenciaDAO.listarTodas().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OcorrenciaResponseDTO> buscarPorId(@PathVariable Long id) {
        Ocorrencia ocorrencia = ocorrenciaDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ocorrencia nao encontrada para o ID: " + id));
        return ResponseEntity.ok(toResponseDTO(ocorrencia));
    }

    @PostMapping
    public ResponseEntity<OcorrenciaResponseDTO> cadastrar(@Valid @RequestBody OcorrenciaRequestDTO dto) {
        Ocorrencia model = modelMapper.map(dto, Ocorrencia.class);
        Ocorrencia salva = ocorrenciaDAO.inserir(model);
        OcorrenciaResponseDTO response = toResponseDTO(salva);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(salva.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OcorrenciaResponseDTO> atualizar(@PathVariable Long id,
                                                           @Valid @RequestBody OcorrenciaRequestDTO dto) {
        Ocorrencia model = modelMapper.map(dto, Ocorrencia.class);
        Ocorrencia atualizada = ocorrenciaDAO.atualizar(id, model);
        return ResponseEntity.ok(toResponseDTO(atualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        ocorrenciaDAO.remover(id);
        return ResponseEntity.noContent().build();
    }

    // Rotas aninhadas 1:N (/ocorrencias/{id}/atualizacoes)
    @GetMapping("/{id}/atualizacoes")
    public ResponseEntity<List<AtualizacaoResponseDTO>> listarAtualizacoes(@PathVariable Long id) {
        List<AtualizacaoResponseDTO> lista = atualizacaoDAO.listarPorOcorrencia(id).stream()
                .map(this::toAtualizacaoDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/{id}/atualizacoes")
    public ResponseEntity<AtualizacaoResponseDTO> adicionarAtualizacao(@PathVariable Long id,
                                                                       @Valid @RequestBody AtualizacaoRequestDTO dto) {
        Atualizacao model = modelMapper.map(dto, Atualizacao.class);
        Atualizacao salva = atualizacaoDAO.inserir(id, model);
        AtualizacaoResponseDTO response = toAtualizacaoDTO(salva);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{attId}")
                .buildAndExpand(salva.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    private OcorrenciaResponseDTO toResponseDTO(Ocorrencia o) {
        OcorrenciaResponseDTO dto = modelMapper.map(o, OcorrenciaResponseDTO.class);
        if (o.getDataAbertura() != null) {
            dto.setDataAbertura(o.getDataAbertura().toString());
        }
        return dto;
    }

    private AtualizacaoResponseDTO toAtualizacaoDTO(Atualizacao a) {
        AtualizacaoResponseDTO dto = modelMapper.map(a, AtualizacaoResponseDTO.class);
        if (a.getDataAtualizacao() != null) {
            dto.setDataAtualizacao(a.getDataAtualizacao().toString());
        }
        return dto;
    }
}
