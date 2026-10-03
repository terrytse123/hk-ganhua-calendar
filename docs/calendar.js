/* Hong Kong perpetual calendar helpers. Lunar data 1900-2100, standard public-domain table. */
const LUNAR_INFO = [
  0x04bd8,0x04ae0,0x0a570,0x054d5,0x0d260,0x0d950,0x16554,0x056a0,0x09ad0,0x055d2,
  0x04ae0,0x0a5b6,0x0a4d0,0x0d250,0x1d255,0x0b540,0x0d6a0,0x0ada2,0x095b0,0x14977,
  0x04970,0x0a4b0,0x0b4b5,0x06a50,0x06d40,0x1ab54,0x02b60,0x09570,0x052f2,0x04970,
  0x06566,0x0d4a0,0x0ea50,0x06e95,0x05ad0,0x02b60,0x186e3,0x092e0,0x1c8d7,0x0c950,
  0x0d4a0,0x1d8a6,0x0b550,0x056a0,0x1a5b4,0x025d0,0x092d0,0x0d2b2,0x0a950,0x0b557,
  0x06ca0,0x0b550,0x15355,0x04da0,0x0a5b0,0x14573,0x052b0,0x0a9a8,0x0e950,0x06aa0,
  0x0aea6,0x0ab50,0x04b60,0x0aae4,0x0a570,0x05260,0x0f263,0x0d950,0x05b57,0x056a0,
  0x096d0,0x04dd5,0x04ad0,0x0a4d0,0x0d4d4,0x0d250,0x0d558,0x0b540,0x0b6a0,0x195a6,
  0x095b0,0x049b0,0x0a974,0x0a4b0,0x0b27a,0x06a50,0x06d40,0x0af46,0x0ab60,0x09570,
  0x04af5,0x04970,0x064b0,0x074a3,0x0ea50,0x06b58,0x055c0,0x0ab60,0x096d5,0x092e0,
  0x0c960,0x0d954,0x0d4a0,0x0da50,0x07552,0x056a0,0x0abb7,0x025d0,0x092d0,0x0cab5,
  0x0a950,0x0b4a0,0x0baa4,0x0ad50,0x055d9,0x04ba0,0x0a5b0,0x15176,0x052b0,0x0a930,
  0x07954,0x06aa0,0x0ad50,0x05b52,0x04b60,0x0a6e6,0x0a4e0,0x0d260,0x0ea65,0x0d530,
  0x05aa0,0x076a3,0x096d0,0x04afb,0x04ad0,0x0a4d0,0x1d0b6,0x0d250,0x0d520,0x0dd45,
  0x0b5a0,0x056d0,0x055b2,0x049b0,0x0a577,0x0a4b0,0x0aa50,0x1b255,0x06d20,0x0ada0,
  0x14b63,0x09370,0x049f8,0x04970,0x064b0,0x168a6,0x0ea50,0x06b20,0x1a6c4,0x0aae0,
  0x0a2e0,0x0d2e3,0x0c960,0x0d557,0x0d4a0,0x0da50,0x05d55,0x056a0,0x0a6d0,0x055d4,
  0x052d0,0x0a9b8,0x0a950,0x0b4a0,0x0b6a6,0x0ad50,0x055a0,0x0aba4,0x0a5b0,0x052b0,
  0x0b273,0x06930,0x07337,0x06aa0,0x0ad50,0x14b55,0x04b60,0x0a570,0x054e4,0x0d160,
  0x0e968,0x0d520,0x0daa0,0x16aa6,0x056d0,0x04ae0,0x0a9d4,0x0a2d0,0x0d150,0x0f252,
  0x0d520
];
const TERM_INFO = [0,21208,42467,63836,85337,107014,128867,150921,173149,195551,218072,240693,263343,285989,308563,331033,353350,375494,397447,419210,440795,462224,483532,504758];
const TERM_NAMES = ["小寒","大寒","立春","雨水","驚蟄","春分","清明","穀雨","立夏","小滿","芒種","夏至","小暑","大暑","立秋","處暑","白露","秋分","寒露","霜降","立冬","小雪","大雪","冬至"];
const LUNAR_MONTH = ["","正月","二月","三月","四月","五月","六月","七月","八月","九月","十月","十一月","十二月"];
const LUNAR_DAY = ["","初一","初二","初三","初四","初五","初六","初七","初八","初九","初十","十一","十二","十三","十四","十五","十六","十七","十八","十九","二十","廿一","廿二","廿三","廿四","廿五","廿六","廿七","廿八","廿九","三十"];
const GAN = ["甲","乙","丙","丁","戊","己","庚","辛","壬","癸"];
const ZHI = ["子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥"];
const MIN_YEAR = 1901;
const MAX_YEAR = 2099;

