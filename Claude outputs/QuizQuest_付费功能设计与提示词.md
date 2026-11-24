# QuizQuest Plus：付费功能设计与 Agent 提示词（按 A2 完善版）

本版根据你的 Assessment 2 演示（*Where You Go, What You Do*，30 页）完善。主要改动：

- 每条设计决定都对应到 A2 的具体页码和 ACS 条款；
- 用 A2 第 13 页的「五步 ACS 检查」审视付费功能；
- 按 Gray et al. (2018) 的五类暗黑模式，加上第 7 页的成瘾机制，逐条自查；
- 补上 A2 第 26 页承诺 #8「每个功能都做伦理审查」。这一步现在是《QuizQuest_修复阶段提示词.md》里的 A7。

方案不变：**演示模式**（不会真正扣费）、**一次性买断 A$4.99**，Plus 包含**自由练习、详细学习报告、外观主题包**。

---

## 一、设计方案

### 1. 免费与付费的划分

| 永远免费（学习本身） | QuizQuest Plus（附加功能） |
|---|---|
| 所有世界、全部 9 个关卡 | **自由练习**：自选世界、难度、题数；不影响星级 |
| 50/50 提示、计时器设置 | **详细学习报告**：按难度的正确率、30 天趋势、平均答题时间；可保存为文本文件 |
| 复习卡片（间隔重复） | **外观主题包**：海洋、森林、日落三套配色，都通过对比度测试 |
| 基础统计，包括**每个世界的正确率** | |
| 每日目标、"done for today" 提示 | |
| 无障碍选项、隐私页（导出和删除） | |

**为什么这样分（A2 依据）：**

- **第 17 页（教育行业）：** 儿童无法真正同意；动机激励和操纵之间要找平衡。对应条款：Ethics 2.3.1 尽量减少伤害，Conduct 1.2.2(a) 支持弱势群体。
  - 所以付费买不到"学得更好"或"分数更高"。
  - 家里付不起钱的学生，学习效果和买了的学生完全一样。
- **第 20 页的结论：** 选择"数据最少、最透明"、又能完成核心功能的设计；有疑问时，公众利益优先（Conduct 1.2.1）。
- **"该多练哪个世界"这个最有学习价值的信息保留在免费区：** 基础统计页已经免费显示每个世界的正确率，Plus 报告只是更细。

### 2. 每条设计决定对应的 A2 内容

| A2 内容（页码） | 付费功能里的设计决定 |
|---|---|
| 第 17 页：儿童无法真正同意；Families 政策 | 购买对话框写 "Ask a parent or guardian before buying"；学习内容不收费 |
| 第 7 页：最受影响的是儿童和处于压力下的人；操纵会绕过理性选择 | 答错、没过关、结果页**都不推销**（学生沮丧时最容易被说动） |
| 第 12 页 Conduct 1.2.2(d)："Not now" 和 "Allow" 视觉权重相同；取消和注册一样容易 | 两个按钮一样大、一样样式；"Reset demo purchase"和购买一样只要一步（真实版本里对应退款说明） |
| 第 12 页 Conduct 1.2.3(b)：不制造虚假紧迫感，不隐藏自动续费 | 一次性付费、没有订阅；不用倒计时、"限时"、假折扣 |
| 第 11 页 Ethics 2.1(b)：不能直接、间接或通过省略来误导 | 每个购买界面都写 "Demo – no real payment is made"；先列出免费内容，再介绍 Plus；数据安全表加上购买记录 |
| 第 16 页（金融）：适当的摩擦本身就是伦理功能；不要奖励高风险行为（Robinhood 撒花案例） | 购买前要确认；买完只显示平静的确认文字，**没有撒花、动画或音效** |
| 第 7 页：可变奖励、连续打卡和损失厌恶 | Plus 内容固定，没有随机奖励或"会员连续登录奖励" |
| 第 24–25 页：用 Koin 注入 NoOpAnalytics 作为"隐私闸门"，同意由架构保证，而不只是界面上的承诺 | Koin 只把 `PurchaseRepository` 绑定到 `DemoPurchaseRepository`。这个版本里没有任何代码能真正扣钱，"演示"由架构保证 |
| 第 11 页 Ethics 2.2(d)：数据最小化，数据留在设备上 | 购买记录只有 3 个字段（产品 ID、时间、是否演示），只存在手机上；导出时包含、删除时一起删 |
| 第 24 页：Data safety 表要申报所有数据 | README 的数据安全表加一行"购买记录"（Play 表格里属于 Financial info → Purchase history） |
| 第 8 页 + Conduct 1.2.4(c)：了解相关法规 | 澳大利亚针对订阅陷阱和操纵性设计的不公平交易法 2027 年 7 月 1 日生效（Norton Rose Fulbright, 2026）；Children's Online Privacy Code；Google Play Families 政策 |
| 第 26 页承诺：通知必须主动开启 | 不发任何购买提醒或"优惠"通知 |
| 第 12 页 Ethics 2.3.1(c)(d)：同意界面要能用 TalkBack 操作 | Plus 页和购买对话框都有语义标注、标题语义和 48 dp 触控区域 |
| 第 26 页承诺 #8：每个功能都做五步 ACS 检查 | Plus 的五步检查写进 docs/ethics-review.md（见下一节） |

