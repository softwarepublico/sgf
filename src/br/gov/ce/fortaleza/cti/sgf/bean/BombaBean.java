/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.bean;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.gov.ce.fortaleza.cti.sgf.entity.Bomba;
import br.gov.ce.fortaleza.cti.sgf.entity.Posto;
import br.gov.ce.fortaleza.cti.sgf.entity.TipoCombustivel;
import br.gov.ce.fortaleza.cti.sgf.service.BaseService;
import br.gov.ce.fortaleza.cti.sgf.service.BombaService;
import br.gov.ce.fortaleza.cti.sgf.service.PostoService;
import br.gov.ce.fortaleza.cti.sgf.service.TipoCombustivelService;
import br.gov.ce.fortaleza.cti.sgf.util.JSFUtil;

/**
 * @author Deivid
 * @since 11/12/09
 */
@Scope("session")
@Component("bombaBean")
public class BombaBean extends EntityBean<Integer, Bomba>{

	//Os services
	@Autowired
	private BombaService bombaService;
	
	@Autowired
	private PostoService postoService;
	
	@Autowired
	private TipoCombustivelService combustivelService;
	
	//Filtros da pesquisa
	private String numeroPesquisa;
	private Posto postoPesquisa;
	private TipoCombustivel combustivelPesquisa;
		
	//Atributos do Bean
	private Posto postoSelecionado;
	private TipoCombustivel combustivelSelecionado;
	private List<SelectItem> postoItens;
	private List<SelectItem> combustivelItens;
	
	@PostConstruct
	@SuppressWarnings("unused")
	public void init(){
		PostoBean postoBean = (PostoBean) JSFUtil.getInstance().getSessionBean("postoBean");
		CombustivelBean combustivelBean = (CombustivelBean) JSFUtil.getInstance().getSessionBean("combustivelBean");
		List<Posto> postos = postoService.retrieveAll();
		postoItens = new ArrayList<SelectItem>();
		for (Posto posto : postos) {
			postoItens.add(new SelectItem(posto, posto.getDescricao()));
		}
		List<TipoCombustivel> combustivels = combustivelService.retrieveAll(); 
		combustivelItens = new ArrayList<SelectItem>();
		for (TipoCombustivel tipoCombustivel : combustivels) {
			if(!tipoCombustivel.getDescricao().equals("N")){
				combustivelItens.add(new SelectItem(tipoCombustivel, tipoCombustivel.getDescricao()));
			}
		}
	}
	
	@Override
	public String prepareUpdate() {
		// TODO Auto-generated method stub
		init();
		return super.prepareUpdate();
	}
	
	@Override
	public String prepareSave() {
		// TODO Auto-generated method stub
		init();
		return super.prepareSave();
	}
	
	@Override
	public String save() {
		// TODO Auto-generated method stub
		boolean existeTipoCombustivel = bombaService.findPostoCombustivel(entity);  
		if(!existeTipoCombustivel){
			FacesContext context = FacesContext.getCurrentInstance();
			context.addMessage("posto", 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, null, 
							"Este posto não oferece esse tipo de combustivel."));
			return FAIL;
		}
		boolean existe = bombaService.findBombaPosto(entity);
		if(existe){
			FacesContext context = FacesContext.getCurrentInstance();
			context.addMessage("numero", 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, null, 
							"Bomba já cadastrada neste posto."));
			return FAIL;
		}
		return super.save();
	}
	
	@Override
	public String search() {
		// TODO Auto-generated method stub
		
		Bomba bomba = new Bomba();
		if(StringUtils.hasText(numeroPesquisa)){
			bomba.setNumero(Integer.valueOf(numeroPesquisa));
		}
		
		bomba.setPosto(postoPesquisa);
		bomba.setCombustivel(combustivelPesquisa);
		
		entities = bombaService.pesquisar(bomba);
		
		setCurrentBean(currentBeanName());
		setCurrentState(SEARCH);
		
		return SUCCESS;
	}
	
	@Override
	protected Bomba createNewEntity() {
		// TODO Auto-generated method stub
		return new Bomba();
	}

	@Override
	protected Integer retrieveEntityId(Bomba entity) {
		// TODO Auto-generated method stub
		return entity.getId();
	}

	@Override
	protected BaseService<Integer, Bomba> retrieveEntityService() {
		// TODO Auto-generated method stub
		return this.bombaService;
	}
	

	public Posto getPostoSelecionado() {
		return postoSelecionado;
	}

	public void setPostoSelecionado(Posto postoSelecionado) {
		this.postoSelecionado = postoSelecionado;
	}

	public TipoCombustivel getCombustivelSelecionado() {
		return combustivelSelecionado;
	}

	public void setCombustivelSelecionado(TipoCombustivel combustivelSelecionado) {
		this.combustivelSelecionado = combustivelSelecionado;
	}

	public List<SelectItem> getPostoItens() {
		return postoItens;
	}

	public void setPostoItens(List<SelectItem> postoItens) {
		this.postoItens = postoItens;
	}

	public List<SelectItem> getCombustivelItens() {
		return combustivelItens;
	}

	public void setCombustivelItens(List<SelectItem> combustivelItens) {
		this.combustivelItens = combustivelItens;
	}

	public BombaService getBombaService() {
		return bombaService;
	}

	public void setBombaService(BombaService bombaService) {
		this.bombaService = bombaService;
	}

	public PostoService getPostoService() {
		return postoService;
	}

	public void setPostoService(PostoService postoService) {
		this.postoService = postoService;
	}

	public TipoCombustivelService getCombustivelService() {
		return combustivelService;
	}

	public void setCombustivelService(TipoCombustivelService combustivelService) {
		this.combustivelService = combustivelService;
	}

	public String getNumeroPesquisa() {
		return numeroPesquisa;
	}

	public void setNumeroPesquisa(String numeroPesquisa) {
		this.numeroPesquisa = numeroPesquisa;
	}

	public Posto getPostoPesquisa() {
		return postoPesquisa;
	}

	public void setPostoPesquisa(Posto postoPesquisa) {
		this.postoPesquisa = postoPesquisa;
	}

	public TipoCombustivel getCombustivelPesquisa() {
		return combustivelPesquisa;
	}

	public void setCombustivelPesquisa(TipoCombustivel combustivelPesquisa) {
		this.combustivelPesquisa = combustivelPesquisa;
	}
	
}
