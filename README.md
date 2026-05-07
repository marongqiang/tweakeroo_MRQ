# Tweakeroo MRQ

Tweakeroo 的 1.20.1 Fabric 分支，添加了中文支持和 JSON 驱动的 GUI 布局系统。

Forked from [sakura-ryoko/tweakeroo](https://github.com/sakura-ryoko/tweakeroo).

## 特性

- **JSON 驱动 GUI 布局** — 通过 `gui_layout.json` 自定义配置界面，支持可折叠分组和多标签页
- **中文翻译** — 完整的简体中文语言支持
- **潜影盒堆叠** — 空潜影盒可在背包和地面堆叠至 64
- **工具切换优化** — 对树叶、羊毛、藤蔓等方块优先使用剪刀而非其他工具
- **Gamma 覆盖修复** — 配置加载后正确恢复 gamma 值
- **潜影盒预览优化** — 预览时隐藏原版物品提示文字

## 编译

```bash
./gradlew build
```

编译产物位于 `build/libs/`。

## 运行

```bash
./gradlew runClient
```

## 配置

配置界面布局由 `config/tweakeroo/gui_layout.json` 控制，首次运行时会自动生成默认布局文件。分组展开/折叠状态保存在 `config/tweakeroo/gui_states.json`。
