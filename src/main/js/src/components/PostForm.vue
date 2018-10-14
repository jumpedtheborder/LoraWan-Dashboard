<template>
    <v-form ref="form" v-model="valid" @submit.prevent="handleSubmit">
        <v-text-field
            v-model="title"
            :rules="titleRules"
            :counter="40"
            label="Title"
            required
        ></v-text-field>
        <v-textarea
            v-model="text"
            :rules="textRules"
            :counter="1000"
            label="Text"
            required
        ></v-textarea>
        <v-btn :disabled="!valid" @click="submit">Submit</v-btn>
    </v-form>
</template>

<script>
import axios from "axios"

export default {
  data() {
    return {
      valid: false,
      title: "",
      titleRules: [
        v => !!v || "Title is required",
        v => v.length <= 40 || "Title must be less than 40 characters"
      ],
      text: "",
      textRules: [
        v => !!v || "Text is required",
        v => v.length <= 1000 || "Text must be less than 1000 characters"
      ]
    }
  },
  methods: {
    submit() {
      if (this.$refs.form.validate()) {
        axios.post("/rest/post", {
          title: this.title,
          body: this.text
        }).then(response => {
          this.$emit('post-created', response.data)
        })
      }
    }
  }
};
</script>
