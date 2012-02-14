/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.gov.ce.fortaleza.cti.sgf.entity.Abastecimento;
import br.gov.ce.fortaleza.cti.sgf.entity.AtendimentoAbastecimento;
import br.gov.ce.fortaleza.cti.sgf.entity.Bomba;
import br.gov.ce.fortaleza.cti.sgf.entity.Cota;
import br.gov.ce.fortaleza.cti.sgf.entity.Motorista;
import br.gov.ce.fortaleza.cti.sgf.entity.Posto;
import br.gov.ce.fortaleza.cti.sgf.entity.TipoCombustivel;
import br.gov.ce.fortaleza.cti.sgf.entity.TipoServico;
import br.gov.ce.fortaleza.cti.sgf.entity.UA;
import br.gov.ce.fortaleza.cti.sgf.entity.UG;
import br.gov.ce.fortaleza.cti.sgf.entity.User;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;
import br.gov.ce.fortaleza.cti.sgf.service.AbastecimentoService;
import br.gov.ce.fortaleza.cti.sgf.service.AtendimentoService;
import br.gov.ce.fortaleza.cti.sgf.service.BombaService;
import br.gov.ce.fortaleza.cti.sgf.service.CotaService;
import br.gov.ce.fortaleza.cti.sgf.service.MotoristaService;
import br.gov.ce.fortaleza.cti.sgf.service.PostoService;
import br.gov.ce.fortaleza.cti.sgf.service.TipoServicoService;
import br.gov.ce.fortaleza.cti.sgf.service.VeiculoService;
import br.gov.ce.fortaleza.cti.sgf.util.DateUtil;
import br.gov.ce.fortaleza.cti.sgf.util.JSFUtil;
import br.gov.ce.fortaleza.cti.sgf.util.SgfUtil;
import br.gov.ce.fortaleza.cti.sgf.util.StatusAbastecimento;
import br.gov.ce.fortaleza.cti.sgf.util.StatusAtendimentoAbastecimento;

/**
 * @author Joel
 * @since 11/12/09
 */
@Scope("session")
@Component("abastecimentoBean")
public class AbastecimentoBean extends EntityBean<Integer, Abastecimento> {

	@Autowired
	private AbastecimentoService service;

	@Autowired
	private AtendimentoService atendimentoService;

	@Autowired
	private VeiculoService veiculoService;

	@Autowired
	private MotoristaService motoristaService;

	@Autowired
	private TipoServicoService tipoServicoService;

	@Autowired
	private PostoService postoService;

	@Autowired
	private BombaService bombaService;

	@Autowired
	private CotaService cotaService;

	private List<Veiculo> veiculos = new ArrayList<Veiculo>();
	private List<TipoServico> tiposServico = new ArrayList<TipoServico>();
	private List<Motorista> motoristas = new ArrayList<Motorista>();
	private List<Posto> postos = new ArrayList<Posto>();
	private List<Bomba> bombas = new ArrayList<Bomba>();
	private List<TipoCombustivel> combustiveis = new ArrayList<TipoCombustivel>();

	private User usuario = SgfUtil.usuarioLogado();
	private Date dtInicial;
	private Date dtFinal;
	private UG orgaoCadSelecionado;
	private UG orgaoSelecionado;
	private UA uaSelecionada;
	private UG ug;
	private Boolean autorizar;
	private Boolean atendimento;
	private Boolean atender;
	private Boolean negar;
	private Boolean start;
	private Boolean mostrarPosto;
	private Long kmAtendimento;
	private Double quantidadeAbastecida;
	private Long ultimaQuilometragem;
	private Bomba bomba;
	private StatusAbastecimento status;
	private String placa;
	private Double saldoAtual;

	@Override
	protected Abastecimento createNewEntity() {

		Abastecimento abastecimento = new Abastecimento();

		abastecimento.setCombustivel(new TipoCombustivel());

		abastecimento.setPosto(new Posto());

		setDtInicial(DateUtil.getDateTime(new Date(), "00:00:00"));

		setDtFinal(DateUtil.getDateTime(new Date(), "23:59:59"));

		this.mostrarPosto = false;

		this.bomba = new Bomba();

		return abastecimento;
	}