function pad(n){ return String(n).padStart(2,"0"); }
function keyOf(d){ return d.getFullYear()+"-"+pad(d.getMonth()+1)+"-"+pad(d.getDate()); }
function parseKey(k){ const [y,m,d]=k.split("-").map(Number); return new Date(y,m-1,d); }
function addDays(d, n){ const x = new Date(d.getFullYear(), d.getMonth(), d.getDate()); x.setDate(x.getDate()+n); return x; }
function sameDay(a,b){ return a.getFullYear()===b.getFullYear() && a.getMonth()===b.getMonth() && a.getDate()===b.getDate(); }

function lYearDays(y){
  let sum = 348;
  for (let i = 0x8000; i > 0x8; i >>= 1) sum += (LUNAR_INFO[y-1900] & i) ? 1 : 0;
  return sum + leapDays(y);
}
function leapMonth(y){ return LUNAR_INFO[y-1900] & 0xf; }
function leapDays(y){ if (leapMonth(y)) return (LUNAR_INFO[y-1900] & 0x10000) ? 30 : 29; return 0; }
function monthDays(y, m){ return (LUNAR_INFO[y-1900] & (0x10000 >> m)) ? 30 : 29; }

function solarToLunar(date){
  if (date.getFullYear() < 1901 || date.getFullYear() > 2099) return null;
  let offset = Math.round((Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()) - Date.UTC(1900,0,31)) / 86400000);
  let year = 1900;
  let temp = 0;
  for (; year < 2101 && offset > 0; year++){
    temp = lYearDays(year);
    offset -= temp;
  }
  if (offset < 0){ offset += temp; year--; }
  const leap = leapMonth(year);
  let isLeap = false;
  let month = 1;
  for (; month < 13 && offset > 0; month++){
    if (leap > 0 && month === leap + 1 && !isLeap){
      --month; isLeap = true; temp = leapDays(year);
    } else {
      temp = monthDays(year, month);
    }
    if (isLeap && month === leap + 1) isLeap = false;
    offset -= temp;
  }
  if (offset === 0 && leap > 0 && month === leap + 1){
    if (isLeap) isLeap = false;
    else { isLeap = true; --month; }
  }
  if (offset < 0){
    offset += temp; --month;
    if (isLeap) isLeap = false;
  }
  const day = offset + 1;
  return { year, month, day, isLeap };
}

function lunarLabel(info){
  if (!info) return "";
  return "農曆" + (info.isLeap ? "閏" : "") + LUNAR_MONTH[info.month] + LUNAR_DAY[info.day];
}
function yearGZ(lunarYear){
  const i = lunarYear - 4;
  return GAN[((i % 10) + 10) % 10] + ZHI[((i % 12) + 12) % 12];
}

function solarTermOn(date){
  const y = date.getFullYear();
  for (let n = 0; n < 24; n++){
    const ms = Date.UTC(1900, 0, 6, 2, 5) + 31556925974.7 * (y - 1900) + TERM_INFO[n] * 60000;
    const dt = new Date(ms);
    if (dt.getUTCFullYear() === y && dt.getUTCMonth() === date.getMonth() && dt.getUTCDate() === date.getDate()){
      return TERM_NAMES[n];
    }
  }
  return "";
}
function chingMing(year){
  const ms = Date.UTC(1900, 0, 6, 2, 5) + 31556925974.7 * (year - 1900) + TERM_INFO[6] * 60000;
  const dt = new Date(ms);
  return new Date(dt.getUTCFullYear(), dt.getUTCMonth(), dt.getUTCDate());
}

function easterSunday(year){
  const a = year % 19;
  const b = Math.floor(year / 100);
  const c = year % 100;
  const d = Math.floor(b / 4);
  const e = b % 4;
  const f = Math.floor((b + 8) / 25);
  const g = Math.floor((b - f + 1) / 3);
  const h = (19 * a + b - d - g + 15) % 30;
  const i = Math.floor(c / 4);
  const k = c % 4;
  const l = (32 + 2 * e + 2 * i - h - k) % 7;
  const m = Math.floor((a + 11 * h + 22 * l) / 451);
  const month = Math.floor((h + l - 7 * m + 114) / 31);
  const day = ((h + l - 7 * m + 114) % 31) + 1;
  return new Date(year, month - 1, day);
}

