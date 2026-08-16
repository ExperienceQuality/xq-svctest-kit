plugins { `java-library` }
java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
dependencies { api(project(":core")); api("org.testcontainers:postgresql:1.20.4"); testImplementation("org.testng:testng:7.10.2") }
