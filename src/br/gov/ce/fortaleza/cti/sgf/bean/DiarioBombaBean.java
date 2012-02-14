/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.bean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.gov.ce.fortaleza.cti.sgf.entity.Bomba;
import br.gov.ce.fortaleza.cti.sgf.entity.DiarioBomba;
import br.gov.ce.fortaleza.cti.sgf.entity.Posto;
import br.gov.ce.fortaleza.cti.sgf.entity.User;
import br.gov.ce.fortaleza.cti.sgf.service.BombaService;
import br.gov.ce.fortaleza.cti.sgf.service.DiarioBombaService;
import br.gov.ce.fortaleza.cti.sgf.service.PostoService;
import br.gov.ce.fortaleza.cti.sgf.util.Constants;
import br.gov.ce.fortaleza.cti.sgf.util.DateUtil;
import br.gov.ce.fortaleza.cti.sgf.util.JSFUtil;
import br.gov.ce.fortaleza.cti.sgf.util.SgfUtil;

/**
 * @author Deivid
 * @since 11/12/09
 */
@Scope("session")
@Component("diarioBombaBean")
public class DiarioBombaBean extends EntityBean<Integer, DiarioBomba>{

	@Autowired
	private DiarioBombaService service;

	@Autowired
	private PostoService postoService;

	@Autowired
	private BombaService bombaService;

	private List<Posto> postos;
	private Bomba bombaSelecionada;
	private Boolean bombaAberta;
	private Boolean bombaFechada;
	private Boolean start;
	private User user = SgfUtil.usuarioLogado();
	private Boolean mostrarZeraBomba = false;

	@Override
	protected Integer retrieveEntityId(DiarioBomba entity) {
		return entity.getId();
	}

	@Override
	protected DiarioBombaService retrieveEntityService() {
		this.bombaSelecionada = new Bomba();
		return this.service;
	}

	protected DiarioBomba createNewEntity() {
		DiarioBomba diario = new DiarioBomba();
		return diario;
	}

	public String prepareState(){
		String retorno = null;
		this.entity.setZerada(false);
		if(this.entity.getStatus() == 0){
			retorno = this.prepareUpdate();
		} else if(this.entity.getStatus() == 1 && this.entity.getValorFinal() == null){
			retorno = this.prepareSave();
		} else if(this.entity.getStatus() == 1 && this.entity.getValorFinal() != null){
			JSFUtil.getInstance().addErrorMessage("msg.error.bomba.fechada");
		}
		return retorno;
	}

