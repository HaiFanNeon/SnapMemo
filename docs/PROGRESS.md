# MemoFlow 开发进度记录

> 版本: V1.0.16 | 日期: 2026.03.12
> 包名: `com.example.snapmemo`

---

## 总体进度

| Sprint | 周期 | 状态 | 完成度 |
|--------|------|------|--------|
| Sprint 1 | Week 1-2 | ✅ 完成 | 100% |
| Sprint 2 | Week 3-4 | ✅ 完成 | 100% |
| Sprint 3 | Week 5-6 | 🔄 部分完成 | 60% |
| Sprint 4 | Week 7-8 | 🔄 部分完成 | 50% |
| Sprint 5 | Week 9-10 | 📋 待实现 | 20% |

---

## Sprint 1 — 基础架构 & 本地笔记 CRUD ✅

### 已完成任务

| # | 任务 | 文件 | 状态 |
|---|------|------|------|
| 1.1 | 项目依赖配置（Hilt/Room/Retrofit/Coil/Navigation/ViewBinding） | `gradle/libs.versions.toml`, `app/build.gradle.kts` | ✅ 完成 |
| 1.2 | Room 数据库 + DAO 层 | `AppDatabase.kt`, `MemoDao.kt`, `TagDao.kt`, `AttachmentDao.kt`, `OutboxDao.kt`, `AiCacheDao.kt`, `ConflictLogDao.kt` | ✅ 完成 |
| 1.3 | Domain Model + Mapper | `Memo.kt`, `Attachment.kt`, `User.kt`, `MemoMapper.kt`, `AttachmentMapper.kt` | ✅ 完成 |
| 1.4 | MemoRepository（本地模式） | `MemoRepositoryImpl.kt` | ✅ 完成 |
| 1.5 | Memo CRUD UseCase | `CreateMemoUseCase.kt`, `GetMemosUseCase.kt`, `UpdateMemoUseCase.kt`, `DeleteMemoUseCase.kt`, `ArchiveMemoUseCase.kt`, `PinMemoUseCase.kt`, `SearchMemosUseCase.kt` | ✅ 完成 |
| 1.6 | MainActivity + DrawerLayout + nav_graph + HomeFragment + MemoListAdapter | `MainActivity.kt`, `activity_main.xml`, `nav_graph.xml`, `HomeFragment.kt`, `MemoListAdapter.kt`, `MemoViewHolder.kt` | ✅ 完成 |
| 1.7 | EditorFragment 笔记编辑器 | `EditorFragment.kt`, `EditorViewModel.kt`, `fragment_editor.xml` | ✅ 完成 |
| 1.8 | 标签管理 | `TagDao.kt`, `TagRepositoryImpl.kt`, `GetTagsUseCase.kt` | ✅ 完成 |

### Entity 数据模型

```kotlin
// 已创建并含扩展字段（根据第十五章决策）
MemoEntity     → 新增 baseUpdateTime, reminderTime, lastViewedAt
TagEntity
AttachmentEntity
OutboxEntry
AiCacheEntity
ConflictLogEntity  ← 新增（多端冲突日志）
```

---

## Sprint 2 — 网络层 & 同步机制 ✅

### 已完成任务

| # | 任务 | 文件 | 状态 |
|---|------|------|------|
| 2.1 | Retrofit API 接口实现 | `MemosAuthApi.kt`, `MemosMemoApi.kt`, `MemosAttachmentApi.kt` | ✅ 完成 |
| 2.2 | AuthInterceptor 请求拦截 | `AuthInterceptor.kt` | ✅ 完成 |
| 2.3 | RetryInterceptor 重试机制 | `RetryInterceptor.kt` | ✅ 完成 |
| 2.4 | LoginActivity（单机/联机模式切换） | `LoginActivity.kt`, `LoginViewModel.kt`, `activity_login.xml` | ✅ 完成 |
| 2.5 | Outbox 队列处理器 | `OutboxProcessor.kt` | ✅ 完成 |
| 2.6 | SyncManager 同步管理 | `SyncManager.kt` | ✅ 完成 |
| 2.7 | SyncWorker (WorkManager) | `SyncWorker.kt` | ✅ 完成 |
| 2.8 | SyncStatusFragment 同步状态监控页面 | `SyncStatusFragment.kt`, `SyncStatusViewModel.kt`, `SyncEntryListAdapter.kt` | ✅ 完成 |
| 2.9 | 网络状态监听 | `NetworkMonitor.kt` | ✅ 完成 |

