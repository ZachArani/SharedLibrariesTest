def call(body) {

    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    node {
        try {
            stage ('Clean') {
                sh "npx @nti/ci-scripts@micro clean"
            }
            println params.createTag
            if(!(params.createTag != '') && (params.createTag == '' || "${BRANCH_NAME}" != "master")) {
                stage ('Prepare') {
                    sh "npx @nti/ci-scripts@micro prepare" 
                }
           }
           stage ('Install') {
               if(params.createTag == '') {
                   sh "npx @nti/ci-scripts@micro install" 
               }
               else {
                    sh "npx @nti/ci-scripts@micro ci"
               }
           }
            stage("Run") {
                if(params.createTag != '' || "${BRANCH_NAME}" == "master") {
                    sh "npx @nti/ci-scripts@micro publish"   
                }
                else {
                    //sh "npm pack"
                    //sh "rm ./*.tgz"
                    sh "npx @nti/ci-scripts@micro pack"
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
