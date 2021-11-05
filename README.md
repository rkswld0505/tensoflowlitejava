# Earsense

### Android TODO

[TODO : tflite model 적용 전]

- [x] UI 작업 - 프로토 타입 : https://ovenapp.io/view/o4E0gYY3pNSxBmFFbSn986j0m3lcOn0U
- [x] 앱 실행시 마이크 권한 여부 확인하는 작업
- [ ] click 이벤트를 이용하여 각 뷰 연결
- [ ] 마이크를 통한 소리 크기 확인 작업
- [ ] 진동 시간을 조절하는 셋팅 화면 작업

[TODO : tflite model 적용]

- [x] tflite이용 assets폴더 내 빌드
- [x] 원하는 소리 데이터가 있는지 확인
- [x] 데이터 정확도 확인

[TODO : tflite model 적용 후]

- [x] 원하는 소리 데이터 선택 출력 확인
- [x] UI 작업 - (View, Progressbar, Seekbar, Dialog)
- [x] 소리 인식시 진동 설정
- [ ] 소리 인식시 인식된 소리 그림으로 출력


# Earsense Project

### 프로젝트의 목적

사람의 오감 중 청각은 외부에 의한 소리를 전달하는 중요한 감각 중 하나이다.

그러나 청각장애인은 평상시에도 소리를 잘 듣지 못하며, 특히 위험한 사고 환경을 인지하지 못하여 안전사고가 나는 경우가 증가하고 있다.

이번 프로젝트를 통해 청각장애인의 주변 소리를 즉각적으로 전달받아 일상생활의 불편함과 미연의 사고를 방지하는 방법을 제시한다.



### 시나리오 설계

1. 스마트폰 마이크를 통해 주변 소리 녹음
2. 주변 소리 중 특정 소리에 대해서만 인식 (Ex: 사이렌, 차 경적, 강아지의 짖는소리, 아기 우는 소리)
3. 특정 소리가 일정 기준 이상의 소리가 나면 진동과 함께 Notification 알림 전달 (화면 / 진동)



### 구현 참고자료

- TFLite 모델 이용

  https://tfhub.dev/google/lite-model/yamnet/classification/tflite/1