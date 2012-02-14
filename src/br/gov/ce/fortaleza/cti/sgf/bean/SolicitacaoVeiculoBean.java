/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.gov.ce.fortaleza.cti.sgf.entity.Motorista;
import br.gov.ce.fortaleza.cti.sgf.entity.RegistroVeiculo;
import br.gov.ce.fortaleza.cti.sgf.entity.SolicitacaoVeiculo;
import br.gov.ce.fortaleza.cti.sgf.entity.UG;
import br.gov.ce.fortaleza.cti.sgf.entity.User;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;
import br.gov.ce.fortaleza.cti.sgf.service.MotoristaService;
import br.gov.ce.fortaleza.cti.sgf.service.RegistroVeiculoService;
import br.gov.ce.fortaleza.cti.sgf.service.SolicitacaoVeiculoService;
import br.gov.ce.fortaleza.cti.sgf.service.UGService;
import br.gov.ce.fortaleza.cti.sgf.service.VeiculoService;
import br.gov.ce.fortaleza.cti.sgf.util.DateUtil;
import br.gov.ce.fortaleza.cti.sgf.util.JSFUtil;
import br.gov.ce.fortaleza.cti.sgf.util.SgfUtil;
import br.gov.ce.fortaleza.cti.sgf.util.StatusRegistroSolicitacaoVeiculo;
import br.gov.ce.fortaleza.cti.sgf.util.StatusSolicitacaoVeiculo;

/**
 * @author Deivid
 * @since 22/12/09
 */
@Component("solicitacaoVeiculoBean")
@Scope("session")
public class SolicitacaoVeiculoBean extends EntityBean<Integer, SolicitacaoVeiculo> {

	private static final Log logger = LogFactory.getLog(SolicitacaoVeiculoBean.class);
	
	@Autowired
	private SolicitacaoVeiculoService service;

	@Autowired
	private VeiculoService veiculoService;

	@Autowired
	private UGService ugService = new UGService();

	@Autowired
	private MotoristaService motoristaService = new MotoristaService();

	@Autowired
	private RegistroVeiculoService registroService = new RegistroVeiculoService();

	private RegistroVeiculo registro;
	private UG orgaoSelecionado = SgfUtil.usuarioLogado().getPessoa().getUa().getUg();
	private List<UG> orgaos;
	private List<Veiculo> veiculos;
	private List<Motorista> motoristas;
	private Veiculo veiculoSelecionado;

	private Boolean mostrarSolicitacoes;
	private Boolean flagNegar;
	private Boolean registrar;
	private Boolean autorizar;
	private Boolean retornoVeiculo;
	private Boolean start = true;
	private StatusSolicitacaoVeiculo statusPesquisa = StatusSolicitacaoVeiculo.SOLICITADO;
	private String imagemURL;
	private String placaVeiculo;
	private Date dataSaida;
	private Date dataRetorno;
	private Date horaSaida;
	private Date horaRetorno;
	private Date horaSaidaReal;
	private Date horaRetornoReal;
	private Boolean autorizado;
	private Boolean externo;
	private Boolean desabilita;
	private User usuario = SgfUtil.usuarioLogado();

	@Override
	protected SolicitacaoVeiculo createNewEntity() {
		SolicitacaoVeiculo solicitacao = new SolicitacaoVeiculo();
		solicitacao.setSolicitante(this.usuario);
		solicitacao.setDestino(null);
		solicitacao.setJustificativa(null);
		this.placaVeiculo = null;
		return solicitacao;
	}

	@Override
	protected Integer retrieveEntityId(SolicitacaoVeiculo entity) {
		return entity.getId();
	}

	@Override
	protected SolicitacaoVeiculoService retrieveEntityService() {
		return this.service;
	}

	public void searchSolicitacaoByUser() {
		this.entities.clear();
		this.entities = service.findByUserAndStatus(this.usuario, statusPesquisa);
		if (!this.entities.isEmpty()) {
			for (SolicitacaoVeiculo s : this.entities) {
				if (s.getStatus().equals(StatusSolicitacaoVeiculo.EXTERNO)) {
					s.setImagemURL("/images/retorno.png");
				} else if (s.getStatus().equals(StatusSolicitacaoVeiculo.FINALIZADO)) {
					s.setImagemURL("/images/ok.jpg");
				} else if (s.getStatus().equals(StatusSolicitacaoVeiculo.SOLICITADO)) {
					s.setImagemURL("/images/tick.png");
				} else if (s.getStatus().equals(StatusSolicitacaoVeiculo.AUTORIZADO)) {
					s.setImagemURL("/images/saida.png");
				}
			}
			this.mostrarSolicitacoes = true;
		} else {
			JSFUtil.getInstance().addErrorMessage("msg.error.veiculo.sol.inexistentes");
			this.mostrarSolicitacoes = false;
		}
	}

