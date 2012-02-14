package br.gov.ce.fortaleza.cti.sgf.service;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.Especie;

@Repository
@Transactional
public class EspecieService extends BaseService<Integer, Especie> {

}