# CodeReview Helper Next

CodeReview Helper Next 是基于 [Veezean / IntellijIDEA-CodeReview-Plugin](https://github.com/veezean/IntellijIDEA-CodeReview-Plugin) 的社区维护 Fork，面向 IDEA 2026.x 的兼容版本。

> 首先感谢原作者 Veezean 提供完整、稳定的插件底座，以及长期以来对 Code Review Helper 的投入。本项目不取代原项目；原作者因个人安排暂停后续维护后，社区用户在新版 IDEA 中遇到兼容问题，因此创建此 Fork 继续维护。

## 为什么需要这个 Fork

原版插件在 IDEA 2026.1 / 2026.2.1 中会遇到以下问题：

| 场景 | 原因 | CodeReview Helper Next 修复 |
| --- | --- | --- |
| 打开 CodeReview 只显示“没有要显示的内容” | 旧版依赖和 GUI Designer 生成代码无法适配新版运行环境，工具窗口初始化失败 | 补齐运行依赖与窗体生成代码，恢复工具窗口显示 |
| 双击评审表格白色区域无法跳转源码 | IDEA 2026 要求文件索引查询在 `ReadAction` 中执行 | 将文件查找放入 `ReadAction`，并使用新版文件打开方式定位到评审行 |

当前维护版本：**4.2.4**。

## 兼容性

- 已针对 IDEA **2026.1**、**2026.2.1** 完成兼容修复。
- 其他 IDEA 版本未逐一验证；建议优先使用原项目适配的发行版本。
- Git 功能依赖 IDEA 自带的 Git 插件，请勿禁用 `Git4Idea`。

## 功能概览

- `Alt + A` 快速添加评审意见
- 编辑器行号旁标记存在评审意见的代码位置
- 双击评审记录中的白色不可编辑区域，跳转到对应代码行
- 双击黄色可编辑区域，直接编辑评审字段
- 本地评审意见的新增、修改、删除、确认与跟踪
- Excel 导入、导出
- 自定义评审字段与中英文界面
- 可选的服务端同步，支持团队协作

## 安装

### 从本仓库安装

下载 [CodeReview-Helper-Next-4.2.4.zip](build/distributions/CodeReview-Helper-Next-4.2.4.zip)，然后在 IDEA 中打开：

`Settings / Preferences → Plugins → ⚙ → Install Plugin from Disk...`

选择 ZIP 后按提示重启 IDEA。安装完成后，可在 IDEA 底部工具窗口打开 `CodeReview Helper Next`。

### 使用说明

1. 在编辑器中选中代码，按 `Alt + A` 添加评审意见。
2. 在 CodeReview Helper Next 工具窗口中，双击白色单元格跳到记录对应的代码行。
3. 按住 `Alt` 单击记录可打开确认窗口。
4. 可通过工具窗口中的导入、导出按钮与 Excel 文件交换评审记录。
5. 服务端模式请在设置按钮中配置地址、账号和密码；服务端项目见原作者的 [CodeReviewServer](https://github.com/veezean/CodeReviewServer)。

## Maven Search 说明

Maven Search 显示 `Nothing to show` 与 CodeReview Helper Next 无关。经日志确认，其默认服务 `https://mvn.coderead.cn/api` 的 HTTPS 证书已过期，查询请求会失败并显示为空结果。

可在以下位置修改：

`Settings / Preferences → Tools → Maven Search → Remote Url`

临时可改为：

```text
http://mvn.coderead.cn/api
```

这属于 Maven Search 的远端服务证书问题，与 CodeReview 的评审数据无关。

## 开发与打包说明

项目使用 IDEA Platform API。若本地开发为了编译 `git4idea.*` 类型而额外添加 `git4idea` JAR，它仅是编译期依赖；原版发布包并未包含该 JAR，最终发布 ZIP 也不应包含。运行时由 IDEA 的 Git 插件通过下列声明提供 API：

```xml
<depends>Git4Idea</depends>
```

打包前请确认：

- 插件 ZIP 根目录为 `CodeReview-Helper-Next/`；
- `CodeReview-Helper-Next/lib/` 中不包含 `git4idea-*.jar`；
- GUI Designer `.form` 文件已完成字节码注入，否则工具窗口可能因 Swing 字段为空而无法初始化；
- 打开 CodeReview 工具窗口与双击跳转均已在目标 IDEA 版本中实际验证。

## 致谢与版权

- 原始插件、功能设计与早期版本：[@veezean](https://github.com/veezean)，原项目 [IntellijIDEA-CodeReview-Plugin](https://github.com/veezean/IntellijIDEA-CodeReview-Plugin)。
- 本仓库仅维护 IDEA 新版本兼容性及相关问题修复；所有新增改动会在本仓库记录。
- 原项目的许可证、版权声明及相关第三方组件声明继续适用，详见 [LICENSE](LICENSE)。

## 反馈

请在本 Fork 的 [Issues](https://github.com/dyj-guguguzi/CodeReview-Helper-Next/issues) 中提交 IDEA 版本、插件版本、复现步骤和 `idea.log` 中的异常片段，便于定位兼容问题。
