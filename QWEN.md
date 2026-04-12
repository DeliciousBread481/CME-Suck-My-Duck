# CME-Suck-My-Duck 项目上下文

## 项目概述

**CME-Suck-My-Duck** 是一个 Java Agent 工具，用于调试 `ConcurrentModificationException` 和 `IndexOutOfBoundsException` 异常。它通过字节码注入技术，监控指定集合字段的修改操作，并记录完整的调用堆栈轨迹到日志文件中，帮助开发者定位并发修改问题的根源。

### 核心功能

- **集合监控**：包装目标类的 List、Set、Map 等集合字段，记录所有修改操作的堆栈轨迹
- **方法注入**：可在指定方法调用时记录堆栈信息
- **局部变量监控**：支持监控方法内的局部变量（集合类型）
- **多线程安全转换**：可选地将集合转换为线程安全实现（不推荐用于生产）
- **多平台支持**：兼容 Minecraft Forge、Fabric、NeoForge 模组加载器，也可用于普通 Java 项目

### 技术栈

- **Java 8**（source/target compatibility）
- **ASM 9.6**：字节码操作框架
- **Gson 2.11.0**：JSON 解析
- **Gradle**：构建工具
- **JUnit 5**：测试框架

### 项目结构

```
src/main/java/com/hexagram2021/cme_suck_my_duck/
├── CMESuckMyDuck.java              # 主入口，Java Agent 实现
├── CMESuckFabricMod.java           # Fabric 模组集成
├── CMESuckForgeMod.java            # Forge 模组集成
├── CMESuckNeoForgeMod.java         # NeoForge 模组集成
├── Type.java                       # 支持的集合类型枚举
├── annotations/                    # 空值注解（NonnullByDefault）
├── containers/                     # 包装集合实现（WrappedList/Map/Set 等）
│   └── iterators/                  # 包装迭代器实现
│   └── spliterators/               # 包装 Spliterator 实现
├── exceptions/                     # 自定义异常（TracedException）
├── log/                            # 日志条目实现
├── transformers/                   # ASM 字节码转换器
│   ├── WrapContainerTransformer.java       # 字段包装转换
│   ├── WrapLocalContainerTransformer.java  # 局部变量包装转换
│   ├── InjectLogTransformer.java           # 方法注入转换
│   └── InjectTraceIdUpdaterTransformer.java # Trace ID 更新注入
└── utils/                          # 工具类（日志、TraceId、常量等）
```

## 构建与运行

### 构建命令

```bash
# 构建 JAR（Windows）
gradlew.bat build

# 仅打包 JAR
gradlew.bat jar

# 运行测试
gradlew.bat test

# 清理构建产物
gradlew.bat clean
```

### 使用方式

#### Minecraft 环境

1. 将构建的 JAR 放入 `mods` 文件夹
2. 在启动器中添加 JVM 参数：
   ```
   -javaagent:mods/CMESuckMyDuck-<version>.jar=<class full name>;<field name>;<type>;<phase>
   ```
3. 运行游戏，等待崩溃发生后查看日志

#### 普通 Java 项目

1. 确保 classpath 中包含 `gson` 和 `asm` JAR
2. 添加 Java Agent 参数（同上）

### 参数说明

| 参数 | 说明 |
|------|------|
| `<class full name>` | 目标类的全限定名（使用 `/` 而非 `.`） |
| `<field name>` | 目标字段名（Forge 使用 SRG 名，Fabric 使用 intermediary 名） |
| `<type>` | 集合类型：`List`、`Set`、`Map`、`Int2ObjectMap`、`Object2IntMap` 等 |
| `<phase>` | `static` 或 `nonstatic`，表示字段是静态还是非静态 |

