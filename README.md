# 安装部署

1. 将两个依赖镜像导入docker

   ```bash
   docker load -i toolbuild-base.tar
   docker load -i toolbuild-maven.tar
   ```

2. 执行构建脚本

   ```bash
   cd ./build-docker/
   chmod +x ./build_image.sh
   sh ./build_image.sh
   #构建成功后出现datatool:1.0.0的镜像
   ```

3. 运行容器

   ```bash
   docker run -d --name dbswitch  -e DBTYPE=h2  -v /tmp:/tmp  -p 9088:9088 datatool:1.0.0
   ```

4. 访问localhost:9088,默认用户名admin，默认密码123456



# 使用