### 3. 用 A2 第 13 页的「五步 ACS 检查」审视 Plus

格式和你 A2 第 13 页一样。可以直接写进 `docs/ethics-review.md`，也能用在反思里。

> **假设的请求（产品经理）：** "青少年做决定很快。在没过关的结果页弹出 Plus：'解锁自由练习，下次一定能过！'，加一个 10 分钟的五折倒计时。'Unlock' 按钮做得又大又亮，'No thanks' 用小号灰字。"

1. **识别利益相关者（Conduct 1.2.1(a)）：**
   - 13–17 岁的学生，包括家里付不起钱的学生；
   - 付钱的家长或监护人；
   - 老师；
   - 开发者；
   - 公众，以及整个 ICT 行业的信誉。
2. **对照价值观检查：**
   - 在学生失败后推销，是利用挫败感，属于绕过理性选择的操纵（第 7 页）。
   - 倒计时加假折扣，是虚假紧迫感，违反 Conduct 1.2.3(b)。
   - 大按钮配小灰字，是界面干扰，减少了用户的控制权，违反 Conduct 1.2.2(d)。
   - "下次一定能过"暗示花钱能提高成绩，是对产品的误导，违反 Ethics 2.1(b) 和 Conduct 1.2.3(b)。
   - 把学习功能锁起来会伤害弱势学生，违反 Conduct 1.2.2(a) 和 Ethics 2.3.1。
3. **解决冲突：** 学生的福祉和公平获得学习的机会，优先于收入（Conduct 1.2.1）。
4. **伤害最小的替代方案：**
   - 学习内容全部免费，Plus 只包含附加功能；
   - 只在学生主动点击的 4 个入口出现；
   - 购买前写明价格，一次性付费；
   - 两个按钮权重相同，附有提醒找家长的文字；
   - 标明演示；购买后没有庆祝效果；撤销和购买一样容易。
5. **记录并上报：** 把这个决定写进 `docs/ethics-review.md` 和 README。按照"不保持沉默"（Ethics 2.1(c)、Conduct 1.2.4(e)）的要求，书面拒绝原来的请求。

### 4. 暗黑模式自查表

依据：Gray et al. (2018) 的五类，加上第 7、12、16 页的内容。

| 类型 | 付费功能里的典型做法 | QuizQuest Plus 的做法 | 怎么验证 |
|---|---|---|---|
| Nagging（纠缠） | 每次启动或每关结束都弹出购买 | 不弹窗；只在学生主动点的 4 个入口出现 | UI 测试：答题页和结果页没有 "Plus" 文字；自检：完整玩一关 |
| Obstruction（阻挠） | 购买一步完成，撤销要翻好几层 | "Reset demo purchase"和购买在同一页面，同样一步；README 写明真实版本的退款途径 | 自检 |
| Sneaking（偷偷摸摸） | 默认勾选、价格最后才出现、隐藏自动续费 | 价格写在按钮前面，含 GST，是一次性付费；没有任何预选 | UI 测试：价格在打开对话框之前就能看到 |
| Interface interference（界面干扰） | 大按钮配小灰字；羞辱式文案（"不，我不想进步"） | 两个按钮都是 OutlinedButton、weight(1f)；用中性的 "Not now" | UI 测试：两个按钮的宽度相同 |
| Forced action（强迫行为） | 不付费就不能继续学习 | `PlusFeature` 只包含附加功能，核心学习不可能被锁 | `FeatureGateTest` |
| 虚假紧迫感（第 12 页） | 倒计时、"只剩 2 个名额" | 没有 | 文案检查 |
| 可变奖励（第 7 页） | 随机奖励、开箱 | 没有，Plus 内容固定 | 设计层面排除 |
| 庆祝高风险行为（第 16 页） | 购买后撒花、播放音效 | 只显示一行平静的确认文字 | 自检 |
| 连续打卡和损失厌恶（第 7 页） | "会员连续登录奖励" | 没有连续打卡 | 设计层面排除 |
| 隐私暗黑模式（第 8 页） | 买 Plus 要多交数据 | 不多收集任何数据，报告在手机上计算 | 合并后的 Manifest 只有 INTERNET |

### 5. 法规与平台政策（Conduct 1.2.4(c)）

- **澳大利亚：**
  - 针对订阅陷阱和操纵性设计的不公平交易法，2027 年 7 月 1 日生效（你 A2 第 8 页的引用）。一次性付费、没有订阅、没有虚假紧迫感，正好避开这些问题。
  - Children's Online Privacy Code 的核心是"儿童的最大利益"（第 17 页）。
- **Google Play：**
  - Families 政策：面向儿童的 app 有额外限制。
  - 在 Play 上销售数字内容，一般要使用 Google Play 结算；结算库会加入 BILLING 权限。
  - Data safety 表要申报购买记录。
  - 这些都写进 README 的"真实版本怎么做"，所以这次只做演示。

### 6. 技术设计（对应讲义）