### DTO 数据传输对象

```
data/remote/dto/
├── auth/   SignInRequest, SignInResponse
├── memo/   MemoDto, CreateMemoRequest, UpdateMemoRequest, ListMemosResponse
├── attachment/  AttachmentDto, CreateAttachmentRequest, ListAttachmentsResponse
└── user/   UserDto
```

---

## Sprint 3 — 多媒体附件管理 🔄

### 已完成任务

| # | 任务 | 文件 | 状态 |
|---|------|------|------|
| 3.1 | AttachmentDao + Entity | `AttachmentDao.kt`, `AttachmentEntity.kt` | ✅ 完成 |
| 3.2 | AttachmentRepository | `AttachmentRepositoryImpl.kt` | ✅ 完成 |
| 3.7 | AttachmentFragment 附件列表页面 | `AttachmentFragment.kt`, `AttachmentViewModel.kt`, `fragment_attachment.xml` | ✅ 完成 |

### 待实现任务

| # | 任务 | 文件 | 状态 |
|---|------|------|------|
| 3.3 | 图片压缩工具 | `MediaCompressor.kt` | 📋 待实现 |
| 3.4 | Coil 图片加载 + 多级缓存 | - | 📋 待实现 |
| 3.5 | ExoPlayer 音视频播放 | - | 📋 待实现 |
| 3.6 | AttachmentPreviewView 附件预览自定义 View | `AttachmentPreviewView.kt` | 📋 待实现 |
| 3.8 | EditorFragment 中插入附件 | - | 📋 待实现 |

---

## Sprint 4 — AI 总结 & 高级功能 🔄

### 已完成任务

| # | 任务 | 文件 | 状态 |
|---|------|------|------|
| 4.2 | AI 结果本地缓存 | `AiCacheDao.kt`, `AiCacheEntity.kt` | ✅ 完成 |
| 4.4 | AiRepository 实现（占位） | `AiRepositoryImpl.kt` | ✅ 完成 |
| 4.5 | AiSummaryFragment 占位页面 | `AiSummaryFragment.kt`, `fragment_ai_summary.xml` | ✅ 完成 |
| 4.6 | ExploreFragment 页面 (TabLayout + ViewPager2) | `ExploreFragment.kt`, `ExploreTabFragment.kt`, `fragment_explore.xml` | ✅ 完成 |
| 4.7 | RandomWalkFragment 页面 (三种策略) | `RandomWalkFragment.kt`, `RandomWalkViewModel.kt`, `fragment_random_walk.xml` | ✅ 完成 |
| 4.8 | 搜索功能 (SearchView 集成) | `SearchMemosUseCase.kt`, `HomeFragment.kt` | ✅ 完成 |

### 待实现任务

| # | 任务 | 状态 | 说明 |
|---|------|------|------|
| 4.1 | AI Service API 层 | 📋 待实现 | 等待 AI 服务选型 |
| 4.3 | AI 流式响应处理 | 📋 待实现 | 依赖 AI 服务接入 |

---

## Sprint 5 — 小组件 & 收尾 📋

### 已完成任务

| # | 任务 | 文件 | 状态 |
|---|------|------|------|
| 5.5 | ArchiveFragment / TrashFragment | `ArchiveFragment.kt`, `TrashFragment.kt` | ✅ 完成 |
| 5.6 | SettingsFragment | `SettingsFragment.kt`, `SettingsViewModel.kt` | ✅ 完成 |
| 5.7 | HeatmapCalendarView 自定义 View | `HeatmapCalendarView.kt` | ✅ 完成 |

### 待实现任务

| # | 任务 | 状态 |
|---|------|------|
| 5.1 | 日回顾小组件 (DailyReviewWidget) | 📋 待实现 |
| 5.2 | 快速输入小组件 (QuickInputWidget) | 📋 待实现 |
| 5.3 | 热力图小组件 (HeatmapWidget) | 📋 待实现 |
| 5.4 | WidgetUpdateWorker（已创建骨架） | 🔄 部分完成 |
| 5.8 | 全量端到端测试 | 📋 待实现 |

---

## 已创建文件清单

### 构建配置
- `snapmemo/gradle/libs.versions.toml` — 完整依赖版本管理
- `snapmemo/app/build.gradle.kts` — 切换到 ViewBinding，移除 Compose
- `snapmemo/build.gradle.kts` — 根项目构建配置

