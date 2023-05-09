# foodtracker

No Google Play: https://play.google.com/store/apps/details?id=br.dev.tiagosutter.foodtracker

App para registrar consumo de alimentos e sintomas alérgicos junto ao registro.

<img src="https://github.com/tiagosutter/foodtracker/blob/main/app/src/main/play/listings/pt-BR/graphics/phone-screenshots/3.png?raw=true" width="200" />

Construído e mantido usando:
- [Firebase App Distribution](https://firebase.google.com/docs/app-distribution?hl=pt-br) - distribuição de apk para testes
- [Github Actions](https://github.com/features/actions) - para executar testes e automatizar release para Google Play e App Distribution
  - Envio automatizado de app para QA usando [Firebase App Distribution Gradle plugin](https://firebase.google.com/docs/app-distribution/android/distribute-gradle?hl=pt-br)
  - Envio automatizao de app para Google Play usando o plugin [Gradle Play Publisher](https://github.com/Triple-T/gradle-play-publisher)
- Arquitetura MVVM
- [Hilt](https://developer.android.com/training/dependency-injection/hilt-android?hl=pt-br) - para injeção de dependência
- [Room](https://developer.android.com/training/data-storage/room?hl=pt-br) - para persistência de dados
- [Jetpack Preferences](https://developer.android.com/jetpack/androidx/releases/preference) - para tela de preferências
- [SharedPreferences](https://developer.android.com/training/data-storage/shared-preferences?hl=pt-br) - para persistência de preferências
- Coroutines e Flows
- Firebase Crashlytics e Analytics
- [Timber](https://github.com/JakeWharton/timber) - no lugar do Log.d, para os logs, e com uso de `Tree` específica para enviar logs ao Firebase Crashlytics
