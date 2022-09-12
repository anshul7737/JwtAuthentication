package com.springboot.jwtaunthetication.service;


import com.springboot.jwtaunthetication.entity.User;
import com.springboot.jwtaunthetication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JwtUserDetailsService implements UserDetailsService {
	@Autowired
	private UserRepository userRepository;

	/**
	 * load user by user name and return user details object if user is found else
	 * throw exception
	 */
	@Override
	public UserDetails loadUserByUsername(String email) {
		Optional<User> user = userRepository.findByEmail(email);
		if (!user.isPresent()) {
			throw new UsernameNotFoundException("User not found with email: " + email);
		}
		return new org.springframework.security.core.userdetails.User(user.get().getUsername(), user.get().getPassword(),
				getAuthorities(user.get()));
	}

	/**
	 * get user roles
	 * 
	 * @param user - logged in user object
	 * @return - return list of granted authorities
	 */
	public List<GrantedAuthority> getAuthorities(User user) {
		
		return user.getRoles().stream()
	                .map(authority -> new SimpleGrantedAuthority(authority.getName().toString()))
	                .collect(Collectors.toList());
		   
	}
}