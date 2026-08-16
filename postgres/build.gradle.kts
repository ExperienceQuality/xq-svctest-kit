plugins { `java-library` }
java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
dependencies { api(project(":core")); api("org.testcontainers:postgresql:1.21.4"); testRuntimeOnly("org.postgresql:postgresql:42.7.13"); testImplementation("org.testng:testng:7.10.2") }
