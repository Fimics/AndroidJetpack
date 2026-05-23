# MCP 外部工具集成指南

Claude Code 通过 MCP（Model Context Protocol）协议连接外部工具。

## 配置位置

| 文件 | 作用域 | 适用场景 |
|------|--------|----------|
| `~/.claude/settings.json` | 全局 | 通用工具（飞书、Jira、日历） |
| `.claude/settings.local.json` | 当前项目 | 项目专用工具（数据库、API） |

## 配置格式

在 `settings.json` 或 `settings.local.json` 中添加 `mcpServers` 字段：

```json
{
  "permissions": { ... },
  "mcpServers": {
    "server-name": {
      "command": "npx",
      "args": ["-y", "mcp-server-xxx"],
      "env": {
        "API_KEY": "your-key"
      }
    }
  }
}
```

## 前置要求

```bash
# 确保 Node.js >= 18
node --version

# 如果未安装，使用 nvm 安装
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash
nvm install 18
```

---

## 1. GitHub

操作 GitHub Issues、PR、代码搜索、管理 Release。

### 获取 Token 步骤

1. 打开 https://github.com/settings/tokens?type=beta
2. 点击 **Generate new token**
3. 填写 Token 名称（如 `claude-code-mcp`）
4. 选择 Repository access → **All repositories**（或指定仓库）
5. 勾选权限：
   - `Issues` — Read and Write
   - `Pull requests` — Read and Write
   - `Contents` — Read（代码搜索）
   - `Metadata` — Read
6. 点击 **Generate token**，复制 `ghp_` 开头的 token

### 配置

```json
{
  "mcpServers": {
    "github": {
      "command": "npx",
      "args": ["-y", "@anthropic-ai/mcp-github"],
      "env": {
        "GITHUB_TOKEN": "ghp_xxxxxxxxxxxx"
      }
    }
  }
}
```

### 验证

在 Claude Code 中输入："查看我的 GitHub Issues" 或 "列出最近的 PR"

---

## 2. Google Drive

读取 Google Drive 文档、表格、幻灯片。

### 获取凭证步骤

1. 打开 https://console.cloud.google.com/
2. 创建新项目（或选择已有项目）
3. 进入 **APIs & Services → Library**
4. 搜索并启用 **Google Drive API**
5. 进入 **APIs & Services → Credentials**
6. 点击 **Create Credentials → OAuth client ID**
7. Application type 选择 **Desktop app**
8. 创建后获得 `Client ID` 和 `Client Secret`
9. 进入 **OAuth consent screen**，添加测试用户（你的 Google 邮箱）
10. 获取 Refresh Token：
    ```bash
    # 方法一：使用 OAuth Playground
    # 打开 https://developers.google.com/oauthplayground/
    # 设置 → 勾选 "Use your own OAuth credentials" → 填入 Client ID/Secret
    # Step 1 → 选择 Google Drive API v3 → https://www.googleapis.com/auth/drive.readonly
    # Step 2 → Exchange authorization code for tokens
    # 复制 Refresh Token

    # 方法二：命令行获取
    npx -y @anthropic-ai/mcp-gdrive auth
    # 按提示完成浏览器授权，自动保存 token
    ```

### 配置

```json
{
  "mcpServers": {
    "gdrive": {
      "command": "npx",
      "args": ["-y", "@anthropic-ai/mcp-gdrive"],
      "env": {
        "GOOGLE_CLIENT_ID": "123456789.apps.googleusercontent.com",
        "GOOGLE_CLIENT_SECRET": "GOCSPX-xxxxxxxxxxxx",
        "GOOGLE_REFRESH_TOKEN": "1//xxxxxxxxxxxx"
      }
    }
  }
}
```

### 验证

在 Claude Code 中输入："搜索我 Google Drive 中的需求文档" 或 "读取 Drive 中的 xxx 表格"

---

## 3. Figma

读取 Figma 设计稿，提取组件信息，生成 Compose/XML 布局代码。

### 获取 Token 步骤

1. 登录 https://www.figma.com/
2. 点击左上角头像 → **Settings**
3. 滚动到 **Personal access tokens**
4. 点击 **Generate new token**
5. 填写描述（如 `claude-code`）
6. Expiration 选择 **No expiration**（或自定义过期时间）
7. Scope 勾选：
   - `File content` — Read only
   - `Dev resources` — Read only（可选）
