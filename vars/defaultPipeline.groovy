def call(body) {

    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    node {
        try {
            println "${WORKSPACE}"
            stage ('Clone') {
                checkout scm
                script {
                    properties([[$class: 'GithubProjectProperty',
                    projectUrlStr: 'https://github.com/NextThought/nti.test.jenkins']])
                }
            }
            stage ('Clean') {
                sh "npx @nti/ci-scripts clean"
            }
            if(params.createTag == null || params.createTag == '' || "${BRANCH_NAME}" != "master") {
                stage ('Prepare') {
                    sh "npx @nti/ci-scripts prepare"
                }
           }
           stage ('Install') {
               if(params.createTag == null || params.createTag == '') {
                   sh "npx @nti/ci-scripts install"
               }
               else {
                    sh "npx @nti/ci-scripts install-strict"
               }
           }
            stage("Run") {
                withCredentials([sshUserPrivateKey(keyFileVariable: 'testFile', credentialsId: '3d8be6d3-d795-4bfc-8962-6a6bd0bbf35d', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                    sh "env"
                    sh "echo $GIT_USERNAME"
                }
                if((params.createTag != null && params.createTag != '') || "${BRANCH_NAME}" == "master") {
                    sh "npx @nti/ci-scripts publish"
                }
                else {
                    sh "npx @nti/ci-scripts pack"
                }
            }
        } catch (err) {
            currentBuild.result = 'FAILED'
            step([$class: 'GitHubIssueNotifier',
                  issueAppend: true,
                  issueLabel: '',
                  issueTitle: '$JOB_NAME $BUILD_DISPLAY_NAME failed'])
            throw err
        }
    parameters {
      string(name: 'createTag', defaultValue: '', description: '')
      booleanParam(name: 'buildAsSnapshot', defaultValue: false)
    }
  }
}
