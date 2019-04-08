package com.uniofsurrey.lorawandashboard.repositories;

import com.uniofsurrey.lorawandashboard.entities.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByUsername(String email);
}
