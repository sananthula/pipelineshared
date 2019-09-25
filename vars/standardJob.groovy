
def call(Map config) {

    def nodeLabel = config?.nodeLabel ?: 'taas_image'
    def gradleTask = config?.gradleTask ?: 'build'
    // TODO - upstreamRepos handling
    //
    //

    milestone()

    lock(resource: "${env.JOB_NAME}", inversePrecedence: true) {

        node(nodeLabel) {

            stage('Pull') {
                checkout scm
            }

            try {
                stage('Build') {

                    withCredentials([[$class: 'UsernamePasswordMultiBinding',
                                      credentialsId: csUtils.getArtifactoryCredentialsId(),
                                      usernameVariable: 'ARTIFACTORY_USERNAME', passwordVariable: 'ARTIFACTORY_PASSWORD']]) {
                        runGradleBuild(gradleTask)
                    }
                }
            } catch (error) {
                echo "The Gradle build failed with ${error}"
                currentBuild.result = 'FAILURE'
            }

        }
        milestone()
    }
}

def runGradleBuild(String gradleTask) {

    String gitCommit = ""

    if (isUnix()) {
        gitCommit = sh script: 'git rev-parse --short HEAD', returnStdout: true
    } else {

        def stdout = bat(returnStdout:true , script: 'git rev-parse --short HEAD').trim()
        gitCommit = stdout.readLines().drop(1).join(" ")
    }

    gitCommit = gitCommit.trim()

    println 'INFO: gitCommit = ' + gitCommit

    def args = """ \
        -i -s --build-cache --refresh-dependencies \
        -Dgradle.wrapperUser=${env.ARTIFACTORY_USERNAME} \
        -Dgradle.wrapperPassword=${env.ARTIFACTORY_PASSWORD} \
        -DCS_ARTIFACTORY_USERNAME=${env.ARTIFACTORY_USERNAME} \
        -DCS_ARTIFACTORY_PASSWORD=${env.ARTIFACTORY_PASSWORD} \
        -DCS_ARTIFACTORY_URL=${csUtils.getArtifactoryUrl()} \
        -PGIT_BRANCH=$env.BRANCH_NAME \
        -DGIT_BRANCH=$env.BRANCH_NAME \
        -PGIT_COMMIT=${gitCommit} \
        -DGIT_COMMIT=${gitCommit} \
        """.stripIndent()

    if (isUnix()) {
        sh """ \
            chmod +x ./gradlew 
            ./gradlew ${gradleTask} --no-daemon ${args}
        """.stripIndent()

    } else {
        bat "gradlew.bat ${gradleTask} ${args}"
    }
}
