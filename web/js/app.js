/**
 * CMS-2027 — 主界面逻辑
 * 路由、加解密、历史、设置、二维码
 */
import { applyTheme, DEFAULT_SEED, PRESET_SEEDS } from "./theme.js";
import { t, setLanguage, onLanguageChange, LANGS, getLanguage } from "./i18n.js";
import {
  loadSettings,
  saveSettings,
  loadHistory,
  addHistory,
  deleteHistory,
  clearHistory,
} from "./store.js";

const QR_MAX = 2900;
const GMT8 = "Asia/Shanghai";

let settings = loadSettings();
let op = "encrypt";
let mode = "normal";
let selecting = false;
let selectedIds = new Set();
let filters = { type: "all", mode: "all", time: "all" };
let scanTimer = null;
let scanStream = null;
let confirmResolver = null;

const $ = (id) => document.getElementById(id);

function toast(msg) {
  const el = $("toast");
  el.textContent = msg;
  el.classList.add("show");
  clearTimeout(toast._t);
  toast._t = setTimeout(() => el.classList.remove("show"), 1800);
}

function showBanner(msg, ok) {
  const el = $("error-banner");
  if (!msg) {
    el.classList.remove("show", "ok");
    el.textContent = "";
    return;
  }
  el.textContent = msg;
  el.classList.add("show");
  el.classList.toggle("ok", !!ok);
}

async function copyText(text) {
  try {
    await navigator.clipboard.writeText(text);
    toast(t("home.copied"));
    return true;
  } catch (e) {
    try {
      const ta = document.createElement("textarea");
      ta.value = text;
      document.body.appendChild(ta);
      ta.select();
      document.execCommand("copy");
      ta.remove();
      toast(t("home.copied"));
      return true;
    } catch (err) {
      toast(t("home.copyFailed"));
      return false;
    }
  }
}

function formatTime(ts) {
  return new Intl.DateTimeFormat(getLanguage(), {
    timeZone: GMT8,
    dateStyle: "medium",
    timeStyle: "medium",
  }).format(new Date(ts));
}

function truncate(s, n) {
  if (!s) return "";
  const chars = Array.from(s);
  return chars.length > n ? chars.slice(0, n).join("") + "…" : s;
}

function persist() {
  saveSettings(settings);
}

function refreshTheme() {
  const seed = settings.colorMode === "custom" ? settings.seed : DEFAULT_SEED;
  applyTheme(seed, settings.themeMode);
}

