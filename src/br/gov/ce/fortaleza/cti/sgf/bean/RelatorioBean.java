package br.gov.ce.fortaleza.cti.sgf.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.AtendimentoAbastecimento;
import br.gov.ce.fortaleza.cti.sgf.entity.Cota;
import br.gov.ce.fortaleza.cti.sgf.entity.DiarioBomba;
import br.gov.ce.fortaleza.cti.sgf.entity.ItemRequisicao;
import br.gov.ce.fortaleza.cti.sgf.entity.Motorista;
import br.gov.ce.fortaleza.cti.sgf.entity.Multa;
import br.gov.ce.fortaleza.cti.sgf.entity.Posto;
import br.gov.ce.fortaleza.cti.sgf.entity.RequisicaoManutencao;
import br.gov.ce.fortaleza.cti.sgf.entity.SolicitacaoVeiculo;
import br.gov.ce.fortaleza.cti.sgf.entity.UG;
import br.gov.ce.fortaleza.cti.sgf.entity.User;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;
import br.gov.ce.fortaleza.cti.sgf.relatorio.GeradorRelatorio;
import br.gov.ce.fortaleza.cti.sgf.service.AbastecimentoService;
import br.gov.ce.fortaleza.cti.sgf.service.AtendimentoService;
import br.gov.ce.fortaleza.cti.sgf.service.BaseService;
import br.gov.ce.fortaleza.cti.sgf.service.CotaService;
import br.gov.ce.fortaleza.cti.sgf.service.DiarioBombaService;
import br.gov.ce.fortaleza.cti.sgf.service.ItemRequisicaoService;
import br.gov.ce.fortaleza.cti.sgf.service.MotoristaService;
import br.gov.ce.fortaleza.cti.sgf.service.MultaService;
import br.gov.ce.fortaleza.cti.sgf.service.PostoService;
import br.gov.ce.fortaleza.cti.sgf.service.RequisicaoManutencaoService;
import br.gov.ce.fortaleza.cti.sgf.service.SolicitacaoVeiculoService;
import br.gov.ce.fortaleza.cti.sgf.service.UGService;
import br.gov.ce.fortaleza.cti.sgf.service.VeiculoService;
import br.gov.ce.fortaleza.cti.sgf.util.Constants;
import br.gov.ce.fortaleza.cti.sgf.util.DateUtil;
import br.gov.ce.fortaleza.cti.sgf.util.JSFUtil;
import br.gov.ce.fortaleza.cti.sgf.util.RelatorioDTO;
import br.gov.ce.fortaleza.cti.sgf.util.RelatorioUtil;
import br.gov.ce.fortaleza.cti.sgf.util.SgfUtil;

@Scope("session")
@Component("relatorioBean")
public class RelatorioBean extends EntityBean<Integer, RelatorioDTO> {

	private static final Log logger = LogFactory.getLog(RelatorioBean.class);

	@Autowired
	public MultaService multaService;

	@Autowired
	public MotoristaService motoristaService;

	@Autowired
	public UGService ugService;

	@Autowired
	public SolicitacaoVeiculoService solicitacaoService;

	@Autowired
	public PostoService postoService;

	@Autowired
	public DiarioBombaService diarioBombaService;

	@Autowired
	public VeiculoService veiculoService;

	@Autowired
	public AbastecimentoService abastecimentoService;

	@Autowired
	public AtendimentoService atendimentoService;

	@Autowired
	public CotaService cotaService;

	@Autowired
	public RequisicaoManutencaoService manutencaoService;

	@Autowired
	public ItemRequisicaoService itemManutencaoService;

	private UG orgao = new UG();
	private List<UG> orgaos = new ArrayList<UG>();
	private Posto posto;
	private List<Posto> postos;
	private Motorista motorista = new Motorista();
	private Veiculo veiculo;
	private DiarioBomba diarioBomba;
	private List<Veiculo> veiculos = new ArrayList<Veiculo>();
	private List<Motorista> motoristas = new ArrayList<Motorista>();
	private List<Multa> multas = new ArrayList<Multa>();
	private List<RelatorioDTO> result = new ArrayList<RelatorioDTO>();
	List<SelectItem> meses = DateUtil.getSelectItemMeses();

	private boolean generate = false;
	private boolean allugs = false;
	private boolean mostrarVeiculos = false;
	private boolean mostrarMotoristas = false;
	private Integer searchId = 1;
	private Integer mes;
	private Integer ano;
	private String searchFlag = "SEARCH_UG";
	private String nomeRelatorio;
	private Date dtInicial;
	private Date dtFinal;

	private final String relMotoristaPontuacao = "relat.motorista.pontuacao";
	private final String relDiarioBombas = "relat.diario.bomba";
	private final String relVeiculoMulta = "relat.veiculo.multa";
	private final String relConferenciaAbastecimento = "relat.conferencia.abastecimento";
	private final String relConsolidadoMensal = "relat.consolidado.mensal";
	private final String relVeiculosEmManutencao = "relat.veiculos.em.manutencao";
	private final String relHistoricoVeiculoManutencao = "relat.historico.veiculo.manutencao";
	private final String relHistoricoTrocaPneus = "relat.historico.troca.pneus";
	private final String relVeiculosSemRetornoManutencao = "relat.veiculos.sem.retorno.manutencao";
	private final String relMultasVeiculoByUG = "relat.multas.veiculo.ug";
	private final String relMultasVeiculos = "relat.multas.veiculo";
	private final String relMultasVeiculoByMotorista = "relat.multas.veiculo.motorista";
	private final String relSolicitacaoVeiculo = "relat.solicitacao.veiculo";

	private final String relInformacoesVeiculo = "relat.informacoes.veiculo";
	private final String relInformacoesKmsRodadosVeiculo = "relat.informacoes.kms.rodados.veiculo";

	@Override
	protected RelatorioDTO createNewEntity() {
		return new RelatorioDTO();
	}

	@Override
	protected Integer retrieveEntityId(RelatorioDTO entity) {
		throw new IllegalStateException("Não implementado");
	}

	@Override
	protected BaseService<Integer, RelatorioDTO> retrieveEntityService() {
		throw new IllegalStateException("Não implementado");
	}

	public List<SelectItem> getMesList(){
		return DateUtil.getSelectItemMeses();
	}

	public List<SelectItem> getAnoList(){
		return DateUtil.getSelectItemAnos();
	}

	public String relatorioInformacoesVeiculos() {
		this.nomeRelatorio = this.relInformacoesVeiculo;
		setCurrentState(RelatorioDTO.INFORMACOES_VEICULO);
		setCurrentBean(currentBeanName());
		this.entities = null;
		this.generate = false;
		return SUCCESS;
	}

	public String relatorioMotoristaPontuacao() {
		this.nomeRelatorio = this.relMotoristaPontuacao;
		setCurrentState(RelatorioDTO.PONTUACAO_MOTORISTA);
		setCurrentBean(currentBeanName());
		this.entities = null;
		this.generate = false;
		return SUCCESS;
	}

	public String relatorioVeiculoMulta() {
		this.nomeRelatorio = this.relVeiculoMulta;
		setCurrentState(RelatorioDTO.VEICULO_MULTA);
		setCurrentBean(currentBeanName());
		this.entities = null;
		return SUCCESS;
	}

