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
                       sh 'rm -f Podfile.lock'
	               sh 'rm -rfv Pods'
		 //      sh "rm -rf ${pipelineParams.schemeName}.xcworkspace"
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
	     	        sh "/Applications/${pipelineParams.xcodeVersion}.app/Contents/Developer/usr/bin/xcrun xcodebuild -scheme ${pipelineParams.schemeName} -workspace ${pipelineParams.schemeName}.xcworkspace -configuration ${pipelineParams.config} -archivePath $HOME/Documents/Build/${pipelineParams.schemeName}/${pipelineParams.schemeName}.xcarchive -derivedDataPath $HOME/Library/Developer/Xcode/DerivedData CODE_SIGN_IDENTITY='${pipelineParams.codeSigningIdentity}' PROVISIONING_PROFILE_SPECIFIER='${pipelineParams.provisioningProfileSpecifier}' archive"
               }
            } 
	    
	   stage('Export IPA') {
                steps {
			writeFile file: 'exportOptions.plist', text: '<?xml version="1.0" encoding="UTF-8"?>\n<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">\n<plist version="1.0">\n<dict>\n<key>method</key>\n<string>same-as-archive</string>\n<key>teamID</key>\n<string>$team</string>\n<key>uploadSymbols</key>\n<true/>\n<key>uploadBitcode</key>\n<false/>\n<key>signingCertificate</key>\n<string>$codeSigningIdentity</string>\n<key>provisioningProfiles</key>\n<dict>\n<key>$BUNDLEID</key>\n<string>$provisioningProfile</string>\n</dict>\n</dict>\n</plist>'
			sh 'cat exportOptions.plist'
		       // sh "/Applications/${pipelineParams.xcodeVersion}.app/Contents/Developer/usr/bin/xcrun xcodebuild -exportArchive -archivePath $HOME/Documents/Build/${pipelineParams.schemeName}/${pipelineParams.schemeName}.xcarchive -exportPath $HOME/Documents/Build/${pipelineParams.schemeName} -exportOptionsPlist exportOptions.plist"
			//sh "zip $HOME/Documents/Build/${pipelineParams.schemeName}/$tag/${pipelineParams.schemeName}.xcarchive.zip $HOME/Documents/Build/${pipelineParams.schemeName}/${pipelineParams.schemeName}.xcarchive"
			//sh "mv $HOME/Documents/Build/${pipelineParams.schemeName}/${pipelineParams.schemeName}.ipa $HOME/Documents/Build/$schemeName/$tag/${pipelineParams.schemeName}.ipa"
               }
            } 	
        } 
       
    }
}
