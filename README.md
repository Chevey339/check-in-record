# 打卡记录 

一个简洁优雅的Android习惯养成应用，帮助您建立和维持良好的日常习惯。通过直观的日历视图和简单的打卡功能，让习惯养成变得更加有趣和可视化。

## 主要功能

-  **直观的日历视图** - 可视化查看打卡记录和进度
-  **简单的打卡操作** - 一键完成每日习惯打卡
-  **完成进度统计** - 实时查看每日完成情况
-  **自定义习惯管理** - 添加、编辑、删除个人习惯
-  **本地数据存储** - 数据安全存储在本地设备
-  **流畅的页面动画** - 优雅的页面转场效果
-  **现代化UI设计** - 基于Material Design 3

## 截图

![Screenshot_1](.\screenshots\Screenshot_1.png)

![Screenshot_2](.\screenshots\Screenshot_2.png)

## 系统要求

- **最低Android版本**: Android 7.0 (API Level 24)
- **目标Android版本**: Android 14 (API Level 36)
- **编译SDK版本**: 36

## 安装和运行

### 前置要求
- Android Studio Hedgehog (2023.1.1) 或更新版本
- JDK 11 或更高版本
- Android SDK 36

### 构建步骤

1. **克隆项目**
   
   ```bash
   git clone https://github.com/your-username/habit-tracker.git
   cd habit-tracker
   ```
   
2. **使用Android Studio打开项目**
   - 启动Android Studio
   - 选择 "Open an existing project"
   - 选择项目根目录

3. **同步依赖**
   ```bash
   ./gradlew sync
   ```

4. **构建项目**
   ```bash
   # Debug版本
   ./gradlew assembleDebug
   
   # Release版本
   ./gradlew assembleRelease
   ```

5. **运行应用**
   - 连接Android设备或启动模拟器
   - 点击Android Studio中的运行按钮
   - 或使用命令行：`./gradlew installDebug`

## 项目架构

```
app/src/main/java/com/example/uldemo/
├── MainActivity.kt                 # 应用入口和导航配置
├── components/                     # UI组件模块
│   ├── CalendarComponents.kt       # 日历相关组件
│   └── HabitComponents.kt         # 习惯相关组件
├── managers/                       # 数据管理模块
│   ├── CheckInManager.kt          # 打卡状态管理
│   └── HabitManager.kt            # 习惯数据管理
├── models/                        # 数据模型
│   └── HabitModels.kt            # 习惯数据类和枚举
├── screens/                       # 页面模块
│   ├── MainScreen.kt             # 主页面
│   ├── EditHabitsScreen.kt       # 编辑习惯页面
│   └── AboutScreen.kt            # 关于页面
└── ui/theme/                     # UI主题
    ├── Color.kt                  # 颜色定义
    ├── Theme.kt                  # 主题配置
    └── Type.kt                   # 字体样式
```

## 数据存储

应用使用Android的SharedPreferences进行本地数据存储：

- **习惯数据**: 存储在`habits_data`键中，JSON格式
- **打卡记录**: 存储在`check_data`键中，按日期组织
- **数据安全**: 所有数据仅存储在本地设备，不会上传到云端

## 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建您的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交您的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开一个Pull Request

## 许可证

本项目采用 MIT 许可证 - 详情请查看 [LICENSE](LICENSE) 文件

## 致谢

感谢以下开源项目为本应用的开发提供支持：

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material Design](https://material.io/design)
- [Android Jetpack](https://developer.android.com/jetpack)
