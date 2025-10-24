plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    kotlin("plugin.serialization") version "2.1.0"
    application
}

group = "id.nearyou.app"
version = "1.0.0"
application {
    mainClass.set("id.nearyou.app.ApplicationKt")
    
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)

    // Authentication & Security
    implementation("io.ktor:ktor-server-auth:3.3.0")
    implementation("io.ktor:ktor-server-auth-jwt:3.3.0")
    implementation("org.mindrot:jbcrypt:0.4")

    // Serialization
    implementation("io.ktor:ktor-server-content-negotiation:3.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.0")

    // Error Handling
    implementation("io.ktor:ktor-server-status-pages:3.3.0")

    // Database
    implementation("org.jetbrains.exposed:exposed-core:0.48.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.48.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.48.0")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.48.0")
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("net.postgis:postgis-jdbc:2.5.1")
    implementation("com.zaxxer:HikariCP:5.1.0")  // Connection pooling

    // Redis
    implementation("io.lettuce:lettuce-core:6.3.1.RELEASE")

    // Dependency Injection
    implementation("io.insert-koin:koin-ktor:4.0.1")
    implementation("io.insert-koin:koin-logger-slf4j:4.0.1")

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("io.mockk:mockk:1.13.13") // For mocking
    testImplementation("com.h2database:h2:2.2.224") // In-memory database for testing
    testImplementation("io.insert-koin:koin-test:4.0.1") // Koin testing
}

// Task untuk test database connection
tasks.register<JavaExec>("testDb") {
    group = "verification"
    description = "Test database connection and PostGIS"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("id.nearyou.app.TestDatabaseConnectionKt")
}