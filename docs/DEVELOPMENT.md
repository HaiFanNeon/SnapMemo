# MemoFlow 项目开发文档

> 版本: V1.0.16 | 日期: 2026.03.12
> 包名: `com.example.snapmemo`

---

## 一、项目概述

MemoFlow 是一款基于 Kotlin 开发的轻量级知识管理 Android 应用。采用 MVVM 架构，通过本地 SQLite 缓存 + 离线优先（Offline-First）同步策略，解决用户在弱网环境下的笔记记录痛点。后端对接开源项目 [memos](https://usememos.com) 的 REST API (`/api/v1`)。

### 1.1 运行模式

| 模式 | 说明 |
|------|------|
| **单机模式** | 数据仅存储在本地 Room 数据库，不依赖任何服务端 |
| **联机模式** | 连接到 memos 后端实例，通过 Outbox 同步重试队列将本地变更同步至服务端 |

用户可随时在设置中切换模式。从单机切换到联机时，触发全量数据同步。

### 1.2 技术栈

| 分类 | 技术选型 |
|------|----------|
| 语言 | Kotlin 2.0+ |
| UI | XML 布局 + ViewBinding + Material Design 3 |
| 架构 | MVVM + Repository Pattern + Clean Architecture |
| 页面管理 | Activity + Fragment |
| 列表 | RecyclerView + ListAdapter + DiffUtil |
| 本地持久化 | Room (SQLite) |
| 依赖注入 | Hilt |
| 网络请求 | Retrofit + OkHttp |
| 图片加载 | Coil |
| 音视频播放 | ExoPlayer (Media3) |
| 后台任务 | WorkManager |
| 异步处理 | Kotlin Coroutines + Flow |
| 导航 | Navigation Component (Fragment) |
| 测试 | JUnit5 + MockK + Turbine + Espresso + Robolectric |

---

## 二、系统架构设计

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                     │
│  ┌───────────┐ ┌──────────┐ ┌──────────┐ ┌───────────┐ │
│  │ Activities │ │Fragments │ │ ViewModels│ │  Widgets  │ │
│  │ + XML      │ │+ Adapters│ │ + LiveData│ │           │ │
│  └─────┬─────┘ └────┬─────┘ └─────┬─────┘ └─────┬─────┘ │
├────────┴─────────────┴─────────────┴─────────────┴───────┤
│                     Domain Layer                          │
│  ┌──────────┐ ┌──────────────┐ ┌─────────────────────┐  │
│  │ UseCases │ │  Repository  │ │   Domain Models     │  │
│  │          │ │  Interfaces  │ │                     │  │
│  └────┬─────┘ └──────┬───────┘ └─────────────────────┘  │
├───────┴──────────────┴───────────────────────────────────┤
│                      Data Layer                           │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────┐  │
│  │   Room   │ │ Retrofit │ │  Outbox  │ │WorkManager│  │
│  │   DAO    │ │  API     │ │  Queue   │ │  Workers  │  │
│  └──────────┘ └──────────┘ └──────────┘ └───────────┘  │
└─────────────────────────────────────────────────────────┘
```

### 2.2 包结构

```
com.example.snapmemo/
├── di/                          # Hilt 依赖注入模块
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   └── AiModule.kt
├── data/
│   ├── local/
│   │   ├── db/
│   │   │   ├── AppDatabase.kt
│   │   │   ├── dao/
│   │   │   │   ├── MemoDao.kt
│   │   │   │   ├── TagDao.kt
│   │   │   │   ├── AttachmentDao.kt
│   │   │   │   ├── OutboxDao.kt
│   │   │   │   └── AiCacheDao.kt
│   │   │   └── entity/
│   │   │       ├── MemoEntity.kt
│   │   │       ├── TagEntity.kt
│   │   │       ├── AttachmentEntity.kt
│   │   │       ├── OutboxEntry.kt
│   │   │       └── AiCacheEntity.kt
│   │   └── datastore/
│   │       └── UserPreferences.kt
│   ├── remote/
│   │   ├── api/
│   │   │   ├── MemosAuthApi.kt
│   │   │   ├── MemosMemoApi.kt
│   │   │   ├── MemosAttachmentApi.kt
│   │   │   ├── MemosUserApi.kt
│   │   │   └── AiServiceApi.kt
│   │   ├── dto/
│   │   │   ├── auth/
│   │   │   │   ├── SignInRequest.kt
│   │   │   │   └── SignInResponse.kt
│   │   │   ├── memo/
│   │   │   │   ├── MemoDto.kt
│   │   │   │   ├── CreateMemoRequest.kt
│   │   │   │   ├── UpdateMemoRequest.kt
│   │   │   │   └── ListMemosResponse.kt
│   │   │   ├── attachment/
│   │   │   │   ├── AttachmentDto.kt
│   │   │   │   ├── CreateAttachmentRequest.kt
│   │   │   │   └── ListAttachmentsResponse.kt
│   │   │   ├── user/
│   │   │   │   └── UserDto.kt
│   │   │   └── ai/
│   │   │       ├── AiSummaryRequest.kt
│   │   │       └── AiSummaryResponse.kt
│   │   └── interceptor/
│   │       ├── AuthInterceptor.kt
│   │       └── RetryInterceptor.kt
│   ├── repository/
│   │   ├── MemoRepositoryImpl.kt
│   │   ├── AttachmentRepositoryImpl.kt
│   │   ├── AuthRepositoryImpl.kt
│   │   ├── TagRepositoryImpl.kt
│   │   └── AiRepositoryImpl.kt
│   ├── mapper/
│   │   ├── MemoMapper.kt
│   │   ├── AttachmentMapper.kt
│   │   └── UserMapper.kt
│   └── sync/
│       ├── SyncManager.kt
│       ├── SyncWorker.kt
│       ├── OutboxProcessor.kt
│       └── ConflictResolver.kt
├── domain/
│   ├── model/
│   │   ├── Memo.kt
│   │   ├── Tag.kt
│   │   ├── Attachment.kt
│   │   ├── User.kt
│   │   ├── SyncStatus.kt
│   │   └── AiSummary.kt
│   ├── repository/
│   │   ├── MemoRepository.kt
│   │   ├── AttachmentRepository.kt
│   │   ├── AuthRepository.kt
│   │   ├── TagRepository.kt
│   │   └── AiRepository.kt
│   └── usecase/
│       ├── memo/
│       │   ├── GetMemosUseCase.kt
│       │   ├── CreateMemoUseCase.kt
│       │   ├── UpdateMemoUseCase.kt
│       │   ├── DeleteMemoUseCase.kt
│       │   ├── ArchiveMemoUseCase.kt
│       │   ├── PinMemoUseCase.kt
│       │   └── SearchMemosUseCase.kt
│       ├── tag/
│       │   ├── GetTagsUseCase.kt
│       │   └── ManageTagsUseCase.kt
│       ├── attachment/
│       │   ├── UploadAttachmentUseCase.kt
│       │   └── CompressMediaUseCase.kt
│       ├── auth/
│       │   ├── SignInUseCase.kt
│       │   └── SwitchModeUseCase.kt
│       ├── sync/
│       │   ├── SyncDataUseCase.kt
│       │   └── GetSyncStatusUseCase.kt
│       └── ai/
│           └── GenerateAiSummaryUseCase.kt
├── ui/
│   ├── adapter/                     # RecyclerView Adapters
│   │   ├── MemoListAdapter.kt
│   │   ├── MemoViewHolder.kt
│   │   ├── AttachmentListAdapter.kt
│   │   ├── AttachmentViewHolder.kt
│   │   ├── SyncEntryListAdapter.kt
│   │   ├── TagListAdapter.kt
│   │   └── OutboxEntryViewHolder.kt
│   ├── customview/                  # 自定义 View
│   │   ├── HeatmapCalendarView.kt
│   │   ├── SyncStatusIndicatorView.kt
│   │   └── AttachmentPreviewView.kt
│   ├── login/
│   │   ├── LoginActivity.kt
│   │   └── LoginViewModel.kt
│   ├── home/
│   │   ├── HomeFragment.kt
│   │   ├── HomeViewModel.kt
│   │   └── DrawerHelper.kt
│   ├── editor/
│   │   ├── EditorFragment.kt
│   │   └── EditorViewModel.kt
│   ├── explore/
│   │   ├── ExploreFragment.kt
│   │   └── ExploreViewModel.kt
│   ├── random/
│   │   ├── RandomWalkFragment.kt
│   │   └── RandomWalkViewModel.kt
│   ├── ai/
│   │   ├── AiSummaryFragment.kt
│   │   └── AiSummaryViewModel.kt
│   ├── attachment/
│   │   ├── AttachmentFragment.kt
│   │   └── AttachmentViewModel.kt
│   ├── archive/
│   │   ├── ArchiveFragment.kt
│   │   └── ArchiveViewModel.kt
│   ├── trash/
│   │   ├── TrashFragment.kt
│   │   └── TrashViewModel.kt
│   ├── sync/
│   │   ├── SyncStatusFragment.kt
│   │   └── SyncStatusViewModel.kt
│   ├── settings/
│   │   ├── SettingsFragment.kt
│   │   └── SettingsViewModel.kt
│   ├── conflict/
│   │   ├── ConflictResolutionDialogFragment.kt
│   │   └── ConflictDiffView.kt
│   └── widget/
│       ├── DailyReviewWidget.kt
│       ├── QuickInputWidget.kt
│       └── HeatmapWidget.kt
├── worker/
│   ├── SyncWorker.kt
│   ├── WidgetUpdateWorker.kt
│   └── MediaCompressWorker.kt
├── util/
│   ├── NetworkMonitor.kt
│   ├── DateTimeUtils.kt
│   ├── ViewBindingDelegate.kt       # Fragment ViewBinding 委托
│   └── MediaCompressor.kt
├── MemoFlowApplication.kt
└── MainActivity.kt                   # 宿主 Activity（DrawerLayout + NavHostFragment）

res/
├── layout/
│   ├── activity_main.xml             # DrawerLayout + NavHostFragment + BottomNav
│   ├── activity_login.xml
│   ├── fragment_home.xml             # RecyclerView + SwipeRefreshLayout + FAB
│   ├── fragment_editor.xml           # EditText + Toolbar + 附件区
│   ├── fragment_explore.xml          # TabLayout + ViewPager2
│   ├── fragment_random_walk.xml      # 策略选择 Spinner + 笔记卡片
│   ├── fragment_ai_summary.xml       # 占位 UI
│   ├── fragment_attachment.xml       # GridRecyclerView
│   ├── fragment_archive.xml          # RecyclerView
│   ├── fragment_trash.xml            # RecyclerView
│   ├── fragment_sync_status.xml      # 分组 RecyclerView
│   ├── fragment_settings.xml         # PreferenceScreen 样式
│   ├── item_memo_card.xml            # 笔记卡片 item
│   ├── item_attachment.xml           # 附件 item
│   ├── item_sync_entry.xml           # 同步条目 item
│   ├── item_tag_chip.xml             # 标签胶囊 item
│   ├── dialog_conflict_resolution.xml
│   ├── layout_drawer_header.xml      # 侧边栏头部（统计 + 热力图）
│   └── layout_drawer_content.xml     # 侧边栏导航菜单
├── menu/
│   ├── drawer_menu.xml               # 侧边栏菜单项
│   └── toolbar_menu.xml              # Toolbar 搜索/同步/通知/设置
├── navigation/
│   └── nav_graph.xml                 # Navigation Component 导航图
├── values/
│   ├── colors.xml
│   ├── strings.xml
│   ├── themes.xml
│   ├── dimens.xml
│   └── styles.xml
├── values-night/
│   ├── colors.xml
│   └── themes.xml
├── drawable/
│   ├── ic_all_notes.xml
│   ├── ic_explore.xml
│   ├── ic_random_walk.xml
│   ├── ic_ai_summary.xml
│   ├── ic_attachment.xml
│   ├── ic_archive.xml
│   ├── ic_trash.xml
│   ├── ic_sync.xml
│   ├── ic_sync_success.xml
│   ├── ic_sync_pending.xml
│   ├── ic_sync_failed.xml
│   ├── ic_notification.xml
│   ├── ic_settings.xml
│   ├── ic_search.xml
│   ├── bg_selected_nav_item.xml      # 选中项红色圆角背景
│   └── bg_tag_chip.xml               # 标签胶囊背景
├── xml/
│   ├── widget_daily_review.xml
│   ├── widget_quick_input.xml
│   ├── widget_heatmap.xml
│   ├── backup_rules.xml
│   └── data_extraction_rules.xml
└── anim/
    ├── slide_in_right.xml
    ├── slide_out_left.xml
    ├── fade_in.xml
    └── fade_out.xml
```

---

## 三、数据模型设计

### 3.1 Room Entity 定义

#### MemoEntity

```kotlin
@Entity(tableName = "memos")
data class MemoEntity(
    @PrimaryKey
    val id: String,                    // 对应 memos API 的 name 字段 (memos/{id})
    val serverId: String? = null,      // memos 后端真实 ID（联机模式下有值）
    val content: String,               // Markdown 内容
    val state: MemoState,              // NORMAL / ARCHIVED
    val visibility: MemoVisibility,    // PRIVATE / PROTECTED / PUBLIC
    val pinned: Boolean = false,
    val creatorName: String? = null,
    val snippet: String? = null,
    val displayTime: Long,             // 展示时间戳
    val createTime: Long,
    val updateTime: Long,
    val hasLink: Boolean = false,
    val hasTaskList: Boolean = false,
    val hasCode: Boolean = false,
    val hasIncompleteTasks: Boolean = false,
    val parentId: String? = null,
    val locationPlaceholder: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,  // 同步状态
    val isDeleted: Boolean = false     // 软删除标记
)

enum class MemoState { NORMAL, ARCHIVED }
enum class MemoVisibility { PRIVATE, PROTECTED, PUBLIC }
enum class SyncStatus { SYNCED, PENDING_CREATE, PENDING_UPDATE, PENDING_DELETE, CONFLICT, FAILED }
```

#### TagEntity

```kotlin
@Entity(
    tableName = "memo_tags",
    primaryKeys = ["memoId", "tag"]
)
data class TagEntity(
    val memoId: String,
    val tag: String
)
```

#### AttachmentEntity

```kotlin
@Entity(tableName = "attachments")
data class AttachmentEntity(
    @PrimaryKey
    val id: String,
    val serverId: String? = null,
    val memoId: String?,
    val filename: String,
    val mimeType: String,
    val size: Long,
    val localPath: String?,       // 本地文件路径
    val externalLink: String?,    // memos 服务端 URL
    val createTime: Long,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
```

#### OutboxEntry

```kotlin
@Entity(tableName = "outbox")
data class OutboxEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entityType: EntityType,        // MEMO / ATTACHMENT
    val entityId: String,              // 本地实体 ID
    val operation: OutboxOperation,    // CREATE / UPDATE / DELETE
    val payload: String,               // JSON 序列化的请求体
    val retryCount: Int = 0,
    val maxRetries: Int = 5,
    val lastError: String? = null,
    val status: OutboxStatus = OutboxStatus.PENDING,
    val createdAt: Long,
    val nextRetryAt: Long,             // 指数退避的下次重试时间
    val completedAt: Long? = null
)

enum class EntityType { MEMO, ATTACHMENT, TAG }
enum class OutboxOperation { CREATE, UPDATE, DELETE }
enum class OutboxStatus { PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED }
```

#### AiCacheEntity

```kotlin
@Entity(tableName = "ai_cache")
data class AiCacheEntity(
    @PrimaryKey
    val memoId: String,
    val summary: String,
    val keywords: String,         // JSON array
    val sentiment: String?,
    val generatedAt: Long,
    val modelVersion: String
)
```

### 3.2 Domain Model

```kotlin
data class Memo(
    val id: String,
    val content: String,
    val state: MemoState,
    val visibility: MemoVisibility,
    val pinned: Boolean,
    val tags: List<String>,
    val attachments: List<Attachment>,
    val displayTime: Instant,
    val createTime: Instant,
    val updateTime: Instant,
    val snippet: String?,
    val property: MemoProperty,
    val syncStatus: SyncStatus,
    val location: Location?
)

data class MemoProperty(
    val hasLink: Boolean,
    val hasTaskList: Boolean,
    val hasCode: Boolean,
    val hasIncompleteTasks: Boolean
)

data class Attachment(
    val id: String,
    val filename: String,
    val mimeType: String,
    val size: Long,
    val localPath: String?,
    val remoteUrl: String?,
    val createTime: Instant
)

data class SyncState(
    val totalPending: Int,
    val totalFailed: Int,
    val totalCompleted: Int,
    val entries: List<OutboxEntryDetail>,
    val isSyncing: Boolean,
    val lastSyncTime: Instant?
)

data class OutboxEntryDetail(
    val id: Long,
    val entityType: EntityType,
    val operation: OutboxOperation,
    val status: OutboxStatus,
    val retryCount: Int,
    val lastError: String?,
    val createdAt: Instant
)
```

---

## 四、Memos REST API 接口定义

> 基础路径: `{BASE_URL}/api/v1`
> 认证方式: `Authorization: Bearer <ACCESS_TOKEN>`

### 4.1 Auth Service — 认证服务

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/auth/signin` | 用户登录，获取 accessToken |
| `POST` | `/auth/signout` | 退出登录，清除 cookie |
| `POST` | `/auth/refresh` | 刷新 accessToken |
| `GET` | `/auth/status` | 获取当前认证状态 |

#### SignIn

```
POST /api/v1/auth/signin
Content-Type: application/json

{
  "passwordCredentials": {
    "username": "string",
    "password": "string"
  }
}

--- Response 200 ---
{
  "user": {
    "name": "users/{userId}",
    "role": "USER",
    "username": "string",
    "email": "string",
    "displayName": "string",
    "avatarUrl": "string",
    "state": "NORMAL",
    "createTime": "2026-03-12T00:00:00Z",
    "updateTime": "2026-03-12T00:00:00Z"
  },
  "accessToken": "string",
  "accessTokenExpiresAt": "2026-03-12T01:00:00Z"
}
```

### 4.2 Memo Service — 笔记服务

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/memos` | 列出笔记（支持分页/筛选/排序） |
| `POST` | `/memos` | 创建笔记 |
| `GET` | `/memos/{memo}` | 获取单条笔记 |
| `PATCH` | `/memos/{memo}` | 更新笔记（需 updateMask） |
| `DELETE` | `/memos/{memo}` | 删除笔记 |
| `GET` | `/memos/{memo}/relations` | 获取笔记关联 |
| `PATCH` | `/memos/{memo}/relations` | 设置笔记关联 |
| `GET` | `/memos/{memo}/attachments` | 获取笔记附件 |
| `PATCH` | `/memos/{memo}/attachments` | 设置笔记附件 |
| `POST` | `/memos/{memo}/comments` | 创建笔记评论 |
| `GET` | `/memos/{memo}/comments` | 获取笔记评论 |
| `POST` | `/memos/{memo}/reactions` | 创建/更新反应 |
| `DELETE` | `/memos/{memo}/reactions/{reaction}` | 删除反应 |

#### ListMemos

```
GET /api/v1/memos?pageSize=50&pageToken=xxx&state=NORMAL&orderBy=display_time desc&filter=xxx&showDeleted=false

--- Response 200 ---
{
  "memos": [
    {
      "name": "memos/{id}",
      "state": "NORMAL",
      "creator": "users/{userId}",
      "createTime": "2026-03-12T00:00:00Z",
      "updateTime": "2026-03-12T00:00:00Z",
      "displayTime": "2026-03-12T00:00:00Z",
      "content": "# Markdown content",
      "visibility": "PRIVATE",
      "tags": ["tag1", "tag2"],
      "pinned": false,
      "attachments": [...],
      "relations": [...],
      "reactions": [...],
      "property": {
        "hasLink": false,
        "hasTaskList": false,
        "hasCode": false,
        "hasIncompleteTasks": false
      },
      "parent": "",
      "snippet": "Preview text...",
      "location": null
    }
  ],
  "nextPageToken": "string"
}
```

#### CreateMemo

```
POST /api/v1/memos
Content-Type: application/json

{
  "content": "# My Memo\nSome content here",
  "state": "NORMAL",
  "visibility": "PRIVATE",
  "pinned": false,
  "attachments": [
    { "name": "attachments/{id}" }
  ],
  "relations": [],
  "location": null
}

--- Response 200 ---
{ ... MemoDto ... }
```

#### UpdateMemo

```
PATCH /api/v1/memos/{memo}?updateMask=content,visibility,pinned
Content-Type: application/json

{
  "content": "Updated content",
  "state": "NORMAL",
  "visibility": "PRIVATE",
  "pinned": true
}

--- Response 200 ---
{ ... MemoDto ... }
```

#### DeleteMemo

```
DELETE /api/v1/memos/{memo}

--- Response 200 ---
{}
```

### 4.3 Attachment Service — 附件服务

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/attachments` | 列出附件 |
| `POST` | `/attachments` | 创建/上传附件 |
| `GET` | `/attachments/{attachment}` | 获取附件详情 |
| `PATCH` | `/attachments/{attachment}` | 更新附件 |
| `DELETE` | `/attachments/{attachment}` | 删除附件 |

#### CreateAttachment

```
POST /api/v1/attachments
Content-Type: application/json

{
  "filename": "photo.jpg",
  "type": "image/jpeg",
  "content": "<base64-encoded-bytes>",
  "memo": "memos/{id}"
}

--- Response 200 ---
{
  "name": "attachments/{id}",
  "createTime": "2026-03-12T00:00:00Z",
  "filename": "photo.jpg",
  "type": "image/jpeg",
  "size": "102400",
  "externalLink": "",
  "memo": "memos/{id}"
}
```

#### ListAttachments

```
GET /api/v1/attachments?pageSize=50&pageToken=xxx&filter=mime_type=="image/png"&orderBy=create_time desc

--- Response 200 ---
{
  "attachments": [...],
  "nextPageToken": "string",
  "totalSize": 0
}
```

### 4.4 User Service — 用户服务

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/users/{user}` | 获取用户信息 |
| `PATCH` | `/users/{user}` | 更新用户信息 |
| `POST` | `/users/{user}/access-tokens` | 创建 Personal Access Token |
| `GET` | `/users/{user}/access-tokens` | 列出 Access Token |
| `DELETE` | `/users/{user}/access-tokens/{token}` | 删除 Access Token |

### 4.5 Retrofit 接口定义

```kotlin
// MemosAuthApi.kt
interface MemosAuthApi {
    @POST("api/v1/auth/signin")
    suspend fun signIn(@Body request: SignInRequest): SignInResponse

    @POST("api/v1/auth/signout")
    suspend fun signOut()

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(): SignInResponse
}

// MemosMemoApi.kt
interface MemosMemoApi {
    @GET("api/v1/memos")
    suspend fun listMemos(
        @Query("pageSize") pageSize: Int = 50,
        @Query("pageToken") pageToken: String? = null,
        @Query("state") state: String? = null,
        @Query("orderBy") orderBy: String? = "display_time desc",
        @Query("filter") filter: String? = null,
        @Query("showDeleted") showDeleted: Boolean = false
    ): ListMemosResponse

    @GET("api/v1/memos/{memo}")
    suspend fun getMemo(@Path("memo") memoId: String): MemoDto

    @POST("api/v1/memos")
    suspend fun createMemo(@Body request: CreateMemoRequest): MemoDto

    @PATCH("api/v1/memos/{memo}")
    suspend fun updateMemo(
        @Path("memo") memoId: String,
        @Query("updateMask") updateMask: String,
        @Body request: UpdateMemoRequest
    ): MemoDto

    @DELETE("api/v1/memos/{memo}")
    suspend fun deleteMemo(@Path("memo") memoId: String)
}

// MemosAttachmentApi.kt
interface MemosAttachmentApi {
    @GET("api/v1/attachments")
    suspend fun listAttachments(
        @Query("pageSize") pageSize: Int = 50,
        @Query("pageToken") pageToken: String? = null,
        @Query("filter") filter: String? = null,
        @Query("orderBy") orderBy: String? = null
    ): ListAttachmentsResponse

    @POST("api/v1/attachments")
    suspend fun createAttachment(@Body request: CreateAttachmentRequest): AttachmentDto

    @GET("api/v1/attachments/{attachment}")
    suspend fun getAttachment(@Path("attachment") attachmentId: String): AttachmentDto

    @DELETE("api/v1/attachments/{attachment}")
    suspend fun deleteAttachment(@Path("attachment") attachmentId: String)
}
```

---

## 五、Outbox 同步机制设计

### 5.1 同步流程

```
用户操作 (CRUD)
    │
    ▼
┌──────────────────┐
│  写入 Room 本地库  │  ← 立即生效，UI 即时响应
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ 写入 Outbox 队列  │  ← 记录待同步操作
└────────┬─────────┘
         │
         ▼
┌──────────────────────────────────┐
│     OutboxProcessor (协程)        │
│  • 监听网络状态 (NetworkMonitor)   │
│  • 网络可用时逐条处理 Outbox       │
│  • 调用 Memos API 同步            │
│  • 成功: 标记 COMPLETED           │
│  • 失败: 递增 retryCount          │
│         计算指数退避下次重试时间     │
│  • 超过 maxRetries: 标记 FAILED   │
└──────────────────────────────────┘
         │
         ▼
┌──────────────────────────────────┐
│   SyncWorker (WorkManager)       │
│   定期触发同步（约束: 有网络时）    │
│   作为兜底机制确保数据最终一致      │
└──────────────────────────────────┘
```

### 5.2 指数退避策略

```kotlin
fun calculateNextRetryDelay(retryCount: Int): Duration {
    val baseDelay = 5.seconds
    val maxDelay = 30.minutes
    val delay = baseDelay * (2.0.pow(retryCount))
    return minOf(delay, maxDelay) + Random.nextLong(0, 1000).milliseconds // jitter
}
```

### 5.3 同步状态监控 UI

用户可通过首页顶部的同步图标（⟲）进入同步状态页面：

| 状态 | 图标 | 说明 |
|------|------|------|
| 全部同步完成 | ✓ 绿色 | 无待同步条目 |
| 同步中 | ⟲ 旋转动画 | 正在处理 Outbox |
| 有待同步条目 | ⟲ 橙色 + 数字角标 | 等待网络/等待重试 |
| 有失败条目 | ⟲ 红色 + 数字角标 | 存在同步失败的记录 |

**同步详情页面 (SyncStatusFragment)**：
- 分组展示：处理中 / 等待中 / 失败 / 已完成
- 每条记录显示：操作类型、实体摘要、重试次数、最后错误信息、下次重试时间
- 失败记录可手动"立即重试"或"取消同步"
- 底部显示上次成功同步时间

### 5.4 冲突解决策略

```
服务端数据 vs 本地数据冲突判定：
1. 拉取服务端数据时，比较 updateTime
2. 如果服务端 updateTime > 本地 updateTime 且本地有未同步修改 → 冲突
3. 冲突策略：默认服务端优先（Last-Write-Wins），保留本地版本为草稿
```

---

## 六、UI 设计规范

### 6.1 页面结构（基于截图分析）

#### 首页（侧边栏 + 内容区）

```
┌────────────────────────────────────────┐
│  本地库        [热力图] [同步] [通知] [设置]  🔍  │
├────────────────────────────────────────┤
│  0          0          0                │
│  笔记        标签        天               │
├────────────────────────────────────────┤
│  ┌────────────────────────────────┐    │
│  │     活跃热力图 (Heatmap)        │    │
│  │  11月    1月    3月              │    │
│  └────────────────────────────────┘    │
├────────────────────────────────────────┤
│  ■ 全部笔记  ← 高亮选中（#D4533B 红色背景）│
│  🌐 探索                               │
│  ✏️ 随机漫步                            │
│  🤖 AI 总结                            │
│  📎 附件                               │
│  📥 归档                               │
├────────────────────────────────────────┤
│  全部标签                    [管理]      │
│  暂无标签                               │
├────────────────────────────────────────┤
│  🗑️ 回收站                             │
│  V1.0.16 | 2026.03.12                  │
└────────────────────────────────────────┘
```

#### 页面导航矩阵

| 页面 | Fragment / Activity | nav_graph ID | 布局文件 | 触发入口 |
|------|---------------------|-------------|----------|----------|
| 登录页 | `LoginActivity` | — (独立 Activity) | `activity_login.xml` | 首次启动/联机模式 |
| 首页（笔记列表）| `HomeFragment` | `homeFragment` | `fragment_home.xml` | 侧边栏"全部笔记" |
| 笔记编辑器 | `EditorFragment` | `editorFragment` | `fragment_editor.xml` | 新建/点击笔记 (arg: `memoId?`) |
| 探索 | `ExploreFragment` | `exploreFragment` | `fragment_explore.xml` | 侧边栏"探索" |
| 随机漫步 | `RandomWalkFragment` | `randomWalkFragment` | `fragment_random_walk.xml` | 侧边栏"随机漫步" |
| AI 总结 | `AiSummaryFragment` | `aiSummaryFragment` | `fragment_ai_summary.xml` | 侧边栏"AI 总结" |
| 附件管理 | `AttachmentFragment` | `attachmentFragment` | `fragment_attachment.xml` | 侧边栏"附件" |
| 归档 | `ArchiveFragment` | `archiveFragment` | `fragment_archive.xml` | 侧边栏"归档" |
| 回收站 | `TrashFragment` | `trashFragment` | `fragment_trash.xml` | 侧边栏"回收站" |
| 同步状态 | `SyncStatusFragment` | `syncStatusFragment` | `fragment_sync_status.xml` | 顶栏同步图标 |
| 设置 | `SettingsFragment` | `settingsFragment` | `fragment_settings.xml` | 顶栏齿轮图标 |

#### Navigation Graph 结构

```xml
<!-- res/navigation/nav_graph.xml -->
<navigation
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    app:startDestination="@id/homeFragment">

    <fragment android:id="@+id/homeFragment"
        android:name="com.example.snapmemo.ui.home.HomeFragment"
        android:label="全部笔记">
        <action android:id="@+id/action_home_to_editor"
            app:destination="@id/editorFragment" />
    </fragment>

    <fragment android:id="@+id/editorFragment"
        android:name="com.example.snapmemo.ui.editor.EditorFragment"
        android:label="编辑笔记">
        <argument android:name="memoId"
            app:argType="string"
            app:nullable="true"
            android:defaultValue="@null" />
    </fragment>

    <fragment android:id="@+id/exploreFragment" ... />
    <fragment android:id="@+id/randomWalkFragment" ... />
    <fragment android:id="@+id/aiSummaryFragment" ... />
    <fragment android:id="@+id/attachmentFragment" ... />
    <fragment android:id="@+id/archiveFragment" ... />
    <fragment android:id="@+id/trashFragment" ... />
    <fragment android:id="@+id/syncStatusFragment" ... />
    <fragment android:id="@+id/settingsFragment" ... />
</navigation>
```

### 6.2 配色方案

```xml
<!-- res/values/colors.xml -->
<resources>
    <!-- 主色调 - 基于截图中的暖红色系 -->
    <color name="primary">#D4533B</color>              <!-- 侧边栏选中项背景 -->
    <color name="primary_variant">#B8422E</color>
    <color name="on_primary">#FFFFFF</color>

    <color name="background">#F8F6F3</color>           <!-- 整体浅暖灰背景 -->
    <color name="surface">#FFFFFF</color>               <!-- 卡片/面板背景 -->
    <color name="on_background">#1A1A1A</color>         <!-- 主文字 -->
    <color name="on_surface">#333333</color>

    <color name="text_secondary">#888888</color>        <!-- 次要文字 -->
    <color name="divider">#E8E4E0</color>               <!-- 分割线 -->

    <color name="sync_success">#4CAF50</color>          <!-- 同步成功 -->
    <color name="sync_pending">#FF9800</color>          <!-- 待同步 -->
    <color name="sync_failed">#F44336</color>           <!-- 同步失败 -->

    <!-- 热力图颜色梯度 -->
    <color name="heatmap_level_0">#EBEDF0</color>
    <color name="heatmap_level_1">#F4C6BC</color>
    <color name="heatmap_level_2">#E8907D</color>
    <color name="heatmap_level_3">#D4533B</color>
    <color name="heatmap_level_4">#8B2E1E</color>
</resources>
```

```xml
<!-- res/values/themes.xml -->
<resources>
    <style name="Theme.MemoFlow" parent="Theme.Material3.Light.NoActionBar">
        <item name="colorPrimary">@color/primary</item>
        <item name="colorPrimaryVariant">@color/primary_variant</item>
        <item name="colorOnPrimary">@color/on_primary</item>
        <item name="android:colorBackground">@color/background</item>
        <item name="colorSurface">@color/surface</item>
        <item name="colorOnSurface">@color/on_surface</item>
    </style>
</resources>
```

### 6.3 关键组件规范

| 组件 | 类型 | 布局文件 | 说明 |
|------|------|----------|------|
| `HeatmapCalendarView` | 自定义 View (Canvas) | 内嵌于 `layout_drawer_header.xml` | 类 GitHub 贡献热力图，继承 View，通过 Canvas 绘制网格，颜色深浅表示当天笔记数量 |
| `MemoListAdapter` | ListAdapter + DiffUtil | `item_memo_card.xml` | 笔记列表适配器，MaterialCardView 卡片布局，显示内容预览、标签 ChipGroup、附件缩略图、时间、同步状态图标 |
| `SyncStatusIndicatorView` | 自定义 View | Toolbar menu item | 顶栏同步状态图标，支持旋转动画 (RotateAnimation) + 角标 (BadgeDrawable) |
| `TagListAdapter` | ListAdapter | `item_tag_chip.xml` | 标签列表，使用 Material Chip 组件，支持点击筛选 |
| `AttachmentPreviewView` | 自定义 View | `item_attachment.xml` | 附件预览，图片用 Coil 加载缩略图 / 音视频显示播放按钮 / 其他文件显示图标 |
| `SyncEntryListAdapter` | ListAdapter + DiffUtil | `item_sync_entry.xml` | 同步条目列表，分组 Header + 条目，显示操作类型/摘要/重试次数/错误信息 |

### 6.4 关键布局结构

#### MainActivity (DrawerLayout 宿主)

```xml
<!-- activity_main.xml -->
<androidx.drawerlayout.widget.DrawerLayout>

    <!-- 主内容区 -->
    <androidx.coordinatorlayout.widget.CoordinatorLayout>
        <com.google.android.material.appbar.AppBarLayout>
            <com.google.android.material.appbar.MaterialToolbar />
        </com.google.android.material.appbar.AppBarLayout>

        <androidx.fragment.app.FragmentContainerView
            android:id="@+id/nav_host_fragment"
            android:name="androidx.navigation.fragment.NavHostFragment"
            app:navGraph="@navigation/nav_graph"
            app:defaultNavHost="true" />

        <com.google.android.material.floatingactionbutton.FloatingActionButton
            android:id="@+id/fab_new_memo" />
    </androidx.coordinatorlayout.widget.CoordinatorLayout>

    <!-- 侧边栏 -->
    <com.google.android.material.navigation.NavigationView
        android:id="@+id/nav_view"
        app:headerLayout="@layout/layout_drawer_header"
        app:menu="@menu/drawer_menu" />

</androidx.drawerlayout.widget.DrawerLayout>
```

#### HomeFragment (笔记列表)

```xml
<!-- fragment_home.xml -->
<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rv_memos"
        app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager" />
</androidx.swiperefreshlayout.widget.SwipeRefreshLayout>
```

#### 笔记卡片 item

```xml
<!-- item_memo_card.xml -->
<com.google.android.material.card.MaterialCardView
    style="@style/Widget.Material3.CardView.Elevated">

    <LinearLayout android:orientation="vertical">
        <TextView android:id="@+id/tv_content" />       <!-- 内容预览 -->
        <com.google.android.material.chip.ChipGroup
            android:id="@+id/chip_group_tags" />          <!-- 标签 -->
        <LinearLayout android:orientation="horizontal">
            <ImageView android:id="@+id/iv_sync_status" /> <!-- 同步状态图标 -->
            <TextView android:id="@+id/tv_time" />        <!-- 时间 -->
            <ImageView android:id="@+id/iv_pinned" />     <!-- 置顶标记 -->
        </LinearLayout>
    </LinearLayout>

</com.google.android.material.card.MaterialCardView>
```

---

## 七、功能模块 TDD 开发计划

### 7.1 Sprint 1 — 基础架构 & 本地笔记 CRUD（Week 1-2）

#### 任务清单

| # | 任务 | 测试类 | 优先级 |
|---|------|--------|--------|
| 1.1 | 项目依赖配置（Hilt/Room/Retrofit/Coil/Navigation Component/ViewBinding） | — | P0 |
| 1.2 | Room 数据库 + DAO 层 | `MemoDaoTest` | P0 |
| 1.3 | Domain Model + Mapper | `MemoMapperTest` | P0 |
| 1.4 | MemoRepository（本地模式） | `MemoRepositoryTest` | P0 |
| 1.5 | Memo CRUD UseCase | `CreateMemoUseCaseTest`, `GetMemosUseCaseTest` 等 | P0 |
| 1.6 | MainActivity + DrawerLayout + nav_graph + HomeFragment + MemoListAdapter | `HomeFragmentTest` | P0 |
| 1.7 | EditorFragment 笔记编辑器 | `EditorFragmentTest` | P0 |
| 1.8 | 标签管理 | `TagDaoTest`, `GetTagsUseCaseTest` | P1 |

#### TDD 示例 — MemoDaoTest

```kotlin
@RunWith(RobolectricTestRunner::class)
class MemoDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var memoDao: MemoDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        memoDao = db.memoDao()
    }

    @After
    fun teardown() { db.close() }

    @Test
    fun `insert memo and retrieve by id`() = runTest {
        val memo = createTestMemoEntity(id = "test-1", content = "Hello World")
        memoDao.insert(memo)
        val result = memoDao.getById("test-1")
        assertThat(result).isNotNull()
        assertThat(result!!.content).isEqualTo("Hello World")
    }

    @Test
    fun `list memos ordered by displayTime descending`() = runTest {
        val memo1 = createTestMemoEntity(id = "1", displayTime = 1000L)
        val memo2 = createTestMemoEntity(id = "2", displayTime = 2000L)
        memoDao.insertAll(listOf(memo1, memo2))

        val results = memoDao.getAll().first()
        assertThat(results).hasSize(2)
        assertThat(results[0].id).isEqualTo("2")
    }

    @Test
    fun `soft delete sets isDeleted flag`() = runTest {
        val memo = createTestMemoEntity(id = "1")
        memoDao.insert(memo)
        memoDao.softDelete("1")

        val result = memoDao.getById("1")
        assertThat(result!!.isDeleted).isTrue()

        val activeList = memoDao.getActiveNormal().first()
        assertThat(activeList).isEmpty()
    }

    @Test
    fun `filter by state returns only matching memos`() = runTest {
        val normal = createTestMemoEntity(id = "1", state = MemoState.NORMAL)
        val archived = createTestMemoEntity(id = "2", state = MemoState.ARCHIVED)
        memoDao.insertAll(listOf(normal, archived))

        val normalList = memoDao.getByState(MemoState.NORMAL).first()
        assertThat(normalList).hasSize(1)
        assertThat(normalList[0].id).isEqualTo("1")
    }

    @Test
    fun `search by content returns matching memos`() = runTest {
        val memo1 = createTestMemoEntity(id = "1", content = "Kotlin coroutines guide")
        val memo2 = createTestMemoEntity(id = "2", content = "Java threads tutorial")
        memoDao.insertAll(listOf(memo1, memo2))

        val results = memoDao.search("%Kotlin%").first()
        assertThat(results).hasSize(1)
        assertThat(results[0].id).isEqualTo("1")
    }

    @Test
    fun `get pending sync count`() = runTest {
        val synced = createTestMemoEntity(id = "1", syncStatus = SyncStatus.SYNCED)
        val pending = createTestMemoEntity(id = "2", syncStatus = SyncStatus.PENDING_CREATE)
        memoDao.insertAll(listOf(synced, pending))

        val count = memoDao.getPendingSyncCount().first()
        assertThat(count).isEqualTo(1)
    }
}
```

#### TDD 示例 — MemoRepositoryTest

```kotlin
class MemoRepositoryTest {
    @MockK private lateinit var memoDao: MemoDao
    @MockK private lateinit var outboxDao: OutboxDao
    @MockK private lateinit var memoApi: MemosMemoApi
    private lateinit var repository: MemoRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = MemoRepositoryImpl(memoDao, outboxDao, memoApi)
    }

    @Test
    fun `createMemo in offline mode saves locally only`() = runTest {
        val memo = CreateMemoParams(content = "Test", visibility = PRIVATE)
        coEvery { memoDao.insert(any()) } returns Unit

        repository.setMode(AppMode.OFFLINE)
        val result = repository.createMemo(memo)

        assertThat(result.isSuccess).isTrue()
        coVerify { memoDao.insert(any()) }
        coVerify(exactly = 0) { memoApi.createMemo(any()) }
    }

    @Test
    fun `createMemo in online mode saves locally and enqueues outbox`() = runTest {
        val memo = CreateMemoParams(content = "Test", visibility = PRIVATE)
        coEvery { memoDao.insert(any()) } returns Unit
        coEvery { outboxDao.insert(any()) } returns 1L

        repository.setMode(AppMode.ONLINE)
        val result = repository.createMemo(memo)

        assertThat(result.isSuccess).isTrue()
        coVerify { memoDao.insert(any()) }
        coVerify { outboxDao.insert(match { it.operation == OutboxOperation.CREATE }) }
    }

    @Test
    fun `getMemos returns flow from local database`() = runTest {
        val entities = listOf(createTestMemoEntity(id = "1"))
        every { memoDao.getActiveNormal() } returns flowOf(entities)

        val result = repository.getMemos().first()
        assertThat(result).hasSize(1)
    }
}
```

### 7.2 Sprint 2 — 网络层 & 同步机制（Week 3-4）

| # | 任务 | 测试类 | 优先级 |
|---|------|--------|--------|
| 2.1 | Retrofit API 接口实现 | `MemosMemoApiTest` | P0 |
| 2.2 | AuthInterceptor 请求拦截 | `AuthInterceptorTest` | P0 |
| 2.3 | RetryInterceptor 重试机制 | `RetryInterceptorTest` | P0 |
| 2.4 | LoginActivity（单机/联机模式切换） | `LoginActivityTest`, `LoginViewModelTest` | P0 |
| 2.5 | Outbox 队列处理器 | `OutboxProcessorTest` | P0 |
| 2.6 | SyncManager 同步管理 | `SyncManagerTest` | P0 |
| 2.7 | SyncWorker (WorkManager) | `SyncWorkerTest` | P0 |
| 2.8 | SyncStatusFragment 同步状态监控页面 | `SyncStatusFragmentTest`, `SyncStatusViewModelTest` | P0 |
| 2.9 | 网络状态监听 | `NetworkMonitorTest` | P1 |

#### TDD 示例 — OutboxProcessorTest

```kotlin
class OutboxProcessorTest {
    @MockK private lateinit var outboxDao: OutboxDao
    @MockK private lateinit var memoApi: MemosMemoApi
    @MockK private lateinit var attachmentApi: MemosAttachmentApi
    @MockK private lateinit var networkMonitor: NetworkMonitor
    private lateinit var processor: OutboxProcessor

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        processor = OutboxProcessor(outboxDao, memoApi, attachmentApi, networkMonitor)
    }

    @Test
    fun `processEntry succeeds and marks COMPLETED`() = runTest {
        val entry = createTestOutboxEntry(
            entityType = EntityType.MEMO,
            operation = OutboxOperation.CREATE,
            status = OutboxStatus.PENDING
        )
        coEvery { outboxDao.getNextPending() } returns entry
        coEvery { memoApi.createMemo(any()) } returns createTestMemoDto()
        coEvery { outboxDao.updateStatus(entry.id, OutboxStatus.COMPLETED, any()) } returns Unit

        processor.processNext()

        coVerify { outboxDao.updateStatus(entry.id, OutboxStatus.COMPLETED, any()) }
    }

    @Test
    fun `processEntry on network error increments retryCount`() = runTest {
        val entry = createTestOutboxEntry(retryCount = 0)
        coEvery { outboxDao.getNextPending() } returns entry
        coEvery { memoApi.createMemo(any()) } throws IOException("Network error")

        processor.processNext()

        coVerify {
            outboxDao.updateForRetry(
                entry.id,
                retryCount = 1,
                nextRetryAt = any(),
                lastError = "Network error"
            )
        }
    }

    @Test
    fun `processEntry exceeding maxRetries marks FAILED`() = runTest {
        val entry = createTestOutboxEntry(retryCount = 5, maxRetries = 5)
        coEvery { outboxDao.getNextPending() } returns entry
        coEvery { memoApi.createMemo(any()) } throws IOException("Network error")

        processor.processNext()

        coVerify { outboxDao.updateStatus(entry.id, OutboxStatus.FAILED, any()) }
    }
}
```

#### TDD 示例 — AuthInterceptorTest

```kotlin
class AuthInterceptorTest {
    @MockK private lateinit var userPreferences: UserPreferences

