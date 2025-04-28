// local.properties 파일을 읽어오는 부분
val localProperties = java.util.Properties()
val localPropertiesFile = rootProject.file("local.properties")
localProperties.load(localPropertiesFile.inputStream())

val openaiApiKey = localProperties.getProperty("OPENAI_API_KEY")

plugins {

    id("com.google.gms.google-services") version "4.4.2" apply false
    alias(libs.plugins.android.application) apply false
}
