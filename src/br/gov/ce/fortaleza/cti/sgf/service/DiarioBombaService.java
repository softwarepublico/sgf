/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.Bomba;
import br.gov.ce.fortaleza.cti.sgf.entity.DiarioBomba;
import br.gov.ce.fortaleza.cti.sgf.entity.Posto;

/**
 * @author Deivid
 * @since 11/12/09
 */
@Repository
@Transactional
public class DiarioBombaService extends BaseService<Integer, DiarioBomba>{

	public DiarioBomba findLastDiariaByDate(Bomba bomba) {
		Query query = entityManager.createQuery("select db from DiarioBomba db where db.dataCadastro = (select max(db.dataCadastro) " +
		"from DiarioBomba db where db.bomba = :bomba) and db.bomba = :bomba");
		query.setParameter("bomba", bomba);
		return (DiarioBomba) query.getSingleResult();
	}

	/**
	 * Busca o registro da última diária da bomba
	 * @param bomba
	 * @param day
	 * @return
	 */

	public DiarioBomba findCurrentDiaryByBomba(Integer bombaId) {
		try {
			Query query = entityManager.createQuery("select d from DiarioBomba d where d.bomba.id = ? and d.horaInicial = (select max(db.horaInicial) from DiarioBomba db where db.bomba.id = ?)");
			query.setParameter(1, bombaId);
			query.setParameter(2, bombaId);
			return (DiarioBomba) query.getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}

	public Date findUltimaDiariaByBomba(Integer bombaId) {
		Query query = entityManager.createQuery("select max(db.horaInicial) from DiarioBomba db where db.bomba.id = :id");
		query.setParameter("id", bombaId);
		return (Date) query.getSingleResult();
	}

	/**
	 * Esse método está trazendo todas as diárias e gerando desperdício de tráfego
	 * @param bombaSelecionada
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<DiarioBomba> findDiariasByBomba(Bomba bombaSelecionada) {
		List<DiarioBomba> diarioBombas = executeResultListGenericQuery("findDiariasByBomba", bombaSelecionada);
		return diarioBombas;
	}

	@SuppressWarnings("unchecked")
	public List<DiarioBomba> findByPeriodoPosto(Date dtInicial, Date dtFinal, Posto posto) {

		List<DiarioBomba> diarioBombas = new ArrayList<DiarioBomba>();
		StringBuffer hql = new StringBuffer("select d from DiarioBomba d where ");
		if(dtInicial != null && dtFinal != null){
			hql.append("d.dtDia >= :dtInicial and d.dtDia <= :dtFinal ");
		}
		if(posto != null){
			hql.append("and d.bomba.posto.codPosto = :postoID ");
		}

		hql.append("order by d.dtDia");
		Query query = entityManager.createQuery(hql.toString()); 
		query.setParameter("dtInicial", dtInicial);
		query.setParameter("dtFinal", dtFinal);
		if(posto != null){
			query.setParameter("postoID", posto.getCodPosto());
		}
		diarioBombas = query.getResultList();
		return diarioBombas;
	}

	@SuppressWarnings("unchecked")
	public List<Posto> findDiariosPeriodoPosto(Date dtInicial, Date dtFinal) {
		List<Posto> postos = null;
		StringBuffer hql = new StringBuffer("select distinct(d.bomba.posto) from DiarioBomba d where ");
		if(dtInicial != null && dtFinal != null){
			hql.append("d.dtDia >= :dtInicial and d.dtDia <= :dtFinal ");
		}
		hql.append("order by d.bomba.posto.descricao");
		Query query = entityManager.createQuery(hql.toString()); 
		query.setParameter("dtInicial", dtInicial);
		query.setParameter("dtFinal", dtFinal);
		postos = query.getResultList();
		return postos;
	}
}
