dependencies {
    implementation(project(":shared"))
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("io.lettuce:lettuce-core:6.2.4.RELEASE")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")
}
