/**
 * CMS-2027 — Material 3 动态取色
 * 用种子色生成 light/dark 方案，写入 CSS 变量。
 */
import {
  argbFromHex,
  hexFromArgb,
  themeFromSourceColor,
} from "../vendor/mcu/index.js";

export const DEFAULT_SEED = "#6750A4";

export const PRESET_SEEDS = [
  "#6750A4",
  "#4355B9",
  "#0061A4",
  "#00639B",
  "#006874",
  "#006A6A",
  "#006D42",
  "#146C2E",
  "#5B6300",
  "#785900",
  "#8B6E00",
  "#9A4520",
  "#BA1A1A",
  "#A8385D",
  "#984061",
  "#7C4899",
  "#765B46",
  "#5F5A62",
  "#000000",
  "#0B57D0",
];

function toCssName(key) {
  return key.replace(/([a-z])([A-Z])/g, "$1-$2").toLowerCase();
}

function extraSurfaces(palettes, isDark) {
  const n1 = palettes.neutral;
  if (isDark) {
    return {
      surfaceDim: n1.tone(6),
      surfaceBright: n1.tone(24),
      surfaceContainerLowest: n1.tone(4),
      surfaceContainerLow: n1.tone(10),
      surfaceContainer: n1.tone(12),
      surfaceContainerHigh: n1.tone(17),
      surfaceContainerHighest: n1.tone(22),
      surfaceTint: palettes.primary.tone(80),
    };
  }
  return {
    surfaceDim: n1.tone(87),
    surfaceBright: n1.tone(98),
    surfaceContainerLowest: n1.tone(100),
    surfaceContainerLow: n1.tone(96),
    surfaceContainer: n1.tone(94),
    surfaceContainerHigh: n1.tone(92),
    surfaceContainerHighest: n1.tone(90),
    surfaceTint: palettes.primary.tone(40),
  };
}

export function resolveDark(themeMode) {
  if (themeMode === "dark") return true;
  if (themeMode === "light") return false;
  return window.matchMedia("(prefers-color-scheme: dark)").matches;
}

export function applyTheme(seedHex, themeMode) {
  const seed = seedHex && /^#[0-9a-fA-F]{6}$/.test(seedHex) ? seedHex : DEFAULT_SEED;
  const isDark = resolveDark(themeMode);
  const theme = themeFromSourceColor(argbFromHex(seed));
  const scheme = isDark ? theme.schemes.dark : theme.schemes.light;
  const roles = { ...scheme.toJSON(), ...extraSurfaces(theme.palettes, isDark) };
  const root = document.documentElement;
  for (const [key, value] of Object.entries(roles)) {
    root.style.setProperty("--md-sys-color-" + toCssName(key), hexFromArgb(value));
  }
  root.setAttribute("data-theme", isDark ? "dark" : "light");
  root.style.colorScheme = isDark ? "dark" : "light";
  return { seed, isDark };
}