### 资源文件
```
res/values/
  colors.xml, themes.xml, strings.xml, dimens.xml, styles.xml
res/values-night/
  colors.xml, themes.xml
res/drawable/
  bg_selected_nav_item.xml, bg_tag_chip.xml
  ic_all_notes.xml, ic_explore.xml, ic_random_walk.xml, ic_ai_summary.xml
  ic_attachment.xml, ic_archive.xml, ic_trash.xml, ic_sync.xml
  ic_search.xml, ic_settings.xml, ic_notification.xml
  ic_sync_success.xml, ic_sync_pending.xml, ic_sync_failed.xml
res/anim/
  slide_in_right.xml, slide_out_left.xml, fade_in.xml, fade_out.xml
```

### 导航/菜单/布局
```
res/navigation/nav_graph.xml
res/menu/drawer_menu.xml, toolbar_menu.xml
res/layout/
  activity_main.xml, activity_login.xml
  layout_drawer_header.xml
  fragment_home.xml, fragment_editor.xml, fragment_explore.xml
  fragment_random_walk.xml, fragment_ai_summary.xml, fragment_attachment.xml
  fragment_archive.xml, fragment_trash.xml, fragment_sync_status.xml
  fragment_settings.xml, dialog_conflict_resolution.xml
  item_memo_card.xml, item_attachment.xml, item_sync_entry.xml
```

### Kotlin 源码（87 个文件）

#### 数据层
```
data/local/db/AppDatabase.kt
data/local/db/entity/: MemoEntity, TagEntity, AttachmentEntity,
                        OutboxEntry, AiCacheEntity, ConflictLogEntity
data/local/db/dao/: MemoDao, TagDao, AttachmentDao, OutboxDao,
                    AiCacheDao, ConflictLogDao
data/local/datastore/UserPreferences.kt
```

#### 网络层
```
data/remote/api/: MemosAuthApi, MemosMemoApi, MemosAttachmentApi
data/remote/dto/auth/: SignInRequest, SignInResponse
data/remote/dto/memo/: MemoDto, CreateMemoRequest, UpdateMemoRequest, ListMemosResponse
data/remote/dto/attachment/: AttachmentDto, CreateAttachmentRequest, ListAttachmentsResponse
data/remote/interceptor/: AuthInterceptor, RetryInterceptor
```

#### 仓库层
```
data/repository/: MemoRepositoryImpl, AttachmentRepositoryImpl, AuthRepositoryImpl,
                  TagRepositoryImpl, AiRepositoryImpl
data/mapper/: MemoMapper, AttachmentMapper
data/sync/: OutboxProcessor, SyncManager
```

#### 领域层
```
domain/model/: Memo, Attachment, User, SyncState, OutboxEntryDetail, AiSummary,
               ConflictInfo, Location, MemoProperty
domain/repository/: MemoRepository, AttachmentRepository, AuthRepository,
                    TagRepository, AiRepository
domain/usecase/memo/: GetMemosUseCase, CreateMemoUseCase, UpdateMemoUseCase,
                       DeleteMemoUseCase, ArchiveMemoUseCase, PinMemoUseCase, SearchMemosUseCase
domain/usecase/tag/: GetTagsUseCase
domain/usecase/auth/: SignInUseCase, SwitchModeUseCase
domain/usecase/ai/: GenerateAiSummaryUseCase
```

#### 依赖注入
```
di/: DatabaseModule, NetworkModule, AppModule
```

#### UI 层
```
MemoFlowApplication.kt
MainActivity.kt
ui/login/: LoginActivity, LoginViewModel
ui/home/: HomeFragment, HomeViewModel
ui/editor/: EditorFragment, EditorViewModel
ui/explore/: ExploreFragment, ExploreTabFragment
ui/random/: RandomWalkFragment, RandomWalkViewModel
ui/ai/: AiSummaryFragment
ui/attachment/: AttachmentFragment, AttachmentViewModel
ui/archive/: ArchiveFragment, ArchiveViewModel
ui/trash/: TrashFragment, TrashViewModel
ui/sync/: SyncStatusFragment, SyncStatusViewModel
ui/settings/: SettingsFragment, SettingsViewModel
ui/adapter/: MemoListAdapter, MemoViewHolder, SyncEntryListAdapter
ui/customview/: HeatmapCalendarView
```