| 部分 | 内容 | 讲义 |
|---|---|---|
| 领域模型 | `PlusFeature`、`ThemePack`、`PlusProduct`、`PurchaseResult`，加上规则对象 `FeatureGate`（纯 Kotlin） | L1 |
| 数据库 | 新增 `Purchase` 表，`Settings` 加 `themePack` 列；版本 2 → 3，用 AutoMigration；写迁移测试 | L7 |
| 仓库 | `PurchaseRepository` 接口加 `DemoPurchaseRepository` 实现。这是"演示闸门"，相当于 A2 第 24 页的 NoOpAnalytics | L4 |
| 依赖注入 | Koin 只绑定演示实现 | L4 |
| 界面 | Plus 页、练习设置页、详细报告页；设置页加主题选择 | L2、L3 |
| 导航 | 新路由：`plus`、`practiceSetup`、`practice/{categoryId}/{difficulty}/{amount}`、`report` | L6 |
| 网络 | 自由练习复用现有的 `loadQuestions`，离线时由 Room 兜底 | L5、L7 |

### 7. 顺序和取舍

- **推荐顺序：**
  1. R6–R16（修复）；
  2. A1–A9（修复文件第六部分：按 A2 完善现有代码，包括伦理审查 A7）；
  3. P1–P10（付费功能）；
  4. 原来的阶段 9、10。
- **时间不够时：** 只做 P1–P5 和 P10，也是一个完整的小功能。这种情况下，要让 Agent 删掉 Plus 页上"自由练习"和"详细报告"的介绍，不能宣传没有实现的功能（Ethics 2.1(b)）。
- **A1–A9 不管做不做 Plus 都应该做。** 它们对应你 A2 写下的承诺。

### 8. 写反思时可用的素材（按 Gibbs 的六个阶段）

- **Description：** 付费功能是 A2 第 13 页那种两难在你自己项目里的真实版本，冲突在收入和学生福祉之间。
- **Feelings：** 第一次把"赚钱"和"面向青少年的伦理"放在一起考虑时的矛盾。比如想让功能更吸引人，又担心变成推销。
- **Evaluation：**
  - 做得好的：学习内容免费；Koin 演示闸门；做了暗黑模式自查。
  - 不足：Plus 报告里的部分学习洞察还是要付费，对弱势学生不完全公平。
- **Analysis：**
  - 为什么不用订阅：对应 Conduct 1.2.3(b)，以及 2027 年生效的法规。
  - 为什么失败后不推销：第 7 页，处于压力下的人最容易被影响。
  - 为什么要有确认这种摩擦：第 16 页的金融案例。
  - 为什么要靠架构来保证：第 24 页。
- **Conclusion：** A2 的原则在真正写代码时，会变成很多具体的小决定，比如按钮样式、文案、提示出现的位置。
- **Action Plan：** 如果真的上架，你会：
  - 接入 Play 结算，使用家长购买审批，重新申报数据安全；
  - 考虑为学校或困难家庭提供免费解锁码，进一步落实 Conduct 1.2.2(a)。

---

## 二、提示词

**用法：**

1. 每个新的 Agent 对话先粘贴《QuizQuest_修复阶段提示词.md》里的「通用开头」。
2. 再粘贴下面的「Plus 规则」。
3. 最后粘贴一个步骤。
4. 每步做完后跑测试，按「自检」检查，再提交。

### Plus 规则（每个 P 步骤的对话都粘贴一次）

```
This task is part of "QuizQuest Plus", a DEMO one-time purchase (A$4.99, no subscription).
It follows my Assessment 2 ethics presentation and the ACS Codes. Rules:
- Learning is never paid for (Ethics 2.3.1, Conduct 1.2.2(a)): every world and level, hints, timer
  settings, Review, the basic Stats screen (including accuracy per world), daily goal, Settings,
  accessibility options and Privacy (export/delete) stay free.
- Plus only adds extras: Free practice, the Detailed learning report and three colour theme packs.
- No real payment: DemoPurchaseRepository is the only implementation and Koin binds only it.
  Every purchase screen and dialog says "Demo – no real payment is made." (Ethics 2.1(b))
- No dark patterns (Gray et al. 2018; Conduct 1.2.2(d), 1.2.3(b)):
  no Plus offers inside a quiz, on the Result screen, after a wrong answer or a failed level, at app
  start or in notifications (nagging); undoing is as easy as buying (obstruction); price shown
  before buying and nothing pre-selected (sneaking); the buy and "Not now" buttons have equal
  weight and neutral wording (interface interference, no confirmshaming); learning never requires
  Plus (forced action); no countdowns, fake discounts, "limited time" or "most popular" labels;
  no random rewards; no confetti, animation or sound after buying.
- The buy dialog says "Ask a parent or guardian before buying." (learners are 13–17)
- Data minimisation (Ethics 2.2(d)): the purchase record has only productId, purchasedAt and
  isDemo; it stays on this phone; Export includes it; "Delete all my data" removes it.
- No new app libraries and no new permissions (no BILLING permission in the demo).
```

---

### E1：伦理审查 → 已移到《QuizQuest_修复阶段提示词.md》的 A7

这一步和 Plus 无关，所以放进了修复文件的第六部分，也就是 A7。还有一处变化：`docs/ethics-review.md` 开头写着 "Written by me (not by the AI agent)"，所以现在由 Agent 生成草稿 `docs/ethics-review-draft.md`，正式版由你自己写。

### P1：规则与领域模型（纯 Kotlin）

