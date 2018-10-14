package com.project.example.controllers;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import com.project.example.repositories.PostRepository;
import com.project.example.entities.Post;
import javax.validation.Valid;
import java.lang.Iterable;
import java.lang.RuntimeException;

@RestController
public class PostController {
    private PostRepository postRepository;

    @Autowired
    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @GetMapping("/rest/posts")
    public Iterable<Post> getPosts() {
        return postRepository.findAll();
    }

    @GetMapping("/rest/post/{id}")
    public Post getPost(@PathVariable Long id) {
        return postRepository.findById(id).orElseThrow(() -> new RuntimeException ("not found"));
    }

    @DeleteMapping("/rest/post/{id}")
    public void deletePost(@PathVariable Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException ("not found"));
        postRepository.delete(post);
    }

    @PostMapping("/rest/post")
    public Post createPost(@Valid @RequestBody Post newPost) {
        return postRepository.save(newPost);
    }
}