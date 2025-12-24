<template>
  <commonDataSource :isReadOnly="true"></commonDataSource>
</template>

<script>
import commonDataSource from '@/views/connection/common'

export default {
  components: { commonDataSource },
  data () {
    return {
    }
  }
}
</script>

<style scoped>
</style>