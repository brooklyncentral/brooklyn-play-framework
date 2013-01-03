package brooklyn.web.play.demo

import brooklyn.entity.Entity
import brooklyn.entity.basic.AbstractApplication
import brooklyn.entity.basic.BasicConfigurableEntityFactory
import brooklyn.entity.basic.Entities
import brooklyn.entity.webapp.ControlledDynamicWebAppCluster
import brooklyn.launcher.BrooklynLauncher
import brooklyn.launcher.BrooklynServerDetails
import brooklyn.location.Location
import brooklyn.web.play.PlayAppInstance

class OnePlayApp {

    public static final String DEFAULT_LOCATION =
        "localhost";
//        "aws-ec2:us-east-1";
    
    public static void main(String[] args) {
        AbstractApplication app = new AbstractApplication(name: "Hello World Application") {
            Entity play = new PlayAppInstance(this);
        };
        app.setConfig(PlayAppInstance.PLAY_APP_DIST_ZIP_URL, "classpath://brooklyn/web/play/demo/helloworld-1.0.zip");
        app.setConfig(PlayAppInstance.COMMAND_LINE_TO_START, "helloworld-1.0/start");
        
        BrooklynServerDetails server = BrooklynLauncher.newLauncher().managing(app).launch();
        
        List<Location> locations = server.getManagementContext().getLocationRegistry().resolve(args ?: [DEFAULT_LOCATION])
        app.start(locations)
        Entities.dumpInfo(app)
    }
    
}
