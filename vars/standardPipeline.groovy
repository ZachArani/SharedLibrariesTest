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
        stage ('Build') {
            sh "echo 'building ${buildAsSnapshot} ...'"
        }
        stage ('Tests') {
            parallel 'static': {
                sh "echo 'shell scripts to run static tests...'"
            },
            'unit': {
                sh "echo 'shell scripts to run unit tests...'"
             },
             'integration': {
                sh "echo 'shell scripts to run integration tests...'"
              }
           }
           stage ('Deploy') {
             sh "echo 'deploying to server ${createTag}...'"
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
