/**
 * 
 */
package br.com.rpires;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.rpires.dao.ClienteDAO;
import br.com.rpires.dao.IClienteDAO;
import br.com.rpires.dao.IProdutoDAO;
import br.com.rpires.dao.IVendaDAO;
import br.com.rpires.dao.ProdutoDAO;
import br.com.rpires.dao.VendaDAO;
import br.com.rpires.dao.generic.jdbc.ConnectionFactory;
import br.com.rpires.domain.Cliente;
import br.com.rpires.domain.Produto;
import br.com.rpires.domain.Venda;
import br.com.rpires.domain.Venda.Status;
import br.com.rpires.exceptions.DAOException;
import br.com.rpires.exceptions.MaisDeUmRegistroException;
import br.com.rpires.exceptions.TableException;
import br.com.rpires.exceptions.TipoChaveNaoEncontradaException;

/**
 * @author rodrigo.pires
 *
 */
public class VendaDAOTest {
	
	private IVendaDAO vendaDao;
	
	private IClienteDAO clienteDao;
	
	private IProdutoDAO produtoDao;

	private Cliente cliente;
	
	private Produto produto;
	
	public VendaDAOTest() {
		vendaDao = new VendaDAO();
		clienteDao = new ClienteDAO();
		produtoDao = new ProdutoDAO();
	}
	
	@Before
	public void init() throws TipoChaveNaoEncontradaException, MaisDeUmRegistroException, TableException, DAOException {
		this.cliente = cadastrarCliente();
		this.produto = cadastrarProduto("A1", BigDecimal.TEN);
	}
	
	@After
	public void end() {
	    assertNotNull("O cliente deve ser inicializado", this.cliente);
	    assertEquals("CPF do cliente não é o esperado", "123.456.789-00", this.cliente.getCpf().toString()); // Alterado para String
	    // Outros testes que você deseja realizar
	}

	private void excluirProdutos() throws DAOException {
		Collection<Produto> list = this.produtoDao.buscarTodos();
		for (Produto prod : list) {
			this.produtoDao.excluir(prod.getCodigo());
		}
	}

	@Test
	public void pesquisar() throws TipoChaveNaoEncontradaException, MaisDeUmRegistroException, TableException, DAOException {
		Venda venda = criarVenda("A1");
		Boolean retorno = vendaDao.cadastrar(venda);
		assertTrue(retorno);
		Venda vendaConsultada = vendaDao.consultar(venda.getCodigo());
		assertNotNull(vendaConsultada);
		assertEquals(venda.getCodigo(), vendaConsultada.getCodigo());
	}
	
	@Test
	public void salvar() throws TipoChaveNaoEncontradaException, DAOException, MaisDeUmRegistroException, TableException {
		Venda venda = criarVenda("A2");
		Boolean retorno = vendaDao.cadastrar(venda);
		assertTrue(retorno);
		
		assertTrue(venda.getValorTotal().equals(BigDecimal.valueOf(20)));
		assertTrue(venda.getStatus().equals(Status.INICIADA));
		
		Venda vendaConsultada = vendaDao.consultar(venda.getCodigo());
		assertTrue(vendaConsultada.getId() != null);
		assertEquals(venda.getCodigo(), vendaConsultada.getCodigo());
	} 
	
	@Test
	public void cancelarVenda() throws TipoChaveNaoEncontradaException, MaisDeUmRegistroException, TableException, DAOException {
		String codigoVenda = "A3";
		Venda venda = criarVenda(codigoVenda);
		Boolean retorno = vendaDao.cadastrar(venda);
		assertTrue(retorno);
		assertNotNull(venda);
		assertEquals(codigoVenda, venda.getCodigo());
		
		vendaDao.cancelarVenda(venda);
		
		Venda vendaConsultada = vendaDao.consultar(codigoVenda);
		assertEquals(codigoVenda, vendaConsultada.getCodigo());
		assertEquals(Status.CANCELADA, vendaConsultada.getStatus());
	}
	
	@Test
	public void adicionarMaisProdutosDoMesmo() throws TipoChaveNaoEncontradaException, MaisDeUmRegistroException, TableException, DAOException {
		String codigoVenda = "A4";
		Venda venda = criarVenda(codigoVenda);
		Boolean retorno = vendaDao.cadastrar(venda);
		assertTrue(retorno);
		assertNotNull(venda);
		assertEquals(codigoVenda, venda.getCodigo());
		
		Venda vendaConsultada = vendaDao.consultar(codigoVenda);
		vendaConsultada.adicionarProduto(produto, 1);
		
		assertTrue(vendaConsultada.getQuantidadeTotalProdutos() == 3);
		BigDecimal valorTotal = BigDecimal.valueOf(30).setScale(2, RoundingMode.HALF_DOWN);
		assertTrue(vendaConsultada.getValorTotal().equals(valorTotal));
		assertTrue(vendaConsultada.getStatus().equals(Status.INICIADA));
	} 
	
	@Test
	public void adicionarMaisProdutosDiferentes() throws TipoChaveNaoEncontradaException, MaisDeUmRegistroException, TableException, DAOException {
		String codigoVenda = "A5";
		Venda venda = criarVenda(codigoVenda);
		Boolean retorno = vendaDao.cadastrar(venda);
		assertTrue(retorno);
		assertNotNull(venda);
		assertEquals(codigoVenda, venda.getCodigo());
		
		Produto prod = cadastrarProduto(codigoVenda, BigDecimal.valueOf(50));
		assertNotNull(prod);
		assertEquals(codigoVenda, prod.getCodigo());
		
		Venda vendaConsultada = vendaDao.consultar(codigoVenda);
		vendaConsultada.adicionarProduto(prod, 1);
		
		assertTrue(vendaConsultada.getQuantidadeTotalProdutos() == 3);
		BigDecimal valorTotal = BigDecimal.valueOf(70).setScale(2, RoundingMode.HALF_DOWN);
		assertTrue(vendaConsultada.getValorTotal().equals(valorTotal));
		assertTrue(vendaConsultada.getStatus().equals(Status.INICIADA));
	} 
	
