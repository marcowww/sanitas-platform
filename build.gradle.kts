plugins {
    java
    id("org.springframework.boot") version "3.2.0" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
}

allprojects {
    group = "com.healthcare.staffing"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
        testImplementation("org.mockito:mockito-core:5.1.1")
        testImplementation("org.mockito:mockito-junit-jupiter:5.1.1")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.test {
        useJUnitPlatform()
    }
}

// Configure Spring Boot services
configure(subprojects.filter { 
    it.name in listOf("booking-service", "carer-service", "view-maintenance-service", "read-api-service") 
}) {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter")
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.springframework.boot:spring-boot-starter-actuator")
        implementation("org.springframework.kafka:spring-kafka")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.springframework.kafka:spring-kafka-test")
    }
}
