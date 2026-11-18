package br.com.fiap.civicatech.dao;

import br.com.fiap.civicatech.exception.RecursoNaoEncontradoException;
import br.com.fiap.civicatech.model.Atualizacao;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AtualizacaoDAO {

    private final DataSource dataSource;
    private final OcorrenciaDAO ocorrenciaDAO;

    public AtualizacaoDAO(DataSource dataSource, OcorrenciaDAO ocorrenciaDAO) {
        this.dataSource = dataSource;
        this.ocorrenciaDAO = ocorrenciaDAO;
    }

    public List<Atualizacao> listarPorOcorrencia(Long ocorrenciaId) {
        if (ocorrenciaDAO.buscarPorId(ocorrenciaId).isEmpty()) {
            throw new RecursoNaoEncontradoException("Ocorrencia nao encontrada para o ID: " + ocorrenciaId);
        }

        String sql = "SELECT cd_atualizacao, cd_ocorrencia, ds_atualizacao, ds_status_novo, dt_atualizacao FROM TB_ATUALIZACAO WHERE cd_ocorrencia = ? ORDER BY cd_atualizacao DESC";
        List<Atualizacao> lista = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, ocorrenciaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao listar atualizacoes da ocorrencia", e);
        }
        return lista;
    }

    public Optional<Atualizacao> buscarPorId(Long id) {
        String sql = "SELECT cd_atualizacao, cd_ocorrencia, ds_atualizacao, ds_status_novo, dt_atualizacao FROM TB_ATUALIZACAO WHERE cd_atualizacao = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao buscar atualizacao por ID", e);
        }
        return Optional.empty();
    }

    public Atualizacao inserir(Long ocorrenciaId, Atualizacao atualizacao) {
        if (ocorrenciaDAO.buscarPorId(ocorrenciaId).isEmpty()) {
            throw new RecursoNaoEncontradoException("Ocorrencia nao encontrada para o ID: " + ocorrenciaId);
        }

        String seqSql = "SELECT SQ_TB_ATUALIZACAO.NEXTVAL FROM DUAL";
        String insertSql = "INSERT INTO TB_ATUALIZACAO (cd_atualizacao, cd_ocorrencia, ds_atualizacao, ds_status_novo, dt_atualizacao) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection()) {
            long novoId;
            try (PreparedStatement psSeq = conn.prepareStatement(seqSql);
                 ResultSet rs = psSeq.executeQuery()) {
                rs.next();
                novoId = rs.getLong(1);
            }

            LocalDate hoje = LocalDate.now();
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setLong(1, novoId);
                ps.setLong(2, ocorrenciaId);
                ps.setString(3, atualizacao.getDescricao());
                ps.setString(4, atualizacao.getStatusNovo());
                ps.setDate(5, Date.valueOf(hoje));
                ps.executeUpdate();
            }

            // Sincroniza o status atual na tabela pai TB_OCORRENCIA
            ocorrenciaDAO.atualizarStatus(ocorrenciaId, atualizacao.getStatusNovo());

            atualizacao.setId(novoId);
            atualizacao.setOcorrenciaId(ocorrenciaId);
            atualizacao.setDataAtualizacao(hoje);
            return atualizacao;
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao inserir atualizacao", e);
        }
    }

    public Atualizacao atualizar(Long id, Atualizacao dados) {
        Atualizacao existente = buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atualizacao nao encontrada para o ID: " + id));

        String sql = "UPDATE TB_ATUALIZACAO SET ds_atualizacao = ?, ds_status_novo = ? WHERE cd_atualizacao = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dados.getDescricao());
            ps.setString(2, dados.getStatusNovo());
            ps.setLong(3, id);
            ps.executeUpdate();

            dados.setId(id);
            dados.setOcorrenciaId(existente.getOcorrenciaId());
            dados.setDataAtualizacao(existente.getDataAtualizacao());
            return dados;
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao atualizar registro de andamento", e);
        }
    }

    public void remover(Long id) {
        if (buscarPorId(id).isEmpty()) {
            throw new RecursoNaoEncontradoException("Atualizacao nao encontrada para o ID: " + id);
        }
        String sql = "DELETE FROM TB_ATUALIZACAO WHERE cd_atualizacao = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao remover atualizacao", e);
        }
    }

    private Atualizacao mapear(ResultSet rs) throws SQLException {
        Atualizacao a = new Atualizacao();
        a.setId(rs.getLong("cd_atualizacao"));
        a.setOcorrenciaId(rs.getLong("cd_ocorrencia"));
        a.setDescricao(rs.getString("ds_atualizacao"));
        a.setStatusNovo(rs.getString("ds_status_novo"));
        Date dt = rs.getDate("dt_atualizacao");
        if (dt != null) {
            a.setDataAtualizacao(dt.toLocalDate());
        }
        return a;
    }
}
