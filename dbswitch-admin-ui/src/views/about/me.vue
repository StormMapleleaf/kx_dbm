<template>
 
    
  
</template>

<script>
import layout from '../layout.vue';
export default {
  components: { layout },
  data () {
    return {
      activeName: 'first'
    };
  },
  methods: {
    handleClick (tab, event) {
      //console.log(tab, event);
    }
  }
}
</script>

<style scoped>
div h3 {
  text-align: center;
}

.content_tag {
  text-align: center;
}

.content_card {
  padding-top: 20px;
}
</style>
