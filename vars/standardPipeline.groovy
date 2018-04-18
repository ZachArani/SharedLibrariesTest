def call(body) {

    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    node {
      try {
        stage ('Clone') {
            checkout scm
        }
        stage ('Tests') {
            parallel 'Version': {
                if(createTag != params.createTag)
                    sh "echo 'version stuff'"
            },
            'Snapshot': {
                if(params.buildAsSnapshot == true)          
                    sh 'echo snapshot'
             },
             'Test': {
                 if(params.buildAsSnapshot == false && params.createTag == '')
                     sh "echo 'test'"
              }
           }
           stage ('Done') {
             sh "echo 'done I guess'"
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
