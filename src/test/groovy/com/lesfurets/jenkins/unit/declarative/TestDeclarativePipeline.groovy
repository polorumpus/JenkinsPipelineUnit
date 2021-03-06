package com.lesfurets.jenkins.unit.declarative

import static org.assertj.core.api.Assertions.*

import org.junit.Before
import org.junit.Test

class TestDeclarativePipeline extends DeclarativePipelineTest {

    @Before
    @Override
    void setUp() throws Exception {
        scriptRoots = ['src/test/jenkins/jenkinsfiles']
        scriptExtension = ''
        super.setUp()
    }

    @Test void jenkinsfile_success() throws Exception {
        def script = runScript('Declarative_Jenkinsfile')
        printCallStack()
        assertCallStackContains('pipeline unit tests PASSED')
        assertCallStackContains('pipeline unit tests completed')
        assertJobStatusSuccess()
    }

    @Test void jenkinsfile_failure() throws Exception {
        helper.registerAllowedMethod('sh', [String.class], { String cmd ->
            updateBuildStatus('FAILURE')
        })
        runScript('Declarative_Jenkinsfile')
        printCallStack()
        assertJobStatusFailure()
        assertCallStack()
        assertCallStack().contains('pipeline unit tests FAILED')
        assertCallStackContains('pipeline unit tests completed')
    }

    @Test void jenkinsfile_aborted() throws Exception {
        helper.registerAllowedMethod('sh', [String.class], { String cmd ->
            updateBuildStatus('ABORTED')
        })
        runScript('Declarative_Jenkinsfile')
        printCallStack()
        assertJobStatusAborted()
        assertCallStack()
        assertCallStack().contains('pipeline unit tests ABORTED')
        assertCallStackContains('pipeline unit tests completed')
    }

    @Test void should_params() throws Exception {
        runScript('Params_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Hello Mr Jenkins')
        assertJobStatusSuccess()
    }

    @Test void when_branch() throws Exception {
        addEnvVar('BRANCH_NAME', 'production')
        runScript('Branch_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Deploying')
        assertJobStatusSuccess()
    }

    @Test void when_branch_not() throws Exception {
        addEnvVar('BRANCH_NAME', 'master')
        runScript('Branch_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example Deploy')
        assertJobStatusSuccess()
    }

    @Test void when_tag() throws Exception {
        addEnvVar('TAG_NAME', 'v1.1.1')
        runScript('Tag_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Deploying')
        assertJobStatusSuccess()
    }

    @Test void when_tag_not() throws Exception {
        addEnvVar('TAG_NAME', 'someothertag')
        runScript('Tag_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example Deploy')
        assertJobStatusSuccess()
    }

    @Test void should_agent() throws Exception {
        runScript('Agent_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('openjdk:8-jre')
        assertCallStack().contains('maven:3-alpine')
        assertJobStatusSuccess()
    }

    @Test void should_credentials() throws Exception {
        addCredential('my-prefined-secret-text', 'something_secret')
        runScript('Credentials_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('AN_ACCESS_KEY:something_secret')
        assertJobStatusSuccess()
    }

    @Test void should_parallel() throws Exception {
        runScript('Parallel_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('sh(run-tests.exe)')
        assertCallStack().contains('sh(run-tests.sh)')
        assertJobStatusSuccess()
    }

    @Test void should_sub_stages() throws Exception {
        runScript('ComplexStages_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('mvn build')
        assertCallStack().contains('mvn --version')
        assertCallStack().contains('java -version')
        assertJobStatusSuccess()
    }

    @Test void should_environment() throws Exception {
        runScript('Environment_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('echo(LEVAR1 LE NEW VALUE)')
        assertCallStack().contains('echo(LEVAR2 A COPY OF LE NEW VALUE in build#1)')
        assertJobStatusSuccess()
    }

    @Test(expected = MissingPropertyException)
    void should_non_valid_fail() throws Exception {
        try {
            runScript('Non_Valid_Jenkinsfile')
        } catch (e) {
            e.printStackTrace()
            throw e
        } finally {
            printCallStack()
        }
    }
}
