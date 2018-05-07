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
                sh 'echo ${scm.getUserRemoteConfigs()[0].getUrl().replace("git@github.com:","https://github.com/")}'
                script {
                    properties([[$class: 'GithubProjectProperty',
                    projectUrlStr: scm.getUserRemoteConfigs()[0].getUrl()]])
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
                if((params.createTag != null && params.createTag != '') || "${BRANCH_NAME}" == "master") {
                    sh "npx @nti/ci-scripts publish"
                }
                else {
                    sh "npx @nti/ci-scripts pack"
                }
            }
        } catch (err) {
            currentBuild.result = 'FAILED'
            if(env.BRANCH_NAME == "master"){
                step([$class: 'GitHubIssueNotifier',
                      issueAppend: true,
                      issueLabel: '',
                      issueTitle: '$JOB_NAME $BUILD_DISPLAY_NAME failed'])
            }
            throw err
        }
    parameters {
      string(name: 'createTag', defaultValue: '', description: '')
      booleanParam(name: 'buildAsSnapshot', defaultValue: false)
    }
  }
}
