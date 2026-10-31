package com.clcwpwqi.cms2027.ui

object S {
    private val zhCN = mapOf(
        "nav.home" to "主页", "nav.history" to "历史", "nav.settings" to "设置",
        "home.title" to "CMS-2027",
        "home.subtitle" to "高强度文本加密封装。AES-256-GCM，口令派生六十万次。两端结果可互相解密。",
        "home.encrypt" to "加密", "home.decrypt" to "解密",
        "home.modeNormal" to "普通模式", "home.modeStrong" to "强加密模式",
        "home.inputEncrypt" to "输入要加密的文本…", "home.inputDecrypt" to "粘贴 CMS-2027 密文…",
        "home.output" to "结果将显示在这里", "home.passphrase" to "密钥",
        "home.passphraseHint" to "输入自定义密钥",
        "home.paste" to "粘贴", "home.scan" to "扫描二维码", "home.clear" to "清空",
        "home.copy" to "复制", "home.qr" to "生成二维码",
        "home.working" to "正在运算…", "home.copied" to "已复制",
        "home.pasteFailed" to "无法读取剪贴板，请手动粘贴。",
        "home.copyFailed" to "复制失败。",
        "home.decryptFailed" to "解密失败：密钥错误或数据已被篡改。",
        "home.invalidPayload" to "不是合法的 CMS-2027 密文。",
        "home.emptyInput" to "请先输入内容。", "home.emptyKey" to "强加密模式需要填写密钥。",
        "home.qrTooLong" to "内容过长，无法生成二维码。",
        "history.title" to "历史记录", "history.search" to "搜索原文、密文或密钥",
        "history.empty" to "暂无记录", "history.emptyHint" to "完成一次加密或解密后，记录会出现在这里。",
        "history.typeAll" to "全部类型", "history.typeEncrypt" to "加密", "history.typeDecrypt" to "解密",
        "history.modeAll" to "全部模式", "history.modeNormal" to "普通", "history.modeStrong" to "强加密",
        "history.timeAll" to "全部时间", "history.timeToday" to "今天", "history.time7d" to "近 7 天", "history.time30d" to "近 30 天",
        "history.select" to "多选", "history.selectAll" to "全选", "history.deleteSelected" to "删除所选",
        "history.cancel" to "取消", "history.clearAll" to "清空全部",
        "history.clearConfirm" to "确定清空全部历史记录？此操作不可撤销。",
        "history.deleteConfirm" to "确定删除所选记录？",
        "history.keyLabel" to "密钥",
        "settings.appearance" to "外观", "settings.theme" to "主题",
        "settings.themeSystem" to "跟随系统", "settings.themeLight" to "浅色", "settings.themeDark" to "深色",
        "settings.color" to "取色", "settings.colorDefault" to "系统取色", "settings.colorCustom" to "自定义",
        "settings.language" to "语言", "settings.langAuto" to "跟随系统",
        "settings.about" to "关于",
        "settings.appDesc" to "跨平台文本加密封装工具。Web 与 Android 使用同一协议，密文可互相解密。",
        "settings.repo" to "代码仓库", "settings.developer" to "开发者",
        "settings.contact" to "联系方式", "settings.algorithm" to "算法",
        "settings.algoDetail" to "AES-256-GCM + PBKDF2-HMAC-SHA256（600,000 次迭代）",
        "dialog.qrTitle" to "二维码", "dialog.qrDownload" to "分享", "dialog.close" to "关闭",
        "dialog.confirm" to "确定", "dialog.cancel" to "取消",
        "lang.zh-CN" to "简体中文", "lang.zh-TW" to "繁體中文", "lang.en" to "English",
        "lang.ru" to "Русский", "lang.fr" to "Français", "lang.de" to "Deutsch", "lang.la" to "Latina",
    )
    private val zhTW = zhCN + mapOf(
        "nav.home" to "主頁", "nav.history" to "歷史", "nav.settings" to "設定",
        "home.subtitle" to "高強度文字加密封裝。AES-256-GCM，口令派生六十萬次。兩端結果可互相解密。",
        "home.encrypt" to "加密", "home.decrypt" to "解密",
        "home.modeNormal" to "普通模式", "home.modeStrong" to "強加密模式",
        "home.inputEncrypt" to "輸入要加密的文字…", "home.inputDecrypt" to "貼上 CMS-2027 密文…",
        "home.output" to "結果將顯示在這裡", "home.passphrase" to "金鑰",
        "home.passphraseHint" to "輸入自訂金鑰",
        "home.paste" to "貼上", "home.scan" to "掃描 QR 碼", "home.clear" to "清空",
        "home.copy" to "複製", "home.qr" to "產生 QR 碼",
        "home.working" to "正在運算…", "home.copied" to "已複製",
        "home.pasteFailed" to "無法讀取剪貼簿，請手動貼上。",
        "home.decryptFailed" to "解密失敗：金鑰錯誤或資料已被竄改。",
        "home.invalidPayload" to "不是合法的 CMS-2027 密文。",
        "home.emptyInput" to "請先輸入內容。", "home.emptyKey" to "強加密模式需要填寫金鑰。",
        "home.qrTooLong" to "內容過長，無法產生 QR 碼。",
        "history.title" to "歷史紀錄", "history.search" to "搜尋原文、密文或金鑰",
        "history.empty" to "暫無紀錄", "history.emptyHint" to "完成一次加密或解密後，紀錄會出現在這裡。",
        "history.clearAll" to "清空全部", "history.clearConfirm" to "確定清空全部歷史紀錄？此操作無法復原。",
        "history.deleteConfirm" to "確定刪除所選紀錄？", "history.keyLabel" to "金鑰",
        "settings.appearance" to "外觀", "settings.theme" to "主題",
        "settings.themeSystem" to "跟隨系統", "settings.themeLight" to "淺色", "settings.themeDark" to "深色",
        "settings.color" to "取色", "settings.colorDefault" to "系統取色", "settings.colorCustom" to "自訂",
        "settings.language" to "語言", "settings.langAuto" to "跟隨系統",
        "settings.about" to "關於",
        "settings.appDesc" to "跨平台文字加密封裝工具。Web 與 Android 使用同一協定，密文可互相解密。",
        "settings.repo" to "程式碼倉庫", "settings.developer" to "開發者",
        "settings.algorithm" to "演算法",
        "dialog.qrTitle" to "QR 碼", "dialog.close" to "關閉", "dialog.confirm" to "確定", "dialog.cancel" to "取消",
    )
    private val en = mapOf(
        "nav.home" to "Home", "nav.history" to "History", "nav.settings" to "Settings",
        "home.title" to "CMS-2027",
        "home.subtitle" to "High-strength text encapsulation. AES-256-GCM with 600,000 PBKDF2 rounds.",
        "home.encrypt" to "Encrypt", "home.decrypt" to "Decrypt",
        "home.modeNormal" to "Normal", "home.modeStrong" to "Strong",
        "home.inputEncrypt" to "Enter text to encrypt…", "home.inputDecrypt" to "Paste a CMS-2027 payload…",
        "home.output" to "The result will appear here", "home.passphrase" to "Passphrase",
        "home.passphraseHint" to "Enter a custom passphrase",
        "home.paste" to "Paste", "home.scan" to "Scan QR", "home.clear" to "Clear",
        "home.copy" to "Copy", "home.qr" to "Make QR",
        "home.working" to "Working…", "home.copied" to "Copied",
        "home.pasteFailed" to "Clipboard unavailable.", "home.copyFailed" to "Copy failed.",
        "home.decryptFailed" to "Decryption failed: wrong passphrase or tampered data.",
        "home.invalidPayload" to "Not a valid CMS-2027 payload.",
        "home.emptyInput" to "Please enter some text first.", "home.emptyKey" to "Strong mode requires a passphrase.",
        "home.qrTooLong" to "Content is too long for a QR code.",
        "history.title" to "History", "history.search" to "Search plaintext, ciphertext, or passphrase",
        "history.empty" to "No records yet", "history.emptyHint" to "Records appear after you encrypt or decrypt.",
        "history.typeAll" to "All types", "history.typeEncrypt" to "Encrypt", "history.typeDecrypt" to "Decrypt",
        "history.modeAll" to "All modes", "history.modeNormal" to "Normal", "history.modeStrong" to "Strong",
        "history.timeAll" to "Any time", "history.timeToday" to "Today", "history.time7d" to "Last 7 days", "history.time30d" to "Last 30 days",
        "history.select" to "Select", "history.selectAll" to "Select all", "history.deleteSelected" to "Delete selected",
        "history.cancel" to "Cancel", "history.clearAll" to "Clear all",
        "history.clearConfirm" to "Clear all history? This cannot be undone.",
        "history.deleteConfirm" to "Delete the selected records?",
        "history.keyLabel" to "Key",
        "settings.appearance" to "Appearance", "settings.theme" to "Theme",
        "settings.themeSystem" to "System", "settings.themeLight" to "Light", "settings.themeDark" to "Dark",
        "settings.color" to "Color", "settings.colorDefault" to "System", "settings.colorCustom" to "Custom",
        "settings.language" to "Language", "settings.langAuto" to "System",
        "settings.about" to "About",
        "settings.appDesc" to "Cross-platform text encapsulation. Web and Android share one protocol.",
        "settings.repo" to "Repository", "settings.developer" to "Developer",
        "settings.contact" to "Contact", "settings.algorithm" to "Algorithm",
        "settings.algoDetail" to "AES-256-GCM + PBKDF2-HMAC-SHA256 (600,000 iterations)",
        "dialog.qrTitle" to "QR code", "dialog.qrDownload" to "Share", "dialog.close" to "Close",
        "dialog.confirm" to "Confirm", "dialog.cancel" to "Cancel",
        "lang.zh-CN" to "简体中文", "lang.zh-TW" to "繁體中文", "lang.en" to "English",
        "lang.ru" to "Русский", "lang.fr" to "Français", "lang.de" to "Deutsch", "lang.la" to "Latina",
    )
    private val ru = en + mapOf(
        "nav.home" to "Главная", "nav.history" to "История", "nav.settings" to "Настройки",
        "home.subtitle" to "Надёжная упаковка текста. AES-256-GCM и 600 000 раундов PBKDF2.",
        "home.encrypt" to "Шифровать", "home.decrypt" to "Расшифровать",
        "home.modeNormal" to "Обычный", "home.modeStrong" to "Сильный",
        "home.inputEncrypt" to "Введите текст…", "home.inputDecrypt" to "Вставьте пакет CMS-2027…",
        "home.output" to "Результат появится здесь", "home.passphrase" to "Пароль",
        "home.working" to "Вычисление…", "home.copied" to "Скопировано",
        "home.decryptFailed" to "Ошибка расшифровки: неверный пароль или данные изменены.",
        "history.title" to "История", "history.empty" to "Записей пока нет",
        "settings.appearance" to "Оформление", "settings.theme" to "Тема",
        "settings.themeSystem" to "Система", "settings.themeLight" to "Светлая", "settings.themeDark" to "Тёмная",
        "settings.language" to "Язык", "settings.about" to "О программе",
        "dialog.close" to "Закрыть", "dialog.confirm" to "Подтвердить", "dialog.cancel" to "Отмена",
    )
    private val fr = en + mapOf(
        "nav.home" to "Accueil", "nav.history" to "Historique", "nav.settings" to "Réglages",
        "home.subtitle" to "Encapsulation de texte à haute résistance. AES-256-GCM et 600 000 tours PBKDF2.",
        "home.encrypt" to "Chiffrer", "home.decrypt" to "Déchiffrer",
        "home.modeNormal" to "Normal", "home.modeStrong" to "Fort",
        "home.working" to "Calcul…", "home.copied" to "Copié",
        "history.title" to "Historique", "history.empty" to "Aucun enregistrement",
        "settings.appearance" to "Apparence", "settings.theme" to "Thème",
        "settings.themeSystem" to "Système", "settings.themeLight" to "Clair", "settings.themeDark" to "Sombre",
        "settings.language" to "Langue", "settings.about" to "À propos",
        "dialog.close" to "Fermer", "dialog.confirm" to "Confirmer", "dialog.cancel" to "Annuler",
    )
    private val de = en + mapOf(
        "nav.home" to "Start", "nav.history" to "Verlauf", "nav.settings" to "Einstellungen",
        "home.subtitle" to "Starke Textkapselung. AES-256-GCM mit 600.000 PBKDF2-Runden.",
        "home.encrypt" to "Verschlüsseln", "home.decrypt" to "Entschlüsseln",
        "home.modeNormal" to "Normal", "home.modeStrong" to "Stark",
        "home.working" to "Berechne…", "home.copied" to "Kopiert",
        "history.title" to "Verlauf", "history.empty" to "Noch keine Einträge",
        "settings.appearance" to "Erscheinungsbild", "settings.theme" to "Thema",
        "settings.themeSystem" to "System", "settings.themeLight" to "Hell", "settings.themeDark" to "Dunkel",
        "settings.language" to "Sprache", "settings.about" to "Über",
        "dialog.close" to "Schließen", "dialog.confirm" to "Bestätigen", "dialog.cancel" to "Abbrechen",
    )
    private val la = en + mapOf(
        "nav.home" to "Domus", "nav.history" to "Historia", "nav.settings" to "Optiones",
        "home.subtitle" to "Inclusio textus firma. AES-256-GCM et sescenta milia iterationum PBKDF2.",
        "home.encrypt" to "Obtege", "home.decrypt" to "Aperi",
        "home.modeNormal" to "Communis", "home.modeStrong" to "Fortis",
        "home.working" to "Computatur…", "home.copied" to "Exscriptum",
        "history.title" to "Historia", "history.empty" to "Nondum monumenta",
        "settings.appearance" to "Species", "settings.theme" to "Thema",
        "settings.themeSystem" to "Systema", "settings.themeLight" to "Clarum", "settings.themeDark" to "Obscurum",
        "settings.language" to "Lingua", "settings.about" to "De re",
        "dialog.close" to "Claude", "dialog.confirm" to "Confirma", "dialog.cancel" to "Omitte",
    )

    private val all = mapOf(
        "zh-CN" to zhCN, "zh-TW" to zhTW, "en" to en, "ru" to ru, "fr" to fr, "de" to de, "la" to la,
    )

    fun detect(pref: String): String {
        if (pref != "auto" && all.containsKey(pref)) return pref
        val nav = java.util.Locale.getDefault().toLanguageTag().lowercase()
        return when {
            nav.startsWith("zh-hant") || nav.startsWith("zh-tw") || nav.startsWith("zh-hk") -> "zh-TW"
            nav.startsWith("zh") -> "zh-CN"
            nav.startsWith("ru") -> "ru"
            nav.startsWith("fr") -> "fr"
            nav.startsWith("de") -> "de"
            nav.startsWith("la") -> "la"
            else -> "en"
        }
    }

    fun t(langPref: String, key: String): String {
        val lang = detect(langPref)
        return all[lang]?.get(key) ?: en[key] ?: key
    }
}
