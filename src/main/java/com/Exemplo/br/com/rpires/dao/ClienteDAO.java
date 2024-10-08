package br.com.rpires.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import br.com.rpires.dao.generic.GenericDAO;
import br.com.rpires.domain.Cliente;

/**
 * @author rodrigo.pires
 *
 */
public class ClienteDAO extends GenericDAO<Cliente, Long> implements IClienteDAO {

	public ClienteDAO() {
		super();
	}

	@Override
	public Class<Cliente> getTipoClasse() {
		return Cliente.class;
	}

	@Override
	public void atualiarDados(Cliente entity, Cliente entityCadastrado) {
		entityCadastrado.setCidade(entity.getCidade());
		entityCadastrado.setCpf(entity.getCpf());
		entityCadastrado.setEnd(entity.getEnd());
		entityCadastrado.setEstado(entity.getEstado());
		entityCadastrado.setNome(entity.getNome());
		entityCadastrado.setNumero(entity.getNumero());
		entityCadastrado.setTel(entity.getTel());
		entityCadastrado.setDataNascimento(entity.getDataNascimento()); // Atualização para o novo campo
	}

	@Override
	protected String getQueryInsercao() {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO TB_CLIENTE ");
		sb.append("(ID, NOME, CPF, TEL, ENDERECO, NUMERO, CIDADE, ESTADO, DATA_NASCIMENTO) "); // Adicionado novo campo
		sb.append("VALUES (nextval('sq_cliente'),?,?,?,?,?,?,?,?)"); // Adicionado novo campo
		return sb.toString();
	}

	@Override
	protected void setParametrosQueryInsercao(PreparedStatement stmInsert, Cliente entity) throws SQLException {
		stmInsert.setString(1, entity.getNome());
		stmInsert.setLong(2, entity.getCpf());
		stmInsert.setLong(3, entity.getTel());
		stmInsert.setString(4, entity.getEnd());
		stmInsert.setInt(5, entity.getNumero());
		stmInsert.setString(6, entity.getCidade());
		stmInsert.setString(7, entity.getEstado());
		stmInsert.setDate(8, java.sql.Date.valueOf(entity.getDataNascimento())); // Adicionado novo campo
	}

	@Override
	protected String getQueryExclusao() {
		return "DELETE FROM TB_CLIENTE WHERE CPF = ?";
	}

	@Override
	protected void setParametrosQueryExclusao(PreparedStatement stmExclusao, Long valor) throws SQLException {
		stmExclusao.setLong(1, valor);
	}
	
	@Override
	protected String getQueryAtualizacao() {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE TB_CLIENTE ");
		sb.append("SET NOME = ?," );
		sb.append("TEL = ?," );
		sb.append("ENDERECO = ?," );
		sb.append("NUMERO = ?," );
		sb.append("CIDADE = ?," );
		sb.append("ESTADO = ?," );
		sb.append("DATA_NASCIMENTO = ? "); // Adicionado novo campo
		sb.append("WHERE CPF = ?");
		return sb.toString();
	}

	@Override
	protected void setParametrosQueryAtualizacao(PreparedStatement stmUpdate, Cliente entity) throws SQLException {
		stmUpdate.setString(1, entity.getNome());
		stmUpdate.setLong(2, entity.getTel());
		stmUpdate.setString(3, entity.getEnd());
		stmUpdate.setInt(4, entity.getNumero()); // Corrigido para Integer
		stmUpdate.setString(5, entity.getCidade());
		stmUpdate.setString(6, entity.getEstado());
		stmUpdate.setDate(7, java.sql.Date.valueOf(entity.getDataNascimento())); // Adicionado novo campo
		stmUpdate.setLong(8, entity.getCpf()); // Mover o CPF para a última posição
	}

	@Override
	protected void setParametrosQuerySelect(PreparedStatement stmSelect, Long valor) throws SQLException {
		stmSelect.setLong(1, valor);
	}
}