```
Step P1 of QuizQuest Plus. Pure Kotlin only – no UI, no database in this step.

1. AGENTS.md: add a subsection "5.x QuizQuest Plus (demo monetisation)" to section 5 with the Plus
   rules I pasted above (keep the ACS clause references), plus this table so every future task can
   check itself:
   | Dark pattern (Gray et al., 2018) | Forbidden in QuizQuest |
   | Nagging | offers in quiz/result/after failure/at start/in notifications |
   | Obstruction | undo harder than buying |
   | Sneaking | hidden price, pre-selected options, auto-renewal |
   | Interface interference | unequal buttons, confirmshaming wording |
   | Forced action | any learning feature that needs Plus |

2. data/model/Plus.kt:
   - enum class PlusFeature { FREE_PRACTICE, LEARNING_REPORT, THEME_PACKS }
     KDoc: "Only optional extras may be added here. Learning features must never be added
     (ACS Ethics 2.3.1, Conduct 1.2.2(a))."
   - enum class ThemePack(val isFree: Boolean) { CLASSIC(true), OCEAN(false), FOREST(false), SUNSET(false) }
   - data class PlusProduct(val productId: String = "quizquest_plus", val priceText: String = "A$4.99")
   - sealed interface PurchaseResult { data object Success; data object Cancelled;
     data class Failed(val reason: String) }   (all implement PurchaseResult)
   - @Serializable data class PurchaseRecord(val productId: String, val purchasedAt: Long, val isDemo: Boolean)

3. data/rules/FeatureGate.kt – an object with KDoc that explains the free/Plus split:
   - fun isAvailable(feature: PlusFeature, plusUnlocked: Boolean): Boolean
   - fun isThemeAvailable(pack: ThemePack, plusUnlocked: Boolean): Boolean  (free packs always true)
   - fun effectiveTheme(selected: ThemePack, plusUnlocked: Boolean): ThemePack (locked -> CLASSIC)

4. Tests in test/data/rules/FeatureGateTest.kt: each feature locked/unlocked; CLASSIC always
   available; a locked pack falls back to CLASSIC; an unlocked pack is kept; and a test that
   PlusFeature.entries contains exactly the three extras (so a learning feature cannot be added
   without this test failing on purpose).
```

**提交：**

```
feat(plus): add Plus rules and FeatureGate

- AGENTS.md: demo purchase rules with ACS references and a dark-pattern table
- PlusFeature, ThemePack, PlusProduct, PurchaseResult, PurchaseRecord
- FeatureGate: learning is never gated; unit tests
```

### P2：数据库版本 3（AutoMigration）

```
Step P2 of QuizQuest Plus: Room schema version 3 (Lecture 7).

1. data/database/Entities.kt:
   - New entity, with a KDoc saying it stores the minimum on purpose (ACS Ethics 2.2(d)):
     @Entity(tableName = "Purchase") data class PurchaseEntity(
       @PrimaryKey val productId: String, val purchasedAt: Long, val isDemo: Boolean)
   - SettingsEntity: add @ColumnInfo(defaultValue = "CLASSIC") val themePack: String = "CLASSIC"
2. data/database/Daos.kt – new PurchaseDao:
   - @Query("SELECT EXISTS(SELECT 1 FROM Purchase WHERE productId = :productId)")
     fun observeIsPurchased(productId: String): Flow<Boolean>
   - @Query("SELECT * FROM Purchase") suspend fun getAll(): List<PurchaseEntity>
   - @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(purchase: PurchaseEntity)
   - @Query("DELETE FROM Purchase WHERE productId = :productId") suspend fun delete(productId: String)
3. QuizDatabase: add PurchaseEntity, version = 3, AutoMigration(from = 2, to = 3) next to 1 -> 2,
   abstract fun purchaseDao(). Build once so app/schemas/.../3.json is generated (commit it).
4. Mappers: PurchaseEntity <-> PurchaseRecord. UserSettings gets themePack: ThemePack = CLASSIC,
   mapped to/from SettingsEntity.themePack (unknown text -> CLASSIC).
5. di/Modules.kt: single { get<QuizDatabase>().purchaseDao() }.
6. Export: add purchases: List<PurchaseRecord> to UserDataExport and fill it in
   PrivacyRepositoryImpl.exportAllDataAsJson() (inject PurchaseDao). deleteAllData() uses
   clearAllTables(), so the Purchase table is deleted too – add a comment that says so.
7. Fakes: FakePurchaseDao in test/fakes/FakeDaos.kt; update constructors in tests.

Tests:
- JVM: mapper tests (unknown themePack -> CLASSIC); the export JSON contains the purchases list.
- androidTest: add androidx.room:room-testing (version.ref = "room") as androidTestImplementation
  ONLY, add android { sourceSets { getByName("androidTest").assets.srcDir("$projectDir/schemas") } },
  and write MigrationTest with MigrationTestHelper: version 2 with one Settings row -> migrate to 3
  -> themePack = "CLASSIC" and the Purchase table exists and is empty.
```

**自检：** 不要卸载 app，直接覆盖安装新版本。旧的进度和设置都应该还在。

**提交：**

```
feat(database): version 3 with Purchase table and theme pack setting

- Minimal PurchaseEntity + PurchaseDao; Settings.themePack default CLASSIC
- AutoMigration 2 -> 3, schema 3.json, MigrationTest
- Export includes purchases; delete-all clears them
```

### P3：购买仓库（演示闸门）