    @Test
    fun `adds bearer token to request header`() = runTest {
        every { userPreferences.accessToken } returns flowOf("test-token-123")
        val interceptor = AuthInterceptor(userPreferences)

        val mockServer = MockWebServer()
        mockServer.enqueue(MockResponse().setResponseCode(200))
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val request = Request.Builder().url(mockServer.url("/")).build()
        client.newCall(request).execute()

        val recorded = mockServer.takeRequest()
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer test-token-123")
    }

    @Test
    fun `skips auth header when no token available`() = runTest {
        every { userPreferences.accessToken } returns flowOf(null)
        val interceptor = AuthInterceptor(userPreferences)

        val mockServer = MockWebServer()
        mockServer.enqueue(MockResponse().setResponseCode(200))
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val request = Request.Builder().url(mockServer.url("/")).build()
        client.newCall(request).execute()

        val recorded = mockServer.takeRequest()
        assertThat(recorded.getHeader("Authorization")).isNull()
    }
}
```

### 7.3 Sprint 3 — 多媒体附件管理（Week 5-6）

| # | 任务 | 测试类 | 优先级 |
|---|------|--------|--------|
| 3.1 | AttachmentDao + Entity | `AttachmentDaoTest` | P0 |
| 3.2 | AttachmentRepository | `AttachmentRepositoryTest` | P0 |
| 3.3 | 图片压缩工具 | `MediaCompressorTest` | P0 |
| 3.4 | Coil 图片加载 + 多级缓存 | `ImageLoadingTest` | P1 |
| 3.5 | ExoPlayer 音视频播放 | `MediaPlayerTest` | P1 |
| 3.6 | AttachmentPreviewView 附件预览自定义 View | `AttachmentPreviewViewTest` | P1 |
| 3.7 | AttachmentFragment 附件列表页面 | `AttachmentFragmentTest` | P1 |
| 3.8 | EditorFragment 中插入附件 | `EditorAttachmentTest` | P0 |

#### TDD 示例 — MediaCompressorTest

```kotlin
class MediaCompressorTest {
    private lateinit var compressor: MediaCompressor