function currentRoute() {
  const h = (location.hash || "#/").replace(/^#/, "");
  if (h.startsWith("/history")) return "/history";
  if (h.startsWith("/settings")) return "/settings";
  return "/";
}

function showView(route) {
  document.querySelectorAll(".view").forEach((v) => v.classList.remove("active"));
  document.querySelectorAll(".nav-pills a").forEach((a) => {
    a.classList.toggle("active", a.getAttribute("data-route") === route);
  });
  const map = { "/": "view-home", "/history": "view-history", "/settings": "view-settings" };
  $(map[route] || "view-home").classList.add("active");
  if (route === "/history") renderHistory();
  if (route === "/settings") renderSettings();
}

function updateOpUi() {
  $("tab-encrypt").classList.toggle("active", op === "encrypt");
  $("tab-decrypt").classList.toggle("active", op === "decrypt");
  $("run-label").textContent = t(op === "encrypt" ? "home.encryptAction" : "home.decryptAction");
  $("input-text").placeholder = t(
    op === "encrypt" ? "home.inputPlaceholderEncrypt" : "home.inputPlaceholderDecrypt"
  );
}

function updateModeUi() {
  $("mode-normal").classList.toggle("active", mode === "normal");
  $("mode-strong").classList.toggle("active", mode === "strong");
  $("key-row").classList.toggle("visible", mode === "strong");
}

function passphrase() {
  return mode === "strong" ? $("passphrase").value : window.CMS2027.BUILTIN_KEY;
}

function setLoading(on) {
  const btn = $("btn-run");
  btn.disabled = on;
  if (on) {
    btn.innerHTML = '<span class="spinner"></span><span>' + t("home.working") + "</span>";
  } else {
    btn.innerHTML = '<span id="run-label">' + t(op === "encrypt" ? "home.encryptAction" : "home.decryptAction") + "</span>";
  }
}

async function runCrypto() {
  const input = $("input-text").value;
  showBanner("");
  if (!input.trim()) {
    showBanner(t("home.emptyInput"));
    return;
  }
  if (mode === "strong" && !$("passphrase").value) {
    showBanner(t("home.emptyKey"));
    return;
  }
  setLoading(true);
  try {
    const key = passphrase();
    if (op === "encrypt") {
      const cipher = await window.CMS2027.encrypt(input, key);
      $("output-text").value = cipher;
      addHistory({
        type: "encrypt",
        mode,
        key: mode === "strong" ? key : null,
        plain: input,
        cipher,
      });
    } else {
      const plain = await window.CMS2027.decrypt(input, key);
      $("output-text").value = plain;
      addHistory({
        type: "decrypt",
        mode,
        key: mode === "strong" ? key : null,
        plain,
        cipher: input,
      });
    }
  } catch (e) {
    const msg = e && e.message;
    if (msg === "DECRYPTION_FAILED") showBanner(t("home.decryptFailed"));
    else if (msg === "INVALID_PAYLOAD" || msg === "UNSUPPORTED_VERSION" || msg === "UNSUPPORTED_KDF" || msg === "UNSUPPORTED_CIPHER") {
      showBanner(t("home.invalidPayload"));
    } else showBanner(t("home.decryptFailed"));
  } finally {
    setLoading(false);
  }
}

function makeQrDataUrl(text) {
  const payload = unescape(encodeURIComponent(text));
  const qr = qrcode(0, "M");
  qr.addData(payload);
  qr.make();
  return qr.createDataURL(6, 4);
}

function openQr(text) {
  if (!text) {
    toast(t("home.emptyInput"));
    return;
  }
  if (Array.from(text).length > QR_MAX) {
    toast(t("home.qrTooLong"));
    return;
  }
  try {
    const url = makeQrDataUrl(text);
    $("qr-img").src = url;
    $("qr-download").href = url;
    $("qr-dialog").classList.add("show");
  } catch (e) {
    toast(t("home.qrTooLong"));
  }
}

function closeQr() {
  $("qr-dialog").classList.remove("show");
}

function stopScan() {
  if (scanTimer) {
    clearTimeout(scanTimer);
    scanTimer = null;
  }
  if (scanStream) {
    scanStream.getTracks().forEach((tr) => tr.stop());
    scanStream = null;
  }
  const v = $("scan-video");
  v.srcObject = null;
}

function onScanResult(data) {
  if (!data) return;
  stopScan();
  $("scan-dialog").classList.remove("show");
  $("input-text").value = data;
}

function tickScan() {
  const video = $("scan-video");
  const canvas = $("scan-canvas");
  if (!video.videoWidth) {
    scanTimer = setTimeout(tickScan, 200);
    return;
  }
  canvas.width = video.videoWidth;
  canvas.height = video.videoHeight;
  const ctx = canvas.getContext("2d");
  ctx.drawImage(video, 0, 0);
  const img = ctx.getImageData(0, 0, canvas.width, canvas.height);
  const code = jsQR(img.data, img.width, img.height);
  if (code && code.data) {
    $("input-text").value = code.data;
    stopScan();
    $("scan-dialog").classList.remove("show");
    return;
  }
  scanTimer = setTimeout(tickScan, 160);
}

async function openScan() {
  $("scan-dialog").classList.add("show");
  stopScan();
  try {
    scanStream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: "environment" },
    });
    $("scan-video").srcObject = scanStream;
    await $("scan-video").play();
    tickScan();
  } catch (e) {
    const name = e && e.name;
    if (name === "NotAllowedError") toast(t("dialog.scanDenied"));
    else toast(t("dialog.scanNoCamera"));
  }
}

function closeScan() {
  stopScan();
  $("scan-dialog").classList.remove("show");
}

function decodeImageFile(file) {
  const img = new Image();
  img.onload = () => {
    const canvas = $("scan-canvas");
    canvas.width = img.width;
    canvas.height = img.height;
    const ctx = canvas.getContext("2d");
    ctx.drawImage(img, 0, 0);
    const data = ctx.getImageData(0, 0, canvas.width, canvas.height);
    const code = jsQR(data.data, data.width, data.height);
    URL.revokeObjectURL(img.src);
    if (code && code.data) {
      $("input-text").value = code.data;
      closeScan();
    } else {
      toast(t("dialog.scanNotFound"));
    }
  };
  img.src = URL.createObjectURL(file);
}