```
Step P3 of QuizQuest Plus: the repository (Lecture 4 repository pattern).

1. data/repository/PurchaseRepository.kt:
   interface PurchaseRepository {
       fun isPlusUnlocked(): Flow<Boolean>
       fun getProduct(): PlusProduct
       suspend fun purchase(): PurchaseResult
       suspend fun restore(): Boolean
       suspend fun resetDemoPurchase()
   }
2. data/repository/DemoPurchaseRepository.kt (purchaseDao, timeProvider, dispatcher):
   - isPlusUnlocked(): purchaseDao.observeIsPurchased(PRODUCT_ID)
   - purchase(): withContext(dispatcher) { delay(800); insert PurchaseEntity(productId,
     timeProvider.now(), isDemo = true); Success }; exceptions -> Failed(message). No network.
   - restore(): true if purchaseDao.getAll() contains the product.
   - resetDemoPurchase(): purchaseDao.delete(PRODUCT_ID).
   - KDoc, two paragraphs:
     (a) "Demo gate": like the NoOpAnalytics privacy gate in my Assessment 2 (slide 24), Koin binds
         PurchaseRepository only to this class, so no code path in this build can take real money –
         the "demo" promise is guaranteed by the architecture, not only by UI text.
     (b) A real GooglePlayPurchaseRepository would implement the same interface with the Play
         Billing Library (BillingClient, queryProductDetailsAsync, launchBillingFlow,
         acknowledgePurchase, queryPurchasesAsync for restore); the library adds the BILLING
         permission and Google Play processes the payment. Comments only – do NOT add the library.
3. di/Modules.kt: single<PurchaseRepository> { DemoPurchaseRepository(get(), get(), get()) } with a
   one-line comment pointing to the KDoc.
4. test/fakes: FakePurchaseRepository (MutableStateFlow<Boolean>, settable nextResult, counters).

Tests (DemoPurchaseRepositoryTest): purchase -> Success, unlocked, isDemo = true; DAO throws ->
Failed; restore true/false; reset -> locked.
```

**提交：**

```
feat(plus): add PurchaseRepository with a demo-only implementation

- Koin binds only DemoPurchaseRepository: no real payment is possible in this build
- Interface ready for a future Google Play Billing implementation
- Fake repository and unit tests
```

### P4：Plus 页面与购买对话框

```
Step P4 of QuizQuest Plus: the Plus screen (Lectures 2, 3, 4, 6). Follow AGENTS.md sections 5 and 6.

1. viewmodel/PlusUIState.kt + PlusViewModel(purchaseRepository): product, isUnlocked, isWorking,
   showConfirmDialog, message (enum PlusMessage { UNLOCKED, RESTORED, NOTHING_TO_RESTORE, FAILED, RESET }).
   onUnlockClicked() only opens the dialog; onConfirmPurchase(); onDismissDialog(); onRestoreClicked();
   onResetDemoClicked(); messageShown(). Ignore taps while isWorking.
2. Navigation: Screens.PlusScreen("plus") in AppNavHost (not a tab). SettingsScreen: new row
   "QuizQuest Plus" (same style as the Privacy row), onPlusClicked navigates there.
3. views/PlusScreen.kt (+ stateless PlusScreenContent), max width 600 dp, in THIS order
   (free first, so nothing is hidden by omission – Ethics 2.1(b)):
   a. Demo banner card (tertiaryContainer): "Demo – no real payment is made."
   b. "Always free" card: all worlds and levels, hints and timer, Review, Stats with accuracy per
      world, daily goal, accessibility settings, export and delete.
   c. "Plus adds" card: Free practice, Detailed learning report, 3 colour themes
      (icons decorative, contentDescription = null).
   d. Price, visible before any button: "A$4.99 one-time – no subscription, no ads".
   e. Not unlocked: Button "Unlock Plus" (48 dp) and TextButton "Restore purchase".
      Unlocked: a plain card "Plus is unlocked. Thank you for supporting QuizQuest!" and an
      OutlinedButton "Reset demo purchase" – undo is one step, as easy as buying (Conduct 1.2.2(d)).
   f. Snackbar for messages.
   No confetti, no animation and no sound after buying (A2 slide 16: no celebratory rewards for
   spending) – do not call SoundPlayer here.
4. Confirm dialog (only after tapping "Unlock Plus"): title "Unlock QuizQuest Plus?" (heading);
   text: "You get Free practice, the Detailed learning report and 3 colour themes. A$4.99
   one-time – no subscription, no ads. Ask a parent or guardian before buying. Demo – no real
   payment is made." Buttons "Not now" and "Unlock (demo)": both OutlinedButton, weight(1f),
   48 dp, same style, neutral wording, nothing pre-selected.
5. All texts in strings.xml. Previews: phone, 200% font, dark.

Tests:
- PlusViewModelTest: unlock click only opens the dialog; Not now -> no purchase; confirm -> UNLOCKED;
  Failed -> FAILED; restore true/false; reset -> RESET and locked.
- androidTest PlusScreenTest: demo notice and price are visible before opening the dialog; the
  dialog shows "Not now", "Unlock (demo)" and the parent/guardian sentence; both buttons have the
  same width (compare getBoundsInRoot()); "Not now" closes it without buying.
- androidTest "no nagging" checks: QuizScreenContent and ResultScreenContent (passed and failed
  states) contain no node with text containing "Plus".
```