	public String relatorioMultasOrgao() {
		setCurrentState(RelatorioDTO.MULTAS_VEICULO);
		setCurrentBean(currentBeanName());
		this.entities = null;
		return SUCCESS;
	}

	public String relatorioMultasVeiculo() {
		this.nomeRelatorio = this.relMultasVeiculos;
		setCurrentState(RelatorioDTO.MULTAS_VEICULO);
		setCurrentBean(currentBeanName());
		this.entities = null;
		return SUCCESS;
	}

	public String relatorioMultasMotorista() {
		this.nomeRelatorio = this.relMultasVeiculoByMotorista;
		setCurrentState(RelatorioDTO.MULTAS_VEICULO);
		setCurrentBean(currentBeanName());
		this.entities = null;
		return SUCCESS;
	}

	public String relatorioConsumoCombustivel() {
		this.nomeRelatorio = this.relConferenciaAbastecimento;
		setCurrentState(RelatorioDTO.CONFERENCIA_ABASTECIMENTO);
		setCurrentBean(currentBeanName());
		this.entities = null;
		return SUCCESS;
	}

	public String relatorioConsolidadoMensal() {
		this.nomeRelatorio = this.relConsolidadoMensal;
		setCurrentState(RelatorioDTO.CONSOLIDADO_MENSAL);
		setCurrentBean(currentBeanName());
		this.entities = null;
		return SUCCESS;
	}	

	public String relatorioSolicitacaoVeiculo(){
		this.nomeRelatorio = relSolicitacaoVeiculo;
		setCurrentState(RelatorioDTO.SOLICITACAO_VEICULO);
		setCurrentBean(currentBeanName());
		this.entities = null;
		return SUCCESS;
	}

	public String relatorioDiarioBomba() {
		nomeRelatorio = relDiarioBombas;
		setCurrentState(RelatorioDTO.DIARIO_BOMBA);
		setCurrentBean(currentBeanName());
		this.entities = null;
		return SUCCESS;
	}

	public String relatorioVeiculosEmManutencao() {
		this.nomeRelatorio = this.relVeiculosEmManutencao;
		setCurrentState(RelatorioDTO.VEICULOS_EM_MANUTENCAO);
		setCurrentBean(currentBeanName());
		this.entities = null;
		return SUCCESS;
	}

	public String relatorioVeiculosSemRetorno() {
		this.nomeRelatorio = this.relVeiculosSemRetornoManutencao;
		setCurrentState(RelatorioDTO.VEICULOS_SEM_RETORNO_MANUTENCAO);
		setCurrentBean(currentBeanName());
		this.entities = null;
		return SUCCESS;
	}

	public String relatorioHistoricoTrocaPneus() {
		this.nomeRelatorio = this.relHistoricoTrocaPneus;
		setCurrentState(RelatorioDTO.HISTORICO_TROCA_PNEUS);
		setCurrentBean(currentBeanName());
		this.entities = null;
		return SUCCESS;
	}

	public String relatorioHistoricoVeiculoManutencao() {
		this.veiculos = new ArrayList<Veiculo>();
		this.orgao = null;
		this.nomeRelatorio = this.relHistoricoVeiculoManutencao;
		setCurrentState(RelatorioDTO.HISTORICO_VEICULO_MANUTENCAO);
		setCurrentBean(currentBeanName());
		this.entities = null;
		return SUCCESS;
	}
	/**
	 * métodos de chamada do relatório de kilometros rodados
	 * @return
	 */
	public String relatorioKilometrosRodadosVeiculo() {
		this.veiculos = new ArrayList<Veiculo>();
		this.orgao = null;
		this.nomeRelatorio = this.relInformacoesKmsRodadosVeiculo;
		setCurrentState(RelatorioDTO.KILOMETROS_RODADOS_VEICULO);
		setCurrentBean(currentBeanName());
		this.entities = null;
		return SUCCESS;
	}

	public boolean isRelatorioInformacoesVeiculoState() {
		return RelatorioDTO.INFORMACOES_VEICULO.equals(getCurrentState());
	}

	public boolean isRelatorioSolicitacaoVeiculoState() {
		return RelatorioDTO.SOLICITACAO_VEICULO.equals(getCurrentState());
	}

	public boolean isRelatorioConsumoCombustivelState() {
		return RelatorioDTO.CONFERENCIA_ABASTECIMENTO.equals(getCurrentState());
	}

	public boolean isRelatorioConsolidadoMensalState() {
		return RelatorioDTO.CONSOLIDADO_MENSAL.equals(getCurrentState());
	}

	public boolean isRelatorioDiarioBombaState() {
		return RelatorioDTO.DIARIO_BOMBA.equals(getCurrentState());
	}

	public boolean isRelatorioMotoristaPontuacaoState() {
		return RelatorioDTO.PONTUACAO_MOTORISTA.equals(getCurrentState());
	}

	public boolean isRelatorioVeiculoMultaState() {
		return RelatorioDTO.VEICULO_MULTA.equals(getCurrentState());
	}

	public boolean isRelatorioMultasOrgaoState() {
		return RelatorioDTO.MULTAS_VEICULO.equals(getCurrentState());
	}

	public boolean isRelatorioHistoricoVeiculoManutencaoState() {
		return RelatorioDTO.HISTORICO_VEICULO_MANUTENCAO.equals(getCurrentState());
	}

	public boolean isRelatorioVeiculosSemRetornoState() {
		return RelatorioDTO.VEICULOS_SEM_RETORNO_MANUTENCAO.equals(getCurrentState());
	}

	public boolean isRelatorioVeiculosEmManutencaoState() {
		return RelatorioDTO.VEICULOS_EM_MANUTENCAO.equals(getCurrentState());
	}

	public boolean isRelatorioHistoricoTrocaPneusState() {
		return RelatorioDTO.HISTORICO_TROCA_PNEUS.equals(getCurrentState());
	}

	public boolean isRelatorioKilometrosRodadosState() {
		return RelatorioDTO.KILOMETROS_RODADOS_VEICULO.equals(getCurrentState());
	}

	public String populate() {
		if (this.searchId == 1) {
			this.motoristas = new ArrayList<Motorista>();
			this.veiculos = new ArrayList<Veiculo>();
			this.mostrarMotoristas = false;
			this.mostrarVeiculos = false;
		} else if (this.searchId == 2) {
			populateMotoristas();
			this.mostrarMotoristas = true;
			this.mostrarVeiculos = false;
		} else if (this.searchId == 3) {
			populateVeiculos();
			this.mostrarVeiculos = true;
			this.mostrarMotoristas = false;
		} else {
			this.motoristas = new ArrayList<Motorista>();
			this.veiculos = new ArrayList<Veiculo>();
			this.mostrarMotoristas = false;
			this.mostrarVeiculos = false;
		}
		this.entities = new ArrayList<RelatorioDTO>();
		return SUCCESS;
	}

	public String populateMultas() {
		if (this.searchId == 2) {
			populateMotoristas();
		} else if (this.searchId == 3) {
			populateVeiculos();
		}
		return SUCCESS;
	}

