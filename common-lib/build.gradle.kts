plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    `java-library`
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    api("org.springdoc:springdoc-openapi-starter-common:3.0.0")
    
	compileOnly("org.projectlombok:lombok:1.18.42")
	annotationProcessor("org.projectlombok:lombok:1.18.42")
}
