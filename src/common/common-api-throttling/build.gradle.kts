plugins {
    id("xroad.java-conventions")
    id("java-test-fixtures")
}

dependencies {
    compileOnly(libs.jakarta.servletApi)

    api(libs.guava)
    api(libs.bucket4j.core)

    implementation(libs.slf4j.api)

    testFixturesImplementation(libs.springBoot.starterTest)
    testFixturesCompileOnly(libs.lombok)
    testFixturesAnnotationProcessor(libs.lombok)
}
