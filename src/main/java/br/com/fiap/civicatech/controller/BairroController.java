package br.com.fiap.civicatech.controller;

import br.com.fiap.civicatech.dao.BairroDAO;
import br.com.fiap.civicatech.dto.BairroDTO;
import br.com.fiap.civicatech.exception.RecursoNaoEncontradoException;
import br.com.fiap.civicatech.model.Bairro;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bairros")
public class BairroController {

    private final BairroDAO bairroDAO;
    private final ModelMapper modelMapper;

    public BairroController(BairroDAO bairroDAO, ModelMapper modelMapper) {
        this.bairroDAO = bairroDAO;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<List<BairroDTO>> listar() {
        List<BairroDTO> lista = bairroDAO.listarTodos().stream()
                .map(b -> modelMapper.map(b, BairroDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BairroDTO> buscarPorId(@PathVariable Long id) {
        Bairro bairro = bairroDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Bairro nao encontrado para o ID: " + id));
        return ResponseEntity.ok(modelMapper.map(bairro, BairroDTO.class));
    }

    @PostMapping
    public ResponseEntity<BairroDTO> cadastrar(@Valid @RequestBody BairroDTO dto) {
        Bairro model = modelMapper.map(dto, Bairro.class);
        Bairro salvo = bairroDAO.inserir(model);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(salvo.getId())
                .toUri();
        return ResponseEntity.created(location).body(modelMapper.map(salvo, BairroDTO.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BairroDTO> atualizar(@PathVariable Long id, @Valid @RequestBody BairroDTO dto) {
        Bairro model = modelMapper.map(dto, Bairro.class);
        Bairro atualizado = bairroDAO.atualizar(id, model);
        return ResponseEntity.ok(modelMapper.map(atualizado, BairroDTO.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        bairroDAO.remover(id);
        return ResponseEntity.noContent().build();
    }
}
