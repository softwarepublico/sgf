/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.service;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.dao.DataAccessException;
import org.springframework.security.concurrent.SessionAlreadyUsedException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import br.gov.ce.fortaleza.cti.sgf.entity.User;

/**
 * @author Deivid
 * @since 03/11/2009	
 */

@Component()
public class CustomUserDetailsService extends BaseService<Integer, User> implements UserDetailsService{

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException,SessionAlreadyUsedException {
		
		User user = null;
		if(username == null || username.equals("")){
			return user;
		} else {
			try {
				Query query = entityManager.createNamedQuery("User.findByUserName");
				query.setParameter(1, username);
				user = (User) query.getSingleResult();
				if(user.getStatus().equals("false")){
					user = null;
					return null;
				}
				
			}catch (NoResultException e) {
				user = null;
			} catch (UsernameNotFoundException e) {
				user = null;
			}catch (DataAccessException e) {
				user = null;
			}catch(SessionAlreadyUsedException e){
				user = null;
			}
		}
		return user;  
	}
}
