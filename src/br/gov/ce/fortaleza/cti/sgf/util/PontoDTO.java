package br.gov.ce.fortaleza.cti.sgf.util;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class PontoDTO  implements Serializable {

	private static final long serialVersionUID = 4561331928131040072L;

	private int index;
	private int id;
	private int tipo;
	private String nome;
	private String placa;
	private String marca;
	private String modelo;
	private Integer ano;
	private String cor;
	private int ordem;
	private float velocidadeMaxima;
	private float lat;
	private float lng;
	private float velocidade;
	private float temperatura;
	private float odometro;
	private float horimetro;
	private boolean ignicao;
	private boolean selecionado;
	private Date dataTransmissao;
	private String pontoMaisProximo;
	private float distPontoMaisProximo;
	private float kmAtual;
	private float nivelCombustivel;
	private float capacidadeTanque;
	private List<PontoDTO> rastro;

	public float getKmAtual() {
		return kmAtual;
	}

	public void setKmAtual(float kmAtual) {
		this.kmAtual = kmAtual;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTipo() {
		return tipo;
	}

	public void setTipo(int tipo) {
		this.tipo = tipo;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getPlaca() {
		return placa;
	}

	public void setPlaca(String placa) {
		this.placa = placa;
	}

	public String getMarca() {
		return marca;
	}

	public void setMarca(String marca) {
		this.marca = marca;
	}

	public String getModelo() {
		return modelo;
	}

	public void setModelo(String modelo) {
		this.modelo = modelo;
	}

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	public String getCor() {
		return cor;
	}

	public int getOrdem() {
		return ordem;
	}

	public void setOrdem(int ordem) {
		this.ordem = ordem;
	}

	public void setCor(String cor) {
		this.cor = cor;
	}

	public float getVelocidadeMaxima() {
		return velocidadeMaxima;
	}

	public void setVelocidadeMaxima(float velocidadeMaxima) {
		this.velocidadeMaxima = velocidadeMaxima;
	}

	public float getLat() {
		return lat;
	}

	public void setLat(float lat) {
		this.lat = lat;
	}

	public float getLng() {
		return lng;
	}

	public void setLng(float lng) {
		this.lng = lng;
	}

	public float getVelocidade() {
		return velocidade;
	}

	public void setVelocidade(float velocidade) {
		this.velocidade = velocidade;
	}

	public float getTemperatura() {
		return temperatura;
	}

	public void setTemperatura(float temperatura) {
		this.temperatura = temperatura;
	}

	public float getOdometro() {
		return odometro;
	}

	public void setOdometro(float odometro) {
		this.odometro = odometro;
	}

	public float getHorimetro() {
		return horimetro;
	}

	public void setHorimetro(float horimetro) {
		this.horimetro = horimetro;
	}

	public boolean getIgnicao() {
		return ignicao;
	}

	public void setIgnicao(boolean ignicao) {
		this.ignicao = ignicao;
	}

	public boolean getSelecionado() {
		return selecionado;
	}

	public void setSelecionado(boolean selecionado) {
		this.selecionado = selecionado;
	}

	public Date getDataTransmissao() {
		return dataTransmissao;
	}

	public void setDataTransmissao(Date dataTransmissao) {
		this.dataTransmissao = dataTransmissao;
	}

	public String getPontoMaisProximo() {
		return pontoMaisProximo;
	}

	public void setPontoMaisProximo(String pontoMaisProximo) {
		this.pontoMaisProximo = pontoMaisProximo;
	}

	public float getDistPontoMaisProximo() {
		return distPontoMaisProximo;
	}

	public void setDistPontoMaisProximo(float distPontoMaisProximo) {
		this.distPontoMaisProximo = distPontoMaisProximo;
	}

	public List<PontoDTO> getRastro() {
		return rastro;
	}

	public void setRastro(List<PontoDTO> rastro) {
		this.rastro = rastro;
	}

	public String getDataTransmissaoFormatada() {
		return DateUtil.parseAsString("HH:mm:ss dd/MM/yyyy", dataTransmissao);
	}

	public String getDataTransmissaoDMY() {
		return DateUtil.parseAsString("dd/MM/yyyy", dataTransmissao);
	}

	public boolean isExcessoVelocidade() {
		return velocidade > velocidadeMaxima;
	}

	public boolean isNaoExcessoVelocidade() {
		return !isExcessoVelocidade();
	}

	public String getDataString() {
		return id + "|" + nome + "|" + tipo + "|" + lat + "|" + lng + "|" + pontoMaisProximo + "|" + distPontoMaisProximo + "|" + velocidade + "|" + DateUtil.parseAsString("dd/MM/yyyy HH:mm:ss", dataTransmissao) + "|" + placa +" |||";
	}

	public String getRastroString() {
		String result = "";
		for (PontoDTO ponto : rastro) {
			result += ponto.getLat() + " " + ponto.getLng() + "|";
		}
		return result.length() > 0 ? result.substring(0, result.length() - 1) : "";
	}

	public float getNivelCombustivel() {
		return nivelCombustivel;
	}

	public void setNivelCombustivel(float nivelCombustivel) {
		this.nivelCombustivel = nivelCombustivel;
	}

	public float getCapacidadeTanque() {
		return capacidadeTanque;
	}

	public void setCapacidadeTanque(float capacidadeTanque) {
		this.capacidadeTanque = capacidadeTanque;
	}
}