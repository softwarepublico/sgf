/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.Abastecimento;
import br.gov.ce.fortaleza.cti.sgf.entity.Cota;
import br.gov.ce.fortaleza.cti.sgf.entity.SolicitacaoLubrificante;
import br.gov.ce.fortaleza.cti.sgf.entity.TipoServico;
import br.gov.ce.fortaleza.cti.sgf.entity.UG;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;
import br.gov.ce.fortaleza.cti.sgf.util.StatusAbastecimento;

/**
 * @author Deivid
 * @since 11/12/09
 */
@Repository
@Transactional
public class AbastecimentoService extends BaseService<Integer, Abastecimento> {
	/**
	 * Retorna o total abastecido pelo veículo no mês.
	 * 
	 * @param veiculo
	 *            veiculo a ser pesquisado o total abastecido
	 * @return o total já abastecido
	 */
	public Double pesquisarTotalAbastecidoMes(Veiculo veiculo) {

		Date now = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));

		Query query = entityManager.createNamedQuery("Abastecimento.findCota");
		query.setParameter("id", veiculo.getId());
		query.setParameter("inicio", cal.getTime());

		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		query.setParameter("fim", cal.getTime());

		Object o = query.getSingleResult();

		if (o == null) {
			o = Double.valueOf(0);
		}

		return (Double) o;
	}
	
	@SuppressWarnings("unchecked")
	public List<Abastecimento> pesquisarPlaca(Date dataIni, Date dataFim, UG ug, String placa, StatusAbastecimento status) {

		List<Abastecimento> result = new ArrayList<Abastecimento>();
		String queryString = "SELECT a from Abastecimento a WHERE a.dataAutorizacao BETWEEN :dataIni AND :dataFim";
		StringBuffer queryBuffer = new StringBuffer(queryString);
		
		if(status != null){
			queryBuffer.append(" AND a.status = :status");
		}
		if(ug != null){
			queryBuffer.append(" AND a.veiculo.ua.ug = :ug");
		}
		if(placa != null){
			queryBuffer.append(" AND a.veiculo.placa = :placa");
		}


		Query query = entityManager.createQuery(queryBuffer.toString());
		query.setParameter("dataIni", dataIni);
		query.setParameter("dataFim", dataFim);
		
		if(status != null){
			query.setParameter("status", status);
		}

		if(ug != null){
			query.setParameter("ug", ug);
		}

		if(placa != null){
			query.setParameter("placa", placa);
		}

		result = query.getResultList();

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Abastecimento> pesquisarPeriodo(Date dtInicial, Date dtFinal, UG orgao, StatusAbastecimento status) {

		List<Abastecimento> abastecimentos = null;

		Query query = entityManager.createQuery("select a from Abastecimento a where a.dataAutorizacao between :dataInicial and :dataFinal and " +
		"a.autorizador.pessoa.ua.ug = :orgao and a.status = :status");

		query.setParameter("dataInicial", dtInicial);
		query.setParameter("dataFinal", dtFinal);
		query.setParameter("orgao", orgao);
		query.setParameter("status", status);

		abastecimentos = query.getResultList();

		return abastecimentos;
	}
	
	@SuppressWarnings("unchecked")
	public List<Abastecimento> pesquisarTodos(Date dtInicial, Date dtFinal, StatusAbastecimento status) {

		List<Abastecimento> abastecimentos = null;

		Query query = entityManager.createQuery("select a from Abastecimento a where a.dataAutorizacao " +
				"between :dataInicial and :dataFinal and a.status = :status");

		query.setParameter("dataInicial", dtInicial);
		query.setParameter("dataFinal", dtFinal);
		query.setParameter("status", status);

		abastecimentos = query.getResultList();

		return abastecimentos;
	}

	public List<Abastecimento> findByPosto(Integer postoId, StatusAbastecimento status){

		return executeResultListQuery("findByPosto", postoId, status);
	}
	
	public List<Abastecimento> findByPeriodoAndPosto(Integer postoId, Date dataIni, Date dataFim, StatusAbastecimento status){

		return executeResultListQuery("findByPeriodoAndPosto", postoId, dataIni, dataFim, status);
	}
	
}