8. 点击 **Generate token**，复制 `figd_` 开头的 token

### 配置

```json
{
  "mcpServers": {
    "figma": {
      "command": "npx",
      "args": ["-y", "@anthropic-ai/mcp-figma"],
      "env": {
        "FIGMA_ACCESS_TOKEN": "figd_xxxxxxxxxxxx"
      }
    }
  }
}
```

### 验证

在 Claude Code 中输入："读取 Figma 文件 https://www.figma.com/file/xxxxx 的设计稿"

---

## 4. Jira

管理项目任务、Sprint、Issue 跟踪。

### 获取 Token 步骤

1. 打开 https://id.atlassian.com/manage-profile/security/api-tokens
2. 点击 **Create API token**
3. 填写 Label（如 `claude-code`）
4. 点击 **Create**，复制 token
5. 记下你的 Jira 域名（如 `your-team.atlassian.net`）和登录邮箱

### 配置

```json
{
  "mcpServers": {
    "jira": {
      "command": "npx",
      "args": ["-y", "mcp-server-jira"],
      "env": {
        "JIRA_BASE_URL": "https://your-team.atlassian.net",
        "JIRA_EMAIL": "your-email@example.com",
        "JIRA_API_TOKEN": "ATATTxxxxxxxxxx"
      }
    }
  }
}
```

### 验证

在 Claude Code 中输入："查看 Jira 中分配给我的 Issue" 或 "创建一个新的 Jira Issue"

---

## 5. 飞书（Lark）

读写飞书文档、多维表格、发送消息。

### 获取凭证步骤

1. 打开 https://open.feishu.cn/app/
2. 点击 **创建企业自建应用**
3. 填写应用名称（如 `Claude Code MCP`）和描述
4. 创建后进入应用 → **凭证与基础信息**
5. 复制 **App ID**（`cli_` 开头）和 **App Secret**
6. 进入 **权限管理 → API 权限**，添加以下权限：
   - `docx:document:readonly` — 读取文档
   - `wiki:wiki:readonly` — 读取知识库
   - `bitable:bitable:readonly` — 读取多维表格
   - `im:message:send_as_bot` — 发送消息（可选）
   - `drive:drive:readonly` — 读取云空间文件（可选）
7. 进入 **版本管理与发布** → 创建版本 → 申请发布
8. 管理员在 **飞书管理后台** 审批应用上线

### 配置

```json
{
  "mcpServers": {
    "feishu": {
      "command": "npx",
      "args": ["-y", "mcp-server-lark"],
      "env": {
        "FEISHU_APP_ID": "cli_xxxxxxxxxxxx",
        "FEISHU_APP_SECRET": "xxxxxxxxxxxxxxxxxxxx"
      }
    }
  }
}
```

### 验证

在 Claude Code 中输入："读取飞书文档 https://xxx.feishu.cn/docx/xxxxx"

---

## 6. Slack

发送消息、读取频道、搜索历史消息。

### 获取 Token 步骤

1. 打开 https://api.slack.com/apps
2. 点击 **Create New App → From scratch**
3. 填写 App Name，选择 Workspace
4. 进入 **OAuth & Permissions**
5. 在 **Bot Token Scopes** 添加：
   - `channels:history` — 读取公共频道消息
   - `channels:read` — 列出频道
   - `chat:write` — 发送消息
   - `search:read` — 搜索消息（可选）
   - `users:read` — 读取用户信息（可选）
6. 点击 **Install to Workspace**，授权
7. 复制 **Bot User OAuth Token**（`xoxb-` 开头）

### 配置

```json
{
  "mcpServers": {
    "slack": {
      "command": "npx",
      "args": ["-y", "@anthropic-ai/mcp-slack"],
      "env": {
        "SLACK_BOT_TOKEN": "xoxb-xxxxxxxxxxxx"
      }
    }
  }
}
```

---

## 7. Sentry（错误监控）

查看崩溃报告，关联到代码位置。适合分析 Android 线上崩溃。

### 获取 Token 步骤

1. 登录 https://sentry.io/
2. 进入 **Settings → Auth Tokens**（或 https://sentry.io/settings/auth-tokens/）
3. 点击 **Create New Token**
4. Scope 勾选：
   - `project:read`
   - `event:read`
   - `issue:read`