    @Before
    fun setup() {
        compressor = MediaCompressor()
    }

    @Test
    fun `compressImage reduces file size below target`() = runTest {
        val input = createTestBitmap(3000, 4000) // 12MP
        val result = compressor.compressImage(input, maxSizeKb = 500, maxDimension = 1920)
        assertThat(result.size).isLessThan(500 * 1024)
        assertThat(maxOf(result.width, result.height)).isLessThanOrEqualTo(1920)
    }

    @Test
    fun `compressImage preserves aspect ratio`() = runTest {
        val input = createTestBitmap(3000, 4000) // 3:4
        val result = compressor.compressImage(input, maxDimension = 1500)
        val ratio = result.width.toFloat() / result.height
        assertThat(ratio).isWithin(0.01f).of(0.75f)
    }

    @Test
    fun `getMimeType returns correct type for known extensions`() {
        assertThat(compressor.getMimeType("photo.jpg")).isEqualTo("image/jpeg")
        assertThat(compressor.getMimeType("audio.mp3")).isEqualTo("audio/mpeg")
        assertThat(compressor.getMimeType("video.mp4")).isEqualTo("video/mp4")
    }
}
```

### 7.4 Sprint 4 — AI 总结 & 高级功能（Week 7-8）

| # | 任务 | 测试类 | 优先级 |
|---|------|--------|--------|
| 4.1 | AI Service API 层 | `AiServiceApiTest` | P0 |
| 4.2 | AI 结果本地缓存 | `AiCacheDaoTest` | P0 |
| 4.3 | AI 流式响应处理 | `AiStreamProcessorTest` | P1 |
| 4.4 | AiRepository 实现 | `AiRepositoryTest` | P0 |
| 4.5 | AiSummaryFragment 页面 | `AiSummaryFragmentTest` | P1 |
| 4.6 | ExploreFragment 页面 (TabLayout + ViewPager2) | `ExploreFragmentTest` | P1 |
| 4.7 | RandomWalkFragment 页面 (策略 Spinner) | `RandomWalkFragmentTest` | P2 |
| 4.8 | 搜索功能 (SearchView 集成) | `SearchMemosUseCaseTest` | P1 |

#### TDD 示例 — AiRepositoryTest

```kotlin
class AiRepositoryTest {
    @MockK private lateinit var aiApi: AiServiceApi
    @MockK private lateinit var aiCacheDao: AiCacheDao
    private lateinit var repository: AiRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = AiRepositoryImpl(aiApi, aiCacheDao)
    }

    @Test
    fun `generateSummary returns cached result when available`() = runTest {
        val cached = AiCacheEntity(
            memoId = "1",
            summary = "Cached summary",
            keywords = "[\"kotlin\"]",
            sentiment = null,
            generatedAt = System.currentTimeMillis(),
            modelVersion = "gpt-4"
        )
        coEvery { aiCacheDao.getByMemoId("1") } returns cached

        val result = repository.generateSummary("1", "Some content")
        assertThat(result.summary).isEqualTo("Cached summary")
        coVerify(exactly = 0) { aiApi.generateSummary(any()) }
    }

    @Test
    fun `generateSummary calls API and caches when no cache`() = runTest {
        coEvery { aiCacheDao.getByMemoId("1") } returns null
        coEvery { aiApi.generateSummary(any()) } returns AiSummaryResponse(
            summary = "New summary",
            keywords = listOf("kotlin", "android")
        )
        coEvery { aiCacheDao.insert(any()) } returns Unit

        val result = repository.generateSummary("1", "Some content")
        assertThat(result.summary).isEqualTo("New summary")
        coVerify { aiCacheDao.insert(any()) }
    }

    @Test
    fun `generateSummary with forceRefresh ignores cache`() = runTest {
        val cached = AiCacheEntity(
            memoId = "1", summary = "Old", keywords = "[]",
            sentiment = null, generatedAt = 0, modelVersion = "gpt-4"
        )
        coEvery { aiCacheDao.getByMemoId("1") } returns cached
        coEvery { aiApi.generateSummary(any()) } returns AiSummaryResponse(
            summary = "Fresh", keywords = listOf()
        )
        coEvery { aiCacheDao.insert(any()) } returns Unit

        val result = repository.generateSummary("1", "Content", forceRefresh = true)
        assertThat(result.summary).isEqualTo("Fresh")
        coVerify { aiApi.generateSummary(any()) }
    }
}
```

### 7.5 Sprint 5 — 小组件 & 收尾（Week 9-10）

| # | 任务 | 测试类 | 优先级 |
|---|------|--------|--------|
| 5.1 | 日回顾小组件 (DailyReviewWidget) | `DailyReviewWidgetTest` | P1 |
| 5.2 | 快速输入小组件 (QuickInputWidget) | `QuickInputWidgetTest` | P1 |
| 5.3 | 热力图小组件 (HeatmapWidget) | `HeatmapWidgetTest` | P1 |
| 5.4 | WidgetUpdateWorker | `WidgetUpdateWorkerTest` | P1 |
| 5.5 | ArchiveFragment / TrashFragment | `ArchiveFragmentTest`, `TrashFragmentTest` | P1 |
| 5.6 | SettingsFragment（模式切换/服务端配置/导出） | `SettingsFragmentTest` | P1 |
| 5.7 | HeatmapCalendarView 自定义 View | `HeatmapCalendarViewTest` | P2 |
| 5.8 | 全量端到端测试 | `E2ETest` | P1 |

---

## 八、Room DAO 接口设计

```kotlin
@Dao
interface MemoDao {
    @Query("SELECT * FROM memos WHERE isDeleted = 0 AND state = 'NORMAL' ORDER BY pinned DESC, displayTime DESC")
    fun getActiveNormal(): Flow<List<MemoEntity>>

