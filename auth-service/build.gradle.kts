plugins {
	id("org.springframework.boot")
	id("io.spring.dependency-management")
}

dependencies {
	implementation(project(":common-lib"))
	implementation(project(":user-service"))

	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-persistence")

	compileOnly("org.projectlombok:lombok:1.18.42")
	annotationProcessor("org.projectlombok:lombok:1.18.42")

	implementation("io.jsonwebtoken:jjwt-api:0.13.0")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

	runtimeOnly("org.postgresql:postgresql")

	testRuntimeOnly("com.h2database:h2")

<<<<<<< HEAD
	// Eureka 제거 - K8s Service Discovery 사용
	// implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
=======
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
>>>>>>> 14f19b069fe1f892bbbd7d93fcb4ffa910548193
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.0")
	implementation("org.apache.commons:commons-lang3")

	// implementation("org.springframework.boot:spring-boot-starter-data-redis")
	// implementation("com.bucket4j:bucket4j-core:8.10.1")
	// implementation("com.bucket4j:bucket4j-redis:8.10.1")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
