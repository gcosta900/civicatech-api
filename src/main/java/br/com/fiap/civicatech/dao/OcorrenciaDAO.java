package br.com.fiap.civicatech.dao;

import br.com.fiap.civicatech.exception.RecursoNaoEncontradoException;
import br.com.fiap.civicatech.exception.RegraNegocioException;
import br.com.fiap.civicatech.model.Ocorrencia;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class OcorrenciaDAO {

    private final DataSource dataSource;

    public OcorrenciaDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Ocorrencia> listarTodas() {
        String sql = "SELECT cd_ocorrencia, nm_titulo, ds_ocorrencia, ds_categoria, nm_bairro, ds_endereco, ds_prioridade, ds_status, dt_abertura FROM TB_OCORRENCIA ORDER BY cd_ocorrencia DESC";
        List<Ocorrencia> lista = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao consultar ocorrencias", e);
        }
        return lista;
    }

    public Optional<Ocorrencia> buscarPorId(Long id) {
        String sql = "SELECT cd_ocorrencia, nm_titulo, ds_ocorrencia, ds_categoria, nm_bairro, ds_endereco, ds_prioridade, ds_status, dt_abertura FROM TB_OCORRENCIA WHERE cd_ocorrencia = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao buscar ocorrencia por ID", e);
        }
        return Optional.empty();
    }

    public boolean existeAbertaNoMesmoEnderecoECategoria(String endereco, String categoria) {
        String sql = "SELECT COUNT(*) FROM TB_OCORRENCIA WHERE LOWER(TRIM(ds_endereco)) = LOWER(TRIM(?)) AND ds_categoria = ? AND ds_status <> 'RESOLVIDA'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, endereco);
            ps.setString(2, categoria);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao validar duplicidade de ocorrencia", e);
        }
        return false;
    }

    public Ocorrencia inserir(Ocorrencia ocorrencia) {
        // Regra de negocio: impedir duplicidade de chamado aberto no mesmo endereco e categoria
        if (existeAbertaNoMesmoEnderecoECategoria(ocorrencia.getEndereco(), ocorrencia.getCategoria())) {
            throw new RegraNegocioException("Ja existe uma ocorrencia aberta para esta mesma categoria neste endereco.");
        }

        String seqSql = "SELECT SQ_TB_OCORRENCIA.NEXTVAL FROM DUAL";
        String insertSql = "INSERT INTO TB_OCORRENCIA (cd_ocorrencia, nm_titulo, ds_ocorrencia, ds_categoria, nm_bairro, ds_endereco, ds_prioridade, ds_status, dt_abertura) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection()) {
            long novoId;
            try (PreparedStatement psSeq = conn.prepareStatement(seqSql);
                 ResultSet rs = psSeq.executeQuery()) {
                rs.next();
                novoId = rs.getLong(1);
            }

            LocalDate hoje = LocalDate.now();
            String statusInicial = (ocorrencia.getStatus() != null) ? ocorrencia.getStatus() : "ABERTA";

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setLong(1, novoId);
                ps.setString(2, ocorrencia.getTitulo());
                ps.setString(3, ocorrencia.getDescricao());
                ps.setString(4, ocorrencia.getCategoria());
                ps.setString(5, ocorrencia.getBairro());
                ps.setString(6, ocorrencia.getEndereco());
                ps.setString(7, ocorrencia.getPrioridade());
                ps.setString(8, statusInicial);
                ps.setDate(9, Date.valueOf(hoje));
                ps.executeUpdate();
            }

            ocorrencia.setId(novoId);
            ocorrencia.setStatus(statusInicial);
            ocorrencia.setDataAbertura(hoje);
            return ocorrencia;
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao inserir ocorrencia", e);
        }
    }

    public Ocorrencia atualizar(Long id, Ocorrencia dados) {
        Ocorrencia existente = buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ocorrencia nao encontrada para o ID: " + id));

        String sql = "UPDATE TB_OCORRENCIA SET nm_titulo = ?, ds_ocorrencia = ?, ds_categoria = ?, nm_bairro = ?, ds_endereco = ?, ds_prioridade = ?, ds_status = ? WHERE cd_ocorrencia = ?";

        String statusFinal = (dados.getStatus() != null) ? dados.getStatus() : existente.getStatus();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dados.getTitulo());
            ps.setString(2, dados.getDescricao());
            ps.setString(3, dados.getCategoria());
            ps.setString(4, dados.getBairro());
            ps.setString(5, dados.getEndereco());
            ps.setString(6, dados.getPrioridade());
            ps.setString(7, statusFinal);
            ps.setLong(8, id);
            ps.executeUpdate();

            dados.setId(id);
            dados.setStatus(statusFinal);
            dados.setDataAbertura(existente.getDataAbertura());
            return dados;
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao atualizar ocorrencia", e);
        }
    }

    public void atualizarStatus(Long id, String novoStatus) {
        String sql = "UPDATE TB_OCORRENCIA SET ds_status = ? WHERE cd_ocorrencia = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, novoStatus);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao atualizar status da ocorrencia", e);
        }
    }

    public void remover(Long id) {
        if (buscarPorId(id).isEmpty()) {
            throw new RecursoNaoEncontradoException("Ocorrencia nao encontrada para o ID: " + id);
        }

        // Remove registros filhos antes de remover a ocorrencia ou bloqueia se preferir
        String deleteFilhosSql = "DELETE FROM TB_ATUALIZACAO WHERE cd_ocorrencia = ?";
        String deleteApoiosSql = "DELETE FROM TB_APOIO_OCORRENCIA WHERE cd_ocorrencia = ?";
        String deletePaiSql = "DELETE FROM TB_OCORRENCIA WHERE cd_ocorrencia = ?";

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement ps1 = conn.prepareStatement(deleteFilhosSql)) {
                ps1.setLong(1, id);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement(deleteApoiosSql)) {
                ps2.setLong(1, id);
                ps2.executeUpdate();
            }
            try (PreparedStatement ps3 = conn.prepareStatement(deletePaiSql)) {
                ps3.setLong(1, id);
                ps3.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao remover ocorrencia", e);
        }
    }

    private Ocorrencia mapear(ResultSet rs) throws SQLException {
        Ocorrencia o = new Ocorrencia();
        o.setId(rs.getLong("cd_ocorrencia"));
        o.setTitulo(rs.getString("nm_titulo"));
        o.setDescricao(rs.getString("ds_ocorrencia"));
        o.setCategoria(rs.getString("ds_categoria"));
        o.setBairro(rs.getString("nm_bairro"));
        o.setEndereco(rs.getString("ds_endereco"));
        o.setPrioridade(rs.getString("ds_prioridade"));
        o.setStatus(rs.getString("ds_status"));
        Date dt = rs.getDate("dt_abertura");
        if (dt != null) {
            o.setDataAbertura(dt.toLocalDate());
        }
        return o;
    }
}
