/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.service;

import java.util.List;

import javax.persistence.NonUniqueResultException;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.Pessoa;

/**
 * @author Deivid
 *
 */
@Repository
@Transactional
public class PessoaService extends BaseService<Integer, Pessoa> {

	@Transactional
	public List<Pessoa> findByUG(String ug) throws NonUniqueResultException{
		return executeResultListQuery("findByUG", ug);
	}

	@Transactional
	public Pessoa findByCPF(String cpf) throws NonUniqueResultException{
		return executeSingleResultQuery("findByCPF", cpf);
	}

	@Transactional
	public Boolean findPessoaByCpf(String cpf) throws NonUniqueResultException{
		Boolean retorno = false;
		Pessoa pessoa = executeSingleResultQuery("findByCPF", cpf);
		if(pessoa != null){
			retorno = true;
		}
		return retorno;
	}
}
