require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
    s.name         = 'react-native-sound-player'
    s.version        = package['version']
    s.summary        = package['description']
    s.description    = package['description']
    s.license        = package['license']
    s.author         = package['author']
    s.homepage       = package['homepage']
    s.source         = { git: '' }

    s.requires_arc   = true
    s.platform       = :ios, '10.0'
    s.libraries      = 'resolv'
    s.source_files   = 'ios/**/*'

    s.dependency 'React'
end