	public void searchSolicitacaoByUG() {

		if (SgfUtil.isAdministrador(this.usuario) || SgfUtil.isCoordenador(this.usuario)) {
			this.entities = this.service.executeResultListQuery("findByStatus",	statusPesquisa);
		} else if (SgfUtil.isChefeTransporte(this.usuario)) {
			this.entities = service.findByUGAndStatus(this.usuario.getPessoa().getUa().getUg(), statusPesquisa);
		} else if (SgfUtil.isChefeSetor(this.usuario)) {
			this.entities = service.findByUserAndStatus(this.usuario, statusPesquisa);
		}
		if (!this.entities.isEmpty()) {
			for (SolicitacaoVeiculo s : this.entities) {
				if (s.getStatus().equals(StatusSolicitacaoVeiculo.EXTERNO)) {
					s.setImagemURL("/images/retorno.png");
				} else if (s.getStatus().equals(StatusSolicitacaoVeiculo.FINALIZADO)) {
					s.setImagemURL("/images/ok.jpg");
				} else if (s.getStatus().equals(StatusSolicitacaoVeiculo.SOLICITADO)) {
					s.setImagemURL("/images/tick.png");
				} else if (s.getStatus().equals(StatusSolicitacaoVeiculo.AUTORIZADO)) {
					s.setImagemURL("/images/saida.png");
				}
			}
			this.mostrarSolicitacoes = true;
		} else {
			JSFUtil.getInstance().addErrorMessage("msg.error.veiculo.sol.inexistentes");
			this.mostrarSolicitacoes = false;
		}
	}

	@Override
	public String update() {
		this.entity.setDataHoraSaida(DateUtil.addTime(getDataSaida(), getHoraSaida()));
		this.entity.setDataHoraRetorno(DateUtil.addTime(getDataRetorno(), getHoraRetorno()));
		this.entity.setStatus(StatusSolicitacaoVeiculo.AUTORIZADO);
		service.update(this.entity);
		setCurrentBean(currentBeanName());
		setCurrentState(SEARCH);
		return SUCCESS;
	}

	public String populate() {
		return super.populate();
	}

	public boolean isVeiculoNull() {
		Veiculo v = this.entity.getVeiculo();
		if (v == null) {
			return true;
		} else {
			return false;
		}
	}

	public String veiculosDisponiveis() {

		this.veiculos = new ArrayList<Veiculo>();
		if (SgfUtil.isAdministrador(this.usuario) || SgfUtil.isCoordenador(this.usuario)) {
			if (this.placaVeiculo == null || this.placaVeiculo == "") {
				 this.veiculos = service.findVeiculosDisponiveis(this.entity);
			} else {
				// this.veiculos = service.findVeiculosDisponiveis(this.entity,
				// null, this.placaVeiculo);
			}
		} else if (SgfUtil.isChefeSetor(this.usuario)|| SgfUtil.isChefeTransporte(this.usuario)) {
			if (this.placaVeiculo == null) {
				 this.veiculos = service.findVeiculosDisponiveis(this.entity);
			} else {
				// this.veiculos = service.findVeiculosDisponiveis(this.entity,
				// orgao, this.placaVeiculo);
			}
		}
		//if (DateUtil.compareDate(this.entity.getDataHoraSaida(), this.entity.getDataHoraRetorno())) {
			if (this.veiculos.isEmpty()) {
				JSFUtil.getInstance().addErrorMessage("msg.error.veiculo.indisponiveis");
			}
		//}
		return SUCCESS;
	}

	public String prepareView() {
		setCurrentBean(currentBeanName());
		setCurrentState(VIEW);
		return SUCCESS;
	}

	public String save() {

		try {
			this.entity.setSolicitante(usuario);
			this.entity.setStatus(StatusSolicitacaoVeiculo.SOLICITADO);
			this.entity.setDataHoraSaida(DateUtil.addTime(getDataSaida(),getHoraSaida()));
			this.entity.setDataHoraRetorno(DateUtil.addTime(getDataRetorno(),getHoraRetorno()));
			if (!DateUtil.compareDate(this.entity.getDataHoraSaida(), this.entity.getDataHoraRetorno())) {
				JSFUtil.getInstance().addErrorMessage("msg.error.datas.inconsistentes");
				return FAIL;
			} else if (!DateUtil.compareDate(DateUtil.getDateTime(new Date(),"07:59:59"), this.entity.getDataHoraSaida())) {
				JSFUtil.getInstance().addErrorMessage("msg.error.datas.invalida");
				return FAIL;
			}
			super.save();
			setCurrentBean(currentBeanName());
			return search();
		} catch (Exception e) {
			e.printStackTrace();
			return FAIL;
		}
	}

