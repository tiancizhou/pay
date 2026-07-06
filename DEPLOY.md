# Docker Compose 部署

## 1. 准备配置

```bash
cp .env.example .env
```

编辑 `.env`，至少修改 `MYSQL_ROOT_PASSWORD`：

```env
MYSQL_ROOT_PASSWORD=替换为强密码
MYSQL_DATABASE=dao_home
APP_PORT=8080
JAVA_OPTS=-Xms256m -Xmx512m
```

## 2. 启动服务

```bash
docker compose up -d --build
```

访问：

- 客户/技师入口：`http://服务器IP:8080/`
- 管理端入口：`http://服务器IP:8080/admin/login`

默认账号会在首次启动时自动初始化：

- 管理员：`admin / admin123`
- 技师：`tech / tech123`
- 客户：`client / client123`

上线后请在管理端立即重置默认密码。

## 3. 常用命令

```bash
docker compose ps
docker compose logs -f app
docker compose logs -f mysql
docker compose restart app
docker compose down
```

数据库数据保存在 Docker volume `mysql-data`，上传文件保存在 `app-uploads`。
