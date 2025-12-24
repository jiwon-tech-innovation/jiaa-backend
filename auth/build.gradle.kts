plugins {
	id("org.springframework.boot")
	id("io.spring.dependency-management")
}

// Disable bootJar because this is a library module within the monolith
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.named<org.gradle.api.tasks.bundling.Jar>("jar") {
    enabled = true
}

dependencies {
	implementation(project(":common"))
	implementation("org.springframework.boot:spring-boot-starter-web")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