	@Override
	protected Integer retrieveEntityId(Abastecimento entity) {

		return entity.getId();
	}

	@Override
	protected AbastecimentoService retrieveEntityService() {

		return this.service;
	}

	@PostConstruct
	public void init() {

		this.start = false;

		this.orgaoSelecionado = usuario.getPessoa().getUa().getUg();

		this.dtInicial = DateUtil.getDateTime(new Date(), "00:00:00");

		this.dtFinal = DateUtil.getDateTime(new Date(), "23:59:59");

		this.status = StatusAbastecimento.AUTORIZADO;

		search();

		this.start = true;
	}

	@Override
	public String prepareSave() {

		this.mostrarPosto = false;

		refreshLists();

		return super.prepareSave();
	}

	@Override
	public String prepareUpdate() {

		if (this.entity.getCombustivel() != null) {

			postoPorCombustivel();
		} else {

			this.mostrarPosto = false;
		}

		refreshLists();

		return super.prepareUpdate();
	}

	public String pesquisarPeriodoOrgao() {

		this.entities = new ArrayList<Abastecimento>();

		this.dtInicial = DateUtil.getDateTime(this.dtInicial, "00:00:00");

		this.dtFinal = DateUtil.getDateTime(this.dtFinal, "23:59:59");

		if (DateUtil.compareDate(this.dtInicial, this.dtFinal)) {

			if (SgfUtil.isOperador(usuario)) {

				if (this.placa != null && this.placa != "") {

					this.entities = service.pesquisarPlaca(this.dtInicial,
							this.dtFinal, this.orgaoSelecionado, this.placa,
							this.status);

				} else {

					this.entities = service.findByPeriodoAndPosto(usuario
							.getPosto().getCodPosto(), this.dtInicial,
							this.dtFinal, status);
				}

			} else if (this.orgaoSelecionado != null
					&& this.orgaoSelecionado.getId() != null) {

				if (this.placa != null && this.placa != "") {

					this.entities = service.pesquisarPlaca(this.dtInicial,
							this.dtFinal, this.orgaoSelecionado, this.placa,
							this.status);

				} else {

					this.entities = service.pesquisarPeriodo(this.dtInicial,
							this.dtFinal, this.orgaoSelecionado, this.status);
				}
			} else {

				this.entities = service.pesquisarTodos(this.dtInicial,
						this.dtFinal, this.status);
			}

			return SUCCESS;

		} else {

			JSFUtil.getInstance().addErrorMessage(
					"msg.error.datas.inconsistentes");

			return FAIL;
		}
	}

	public void loadVeiculos() {

		this.veiculos = new ArrayList<Veiculo>();

		if (this.orgaoSelecionado != null) {

			this.veiculos = veiculoService.findByUG(this.orgaoSelecionado);
		}
		
		loadMotoristas();

	}

	public void loadMotoristas() {

		this.motoristas = new ArrayList<Motorista>();

		if (this.orgaoSelecionado != null) {

			this.motoristas = motoristaService.findByUG(this.orgaoSelecionado
					.getId());
		}

	}

	private void refreshLists() {

		this.veiculos.clear();

		this.motoristas.clear();

		this.tiposServico.clear();

		if (entity != null) {

			if (entity.getQuilometragem() != null) {

				this.ultimaQuilometragem = entity.getQuilometragem();
			}
		}

		this.veiculos.addAll(veiculoService.findByUG(this.orgaoSelecionado));

		this.motoristas.addAll(motoristaService
				.findByUG(this.orgaoSelecionado.getId()));

		this.tiposServico.add(tipoServicoService.retrieve(1));

		if (this.entity.getPosto() != null) {

			setBombas(this.entity.getPosto().getListaBomba());
		}
	}

