plugins {
    id("xroad.java-conventions")
}

dependencies {
    annotationProcessor(libs.mapstructProcessor)
    annotationProcessor(libs.lombokMapstructBinding)

    implementation(project(":common:common-db"))
    implementation(project(":common:common-message"))
    implementation(project(":common:common-messagelog"))
    implementation(libs.bouncyCastle.bcpkix)
    implementation(libs.slf4j.api)
    implementation(libs.mapstruct)
}
