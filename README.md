# 安装部署

1. 请在amd64平台的Linux或WSL下使用

2. 将两个依赖镜像导入docker

   ```bash
   docker load -i toolbuild-base.tar
   docker load -i toolbuild-maven.tar
   ```

3. 执行构建脚本

   ```bash
   cd ./build-docker/
   chmod +x ./build_image.sh
   sh ./build_image.sh
   #构建成功后出现datatool:1.0.0的镜像
   ```

4. 运行容器

   ```bash
   docker run -d --name dbswitch  -e DBTYPE=h2  -v /tmp:/tmp  -p 9088:9088 datatool:1.0.0
   ```

5. 访问localhost:9088,默认用户名admin，默认密码123456



# 使用

1. 登录，输入用户名密码登录![image-20251225141506365](/Users/sleaves/Library/Application Support/typora-user-images/image-20251225141506365.png)

2.点击左侧菜单栏连接管理，数据源页面，点击右上角创建数据源

![image-20251225141616281](/Users/sleaves/Library/Application Support/typora-user-images/image-20251225141616281.png)

3. 以postgres为例，输入数据库连接信息，点击下方测试即可测试数据库连通性，测试连通性后点击创建

   ![image-20251225141906504](/Users/sleaves/Library/Application Support/typora-user-images/image-20251225141906504.png)

4. 点击迁移任务，任务管理页面，创建任务

   ![image-20251225142931018](/Users/sleaves/Library/Application Support/typora-user-images/image-20251225142931018.png)

5. 选择源数据库，目标数据库，配置需要迁移的表以及其他信息，最后提交保存

   ![image-20251225143051834](/Users/sleaves/Library/Application Support/typora-user-images/image-20251225143051834.png)

![image-20251225143117720](/Users/sleaves/Library/Application Support/typora-user-images/image-20251225143117720.png)

6. 点击启动任务，手动模式需要手动点击执行按钮来完成迁移任务

   ![image-20251225143232918](/Users/sleaves/Library/Application Support/typora-user-images/image-20251225143232918.png)

7. 监控调度页面可以查看执行详情等信息

   ![image-20251225143414811](/Users/sleaves/Library/Application Support/typora-user-images/image-20251225143414811.png)