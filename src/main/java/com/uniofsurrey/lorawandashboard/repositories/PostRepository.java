package com.uniofsurrey.lorawandashboard.repositories;

import com.uniofsurrey.lorawandashboard.entities.Post;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PostRepository extends CrudRepository<Post, Long> {
    List<Post> findByTitle(String title);
}
