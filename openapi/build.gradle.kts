plugins { `java-library` }
java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
dependencies { api(project(":http")); implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2"); testImplementation("org.testng:testng:7.10.2") }
