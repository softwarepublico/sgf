/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.gov.ce.fortaleza.cti.sgf.entity.AtendimentoAbastecimento;
import br.gov.ce.fortaleza.cti.sgf.service.AtendimentoService;
import br.gov.ce.fortaleza.cti.sgf.service.BaseService;

/**
 * @author Deivid
 * @since 28/01/2010	
 */
@Scope("session")
@Component("atendimentoAbastecimentoBean")
public class AtendimentoAbastecimentoBean extends EntityBean<Integer, AtendimentoAbastecimento>{
	
	@Autowired
	private AtendimentoService service;

	@Override
	protected AtendimentoAbastecimento createNewEntity() {
		// TODO Auto-generated method stub
		return new AtendimentoAbastecimento();
	}

	@Override
	protected Integer retrieveEntityId(AtendimentoAbastecimento entity) {
		// TODO Auto-generated method stub
		return entity.getId();
	}

	@Override
	protected BaseService<Integer, AtendimentoAbastecimento> retrieveEntityService() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setService(AtendimentoService service) {
		this.service = service;
	}

	public AtendimentoService getService() {
		return service;
	}

	

}