	@Test(expected = DAOException.class)
	public void salvarVendaMesmoCodigoExistente() throws TipoChaveNaoEncontradaException, DAOException {
		Venda venda = criarVenda("A6");
		Boolean retorno = vendaDao.cadastrar(venda);
		assertTrue(retorno);
	
		Boolean retorno1 = vendaDao.cadastrar(venda);
		assertFalse(retorno1);
		assertTrue(venda.getStatus().equals(Status.INICIADA));
	} 
	
	@Test
	public void removerProduto() throws TipoChaveNaoEncontradaException, MaisDeUmRegistroException, TableException, DAOException {
		String codigoVenda = "A7";
		Venda venda = criarVenda(codigoVenda);
		Boolean retorno = vendaDao.cadastrar(venda);
		assertTrue(retorno);
		assertNotNull(venda);
		assertEquals(codigoVenda, venda.getCodigo());
		
		Produto prod = cadastrarProduto(codigoVenda, BigDecimal.valueOf(50));
		assertNotNull(prod);
		assertEquals(codigoVenda, prod.getCodigo());
		
		Venda vendaConsultada = vendaDao.consultar(codigoVenda);
		vendaConsultada.adicionarProduto(prod, 1);
		assertTrue(vendaConsultada.getQuantidadeTotalProdutos() == 3);
		BigDecimal valorTotal = BigDecimal.valueOf(70).setScale(2, RoundingMode.HALF_DOWN);
		assertTrue(vendaConsultada.getValorTotal().equals(valorTotal));
		
		
		vendaConsultada.removerProduto(prod, 1);
		assertTrue(vendaConsultada.getQuantidadeTotalProdutos() == 2);
		valorTotal = BigDecimal.valueOf(20).setScale(2, RoundingMode.HALF_DOWN);
		assertTrue(vendaConsultada.getValorTotal().equals(valorTotal));
		assertTrue(vendaConsultada.getStatus().equals(Status.INICIADA));
	} 
	
	@Test
	public void removerApenasUmProduto() throws TipoChaveNaoEncontradaException, MaisDeUmRegistroException, TableException, DAOException {
		String codigoVenda = "A8";
		Venda venda = criarVenda(codigoVenda);
		Boolean retorno = vendaDao.cadastrar(venda);
		assertTrue(retorno);
		assertNotNull(venda);
		assertEquals(codigoVenda, venda.getCodigo());
		
		Produto prod = cadastrarProduto(codigoVenda, BigDecimal.valueOf(50));
		assertNotNull(prod);
		assertEquals(codigoVenda, prod.getCodigo());
		
		Venda vendaConsultada = vendaDao.consultar(codigoVenda);
		vendaConsultada.adicionarProduto(prod, 1);
		assertTrue(vendaConsultada.getQuantidadeTotalProdutos() == 3);
		BigDecimal valorTotal = BigDecimal.valueOf(70).setScale(2, RoundingMode.HALF_DOWN);
		assertTrue(vendaConsultada.getValorTotal().equals(valorTotal));
		
		
		vendaConsultada.removerProduto(prod, 1);
		assertTrue(vendaConsultada.getQuantidadeTotalProdutos() == 2);
		valorTotal = BigDecimal.valueOf(20).setScale(2, RoundingMode.HALF_DOWN);
		assertTrue(vendaConsultada.getValorTotal().equals(valorTotal));
		assertTrue(vendaConsultada.getStatus().equals(Status.INICIADA));
	} 
	
	@Test
	public void adicionarUmProduto() throws TipoChaveNaoEncontradaException, MaisDeUmRegistroException, TableException, DAOException {
		String codigoVenda = "A9";
		Venda venda = criarVenda(codigoVenda);
		Boolean retorno = vendaDao.cadastrar(venda);
		assertTrue(retorno);
		assertNotNull(venda);
		assertEquals(codigoVenda, venda.getCodigo());
		
		Venda vendaConsultada = vendaDao.consultar(codigoVenda);
		vendaConsultada.adicionarProduto(produto, 1);
		
		assertTrue(vendaConsultada.getQuantidadeTotalProdutos() == 3);
		BigDecimal valorTotal = BigDecimal.valueOf(30).setScale(2, RoundingMode.HALF_DOWN);
		assertTrue(vendaConsultada.getValorTotal().equals(valorTotal));
		assertTrue(vendaConsultada.getStatus().equals(Status.INICIADA));
	}

	private Cliente cadastrarCliente() throws TipoChaveNaoEncontradaException, DAOException {
		Cliente cliente = new Cliente();
		clienteDao.cadastrar(cliente);
		return cliente;
	}
	
	private Produto cadastrarProduto(String codigo, BigDecimal valor) throws TipoChaveNaoEncontradaException, DAOException {
		Produto produto = new Produto();
		produtoDao.cadastrar(produto);
		return produto;
	}
	
	private Venda criarVenda(String codigo) {
		Venda venda = new Venda(Instant.now(), cliente);
		venda.setCodigo(codigo);
		venda.adicionarProduto(produto, 2);
		return venda;
	}
}
