plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "com.cms902"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_22
    targetCompatibility = JavaVersion.VERSION_22
}

repositories {
    mavenCentral()
}

javafx {
    version = "22"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics")
}

dependencies {
    implementation("org.mariadb.jdbc:mariadb-java-client:3.3.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
    testImplementation("com.h2database:h2:2.2.224")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "com.cms902.CMS902Application"
    applicationDefaultJvmArgs = listOf("-Dprism.order=sw")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.cms902.HeadlessMain"
    }
}