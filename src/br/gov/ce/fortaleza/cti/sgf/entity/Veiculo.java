package br.gov.ce.fortaleza.cti.sgf.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SecondaryTable;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SecondaryTable(name = "TB_ULTIMATRANSMISSAO", schema = "TESTE")
@Table(name = "TB_CADVEICULO", schema = "TESTE")
@NamedQueries( {
	@NamedQuery(name = "Veiculo.findVeiculosSemCota", query = "select v from Veiculo as v where v not in(select c.veiculo from Cota c)"),
	@NamedQuery(name = "Veiculo.findVeiculosSemCotaByPlaca", query = "select v from Veiculo as v where v not in(select c.veiculo from Cota c) and v.placa LIKE ?"),
	@NamedQuery(name = "Veiculo.findVeiculosComCota", query = "select v from Veiculo as v inner join v.cota as cota"),
	@NamedQuery(name = "Veiculo.findByPCR", query = "SELECT v FROM Veiculo AS v WHERE (v.placa LIKE ?) OR  (v.chassi LIKE ?) OR (v.renavam LIKE ?)"),
	@NamedQuery(name = "Veiculo.findByUGPCR", query = "SELECT v FROM Veiculo AS v WHERE (v.ua.ug.id LIKE ?) AND ((v.placa LIKE ?) OR  (v.chassi LIKE ?) OR (v.renavam LIKE ?))"),
	@NamedQuery(name = "Veiculo.findByUG", query = "SELECT v FROM Veiculo AS v WHERE (v.ua.ug.id = ?)"),
	@NamedQuery(name = "Veiculo.findByPlaca", query = "SELECT v FROM Veiculo AS v WHERE (v.placa LIKE ?)") })
	public class Veiculo implements Serializable {

	private static final long serialVersionUID = 1031161986293985845L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_veiculo")
	@SequenceGenerator(name = "seq_veiculo", sequenceName = "sgf.codveiculo_seq", allocationSize = 1)
	@Column(name = "CODVEICULO", nullable = false)
	private Integer id;

	@Column(name = "PLACA", nullable = false, length = 7)
	private String placa;

	@Column(name = "VALORAQUISICAO")
	private Float valorAquisicao;

	@Column(name = "NUMTOMBAMENTO")
	private Integer numeroTombamento;

	@Column(name = "COR", length = 20)
	private String cor;

	@Column(name = "CHASSI", length = 30)
	private String chassi;

	@Column(name = "RENAVAM", length = 30)
	private String renavam;

	@Column(name = "SERIE", length = 30)
	private String serie;

	@Column(name = "ANOFAB", length = 4)
	private Integer anoFabricacao;

	@Column(name = "ANOMODELO", nullable = true)
	private Integer anoModelo;

	@Column(name = "TEMSEGURO", nullable = true)
	private Integer temSeguro;

	@Column(name = "STATUS", nullable = false)
	private Integer status;

	@Column(name = "COMBUSTIVEL", length = 50)
	private String combustivel;

	@Column(name = "PROPRIEDADE", length = 150)
	private String propriedade;

	@Column(name = "ESTACIONAMENTO")
	private String estacionamento;

	@Column(name = "KMIMPLANTACAO")
	private Integer kilometroImplantacao;

	@Column(name = "KM_ATUAL")
	private Integer kmAtual;

	@Column(name = "COD_PATRIMONIO")
	private String numeroPatrimonio;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DT_CADASTRO")
	private Date dataCadastro;

	@ManyToOne
	@JoinColumn(name = "COD_UA_ASI")
	private UA ua;

	@ManyToOne
	@JoinColumn(name = "CODMODELO")
	private Modelo modelo;

	@ManyToOne
	@JoinColumn(name = "CODESPECIE")
	private Especie especie;

	@OneToOne(mappedBy = "veiculo", fetch=FetchType.LAZY)
	@Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
	private Cota cota;

	@Transient
	private Double cotaDisponivel;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "veiculo", cascade = {CascadeType.PERSIST, CascadeType.MERGE })
	@Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE,	org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
	private Set<PostoServicoVeiculo> postosServicosVeiculos;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "veiculo", cascade = {CascadeType.PERSIST, CascadeType.MERGE })
	@Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE,	org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
	private Set<Abastecimento> abastecimentos;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "veiculo", cascade = {CascadeType.PERSIST, CascadeType.MERGE })
	@Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE,org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
	private Set<SolicitacaoVeiculo> solicitacoesVeiculo;

//	@Column(name="GEOMPONTO", table = "TB_ULTIMATRANSMISSAO")
//	@Type(type="org.hibernatespatial.GeometryUserType")
//	private Point geometry;
	