	public String populateMotoristas() {
		User user = SgfUtil.usuarioLogado();
		if (this.orgao != null) {
			this.motoristas = motoristaService.findByUG(orgao.getId());
		} else {
			if(SgfUtil.isAdministrador(user)){
				this.motoristas = motoristaService.retrieveAll();
			}
		}
		return SUCCESS;
	}

	public String populateVeiculos() {
		this.entities = new ArrayList<RelatorioDTO>();
		User user = SgfUtil.usuarioLogado();
		this.veiculos = new ArrayList<Veiculo>();
		if (this.orgao != null) {
			this.veiculos = veiculoService.retrieveByUG(this.orgao.getId());
		} else {
			if(SgfUtil.isAdministrador(user)){
				this.veiculos = veiculoService.retrieveAll();
			}
		}
		Collections.sort(this.veiculos, new Comparator<Veiculo>() {
			public int compare(Veiculo o1, Veiculo o2) {
				return o1.getPlaca().compareTo(o2.getPlaca());
			}
		});
		return SUCCESS;
	}

	public String consultarSolicitacaoVeiculo(){
		if (!DateUtil.compareDate(this.dtInicial, this.dtFinal)) {
			JSFUtil.getInstance().addErrorMessage("msg.error.datas.inconsistentes");
			return FAIL;
		}
		this.entities = new ArrayList<RelatorioDTO>();
		List<SolicitacaoVeiculo> solicitacoes;
		if(this.orgao != null){
			solicitacoes = solicitacaoService.findSolVeiculoOrgao(this.dtInicial, this.dtFinal, this.orgao);
		} else {
			solicitacoes = solicitacaoService.findSolVeiculoOrgao(this.dtInicial, this.dtFinal, null);
		}
		Map<UG, Map<Veiculo, List<SolicitacaoVeiculo>>> map = new HashMap<UG, Map<Veiculo,List<SolicitacaoVeiculo>>>();
		if(solicitacoes.size() > 0){
			for (SolicitacaoVeiculo solicitacao : solicitacoes) {
				UG ug = solicitacao.getVeiculo().getUa().getUg();
				if(map.containsKey(ug)){
					Map<Veiculo, List<SolicitacaoVeiculo>>  mapVeiculo = map.get(ug);
					Veiculo veiculo = solicitacao.getVeiculo();
					if(mapVeiculo.containsKey(veiculo)){
						mapVeiculo.get(veiculo).add(solicitacao);
					} else {
						List<SolicitacaoVeiculo> list = new ArrayList<SolicitacaoVeiculo>();
						list.add(solicitacao);
						mapVeiculo.put(veiculo, list);
					}
				} else {
					Map<Veiculo, List<SolicitacaoVeiculo>>  mapVeiculo = new HashMap<Veiculo, List<SolicitacaoVeiculo>>();
					Veiculo veiculo = solicitacao.getVeiculo();
					List<SolicitacaoVeiculo> list = new ArrayList<SolicitacaoVeiculo>();
					list.add(solicitacao);
					mapVeiculo.put(veiculo, list);
					map.put(ug, mapVeiculo);
				}
			}
			for (UG ug : map.keySet()) {
				RelatorioDTO relatorio = new RelatorioDTO();
				relatorio.setOrgao(ug);
				relatorio.setRelatorios(new ArrayList<RelatorioDTO>());
				Map<Veiculo, List<SolicitacaoVeiculo>> mapVeiculo = map.get(ug);
				for (Veiculo veiculo : mapVeiculo.keySet()) {
					RelatorioDTO rel = new RelatorioDTO();
					rel.setVeiculo(veiculo);
					rel.setOrgao(ug);
					rel.setRelatorios(new ArrayList<RelatorioDTO>());
					List<SolicitacaoVeiculo> result = mapVeiculo.get(veiculo);
					for (SolicitacaoVeiculo s : result) {
						RelatorioDTO dto = new RelatorioDTO();
						dto.setSolicitacaoVeiculo(s);
						dto.setVeiculo(veiculo);
						dto.setOrgao(ug);
						dto.setDtInicial(this.dtInicial);
						dto.setDtFinal(this.dtFinal);
						rel.getRelatorios().add(dto);
						this.result.add(dto);
					}
					relatorio.getRelatorios().add(rel);
				}
				this.entities.add(relatorio);
			}
		}
		return SUCCESS;
	}

	public String consultarDiarios() {
		this.entities = new ArrayList<RelatorioDTO>();
		if (!DateUtil.compareDate(this.dtInicial, dtFinal)) {
			JSFUtil.getInstance().addErrorMessage("msg.error.datas.inconsistentes");
			return FAIL;
		}
		this.dtInicial = DateUtil.getDateStartDay(this.dtInicial);
		this.dtFinal = DateUtil.getDateEndDay(this.dtFinal);
		if(this.posto != null){
			RelatorioDTO relatorio = new RelatorioDTO();
			relatorio.setRelatorios(new ArrayList<RelatorioDTO>());
			relatorio.setPosto(this.posto);
			List<RelatorioDTO> relatorios = new ArrayList<RelatorioDTO>();
			List<DiarioBomba> diarioBombas = diarioBombaService.findByPeriodoPosto(this.dtInicial, this.dtFinal, this.posto);
			relatorio.getPosto().setDiarioBombas(diarioBombas);
			Float total = 0F;
			for (DiarioBomba d : diarioBombas) {
				RelatorioDTO dto = new RelatorioDTO();
				if(d.getValorFinal() != null && d.getValorInicial() != null){
					if(d.getValorFinal() < d.getValorInicial()){
						if(d.getValorFinal() < Constants.LIM_INF_LEIT_BOMBA_COMBUSTIVEL && d.getValorInicial() > Constants.LIM_SUP_LEIT_BOMBA_COMBUSTIVEL){
							Float la =  ((Constants.LIMITE_LEIT_BOMBA_COMBUSTIVEL - d.getValorInicial()) + d.getValorFinal());
							d.setLitrosAbastecidos(la);
							total+=la;
						}
					} else {
						Float la = d.getValorFinal() - d.getValorInicial();
						d.setLitrosAbastecidos(la);
						total+=la;
					}
				}
				dto.setDiarioBomba(d);
				dto.setPosto(this.posto);
				dto.setDtInicial(this.dtInicial);
				dto.setDtFinal(this.dtFinal);
				relatorios.add(dto);
			}
			relatorio.getRelatorios().addAll(relatorios);
			relatorio.setConsumoPosto(total);
			this.entities.add(relatorio);

		} else {
			this.postos = diarioBombaService.findDiariosPeriodoPosto(this.dtInicial, this.dtFinal);
			for (Posto posto : postos) {
				List<DiarioBomba> diarioBombasAux = diarioBombaService.findByPeriodoPosto(this.dtInicial, this.dtFinal, posto);
				RelatorioDTO relatorio = new RelatorioDTO();
				relatorio.setRelatorios(new ArrayList<RelatorioDTO>());
				List<RelatorioDTO> relatorios = new ArrayList<RelatorioDTO>();
				posto.setDiarioBombas(diarioBombasAux);
				relatorio.setPosto(posto);
				Float total = 0F;
				for (DiarioBomba d : diarioBombasAux) {
					RelatorioDTO dto = new RelatorioDTO();
					if(d.getValorFinal() != null && d.getValorInicial() != null){
						if(d.getValorFinal() > d.getValorInicial()){
							if(d.getValorFinal() < Constants.LIM_INF_LEIT_BOMBA_COMBUSTIVEL && d.getValorInicial() > Constants.LIM_SUP_LEIT_BOMBA_COMBUSTIVEL){
								Float la =  ((Constants.LIMITE_LEIT_BOMBA_COMBUSTIVEL - d.getValorInicial()) + d.getValorFinal());
								d.setLitrosAbastecidos(la);
								total+=la;
							}
						} else {
							Float la = d.getValorFinal() - d.getValorInicial();
							d.setLitrosAbastecidos(la);
							total+=la;
						}
					}
					dto.setDiarioBomba(d);
					dto.setPosto(posto);
					dto.setDtInicial(this.dtInicial);
					dto.setDtFinal(this.dtFinal);
					relatorios.add(dto);
				}
				relatorio.getRelatorios().addAll(relatorios);
				relatorio.setConsumoPosto(total);
				this.entities.add(relatorio);
			}
		}
		return SUCCESS;
	}