**自检：**

1. 设置 → QuizQuest Plus：先看到免费清单，再看到 Plus 清单和价格。
2. 点 Unlock：对话框的两个按钮一样大；点 Not now，不会购买。
3. 点 Unlock (demo)：只显示一行确认文字，没有动画，也没有声音。
4. 点 Reset demo purchase：一步就恢复为未解锁。
5. 完整玩一关，故意没过关：结果页没有任何 Plus 提示。

**提交：**

```
feat(plus): add the Plus screen with a clear demo purchase flow

- Free list first, price before buying, demo notice
- Equal-weight confirm dialog with a parent/guardian note
- One-step reset; no celebration after buying
- ViewModel, UI and "no nagging" tests
```

### P5：外观主题包

```
Step P5 of QuizQuest Plus: colour theme packs (Lecture 3). Dynamic colour stays off.

1. ui/theme/ThemePalettes.kt: light + dark ColorScheme per ThemePack. CLASSIC = the current
   schemes (move them, unchanged). New packs as Material 3 tonal schemes from these seeds:
   OCEAN #006874, FOREST #386A20, SUNSET #8B5000. fun colorSchemeFor(pack: ThemePack, dark: Boolean)
2. QuizQuestTheme gets themePack: ThemePack = ThemePack.CLASSIC.
3. Reuse ui/theme/ContrastChecker.kt from step A6 and extend ThemeContrastTest so it loops over
   every pack, light and dark: >= 4.5:1 for primary/onPrimary, primaryContainer/onPrimaryContainer,
   secondaryContainer/onSecondaryContainer, tertiaryContainer/onTertiaryContainer,
   errorContainer/onErrorContainer, surface/onSurface, surfaceVariant/onSurfaceVariant,
   background/onBackground; >= 3:1 for primary/surface. Use Color.toArgb(). If a colour fails,
   change the colour – never lower the threshold (A2 commitment: contrast-checked theme).
4. MainViewModel: inject PurchaseRepository; themePack = FeatureGate.effectiveTheme(
   settings.themePack, plusUnlocked); MainActivity passes it to QuizQuestTheme.
5. Settings: SettingsViewModel.updateThemePack(pack) and isPlusUnlocked in the state. SettingsScreen
   "Colour theme" section: one 48 dp row per pack (RadioButton + name + three decorative swatches,
   Role.RadioButton). Locked packs show a lock icon and "Plus"; tapping one does NOT change the
   setting – it calls onPlusClicked() (information only). stateDescription for a locked row:
   "Locked, part of QuizQuest Plus".
6. All labels in strings.xml; previews in each pack.

Tests: ThemeContrastTest; MainViewModelTest (locked -> CLASSIC, unlocked -> OCEAN);
SettingsViewModelTest (updateThemePack); androidTest SettingsScreenTest (a locked pack shows "Plus"
and calls onPlusClicked, not updateThemePack).
```

**自检：**

- 未解锁时，海洋、森林、日落三套主题都带锁，点一下会跳到 Plus 页。
- 解锁后选择海洋，整个 app 换成海洋配色，深色模式也正常。
- 点 Reset 之后，配色回到 Classic。

**提交：**

```
feat(theme): add Plus colour theme packs with contrast tests

- Ocean, Forest, Sunset light/dark schemes; Classic stays free
- WCAG contrast test for every pack
- Locked packs only open the Plus screen; theme falls back to Classic
```

### P6：答题 ViewModel 支持练习模式（重构，不改现有行为）

```
Step P6 of QuizQuest Plus: refactor QuizViewModel so it can run a practice quiz. Levels must work
exactly as before.

1. data/model/QuizRequest.kt:
   sealed interface QuizRequest {
       data class Level(val categoryId: Int, val level: Int) : QuizRequest
       data class Practice(val categoryId: Int, val difficulty: Difficulty, val amount: Int) : QuizRequest
   }
2. QuizViewModel takes request: QuizRequest instead of categoryId and level.
   Level -> LevelPolicy.difficultyFor(level) and settings.questionsPerLevel (as now).
   Practice -> its own difficulty and amount (coerce to 5..20). QuizUIState gets isPractice.
3. di/Modules.kt: viewModel { (request: QuizRequest) -> QuizViewModel(request = request, ...) }.
   QuizScreen(categoryId, level) builds QuizRequest.Level and passes it with parametersOf(request).
4. Do not change saving yet.

Tests: update every QuizViewModelTest constructor to QuizRequest.Level – all existing tests must
still pass. Add: a Practice request asks FakeTriviaRepository for the given difficulty and amount.
```

**提交：**

```
refactor(quiz): QuizViewModel takes a QuizRequest (level or practice)
```

### P7：自由练习