function findLunar(year, month, day, isLeap){
  const start = new Date(year - 1, 0, 1);
  const end = new Date(year + 1, 11, 31);
  for (let d = start; d <= end; d = addDays(d, 1)){
    const info = solarToLunar(d);
    if (!info) continue;
    if (info.month === month && info.day === day && info.isLeap === !!isLeap && d.getFullYear() === year) return d;
  }
  return null;
}
function lunarNewYear(gregorianYear){
  return findLunar(gregorianYear, 1, 1, false);
}

const holidayCache = {};
function hkHolidays(year){
  if (holidayCache[year]) return holidayCache[year];
  const map = {};
  const put = (date, name) => { if (date && date.getFullYear() === year) map[keyOf(date)] = name; };
  const nextFree = (date) => {
    let d = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    while (d.getDay() === 0 || map[keyOf(d)]) d = addDays(d, 1);
    return d;
  };
  const fixed = (month, day, name) => {
    let d = new Date(year, month, day);
    if (d.getDay() === 0) d = addDays(d, 1);
    if (map[keyOf(d)]) d = nextFree(d);
    put(d, name);
  };
  fixed(0, 1, "元旦");
  const cny = lunarNewYear(year);
  if (cny){
    const names = ["農曆年初一","農曆年初二","農曆年初三"];
    for (let i = 0; i < 3; i++){
      const d = addDays(cny, i);
      if (d.getDay() === 0) put(addDays(cny, 3), "年初四（補" + ["年初一","年初二","年初三"][i] + "）");
      else put(d, names[i]);
    }
  }
  const goodFriday = addDays(easterSunday(year), -2);
  put(goodFriday, "耶穌受難節");
  put(addDays(goodFriday, 1), "耶穌受難節翌日");
  let qm = chingMing(year);
  if (qm.getDay() === 0) qm = nextFree(addDays(chingMing(year), 1));
  else if (map[keyOf(qm)]) qm = nextFree(qm);
  put(qm, sameDay(qm, chingMing(year)) ? "清明節" : "清明節補假");
  let easterMonday = addDays(goodFriday, 3);
  if (map[keyOf(easterMonday)] || easterMonday.getDay() === 0) easterMonday = nextFree(easterMonday);
  put(easterMonday, sameDay(easterMonday, addDays(goodFriday, 3)) ? "復活節星期一" : "復活節星期一補假");
  fixed(4, 1, "勞動節");
  const buddha = findLunar(year, 4, 8, false);
  if (buddha){
    let d = buddha;
    if (d.getDay() === 0 || map[keyOf(d)]) d = nextFree(d.getDay() === 0 ? addDays(buddha, 1) : d);
    put(d, sameDay(d, buddha) ? "佛誕" : "佛誕補假");
  }
  const tuen = findLunar(year, 5, 5, false);
  if (tuen){
    let d = tuen;
    if (d.getDay() === 0 || map[keyOf(d)]) d = nextFree(d.getDay() === 0 ? addDays(tuen, 1) : d);
    put(d, sameDay(d, tuen) ? "端午節" : "端午節補假");
  }
  fixed(6, 1, "特區成立紀念日");
  const mid = findLunar(year, 8, 15, false);
  if (mid){
    let d = addDays(mid, 1);
    if (d.getDay() === 0) d = addDays(mid, 2);
    if (map[keyOf(d)]) d = nextFree(d);
    put(d, "中秋節翌日");
  }
  fixed(9, 1, "國慶日");
  const chung = findLunar(year, 9, 9, false);
  if (chung){
    let d = chung;
    if (d.getDay() === 0 || map[keyOf(d)]) d = nextFree(d.getDay() === 0 ? addDays(chung, 1) : d);
    put(d, sameDay(d, chung) ? "重陽節" : "重陽節補假");
  }
  const xmas = new Date(year, 11, 25);
  if (xmas.getDay() === 0) put(addDays(xmas, 2), "聖誕節補假");
  else put(xmas, "聖誕節");
  let after = addDays(xmas, 1);
  while (after.getDay() === 0) after = addDays(after, 1);
  if (map[keyOf(after)]) after = nextFree(after);
  put(after, "聖誕節後第一個周日");
  holidayCache[year] = map;
  return map;
}

if (typeof module !== "undefined") module.exports = { solarToLunar, lunarLabel, hkHolidays, keyOf, chingMing, lunarNewYear, yearGZ };
