
import com.cloudbees.groovy.cps.NonCPS

/**************************************************************************************************
 *    Artifactory Utilities
 *
 **************************************************************************************************/
@NonCPS
String getArtifactoryHost() { 'domainName' }

@NonCPS
String getArtifactoryUrl() { "https://${getArtifactoryHost()}/artifactory" }

@NonCPS
String getArtifactoryCredentialsId() { 'Credentials Id from jenkins' }

