package com.example.kadai_002.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kadai_002.entity.User;

public interface UserRepository extends JpaRepository<User, Integer>{
	public User findByEmail(String email);
	public Page<User> findByEmailLike(String keyword, Pageable pageable);
	public Long countByRoleId(Integer roleId);

}
