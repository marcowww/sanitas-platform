dependencies {
    implementation(project(":shared"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("org.flywaydb:flyway-core:9.21.1")
    testImplementation("com.h2database:h2:2.2.220")
}
