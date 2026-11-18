package br.com.fiap.civicatech.dao;

import br.com.fiap.civicatech.exception.RecursoNaoEncontradoException;
import br.com.fiap.civicatech.exception.RegraNegocioException;
import br.com.fiap.civicatech.model.Bairro;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class BairroDAO {

    private final DataSource dataSource;

    public BairroDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Bairro> listarTodos() {
        String sql = "SELECT cd_bairro, nm_bairro, ds_regiao FROM TB_BAIRRO ORDER BY nm_bairro";
        List<Bairro> lista = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao listar bairros", e);
        }
        return lista;
    }

    public Optional<Bairro> buscarPorId(Long id) {
        String sql = "SELECT cd_bairro, nm_bairro, ds_regiao FROM TB_BAIRRO WHERE cd_bairro = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao buscar bairro por ID", e);
        }
        return Optional.empty();
    }

    public Bairro inserir(Bairro bairro) {
        String seqSql = "SELECT SQ_TB_BAIRRO.NEXTVAL FROM DUAL";
        String insertSql = "INSERT INTO TB_BAIRRO (cd_bairro, nm_bairro, ds_regiao) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
            long novoId;
            try (PreparedStatement psSeq = conn.prepareStatement(seqSql);
                 ResultSet rs = psSeq.executeQuery()) {
                rs.next();
                novoId = rs.getLong(1);
            }
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setLong(1, novoId);
                ps.setString(2, bairro.getNome());
                ps.setString(3, bairro.getRegiao());
                ps.executeUpdate();
            }
            bairro.setId(novoId);
            return bairro;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new RegraNegocioException("Ja existe um bairro cadastrado com este nome.");
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao inserir bairro", e);
        }
    }

    public Bairro atualizar(Long id, Bairro bairro) {
        if (buscarPorId(id).isEmpty()) {
            throw new RecursoNaoEncontradoException("Bairro nao encontrado para o ID: " + id);
        }
        String sql = "UPDATE TB_BAIRRO SET nm_bairro = ?, ds_regiao = ? WHERE cd_bairro = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bairro.getNome());
            ps.setString(2, bairro.getRegiao());
            ps.setLong(3, id);
            ps.executeUpdate();
            bairro.setId(id);
            return bairro;
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao atualizar bairro", e);
        }
    }

    public void remover(Long id) {
        if (buscarPorId(id).isEmpty()) {
            throw new RecursoNaoEncontradoException("Bairro nao encontrado para o ID: " + id);
        }
        String sql = "DELETE FROM TB_BAIRRO WHERE cd_bairro = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new RegraNegocioException("Nao e possivel excluir um bairro que possui ocorrencias vinculadas.");
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao remover bairro", e);
        }
    }

    private Bairro mapear(ResultSet rs) throws SQLException {
        Bairro b = new Bairro();
        b.setId(rs.getLong("cd_bairro"));
        b.setNome(rs.getString("nm_bairro"));
        b.setRegiao(rs.getString("ds_regiao"));
        return b;
    }
}
