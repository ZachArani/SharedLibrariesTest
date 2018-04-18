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
                sh "echo 'version stuff'"
            },
            'Snapshot': {
                sh "echo 'snapshot'"
             },
             'Test': {
                sh "echo 'test'"
              }
           }
           stage ('Done') {
             sh "echo 'We're done I guess'"
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
