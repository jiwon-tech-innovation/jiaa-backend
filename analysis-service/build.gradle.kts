plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":common-lib"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    runtimeOnly("org.postgresql:postgresql")

<<<<<<< HEAD
    // Eureka 제거 - K8s Service Discovery 사용
    // implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
=======
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
>>>>>>> 14f19b069fe1f892bbbd7d93fcb4ffa910548193
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.0")
    implementation("org.apache.commons:commons-lang3")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}