```
Step P7 of QuizQuest Plus: Free practice. Read the Plus rules again.

1. QuizViewModel, Practice mode finished: do NOT call progressRepository.saveQuizResult (no stars,
   level progress, daily goal or history – practice stores less data, Ethics 2.2(d)). DO call
   reviewRepository.addWrongAnswers (try/catch). Set practiceSummary = PracticeSummary(correct,
   total) instead of finishedSessionId.
2. QuizScreen in practice mode: title "Practice". When practiceSummary is set, show "Practice
   complete: %1$d / %2$d. Wrong answers were added to Review." with two equal-weight
   OutlinedButtons "Practice again" (restart() with the same request) and "Done" (onBackPressed).
   No stars, no Plus offers.
3. Navigation: Screens.PracticeSetupScreen("practiceSetup") and the route
   "practice/{categoryId}/{difficulty}/{amount}" (difficulty = enum name) showing QuizScreen with
   QuizRequest.Practice.
4. PracticeSetupViewModel(triviaRepository, purchaseRepository) + PracticeSetupScreen:
   - locked: short explanation + "Learn about Plus" (safety net);
   - unlocked: choose a world (RadioButton rows), difficulty (FilterChips), number of questions
     (FilterChips 5/10/15/20, default 10), Start (48 dp);
   - no categories offline: the same "worlds unavailable" message with Retry as Home.
5. Home: isPlusUnlocked from PurchaseRepository. A "Free practice" card under the Review shortcut,
   same style as the other cards. Unlocked -> practiceSetup; locked -> small "Plus" label, opens
   the Plus screen (information only, never a purchase dialog).
6. All texts in strings.xml.

Tests: QuizViewModelTest (practice does not call saveQuizResult, calls addWrongAnswers, sets
practiceSummary, restart works); PracticeSetupViewModelTest; HomeViewModelTest (isPlusUnlocked);
androidTest: locked PracticeSetupScreen shows "Learn about Plus".
```

**自检：**

- 解锁后练习一轮：
  - 结束后显示"x / y"；
  - 答错的题进入 Review；
  - 统计页和关卡星级都不变。
- 未解锁时，首页卡片带 "Plus" 标签，点进去只有说明，不会弹出购买对话框。

**提交：**

```
feat(practice): add Free practice for Plus

- Choose world, difficulty and number of questions
- Practice does not change stars, levels, daily goal or history
- Wrong answers still go to Review; Home card opens the Plus screen when locked
```

### P8：学习报告的计算（纯 Kotlin）

```
Step P8 of QuizQuest Plus: the data for the Detailed learning report. No UI.

1. data/model/LearningReport.kt:
   data class AccuracyStat(val answered: Int, val correct: Int)
   data class LearningReport(
       val accuracyByDifficulty: Map<Difficulty, AccuracyStat>,
       val last30Days: List<DailyActivity>,
       val averageSecondsPerAnswer: Double?,
       val strongestWorld: String?,      // highest accuracy, at least 10 answers
       val worldToPractise: String?,     // lowest accuracy, at least 10 answers
       val totalSessions: Int,
   )
2. data/rules/LearningReportBuilder.build(sessions, answers, categoryNames: Map<Int, String>,
   nowMillis, timeZone): LearningReport
   - session difficulty = LevelPolicy.difficultyFor(session.level)
   - last30Days with DailyGoalTracker.startOfDayMillis (like last7Days in StatsRepositoryImpl)
   - average time uses timeTakenMs > 0 only; KDoc: answers may be deleted by the retention setting,
     so this covers kept answers only.
   - Everything is computed on the phone; nothing is sent anywhere (Conduct 1.2.2(c)).
3. AnswerRecordDao: @Query("SELECT * FROM AnswerRecord") fun getAllAnswers(): Flow<List<AnswerRecordEntity>>
4. StatsRepository.getLearningReport(): Flow<LearningReport> in StatsRepositoryImpl (inject
   AnswerRecordDao; update Koin and fakes).

Tests (LearningReportBuilderTest): empty data; accuracy split by difficulty (levels 1–3/4–6/7–9);
30 days with today last; the 10-answer minimum; average ignores 0; a day boundary in Australia/Sydney.
```

**提交：**

```
feat(report): build the detailed learning report on the device

- LearningReportBuilder (pure Kotlin) with unit tests
- Accuracy by difficulty, 30-day activity, average answer time, strongest/practise worlds
```

### P9：详细报告页 + 保存为文本文件

```
Step P9 of QuizQuest Plus: the Detailed report screen. It reuses ExportWriter from step R11
(data/export). If ExportWriter does not exist yet, stop and tell me.

1. Screens.ReportScreen("report") in AppNavHost (not a tab).
2. ReportViewModel(statsRepository, purchaseRepository, exportWriter) + ReportUIState:
   isLoading, isUnlocked, report, isSaving, message (SAVED / FAILED);
   saveReport(uriString: String, text: String) via exportWriter (try/catch).
3. views/ReportScreen.kt (+ ReportScreenContent), max width 600 dp:
   - locked: explanation + "Learn about Plus";
   - unlocked: "Accuracy by difficulty" (three rows, LinearProgressIndicator + percentage text –
     not colour alone); "Last 30 days" bar chart with ONE contentDescription summary for TalkBack
     (e.g. "You answered 120 questions on 9 of the last 30 days"); "Average time per answer" (or
     "Not enough data yet"); "Strongest world" / "World to practise" (or "Answer at least 10
     questions in a world"); empty state when there are no sessions.
   - Wording is encouraging and neutral – no ranking against other learners, no "you are behind".
   - "Save report": CreateDocument("text/plain"), name "quizquest-report-<yyyy-MM-dd>.txt"
     (SimpleDateFormat; minSdk 24). Build the text in the views layer with
     fun LearningReport.toPlainText(context: Context): String using string resources, then call
     viewModel.saveReport(uri.toString(), text).
4. StatsScreen: "Detailed report" card at the bottom. Unlocked -> report; locked -> small "Plus"
   label and it opens the Plus screen (information only). The free per-world accuracy on Stats
   stays as it is.
5. All texts in strings.xml (plurals for numbers).

Tests: ReportViewModelTest (locked, unlocked, save success/failure); androidTest ReportScreenTest
(percentages shown as text; locked state shows "Learn about Plus").
```

