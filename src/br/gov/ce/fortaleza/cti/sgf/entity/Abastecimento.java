package br.gov.ce.fortaleza.cti.sgf.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import br.gov.ce.fortaleza.cti.sgf.util.StatusAbastecimento;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "TB_ABASTECIMENTO", schema = "TESTE")
@NamedQueries( {
		/*
		 * @NamedQuery(name = "Abastecimento.findCota", query =
		 * "select sum(o.atendimento.quantidadeAbastecida) from " +
		 * "Abastecimento o where o.veiculo.id = :id and (o.atendimento.data >= :inicio and o.atendimento.data <= :fim)"
		 * ),
		 */
		@NamedQuery(name = "Abastecimento.findLast", query = "select a from Abastecimento a where a.id = (select max(o.id) "
				+ "from Abastecimento o) and a.veiculo.id = ?"),
		@NamedQuery(name = "Abastecimento.findByPosto", query = "select a from Abastecimento a where a.posto.codPosto = ? and a.status = ?"),
		@NamedQuery(name = "Abastecimento.findByPeriodoAndPosto", query = "select a from Abastecimento a where a.posto.codPosto = ? and a.dataAutorizacao BETWEEN ? and ? and a.status = ?") })
public class Abastecimento implements Serializable {

	/**
	 * SerialVersionUID.
	 */
	private static final long serialVersionUID = -5061858557705018194L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_abastecimento")
	@SequenceGenerator(name = "seq_abastecimento", sequenceName = "sgf.abastecimento_seq", allocationSize = 1)
	@Column(name = "CODSOLABASTECIMENTO", nullable = false)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "CODVEICULO", nullable = false)
	private Veiculo veiculo;

	@ManyToOne
	@JoinColumn(name = "CODUSUARIOAUT")
	private User autorizador;

	@ManyToOne
	@JoinColumn(name = "CODMOTORISTA", nullable = false)
	private Motorista motorista;

	@ManyToOne
	@JoinColumn(name = "CODPOSTO", nullable = false)
	private Posto posto;

	@ManyToOne
	@JoinColumn(name = "CODTIPOSERVICO", nullable = false)
	private TipoServico tipoServico;

	@ManyToOne
	@JoinColumn(name = "CODTIPOCOMBUSTIVEL", nullable = false)
	private TipoCombustivel combustivel;

	// @OneToOne(mappedBy = "abastecimento")
	// @JoinColumn(name = "CODSOLABASTECIMENTO", table =
	// "TB_ATENDABASTECIMENTO")
	// private AtendimentoAbastecimento atendimento;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DTAUTORIZACAO")
	private Date dataAutorizacao;

	@Column(name = "KM_ATENDIMENTO")
	private Long quilometragem;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "STATUS")
	private StatusAbastecimento status = StatusAbastecimento.AUTORIZADO;

	@Column(name = "JUSTIFICATIVAATEND")
	private String justificativa;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Veiculo getVeiculo() {
		return veiculo;
	}

	public void setVeiculo(Veiculo veiculo) {
		this.veiculo = veiculo;
	}

	public User getAutorizador() {
		return autorizador;
	}

	public void setAutorizador(User autorizador) {
		this.autorizador = autorizador;
	}

	public Motorista getMotorista() {
		return motorista;
	}

	public void setMotorista(Motorista motorista) {
		this.motorista = motorista;
	}

	public Posto getPosto() {
		return posto;
	}

	public void setPosto(Posto posto) {
		this.posto = posto;
	}

	public TipoServico getTipoServico() {
		return tipoServico;
	}

	public void setTipoServico(TipoServico tipoServico) {
		this.tipoServico = tipoServico;
	}

	public Date getDataAutorizacao() {
		return dataAutorizacao != null ? (Date) dataAutorizacao.clone() : null;
	}

	public void setDataAutorizacao(Date dataAutorizacao) {
		this.dataAutorizacao = dataAutorizacao;
	}

	public Long getQuilometragem() {
		return quilometragem;
	}

	public void setQuilometragem(Long quilometragem) {
		this.quilometragem = quilometragem;
	}

	public StatusAbastecimento getStatus() {
		return status;
	}

	public void setStatus(StatusAbastecimento status) {
		this.status = status;
	}

	public String getJustificativa() {
		return justificativa;
	}

	public void setJustificativa(String justificativa) {
		this.justificativa = justificativa;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((motorista == null) ? 0 : motorista.hashCode());
		result = prime * result + ((posto == null) ? 0 : posto.hashCode());
		result = prime * result
				+ ((quilometragem == null) ? 0 : quilometragem.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result
				+ ((tipoServico == null) ? 0 : tipoServico.hashCode());
		result = prime * result + ((veiculo == null) ? 0 : veiculo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}

		if (getClass() != obj.getClass())
			return false;
		Abastecimento other = (Abastecimento) obj;

		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		if (getMotorista() == null) {
			if (other.getMotorista() != null)
				return false;
		} else if (!getMotorista().equals(other.getMotorista()))
			return false;
		if (getPosto() == null) {
			if (other.getPosto() != null)
				return false;
		} else if (!getPosto().equals(other.getPosto()))
			return false;
		if (getQuilometragem() == null) {
			if (other.getQuilometragem() != null)
				return false;
		} else if (!getQuilometragem().equals(other.getQuilometragem()))
			return false;
		if (getStatus() == null) {
			if (other.getStatus() != null)
				return false;
		} else if (!getStatus().equals(other.getStatus()))
			return false;
		if (getTipoServico() == null) {
			if (other.getTipoServico() != null)
				return false;
		} else if (!getTipoServico().equals(other.getTipoServico()))
			return false;
		if (getVeiculo() == null) {
			if (other.getVeiculo() != null)
				return false;
		} else if (!getVeiculo().equals(other.getVeiculo()))
			return false;
		return true;
	}

	// public AtendimentoAbastecimento getAtendimento() {
	// return atendimento;
	// }
	//
	// public void setAtendimento(AtendimentoAbastecimento atendimento) {
	// this.atendimento = atendimento;
	// }

	public void setCombustivel(TipoCombustivel combustivel) {
		this.combustivel = combustivel;
	}

	public TipoCombustivel getCombustivel() {
		return combustivel;
	}

}
