plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

<<<<<<< HEAD
    // Eureka 제거 - K8s Service Discovery 사용
    // implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    // implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")
=======
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")
>>>>>>> 14f19b069fe1f892bbbd7d93fcb4ffa910548193
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:3.0.0")

    // MacOS DNS resolver 네이티브 라이브러리 (로컬 개발 환경 전용)
    if (System.getProperty("os.name").lowercase().contains("mac")) {
        val arch = System.getProperty("os.arch")
        val classifier = if (arch == "aarch64") "osx-aarch_64" else "osx-x86_64"
        runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.2.9.Final:$classifier")
    }

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    // Java 22+ FFM API 제한 경고 해결
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}