	private boolean validaQuilometragem() {

		if (this.atendimento) {

			Abastecimento last = service.executeSingleResultQuery("findLast",
					entity.getVeiculo().getId());

			if (last != null) {

				this.ultimaQuilometragem = last.getQuilometragem();

				if (last.getQuilometragem() != null) {

					if (last.getQuilometragem() > entity.getQuilometragem()) {

						JSFUtil.getInstance().addErrorMessage(
								"msg.error.quilometragem.inconsistente");

						return false;
					}
				}
			}
		}
		
		if (validaCota()) {
			return true;
		} else {
			JSFUtil.getInstance().addErrorMessage(
								"msg.error.cota.utrapassada");
			return false;
		}
	}
	/**
	 * Valida a cota disponível para o veículo durante o atendimento
	 * do abastecimento
	 * 
	 * @return Boolean
	 */
	private Boolean validaCota() {
		return this.quantidadeAbastecida <= this.entity.getVeiculo().getCota().getCotaDisponivel() ? true : false;
	}

	private boolean isValid() {

		// Double total =
		// this.entity.getVeiculo().getCota().getCotaDisponivel();

		Cota cota = this.entity.getVeiculo().getCota();

		if (cota != null) {

			if (cota.getCotaDisponivel() > 0) {

				return true;

			} else {

				JSFUtil.getInstance().addErrorMessage(
						"msg.error.cota.utrapassada");
			}
		} else {

			JSFUtil.getInstance()
					.addErrorMessage("msg.error.cota.indisponivel");

			return false;
		}

		return false;
	}

	public String postoPorCombustivel() {

		this.postos = new ArrayList<Posto>();

		this.postos = postoService.findByCombustivel(this.entity
				.getCombustivel());

		this.mostrarPosto = true;

		return SUCCESS;
	}

	public String populate() {

		this.placa = null;
		return SUCCESS;
	}

	@Override
	public String search() {

		Set<Abastecimento> filtro = new HashSet<Abastecimento>(0);

		this.entities = new ArrayList<Abastecimento>();

		this.tiposServico.add(tipoServicoService.retrieve(1));

		UA ua = usuario.getPessoa().getUa();

		if (ua != null) {

			this.orgaoSelecionado = ua.getUg();

		}

		this.autorizar = false;

		this.atender = false;

		this.atendimento = false;

		this.status = StatusAbastecimento.AUTORIZADO;

		if (SgfUtil.isAdministrador(usuario) || SgfUtil.isCoordenador(usuario)) {

			super.search();

		} else if (SgfUtil.isOperador(usuario)) {

			this.dtInicial = DateUtil.getDateTime(new Date(), "00:00:00");

			this.dtFinal = DateUtil.getDateTime(new Date(), "23:59:59");

			this.entities = service.findByPeriodoAndPosto(usuario.getPosto()
					.getCodPosto(), this.dtInicial, this.dtFinal, this.status);

		} else if (SgfUtil.isChefeTransporte(usuario)) {

			this.dtInicial = DateUtil.getDateTime(new Date(), "00:00:00");

			this.dtFinal = DateUtil.getDateTime(new Date(), "23:59:59");

			this.entities = service.pesquisarPeriodo(this.dtInicial,
					this.dtFinal, usuario.getPessoa().getUa().getUg(),
					this.status);

			loadVeiculos();

			loadMotoristas();

			setCurrentBean(currentBeanName());

			setCurrentState(SEARCH);
		}

		Double qtdAbastecida = 0.0;

		for (Abastecimento o : this.entities) {

			if (SgfUtil.isAdministrador(usuario)
					|| SgfUtil.isCoordenador(usuario)) {

				this.autorizar = true;

			} else if (usuario.getAutoriza() != null) {

				this.autorizar = this.usuario.getAutoriza();
			}

			if (!this.atendimento) {

				this.atendimento = SgfUtil.isOperador(usuario)
						&& (o.getStatus()
								.equals(StatusAbastecimento.AUTORIZADO));
			}

			if (this.autorizar || this.atendimento) {

				this.negar = false;

				this.atender = false;

				filtro.add(o);
			}
		}

		this.entities.clear();

		this.entities.addAll(filtro);

		this.start = false;

		setCurrentBean(currentBeanName());

		setCurrentState(SEARCH);

		return SUCCESS;
	}

