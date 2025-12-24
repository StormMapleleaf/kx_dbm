<template>
  <el-breadcrumb class="app-breadcrumb" separator-class="el-icon-arrow-right">
    <transition-group>
      <el-breadcrumb-item v-for="(item,index) in levelList" :key="item.path" v-if="item.name">
        <span v-if='item.redirect==="noredirect"||index==levelList.length-1'  class="no-redirect">{{item.name}}</span>
        <router-link v-else :to="item.redirect||item.path">{{item.name}}</router-link>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script>
export default {
  name: "breadcrumb",
  data() {
    return {
      levelList: []
    };
  },
  created() {
    this.getBreadcrumb();
  },
  watch: {
    $route() {
      this.getBreadcrumb();
    }
  },
  methods: {
    getBreadcrumb() {
      let matched = this.$route.matched.filter(item => item.name); 
    }
  }
};
</script>

<style scoped>
.el-header .el-breadcrumb {
  float: left;
  margin: 18px 0 0 20px;
}

.app-breadcrumb .el-breadcrumb .no-redirect {
  color: #909399;
  cursor: text;
}
</style>