5. 复制 `sntrys_` 开头的 token
6. 记下你的 Organization slug（URL 中 sentry.io/organizations/`your-org`/）

### 配置

```json
{
  "mcpServers": {
    "sentry": {
      "command": "npx",
      "args": ["-y", "mcp-server-sentry"],
      "env": {
        "SENTRY_AUTH_TOKEN": "sntrys_xxxxxxxxxxxx",
        "SENTRY_ORG": "your-org"
      }
    }
  }
}
```

---

## 8. 文件系统（Filesystem）

访问指定目录的文件，适合跨项目引用代码。无需 Token。

### 配置

```json
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@anthropic-ai/mcp-filesystem", "/home/mac/documents", "/home/mac/other-projects"]
    }
  }
}
```

---

## 9. PostgreSQL / MySQL 数据库

直接查询数据库，适合后端联调。无需额外 Token，使用数据库连接串。

### 配置

```json
{
  "mcpServers": {
    "postgres": {
      "command": "npx",
      "args": ["-y", "@anthropic-ai/mcp-postgres", "postgresql://user:password@localhost:5432/dbname"]
    }
  }
}
```

---

## 10. 浏览器自动化（Puppeteer）

自动化浏览器操作，截图、爬取页面。无需 Token。

### 配置

```json
{
  "mcpServers": {
    "puppeteer": {
      "command": "npx",
      "args": ["-y", "@anthropic-ai/mcp-puppeteer"]
    }
  }
}
```

---

## 本项目当前配置

`.claude/settings.local.json` 已配置以下 MCP Server（需替换占位符）：

| 工具 | 状态 | 占位符替换 |
|------|------|-----------|
| GitHub | `<your-github-token>` → 替换为 `ghp_xxx` | 参考上方第 1 节 |
| Google Drive | `<your-client-id>` 等 → 替换为 OAuth 凭证 | 参考上方第 2 节 |
| Figma | `<your-figma-token>` → 替换为 `figd_xxx` | 参考上方第 3 节 |
| Jira | `<your-jira-token>` 等 → 替换为 API Token | 参考上方第 4 节 |
| 飞书 | `<your-app-id>` 等 → 替换为应用凭证 | 参考上方第 5 节 |

## 启用步骤

```bash
# 1. 编辑配置文件，替换所有 <your-xxx> 占位符
vim .claude/settings.local.json

# 2. 重启 Claude Code 使配置生效
# 退出当前会话，重新启动 claude

# 3. 启动后 Claude Code 会自动连接已配置的 MCP Server
# 首次运行会通过 npx 自动下载依赖包

# 4. 验证连接（在 Claude Code 对话中测试）
# "查看我的 GitHub Issues"
# "读取飞书文档 xxx"
# "查看 Figma 设计稿 xxx"
```

## 安全注意事项

- `.claude/` 已在 `.gitignore` 中，Token 不会被提交到 Git
- 遵循最小权限原则，只授予必要的 API 权限
- 定期轮换 Token（建议每 90 天）
- 生产环境数据库使用只读账号连接
- 不需要的工具可以从配置中删除，减少攻击面

## 常见问题

### Q: 启动报错 `npx: command not found`
A: 安装 Node.js >= 18：`nvm install 18`

### Q: MCP Server 连接超时
A: 检查网络代理设置，部分 npm 包需要访问外网。可设置：
```bash
npm config set proxy http://your-proxy:port
npm config set https-proxy http://your-proxy:port
```

### Q: Token 填入后仍然无法使用
A: 确保重启了 Claude Code（完全退出再重新启动），配置仅在启动时加载。

### Q: 如何只启用部分工具？
A: 在 `settings.local.json` 中只保留需要的 `mcpServers` 条目，删除不需要的即可。

## Android 开发推荐组合

| 优先级 | 工具 | 用途 |
|--------|------|------|
| 高 | **GitHub** | 管理 Issue、PR review、代码搜索 |
| 高 | **Figma** | 设计稿 → Compose/XML 布局代码 |
| 中 | **飞书/Jira** | 同步任务状态、读取需求文档 |
| 中 | **Sentry** | 分析线上崩溃，直接定位到源码行 |
| 低 | **Filesystem** | 引用其他项目的公共模块代码 |
| 低 | **Puppeteer** | Web 管理后台自动化测试 |