	public String consultarMultasVeiculo() {
		if (!DateUtil.compareDate(this.dtInicial, this.dtFinal)) {
			JSFUtil.getInstance().addErrorMessage("msg.error.datas.inconsistentes");
			return FAIL;
		}
		if (this.searchFlag.equals("SEARCH_VEICULO")) {
			consultarMultasPorVeiculo();
		} else if (this.searchFlag.equals("SEARCH_MOTORISTA")) {
			consultarMultasPorMotorista();
		} else if (this.searchFlag.equals("SEARCH_UG")) {
			consultarMultasPorOrgao();
		}
		return SUCCESS;
	}

	public String consultarMultasPorOrgao() {
		this.nomeRelatorio = this.relMultasVeiculoByUG;
		this.entities = new ArrayList<RelatorioDTO>();
		this.result = new ArrayList<RelatorioDTO>();
		List<Multa> multas =  multaService.findByUG(this.orgao, this.dtInicial, this.dtFinal);
		if(multas != null && multas.size() > 0){
			Map<UG, List<Multa>> map = new HashMap<UG, List<Multa>>();
			for (Multa m : multas) {
				UG ug = null;  
				if(m.getVeiculo().getUa() != null){
					ug = m.getVeiculo().getUa().getUg();
				}
				if(map.containsKey(ug) == false){
					List<Multa> list = new ArrayList<Multa>();
					list.add(m);
					map.put(ug, list);
				} else {
					map.get(ug).add(m);
				}
			}
			for (UG ug : map.keySet()) {
				RelatorioDTO dto = new RelatorioDTO();
				dto.setOrgao(ug);
				dto.setDtInicial(this.dtInicial);
				dto.setDtFinal(this.dtFinal);
				dto.setRelatorios(new ArrayList<RelatorioDTO>());
				List<Multa> result = map.get(ug);
				for(Multa multa: result){
					RelatorioDTO rel = new RelatorioDTO();
					rel.setMulta(multa);
					rel.setOrgao(ug);
					rel.setDtInicial(this.dtInicial);
					rel.setDtFinal(this.dtFinal);
					dto.getRelatorios().add(rel);
				}
				this.entities.add(dto);
				this.result.addAll(dto.getRelatorios());
			}
		}
		return SUCCESS;
	}


	public String consultarMultasPorVeiculo() {
		this.nomeRelatorio = this.relMultasVeiculos;
		List<Multa> multas = null;
		this.result = new ArrayList<RelatorioDTO>();
		this.entities = new ArrayList<RelatorioDTO>();
		if(this.veiculo != null){
			multas = multaService.findByVeiculo(this.veiculo, null, this.dtInicial, this.dtFinal);
		} else {
			multas = multaService.findByUG(this.orgao, this.dtInicial, this.dtFinal);
		}
		Map<Veiculo, List<Multa>> map = new HashMap<Veiculo, List<Multa>>();
		for (Multa m : multas) {
			Veiculo v = m.getVeiculo();
			if(map.containsKey(v) == false){
				List<Multa> list = new ArrayList<Multa>();
				list.add(m);
				map.put(v, list);
			} else {
				map.get(v).add(m);
			}
		}

		for (Veiculo v : map.keySet()) {
			RelatorioDTO dto = new RelatorioDTO();
			dto.setVeiculo(v);
			dto.setDtInicial(this.dtInicial);
			dto.setDtFinal(this.dtFinal);
			dto.setRelatorios(new ArrayList<RelatorioDTO>());
			List<Multa> result = map.get(v);
			for(Multa multa: result){
				RelatorioDTO rel = new RelatorioDTO();
				rel.setMulta(multa);
				rel.setVeiculo(v);
				rel.setPeriodo(this.dtInicial);
				rel.setDtInicial(this.dtInicial);
				rel.setDtFinal(this.dtFinal);
				dto.getRelatorios().add(rel);
			}
			this.result.addAll(dto.getRelatorios());
			this.entities.add(dto);
		}
		return SUCCESS;
	}

	public String consultarMultasPorMotorista() {
		this.nomeRelatorio = this.relMultasVeiculoByMotorista;
		List<Multa> multas = null;
		this.result = new ArrayList<RelatorioDTO>();
		this.entities = new ArrayList<RelatorioDTO>();
		if(this.motorista != null){
			multas = multaService.findByMotorista(this.motorista, null, this.dtInicial, this.dtFinal);
		} else {
			multas = multaService.findByUG(this.orgao, this.dtInicial, this.dtFinal);
		}
		Map<Motorista, List<Multa>> map = new HashMap<Motorista, List<Multa>>();
		for (Multa m : multas) {
			Motorista motorista = m.getMotorista();
			if(motorista != null){
				if(map.containsKey(motorista) == false){
					List<Multa> list = new ArrayList<Multa>();
					list.add(m);
					map.put(motorista, list);
				} else {
					map.get(motorista).add(m);
				}
			}
		}

		for (Motorista m : map.keySet()) {
			RelatorioDTO dto = new RelatorioDTO();
			dto.setMotorista(m);
			dto.setRelatorios(new ArrayList<RelatorioDTO>());
			List<Multa> result = map.get(m);
			for(Multa multa: result){
				RelatorioDTO rel = new RelatorioDTO();
				rel.setMulta(multa);
				rel.setPeriodo(this.dtInicial);
				rel.setMotorista(multa.getMotorista());
				dto.getRelatorios().add(rel);
			}
			this.result.addAll(dto.getRelatorios());
			this.entities.add(dto);
		}
		return SUCCESS;
	}

