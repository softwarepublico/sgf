package br.gov.ce.fortaleza.cti.sgf.service;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.Modelo;

/**
 * Servi�o que gerencia aos dados referentes aos modelos de ve�culos no
 * banco de dados
 * 
 * @author Edilson
 *
 */
@Repository
@Transactional
public class ModeloService extends BaseService<Integer, Modelo> {

	/**
	 * Busca um modelo atrav�s de uma descri��o passada
	 * 
	 * @param modelo
	 * @return
	 */
	public Modelo findByDesc(String modelo) {
		
		Query query = this.entityManager.createNamedQuery("Modelo.findByDesc");
		
		query.setParameter("descricao", modelo);
		
		return (Modelo) query.getSingleResult();
	}
}