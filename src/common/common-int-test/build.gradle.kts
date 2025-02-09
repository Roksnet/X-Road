plugins {
    id("xroad.java-conventions")
}

dependencies {
    api(project(":common:common-message"))
    api(project(":lib:globalconf-core"))
    api(project(":service:signer:signer-client"))
    api(project(":addons:hwtoken"))

    api(libs.bundles.testAutomation)
    api(libs.testAutomation.selenide) {
        exclude(group = "org.slf4j", module = "*")
    }
    api(libs.bouncyCastle.bcpkix)
    api(libs.awaitility)
}

tasks.test {
    useJUnitPlatform()
}
