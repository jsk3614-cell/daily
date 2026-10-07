# iOS · Android 실행 안내

이 앱은 Expo React Native로 작성되어 같은 코드로 iOS와 Android를 지원한다.

## 시작

```bash
npm install
npm start
```

터미널에 QR 코드가 나타나면 Android는 Expo Go로 스캔하고, iPhone은 카메라로 스캔한 뒤 Expo Go로 연다.

개발 환경에 에뮬레이터가 설치되어 있으면 다음 명령도 사용할 수 있다.

```bash
npm run android
npm run ios
```

실제 배포용 앱은 Expo 계정과 EAS Build 설정이 추가로 필요하다. 이번 MVP는 서버·로그인 없이 기기 내부에 오늘의 할 일을 저장한다.
