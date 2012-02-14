package br.gov.ce.fortaleza.cti.sgf.bean;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

import br.gov.ce.fortaleza.cti.sgf.entity.LogUsuario;
import br.gov.ce.fortaleza.cti.sgf.entity.Permissao;
import br.gov.ce.fortaleza.cti.sgf.entity.Role;
import br.gov.ce.fortaleza.cti.sgf.entity.User;
import br.gov.ce.fortaleza.cti.sgf.service.LogUsuarioService;
import br.gov.ce.fortaleza.cti.sgf.service.UsuarioService;
import br.gov.ce.fortaleza.cti.sgf.util.DateUtil;
import br.gov.ce.fortaleza.cti.sgf.util.SgfUtil;

@Scope("session")
@Component("controlBean")
public class ControlStateBean extends BaseStateBean implements Serializable {


	private static final long serialVersionUID = 1L;
	private static final Log logger = LogFactory.getLog(ControlStateBean.class);

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private LogUsuarioService logService;

	public String userMail;
	private User usuario;
	private Role grupo;
	private Boolean validaUsuario = true;
	private Set<String> permissoes;

	@PostConstruct
	public String init() throws ServletException, IOException {
		if (!SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("roleAnonymous")) {
			this.usuario = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			SgfUtil.getSession().setAttribute(SgfUtil.USUARIO_LOGADO, this.usuario);
			SgfUtil.getSession().setAttribute(SgfUtil.CONECTED_IP, SgfUtil.getRequest().getRemoteAddr());
			createLogUsuario(this.usuario, SgfUtil.getSession());
			this.validaUsuario = this.usuario.getLogado();
			this.permissoes = new HashSet<String>();
			this.grupo = this.usuario.getRole();
			for (Permissao permissao : this.grupo.getPermissoes()) {
				this.permissoes.add(permissao.getNome());
			}
			this.usuario.setPermissoes(this.permissoes);
		}
		return SUCCESS;
	}

	public boolean hasPermission(String... roles) {
		this.permissoes = this.usuario.getPermissoes();
		boolean permit = false;
		if (roles != null && roles.length > 0) {
			for (String s : roles) {
				if (this.permissoes.contains(s)) {
					permit = true;
					break;
				}
			}
		}
		return permit;
	}

	public String voltar() {
		if (SgfUtil.getSession() != null) {
			SgfUtil.getSession().invalidate();
		}
		return SUCCESS;
	}

	public boolean isLoginErro() {
		updateLogUsuario(this.usuario, SgfUtil.getSession());
		return SgfUtil.getRequest().getParameter("login_error") != null;
	}

	public String logout() {
		updateLogUsuario(this.usuario, SgfUtil.getSession());
		SgfUtil.getSession().invalidate();
		return SUCCESS;
	}