    @Query("SELECT * FROM memos WHERE isDeleted = 0 AND state = 'ARCHIVED' ORDER BY updateTime DESC")
    fun getArchived(): Flow<List<MemoEntity>>

    @Query("SELECT * FROM memos WHERE isDeleted = 1 ORDER BY updateTime DESC")
    fun getDeleted(): Flow<List<MemoEntity>>

    @Query("SELECT * FROM memos WHERE id = :id")
    suspend fun getById(id: String): MemoEntity?

    @Query("SELECT * FROM memos WHERE isDeleted = 0 AND content LIKE :query ORDER BY displayTime DESC")
    fun search(query: String): Flow<List<MemoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memo: MemoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memos: List<MemoEntity>)

    @Update
    suspend fun update(memo: MemoEntity)

    @Query("UPDATE memos SET isDeleted = 1, syncStatus = :syncStatus, updateTime = :now WHERE id = :id")
    suspend fun softDelete(id: String, syncStatus: SyncStatus = SyncStatus.PENDING_DELETE, now: Long = System.currentTimeMillis())

    @Query("UPDATE memos SET isDeleted = 0, state = 'NORMAL', syncStatus = :syncStatus WHERE id = :id")
    suspend fun restore(id: String, syncStatus: SyncStatus = SyncStatus.PENDING_UPDATE)

    @Query("DELETE FROM memos WHERE id = :id")
    suspend fun hardDelete(id: String)

    @Query("UPDATE memos SET state = 'ARCHIVED', syncStatus = :syncStatus WHERE id = :id")
    suspend fun archive(id: String, syncStatus: SyncStatus = SyncStatus.PENDING_UPDATE)

    @Query("UPDATE memos SET pinned = :pinned, syncStatus = :syncStatus WHERE id = :id")
    suspend fun setPinned(id: String, pinned: Boolean, syncStatus: SyncStatus = SyncStatus.PENDING_UPDATE)

    @Query("UPDATE memos SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)

    @Query("SELECT COUNT(*) FROM memos WHERE syncStatus != 'SYNCED'")
    fun getPendingSyncCount(): Flow<Int>

    @Query("SELECT * FROM memos WHERE syncStatus IN ('PENDING_CREATE', 'PENDING_UPDATE', 'PENDING_DELETE')")
    suspend fun getPendingSync(): List<MemoEntity>

    // 热力图统计：按天分组统计笔记数
    @Query("""
        SELECT date(displayTime / 1000, 'unixepoch', 'localtime') as day, COUNT(*) as count
        FROM memos WHERE isDeleted = 0
        GROUP BY day
        ORDER BY day DESC
        LIMIT :days
    """)
    fun getDailyStats(days: Int = 365): Flow<List<DailyStat>>

    @Query("SELECT COUNT(*) FROM memos WHERE isDeleted = 0 AND state = 'NORMAL'")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT date(displayTime / 1000, 'unixepoch', 'localtime')) FROM memos WHERE isDeleted = 0")
    fun getActiveDayCount(): Flow<Int>
}

