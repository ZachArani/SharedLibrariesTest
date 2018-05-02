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
                    //sh "npm pack"
                    //sh "rm ./*.tgz"
                    sh "npx @nti/ci-scripts pack"
                }
            }
        } catch (err) {
            currentBuild.result = 'FAILED'
            throw err
        }
    parameters {
      string(name: 'createTag', defaultValue: '', description: '')
      booleanParam(name: 'buildAsSnapshot', defaultValue: false)
    }
  }
}
