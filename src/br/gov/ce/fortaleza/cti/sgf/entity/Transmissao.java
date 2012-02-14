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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="TB_TRANSMISSAO", schema = "TESTE")
@NamedQueries({
    @NamedQuery(name = "Transmissao.findByVeiculo", query = "SELECT p FROM Transmissao p WHERE p.veiculoId = ?  AND p.dataTransmissao between ? AND ? ORDER BY p.dataTransmissao desc"),
	@NamedQuery(name = "Transmissao.findByVeiculoVelocidade",	query = "SELECT p FROM Transmissao p WHERE p.ponto.id != 0 AND p.veiculoId = ?  AND p.dataTransmissao between ? AND ? AND p.velocidade >= ? ORDER BY p.dataTransmissao")
})
public class Transmissao implements Serializable {

	private static final long serialVersionUID = 9048294011474844463L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="CODTRANSMISSAO", nullable = false)
	private Long id;
	
//	@Type(type="org.hibernatespatial.GeometryUserType")	
//	@Column(name = "GEOMPONTO", nullable = false)
//	private Geometry geometry;
	
	@Column(name = "X", nullable = false)
	private Double x;
	
	@Column(name = "Y", nullable = false)
	private Double y;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DAT_TRANSMISSAO", nullable = false)
	private Date dataTransmissao;

	@Column(name="DISTANCIA_PONTOPROX")
	private Float distancia;

	@Column(name="VELOCIDADE")
	private Float velocidade;

	@Column(name="TEMPERATURA")
	private Float temperatura;

	@Column(name="ODOMETRO")
	private Float odometro;

	@Column(name="CODVEICULO")
	private Integer veiculoId;

	@ManyToOne
	@JoinColumn(name="CODPONTO")
	private Ponto ponto;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDataTransmissao() {
		return dataTransmissao;
	}

	public void setDataTransmissao(Date dataTransmissao) {
		this.dataTransmissao = dataTransmissao;
	}

	public Float getDistancia() {
		return distancia;
	}

	public void setDistancia(Float distancia) {
		this.distancia = distancia;
	}

	public Float getVelocidade() {
		return velocidade;
	}

	public void setVelocidade(Float velocidade) {
		this.velocidade = velocidade;
	}

	public Float getTemperatura() {
		return temperatura;
	}

	public void setTemperatura(Float temperatura) {
		this.temperatura = temperatura;
	}

	public Float getOdometro() {
		return odometro;
	}

	public void setOdometro(Float odometro) {
		this.odometro = odometro;
	}

	public Integer getVeiculoId() {
		return veiculoId;
	}

	public void setVeiculoId(Integer veiculoId) {
		this.veiculoId = veiculoId;
	}

	public Ponto getPonto() {
		return ponto;
	}

	public void setPonto(Ponto ponto) {
		this.ponto = ponto;
	}
	
	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Transmissao other = (Transmissao) obj;
		return ((id == null && other.id == null) || (id != null && id.equals(other.id)));
	}
}
