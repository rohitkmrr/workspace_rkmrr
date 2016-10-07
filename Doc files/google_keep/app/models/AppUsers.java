package models;

import java.awt.List;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.UniqueConstraint;

import play.data.validation.Constraints.Required;


@Entity
public class AppUsers extends com.avaje.ebean.Model{
	@Id
	 @Required
	private String username;
	
	@Column(nullable=false)
	private String password;
	@Column(unique=true)
	private String token;
	
	
	@Column(unique=true,nullable=false)
	private String email;
	public AppUsers(String username){
		this.username=username;
	}
	public AppUsers(String username,String password,String email,String token){
		this.email=email;
		this.username=username;
		this.password=password;
		this.token=token;
		
	}
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password=password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	Finder<Long,AppUsers> finder=new Finder<Long, AppUsers>(AppUsers.class);
}