function confirmDialog(message) {
  $("confirm-title").textContent = t("dialog.confirm");
  $("confirm-msg").textContent = message;
  $("confirm-dialog").classList.add("show");
  return new Promise((resolve) => {
    confirmResolver = resolve;
  });
}

function closeConfirm(ok) {
  $("confirm-dialog").classList.remove("show");
  if (confirmResolver) confirmResolver(ok);
  confirmResolver = null;
}

function inTimeRange(ts, range) {
  if (range === "all") return true;
  const now = Date.now();
  if (range === "today") {
    const d = new Date();
    const utc = d.getTime() + d.getTimezoneOffset() * 60000;
    const sh = new Date(utc + 8 * 3600000);
    sh.setHours(0, 0, 0, 0);
    const start = sh.getTime() - 8 * 3600000;
    return ts >= start;
  }
  if (range === "7d") return ts >= now - 7 * 86400000;
  if (range === "30d") return ts >= now - 30 * 86400000;
  return true;
}

function filteredHistory() {
  const q = ($("history-search").value || "").toLowerCase();
  return loadHistory().filter((item) => {
    if (filters.type !== "all" && item.type !== filters.type) return false;
    if (filters.mode !== "all" && item.mode !== filters.mode) return false;
    if (!inTimeRange(item.ts, filters.time)) return false;
    if (q) {
      const blob = ((item.plain || "") + " " + (item.cipher || "") + " " + (item.key || "")).toLowerCase();
      if (!blob.includes(q)) return false;
    }
    return true;
  });
}

function renderChips() {
  const groups = [
    [
      ["all", "history.typeAll"],
      ["encrypt", "history.typeEncrypt"],
      ["decrypt", "history.typeDecrypt"],
    ],
    [
      ["all", "history.modeAll"],
      ["normal", "history.modeNormal"],
      ["strong", "history.modeStrong"],
    ],
    [
      ["all", "history.timeAll"],
      ["today", "history.timeToday"],
      ["7d", "history.time7d"],
      ["30d", "history.time30d"],
    ],
  ];
  const keys = ["type", "mode", "time"];
  const wrap = $("history-chips");
  wrap.innerHTML = "";
  groups.forEach((group, gi) => {
    group.forEach(([val, key]) => {
      const b = document.createElement("button");
      b.type = "button";
      b.className = "chip" + (filters[keys[gi]] === val ? " active" : "");
      b.textContent = t(key);
      b.addEventListener("click", () => {
        filters[keys[gi]] = val;
        renderHistory();
      });
      wrap.appendChild(b);
    });
  });
}

function renderToolbar() {
  const bar = $("history-toolbar");
  bar.innerHTML = "";
  if (selecting) {
    const all = document.createElement("button");
    all.className = "text-btn";
    all.textContent = t("history.selectAll");
    all.onclick = () => {
      filteredHistory().forEach((i) => selectedIds.add(i.id));
      renderHistory();
    };
    const del = document.createElement("button");
    del.className = "text-btn danger";
    del.textContent = t("history.deleteSelected");
    del.onclick = async () => {
      if (!selectedIds.size) return;
      if (await confirmDialog(t("history.deleteConfirm"))) {
        deleteHistory([...selectedIds]);
        selectedIds.clear();
        selecting = false;
        renderHistory();
      }
    };
    const cancel = document.createElement("button");
    cancel.className = "text-btn";
    cancel.textContent = t("history.cancel");
    cancel.onclick = () => {
      selecting = false;
      selectedIds.clear();
      renderHistory();
    };
    bar.append(all, del, cancel);
  } else {
    const sel = document.createElement("button");
    sel.className = "text-btn";
    sel.textContent = t("history.select");
    sel.onclick = () => {
      selecting = true;
      renderHistory();
    };
    const clear = document.createElement("button");
    clear.className = "text-btn danger";
    clear.textContent = t("history.clearAll");
    clear.onclick = async () => {
      if (await confirmDialog(t("history.clearConfirm"))) {
        clearHistory();
        renderHistory();
      }
    };
    bar.append(sel, clear);
  }
}

