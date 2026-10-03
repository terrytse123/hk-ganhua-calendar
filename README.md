# 香港幹話曆

離線香港萬年曆。每日一句原創廣東話幹話，加上農曆、節氣同香港公眾假期。

唔係煦暖曆，亦唔係任何賣緊嘅幹話曆。句子係原創。

## 功能

- 1901–2099 萬年曆
- 廣東話幹話、玩笑版宜忌
- 農曆、干支、二十四節氣
- 香港公眾假期（跟《公眾假期條例》一般補假規則）
- 收藏同分享，資料只放喺部手機

## 網頁版

用瀏覽器打開 [`www/index.html`](www/index.html)。GitHub Pages 如果開咗，可以喺呢度睇：

https://terrytse123.github.io/hk-ganhua-calendar/www/

## Android

已簽名 APK 喺 [Releases](https://github.com/terrytse123/hk-ganhua-calendar/releases)。Android 7 或以上，允許呢個來源安裝。

源碼係一個 WebView 殼：

- `android/AndroidManifest.xml`
- `android/src/com/ganhua/calendar/MainActivity.java`
- `www/` 放喺 `assets/www/`

套件名 `com.ganhua.calendar`。

## 假期說明

標示嘅係一般公眾假期，包括星期日補假順延。某一年政府憲報如果另有指定，可能差一日。冬至只當節氣；法定假日入面冬至定聖誕由僱主二揀一。

農曆同節氣用通用算法，跨日邊界可能差一日。