	@Override
	public String save() {

		if (isValid()) {

			this.entity.setDataAutorizacao(new Date());

			this.entity.setAutorizador(SgfUtil.usuarioLogado());

			return super.save();
		}

		return FAIL;
	}

	@Override
	public String update() {

		if (getCurrentState().equals(VIEW) && this.entity.getStatus().equals(StatusAbastecimento.AUTORIZADO)) {

			StatusAbastecimento status = entity.getStatus();

				if (validaQuilometragem()) {

					this.entity.setQuilometragem(kmAtendimento);

					this.entity.setStatus(StatusAbastecimento.ATENDIDO);

					Date now = new Date();

					AtendimentoAbastecimento atendimento = new AtendimentoAbastecimento();

					atendimento.setBomba(this.bomba);

					atendimento.setData(now);

					atendimento.setHora(now);

					atendimento.setQuantidadeAbastecida(quantidadeAbastecida);

					atendimento.setQuilometragem(kmAtendimento);

					atendimento.setUsuario(SgfUtil.usuarioLogado());

					atendimento
							.setStatus(StatusAtendimentoAbastecimento.ATENDIDO);

					atendimento.setAbastecimento(this.entity);

					atendimentoService.save(atendimento);

				} else {
					return FAIL;
				}
		}
		return super.update();
	}

	public void negar() {

		Date now = new Date();

		AtendimentoAbastecimento atendimento = new AtendimentoAbastecimento();

		atendimento.setData(now);

		atendimento.setHora(now);

		atendimento.setUsuario(SgfUtil.usuarioLogado());

		atendimento.setStatus(StatusAtendimentoAbastecimento.NEGADO);

		atendimento.setAbastecimento(this.entity);

		atendimentoService.save(atendimento);

		// this.entity.setAtendimento(new AtendimentoAbastecimento());
		//
		// this.entity.getAtendimento().setData(now);
		//
		// this.entity.getAtendimento().setHora(now);
		//
		// this.entity.getAtendimento().setUsuario(SgfUtil.usuarioLogado());
		//
		// this.entity.getAtendimento().setStatus(StatusAtendimentoAbastecimento.NEGADO);
		//
		// this.entity.getAtendimento().setAbastecimento(this.entity);
		//
		// atendimentoService.save(this.entity.getAtendimento());

		this.entity.setStatus(StatusAbastecimento.NEGADO);

		super.update();
	}

	public String atenderAbastecimento() {

		Cota cota = this.entity.getVeiculo().getCota();

		this.saldoAtual = cota.getCotaDisponivel();

		this.bombas = new ArrayList<Bomba>();

		this.kmAtendimento = null;

		 this.quantidadeAbastecida = null;

		this.bomba = null;

		this.veiculos = new ArrayList<Veiculo>();

		this.tiposServico = new ArrayList<TipoServico>();

		this.tiposServico.add(tipoServicoService.retrieve(1));

		// this.veiculos =
		// veiculoService.findByUG(usuario.getPessoa().getUa().getUg());
		// this.motoristas =
		// motoristaService.findByOrgao(usuario.getPessoa().getUa().getUg());

		for (Abastecimento e : this.entities) {

			this.veiculos.add(e.getVeiculo());

			this.motoristas.add(e.getMotorista());
		}

		if (usuario.getPosto() != null) {

			this.bombas.addAll(bombaService.findBombaByPosto(usuario.getPosto()
					.getCodPosto()));
		}

		prepareView();

		this.atendimento = true;

		return SUCCESS;
	}

	public List<Veiculo> getVeiculos() {
		return veiculos;
	}

	public void setVeiculos(List<Veiculo> veiculos) {
		this.veiculos = veiculos;
	}

