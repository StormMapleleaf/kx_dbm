<template>
  <div class="asideBarItem-container">
    <el-submenu :index="router.path" v-if="hasChildrenAndShow(router)">
      <template slot="title">
      <i :class="router.icon"></i>
      <span slot="title">{{router.name}}</span>
      </template>
      <asideBarItem v-for="(child, childKey) in router.children" :key="child.path" :router="child"></asideBarItem>
    </el-submenu>
    <el-menu-item v-else :key="router.path" :index="router.path" @click="saveActivePath(router.path)">
       <i :class="router.icon"></i>
       <span>{{router.name}}</span>
    </el-menu-item>
  </div>
</template>
 
<script>
export default {
  name: "asideBarItem",
  props: {
    router: {
      type: Object
    },
  },
  components: {},
  data() {
    return {
    };
  },
  computed: {
  },
  watch: {},
  methods: {
    hasChildrenAndShow(router){
      if(router.hidden){
        return false
      }

      return router.hasOwnProperty('children');
    },
    saveActivePath(path) {
      this.$emit('setActivePath',path);
    },
  },
  created() {
  },
  mounted() {}
};
</script>
 
<style scoped>
.el-menu-item.is-active {
  background-color: #ecf5ff !important;
  color: #409eff !important;
}

  .el-menu--collapse .asideBarItem-container span{
    display: none;
  }
  .el-menu--collapse .asideBarItem-container .el-submenu__title .el-submenu__icon-arrow{
    display: none;
  }
  
</style>