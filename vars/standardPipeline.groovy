def call(body) {

    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    node {
        try {
            stage ('Clean Workspace') {
                //sh "rm -r node_modules"
                //sh "rm -r reports"
                sh "echo 'remove node_modules and reports'"
            }
            if(params.createTag != '') {
                stage ('Prepare') {
                    sh "echo 'do something here'" 
                }
           }
           stage ('Install') {
             //sh "npm install"
             sh "echo 'install'"
           }
            stage("Run") {
                if(params.createTag != '') {
                    //sh "npm publish"   
                    sh "echo 'publish'"
                    sh "printenv";
                }
                else {
                    //sh "npm pack"
                    //sh "rm ./*.tgz"
                    sh "echo 'pack and delete'"
                    sh "printenv";
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
