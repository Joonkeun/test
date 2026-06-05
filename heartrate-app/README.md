# Galaxy Watch 실시간 심박수 모니터

갤럭시 워치에서 실시간 심박수를 측정해 안드로이드 폰에서 확인할 수 있는 앱입니다.

## 구조

```
heartrate-app/
├── wear/          # 갤럭시 워치 앱 (Wear OS)
└── mobile/        # 안드로이드 모바일 앱
```

## 동작 방식

```
[갤럭시 워치]                        [안드로이드 폰]
 Health Services API                  Wearable Data Layer API
 (심박수 측정)          →  BLE/WiFi  →  (실시간 수신 & 표시)
 HeartRateManager                     HeartRateWearableListenerService
```

## 요구사항

- 갤럭시 워치 4 이상 (Wear OS 3+)
- 안드로이드 8.0 (API 26) 이상
- 동일한 Google 계정으로 연결된 워치 & 폰
- **동일한 applicationId** 사용 필수 (Data Layer 통신 조건)

> ⚠️ wear 앱과 mobile 앱의 applicationId를 동일하게 맞춰야 Wearable Data Layer가 작동합니다.
> 현재: `com.heartrate.wear` / `com.heartrate.mobile` → 둘 다 `com.heartrate.app`으로 통일 권장

## 주요 기능

### 워치 앱
- Health Services API로 실시간 심박수 측정
- Foreground Service로 백그라운드 측정 유지
- Wearable Data Layer로 폰에 즉시 전송

### 모바일 앱
- 실시간 심박수 대형 표시 + 심장박동 애니메이션
- 심박 구간 분류 (안정 / 정상 / 유산소 / 고강도 / 최대)
- 최근 50개 측정 기록
- 워치 연결 상태 표시

## 빌드 방법

```bash
# Android Studio에서 프로젝트 열기
# wear 모듈 → 워치에 설치
# mobile 모듈 → 폰에 설치
```

## 권한

| 앱 | 권한 |
|---|---|
| 워치 | `BODY_SENSORS`, `FOREGROUND_SERVICE_HEALTH` |
| 모바일 | 없음 (Data Layer는 권한 불필요) |
