<template>
  <div class="user-dropdown-wrap">
    <el-dropdown>
      <div class="user-dropdown-photo">
        <span class="user-dropdown-text">
          {{username}}
          <i class="el-icon-caret-bottom"></i>
        </span>
        <span class="user-avatar-text">用户</span>
      </div>
      <el-dropdown-menu solt="dropdown">
        <el-dropdown-item>
          <router-link to="/user/personal">
            <i class="el-icon-s-custom"></i>个人信息
          </router-link>
        </el-dropdown-item>
        <el-dropdown-item divided>
          <a @click="hadleLogout()">
            <i class="el-icon-switch-button"></i>退出登录
          </a>
        </el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
  </div>
</template>

<script>
export default {
  data () {
    return {
      username: "",
      nickname: ""
    };
  },
  created () {
    this.username = window.sessionStorage.getItem("username");
    this.nickname = window.sessionStorage.getItem("realname");
  },
  methods: {
    hadleLogout () {
      window.sessionStorage.clear();
      this.$http({
        method: 'GET',
        url: '/dbswitch/admin/api/v1/authentication/logout'
      }),
        this.$router.push("/login");
    }
  },
  destroyed () {
    window.sessionStorage.setItem("activePath", "/");
  }
};
</script>
<style scoped>
.user-dropdown-wrap {
  height: 60px;
  padding: 10px 0;
  float: right;
  cursor: pointer;
  color: #606266;
}

.user-dropdown-wrap .user-dropdown-photo {
  display: flex;
  align-items: center;
}

.user-dropdown-wrap .user-dropdown-photo img {
  display: none;
}

.user-avatar-text {
  width: 30px;
  height: 30px;
  background: #ffffff;
  color: #606266;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 14px;
  margin-right: 10px;
  cursor: pointer;
  user-select: none;
}

.user-dropdown-text {
  color: #606266;
}
</style>
