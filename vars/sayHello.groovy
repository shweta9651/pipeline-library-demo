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
			sh 'rm -rf 'pipelineParams.schemeName'.xcworkspace'
	                sh 'echo "Installing pods"'
			sh 'pod _${pipelineParams[podVersion]}_ install' 
	                sh 'archive'
                }
            }

            // stage ('Build archive') {
            //     steps {
            //         sh '/Applications/pipelineParams.xcodeVersion.app/Contents/Developer/usr/bin/xcrun xcodebuild -scheme $schemeName -workspace ${schemeName}.xcworkspace -configuration $config clean'
		          //   sh '(/Applications/${xcodeVersion}.app/Contents/Developer/usr/bin/xcrun xcodebuild -scheme $schemeName -workspace ${schemeName}.xcworkspace -archivePath $HOME/Documents/Build/$schemeName/$tag/${schemeName}.xcarchive -configuration $config CODE_SIGN_IDENTITY="${codeSigningIdentity}" PROVISIONING_PROFILE_SPECIFIER="${provisioningProfileSpecifier}" RUN_SWIFT_LINT=FALSE archive) | tee archive.log'
            //     }
            // }   
        }
       
    }
}
