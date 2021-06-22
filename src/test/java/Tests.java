import be.ugent.IfcSpfReader;
import org.apache.jena.graph.Graph;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;

public class Tests {
    @Test
    public void test() throws IOException {
        IfcSpfReader reader = new IfcSpfReader();
        // URL resource =
        // getClass().getResource("/showfiles/Barcelona_Pavilion.ifc");
        File file = new File("C:\\Users\\fsuda\\Desktop\\IFC Autobahn\\ABM_ARCH.ifc");
        File ofile = new File("C:\\Users\\fsuda\\Desktop\\IFC Autobahn\\ABM_ARCH.ttl");
        reader.setup(file.getAbsolutePath());
        reader.convert(file.getAbsolutePath(), ofile.getAbsolutePath(), "http://linkedbuildingdata.net/ifc/resources/");
    }
}
