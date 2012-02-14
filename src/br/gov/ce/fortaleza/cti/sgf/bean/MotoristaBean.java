/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.persistence.NonUniqueResultException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.gov.ce.fortaleza.cti.sgf.entity.Motorista;
import br.gov.ce.fortaleza.cti.sgf.entity.Pessoa;
import br.gov.ce.fortaleza.cti.sgf.entity.Posto;
import br.gov.ce.fortaleza.cti.sgf.entity.Role;
import br.gov.ce.fortaleza.cti.sgf.entity.UA;
import br.gov.ce.fortaleza.cti.sgf.entity.UG;
import br.gov.ce.fortaleza.cti.sgf.service.MotoristaService;
import br.gov.ce.fortaleza.cti.sgf.service.PessoaService;
import br.gov.ce.fortaleza.cti.sgf.service.UAService;
import br.gov.ce.fortaleza.cti.sgf.service.UGService;
import br.gov.ce.fortaleza.cti.sgf.util.DateUtil;
import br.gov.ce.fortaleza.cti.sgf.util.JSFUtil;
import br.gov.ce.fortaleza.cti.sgf.util.SgfUtil;

/**
 * @author Deivid
 * @since 07/12/09	
 */
@Scope("session")
@Component("motoristaBean")
public class MotoristaBean extends EntityBean<Integer, Motorista>{

	@Autowired
	protected MotoristaService motoristaService;

	@Autowired
	private UGService ugService;

	@Autowired
	private UAService uaService;

	@Autowired
	private PessoaService pessoaService;

	private UG ug;
	private UA ua;
	private Pessoa pessoa;
	private List<UG> ugs;
	private List<UA> uas;
	private List<Posto> listaPostos;
	private List<SelectItem> postoItens;
	private Boolean status = true;
	private Boolean mostrarUA = false;
	private Boolean searchName = true;
	private Boolean pessoaCadastrada = false;
	private String cpf;
	private String validadeCnh;


	@Override
	protected Motorista createNewEntity() {

		this.pessoaCadastrada = false;

		this.ug = new UG();

		this.ua = new UA();

		this.uas = new ArrayList<UA>();

		this.ugs = new ArrayList<UG>();

		this.validadeCnh = null;

		return new Motorista();
	}

	@Override
	protected Integer retrieveEntityId(Motorista entity) {

		this.pessoaCadastrada = false;

		return entity.getCodMotorista();
	}

	@Override
	protected MotoristaService retrieveEntityService() {

		return this.motoristaService;
	}

	@SuppressWarnings("static-access")
	public String loadUas(ActionEvent event){

		this.uas = new ArrayList<UA>();

		if(this.ug != null){

			this.uas = new ArrayList<UA>(uaService.retrieveByUG(this.ug.getId()));

			Collections.sort(this.uas, new Comparator<UA>() {
				public int compare(UA o1, UA o2) {
					return o1.getDescricao().compareTo(o2.getDescricao());
				}
			});

			this.mostrarUA = true;

			return super.SUCCESS;

		} else {

			this.uas = null;

			this.mostrarUA = false;

			return super.SUCCESS;
		}
	}

	@Override
	public String prepareUpdate() {

		this.pessoa = pessoaService.executeSingleResultQuery("findByCPF", entity.getPessoa().getCpf());

		if(this.pessoa.getUa() != null){

			this.mostrarUA = true;

			this.ua = this.pessoa.getUa();

			if(this.ua != null){

				this.ug = this.ua.getUg();

				this.uas = new ArrayList<UA>(uaService.retrieveByUG(this.ug.getId()));
			}

			this.ug = this.pessoa.getUa().getUg();

			this.uas = new ArrayList<UA>(uaService.retrieveByUG(this.ug.getId()));
		}

		this.validadeCnh = DateUtil.parseAsString("dd/MM/yyyy", this.entity.getDtValidade());

		return super.prepareUpdate();
	}

	public void bloquear(){

		this.entity.setAtivo("true");

		this.motoristaService.update(this.entity);

		search();
	}

	public void desbloquear(){

		this.entity.setAtivo("false");

		this.motoristaService.update(this.entity);

		search();
	}

	public String save() {

		this.entity.setPessoa(this.pessoa);

		this.entity.setAtivo("true");

		this.entity.setPontosCnh(0);

		this.entity.getPessoa().setUa(this.ua);

		this.pessoaService.update(this.entity.getPessoa());

		this.pessoaCadastrada = false;

		super.save();

		searchSort();

		setCurrentBean(currentBeanName());

		return SUCCESS;
	}

	public String update() {

		this.entity.getPessoa().setUa(this.ua);

		pessoaService.update(this.entity.getPessoa());

		return super.update();
	}