**自检：**

- 打开 TalkBack，30 天的图表只朗读一句总结。
- 点 Save report，保存的文本文件可以正常打开阅读。
- 统计页原本免费显示的每个世界的正确率没有被挪走。

**提交：**

```
feat(report): add the Detailed learning report screen for Plus

- Accuracy by difficulty, 30-day chart with a TalkBack summary, neutral wording
- Save as a text file; free per-world accuracy on Stats is unchanged
```

### P10：隐私页与文档

```
Step P10 of QuizQuest Plus: privacy copy and documentation. No new features.

1. Privacy screen (strings.xml):
   - "What is stored": add "your QuizQuest Plus unlock (demo)".
   - Delete dialog text: add "and your QuizQuest Plus demo unlock".
   - New section "Purchases": "QuizQuest Plus is a demo: no payment is made and no payment details
     are collected. The unlock is saved only on this phone. If you delete all your data, the demo
     unlock is deleted too."
2. README.md:
   - Features table: Free practice, Detailed report, Theme packs marked "QuizQuest Plus (demo)".
   - New section "QuizQuest Plus (demo monetisation)":
     • the free/Plus table and why learning is never paid for (ACS Ethics 2.3.1, Conduct 1.2.2(a));
     • a table "Assessment 2 → design decision" (slides 7, 8, 11, 12, 13, 16, 17, 24, 26 as listed
       in AGENTS.md 5.x);
     • the dark-pattern checklist (Gray et al., 2018) with the test or check for each row;
     • why it is a demo, and the "demo gate" (Koin binds only DemoPurchaseRepository, like the
       NoOpAnalytics gate in Assessment 2);
     • how a real version would work: Google Play Billing Library (BillingClient, product details,
       launchBillingFlow, acknowledgement, restore with queryPurchasesAsync); Google Play processes
       the payment; the library adds the BILLING permission; parents can use Google Play's family
       purchase approvals; the Data safety form must declare Financial info → Purchase history;
       refunds would follow Google Play's refund process;
     • legislation to watch (ACS Conduct 1.2.4(c)): Australia's unfair trading practices laws on
       subscription traps and manipulative design (from 1 July 2027), the Children's Online Privacy
       Code, and Google Play's Families policy.
   - Data safety table: new row "Purchase history | Stored on device only (demo) | No | Demo unlock;
     a real version would receive purchase details from Google Play".
   - Architecture notes: Room version 3 and AutoMigration 2 -> 3.
3. docs/ethics-review-draft.md (NOT docs/ethics-review.md, which I write myself): add a draft entry
   "QuizQuest Plus" in the same five-step format as step A7. Use this request as the dilemma: "Show a Plus offer on the Result screen after a failed
   level with a 50%-off countdown, a big Unlock button and a small grey 'No thanks'." Then steps
   1–5: stakeholders; values test (Conduct 1.2.3(b), 1.2.2(d), Ethics 2.1(b), Conduct 1.2.2(a));
   resolution (Conduct 1.2.1); least-harm design (what we built); document & push back in writing
   (Ethics 2.1(c), Conduct 1.2.4(e)). Add the evidence (test classes) for each decision.
4. docs/git-timeline.md: add the P1–P10 commits (facts only). Do not edit docs/dev-log.md – I write it.
5. Release Merged Manifest still has only INTERNET. Run ./gradlew testDebugUnitTest and give me the
   updated test table.
```

**提交：**

```
docs(plus): A2-based ethics review, data safety and README for the demo purchase
```

---

## 三、做完后检查一遍（对照 A2）

- [ ] **Nagging：** 完整玩一关，包括没过关的情况：答题页和结果页都没有 Plus 提示，也没有通知。
- [ ] **Forced action：** 不解锁也能使用所有关卡、复习、统计（包括每个世界的正确率）、设置和隐私功能。
- [ ] **Sneaking：** 打开对话框之前就能看到价格，写明是一次性付费、没有订阅、没有预选。
- [ ] **Interface interference：** 两个按钮一样大，文案中性。
- [ ] **Obstruction：** Reset 和购买一样只要一步。
- [ ] **第 16 页：** 购买后没有撒花、动画或音效。
- [ ] **Ethics 2.1(b)：** 每个购买界面都写着 "Demo – no real payment is made"。
- [ ] **Ethics 2.2(d)：** 导出的 JSON 里有 `purchases`；删除所有数据后，Plus 恢复为未解锁，配色回到 Classic。
- [ ] **第 24 页：** release 版合并后的 Manifest 只有 INTERNET；代码里只有 `DemoPurchaseRepository` 一个实现。
- [ ] **第 12 页 Ethics 2.3.1(c)(d)：** 用 TalkBack 走一遍 Plus 页、主题选择和报告页。
- [ ] **承诺 #8：** ethics-review.md 里有每个功能的五步检查，包括 Plus。
