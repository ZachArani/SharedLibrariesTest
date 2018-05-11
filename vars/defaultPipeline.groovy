def call(body) {

    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    node {
        try {
            stage ('Clone') {
                checkout scm
                def gitURL = scm.getUserRemoteConfigs()[0].getUrl().replace("git@github.com:","https://github.com/").replace(".git","");
                script {
                    properties([[$class: 'GithubProjectProperty',
                    projectUrlStr: gitURL]])
                }
            }
            stage ('Clean') {
                sh "npx @nti/ci-scripts clean"
            }
            if(!("${BRANCH_NAME}" ==~ /v\d+\.\d+\.\d+/)) { //If not version
                stage ('Prepare') {
                    sh "npx @nti/ci-scripts prepare"
                }
           }
           stage ('Install') {
               if(!("${BRANCH_NAME}" ==~ /v\d+\.\d+\.\d+/)) {
                   sh "npx @nti/ci-scripts install"
               }
               else {
                    sh "npx @nti/ci-scripts install-strict"
               }
           }
            stage("Run") {
                if(("${BRANCH_NAME}" ==~ /v\d+\.\d+\.\d+/) || "${BRANCH_NAME}" == "master") { //Version or snapshot
                    sh "npx @nti/ci-scripts publish"
                }
                else {
                    sh "npx @nti/ci-scripts pack"
                }
            }
        } catch (err) {
         //   currentBuild.result = 'FAILED'
            def isAbort = err instanceof hudson.AbortException;
            if(env.BRANCH_NAME == "master" && !isAbort){
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
