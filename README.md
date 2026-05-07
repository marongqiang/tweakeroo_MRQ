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

## MRQ 修复

本分支在原有特性基础上进行了以下 bug 修复：

- **SNAP_AIM_LAST_PITCH 配置丢失**: `Configs.Internal.OPTIONS` 持久化列表遗漏了 `SNAP_AIM_LAST_PITCH`，已补回
- **FeatureToggle 线程可见性**: `valueBoolean` 添加 `volatile` 修饰
- **MiscUtils 线程安全**: 移除共享可变 `Date` 对象，`SimpleDateFormat` 缓存复用
- **Pattern 缓存**: `InventoryUtils.PATTERN_SLOT_RANGE` 静态编译
- **异常日志**: `EntityRestriction`、`InventoryUtils`、`MixinPresetsScreen` 中空 catch 块添加 `Tweakeroo.logger` 日志

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