	@Override
	public String search() {
		statusPesquisa = StatusSolicitacaoVeiculo.SOLICITADO;
		this.orgaoSelecionado = this.usuario.getPessoa().getUa().getUg();
		this.flagNegar = false;
		this.registrar = false;
		this.autorizar = false;
		searchSolicitacaoByUG();
		setCurrentBean(currentBeanName());
		setCurrentState(SEARCH);
		this.start = false;
		return SUCCESS;
	}

	public String autorizar() {
		this.entity.setAutorizador(SgfUtil.usuarioLogado());
		this.entity.setStatus(StatusSolicitacaoVeiculo.AUTORIZADO);
		this.flagNegar = false;
		return super.update();
	}

	public void negar() {
		this.flagNegar = true;
		super.prepareEdit();
	}

	public String confirmarNegar() {
		this.flagNegar = false;
		this.entity.setStatus(StatusSolicitacaoVeiculo.NEGADO);
		return super.update();
	}

	public String registrarSaida() {

		this.registrar = false;
		this.entity.setDtSaida(DateUtil.addTime(new Date(),this.horaSaidaReal));
		this.entity.setStatusAtendimento(StatusRegistroSolicitacaoVeiculo.EM_SERVICO);
		this.entity.setUsuario(SgfUtil.usuarioLogado());
		this.entity.setStatus(StatusSolicitacaoVeiculo.EXTERNO);
		service.update(this.entity);
		return search();
	}

	public String registrarRetorno() {
		this.registrar = false;
		this.entity.setDtRetorno(DateUtil.addTime(new Date(),this.horaRetornoReal));
		this.entity.setStatusAtendimento(StatusRegistroSolicitacaoVeiculo.FINALIZADO);
		this.entity.setStatus(StatusSolicitacaoVeiculo.FINALIZADO);
		service.update(this.entity);
		return search();
	}

	public String prepareUpdate() {
		this.motoristas = motoristaService.findByUG(SgfUtil.usuarioLogado().getPessoa().getUa().getUg().getId());
		setHoraSaida(DateUtil.getDateAsTimeString(this.entity.getDataHoraSaida()));
		setHoraRetorno(DateUtil.getDateAsTimeString(this.entity.getDataHoraRetorno()));
		return super.prepareUpdate();
	}

	public String prepareSave() {
		super.prepareSave();
		this.autorizado = false;
		this.externo = false;
		this.dataSaida = null;
		this.dataRetorno = null;
		this.horaSaida = null;
		this.horaRetorno = null;
		this.horaSaidaReal = null;
		this.horaRetornoReal = null;
		this.entity.setSolicitante(this.usuario);
		this.motoristas = motoristaService.findByUG(SgfUtil.usuarioLogado().getPessoa().getUa().getUg().getId());
		this.veiculos = service.findVeiculosDisponiveis(this.entity);
		return SUCCESS;
	}

	@Override
	public String prepareEdit() {
		this.motoristas = motoristaService.findByUG(SgfUtil.usuarioLogado().getPessoa().getUa().getUg().getId());
		this.desabilita = false;
		if (this.entity.getStatus().equals(StatusSolicitacaoVeiculo.AUTORIZADO)) {
			this.autorizado = true;
			this.externo = false;
			this.desabilita = true;
		}
		if (this.entity.getStatus().equals(StatusSolicitacaoVeiculo.EXTERNO)) {
			this.externo = true;
			this.autorizado = false;
			this.desabilita = true;
		}

		this.dataSaida = this.entity.getDataHoraSaida();
		this.dataRetorno = this.entity.getDataHoraRetorno();
		this.horaSaida = this.entity.getDataHoraSaida();
		this.horaRetorno = this.entity.getDataHoraRetorno();
		this.veiculos = new ArrayList<Veiculo>();

		if (this.entity.getVeiculo() == null) {
			this.veiculos = service.findVeiculosDisponiveis(this.entity);
		} else {
			this.veiculos.add(this.entity.getVeiculo());
		}

		if (this.entity.getStatus() == StatusSolicitacaoVeiculo.SOLICITADO || this.entity.getStatus() == StatusSolicitacaoVeiculo.AUTORIZADO) {
//			if (this.entity.getRegistro() != null) {
//				this.registro = this.entity.getRegistro();
//			} else {
//				this.registro = new RegistroVeiculo();
//			}

			if (this.motoristas == null) {
				this.motoristas = motoristaService.findByUG(SgfUtil.usuarioLogado().getPessoa().getUa().getUg().getId());
			}
		}
		setCurrentBean(currentBeanName());
		setCurrentState(EDIT);
		return SUCCESS;
	}

