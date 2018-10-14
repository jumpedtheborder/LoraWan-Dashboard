package com.project.example.repositories;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import com.project.example.entities.Post;

public interface PostRepository extends CrudRepository<Post, Long> {
    List<Post> findByTitle(String title);
}
