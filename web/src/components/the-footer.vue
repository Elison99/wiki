<template>
  <a-layout-footer style="text-align: center">
    Ant Design ©2018 Created by Ant UED1111{{user.name}}
  </a-layout-footer>
</template>

<script lang="ts">
import {computed, defineComponent} from 'vue';
import store from "@/store";

export default defineComponent({
  name: 'the-footer',
  setup(){
    const user = computed(()=>store.state.user)
    return{
      user
    }
  }
});
</script>