	public void createLogUsuario(User usuario, HttpSession request) {
		try {
			logService = getLogUsuarioService(LogUsuarioService.class, request);
			usuarioService = getUsuarioService(UsuarioService.class, request);
			if(usuario.getLogado().equals(false) || usuario.getLogado().equals(null)){
				LogUsuario log = new LogUsuario();
				log.setUsuario(usuario);
				log.setDataLogin(DateUtil.getDateNow());
				log.setIpcon(SgfUtil.getRequest().getRemoteAddr());
				logService.save(log);
				usuario.setLogado(true);
				usuarioService.update(usuario);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateLogUsuario(User usuario, HttpSession request) {
		try {
			logService = getLogUsuarioService(LogUsuarioService.class, request);
			usuarioService = getUsuarioService(UsuarioService.class, request);
			if(usuario.getLogado()){
				LogUsuario log = logService.findUltimoLogin(usuario.getCodPessoaUsuario());
				log.setUsuario(usuario);
				log.setDataLogout(DateUtil.getDateNow());
				logService.update(log);
				usuario.setLogado(false);
				usuarioService.update(usuario);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	protected static <E> E getUsuarioService(Class<E> clazz, HttpSession session) {
		ApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(session.getServletContext());
		return (E) wac.getBean("usuarioService");
	}

	@SuppressWarnings("unchecked")
	protected static <E> E getLogUsuarioService(Class<E> clazz, HttpSession session) {
		ApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(session.getServletContext());
		return (E) wac.getBean("logUsuarioService");
	}

	public boolean isAdministrador() {
		return SgfUtil.isAdministrador(this.usuario);
	}

	public boolean isCoordenador() {
		return SgfUtil.isCoordenador(this.usuario);
	}

	public boolean isChefeTransporte() {
		return SgfUtil.isChefeTransporte(this.usuario);
	}

	public boolean isChefeSetor() {
		return SgfUtil.isChefeSetor(this.usuario);
	}

	public boolean isOperador() {
		return SgfUtil.isOperador(this.usuario);
	}

	public boolean isUser() {
		return SgfUtil.isUser(this.usuario);
	}

	public boolean isPermissaoBean(){
		return PermissaoBean.class.getSimpleName().equals(getCurrentBean());
	}

	public boolean isMarcaBean(){
		return MarcaBean.class.getSimpleName().equals(getCurrentBean());
	}

	public boolean isModeloBean(){
		return ModeloBean.class.getSimpleName().equals(getCurrentBean());
	}

	public boolean isEspecieBean(){
		return EspecieBean.class.getSimpleName().equals(getCurrentBean());
	}

	public boolean isInfracaoBean(){
		return InfracaoBean.class.getSimpleName().equals(getCurrentBean());
	}

	public boolean isMultaBean(){
		return MultaBean.class.getSimpleName().equals(getCurrentBean());
	}

	public boolean isNumeroMotorBean(){
		return NumeroMotorBean.class.getSimpleName().equals(getCurrentBean());
	}

	public boolean isVeiculoBean(){
		return VeiculoBean.class.getSimpleName().equals(getCurrentBean());
	}

	public boolean isPessoaBean(){
		return PessoaBean.class.getSimpleName().equals(getCurrentBean());
	}

	public boolean isUsuarioBean(){
		return UsuarioBean.class.getSimpleName().equals(getCurrentBean());
	}

	public boolean isRoleBean(){
		return RoleBean.class.getSimpleName().equals(getCurrentBean());
	}

	public boolean isCotaBean(){
		return CotaBean.class.getSimpleName().equals(getCurrentBean());
	}	

	public boolean isUABean(){
		return UABean.class.getSimpleName().equals(getCurrentBean());
	}

	public boolean isMotoristaBean(){
		return MotoristaBean.class.getSimpleName().equals(getCurrentBean());
	}

	public boolean isBombaBean(){
		return BombaBean.class.getSimpleName().equals(getCurrentBean());
	}

	public boolean isOficinaBean(){
		return OficinaBean.class.getSimpleName().equals(getCurrentBean());
	}

	public boolean isDiarioBombaBean(){
		return DiarioBombaBean.class.getSimpleName().equals(getCurrentBean());
	}

	public boolean isSolicitacaoVeiculoBean(){
		return SolicitacaoVeiculoBean.class.getSimpleName().equals(getCurrentBean());
	}	


	public boolean isRequisicaoManutencaoBean(){
		return RequisicaoManutencaoBean.class.getSimpleName().equals(getCurrentBean());
	}

	public boolean isSolicitacaoLubrificanteBean(){
		return SolicitacaoLubrificanteBean.class.getSimpleName().equals(getCurrentBean());
	}	

	public boolean isTipoServicoBean(){
		return TipoServicoBean.class.getSimpleName().equals(getCurrentBean());
	}

	public boolean isAbastecimentoBean() {
		return AbastecimentoBean.class.getSimpleName().equals(getCurrentBean());
	}

	public boolean isSincronizaBean() {
		return SincronizaBean.class.getSimpleName().equals(getCurrentBean());
	}
	
	public boolean isPontoBean() {
		return PontoBean.class.getSimpleName().equals(getCurrentBean());
	}
	
	public boolean isMonitoramentoBean(){
		return MonitoramentoBean.class.getSimpleName().equals(getCurrentBean());
	}

	public boolean isRelatorioBean(){
		return RelatorioBean.class.getSimpleName().equals(getCurrentBean());
	}

	public boolean isRelatorioPontuacaoMotorista(){
		return true;
	}

	public boolean isRelatorioVeiculoMulta(){
		return true;
	}

	public boolean isRelatorioConsumoCombustivel(){
		return true;
	}

	public boolean isRelatorioDiarioBomba(){
		return true;
	}

	public boolean isRelatorioSolicitacaoVeiculo(){
		return true;
	}

	public String enviarEmail(){
		if ((userMail != null)&& (!"".equals(userMail.trim()))) {
			List<User> list = usuarioService.retriveByMail(userMail);
			if ((list != null)&& (list.size() > 0)) {
				for (User usuario: list) {
					try {
						String str = SgfUtil.sendMailToUser(userMail, usuario);
						if (str.equalsIgnoreCase(SUCCESS)) {
							SgfUtil.getRequest().setAttribute("sendmail_msg", "A Senha foi enviada p/ o e-mail informado.");
							return SUCCESS;
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			} else {
				SgfUtil.getRequest().setAttribute("sendmail_err", "Não foi encontrado usuário p/ o e-mail informado!");
				return FAIL;
			}
		} else {
			SgfUtil.getRequest().setAttribute("sendmail_wrn", "Por favor informe o e-mail!");
			return FAIL;
		}
		return SUCCESS;
	}

	public String getUserMail() {
		return userMail;
	}

	public void setUserMail(String userMail) {
		this.userMail = userMail;
	}

	public User getUsuario() {
		return usuario;
	}

	public void setUsuario(User usuario) {
		this.usuario = usuario;
	}

	public Role getGrupo() {
		return grupo;
	}

	public void setGrupo(Role grupo) {
		this.grupo = grupo;
	}

	public Set<String> getPermissoes() {
		return permissoes;
	}

	public void setPermissoes(Set<String> permissoes) {
		this.permissoes = permissoes;
	}

	public Boolean getValidaUsuario() {
		return validaUsuario;
	}

	public void setValidaUsuario(Boolean validaUsuario) {
		this.validaUsuario = validaUsuario;
	}
}