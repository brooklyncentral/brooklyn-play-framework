package brooklyn.web.play

import groovy.transform.InheritConstructors

import org.jclouds.compute.domain.ExecResponse
import org.jclouds.scriptbuilder.statements.java.InstallJDK

import brooklyn.entity.basic.AbstractSoftwareProcessSshDriver
import brooklyn.entity.basic.Attributes
import brooklyn.entity.basic.SoftwareProcessDriver
import brooklyn.entity.basic.SoftwareProcessEntity
import brooklyn.entity.basic.lifecycle.CommonCommands
import brooklyn.entity.webapp.RootUrl
import brooklyn.event.adapter.FunctionSensorAdapter
import brooklyn.event.basic.BasicAttributeSensor
import brooklyn.event.basic.BasicConfigKey
import brooklyn.event.basic.PortAttributeSensorAndConfigKey
import brooklyn.location.basic.jclouds.JcloudsLocation.JcloudsSshMachineLocation
import brooklyn.util.ResourceUtils
import brooklyn.util.flags.SetFromFlag

@InheritConstructors
class PlayAppInstance extends SoftwareProcessEntity {

    @SetFromFlag("httpPort")
    public static final PortAttributeSensorAndConfigKey HTTP_PORT = [ Attributes.HTTP_PORT, "9000+" ];
    
    public static BasicConfigKey<String> PLAY_APP_DIST_ZIP_URL = [ String, "playframework.app.dist.zip.url", "URL of the ZIP of the play application dist" ];
    public static BasicConfigKey<String> COMMAND_LINE_TO_START = [ String, "playframework.app.cli.start", "CLI to start app, relative to dir where ZIP isunpacked, e.g. /myapp/bin/start -Darg1"];
    
    public static final BasicAttributeSensor<String> ROOT_URL = RootUrl.ROOT_URL;
    
    
    protected void connectSensors() {
        setAttribute(ROOT_URL, "http://"+getAttribute(HOSTNAME)+":"+getAttribute(HTTP_PORT)+"/");
        
        // TODO repeatedly polling http port would be a better test
        FunctionSensorAdapter f = new FunctionSensorAdapter({ getDriver()?.isRunning() });
        sensorRegistry.register(f);
        f.poll(SERVICE_UP);

        // TODO get other metrics from play Netty server ?
    }

    @Override
    public Class getDriverInterface() {
        return PlayAppInstanceDriver;
    }

    public String getCommandLine() {
        return "nohup "+getConfig(PlayAppInstance.COMMAND_LINE_TO_START)+
            " -Dhttp.port="+getAttribute(PlayAppInstance.HTTP_PORT)+
            getExtraCommandLineArgs()+
             " > out.txt 2> err.txt < /dev/null &"
    }
    protected String getExtraCommandLineArgs() {
        return "";
    }
    
}

interface PlayAppInstanceDriver extends SoftwareProcessDriver {
}

@InheritConstructors
class PlayAppInstanceSshDriver extends AbstractSoftwareProcessSshDriver implements PlayAppInstanceDriver {

    @Override
    public void install() {
        if (machine in JcloudsSshMachineLocation) {
            // some cloud machines need non-tty sudo access
            newScript("disable requiretty").
                    setFlag("allocatePTY", true).
                    body.append(CommonCommands.sudo("bash -c 'sed -i s/.*requiretty.*/#brooklyn-removed-require-tty/ /etc/sudoers'")).
                    execute();
            // and we need Java...
            ExecResponse result = ((JcloudsSshMachineLocation)machine).submitRunScript(InstallJDK.fromOpenJDK()).get();
        }
        
        newScript("installing-play-1").
            updateTaskAndFailOnNonZeroResultCode().
            body.append(
                CommonCommands.installExecutable("unzip"),
                "mkdir -p "+getRunDir()
            ).
            execute();
        getMachine().copyTo(new ResourceUtils(this).getResourceFromUrl(getEntity().getConfig(PlayAppInstance.PLAY_APP_DIST_ZIP_URL)), 
            getRunDir()+"/"+"app.zip");
        newScript("installing-play-2").
            updateTaskAndFailOnNonZeroResultCode().
            body.append(
                "cd "+getRunDir(), 
                "unzip app.zip",
                "chmod +x "+getEntity().getConfig(PlayAppInstance.COMMAND_LINE_TO_START).split(" ")[0]).
            execute();
    }

    @Override
    public void customize() {
    }

    @Override
    public void launch() {
        newScript(LAUNCHING, usePidFile: true).
            updateTaskAndFailOnNonZeroResultCode().
            body.append(
                getEntity().getCommandLine()
            ).
            execute();
    }
 
    @Override
    public boolean isRunning() {
        return newScript(CHECK_RUNNING, usePidFile: true).execute() == 0;
    }

    @Override
    public void stop() {
        newScript(STOPPING, usePidFile: true).execute();
    }

}