### 系统属性配置

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `cme_suck_my_duck.log_level` | `1` | 日志级别，`0` 输出调试信息 |
| `cme_suck_my_duck.asm_api_version` | `9` | ASM API 版本（旧版 MC 可能需要设为 `5`） |
| `cme_suck_my_duck.file_max_entries` | `1000` | 每个日志文件的最大堆栈条目数 |
| `cme_suck_my_duck.log_wait_time` | `500` | 日志线程等待时间（毫秒） |
| `cme_suck_my_duck.whitelist_constructor_stacktrace` | 空 | 白名单堆栈内容，仅匹配特定构造路径 |
| `cme_suck_my_duck.transform_to_thread_safe` | `false` | 转换为线程安全集合（不推荐） |
| `cme_suck_my_duck.inject_method` | `false` | 启用方法注入模式 |
| `cme_suck_my_duck.ignore_threads` | 空 | 忽略的线程名列表（分号分隔） |
| `cme_suck_my_duck.stop_logging_if_exception_created` | `true` | 发生严重异常后是否停止日志 |
| `cme_suck_my_duck.trace_id_updater` | 空 | Trace ID 更新方法（`<class>;<method>`） |
| `cme_suck_my_duck.local_var_index` | null | 监控局部变量的索引 |
| `cme_suck_my_duck.match_local_index` | `-1` | 匹配 `ASTORE` 操作的序数 |

### 使用示例

```bash
# 监控 Forge 1.20.1 SoundEngine 的 Map 字段
-javaagent:mods/CMESuckMyDuck-1.0.0.jar=net/minecraft/client/sounds/SoundEngine;f_120229_;Map;nonstatic

# 监控 Forge 1.20.1 PotionBrewing 的静态 List 字段
-javaagent:mods/CMESuckMyDuck-1.0.0.jar=net/minecraft/world/item/alchemy/PotionBrewing;f_43494_;List;static

# 监控 Zeta 模组的 Map 字段
-javaagent:CMESuckMyDuck-1.0.0.jar=org/violetmoon/zetaimplforge/event/ForgeZetaEventBus;convertedHandlers;Map;nonstatic
```

## 开发规范

### 代码规范

- **Java 版本**：项目目标为 Java 8 兼容性
- **空值注解**：使用 `@FieldsAreNonnullByDefault`、`@MethodsReturnNonnullByDefault`、`@ParametersAreNonnullByDefault`
- **包命名**：全小写加下划线（如 `cme_suck_my_duck`）
- **类命名**：大驼峰式（如 `WrapContainerTransformer`）
- **方法命名**：小驼峰式（如 `getTransformer`）
- **常量命名**：全大写加下划线（如 `ASM_API_VERSION`）

### 项目配置

- **Group ID**：`com.hexagram2021.cme_suck_my_duck`
- **版本**：`1.1.2`
- **编码**：UTF-8
- **JAR Manifest**：
  - `Premain-Class`: `com.hexagram2021.cme_suck_my_duck.CMESuckMyDuck`
  - `Main-Class`: `com.hexagram2021.cme_suck_my_duck.CMESuckMyDuck`
  - `Can-Redefine-Classes`: `true`
  - `Can-Retransform-Classes`: `true`

### 依赖管理

- **核心依赖**：`asm`、`gson`、`jsr305`（实现依赖）
- **可选依赖**（compileOnly）：`fabric-loader`、`fastutil`、`guava`、NeoForge loader
- **测试依赖**：JUnit 5（通过 `junit-bom` 管理版本）

### Maven 仓库

- Maven Central
- Minecraft Libraries (`libraries.minecraft.net`)
- Fabric Maven (`maven.fabricmc.net`)
- Forge Maven (`maven.minecraftforge.net`)
- NeoForge Maven (`maven.neoforged.net/releases`)
- 本地 `libs` 目录

## 日志输出

- 默认日志路径由 `SharedConstants.LOG_PATH` 定义
- 日志文件按 `file_max_entries` 轮转，保留最近 2000 条记录
- 支持自定义日志级别和线程等待时间

## 注意事项

- 本项目**仅能作为 Java Agent 使用**，不能直接运行
- 转换为线程安全集合会降低性能，**不推荐**用于生产环境
- 对于旧版本 Minecraft（如 1.12.2），需要调整 `asm_api_version` 为较低值（如 `5`）
- 日志白名单功能可精确过滤特定构造路径的集合
