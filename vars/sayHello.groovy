def call(Map pipelineParams) {

    pipeline {
        agent any
        stages {
            stage('checkout git') {
                steps {
                    git branch: pipelineParams.branch, credentialsId: 'git', url: pipelineParams.scmUrl
                }
            }

            stage('Pod install') {
                steps {
                        sh 'rm -rf Podfile.lock'
	                sh 'rm -rf Pods'
			sh "rm -rf ${pipelineParams.schemeName}.xcworkspace"
	                echo "Installing pods"
			sh "/usr/local/bin/pod _${pipelineParams.podVersion}_ install" 
                }
            }

            stage('Unlock Keychain') {
                steps {
                        sh 'security unlock-keychain -p PASSWORD ${HOME}/Library/Keychains/login.keychain'
                        sh 'security set-keychain-settings -t 3600 -l ${HOME}/Library/Keychains/login.keychain'
                }
            }
		
            stage('Build archive') {
                steps {
		        sh "/Applications/${pipelineParams.xcodeVersion}.app/Contents/Developer/usr/bin/xcrun xcodebuild -scheme ${pipelineParams.schemeName} -workspace ${pipelineParams.schemeName}.xcworkspace -configuration ${pipelineParams.config} clean"
	     	        sh "/Applications/${pipelineParams.xcodeVersion}.app/Contents/Developer/usr/bin/xcrun xcodebuild -scheme ${pipelineParams.schemeName} -workspace ${pipelineParams.schemeName}.xcworkspace -configuration ${pipelineParams.config} CODE_SIGN_IDENTITY=${pipelineParams.codeSigningIdentity} PROVISIONING_PROFILE_SPECIFIER=${pipelineParams.provisioningProfileSpecifier} RUN_SWIFT_LINT=FALSE archive"
               }
            }    
        } 
       
    }
}
