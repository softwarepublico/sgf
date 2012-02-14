/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

/**
 * @author Deivid
 * @since 11/12/09
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "tb_diariobomba", schema = "TESTE")
@NamedQueries({
	@NamedQuery(name = "DiarioBomba.findLastDiariaByDate", query = "select max(db.horaInicial) from DiarioBomba db where db.bomba.id = ?"),
	@NamedQuery(name = "DiarioBomba.findDiariasByBomba",  query = "select db from DiarioBomba db where db.bomba = ?"),
	@NamedQuery(name = "DiarioBomba.findCurrentDiaryByBomba",  query = "select db from DiarioBomba db where db.bomba.id = ? and day(db.horaInicial) = ?")
})
			
public class DiarioBomba implements Serializable{

	private static final long serialVersionUID = -867654219155183026L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_diariobomba")
	@SequenceGenerator(name = "seq_diariobomba", sequenceName = "sgf.coddiariobomba_seq", allocationSize = 1)
	@Column(name="coddiariobomba", nullable = false)
	private Integer id;
	
	@Column(name = "data")
	private Date dataCadastro;
	
	@Column(name = "vlinicia")
	private Float valorInicial;
	
	@Column(name = "vlfinal")
	private Float valorFinal;
	
	@Column(name = "status")
	private Integer status;
	
	@Column(name = "hora_ini")
	private Date horaInicial;
	
	@Column(name = "hora_fim")
	private Date horaFinal;
	
	@Type(type="boolean")
	@Column(name="zerada")
	private Boolean zerada;
	
	@ManyToOne
	@JoinColumn(name = "codusuario")
	private User user;
	
	@ManyToOne
	@JoinColumn(name = "codbomba")
	private Bomba bomba;
	
	@Transient
	private String imageStatus;
	
	@Transient
	private Float litrosAbastecidos;
	
	public int hashCode() {
		int result = 1;
		result = 31 * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public boolean equals(Object obj) {

		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;

		DiarioBomba other = (DiarioBomba) obj;

		return ((id == null && other.id == null) || (id != null && id.equals(other.id))) &&
		((dataCadastro == null && other.dataCadastro == null) || (dataCadastro != null && dataCadastro.equals(other.dataCadastro))) &&
		((valorInicial == null && other.valorInicial == null) || (valorInicial != null && valorInicial.equals(other.valorInicial))) &&
		((valorFinal == null && other.valorFinal == null) || (valorFinal != null && valorFinal.equals(other.valorFinal))) &&
		((status == null && other.status == null) || (status != null && status.equals(other.status))) &&
		((horaInicial == null && other.horaInicial == null) || (horaInicial != null && horaInicial.equals(other.horaInicial))) &&
		((horaFinal == null && other.horaFinal == null) || (horaFinal != null && horaFinal.equals(other.horaFinal)));
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(Date dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	public Float getValorInicial() {
		return valorInicial;
	}

	public void setValorInicial(Float valorInicial) {
		this.valorInicial = valorInicial;
	}

	public Float getValorFinal() {
		return valorFinal;
	}

	public void setValorFinal(Float valorFinal) {
		if(valorFinal != null){
			if(valorFinal.equals(new Float(0))){
				this.valorFinal = null;
			}else{
				this.valorFinal = valorFinal;
			}
		}else{
			this.valorFinal = valorFinal;
		}
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getHoraInicial() {
		return horaInicial;
	}

	public void setHoraInicial(Date horaInicial) {
		this.horaInicial = horaInicial;
	}

	public Date getHoraFinal() {
		return horaFinal;
	}

	public void setHoraFinal(Date horaFinal) {
		this.horaFinal = horaFinal;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Bomba getBomba() {
		return bomba;
	}

	public void setBomba(Bomba bomba) {
		this.bomba = bomba;
	}

	public void setImageStatus(String imageStatus) {
		this.imageStatus = imageStatus;
	}

	public String getImageStatus() {
		return imageStatus;
	}

	public void setLitrosAbastecidos(Float litrosAbastecidos) {
		this.litrosAbastecidos = litrosAbastecidos;
	}

	public Float getLitrosAbastecidos() {
		return litrosAbastecidos;
	}

	public void setZerada(Boolean zerada) {
		this.zerada = zerada;
	}

	public Boolean isZerada() {
		return zerada;
	}
}
