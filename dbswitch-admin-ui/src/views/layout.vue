<template>
  <el-container class="index-container">
    <el-aside :width="isCollapse?'64px':'250px'">
      <div class="title">
        <span class="title-text"
              v-if="!isCollapse">{{title}}</span>
      </div>
      <asideBar ref="asideBar"></asideBar>
    </el-aside>
    <el-main>
      <el-header>
        <div @click="handleToggleCollapse"
             class="collapse">
          <i class="el-icon-s-unfold"
             v-if="isCollapse"></i>
          <i class="el-icon-s-fold"
             v-else></i>
        </div>

        <breadcrumb></breadcrumb>
        <userDropdown></userDropdown>
      </el-header>

      <viewMain></viewMain>
    </el-main>
  </el-container>
</template>
 
<script>
import asideBar from "@/components/asideBar/asideBar";
import breadcrumb from "@/components/breadcrumb/index";
import userDropdown from "@/components/userDropdown/index"
import viewMain from "@/views/viewer";

export default {
  name: "home",
  components: {
    asideBar,
    breadcrumb,
    userDropdown,
    viewMain
  },
  data () {
    return {
      title: "迁移工具",
      isCollapse: null
    };
  },
  computed: {},
  watch: {},
  methods: {
    handleToggleCollapse () {
      let status = !this.isCollapse;
      this.isCollapse = status;
      this.$refs.asideBar.updateCollapse(status);
    }
  },
  created () {
  },
  mounted () { }
};
</script>
 
<style scoped>
.index-container {
  height: 100%;
}

.el-aside {
  float: left;
  background: #ffffff;
  color: #333;
  text-align: left;
  border-right: 1px solid #e4e7ed;
}

.el-aside .title {
  height: 60px;
  background: #f5f7fa;
  line-height: 64px;
  text-align: center;
  border-bottom: 1px solid #e4e7ed;
}

.el-aside .title img {
  width: 50px;
  height: 50px;
  vertical-align: middle;
}

.el-aside .title .title-text {
  color: #606266;
  font-weight: 600;
  font-size: 20px;
  vertical-align: middle;
}

.el-header {
  padding: 0px;
  color: #606266;
  width: 100%;
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
}

.el-header .collapse {
  float: left;
  padding-left: 2px;
  font-size: 22px;
  color: #909399;
  margin-top: 10px;
  margin-right: 20px;
  cursor: pointer;
}

.el-main {
  padding: 0px;
  float: left;
  background-color: #f5f7fa;
}
</style>