	public Boolean valida(){
		if (!isEditState()) {
			if(this.entity.getValorFinal() == null){
				JSFUtil.getInstance().addErrorMessage("msg.error.bomba.valFim");
				return false;
			}
		}
		if(this.entity.getValorInicial() != null && this.entity.getValorFinal() != null){
			if((this.entity.getValorFinal() < this.entity.getValorInicial()) && !this.entity.isZerada()){
				JSFUtil.getInstance().addErrorMessage("msg.error.bomba.valFim.inconsistente");
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
	}

	@Override
	public String prepareSave() {
		this.mostrarZeraBomba = false;
		DiarioBomba ultimaDiaria = service.findCurrentDiaryByBomba(this.entity.getBomba().getId());
		if(ultimaDiaria != null){
			this.entity.setHoraInicial(new Date());
			this.entity.setValorInicial(ultimaDiaria.getValorFinal());
			if(existeDiarias()){
				if(this.entity.getValorInicial() != null){
					verificaBombaZerada();
				}
			}
			setCurrentBean(currentBeanName());
			setCurrentState(SAVE);
			return SUCCESS;
		} else {
			return FAIL;
		}
	}

	@Override
	public String prepareUpdate() {
		this.mostrarZeraBomba = false;
		verificaBombaZerada();
		return super.prepareUpdate();
	}

	@Override
	public String save() {
		DiarioBomba ultimaDiaria = service.findCurrentDiaryByBomba(this.entity.getBomba().getId());
		if(ultimaDiaria.getValorInicial() > this.entity.getValorInicial()){
			JSFUtil.getInstance().addErrorMessage("msg.error.bomba.valorInicialMenorQFinalAnterior");
			return FAIL;
		}
		if(this.entity.getValorInicial() == null){
			JSFUtil.getInstance().addErrorMessage("msg.error.bomba.valorInicialNulo");
			return FAIL;
		}
		this.entity.setStatus(0);
		this.entity.setImageStatus("/images/open_icon.png");
		this.entity.setHoraInicial(new Date());
		return super.save();
	}

	@Override
	public String update() {
		if(valida()){
			if(this.entity.getValorInicial() == null){
				this.entity.setValorFinal(null);
				this.entity.setHoraFinal(null);
				this.entity.setHoraInicial(null);
				this.entity.setStatus(1);
				this.entity.setImageStatus("/images/closed.gif");
				service.delete(this.entity.getId());
			} else if(this.entity.getValorFinal() == null){
				this.entity.setStatus(0);
				this.entity.setImageStatus("/images/open_icon.png");
				this.entity.setHoraFinal(null);
				super.update();
				return searchSort();

			} else {
				this.entity.setStatus(1);
				this.entity.setImageStatus("/images/tick.png");
				this.entity.setHoraFinal(new Date());
				super.update();
				return searchSort();
			}
			return searchSort();
		} else {
			return FAIL;
		}
	}

	@Override
	public String searchSort() {
		iniciarDiarias();
		setCurrentBean(currentBeanName());
		setCurrentState(SEARCH);
		return SUCCESS;
	}

	public void zerarBomba(){
		this.entity.setZerada(true);
	}

	/**
	 * this.service.findDiariasByBomba(bombaSelecionada) faz uma consulta a todos as diárias
	 * @return
	 */
	private Boolean existeDiarias() {
		Date ultimaDiaria = service.findUltimaDiariaByBomba(bombaSelecionada.getId());
		Calendar calendar = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar2.setTime(ultimaDiaria);
		if(calendar2.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)){
			return true;
		}
		return false;
	}

	public void verificaBombaZerada(){
		if(this.entity.getValorInicial() >= Constants.LIM_SUP_LEIT_BOMBA_COMBUSTIVEL){
			this.entity.setZerada(true);
			setMostrarZeraBomba(true);
		}
	}

	public String voltar(){
		this.mostrarZeraBomba = false;
		iniciarDiarias();
		return super.searchSort();
	}

	public void iniciarDiarias() {
		this.postos = new ArrayList<Posto>();
		List<Posto> tmp = new ArrayList<Posto>();
		if(SgfUtil.isAdministrador(this.user) || SgfUtil.isCoordenador(this.user)){
			tmp = postoService.retrieveAll();
		} else if(SgfUtil.isOperador(this.user)){
			tmp.add(postoService.retrieve(this.user.getPosto().getCodPosto()));
		}
		for (Posto p : tmp) {
			for (Bomba b : p.getListaBomba()) {
				boolean existeDiaria = false;
				Calendar calendar = Calendar.getInstance();
				Calendar calendar2 = Calendar.getInstance();
				calendar.setTime(new Date());
				Date ultimaDiaria = service.findUltimaDiariaByBomba(b.getId());
				DiarioBomba diaria = service.findCurrentDiaryByBomba(b.getId());
				if(ultimaDiaria != null){
					calendar2.setTime(ultimaDiaria);
					if(calendar2.get(Calendar.DAY_OF_MONTH) < calendar.get(Calendar.DAY_OF_MONTH)){
						existeDiaria = false;
					} else {
						existeDiaria = true;
					}
				} else {
					existeDiaria = false;
				}
				if(existeDiaria){
					if(isUpdateState()){
						diaria.setImageStatus("/images/tick.png");
					}
					b.setDiarioBomba(diaria);
				} else {
					DiarioBomba db = new DiarioBomba();
					db.setDataCadastro(new Date());
					db.setHoraInicial(null);
					db.setHoraFinal(null);
					db.setStatus(1);
					db.setImageStatus("/images/closed.gif");
					db.setUser(user);
					db.setValorInicial(null);
					db.setBomba(b);
					b.setDiarioBomba(db);
				}
			}
			this.postos.add(p);
		}
	}

	/**
	 * Caso a bomba não tenha nenhuma diária, uma diária será criada
	 * Caso Contrário a bomba tenha diária, a diário corrente será mostrada
	 * @param list
	 */
	public void setarValores(List<Posto> list) {
		for (Posto posto : list) {
			for (Bomba bomba : posto.getListaBomba()) {

				if(bomba.getDiarioBombas() == null || bomba.getDiarioBombas().isEmpty()){
					DiarioBomba d = new DiarioBomba();
					d.setDataCadastro(new Date());
					d.setHoraInicial(null);
					d.setHoraFinal(null);
					d.setStatus(1);
					d.setImageStatus("/images/closed.gif");
					d.setUser(user);
					d.setValorInicial(null);
					d.setBomba(bomba);
					bomba.setDiarioBomba(d);
				} else {
					boolean diariaHoje = false;
					for (DiarioBomba diarioBomba : bomba.getDiarioBombas()) {
						if(DateUtil.compareDate(DateUtil.getDateStartDay(new Date()), DateUtil.getDateStartDay(diarioBomba.getDataCadastro()))){
							diariaHoje = true;
							if(diarioBomba.getStatus() == 1){
								diarioBomba.setImageStatus("/images/tick.png");
							} else if(diarioBomba.getStatus() == 0){
								diarioBomba.setImageStatus("/images/open_icon.png");
							}
							bomba.setDiarioBomba(diarioBomba);
							break;
						}
					}
					if(!diariaHoje){
						DiarioBomba d = new DiarioBomba();
						d.setDataCadastro(new Date());
						d.setHoraInicial(null);
						d.setHoraFinal(null);
						d.setStatus(1);
						d.setImageStatus("/images/closed.gif");
						d.setUser(user);
						d.setBomba(bomba);
						bomba.setDiarioBomba(d);
					}	
				}
			}
		}
	}

	private Float recuperarValorFinal(DiarioBomba diarioBomba, Bomba bomba) {
		DiarioBomba diarioBombaAux = this.service.findLastDiariaByDate(bomba);
		if(diarioBomba.getDataCadastro().equals(diarioBombaAux.getDataCadastro())){
			return diarioBomba.getValorFinal();
		} else {
			return diarioBombaAux.getValorFinal();
		}
	}

	public void setBombaSelecionada(Bomba bombaSelecionada) {
		this.bombaSelecionada = bombaSelecionada;
	}

	public Bomba getBombaSelecionada() {
		return bombaSelecionada;
	}

	public Boolean getBombaAberta() {
		return bombaAberta;
	}

	public void setBombaAberta(Boolean bombaAberta) {
		this.bombaAberta = bombaAberta;
	}

	public Boolean getBombaFechada() {
		return bombaFechada;
	}

	public void setBombaFechada(Boolean bombaFechada) {
		this.bombaFechada = bombaFechada;
	}

	public boolean isStart(){
		return start;
	}

	public void setStart(Boolean start) {
		this.start = start;
	}

	public void setMostrarZeraBomba(Boolean mostrarZeraBomba) {
		this.mostrarZeraBomba = mostrarZeraBomba;
	}

	public Boolean getMostrarZeraBomba() {
		return mostrarZeraBomba;
	}

	public List<Posto> getPostos() {
		return postos;
	}

	public void setPostos(List<Posto> postos) {
		this.postos = postos;
	}
}
