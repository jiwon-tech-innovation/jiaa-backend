plugins {
    java
    id("org.springframework.boot") version "4.0.1" apply false
    id("io.spring.dependency-management") version "1.1.7"
}

// Configure repositories for all projects (including root)
allprojects {
    group = "io.github.jiwon-tech-innovation"
    version = "1.0-SNAPSHOT"


}

// Add dependencies for the root project itself
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.modulith:spring-modulith-bom:2.0.1")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.1.0")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

// Configure test task for root project
tasks.test {
    useJUnitPlatform()
}