function renderHistory() {
  renderChips();
  renderToolbar();
  const list = $("history-list");
  const items = filteredHistory();
  $("history-empty").style.display = items.length ? "none" : "block";
  list.classList.toggle("selecting", selecting);
  list.innerHTML = "";
  items.forEach((item) => {
    const row = document.createElement("div");
    row.className = "history-item";
    const check = document.createElement("input");
    check.type = "checkbox";
    check.className = "check";
    check.checked = selectedIds.has(item.id);
    check.addEventListener("change", () => {
      if (check.checked) selectedIds.add(item.id);
      else selectedIds.delete(item.id);
    });
    const body = document.createElement("div");
    const badges = document.createElement("div");
    badges.className = "badges";
    const b1 = document.createElement("span");
    b1.className = "badge";
    b1.textContent = t(item.type === "encrypt" ? "history.typeEncrypt" : "history.typeDecrypt");
    const b2 = document.createElement("span");
    b2.className = "badge";
    b2.textContent = t(item.mode === "strong" ? "history.modeStrong" : "history.modeNormal");
    badges.append(b1, b2);
    const p = document.createElement("div");
    p.className = "preview";
    p.textContent = truncate(item.plain, 80);
    const c = document.createElement("div");
    c.className = "preview muted";
    c.textContent = truncate(item.cipher, 80);
    const meta = document.createElement("div");
    meta.className = "meta";
    meta.textContent =
      formatTime(item.ts) +
      (item.mode === "strong" && item.key ? " · " + t("history.keyLabel") + " " + item.key : "");
    body.append(badges, p, c, meta);
    const actions = document.createElement("div");
    actions.className = "icon-row";
    const copyBtn = document.createElement("button");
    copyBtn.type = "button";
    copyBtn.className = "icon-btn";
    copyBtn.title = t("history.copyCipher");
    copyBtn.innerHTML = '<svg viewBox="0 0 24 24"><path d="M16 1H4a2 2 0 0 0-2 2v12h2V3h12V1zm3 4H8a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h11a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2zm0 16H8V7h11v14z"/></svg>';
    copyBtn.onclick = () => copyText(item.cipher);
    const qrBtn = document.createElement("button");
    qrBtn.type = "button";
    qrBtn.className = "icon-btn";
    qrBtn.title = t("history.makeQr");
    qrBtn.innerHTML = '<svg viewBox="0 0 24 24"><path d="M3 11h8V3H3v8zm2-6h4v4H5V5zm8-2v8h8V3h-8zm6 6h-4V5h4v4zM3 21h8v-8H3v8zm2-6h4v4H5v-4zm13-2h-2v3h-3v2h3v3h2v-3h3v-2h-3z"/></svg>';
    qrBtn.onclick = () => openQr(item.cipher);
    const delBtn = document.createElement("button");
    delBtn.type = "button";
    delBtn.className = "icon-btn danger";
    delBtn.title = t("history.clear");
    delBtn.innerHTML = '<svg viewBox="0 0 24 24"><path d="M6 19a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>';
    delBtn.onclick = async () => {
      if (await confirmDialog(t("history.deleteConfirm"))) {
        deleteHistory([item.id]);
        renderHistory();
      }
    };
    actions.append(copyBtn, qrBtn, delBtn);
    row.append(check, body, actions);
    list.appendChild(row);
  });
}

