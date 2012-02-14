package br.gov.ce.fortaleza.cti.sgf.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.Ponto;
import br.gov.ce.fortaleza.cti.sgf.entity.Transmissao;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;
import br.gov.ce.fortaleza.cti.sgf.service.PontoService;
import br.gov.ce.fortaleza.cti.sgf.service.TransmissaoService;
import br.gov.ce.fortaleza.cti.sgf.service.VeiculoService;
import br.gov.ce.fortaleza.cti.sgf.util.DateUtil;
import br.gov.ce.fortaleza.cti.sgf.util.JSFUtil;
import br.gov.ce.fortaleza.cti.sgf.util.PontoDTO;
import br.gov.ce.fortaleza.cti.sgf.util.SgfUtil;

@Scope("session")
@Component("pontoBean")
public class PontoBean extends EntityBean<Integer, Ponto> {

	private static final Log logger = LogFactory.getLog(PontoBean.class);

	@Autowired
	private PontoService service;

	@Autowired
	private TransmissaoService transmissaoService;

	@Autowired
	private VeiculoService veiculoService;
	
	private Boolean reload = true;

	private Double lat;
	private Double lng;
	private String ponto;
	private List<PontoDTO> pontos;
	private List<Veiculo> veiculos;
	private String geoms = new String("");
	private boolean show = false;
	private Veiculo veiculo;
	private String route = new String("");


	@Override
	protected Integer retrieveEntityId(Ponto entity) {
		return entity.getId();
	}

	@Override
	protected PontoService retrieveEntityService() {
		return this.service;
	}

	@Override
	protected Ponto createNewEntity() {
		Ponto ponto = new Ponto();
		return ponto;
	}

	@Override
	public String search(){
		this.geoms = new String("");
		String result = mostrarVeiculoMap();
		setCurrentBean(currentBeanName());
		setCurrentState(SEARCH);
		return result;
	}

	public String mostrarRotaVeiculo(){
		this.reload = false;
		this.route = "";
		if(this.veiculo != null){
			Veiculo v = veiculoService.retrieve(veiculo.getId());
			Date current = new Date();
			List<Transmissao> transmissoes = transmissaoService.findByVeiculo(veiculo.getId(), DateUtil.adicionarOuDiminuir(current, -DateUtil.DAY_IN_MILLIS), current);
			if(transmissoes.size() > 0){
				for (Transmissao t : transmissoes) {
					Float vel = t.getVelocidade() == null ? 0F : t.getVelocidade();
					Float odometro = t.getOdometro() == null ? 0F : t.getOdometro();
					String placa = v.getPlaca();
					String pprox = t.getPonto() == null ? "--" : t.getPonto().getDescricao();
					Float dist = t.getDistancia() == null ? 0F : t.getDistancia();
					this.route += t.getX() + "#%" + t.getY() + "#%" + t.getVeiculoId() + "#%" +  placa + "#%" + vel + "#%" + odometro + 
					"#%" + pprox + "#%" + dist + "#%" + DateUtil.parseAsString("dd/MM/yyyy HH:mm", t.getDataTransmissao())  + "$*@";
				}
			} else {
				JSFUtil.getInstance().addErrorMessage("msg.error.veiculo.sem.rota");
				return FAIL;
			}
		} else {
			return FAIL;
		}
		return SUCCESS;
	}