	public String consultarConsumoVeiculo() {
		if (!DateUtil.compareDate(this.dtInicial, this.dtFinal)) {
			JSFUtil.getInstance().addErrorMessage("msg.error.datas.inconsistentes");
			return FAIL;
		}
		List<AtendimentoAbastecimento> atendimentos = null;
		if(this.orgao != null){
			atendimentos = atendimentoService.findByPeriodo(this.orgao.getId().toString(), null, this.dtInicial, this.dtFinal);
		} else {
			atendimentos = atendimentoService.findByPeriodo(null, null, this.dtInicial, this.dtFinal);
		}
		this.entities = new ArrayList<RelatorioDTO>();
		List<Integer> ids = new ArrayList<Integer>();
		Map<Veiculo, List<AtendimentoAbastecimento> > map = new HashMap<Veiculo, List<AtendimentoAbastecimento> >();
		for (AtendimentoAbastecimento a : atendimentos) {
			Veiculo v = a.getAbastecimento().getVeiculo();
			if(map.containsKey(v) == false){
				List<AtendimentoAbastecimento> list = new ArrayList<AtendimentoAbastecimento>();
				list.add(a);
				map.put(v, list);
				ids.add(v.getId());
			} else {
				map.get(v).add(a);
			}
		}
		Map<Veiculo, Cota> mapCota = cotaService.retrieveMapVeiculoCota(ids);
		for (Veiculo v : map.keySet()) {
			RelatorioDTO dto = new RelatorioDTO();
			dto.setVeiculo(v);
			dto.setRelatorios(new ArrayList<RelatorioDTO>());
			Float total = 0F;
			List<AtendimentoAbastecimento> list = map.get(v);
			for (AtendimentoAbastecimento a : list) {
				RelatorioDTO rel = new RelatorioDTO();
				total += a.getQuantidadeAbastecida() != null ? a.getQuantidadeAbastecida().floatValue() : 0;
				rel.setVeiculo(v);
				rel.setAtendimento(a);
				rel.setAbastecimento(a.getAbastecimento());
				rel.setMotorista(a.getAbastecimento().getMotorista());
				rel.setConsumo(a.getQuantidadeAbastecida() != null ? a.getQuantidadeAbastecida().floatValue() :  0F);
				rel.setCota(mapCota.get(v).getCota().floatValue());
				rel.setSaldoCota((float)mapCota.get(v).getCota().floatValue() - total);
				rel.setConsumoTotal(total);
				rel.setSaldoFinal((float)mapCota.get(v).getCota().floatValue() - total);
				rel.setKmAtual(a.getQuilometragem() != null ? a.getQuilometragem().intValue() : 0);
				dto.getRelatorios().add(rel);
			}
			dto.setConsumo(total);
			dto.setCota((float)mapCota.get(v).getCota().floatValue());

			this.entities.add(dto);
			this.result.addAll(dto.getRelatorios());
		}
		return SUCCESS;
	}

