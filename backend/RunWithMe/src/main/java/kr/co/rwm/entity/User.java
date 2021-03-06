package kr.co.rwm.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Integer userId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "gugunId", nullable = true)
	private Gugun gugunId;

	@Column(name = "user_email", nullable = false)
	private String userEmail;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(name = "user_pw", nullable = false)
	private String userPw;

	@Column(name = "user_name", nullable = false)
	private String userName;

	@Column(name = "profile")
	private String profile;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(name = "auth", columnDefinition = "boolean default false")
	private boolean emailAuth;
	
	@Column(name = "gender") // 1: 남자 2: 여자
	private Integer gender;
	
	@Column(name = "mileage", columnDefinition = "Integer default 0")
	private Integer mileage;
	
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@OneToOne(fetch = FetchType.LAZY, mappedBy="userId", cascade = CascadeType.ALL, orphanRemoval = true)
	private Ranks rank;
	
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@OneToOne(mappedBy="userId", orphanRemoval = true)
	private RunningUser runningUser;
	
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "user_id")
	private List<Record> record;
	
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "user_id")
	private List<Running> running;
	
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "user_id")
	private List<ChallengeUser> challengeUser;
	
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "user_userId")
	private List<Friend> friends;
	
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String changePw;

	public String getChangePw() {
		return this.changePw;
	}

	public void setUserPw(String userPw) {
		this.userPw = userPw;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@Builder.Default
	private List<String> roles = new ArrayList<>();

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
	}

	@Override
	public String getUsername() {
		return this.userName;
	}

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Override
	public String getPassword() {
		return this.userPw;
	}

	public boolean getAuth() {
		return this.emailAuth;
	}

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Override
	public boolean isEnabled() {
		return true;
	}

}
