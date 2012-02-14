package br.gov.ce.fortaleza.cti.sgf.service;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.AtendimentoAbastecimento;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;

/**
 * 
 * @author Deivid
 * @since 17/01/2010
 *
 */
@Transactional
@Repository
public class AtendimentoService extends BaseService<Integer, AtendimentoAbastecimento>{


	public List<AtendimentoAbastecimento> findByPeriodo(String ug, String veiculo, Date dataInicio, Date dataFim){

		StringBuffer str = new StringBuffer("select o from AtendimentoAbastecimento o where o.data between ? and ?");

		if(ug != null){

			str.append(" and o.abastecimento.veiculo.ua.ug.id = :ug");
		}

		if(veiculo != null){

			str.append(" and o.abastecimento.veiculo.id = :veiculo");
		}

		str.append(" order by o.data asc");

		Query query = entityManager.createQuery(str.toString());

		query.setParameter(1, dataInicio);

		query.setParameter(2, dataFim);

		if(ug != null){

			query.setParameter("ug", ug);
		}

		if(veiculo != null){

			query.setParameter("veiculo", veiculo);
		}

		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<AtendimentoAbastecimento> findByMesAno(Integer mes, Integer ano){
		Query query = entityManager.createQuery("select o from AtendimentoAbastecimento o where " +
		"month(o.data) = ? and year(o.data) = ?) order by o.data asc");
		query.setParameter(1, mes);
		query.setParameter(2, ano);
		return query.getResultList();
	}
	/**
	 * Busca a quantidade abastecida no mês pelo veículo
	 * 
	 * @param veiculo
	 * @return
	 */
	public Double findQtdAbastecida(Veiculo veiculo) {
		Double result = 0.0;
		Query query = entityManager.createNamedQuery("AtendimentoAbastecimento.findCota");
		query.setParameter("veiculo", veiculo);
		query.setParameter("mes", new Date().getMonth());
		result = (Double) query.getSingleResult();
		return result;
	}
}
