def call(body) {

    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    node {
        try {
            stage ('Clean') {
                sh "npx @nti/ci-scripts clean"
            }
            if(params.createTag == '') {
                stage ('Prepare Snapshot') {
                    sh "echo 'do something here'" 
                }
           }
           stage ('Install') {
             sh "npm install"
           }
            stage("Run") {
                if(params.createTag != '' || "${BRANCH_NAME}" == "master") {
                    sh "npm publish"   
                }
                else {
                    //sh "npm pack"
                    //sh "rm ./*.tgz"
                    sh "echo 'pack and delete'"
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
