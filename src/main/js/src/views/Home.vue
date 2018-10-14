<template>
  <v-flex xs6 id="home">
    <h1>Home</h1>
    <div class="posts" v-for="post in posts" :key="post.id">
      <post :post="post" v-on:post-deleted="handleDeleted"/>
    </div>
    <p v-if="posts.length == 0">There are no post here. Create a new one below!</p>
    <post-form v-on:post-created="handleCreated"/>
  </v-flex>
</template>

<script>
import axios from 'axios'
import Post from '../components/Post'
import PostForm from '../components/PostForm'

export default {
  data () {
    return {
      posts: []
    }
  },
  methods: {
    handleFetch () {
      axios.get('/rest/posts').then((response) => {
        this.posts = response.data
      })
    },
    handleCreated (post) {
      this.posts.push(post)
    },
    handleDeleted (id) {
      this.posts = this.posts.filter(post => post.id != id)
    }
  },
  mounted: function () {
    this.$nextTick(function () {
      this.handleFetch()
    })
  },
  components: {
    Post,
    PostForm
  }
}
</script>