	public synchronized List<Motorista> getMotoristas(){

		Role role = SgfUtil.usuarioLogado().getRole();

		UA ua = SgfUtil.usuarioLogado().getPessoa().getUa();

		List<Motorista> result = new ArrayList<Motorista>();

		if(ua != null){

			UG ug = ua.getUg();

			if(role.getAuthority().equals("ROLE_ADMIN") || role.getAuthority().equals("ROLE_COORD_TRANSP")){

				result = motoristaService.findByStatus("true");
			} else {

				result = motoristaService.findByUGStatus(ug.getId(), "true");
			}
		}

		Collections.sort(result, new Comparator<Motorista>() {
			public int compare(Motorista o1, Motorista o2) {
				return o1.getPessoa().getNome().compareTo(o2.getPessoa().getNome());
			}
		});

		return  result;
	}
	
	public String search(){

		return searchSort();
	}

	public String searchSort(){

		UA ua = SgfUtil.usuarioLogado().getPessoa().getUa();

		Role role = SgfUtil.usuarioLogado().getRole();

		if(ua != null){

			UG ug = ua.getUg();

			if(role.getAuthority().equals("ROLE_ADMIN") || role.getAuthority().equals("ROLE_COORD_TRANSP")){

				this.entities = motoristaService.findByStatus("true");
			} else {

				this.entities = motoristaService.findByUGStatus(ug.getId(), "true");
			}
		}

		Collections.sort(this.entities, new Comparator<Motorista>() {

			public int compare(Motorista o1, Motorista o2) {

				return o1.getPessoa().getNome().compareTo(o2.getPessoa().getNome());
			}
		});

		setCurrentBean(currentBeanName());

		setCurrentState(SEARCH);

		return SUCCESS;
	}


	public String searchMotoristas(){

		UA ua = SgfUtil.usuarioLogado().getPessoa().getUa();

		if(ua != null){

			UG ug = ua.getUg();

			Role role = SgfUtil.usuarioLogado().getRole();

			if(role.getAuthority().equals("ROLE_ADMIN") || role.getAuthority().equals("ROLE_COORD_TRANSP")){

				String str = this.status == true ? "true" : "false";

				if(this.filter == ""){

					this.entities = motoristaService.findByUGNameStatus(null, null, str);
				} else {

					this.entities = motoristaService.findByUGNameStatus(null, this.filter, str);
				}

			} else {

				String str = this.status == true ? "true" : "false";

				if(this.filter == ""){

					this.entities = motoristaService.findByUGNameStatus(ug.getId(), null, str);
				} else {

					this.entities = motoristaService.findByUGNameStatus(ug.getId(), this.filter, str);
				}
			}
		}

		return SUCCESS;
	}

	public String searchMotoristaByCpf(){

		try {

			this.pessoa = pessoaService.findByCPF(this.cpf);

			if(this.pessoa != null){

				this.pessoaCadastrada = true;

				boolean motoristaCadastrado = motoristaService.findPessoaByCpf(this.cpf);

				if(motoristaCadastrado){

					this.pessoaCadastrada = false;

					JSFUtil.getInstance().addErrorMessage("msg.error.motorista.cadastrado");
				}

			} else {

				this.pessoaCadastrada = false;

				JSFUtil.getInstance().addErrorMessage("msg.error.cpf.inexistente");
			}

		} catch (NonUniqueResultException e) {

			JSFUtil.getInstance().addErrorMessage("msg.error.cpf.duplicado");
		}

		return SUCCESS;
	}

	@Override
	public String populate(){

		this.filter = "";

		return super.populate();
	}

	public UG getUg() {
		return ug;
	}

	public void setUg(UG ug) {
		this.ug = ug;
	}

	public UA getUa() {
		return ua;
	}

	public void setUa(UA ua) {
		this.ua = ua;
	}

	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}

	public List<UG> getUgs() {
		return ugs;
	}

	public void setUgs(List<UG> ugs) {
		this.ugs = ugs;
	}

	public List<UA> getUas() {
		return uas;
	}

	public void setUas(List<UA> uas) {
		this.uas = uas;
	}

	public List<Posto> getListaPostos() {
		return listaPostos;
	}

	public void setListaPostos(List<Posto> listaPostos) {
		this.listaPostos = listaPostos;
	}

	public List<SelectItem> getPostoItens() {
		return postoItens;
	}

	public void setPostoItens(List<SelectItem> postoItens) {
		this.postoItens = postoItens;
	}

	public Boolean getMostrarUA() {
		return mostrarUA;
	}

	public void setMostrarUA(Boolean mostrarUA) {
		this.mostrarUA = mostrarUA;
	}

	public Boolean getPessoaCadastrada() {
		return pessoaCadastrada;
	}

	public void setPessoaCadastrada(Boolean pessoaCadastrada) {
		this.pessoaCadastrada = pessoaCadastrada;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public Boolean getSearchName() {
		return searchName;
	}

	public void setSearchName(Boolean searchName) {
		this.searchName = searchName;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getValidadeCnh() {
		return validadeCnh;
	}

	public void setValidadeCnh(String validadeCnh) {
		this.validadeCnh = validadeCnh;
	}
}
