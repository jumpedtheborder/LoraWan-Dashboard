<template>
  <v-card>
    <v-card-title primary-title>
      <h3 class="headline mb-0">{{post.title}}</h3>
    </v-card-title>
    <v-card-text>
      {{post.body}}
    </v-card-text>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn icon @click="handleDelete"><v-icon>delete</v-icon></v-btn>
    </v-card-actions>
  </v-card>
</template>

<script>
import axios from "axios"

export default {
  data () {
    return {

    }
  },
  props: {
    post: {
      returied: true,
      type: Object
    }
  },
  methods: {
    handleDelete() {
      axios.delete(`/rest/post/${this.post.id}`).then(() => {
        this.$emit('post-deleted', this.post.id)
      })
    }
  }
}
</script>