	public UG getOrgaoSelecionado() {
		return orgaoSelecionado;
	}

	public void setOrgaoSelecionado(UG orgaoSelecionado) {
		this.orgaoSelecionado = orgaoSelecionado;
	}

	public void setOrgaos(List<UG> orgaos) {
		this.orgaos = orgaos;
	}

	public List<UG> getOrgaos() {
		return orgaos;
	}

	public void setVeiculos(List<Veiculo> veiculos) {
		this.veiculos = veiculos;
	}

	public List<Veiculo> getVeiculos() {
		return veiculos;
	}

	public void setVeiculoSelecionado(Veiculo veiculoSelecionado) {
		this.veiculoSelecionado = veiculoSelecionado;
	}

	public Veiculo getVeiculoSelecionado() {
		return veiculoSelecionado;
	}

	public void setMostrarSolicitacoes(Boolean mostrarSolicitacoes) {
		this.mostrarSolicitacoes = mostrarSolicitacoes;
	}

	public Boolean getMostrarSolicitacoes() {
		return mostrarSolicitacoes;
	}

	public void setFlagNegar(Boolean flagNegar) {
		this.flagNegar = flagNegar;
	}

	public Boolean getFlagNegar() {
		return flagNegar;
	}

	public void setRegistro(RegistroVeiculo registro) {
		this.registro = registro;
	}

	public RegistroVeiculo getRegistro() {
		return registro;
	}

	public void setRegistrar(Boolean registrar) {
		this.registrar = registrar;
	}

	public Boolean getRegistrar() {
		return registrar;
	}

	public List<Motorista> getMotoristas() {
		return motoristas;
	}

	public void setMotoristas(List<Motorista> motoristas) {
		this.motoristas = motoristas;
	}

	public String getImagemURL() {
		return imagemURL;
	}

	public void setImagemURL(String imagemURL) {
		this.imagemURL = imagemURL;
	}

	public void setRetornoVeiculo(boolean retornoVeiculo) {
		this.retornoVeiculo = retornoVeiculo;
	}

	public boolean isRetornoVeiculo() {
		return retornoVeiculo;
	}

	public void setAutorizar(Boolean autorizar) {
		this.autorizar = autorizar;
	}

	public Boolean getAutorizar() {
		return autorizar;
	}

	public void setStart(Boolean start) {
		this.start = start;
	}

	public boolean isStart() {
		return start;
	}

	public StatusSolicitacaoVeiculo getStatusPesquisa() {
		return statusPesquisa;
	}

	public void setStatusPesquisa(StatusSolicitacaoVeiculo statusPesquisa) {
		this.statusPesquisa = statusPesquisa;
	}

	public String getPlacaVeiculo() {
		return placaVeiculo;
	}

	public void setPlacaVeiculo(String placaVeiculo) {
		this.placaVeiculo = placaVeiculo;
	}

	public Date getDataSaida() {
		return dataSaida;
	}

	public void setDataSaida(Date dataSaida) {
		this.dataSaida = dataSaida;
	}

	public Date getDataRetorno() {
		return dataRetorno;
	}

	public void setDataRetorno(Date dataRetorno) {
		this.dataRetorno = dataRetorno;
	}

	public Date getHoraSaida() {
		return horaSaida;
	}

	public void setHoraSaida(Date horaSaida) {
		this.horaSaida = horaSaida;
	}

	public Date getHoraRetorno() {
		return horaRetorno;
	}

	public void setHoraRetorno(Date horaRetorno) {
		this.horaRetorno = horaRetorno;
	}

	public User getUsuario() {
		return usuario;
	}

	public void setUsuario(User usuario) {
		this.usuario = usuario;
	}

	public Date getHoraSaidaReal() {
		return horaSaidaReal;
	}

	public void setHoraSaidaReal(Date horaSaidaReal) {
		this.horaSaidaReal = horaSaidaReal;
	}

	public Date getHoraRetornoReal() {
		return horaRetornoReal;
	}

	public void setHoraRetornoReal(Date horaRetornoReal) {
		this.horaRetornoReal = horaRetornoReal;
	}

	public Boolean getAutorizado() {
		return autorizado;
	}

	public void setAutorizado(Boolean autorizado) {
		this.autorizado = autorizado;
	}

	public Boolean getExterno() {
		return externo;
	}

	public void setExterno(Boolean externo) {
		this.externo = externo;
	}

	public Boolean getDesabilita() {
		return desabilita;
	}

	public void setDesabilita(Boolean desabilita) {
		this.desabilita = desabilita;
	}

}