function renderSettings() {
  document.querySelectorAll("[data-theme-mode]").forEach((b) => {
    b.classList.toggle("active", b.getAttribute("data-theme-mode") === settings.themeMode);
  });
  document.querySelectorAll("[data-color-mode]").forEach((b) => {
    b.classList.toggle("active", b.getAttribute("data-color-mode") === settings.colorMode);
  });
  const grid = $("color-grid");
  const picker = $("color-picker");
  const custom = settings.colorMode === "custom";
  grid.classList.toggle("visible", custom);
  picker.classList.toggle("visible", custom);
  grid.innerHTML = "";
  PRESET_SEEDS.forEach((hex) => {
    const b = document.createElement("button");
    b.type = "button";
    b.className = "swatch" + (settings.seed.toUpperCase() === hex.toUpperCase() ? " selected" : "");
    b.style.background = hex;
    b.title = hex;
    b.onclick = () => {
      settings.seed = hex;
      $("color-input").value = hex;
      $("color-hex").textContent = hex;
      persist();
      refreshTheme();
      renderSettings();
    };
    grid.appendChild(b);
  });
  $("color-input").value = settings.seed;
  $("color-hex").textContent = settings.seed;
  const list = $("lang-list");
  list.innerHTML = "";
  LANGS.forEach((lang) => {
    const row = document.createElement("label");
    row.className = "lang-item";
    const radio = document.createElement("input");
    radio.type = "radio";
    radio.name = "lang";
    radio.value = lang.id;
    radio.checked = settings.language === lang.id;
    radio.onchange = () => {
      settings.language = lang.id;
      persist();
      setLanguage(lang.id);
    };
    const span = document.createElement("span");
    span.textContent = lang.id === "auto" ? t("settings.langAuto") : lang.native;
    row.append(radio, span);
    list.appendChild(row);
  });
}

function bind() {
  $("tab-encrypt").onclick = () => {
    op = "encrypt";
    updateOpUi();
  };
  $("tab-decrypt").onclick = () => {
    op = "decrypt";
    updateOpUi();
  };
  $("mode-normal").onclick = () => {
    mode = "normal";
    updateModeUi();
  };
  $("mode-strong").onclick = () => {
    mode = "strong";
    updateModeUi();
  };
  $("btn-run").onclick = runCrypto;
  $("btn-paste").onclick = async () => {
    try {
      const text = await navigator.clipboard.readText();
      $("input-text").value = text;
    } catch (e) {
      toast(t("home.pasteFailed"));
    }
  };
  $("btn-scan").onclick = openScan;
  $("btn-clear-in").onclick = () => {
    $("input-text").value = "";
  };
  $("btn-clear-out").onclick = () => {
    $("output-text").value = "";
    showBanner("");
  };
  $("btn-copy").onclick = () => copyText($("output-text").value);
  $("btn-qr").onclick = () => openQr($("output-text").value);
  $("btn-toggle-key").onclick = () => {
    const input = $("passphrase");
    const hide = input.type === "password";
    input.type = hide ? "text" : "password";
    $("btn-toggle-key").title = t(hide ? "home.hideKey" : "home.showKey");
  };
  $("qr-close").onclick = closeQr;
  $("qr-dialog").addEventListener("click", (e) => {
    if (e.target === $("qr-dialog")) closeQr();
  });
  $("scan-close").onclick = closeScan;
  $("scan-dialog").addEventListener("click", (e) => {
    if (e.target === $("scan-dialog")) closeScan();
  });
  $("scan-file").onchange = (e) => {
    const file = e.target.files && e.target.files[0];
    if (file) decodeImageFile(file);
    e.target.value = "";
  };
  $("confirm-ok").onclick = () => closeConfirm(true);
  $("confirm-cancel").onclick = () => closeConfirm(false);
  $("confirm-dialog").addEventListener("click", (e) => {
    if (e.target === $("confirm-dialog")) closeConfirm(false);
  });
  $("history-search").oninput = renderHistory;
  document.querySelectorAll("[data-theme-mode]").forEach((b) => {
    b.onclick = () => {
      settings.themeMode = b.getAttribute("data-theme-mode");
      persist();
      refreshTheme();
      renderSettings();
    };
  });
  document.querySelectorAll("[data-color-mode]").forEach((b) => {
    b.onclick = () => {
      settings.colorMode = b.getAttribute("data-color-mode");
      if (settings.colorMode === "system") settings.seed = DEFAULT_SEED;
      persist();
      refreshTheme();
      renderSettings();
    };
  });
  $("color-input").oninput = (e) => {
    settings.seed = e.target.value;
    $("color-hex").textContent = settings.seed;
    persist();
    refreshTheme();
  };
  window.addEventListener("hashchange", () => showView(currentRoute()));
  window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", () => {
    if (settings.themeMode === "system") refreshTheme();
  });
  onLanguageChange(() => {
    updateOpUi();
    if (currentRoute() === "/history") renderHistory();
    if (currentRoute() === "/settings") renderSettings();
  });
}

function init() {
  setLanguage(settings.language);
  refreshTheme();
  updateOpUi();
  updateModeUi();
  bind();
  showView(currentRoute());
}

init();
