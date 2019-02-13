package com.uniofsurrey.lorawandashboard.repositories;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import com.uniofsurrey.lorawandashboard.entities.Post;

public interface PostRepository extends CrudRepository<Post, Long> {
    List<Post> findByTitle(String title);
}