	public List<TipoServico> getTiposServico() {
		return tiposServico;
	}

	public void setTiposServico(List<TipoServico> tiposServico) {
		this.tiposServico = tiposServico;
	}

	public List<Motorista> getMotoristas() {
		return motoristas;
	}

	public void setMotoristas(List<Motorista> motoristas) {
		this.motoristas = motoristas;
	}

	public List<Posto> getPostos() {
		return postos;
	}

	public void setPostos(List<Posto> postos) {
		this.postos = postos;
	}

	public User getUsuario() {
		return usuario;
	}

	public void setUsuario(User usuario) {
		this.usuario = usuario;
	}

	public boolean isAutorizar() {
		return autorizar;
	}

	public void setAutorizar(boolean autorizar) {
		this.autorizar = autorizar;
	}

	public void setAtendimento(Boolean atendimento) {
		this.atendimento = atendimento;
	}

	public boolean isAtendimento() {
		return atendimento;
	}

	public boolean isAtender() {
		return atender;
	}

	public void setAtender(boolean atender) {
		this.atender = atender;
	}

	public void setBombas(List<Bomba> bombas) {
		this.bombas = bombas;
	}

	public List<Bomba> getBombas() {
		return bombas;
	}

	public Long getKmAtendimento() {
		return kmAtendimento;
	}

	public void setKmAtendimento(Long kmAtendimento) {
		this.kmAtendimento = kmAtendimento;
	}

	public Double getQuantidadeAbastecida() {
		return quantidadeAbastecida;
	}

	public void setQuantidadeAbastecida(Double quantidadeAbastecida) {
		this.quantidadeAbastecida = quantidadeAbastecida;
	}

	public Bomba getBomba() {
		return bomba;
	}

	public void setBomba(Bomba bomba) {
		this.bomba = bomba;
	}

	public void setNegar(Boolean negar) {
		this.negar = negar;
	}

	public Boolean getNegar() {
		return negar;
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

	public void setCombustiveis(List<TipoCombustivel> combustiveis) {
		this.combustiveis = combustiveis;
	}

	public List<TipoCombustivel> getCombustiveis() {
		return combustiveis;
	}

	public void setUltimaQuilometragem(Long ultimaQuilometragem) {
		this.ultimaQuilometragem = ultimaQuilometragem;
	}

	public Long getUltimaQuilometragem() {
		return ultimaQuilometragem;
	}

	public void setStart(Boolean start) {
		this.start = start;
	}

	public boolean isStart() {
		return start;
	}

	public void setOrgaoSelecionado(UG orgaoSelecionado) {
		this.orgaoSelecionado = orgaoSelecionado;
	}

	public UG getOrgaoSelecionado() {
		return orgaoSelecionado;
	}

	public UG getUg() {
		return ug;
	}

	public void setUg(UG ug) {
		this.ug = ug;
	}

	public void setMostrarPosto(Boolean mostrarPosto) {
		this.mostrarPosto = mostrarPosto;
	}

	public Boolean getMostrarPosto() {
		return mostrarPosto;
	}

	public UG getOrgaoCadSelecionado() {
		return orgaoCadSelecionado;
	}

	public void setOrgaoCadSelecionado(UG orgaoCadSelecionado) {
		this.orgaoCadSelecionado = orgaoCadSelecionado;
	}

	public UA getUaSelecionada() {
		return uaSelecionada;
	}

	public void setUaSelecionada(UA uaSelecionada) {
		this.uaSelecionada = uaSelecionada;
	}

	public StatusAbastecimento getStatus() {
		return status;
	}

	public void setStatus(StatusAbastecimento status) {
		this.status = status;
	}

	public String getPlaca() {
		return placa;
	}

	public void setPlaca(String placa) {
		this.placa = placa;
	}

	public Double getSaldoAtual() {
		return saldoAtual;
	}

	public void setSaldoAtual(Double saldoAtual) {
		this.saldoAtual = saldoAtual;
	}

}