data class DailyStat(
    val day: String,
    val count: Int
)

@Dao
interface TagDao {
    @Query("SELECT DISTINCT tag FROM memo_tags ORDER BY tag")
    fun getAllTags(): Flow<List<String>>

    @Query("SELECT tag FROM memo_tags WHERE memoId = :memoId")
    fun getTagsForMemo(memoId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: TagEntity)

    @Query("DELETE FROM memo_tags WHERE memoId = :memoId")
    suspend fun deleteAllForMemo(memoId: String)

    @Transaction
    suspend fun replaceTagsForMemo(memoId: String, tags: List<String>) {
        deleteAllForMemo(memoId)
        tags.forEach { insert(TagEntity(memoId, it)) }
    }

    @Query("SELECT COUNT(DISTINCT tag) FROM memo_tags")
    fun getTagCount(): Flow<Int>
}

@Dao
interface OutboxDao {
    @Insert
    suspend fun insert(entry: OutboxEntry): Long

    @Query("""
        SELECT * FROM outbox
        WHERE status = 'PENDING' AND nextRetryAt <= :now
        ORDER BY createdAt ASC
        LIMIT 1
    """)
    suspend fun getNextPending(now: Long = System.currentTimeMillis()): OutboxEntry?

    @Query("SELECT * FROM outbox ORDER BY createdAt DESC")
    fun getAll(): Flow<List<OutboxEntry>>