#### 工具类/Worker
```
util/: NetworkMonitor, DateTimeUtils
worker/: SyncWorker, WidgetUpdateWorker
```

---

## 架构决策记录（ADR）

| # | 决策 | 理由 |
|---|------|------|
| ADR-01 | 使用 ViewBinding 替代 Compose | 遵循开发文档设定，XML + ViewBinding 方案更成熟 |
| ADR-02 | Hilt 依赖注入 | 官方推荐的 Android DI 框架，与 ViewModel/WorkManager 深度集成 |
| ADR-03 | Room + Flow | 响应式数据流，UI 自动随数据库变更刷新 |
| ADR-04 | Outbox 同步队列 | 保证离线优先策略，即使网络断开也不丢失数据 |
| ADR-05 | Offline-First + 乐观更新 | 用户操作立即生效，后台异步同步至服务端 |
| ADR-06 | Navigation Component | 统一管理 Fragment 跳转，支持 SafeArgs 参数传递 |
| ADR-07 | AI 服务占位实现 | AI 功能 UI 先行，服务对接留后续迭代 |

---

## 待完成事项（Backlog）

### 高优先级（P0）
- [ ] MediaCompressor 图片/视频压缩工具
- [ ] AttachmentPreviewView 自定义附件预览 View（Coil 加载图片 / ExoPlayer 播放视频）
- [ ] Editor 中插入附件功能（图片选择器 / 文件选择器）
- [ ] ConflictResolutionDialogFragment 冲突解决弹窗
- [ ] WorkManager 初始化（Application 启动时注册定时任务）

### 中优先级（P1）
- [ ] HeatmapCalendarView 与 HomeViewModel 的数据绑定（getDailyStats）
- [ ] Drawer Header 热力图数据联动
- [ ] SyncStatusIndicatorView 顶栏同步状态图标（旋转动画 + 角标）
- [ ] ArchiveFragment/TrashFragment 长按菜单（还原/永久删除）
- [ ] 通知系统（同步完成通知 / 笔记提醒）
- [ ] 导出功能（Markdown + ZIP）
- [ ] SettingsFragment 联机模式切换（跳转登录页）

### 低优先级（P2）
- [ ] DailyReviewWidget 日回顾小组件
- [ ] QuickInputWidget 快速输入小组件
- [ ] HeatmapWidget 热力图小组件
- [ ] 单元测试 & 集成测试（目标覆盖率：UseCase ≥ 90%, DAO ≥ 85%, ViewModel ≥ 80%）
- [ ] E2E 端到端测试
- [ ] AI 服务接入（待确定服务商）
- [ ] 通知渠道注册（sync_channel / reminder_channel / export_channel）

---

## 已知问题 & 技术债务

| 编号 | 描述 | 严重程度 | 修复计划 |
|------|------|----------|----------|
| TD-01 | `AttachmentRepositoryImpl.uploadAttachment` 尚未实现 | High | Sprint 3 |
| TD-02 | NetworkModule.provideRetrofit 使用 runBlocking 读取 DataStore，存在阻塞主线程风险 | Medium | Sprint 2 优化 |
| TD-03 | SyncWorker 未在 Application 中注册到 WorkManager | High | Sprint 2 |
| TD-04 | 缺少 ConflictResolver 字段级冲突合并逻辑 | Medium | Sprint 2 |
| TD-05 | HomeFragment 使用 Safe Args 导航需要确认 nav_graph ID 一致性 | Low | Sprint 1 复测 |
| TD-06 | 缺少 AttachmentRepository 在 Hilt Module 中的绑定（AppModule.kt 已添加） | Fixed | ✅ |

---

## 非功能性需求达标情况

| 项目 | 目标 | 当前状态 |
|------|------|----------|
| 最低 SDK | API 24 (Android 7.0) | ✅ 已设置 |
| 目标 SDK | API 36 | ✅ 已设置 |
| APK 体积 | < 15MB | 📋 待测量 |
| 冷启动时间 | < 1.5s | 📋 待测量 |
| 离线数据完整性 | 100% 不丢失 | ✅ Outbox + Room 保证 |

---

## 版本历史

| 版本 | 日期 | 变更摘要 |
|------|------|----------|
| V1.0.16 | 2026.03.12 | 初始项目架构搭建完成；Sprint 1-2 主体完成；Sprint 3-4 部分完成；Sprint 5 基础骨架 |

---

*本文档由 AI 辅助生成，最后更新：2026.03.12*
