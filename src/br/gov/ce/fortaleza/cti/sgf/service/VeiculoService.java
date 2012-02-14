package br.gov.ce.fortaleza.cti.sgf.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.postgis.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.Transmissao;
import br.gov.ce.fortaleza.cti.sgf.entity.UA;
import br.gov.ce.fortaleza.cti.sgf.entity.UG;
import br.gov.ce.fortaleza.cti.sgf.entity.User;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;
import br.gov.ce.fortaleza.cti.sgf.util.DateUtil;
import br.gov.ce.fortaleza.cti.sgf.util.PontoDTO;
import br.gov.ce.fortaleza.cti.sgf.util.SgfUtil;

@Repository
@Transactional
public class VeiculoService extends BaseService<Integer, Veiculo>{
	
	@Autowired
	private TransmissaoService transmissaoService;

	public List<Veiculo> findAll(){
		List<Veiculo> result = new ArrayList<Veiculo>();
		User user = SgfUtil.usuarioLogado();
		if(SgfUtil.isAdministrador(user)  || SgfUtil.isCoordenador(user)){
			result = retrieveAll();
		} else {
			UA ua = SgfUtil.usuarioLogado().getPessoa().getUa();
			if(ua != null){
				result = retrieveByUG(ua.getUg().getId());
			}
		}
		Collections.sort(result, new Comparator<Veiculo>() {
			public int compare(Veiculo o1, Veiculo o2) {
				return o1.getPlaca().compareTo(o2.getPlaca());
			}
		});
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Veiculo> findVeiculobyModelo(String filter) {
		List<Veiculo> listaVeiculo = new ArrayList<Veiculo>();
		Query query = entityManager.createQuery("Select object(v) from Veiculo v where lower(v.modelo.descricao) like lower(:busca)");
		query.setParameter("busca", "%"+filter+"%");
		listaVeiculo = query.getResultList();
		return listaVeiculo;
	}

	@SuppressWarnings("unchecked")
	public List<Veiculo> retrieveByUG(String ug){
		Query query = entityManager.createQuery("Select v from Veiculo v where v.ua.ug.id = ? ");
		query.setParameter(1, ug);
		List<Veiculo> result =  query.getResultList();
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Veiculo> findByPCR(String p, String c, String r){
		List<Veiculo> result = executeResultListGenericQuery("findByPCR", p, c, r);
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Veiculo> findByUGPCR(String ugId, String p, String c, String r){
		List<Veiculo> result = executeResultListGenericQuery("findByUGPCR", ugId, p, c, r);
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Veiculo> findByUG(UG ug) {
		List<Veiculo> veiculos = new ArrayList<Veiculo>();
		if(ug != null){
			Query query = entityManager.createQuery("select o from Veiculo o where o.ua.ug.id = :id");
			query.setParameter("id", ug.getId());
			veiculos = query.getResultList();
		}
		return veiculos;
	}

	public List<Veiculo> findByPlaca(String placa){
		List<Veiculo> result = executeResultListQuery("findByPlaca", placa.toUpperCase());
		return result;
	}

	public Veiculo findByPlacaSingle(String placa){
		return executeSingleResultQuery("findByPlaca", placa);
	}
	
	public List<Integer> veiculoIds(String ugId){
		StringBuilder sql = new StringBuilder("SELECT v.id FROM Veiculo v WHERE 1 = 1");
		if(ugId != null){
			sql.append(" and v.ua.ug.id = :id");
		}
		Query query = entityManager.createQuery(sql.toString());
		if(ugId != null){
			query.setParameter("id", ugId);
		}
		return query.getResultList();
	}
	/**
	 * Busca a quantidade abastecida no mês para o veículo informado
	 * 
	 * @param veiculo
	 * @return Double
	 */
	public Double findQtdAbastecida(Veiculo veiculo) {
		Double result = 0.0;
		Date data = new Date();
		Query query = entityManager.createNamedQuery("AtendimentoAbastecimento.findCota");
		query.setParameter("veiculo", veiculo);
		query.setParameter("mes", data.getMonth());
		result = (Double) query.getSingleResult();
		return result;
	}
	
//	@Transactional(readOnly = true)
//	public List<PontoDTO> searchPontosMonitoramento(List<Veiculo> veiculos, boolean exibirPontos, Float velocidadeMaxima, Date init) {
//
//		Map<Integer, List<Transmissao>> mapTransmissoes = null;
//		List<Integer> veiculoIds = new ArrayList<Integer>();
//		for (Veiculo veiculo : veiculos) {
//			veiculoIds.add(veiculo.getId());
//		}
//
//		if (exibirPontos) {
//			Date end = DateUtil.getDateEndDay();
//			mapTransmissoes  = transmissaoService.findByVeiculoList(veiculoIds, init, end);
//		}
//
//		int k = 0;
//		List<PontoDTO> pontos = new ArrayList<PontoDTO>();
//
//		try {
//			for (Veiculo veiculo : veiculos) {
//				if (veiculo.getDataTransmissao() != null) {
//					PontoDTO m = new PontoDTO();
//
//					try {
//						m.setId(veiculo.getId());
//						m.setNome(veiculo.getModelo().getDescricao().replace("|", ":"));
//						m.setPlaca(veiculo.getPlaca());
//						m.setVelocidade(veiculo.getVelocidade());
//						m.setOdometro(veiculo.getOdometro());
//							Float kmAtual = veiculo.getOdometro() == null ? 0f: veiculo.getOdometro();
//						m.setKmAtual(kmAtual);
//						m.setDataTransmissao(veiculo.getDataTransmissao());
//							String pontoNome = veiculo.getPontoProximo() == null ? "" : veiculo.getPontoProximo().getDescricao();
//						m.setPontoMaisProximo(pontoNome);
//						m.setDistPontoMaisProximo(veiculo.getDistancia());
//							String marcaDescricao = veiculo.getModelo() == null ? "" : veiculo.getModelo().getMarca().getDescricao();
//						m.setMarca(marcaDescricao);
//						m.setCor(veiculo.getCor());
//							String modeloDescricao = veiculo.getModelo() == null ? "" : veiculo.getModelo().getDescricao();
//						m.setModelo(modeloDescricao);
//						m.setAno(veiculo.getAnoFabricacao());
//
//					} catch (Exception e) {
//						//logger.error(e.getMessage(), e);
//					}
//
//					if (veiculo.getGeometry() != null) {
//						m.setLat((float) ((Point)veiculo.getGeometry()).x);
//						m.setLng((float) ((Point)veiculo.getGeometry()).y);
//					}
//
//					if (exibirPontos) {
//						m.setVelocidadeMaxima(velocidadeMaxima);
//						List<PontoDTO> rastro = new ArrayList<PontoDTO>();
//						List<Transmissao> historicoTransmissoes = mapTransmissoes.get(veiculo.getId());
//
//						if (historicoTransmissoes != null && historicoTransmissoes.size() > 0) {
//							for (Transmissao transmissao : historicoTransmissoes) {
//								try {
//									PontoDTO ponto = new PontoDTO();
//									ponto.setNome(veiculo.getModelo().getDescricao());
//									ponto.setPlaca(veiculo.getPlaca());
//									ponto.setPontoMaisProximo(transmissao.getPonto().getDescricao());
//									ponto.setDistPontoMaisProximo(transmissao.getDistancia());
//									ponto.setVelocidade(transmissao.getVelocidade());
//									ponto.setDataTransmissao(transmissao.getDataTransmissao());
////									ponto.setLat((float) transmissao.getGeometry().getX());
////									ponto.setLng((float) transmissao.getGeometry().getY());
//									ponto.setLat((float) ((Point)veiculo.getGeometry()).x);
//									ponto.setLng((float) ((Point)veiculo.getGeometry()).y);
//									ponto.setVelocidadeMaxima(velocidadeMaxima);
//									rastro.add(ponto);
//								} catch (Exception e) {
//									//logger.error(e.getMessage(), e);
//								}
//							}
//						}
//						m.setRastro(rastro);
//					}
//
//					m.setIndex(k++);
//					pontos.add(m);
//				}
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return classificar(pontos);
//	}
//
//	@Transactional(readOnly = true)
//	private List<PontoDTO> classificar(List<PontoDTO> list) {
//		Collections.sort(list, new Comparator<PontoDTO>() {
//			public int compare(PontoDTO m1, PontoDTO m2) {
//				return m1.getDataTransmissao().before(m2.getDataTransmissao()) ? 1 : -1;
//			}
//		});
//		return list;
//	}
}