    @Query("SELECT * FROM outbox WHERE status = 'FAILED' ORDER BY createdAt DESC")
    fun getFailed(): Flow<List<OutboxEntry>>

    @Query("SELECT COUNT(*) FROM outbox WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM outbox WHERE status = 'FAILED'")
    fun getFailedCount(): Flow<Int>

    @Query("UPDATE outbox SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: OutboxStatus, completedAt: Long? = null)

    @Query("""
        UPDATE outbox SET
            retryCount = :retryCount,
            nextRetryAt = :nextRetryAt,
            lastError = :lastError,
            status = 'PENDING'
        WHERE id = :id
    """)
    suspend fun updateForRetry(id: Long, retryCount: Int, nextRetryAt: Long, lastError: String?)

    @Query("UPDATE outbox SET status = 'FAILED' WHERE id = :id")
    suspend fun markFailed(id: Long)

    @Query("UPDATE outbox SET status = 'PENDING', retryCount = 0, nextRetryAt = :now WHERE id = :id")
    suspend fun retryNow(id: Long, now: Long = System.currentTimeMillis())

    @Query("UPDATE outbox SET status = 'CANCELLED' WHERE id = :id")
    suspend fun cancel(id: Long)

    @Query("DELETE FROM outbox WHERE status IN ('COMPLETED', 'CANCELLED') AND completedAt < :before")
    suspend fun cleanupOld(before: Long)
}

@Dao
interface AttachmentDao {
    @Query("SELECT * FROM attachments WHERE memoId = :memoId")
    fun getByMemoId(memoId: String): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments ORDER BY createTime DESC")
    fun getAll(): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE mimeType LIKE :typePrefix || '%' ORDER BY createTime DESC")
    fun getByType(typePrefix: String): Flow<List<AttachmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: AttachmentEntity)

