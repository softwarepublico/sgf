package br.gov.ce.fortaleza.cti.sgf.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.Transmissao;

@Repository
@Transactional
public class TransmissaoService extends BaseService<Long, Transmissao> {

	@Transactional(readOnly = true)
	public List<Transmissao> findByVeiculo(Integer veiculoId, Date dataHoraInicio, Date dataHoraFim) {
		return executeResultListQuery("findByVeiculo", veiculoId, dataHoraInicio, dataHoraFim);
	}

	@Transactional(readOnly = true)
	public List<Transmissao> findByVeiculoVelocidade(Integer veiculoId, Date dataInicio, Date dataFim, Float velocidade) {
		return executeResultListQuery("findByVeiculoVelocidade", veiculoId, dataInicio, dataFim, velocidade);
	}

	@Transactional(readOnly = true)
	public Map<Integer, List<Transmissao>> findByVeiculoList(List<Integer> veiculoIds, Date dataInicio, Date dataFim) {
		Map<Integer, List<Transmissao>> result = new HashMap<Integer, List<Transmissao>>();
		for (Integer veiculoId : veiculoIds) {
			result.put(veiculoId, new ArrayList<Transmissao>());
		}
		List<Transmissao> resultQuery = createVeiculoListQuery(veiculoIds, dataInicio, dataFim);
		for (Transmissao Transmissao : resultQuery) {
			result.get(Transmissao.getVeiculoId()).add(Transmissao);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List<Transmissao> createVeiculoListQuery(List<Integer> veiculoIds, Date dataInicio, Date dataFim) {
		if (veiculoIds != null && veiculoIds.size() > 0) {
			String idsList = veiculoIds.toString().replaceAll("\\[", "(").replaceAll("\\]", ")");
			Query query = entityManager.createQuery("SELECT p FROM Transmissao p WHERE p.veiculoId IN " + idsList +
					" AND p.dataTransmissao between ? AND ? AND p.ponto.id > 0 ORDER BY p.dataTransmissao");
			query.setParameter(1, dataInicio);
			query.setParameter(2, dataFim);
			return (List<Transmissao>) query.getResultList();
		} else {
			return new ArrayList<Transmissao>();
		}
	}
}