# CMS-2027

跨平台文本加密封装工具。Web 与 Android 共用同一协议，密文可互相解密。

- 算法：AES-256-GCM + PBKDF2-HMAC-SHA256（600,000 次迭代）
- 普通模式使用内置密钥；强加密模式使用自定义密钥
- 历史记录、二维码生成/识别、七种语言、Material 3 动态取色

## Web

纯静态站点，目录为 `web/`。

本地预览：

```bash
python3 -m http.server 8080 --directory web
```

GitHub Pages 由 `.github/workflows/pages.yml` 自动部署 `web/`。

## Android

Android 工程在仓库根目录，`minSdk 26`。

```bash
./gradlew assembleRelease
```

产物：`app/build/outputs/apk/release/`。

推送到 `main`/`master` 或打 `v*` 标签时，`.github/workflows/android.yml` 会构建 APK 并上传为 Artifact。

## 算法规范

见 `cms_2027/SPEC.md`。JavaScript 与 Kotlin 实现分别位于：

- `cms_2027/js/cms2027.js`
- `cms_2027/kotlin/Cms2027.kt`

## 开发者

clcwpwqi · [GitHub](https://github.com/clcwpwqi/CMS-2027) · Telegram / 微信公众号 @clcwpwqi · clcwpwqi@outlook.com
