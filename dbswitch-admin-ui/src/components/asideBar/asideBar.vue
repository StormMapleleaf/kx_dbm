<template>
  <div class="aside-container">
    <el-row class="tac">
      <el-col :span="24">
        <el-menu
          :router="true"
          unique-opened
          @open="handleOpen"
          @close="handleClose"
          background-color="#ffffff"
          text-color="#606266"
          active-text-color="#409eff"
          :collapse="collapsed"
          :default-active="initActivePath"
        >
          <asideBarItem v-for="router in routers" :router="router" v-if="showBarItem(router)" @setActivePath='setActivePath' :key="router.path"></asideBarItem>
        </el-menu>
      </el-col>
    </el-row>
  </div>
</template>
 
<script>
import asideBarItem from "@/components/asideBar/asideBarItem";
export default {
  name: "asideBar",
  components: {
    asideBarItem
  },
  data() {
    return {
      collapsed:false,
      initActivePath:'/dashboard'
    };
  },
  computed: {
    routers() {
      return this.$router.options.routes[0].children;
    },
  },
  watch: {},
  methods: {
    showBarItem(router){
      if(router.hidden){
        return false
      }

      return true;
    },
    handleOpen(key, keyPath) {
    },
    handleClose(key, keyPath) {
    },
    updateCollapse(collapse){
      this.collapsed=collapse;
    },
    setActivePath(path){
      this.initActivePath=path;
      window.sessionStorage.setItem("activePath", path);
    },
    getActivePath(){
      return window.sessionStorage.getItem("activePath");
    }
  },
  created() {
    this.initActivePath = this.getActivePath();
  },
  mounted() {
  }
};
</script>
 
 <style scoped>
.aside-container {
  padding-top: 2px;
}

.el-menu {
  padding: 0;
  border-right: none;
}

</style>