    @Delete
    suspend fun delete(attachment: AttachmentEntity)

    @Query("SELECT * FROM attachments WHERE id = :id")
    suspend fun getById(id: String): AttachmentEntity?
}

@Dao
interface AiCacheDao {
    @Query("SELECT * FROM ai_cache WHERE memoId = :memoId")
    suspend fun getByMemoId(memoId: String): AiCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cache: AiCacheEntity)

    @Query("DELETE FROM ai_cache WHERE memoId = :memoId")
    suspend fun deleteByMemoId(memoId: String)

    @Query("DELETE FROM ai_cache WHERE generatedAt < :before")
    suspend fun cleanupOld(before: Long)
}
```

---

## 九、Hilt 依赖注入配置

```kotlin
// DatabaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "memoflow.db")
            .addMigrations(/* ... */)
            .build()

    @Provides fun provideMemoDao(db: AppDatabase): MemoDao = db.memoDao()
    @Provides fun provideTagDao(db: AppDatabase): TagDao = db.tagDao()
    @Provides fun provideAttachmentDao(db: AppDatabase): AttachmentDao = db.attachmentDao()
    @Provides fun provideOutboxDao(db: AppDatabase): OutboxDao = db.outboxDao()
    @Provides fun provideAiCacheDao(db: AppDatabase): AiCacheDao = db.aiCacheDao()
}

// NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        retryInterceptor: RetryInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(retryInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient, prefs: UserPreferences): Retrofit =
        Retrofit.Builder()
            .baseUrl(prefs.serverUrl.value ?: "http://localhost/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides fun provideAuthApi(retrofit: Retrofit): MemosAuthApi =
        retrofit.create(MemosAuthApi::class.java)
    @Provides fun provideMemoApi(retrofit: Retrofit): MemosMemoApi =
        retrofit.create(MemosMemoApi::class.java)
    @Provides fun provideAttachmentApi(retrofit: Retrofit): MemosAttachmentApi =
        retrofit.create(MemosAttachmentApi::class.java)
}
```

---

## 十、关键业务流程

### 10.1 用户登录流程

```
启动应用
    │
    ▼
检查本地登录状态 (DataStore)
    │
    ├── 已登录（单机模式）────────────→ 进入首页
    ├── 已登录（联机模式）
    │      │
    │      ▼
    │   刷新 Token ─── 成功 ──→ 进入首页
    │      │
    │      └── 失败 ──→ 登录页面
    └── 未登录 ──────────────→ 登录页面
                                   │
                    ┌──────────────┴──────────────┐
                    │                              │
                选择"单机模式"               选择"联机模式"
                    │                              │
              直接进入首页                   输入服务器地址
              本地存储数据                   输入用户名/密码
                                                   │
                                          POST /auth/signin
                                                   │
                                          保存 Token + 用户信息
                                          触发首次全量同步
                                                   │
                                              进入首页
```

### 10.2 笔记创建流程（联机模式）

```
用户点击"新建笔记"
    │
    ▼
打开编辑器 → 输入内容/插入附件/添加标签
    │
    ▼
点击"保存"
    │
    ├── 1. 生成本地 UUID
    ├── 2. 写入 MemoEntity (syncStatus = PENDING_CREATE)
    ├── 3. 写入 TagEntity(s)
    ├── 4. 写入 AttachmentEntity(s)
    ├── 5. 写入 OutboxEntry (operation = CREATE, payload = JSON)
    │
    ▼
UI 立即展示新笔记（乐观更新）
    │
    ▼
OutboxProcessor 检测到新条目
    │
    ├── 网络可用 → 调用 POST /api/v1/memos
    │      │
    │      ├── 成功 → 更新 serverId, syncStatus = SYNCED
    │      │         标记 OutboxEntry = COMPLETED
    │      │
    │      └── 失败 → 递增 retryCount, 计算退避时间
    │
    └── 网络不可用 → 等待网络恢复
```

### 10.3 数据同步流程（全量拉取）

```
触发条件：首次联机 / 手动刷新 / SyncWorker 定时
    │
    ▼
GET /api/v1/memos?pageSize=100 (分页遍历)
    │
    ▼
对比本地数据与远端数据
    │
    ├── 远端有、本地无 → INSERT 到 Room
    ├── 远端有、本地有 → 比较 updateTime，取较新版本
    ├── 远端无、本地有(已同步) → 标记为远端已删除
    └── 本地有未同步修改 → 保留本地版本，继续 Outbox 同步
```

---

## 十一、测试策略

### 11.1 测试金字塔

```
          ┌──────────┐
          │   E2E    │  ← Espresso UI Test (少量关键流程)
         ┌┴──────────┴┐
         │ Integration │ ← Repository + DAO + MockWebServer + Fragment Scenario
        ┌┴────────────┴┐
        │  Unit Tests   │ ← ViewModel / UseCase / Mapper / Processor / Adapter
        └──────────────┘
```

### 11.2 测试工具

| 工具 | 用途 |
|------|------|
| JUnit5 | 测试框架 |
| MockK | Kotlin Mock 库 |
| Turbine | Flow 测试 |
| Robolectric | Android 本地测试 |
| MockWebServer | API 集成测试 |
| Espresso | UI 交互测试（点击、输入、断言 View 状态） |
| FragmentScenario | Fragment 隔离测试（launchFragmentInContainer） |
| RecyclerView Testing | `RecyclerViewActions` 列表滚动/点击测试 |
| Hilt Testing | DI 测试 |
| Navigation Testing | `TestNavHostController` 导航断言 |

### 11.3 测试覆盖率目标

| 层 | 覆盖率目标 |
|----|-----------|
| Domain (UseCase) | ≥ 90% |
| Data (Repository/DAO) | ≥ 85% |
| ViewModel | ≥ 80% |
| UI (Fragment/Adapter) | 关键路径覆盖 |

---

## 十二、依赖清单

```toml
# gradle/libs.versions.toml 需新增

[versions]
hilt = "2.51.1"
room = "2.6.1"
retrofit = "2.11.0"
okhttp = "4.12.0"
coil = "2.7.0"
exoplayer = "1.4.1"          # Media3 ExoPlayer
navigation = "2.8.4"
datastore = "1.1.1"
workmanager = "2.10.0"
material = "1.12.0"
recyclerview = "1.3.2"
swiperefreshlayout = "1.1.0"
viewpager2 = "1.1.0"
fragment = "1.8.5"
constraintlayout = "2.2.0"
coordinatorlayout = "1.2.0"
drawerlayout = "1.2.0"
mockk = "1.13.13"
turbine = "1.2.0"
coroutines = "1.9.0"
gson = "2.11.0"

[libraries]
# Hilt
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-android-compiler", version.ref = "hilt" }

# Room
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
room-testing = { module = "androidx.room:room-testing", version.ref = "room" }

# Network
retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }
retrofit-gson = { module = "com.squareup.retrofit2:converter-gson", version.ref = "retrofit" }
okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }
okhttp-logging = { module = "com.squareup.okhttp3:logging-interceptor", version.ref = "okhttp" }
okhttp-mockwebserver = { module = "com.squareup.okhttp3:mockwebserver", version.ref = "okhttp" }

# Image (非 Compose 版本)
coil = { module = "io.coil-kt:coil", version.ref = "coil" }

# Media
media3-exoplayer = { module = "androidx.media3:media3-exoplayer", version.ref = "exoplayer" }
media3-ui = { module = "androidx.media3:media3-ui", version.ref = "exoplayer" }

# UI - XML 布局体系
material = { module = "com.google.android.material:material", version.ref = "material" }
recyclerview = { module = "androidx.recyclerview:recyclerview", version.ref = "recyclerview" }
swiperefreshlayout = { module = "androidx.swiperefreshlayout:swiperefreshlayout", version.ref = "swiperefreshlayout" }
viewpager2 = { module = "androidx.viewpager2:viewpager2", version.ref = "viewpager2" }
constraintlayout = { module = "androidx.constraintlayout:constraintlayout", version.ref = "constraintlayout" }
coordinatorlayout = { module = "androidx.coordinatorlayout:coordinatorlayout", version.ref = "coordinatorlayout" }
drawerlayout = { module = "androidx.drawerlayout:drawerlayout", version.ref = "drawerlayout" }

# Fragment
fragment-ktx = { module = "androidx.fragment:fragment-ktx", version.ref = "fragment" }
fragment-testing = { module = "androidx.fragment:fragment-testing", version.ref = "fragment" }

# Navigation (Fragment 版本，非 Compose)
navigation-fragment-ktx = { module = "androidx.navigation:navigation-fragment-ktx", version.ref = "navigation" }
navigation-ui-ktx = { module = "androidx.navigation:navigation-ui-ktx", version.ref = "navigation" }
navigation-testing = { module = "androidx.navigation:navigation-testing", version.ref = "navigation" }

# DataStore
datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }

# WorkManager
workmanager = { module = "androidx.work:work-runtime-ktx", version.ref = "workmanager" }
workmanager-testing = { module = "androidx.work:work-testing", version.ref = "workmanager" }

# Coroutines
coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }

# Gson
gson = { module = "com.google.code.gson:gson", version.ref = "gson" }

# Testing
mockk = { module = "io.mockk:mockk", version.ref = "mockk" }
mockk-android = { module = "io.mockk:mockk-android", version.ref = "mockk" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
truth = { module = "com.google.truth:truth", version = "1.4.4" }
robolectric = { module = "org.robolectric:robolectric", version = "4.13" }
espresso-core = { module = "androidx.test.espresso:espresso-core", version = "3.7.0" }
espresso-contrib = { module = "androidx.test.espresso:espresso-contrib", version = "3.7.0" }

[plugins]
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version = "2.0.21-1.0.28" }
navigation-safeargs = { id = "androidx.navigation.safeargs.kotlin", version.ref = "navigation" }
```

#### app/build.gradle.kts 关键配置

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.navigation.safeargs)
}

android {
    // ...
    buildFeatures {
        viewBinding = true    // 启用 ViewBinding，不使用 Compose
    }
}

dependencies {
    // UI - XML 布局体系
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation(libs.swiperefreshlayout)
    implementation(libs.viewpager2)
    implementation(libs.constraintlayout)
    implementation(libs.coordinatorlayout)
    implementation(libs.drawerlayout)
    implementation(libs.fragment.ktx)

    // Navigation (Fragment)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    // DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Image
    implementation(libs.coil)

    // Media
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)

    // Async
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.datastore.preferences)
    implementation(libs.workmanager)

    // Testing
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    testImplementation(libs.robolectric)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.room.testing)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.contrib)
    androidTestImplementation(libs.fragment.testing)
    androidTestImplementation(libs.navigation.testing)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.workmanager.testing)
}
```

