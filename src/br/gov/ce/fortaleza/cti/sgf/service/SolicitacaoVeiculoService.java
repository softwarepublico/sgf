/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.SolicitacaoVeiculo;
import br.gov.ce.fortaleza.cti.sgf.entity.UG;
import br.gov.ce.fortaleza.cti.sgf.entity.User;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;
import br.gov.ce.fortaleza.cti.sgf.util.SgfUtil;
import br.gov.ce.fortaleza.cti.sgf.util.StatusSolicitacaoVeiculo;

/**
 * @author Deivid
 * @since 22/12/2009
 */
@Repository
@Transactional
public class SolicitacaoVeiculoService extends BaseService<Integer, SolicitacaoVeiculo> {

	private static final Log logger = LogFactory.getLog(BaseService.class);

	@Autowired
	private VeiculoService veiculoService;

	@SuppressWarnings("unchecked")
	public List<SolicitacaoVeiculo> findByUGAndStatus(UG ug, StatusSolicitacaoVeiculo status) {

		List<SolicitacaoVeiculo> solicitacaoVeiculos = new ArrayList<SolicitacaoVeiculo>();
		try {
			Query query = entityManager.createQuery("select s from SolicitacaoVeiculo s where s.solicitante.pessoa.ua.ug.id = ? and s.status = ?");
			query.setParameter(1, ug.getId());
			query.setParameter(2, status);
			solicitacaoVeiculos = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return solicitacaoVeiculos;
	}

	@SuppressWarnings("unchecked")
	public List<SolicitacaoVeiculo> findSolVeiculoOrgao(Date dtInicial, Date dtFinal, UG orgao) {

		String sql = "select o from SolicitacaoVeiculo o where o.dataHoraSaida >= :inicio and o.dataHoraRetorno <= :fim";
		StringBuffer hql = new StringBuffer(sql);
		if(orgao != null){
			hql.append(" and o.veiculo.ua.ug.id = :ugid");
		}
		Query query = entityManager.createQuery(hql.toString());
		query.setParameter("inicio", dtInicial);
		query.setParameter("fim", dtFinal);
		if(orgao != null){
			query.setParameter("ugid", orgao.getId());
		}
		return query.getResultList();
	}

	/**
	 * 
	 * @param retorna um mapeamento dos kilometros rodados por veículo
	 * @param begin
	 * @param end
	 * @return
	 */
	public Map<Veiculo, Float>  mapKilometragem(UG ug, Date begin, Date end){
		Map<Veiculo, Float> result = new HashMap<Veiculo, Float>();
		try {

			List<Veiculo> veiculos = veiculoService.findByUG(ug);

			for (Veiculo veiculo : veiculos) {
				Query query = entityManager.createQuery("SELECT s FROM SolicitacaoVeiculo s WHERE  s.veiculo.id = ? and s.dataHoraSaida = " +
				"(SELECT min(s1.dataHoraSaida) FROM SolicitacaoVeiculo s1 WHERE  s1.veiculo.id = ? and s1.status != 3 and s1.dataHoraRetorno BETWEEN ? and ?)");
				Query query2 = entityManager.createQuery("SELECT s FROM SolicitacaoVeiculo s WHERE  s.veiculo.id = ? and s.dataHoraRetorno = " +
				"(SELECT max(s1.dataHoraRetorno) FROM SolicitacaoVeiculo s1 WHERE  s1.veiculo.id = ? and s1.status != 3 and s1.dataHoraRetorno BETWEEN ? and ?)");
				query.setParameter(1, veiculo.getId());
				query.setParameter(2, veiculo.getId());
				query.setParameter(3, begin);
				query.setParameter(4, end);
				query2.setParameter(1, veiculo.getId());
				query2.setParameter(2, veiculo.getId());
				query2.setParameter(3, begin);
				query2.setParameter(4, end);

				SolicitacaoVeiculo min = null;
				SolicitacaoVeiculo max = null;
				try {
					min = (SolicitacaoVeiculo) query.getSingleResult();
					max = (SolicitacaoVeiculo) query2.getSingleResult();
				} catch (Exception e) {
				}

				if(min != null && max != null){
					if(min != null){
						Float kmmin = null;
						Float kmmax = null;
						Float kmrod = null;
						try {
							kmmin = min.getKmSaida().floatValue();
							kmmax = max.getKmRetorno().floatValue();
							kmrod = kmmax - kmmin;
						} catch (Exception e) {
						}
						if(result.containsKey(veiculo)){
							result.put(veiculo, kmrod);
						} else {
							result.put(veiculo, kmrod);
						}
					}
				} else {
					if(result.containsKey(veiculo)){
						result.put(veiculo, null);
					} else {
						result.put(veiculo, null);
					}
				}
			}
			return result;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * s.veiculo.status = 4 é o caso em o veículo está indisponível
	 * @param user
	 * @param status
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<SolicitacaoVeiculo> findByUserAndStatus(User user, StatusSolicitacaoVeiculo status) {
		List<SolicitacaoVeiculo> solicitacaoVeiculos = new ArrayList<SolicitacaoVeiculo>();
		try {
			StringBuffer hql = new StringBuffer("select s from SolicitacaoVeiculo s where s.solicitante.codPessoaUsuario = ?");
			if (status != null) {
				hql.append("and s.status = ?");
			}
			Query query = entityManager.createQuery(hql.toString());
			query.setParameter(1, user.getCodPessoaUsuario());
			if (status != null) {
				query.setParameter(2, status);
			}
			return query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return solicitacaoVeiculos;
	}

	@SuppressWarnings("unchecked")
	public List<Veiculo> findVeiculosDisponiveis(SolicitacaoVeiculo solicitacao) {

		List<Veiculo> veiculos = new ArrayList<Veiculo>();
		List<SolicitacaoVeiculo> solicitacaoVeiculos = new ArrayList<SolicitacaoVeiculo>();

		StringBuffer hql = new StringBuffer(
				"SELECT s FROM SolicitacaoVeiculo s WHERE ((s.dataHoraRetorno BETWEEN :saida and :retorno) AND (s.dataHoraSaida BETWEEN :saida and :retorno)) " +
				"or (s.dataHoraRetorno BETWEEN :saida AND :retorno) or (s.dataHoraSaida BETWEEN :saida AND :retorno)");
		UG ug = null;
		if(!SgfUtil.isAdministrador(solicitacao.getSolicitante()) && !SgfUtil.isCoordenador(solicitacao.getSolicitante())){
			ug = solicitacao.getSolicitante().getPessoa().getUa().getUg();
		}
		if (ug != null) {
			hql.append("and s.solicitante.pessoa.ua.ug.id = :ugId");
		}

		Query query = entityManager.createQuery(hql.toString());
		query.setParameter("saida", solicitacao.getDataHoraSaida());
		query.setParameter("retorno", solicitacao.getDataHoraRetorno());

		if (ug != null) {
			query.setParameter("ugId", ug.getId());
		}

		solicitacaoVeiculos = query.getResultList();
		List<Veiculo> remove = new ArrayList<Veiculo>();
		for (SolicitacaoVeiculo sol : solicitacaoVeiculos) {
			remove.add(sol.getVeiculo());
		}

		if(SgfUtil.isAdministrador(solicitacao.getSolicitante())){
			veiculos = veiculoService.findAll();
		} else {
			veiculos = veiculoService.findByUG(ug);
		}
		veiculos.removeAll(remove);
		return veiculos;
	}

	@SuppressWarnings("unchecked")
	public List<SolicitacaoVeiculo> pesquisarSolUsuario(User usuario, StatusSolicitacaoVeiculo status) {
		List<SolicitacaoVeiculo> solicitacaoVeiculos = new ArrayList<SolicitacaoVeiculo>();
		Query query = entityManager.createQuery("select s from SolicitacaoVeiculo s where s.solicitante.codPessoaUsuario = ? and s.status = ?");
		query.setParameter(1, usuario.getCodPessoaUsuario());
		query.setParameter(2, status);
		solicitacaoVeiculos = query.getResultList();
		return solicitacaoVeiculos;
	}

	public List<SolicitacaoVeiculo> pesquisarSolicitacaoUser(Integer id, StatusSolicitacaoVeiculo status) {
		return executeResultListQuery("findByUsuarioStatus", id, status);
	}

	@SuppressWarnings("unchecked")
	public List<SolicitacaoVeiculo> findSolicitacoesVeiculo(Veiculo veiculo) {
		List<SolicitacaoVeiculo> solicitacaoVeiculos = null;
		Query query = entityManager.createQuery("select o from SolicitacaoVeiculo o where s.veiculo.id = :id");
		query.setParameter("id", veiculo.getId());
		solicitacaoVeiculos = query.getResultList();
		return solicitacaoVeiculos;
	}
}
