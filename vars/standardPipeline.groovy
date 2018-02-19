def call(body) {

    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    node {
        // Clean workspace before doing anything
        deleteDir()

        try {
            stage ('Clone') {
            checkout scm
        }
        stage('Build') {
      parallel {
        stage('Version') {
          when {
            not {
                environment name: '${params.createTag}', value: ''
            }
            
          }
          steps {
            echo "${params.createTag}"
          }
        }
        stage('Snapshot') {
          when {
            anyOf {
                environment name: '${params.buildAsSnapshot}', value: 'true'
              not {
                  environment name: '${params.head}', value: ''
              }
              
            }
            
          }
          steps {
            echo "${params.head}"
          }
        }
        stage('Test') {
          when {
            allOf {
                environment name: '${params.createTag}', value: ''
                environment name: '${params.head}', value: ''
            }
            
          }
          steps {
            echo 'Test'
          }
        }
      }
}
        } catch (err) {
            currentBuild.result = 'FAILED'
            throw err
        }
    }
}
