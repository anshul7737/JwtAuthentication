/**
 * 
 */
package com.springboot.jwtaunthetication.entity;

import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

/**
 * @author Naveen
 *
 */

@Entity
@Table(name = "role")
public class Role {

	public enum RoleName {
		ROLE_USER, ROLE_ADMIN
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@NaturalId
	@Column(name = "name", length = 30, unique = true)
	private RoleName name;

	@Column(name="description", length=45)
	private String description;
	
	
	public Role() {

	}

	public Role(RoleName name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public RoleName getName() {
		return name;
	}

	public void setName(RoleName name) {
		this.name = name;
	}
}
