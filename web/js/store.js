/**
 * CMS-2027 — localStorage 封装
 * 设置：cms2027.settings
 * 历史：cms2027.history（最新在前，上限 500）
 */
const SETTINGS_KEY = "cms2027.settings";
const HISTORY_KEY = "cms2027.history";
const HISTORY_LIMIT = 500;

const DEFAULT_SETTINGS = {
  themeMode: "system",
  colorMode: "system",
  seed: "#6750A4",
  language: "auto",
};

export function loadSettings() {
  try {
    const raw = localStorage.getItem(SETTINGS_KEY);
    if (!raw) return { ...DEFAULT_SETTINGS };
    const parsed = JSON.parse(raw);
    return { ...DEFAULT_SETTINGS, ...parsed };
  } catch (e) {
    return { ...DEFAULT_SETTINGS };
  }
}

export function saveSettings(settings) {
  localStorage.setItem(SETTINGS_KEY, JSON.stringify(settings));
}

export function loadHistory() {
  try {
    const raw = localStorage.getItem(HISTORY_KEY);
    if (!raw) return [];
    const list = JSON.parse(raw);
    return Array.isArray(list) ? list : [];
  } catch (e) {
    return [];
  }
}

export function saveHistory(list) {
  localStorage.setItem(HISTORY_KEY, JSON.stringify(list.slice(0, HISTORY_LIMIT)));
}

export function addHistory(entry) {
  const list = loadHistory();
  list.unshift({
    id: Date.now().toString(36) + Math.random().toString(36).slice(2, 8),
    ts: Date.now(),
    ...entry,
  });
  saveHistory(list);
  return list;
}

export function deleteHistory(ids) {
  const set = new Set(ids);
  const list = loadHistory().filter((item) => !set.has(item.id));
  saveHistory(list);
  return list;
}

export function clearHistory() {
  saveHistory([]);
  return [];
}

export { DEFAULT_SETTINGS, HISTORY_LIMIT };