	public String mostrarVeiculoMap(){
		this.reload = true;
		this.veiculos = new ArrayList<Veiculo>();
		this.geoms = new String("");
		if(SgfUtil.isAdministrador(SgfUtil.usuarioLogado())){
			this.veiculos = veiculoService.retrieveAll();
			for (Veiculo v : this.veiculos) {
				if(v.getX() != null && v.getY() != null){
					String modelo = v.getModelo() == null ? " " : v.getModelo().getDescricao();
					Float vel = v.getVelocidade() == null ? 0F : v.getVelocidade();
					Float odometro = v.getOdometro() == null ? 0F : v.getOdometro();
					String pprox = v.getPontoProximo() == null ? "--" :   v.getPontoProximo().getDescricao();
					this.geoms += v.getX() + "#%" + v.getY() + "#%" + modelo + "#%" + v.getPlaca() + "#%" + vel + "#%" + odometro + 
					"#%" + pprox + "#%" + v.getDistancia() + "#%" +DateUtil.parseAsString("dd/MM/yyyy HH:mm", v.getDataTransmissao())  + "$*@";
					//System.out.println(v.getX() + "#%" + v.getY());
				}
			}
		} else if(this.veiculo == null){
			this.veiculos = veiculoService.findByUG(SgfUtil.usuarioLogado().getPessoa().getUa().getUg());
			for (Veiculo v : this.veiculos) {
				if(v.getX() != null && v.getY() != null){
					String modelo = v.getModelo() == null ? " " : v.getModelo().getDescricao();
					Float vel = v.getVelocidade() == null ? 0F : v.getVelocidade();
					Float odometro = v.getOdometro() == null ? 0F : v.getOdometro();
					String pprox = v.getPontoProximo() == null ? "--" :   v.getPontoProximo().getDescricao();
					this.geoms += v.getX() + "#%" + v.getY() + "#%" + modelo + "#%" + v.getPlaca() + ":" + vel + "#%" + odometro + 
					"#%" + pprox + "#%" + v.getDistancia() + "#%" +DateUtil.parseAsString("dd/MM/yyyy HH:mm", v.getDataTransmissao())  + "$*@";
				}
			}
		} else {
			if(veiculo.getX() != null && veiculo.getY() != null){
				String modelo = veiculo.getModelo() == null ? " " : veiculo.getModelo().getDescricao();
				Float vel = veiculo.getVelocidade() == null ? 0F : veiculo.getVelocidade();
				Float odometro = veiculo.getOdometro() == null ? 0F : veiculo.getOdometro();
				String pprox = veiculo.getPontoProximo() == null ? "--" :   veiculo.getPontoProximo().getDescricao();
				this.geoms += veiculo.getX() + "#%" + veiculo.getY() + "#%" + modelo + "#%" + veiculo.getPlaca() + "#%" + vel + "#%" + odometro + 
				"#%" + pprox + "#%" + veiculo.getDistancia() + "#%" +DateUtil.parseAsString("dd/MM/yyyy HH:mm", veiculo.getDataTransmissao())  + "$*@";
			}
		}
		return SUCCESS;
	}

	@Override
	public String prepareSave() {
		this.lat = null;
		this.lng = null;
		return super.prepareSave();
	}

	@Override
	public String save() {
		if(lat != null && lng != null){
			this.entity.setX(lat);
			this.entity.setY(lng);
			JSFUtil.getInstance().addErrorMessage("msg.error.veiculo.localizacao.gravada");
			return super.save();
		} else {
			JSFUtil.getInstance().addErrorMessage("msg.error.veiculo.localizacao.nao.encontrada");
			return FAIL;
		}
	}

	@Override
	public String prepareUpdate() {
		this.lat = this.entity.getX(); //(float) ((Point)this.entity.getGeometry()).getX();
		this.lng = this.entity.getY();//(float) ((Point)this.entity.getGeometry()).getY();
		return super.prepareUpdate();
	}

	@Override
	public String update() {
		if(this.lat != null && this.lat != null){
			this.entity.setX(this.lat);
			this.entity.setY(this.lng);
			return super.update();
		} else {
			JSFUtil.getInstance().addErrorMessage("msg.error.veiculo.localizacao.nao.encontrada");
			return FAIL;
		}
	}

	@Transactional
	public String salvarLocalizacaoVeiculo(){
		this.reload = true;
		if(this.veiculo != null){
			if(this.lat != null && this.lng != null){
				Transmissao t = new Transmissao();
				t.setVeiculoId(this.veiculo.getId());
				t.setDataTransmissao(new Date());
				t.setX(this.lat);
				t.setY(this.lng);
				this.transmissaoService.save(t);
				setCurrentBean(currentBeanName());
				setCurrentState(SEARCH);
				return SUCCESS;
			} else {
				JSFUtil.getInstance().addErrorMessage("msg.error.veiculo.localizacao.nao.encontrada");
				return FAIL;
			}
		} else {
			JSFUtil.getInstance().addErrorMessage("msg.error.veiculo.localizacao.nao.encontrada");
			return FAIL;
		}
	}

	public Double getLat() {
		return lat;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}

	public Double getLng() {
		return lng;
	}

	public void setLng(Double lng) {
		this.lng = lng;
	}

	public String getPonto() {
		return ponto;
	}

	public void setPonto(String ponto) {
		this.ponto = ponto;
	}

	public boolean isShow() {
		return show;
	}

	public void setShow(boolean show) {
		this.show = show;
	}

	public List<PontoDTO> getPontos() {
		return pontos;
	}

	public void setPontos(List<PontoDTO> pontos) {
		this.pontos = pontos;
	}

	public String getGeoms() {
		return geoms;
	}

	public void setGeoms(String geoms) {
		this.geoms = geoms;
	}

	public Veiculo getVeiculo() {
		return veiculo;
	}

	public void setVeiculo(Veiculo veiculo) {
		this.veiculo = veiculo;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public Boolean getReload() {
		return reload;
	}

	public void setReload(Boolean reload) {
		this.reload = reload;
	}
}