	/**
	 * 
	 * pesquisa está sendo feita p/ todos os veículos
	 */
	@Transactional
	public String consultarConsolidadoMensal() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.MONTH, this.mes - 1);
		calendar.set(Calendar.DAY_OF_MONTH, 1);

		this.dtInicial = DateUtil.getDateStartDay(calendar.getTime());
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DATE));
		this.dtFinal = DateUtil.getDateEndDay(calendar.getTime());

		if (!DateUtil.compareDate(this.dtInicial, this.dtFinal)) {
			JSFUtil.getInstance().addErrorMessage("msg.error.datas.inconsistentes");
			return FAIL;
		}
		List<AtendimentoAbastecimento> atendimentos = new ArrayList<AtendimentoAbastecimento>();
		if(this.orgao != null){
			atendimentos = atendimentoService.findByPeriodo(this.orgao.getId().toString(), null, this.dtInicial, this.dtFinal);
		} else {
			atendimentos = atendimentoService.findByPeriodo(null, null, this.dtInicial, this.dtFinal);
		}

		this.entities = new ArrayList<RelatorioDTO>();
		List<Integer> ids = new ArrayList<Integer>();
		Map<Veiculo, List<AtendimentoAbastecimento> > map = new HashMap<Veiculo, List<AtendimentoAbastecimento> >();
		for (AtendimentoAbastecimento a : atendimentos) {
			Veiculo v = a.getAbastecimento().getVeiculo();
			if(map.containsKey(v) == false){
				List<AtendimentoAbastecimento> list = new ArrayList<AtendimentoAbastecimento>();
				list.add(a);
				map.put(v, list);
				ids.add(v.getId());
			} else {
				map.get(v).add(a);
			}
		}
		Map<Veiculo, Cota> mapCota = cotaService.retrieveMapVeiculoCota(ids);
		for (Veiculo v : map.keySet()) {
			RelatorioDTO dto = new RelatorioDTO();
			dto.setVeiculo(v);
			dto.setOrgao(v.getUa().getUg());
			dto.setRelatorios(new ArrayList<RelatorioDTO>());
			Float total = 0F;
			Float kmInicial = 0F;
			Float kmFinal = 0F;
			List<AtendimentoAbastecimento> list = map.get(v);
			for (int j = 0; j < list.size(); j++) {
				AtendimentoAbastecimento a = list.get(j);
				total += a.getQuantidadeAbastecida() != null ? a.getQuantidadeAbastecida().floatValue() : 0F;
				if(j == 0){
					Float aux = (a.getQuilometragem() == null)?0f:a.getQuilometragem();
					kmInicial = aux;
				}
				if(j == (list.size() - 1)){
					kmFinal = (float)a.getQuilometragem();
				}
				dto.setCombustivel(a.getAbastecimento().getCombustivel().getDescricao());
			}
			dto.setConsumo(total);
			dto.setKmRodados(kmFinal - kmInicial);
			dto.setKmPorLitro((kmFinal - kmInicial)/total);
			dto.setCota(mapCota.get(v).getCota().floatValue());
			dto.setSaldoCota(mapCota.get(v).getCota().floatValue() - total);
			dto.setNumeroAbastecimentos(list.size());
			this.entities.add(dto);
		}
		return SUCCESS;
	}

	public String consultarPontuacaoMotorista() {
		List<Motorista> motoristas = null;
		this.entities = new ArrayList<RelatorioDTO>();
		if(this.orgao != null){
			this.orgaos = new ArrayList<UG>();
			motoristas = motoristaService.findByUG(orgao.getId());
			for (Motorista motorista : motoristas) {
				RelatorioDTO dto = new RelatorioDTO();
				dto.setOrgao(this.orgao);
				motorista.setStatus(motorista.getPontosCnh() == 20 ? "Atingiu 20pt" : motorista.getPontosCnh() > 20 ? "Suspensa" : "OK");
				dto.setMotorista(motorista);
				this.entities.add(dto);
			}
			this.orgao.setMotoristas(motoristas);
			this.orgaos.add(this.orgao);
		}else{
			orgaos = motoristaService.findMotoristaOrgao();
			if(orgaos.size() > 0){

				for (UG ug : this.orgaos) {
					motoristas = motoristaService.findByOrgao(ug);
					for (Motorista motorista : motoristas) {
						RelatorioDTO dto = new RelatorioDTO();
						dto.setOrgao(ug);
						motorista.setStatus(motorista.getPontosCnh() == 20 ? "Atingiu 20pt" : motorista.getPontosCnh() > 20 ? "Suspensa" : "OK");
						dto.setMotorista(motorista);
						dto.setStatus(motorista.getPontosCnh() == 20 ? "Atingiu 20pt" : motorista.getPontosCnh() > 20 ? "Suspensa" : "OK");
						ug.setMotoristas(motoristas);
						this.entities.add(dto);
					}
				}
			} else {
				this.entities = new ArrayList<RelatorioDTO>();
			}
		}
		return SUCCESS;
	}


	public String consultarHistoricoVeiculoManutencao() {
		if (!DateUtil.compareDate(this.dtInicial, this.dtFinal)) {
			JSFUtil.getInstance().addErrorMessage("msg.error.datas.inconsistentes");
			return FAIL;
		}
		List<RequisicaoManutencao> result = null;
		this.entities = new ArrayList<RelatorioDTO>();
		this.result = new ArrayList<RelatorioDTO>();
		if(this.orgao == null && this.veiculo == null){
			result = manutencaoService.findByPeriodo(null, null, this.dtInicial, this.dtFinal);
		}
		if(this.orgao != null && this.veiculo == null){
			this.searchId = 2;
			result = manutencaoService.findByPeriodo(this.orgao.getId(), null, DateUtil.getDateStartDay(this.dtInicial), DateUtil.getDateEndDay(this.dtFinal));
		} else if(this.orgao != null && this.veiculo != null){
			this.searchId = 1;
			result = manutencaoService.findByPeriodo(this.orgao.getId(), this.veiculo.getId(), this.dtInicial, this.dtFinal);
		}

		Map<Veiculo, List<RequisicaoManutencao>> map = new HashMap<Veiculo, List<RequisicaoManutencao>>();
		for (RequisicaoManutencao req : result) {
			if(map.containsKey(req.getVeiculo()) == false){
				List<RequisicaoManutencao> list = new ArrayList<RequisicaoManutencao>();
				list.add(req);
				map.put(req.getVeiculo(), list);
			} else {
				map.get(req.getVeiculo()).add(req);
			}
		}
		for (Veiculo v : map.keySet()) {
			List<RequisicaoManutencao> list = map.get(v);
			RelatorioDTO relatorio = new RelatorioDTO();
			relatorio.setRelatorios(new ArrayList<RelatorioDTO>());
			relatorio.setVeiculo(v);
			relatorio.setOrgao(this.orgao);
			for (RequisicaoManutencao r : list) {
				RelatorioDTO dto = new RelatorioDTO();
				dto.setManutencao(r);
				dto.setOrgao(r.getVeiculo().getUa() != null ? r.getVeiculo().getUa().getUg() : null);
				dto.setVeiculo(v);
				if(r.getDataRetorno() != null && r.getDataSaida() != null){
					dto.setDuracaoManutencao(DateUtil.tempoEntreDatas(r.getDataRetorno(), r.getDataSaida()));
				}
				relatorio.getRelatorios().add(dto);
			}
			this.entities.add(relatorio);
			this.result.addAll(relatorio.getRelatorios());
		}
		return SUCCESS;
	}

	public String consultarVeiculosSemRetorno() {
		this.entities = new ArrayList<RelatorioDTO>();
		this.result = new ArrayList<RelatorioDTO>();
		if (!DateUtil.compareDate(this.dtInicial, this.dtFinal)) {
			JSFUtil.getInstance().addErrorMessage("msg.error.datas.inconsistentes");
			return FAIL;
		}
		List<RequisicaoManutencao> result = null;
		if(this.orgao != null){
			result = manutencaoService.findManutencaoSemRetorno(this.orgao, this.dtInicial, this.dtFinal);
		} else {
			result = manutencaoService.findManutencaoSemRetorno(this.orgao, DateUtil.getDateStartDay(this.dtInicial), 
					DateUtil.getDateEndDay(this.dtFinal));
		}
		Map<UG, List<RequisicaoManutencao>> map = new HashMap<UG, List<RequisicaoManutencao>>();
		for (RequisicaoManutencao req : result) {
			UG ug  = req.getVeiculo().getUa().getUg();
			if(!map.containsKey(ug)){
				List<RequisicaoManutencao> list = new ArrayList<RequisicaoManutencao>();
				list.add(req);
				map.put(ug, list);
			} else {
				map.get(ug).add(req);
			}
		}
		for (UG ug : map.keySet()) {
			List<RequisicaoManutencao> list = map.get(ug);
			RelatorioDTO relatorio = new RelatorioDTO();
			relatorio.setRelatorios(new ArrayList<RelatorioDTO>());
			relatorio.setOrgao(ug);
			relatorio.setDtInicial(this.dtInicial);
			relatorio.setDtFinal(this.dtFinal);
			Date dtAtual = new Date();
			for (RequisicaoManutencao m : list) {
				RelatorioDTO dto = new RelatorioDTO();
				dto.setDtInicial(this.dtInicial);
				dto.setDtFinal(this.dtFinal);
				dto.setManutencao(m);
				dto.setOrgao(ug);
				dto.setAtraso(DateUtil.tempoEntreDatas(dtAtual, m.getDataFim()));
				relatorio.getRelatorios().add(dto);
			}
			this.entities.add(relatorio);
			this.result.addAll(relatorio.getRelatorios());
		}
		return SUCCESS;
	}

	public String consultarVeiculosEmManutencao() {
		this.entities = new ArrayList<RelatorioDTO>();
		this.result = new ArrayList<RelatorioDTO>();
		if (!DateUtil.compareDate(this.dtInicial, this.dtFinal)) {
			JSFUtil.getInstance().addErrorMessage("msg.error.datas.inconsistentes");
			return FAIL;
		}
		List<RequisicaoManutencao> result = null;
		if(this.orgao != null){
			result = manutencaoService.findByUgStatus(this.orgao, this.dtInicial, this.dtFinal);
		} else {
			result = manutencaoService.findByUgStatus(this.orgao, DateUtil.getDateStartDay(this.dtInicial), DateUtil.getDateEndDay(this.dtFinal));
		}

		Map<UG, List<RequisicaoManutencao>> map = new HashMap<UG, List<RequisicaoManutencao>>();
		for (RequisicaoManutencao req : result) {
			UG ug  = req.getVeiculo().getUa().getUg();
			if(!map.containsKey(ug)){
				List<RequisicaoManutencao> list = new ArrayList<RequisicaoManutencao>();
				list.add(req);
				map.put(ug, list);
			} else {
				map.get(ug).add(req);
			}
		}

		for (UG ug : map.keySet()) {
			List<RequisicaoManutencao> list = map.get(ug);
			RelatorioDTO relatorio = new RelatorioDTO();
			relatorio.setRelatorios(new ArrayList<RelatorioDTO>());
			relatorio.setOrgao(ug);
			relatorio.setDtInicial(this.dtInicial);
			relatorio.setDtFinal(this.dtFinal);
			relatorio.setNumeroManutencoes(list.size());
			for (RequisicaoManutencao m : list) {
				RelatorioDTO dto = new RelatorioDTO();
				dto.setDtInicial(this.dtInicial);
				dto.setDtFinal(this.dtFinal);
				dto.setManutencao(m);
				for (ItemRequisicao item : m.getItensRequisicao()) {
					RelatorioDTO toReport = new RelatorioDTO();
					toReport.setDtInicial(this.dtInicial);
					toReport.setDtFinal(this.dtFinal);
					toReport.setOrgao(ug);
					toReport.setVeiculo(m.getVeiculo());
					toReport.setManutencao(m);
					toReport.setItemManutencao(item);
					toReport.setNumeroManutencoes(list.size());
					this.result.add(toReport);
				}
				relatorio.getRelatorios().add(dto);
			}
			this.entities.add(relatorio);
		}
		return SUCCESS;
	}

	public String consultarHistoricoTrocaPneus() {
		if (!DateUtil.compareDate(this.dtInicial, this.dtFinal)) {
			JSFUtil.getInstance().addErrorMessage("msg.error.datas.inconsistentes");
			return FAIL;
		}
		this.entities = new ArrayList<RelatorioDTO>();
		this.result = new ArrayList<RelatorioDTO>();
		List<ItemRequisicao> itens;

		if(this.veiculo == null){
			List<Integer> ids = new ArrayList<Integer>();
			if(this.veiculos.size() > 0){
				for (Veiculo v : this.veiculos) {
					ids.add(v.getId());
				}
			}
			Integer tipoServicoId = 40;
			itens = itemManutencaoService.findByVeiculosTipoServico(ids, tipoServicoId);
		} else {
			Integer tipoServicoId = 40;
			List<Integer> ids = new ArrayList<Integer>();
			ids.add(this.veiculo.getId());
			itens = itemManutencaoService.findByVeiculosTipoServico(ids, tipoServicoId);
		}
		Map<Veiculo, List<ItemRequisicao>> map = new HashMap<Veiculo, List<ItemRequisicao>>();
		for (ItemRequisicao item : itens) {
			if(map.containsKey(item.getRequisicaoManutencao().getVeiculo()) == false){
				List<ItemRequisicao> list = new ArrayList<ItemRequisicao>();
				list.add(item);
				map.put(item.getRequisicaoManutencao().getVeiculo(), list);
			} else {
				map.get(item.getRequisicaoManutencao().getVeiculo()).add(item);
			}
		}
		for (Veiculo v : map.keySet()) {
			List<ItemRequisicao> list = map.get(v);
			RelatorioDTO relatorio = new RelatorioDTO();
			relatorio.setRelatorios(new ArrayList<RelatorioDTO>());
			relatorio.setVeiculo(v);
			relatorio.setOrgao(this.orgao);
			relatorio.setDtInicial(this.dtInicial);
			relatorio.setDtFinal(this.dtFinal);
			Integer total = 0;
			for (ItemRequisicao i : list) {
				RelatorioDTO dto = new RelatorioDTO();
				dto.setVeiculo(v);
				dto.setDtInicial(this.dtInicial);
				dto.setDtFinal(this.dtFinal);
				dto.setManutencao(i.getRequisicaoManutencao());
				dto.setItemManutencao(i);
				dto.setQuantTrocaPneus(i.getQuantidade());
				total += i.getQuantidade();
				relatorio.getRelatorios().add(dto);
			}
			relatorio.setQuantTrocaPneus(total);
			this.entities.add(relatorio);
			this.result.addAll(relatorio.getRelatorios());
		}
		return SUCCESS;
	}

	public String consultarInformacoesVeiculo(){
		this.entities = new ArrayList<RelatorioDTO>();
		this.result = new ArrayList<RelatorioDTO>();
		List<Veiculo> veiculos = null;
		if(this.orgao == null){
			veiculos = veiculoService.retrieveAll();
		} else {
			veiculos = veiculoService.findByUG(this.orgao);
		}
		Map<UG, List<Veiculo>> map = new HashMap<UG, List<Veiculo>>();
		for (Veiculo veiculo : veiculos) {
			if(map.containsKey(veiculo.getUa().getUg())){
				map.get(veiculo.getUa().getUg()).add(veiculo);
			} else {
				List<Veiculo> vlist = new ArrayList<Veiculo>();
				vlist.add(veiculo);
				map.put(veiculo.getUa().getUg(), vlist);
			}
		}
		for (UG ug : map.keySet()) {
			RelatorioDTO relatorio = new RelatorioDTO();
			relatorio.setOrgao(ug);
			relatorio.setRelatorios(new ArrayList<RelatorioDTO>());
			List<Veiculo> vlist = map.get(ug);
			for (Veiculo veiculo : vlist) {
				RelatorioDTO dto = new RelatorioDTO();
				dto.setVeiculo(veiculo);
				dto.setOrgao(ug);
				relatorio.getRelatorios().add(dto);
			}
			this.entities.add(relatorio);
			this.result.addAll(relatorio.getRelatorios());
		}
		return SUCCESS;
	}

	/**
	 * 
	 * gera consulta dos kilomentros rodados dos veículos do orgãos
	 * @return
	 */

	public String consultarKilometrosRodados(){

		this.entities = new ArrayList<RelatorioDTO>();
		this.result = new ArrayList<RelatorioDTO>();
		Map<Veiculo, Float> mapKilometragem = null;
		if(this.orgao != null){
			RelatorioDTO relatorio = new RelatorioDTO();
			relatorio.setRelatorios(new ArrayList<RelatorioDTO>());
			relatorio.setOrgao(this.orgao);
			mapKilometragem = solicitacaoService.mapKilometragem(this.orgao, dtInicial, dtFinal);
			if(mapKilometragem.size() > 0){
				for (Veiculo v  : mapKilometragem.keySet()) {
					RelatorioDTO dto = new RelatorioDTO();
					dto.setVeiculo(v);
					dto.setOrgao(orgao);
					dto.setKmRodados(mapKilometragem.get(v));
					this.result.add(dto);
					relatorio.getRelatorios().add(dto);
				}
				this.entities.add(relatorio);
			}
		} else {
			this.orgaos = ugService.retrieveAll();
			if(this.orgaos.size() > 0){
				for (UG ug : this.orgaos) {
					RelatorioDTO relatorio = new RelatorioDTO();
					relatorio.setRelatorios(new ArrayList<RelatorioDTO>());
					relatorio.setOrgao(ug);
					mapKilometragem = solicitacaoService.mapKilometragem(ug, dtInicial, dtFinal);
					if(mapKilometragem.size() > 0){
						for (Veiculo v  : mapKilometragem.keySet()) {
							RelatorioDTO dto = new RelatorioDTO();
							dto.setVeiculo(v);
							dto.setOrgao(ug);
							dto.setKmRodados(mapKilometragem.get(v));
							this.result.add(dto);
							relatorio.getRelatorios().add(dto);
						}
						this.entities.add(relatorio);
					}
				}
			}
		}
		return SUCCESS;
	}

	private Map<String, Object> montarParametrosRelat() {
		Map<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IMAGEM_URI", RelatorioUtil.getInstance().retornarImagensDir());
		return parametros;
	}

	public void gerarRelatorio() {

		Map<String, Object> parametros = null;
		parametros = montarParametrosRelat();

		try {

			if (this.nomeRelatorio.equals(this.relInformacoesVeiculo)) {

				gerarRelatorioCollection(parametros, this.result, this.nomeRelatorio);

			} else if (this.nomeRelatorio.equals(this.relHistoricoVeiculoManutencao)) {

				gerarRelatorioCollection(parametros, this.result, this.nomeRelatorio);

			} else if (this.nomeRelatorio.equals(this.relMotoristaPontuacao)) {

				gerarRelatorioCollection(parametros, this.entities, this.nomeRelatorio);

			} else if (this.nomeRelatorio.equals(this.relDiarioBombas)) {

				List<RelatorioDTO> list = new ArrayList<RelatorioDTO>();

				for (RelatorioDTO r : this.entities) {

					list.addAll(r.getRelatorios());
				}

				gerarRelatorioCollection(parametros, list, this.nomeRelatorio);

			} else if (this.nomeRelatorio.equals(this.relVeiculoMulta)) {

				gerarRelatorioCollection(parametros, this.entities, this.nomeRelatorio);

			} else if (this.nomeRelatorio.equals(this.relMultasVeiculoByUG) || 
					this.nomeRelatorio.equals(this.relMultasVeiculos) || 
					this.nomeRelatorio.equals(this.relMultasVeiculoByMotorista)) {

				gerarRelatorioCollection(parametros, this.result, this.nomeRelatorio);

			} else if (this.nomeRelatorio.equals(this.relConferenciaAbastecimento)) {

				gerarRelatorioCollection(parametros, result, this.nomeRelatorio);

			} else if (this.nomeRelatorio.equals(this.relConsolidadoMensal)) {

				gerarRelatorioCollection(parametros, this.entities, this.nomeRelatorio);

			}  else if (this.nomeRelatorio.equals(this.relVeiculosEmManutencao)) {

				gerarRelatorioCollection(parametros, this.result, this.nomeRelatorio);

			}  else if (this.nomeRelatorio.equals(this.relHistoricoVeiculoManutencao)) {

				gerarRelatorioCollection(parametros, this.entities, this.nomeRelatorio);

			}  else if (this.nomeRelatorio.equals(this.relHistoricoTrocaPneus)) {

				gerarRelatorioCollection(parametros, this.result, this.nomeRelatorio);

			}  else if (this.nomeRelatorio.equals(this.relVeiculosSemRetornoManutencao)) {

				gerarRelatorioCollection(parametros, this.result, this.nomeRelatorio);

			} else if (this.nomeRelatorio.equals(this.relSolicitacaoVeiculo)) {

				gerarRelatorioCollection(parametros, this.result, this.nomeRelatorio);

			} else if(this.nomeRelatorio.equals(this.relInformacoesKmsRodadosVeiculo)){

				gerarRelatorioCollection(parametros, this.result, this.nomeRelatorio);
			}

		} catch (IOException e) {

			e.printStackTrace();

			logger.error(e.getMessage(), e);

		} catch (JRException e) {

			e.printStackTrace();

			logger.error(e.getMessage(), e);
		}
	}

	public void gerarRelatorioCollection(Map<String, Object> parametros, Collection<?> colecao, String filePropertie) throws IOException, JRException {

		// Gerando relatorio
		// Montando jasper path
		String jasperPath = RelatorioUtil.getInstance().retornarJasperPath(filePropertie);
		byte[] array = GeradorRelatorio.gerarPdfCollection(parametros, colecao, jasperPath);

		// Resgatando response
		HttpServletResponse res = JSFUtil.getInstance().getResponse(FacesContext.getCurrentInstance());

		// Configurando cabeçalho
		res.setContentType("application/pdf");
		res.setHeader("Pragma", "public");
		res.setHeader("Cache-control", "must-revalidate");
		res.setHeader("Content-Disposition", "attachment;filename=" + nomeRelatorio + ".pdf");

		// Enviando o pdf para o navegador
		ServletOutputStream servletOutputStream = res.getOutputStream();
		servletOutputStream.write(array);
		FacesContext.getCurrentInstance().responseComplete();
	}

	public UG getOrgao() {
		return orgao;
	}

	public void setOrgao(UG orgao) {
		this.orgao = orgao;
	}

	public Motorista getMotorista() {
		return motorista;
	}

	public void setMotorista(Motorista motorista) {
		this.motorista = motorista;
	}

	public Veiculo getVeiculo() {
		return veiculo;
	}

	public void setVeiculo(Veiculo veiculo) {
		this.veiculo = veiculo;
	}

	public Integer getSearchId() {
		return searchId;
	}

	public void setSearchId(Integer searchId) {
		this.searchId = searchId;
	}

	public boolean isMostrarVeiculos() {
		return mostrarVeiculos;
	}

	public void setMostrarVeiculos(boolean mostrarVeiculos) {
		this.mostrarVeiculos = mostrarVeiculos;
	}

	public List<Veiculo> getVeiculos() {
		return veiculos;
	}

	public void setVeiculos(List<Veiculo> veiculos) {
		this.veiculos = veiculos;
	}

	public List<Motorista> getMotoristas() {
		return motoristas;
	}

	public void setMotoristas(List<Motorista> motoristas) {
		this.motoristas = motoristas;
	}

	public boolean isMostrarMotoristas() {
		return mostrarMotoristas;
	}

	public void setMostrarMotoristas(boolean mostrarMotoristas) {
		this.mostrarMotoristas = mostrarMotoristas;
	}

	public void setNomeRelatorio(String nomeRelatorio) {
		this.nomeRelatorio = nomeRelatorio;
	}

	public String getNomeRelatorio() {
		return nomeRelatorio;
	}

	public DiarioBomba getDiarioBomba() {
		return diarioBomba;
	}

	public void setDiarioBomba(DiarioBomba diarioBomba) {
		this.diarioBomba = diarioBomba;
	}

	public Posto getPosto() {
		return posto;
	}

	public void setPosto(Posto posto) {
		this.posto = posto;
	}

	public Date getDtInicial() {
		return dtInicial;
	}

	public void setDtInicial(Date dtInicial) {
		this.dtInicial = dtInicial;
	}

	public Date getDtFinal() {
		return dtFinal;
	}

	public void setDtFinal(Date dtFinal) {
		this.dtFinal = dtFinal;
	}

	public List<Multa> getMultas() {
		return multas;
	}

	public void setMultas(List<Multa> multas) {
		this.multas = multas;
	}

	public Integer getMes() {
		return mes;
	}

	public void setMes(Integer mes) {
		this.mes = mes;
	}

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	public boolean isGenerate() {
		return generate;
	}

	public void setGenerate(boolean generate) {
		this.generate = generate;
	}

	public boolean isAllugs() {
		return allugs;
	}

	public void setAllugs(boolean allugs) {
		this.allugs = allugs;
	}

	public List<Posto> getPostos() {
		return postos;
	}

	public void setPostos(List<Posto> postos) {
		this.postos = postos;
	}

	public void setOrgaos(List<UG> orgaos) {
		this.orgaos = orgaos;
	}

	public List<UG> getOrgaos() {
		return orgaos;
	}

	public List<RelatorioDTO> getListToPDF() {
		return result;
	}

	public void setListToPDF(List<RelatorioDTO> listToPDF) {
		this.result = listToPDF;
	}

	public String getSearchFlag() {
		return searchFlag;
	}

	public void setSearchFlag(String searchFlag) {
		this.searchFlag = searchFlag;
	}

	public List<SelectItem> getMeses() {
		return meses;
	}

	public void setMeses(List<SelectItem> meses) {
		this.meses = meses;
	}
}