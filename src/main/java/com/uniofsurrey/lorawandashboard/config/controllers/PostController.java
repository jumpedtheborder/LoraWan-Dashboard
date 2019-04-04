package com.uniofsurrey.lorawandashboard.config.controllers;

import com.uniofsurrey.lorawandashboard.config.entities.Post;
import com.uniofsurrey.lorawandashboard.config.repositories.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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