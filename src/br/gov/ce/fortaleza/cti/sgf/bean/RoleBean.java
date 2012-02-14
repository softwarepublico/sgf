/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.Permissao;
import br.gov.ce.fortaleza.cti.sgf.entity.Role;
import br.gov.ce.fortaleza.cti.sgf.entity.UA;
import br.gov.ce.fortaleza.cti.sgf.service.PermissaoService;
import br.gov.ce.fortaleza.cti.sgf.service.RoleService;

/**
 * @author Deivid
 *
 */
@Scope("session")
@Component("roleBean")
public class RoleBean extends EntityBean<Integer, Role>{


	@Autowired
	private RoleService service;

	@Autowired
	private PermissaoService permissaoService;

	private Permissao permissao = new Permissao();
	private List<Permissao> permissoes;

	@Override
	protected Role createNewEntity() {
		this.permissoes = new ArrayList<Permissao>();
		this.permissoes.addAll(permissaoService.retrieveAll());
		this.permissao = new Permissao();
		return new Role();
	}

	@Override
	protected Integer retrieveEntityId(Role entity) {

		return entity.getCodGrupo();
	}

	@Override
	protected RoleService retrieveEntityService() {

		return this.service;
	}

	@Override
	public String prepareEdit() {

		this.permissao = new Permissao();
		this.entity = service.retrieve(this.entity.getCodGrupo());
		this.permissoes = permissaoService.retrieveAll();
		this.permissoes.removeAll(this.entity.getPermissoes());
		Collections.sort(this.permissoes, new Comparator<Permissao>() {
			public int compare(Permissao o1, Permissao o2) {
				return o1.getDescricao().compareTo(o2.getDescricao());
			}
		});
		setCurrentBean(currentBeanName());
		setCurrentState(EDIT);
		return SUCCESS;
	}

	public boolean isPermissaoStatus(){

		return this.permissoes.size() > 0;
	}

	@Transactional(readOnly=true)
	public List<Role> getRoles(){

		return  new ArrayList<Role>(service.findAll());
	}

	public String add(){

		this.entity.getPermissoes().add(this.permissao);
		
		service.update(entity);

		this.permissoes.remove(this.permissao);

		return SUCCESS;
	}

	public String remove(){

		this.entity.getPermissoes().remove(this.permissao);

		this.permissoes.add(this.permissao);

		return SUCCESS;
	}

	public Permissao getPermissao() {
		return permissao;
	}

	public void setPermissao(Permissao permissao) {
		this.permissao = permissao;
	}

	public List<Permissao> getPermissoes() {
		return permissoes;
	}

	public void setPermissoes(List<Permissao> permissoes) {
		this.permissoes = permissoes;
	}
}