---

## 十三、非功能需求

| 项目 | 指标 |
|------|------|
| 冷启动时间 | < 1.5s |
| 列表滚动帧率 | ≥ 55fps |
| 图片加载（缓存命中） | < 50ms |
| 本地笔记搜索 | < 200ms (1000条以内) |
| APK 体积 | < 15MB |
| 最低 SDK | API 24 (Android 7.0) |
| 目标 SDK | API 36 |
| 同步延迟（有网） | < 5s |
| 离线数据完整性 | 100% 不丢失 |

---

## 十四、已确认设计决策

| # | 问题 | 决策 | 影响范围 |
|---|------|------|----------|
| 1 | AI 服务选型 | UI 和入口先做出来，AI 服务对接留待后续迭代实现 | `AiSummaryFragment` 提供占位 UI；`AiServiceApi` 定义接口但暂不对接真实服务 |
| 2 | memos 服务端版本 | 对接 **memos 最新版** API (`/api/v1`) | 所有 Retrofit 接口按最新文档定义 |
| 3 | 附件大小限制 | 总存储上限 **5GB** | 需在设置中显示已用/总量；上传前检查剩余空间；大文件走分片上传 |
| 4 | 多端同步冲突 | **需要处理**多设备同时编辑冲突 | 升级冲突策略为字段级合并（见 14.1） |
| 5 | 随机漫步逻辑 | **提供用户选项**：完全随机 / 基于标签相似度 / 基于时间衰减 | `RandomWalkFragment` 顶部 Spinner 提供策略选择器 |
| 6 | 探索页面 | UI 选项做出来，**暂不实现**业务逻辑 | `ExploreFragment` 使用 TabLayout + ViewPager2 展示公开笔记/热门标签/推荐三个 Tab，内容显示占位 |
| 7 | 小组件更新频率 | **30 分钟** | WorkManager PeriodicWorkRequest 间隔 30min |
| 8 | 数据导出 | 支持导出为 **Markdown + ZIP** 打包 | 新增 `ExportUseCase` + `ExportWorker`；设置页面增加导出入口 |
| 9 | 通知功能 | 触发场景：**同步完成通知** + **用户自定义提醒** | 需要 NotificationChannel；提醒功能绑定到笔记 |

### 14.1 多端冲突解决策略（升级版）

由于需要处理多设备同时编辑的场景，将冲突策略从简单的 Last-Write-Wins 升级为**字段级合并 + 用户选择**：

```
拉取远端数据时：
    │
    ▼
比较 remote.updateTime vs local.updateTime
    │
    ├── 本地无未同步修改 → 直接用远端数据覆盖
    │
    ├── 本地有未同步修改 → 进入冲突解决
    │      │
    │      ▼
    │   字段级对比 (content / tags / visibility / pinned / state)
    │      │
    │      ├── 修改字段不重叠 → 自动合并（各取修改方的字段值）
    │      │
    │      └── 修改字段重叠 → 标记冲突，弹出 UI 让用户选择
    │             │
    │             ├── 保留本地版本
    │             ├── 使用远端版本
    │             └── 手动合并（打开 Diff 视图）
    │
    └── 冲突记录写入 ConflictLog 供后续查看
```

**实现要点**：
- `MemoEntity` 新增 `baseUpdateTime: Long` 字段，记录上次同步时的远端 updateTime，用于三方合并比对
- `ConflictResolver` 类负责字段级 diff 和自动合并逻辑
- 冲突弹窗 `ConflictResolutionDialogFragment` 提供并排对比视图

### 14.2 数据导出方案

```
用户点击"导出"
    │
    ▼
ExportWorker (WorkManager, 后台执行)
    │
    ├── 1. 查询所有笔记 (Room)
    ├── 2. 每条笔记生成 .md 文件
    │       - 文件名: {displayTime}_{snippet}.md
    │       - YAML front matter: tags, createTime, state
    ├── 3. 收集所有附件文件（本地路径）
    ├── 4. 打包为 ZIP
    │       memoflow_export_20260312/
    │       ├── memos/
    │       │   ├── 2026-03-12_meeting-notes.md
    │       │   └── 2026-03-11_idea.md
    │       └── attachments/
    │           ├── photo_001.jpg
    │           └── audio_002.mp3
    ├── 5. 保存到 Downloads 目录
    └── 6. 发送导出完成通知
```

**Markdown 文件格式示例**：

```markdown
---
tags: [kotlin, android]
created: 2026-03-12T10:30:00Z
updated: 2026-03-12T14:20:00Z
state: NORMAL
visibility: PRIVATE
pinned: false
---

# Meeting Notes

Today we discussed the new architecture...

![photo](../attachments/photo_001.jpg)
```

### 14.3 通知系统设计

| 通知渠道 | Channel ID | 优先级 | 场景 |
|----------|-----------|--------|------|
| 同步通知 | `sync_channel` | LOW | 后台同步完成 / 同步失败告警 |
| 提醒通知 | `reminder_channel` | HIGH | 用户设定的笔记提醒到时间 |
| 导出通知 | `export_channel` | DEFAULT | 数据导出完成 |

**笔记提醒功能**：
- 编辑器中可为单条笔记设置提醒时间
- `MemoEntity` 新增 `reminderTime: Long?` 字段
- 使用 `AlarmManager` (精确闹钟) 触发提醒
- 点击通知打开对应笔记

### 14.4 随机漫步策略

| 策略 | 算法 | 说明 |
|------|------|------|
| 完全随机 | `ORDER BY RANDOM() LIMIT 1` | 从所有笔记中等概率随机取一条 |
| 标签相似度 | 基于当前笔记的 tags，找共享标签最多的其他笔记，加权随机 | 帮助用户发现标签关联的知识点 |
| 时间衰减 | 越久未查看的笔记权重越高，公式: `weight = daysSinceLastView ^ 1.5` | 帮助用户回顾遗忘的旧笔记 |

需要 `MemoEntity` 新增 `lastViewedAt: Long?` 字段以支持时间衰减策略。

---

## 十五、新增/修改字段汇总

根据以上确认的决策，以下字段需追加到原有数据模型中：

### MemoEntity 新增字段

```kotlin
val baseUpdateTime: Long? = null,     // 多端冲突：上次同步时远端 updateTime
val reminderTime: Long? = null,       // 笔记提醒时间
val lastViewedAt: Long? = null        // 最后查看时间（随机漫步用）
```

### 新增 Entity

```kotlin
@Entity(tableName = "conflict_log")
data class ConflictLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memoId: String,
    val localContent: String,
    val remoteContent: String,
    val conflictFields: String,         // JSON: ["content", "tags"]
    val resolution: ConflictResolution, // LOCAL / REMOTE / MERGED
    val resolvedAt: Long? = null,
    val createdAt: Long
)

enum class ConflictResolution { PENDING, LOCAL, REMOTE, MERGED }
```

### 包结构新增

```
com.example.snapmemo/
├── data/
│   ├── local/db/
│   │   ├── dao/
│   │   │   └── ConflictLogDao.kt                 ← 新增
│   │   └── entity/
│   │       └── ConflictLogEntity.kt               ← 新增
│   └── export/
│       └── MarkdownExporter.kt                    ← 新增
├── domain/
│   ├── model/
│   │   └── ConflictInfo.kt                        ← 新增
│   └── usecase/
│       ├── export/
│       │   └── ExportMemosUseCase.kt              ← 新增
│       └── reminder/
│           └── SetReminderUseCase.kt              ← 新增
├── ui/
│   ├── conflict/
│   │   ├── ConflictResolutionDialogFragment.kt    ← 新增
│   │   └── ConflictDiffView.kt                    ← 新增 (自定义 View)
│   └── notification/
│       └── NotificationHelper.kt                  ← 新增
├── worker/
│   ├── ExportWorker.kt                            ← 新增
│   └── ReminderScheduler.kt                       ← 新增
└── res/layout/
    └── dialog_conflict_resolution.xml             ← 新增
```

---

## 十六、更新后的 Sprint 计划

| Sprint | 周期 | 核心内容 | 变更 |
|--------|------|----------|------|
| Sprint 1 | Week 1-2 | 基础架构 + 本地 CRUD | ViewBinding + Navigation Component + DrawerLayout + Fragment 体系搭建；`lastViewedAt` / `reminderTime` / `baseUpdateTime` 字段纳入建表 |
| Sprint 2 | Week 3-4 | 网络层 + 同步机制 | LoginActivity + SyncStatusFragment；冲突解决升级为字段级合并 + ConflictResolutionDialogFragment |
| Sprint 3 | Week 5-6 | 多媒体附件 | AttachmentFragment (GridLayoutManager) + AttachmentPreviewView；5GB 总量限制检查 + 存储用量统计 |
| Sprint 4 | Week 7-8 | AI 总结(UI) + 随机漫步 + 探索(UI) + 导出 | AiSummaryFragment/ExploreFragment (TabLayout+ViewPager2) 仅做 UI 占位；RandomWalkFragment 三种策略；导出 Markdown+ZIP |
| Sprint 5 | Week 9-10 | 小组件(30min) + 通知 + 收尾 | SettingsFragment 导出入口；HeatmapCalendarView；同步/提醒/导出三个通知渠道 |