//	@Convert("geometryConverter")
//	@Column(name = "GEOMPONTO", table = "TB_ULTIMATRANSMISSAO", nullable = false)
//	private org.postgis.Geometry geometry;
	
	@Column(name = "X", table = "TB_ULTIMATRANSMISSAO")
	private Double x;
	
	@Column(name = "Y", table = "TB_ULTIMATRANSMISSAO")
	private Double y;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DAT_TRANSMISSAO", table = "TB_ULTIMATRANSMISSAO")
	private Date dataTransmissao;

	@Column(name="DISTANCIA_PONTOPROX", table = "TB_ULTIMATRANSMISSAO")
	private Float distancia;

	@Column(name="VELOCIDADE", table = "TB_ULTIMATRANSMISSAO")
	private Float velocidade;

	@Column(name="TEMPERATURA", table = "TB_ULTIMATRANSMISSAO")
	private Float temperatura;

	@Column(name="ODOMETRO", table = "TB_ULTIMATRANSMISSAO")
	private Float odometro;

	@ManyToOne
	@JoinColumn(name="CODPONTOPROX", table = "TB_ULTIMATRANSMISSAO")
	private Ponto pontoProximo;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public UA getUa() {
		return ua;
	}

	public void setUa(UA ua) {
		this.ua = ua;
	}

	public String getPlaca() {
		return this.placa;
	}

	public void setPlaca(String placa) {
		this.placa = placa;
	}

	public String getChassi() {
		return this.chassi;
	}

	public void setChassi(String chassi) {
		this.chassi = chassi;
	}

	public String getSerie() {
		return this.serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
	}

	public Integer getAnoFabricacao() {
		return anoFabricacao;
	}

	public void setAnoFabricacao(Integer anoFabricacao) {
		this.anoFabricacao = anoFabricacao;
	}

	public Integer getAnoModelo() {
		return anoModelo;
	}

	public void setAnoModelo(Integer anoModelo) {
		this.anoModelo = anoModelo;
	}

	public Modelo getModelo() {
		return this.modelo;
	}

	public void setModelo(Modelo modelo) {
		this.modelo = modelo;
	}

	public String getRenavam() {
		return this.renavam;
	}

	public void setRenavam(String renavam) {
		this.renavam = renavam;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Especie getEspecie() {
		return this.especie;
	}

	public void setEspecie(Especie especie) {
		this.especie = especie;
	}

	public Float getValorAquisicao() {
		return this.valorAquisicao;
	}

	public void setValorAquisicao(Float valorAquisicao) {
		this.valorAquisicao = valorAquisicao;
	}

	public Integer getNumeroTombamento() {
		return this.numeroTombamento;
	}

	public void setNumeroTombamento(Integer numeroTombamento) {
		this.numeroTombamento = numeroTombamento;
	}

	public String getCor() {
		return this.cor;
	}

	public void setCor(String cor) {
		this.cor = cor;
	}

	public String getEstacionamento() {
		return estacionamento;
	}

	public void setEstacionamento(String estacionamento) {
		this.estacionamento = estacionamento;
	}

	public Integer getTemSeguro() {
		return this.temSeguro;
	}

	public void setTemSeguro(Integer temSeguro) {
		this.temSeguro = temSeguro;
	}

	public String getCombustivel() {
		return this.combustivel;
	}

	public void setCombustivel(String combustivel) {
		this.combustivel = combustivel;
	}

	public String getPropriedade() {
		return this.propriedade;
	}

	public void setPropriedade(String propriedade) {
		this.propriedade = propriedade;
	}

	public Integer getKilometroImplantacao() {
		return kilometroImplantacao;
	}

	public void setKilometroImplantacao(Integer kilometroImplantacao) {
		this.kilometroImplantacao = kilometroImplantacao;
	}

	public Integer getKmAtual() {
		return kmAtual;
	}

	public void setKmAtual(Integer kmAtual) {
		this.kmAtual = kmAtual;
	}

	public String getNumeroPatrimonio() {
		return numeroPatrimonio;
	}

	public void setNumeroPatrimonio(String numeroPatrimonio) {
		this.numeroPatrimonio = numeroPatrimonio;
	}

	public void setPostosServicosVeiculos(
			Set<PostoServicoVeiculo> postosServicosVeiculos) {
		this.postosServicosVeiculos = postosServicosVeiculos;
	}

	public Set<PostoServicoVeiculo> getPostosServicosVeiculos() {
		return postosServicosVeiculos;
	}

	public void setCota(Cota cota) {
		this.cota = cota;
	}

	public Cota getCota() {
		return cota;
	}

	public void setAbastecimentos(Set<Abastecimento> abastecimentos) {
		this.abastecimentos = abastecimentos;
	}

	public Set<Abastecimento> getAbastecimentos() {
		return abastecimentos;
	}

	public Set<SolicitacaoVeiculo> getSolicitacoesVeiculo() {
		return solicitacoesVeiculo;
	}

	public void setSolicitacoesVeiculo(
			Set<SolicitacaoVeiculo> solicitacoesVeiculo) {
		this.solicitacoesVeiculo = solicitacoesVeiculo;
	}

	public Date getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(Date dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	public Double getCotaDisponivel() {
		return cotaDisponivel;
	}

	public void setCotaDisponivel(Double cotaDisponivel) {
		this.cotaDisponivel = cotaDisponivel;
	}

//	public org.postgis.Geometry getGeometry() {
//		return geometry;
//	}
//
//	public void setGeometry(org.postgis.Geometry geometry) {
//		this.geometry = geometry;
//	}
//
	public Float getTemperatura() {
		return temperatura;
	}

	public void setTemperatura(Float temperatura) {
		this.temperatura = temperatura;
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

	public Float getOdometro() {
		return odometro;
	}

	public void setOdometro(Float odometro) {
		this.odometro = odometro;
	}

	public Ponto getPontoProximo() {
		return pontoProximo;
	}

	public void setPontoProximo(Ponto pontoProximo) {
		this.pontoProximo = pontoProximo;
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
		int result = 1;
		result = 31 * result + ((id == null) ? 0 : id.hashCode());
		result = 31 * result + ((placa == null) ? 0 : placa.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Veiculo other = (Veiculo) obj;
		return ((id == null && other.id == null) || (id != null && id.equals(other.id)))
		&& ((placa == null && other.placa == null) || (placa != null && placa.equals(other.placa)))
		&& ((ua == null && other.ua == null) || (ua != null && ua.equals(other.ua)));
	}
}
