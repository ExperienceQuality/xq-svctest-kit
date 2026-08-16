plugins { `java-library` }
java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
dependencies { api(project(":core")); testImplementation("org.testng:testng:7.10.2") }
